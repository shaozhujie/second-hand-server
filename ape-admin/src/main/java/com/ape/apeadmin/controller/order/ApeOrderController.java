package com.ape.apeadmin.controller.order;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeBlack;
import com.ape.apesystem.domain.ApeCommodity;
import com.ape.apesystem.domain.ApeOrder;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeBlackService;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeOrderService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 订单controller
 * @date 2024/02/20 02:08
 */
@Controller
@ResponseBody
@RequestMapping("order")
public class ApeOrderController {

    @Autowired
    private ApeOrderService apeOrderService;
    @Autowired
    private ApeCommodityService apeCommodityService;
    @Autowired
    private ApeBlackService apeBlackService;
    @Autowired
    private ApeUserService apeUserService;

    /** 分页获取订单 */
    @Log(name = "分页获取订单", type = BusinessType.OTHER)
    @PostMapping("getApeOrderPage")
    public Result getApeOrderPage(@RequestBody ApeOrder apeOrder) {
        Page<ApeOrder> page = new Page<>(apeOrder.getPageNumber(),apeOrder.getPageSize());
        QueryWrapper<ApeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeOrder.getCommodityName()),ApeOrder::getCommodityName,apeOrder.getCommodityName())
                .like(StringUtils.isNotBlank(apeOrder.getExpressName()),ApeOrder::getExpressName,apeOrder.getExpressName())
                .like(StringUtils.isNotBlank(apeOrder.getExpressOrder()),ApeOrder::getExpressOrder,apeOrder.getExpressOrder())
                .eq(apeOrder.getUnitPrice() != null,ApeOrder::getUnitPrice,apeOrder.getUnitPrice())
                .eq(apeOrder.getTotalPrice() != null,ApeOrder::getTotalPrice,apeOrder.getTotalPrice())
                .eq(apeOrder.getNum() != null,ApeOrder::getNum,apeOrder.getNum())
                .eq(StringUtils.isNotBlank(apeOrder.getType()),ApeOrder::getType,apeOrder.getType())
                .eq(apeOrder.getState()!=null,ApeOrder::getState,apeOrder.getState())
                .eq(StringUtils.isNotBlank(apeOrder.getCreateBy()),ApeOrder::getCreateBy,apeOrder.getCreateBy());
        Page<ApeOrder> apeOrderPage = apeOrderService.page(page, queryWrapper);
        return Result.success(apeOrderPage);
    }

    @GetMapping("getApeOrderList")
    public Result getApeOrderList(@RequestParam("userId")String userId) {
        QueryWrapper<ApeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeOrder::getFromId,userId).orderByDesc(ApeOrder::getCreateTime);
        List<ApeOrder> orderList = apeOrderService.list(queryWrapper);
        return Result.success(orderList);
    }

    @GetMapping("getApeOrderListByUserId")
    public Result getApeOrderListByUserId(@RequestParam("userId")String userId) {
        QueryWrapper<ApeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeOrder::getUserId,userId).orderByDesc(ApeOrder::getCreateTime);
        List<ApeOrder> orderList = apeOrderService.list(queryWrapper);
        return Result.success(orderList);
    }

    /** 根据id获取订单 */
    @Log(name = "根据id获取订单", type = BusinessType.OTHER)
    @GetMapping("getApeOrderById")
    public Result getApeOrderById(@RequestParam("id")String id) {
        ApeOrder apeOrder = apeOrderService.getById(id);
        ApeUser user = apeUserService.getById(apeOrder.getFromId());
        if (user != null) {
            apeOrder.setFromName(user.getUserName());
        } else {
            apeOrder.setFromName("");
        }
        return Result.success(apeOrder);
    }

    /** 保存订单 */
    @Log(name = "保存订单", type = BusinessType.INSERT)
    @PostMapping("saveApeOrder")
    @Transactional(rollbackFor = Exception.class)
    public Result saveApeOrder(@RequestBody ApeOrder apeOrder) {
        //对一下库存
        ApeCommodity apeCommodity = apeCommodityService.getById(apeOrder.getCommodityId());
        if (apeCommodity.getState() == 1) {
            return Result.fail("商品已下架，无法下单");
        }
        ApeUser apeUser = ShiroUtils.getUserInfo();
        QueryWrapper<ApeBlack> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeBlack::getUserId,apeCommodity.getUserId())
                .eq(ApeBlack::getFromId,apeUser.getId());
        int count = apeBlackService.count(queryWrapper);
        if (count > 0) {
            return Result.fail("您已被卖家加入黑名单");
        }
        apeOrder.setFromId(apeCommodity.getUserId());
        if ((apeCommodity.getNum() - apeOrder.getNum()) < 0) {
            return Result.fail("库存不足，无法下单");
        }
        //更新库存
        apeCommodity.setNum(apeCommodity.getNum() - apeOrder.getNum());
        if ((apeCommodity.getNum() - apeOrder.getNum()) == 0) {
            apeCommodity.setState(1);
        }
        apeCommodityService.updateById(apeCommodity);
        boolean save = apeOrderService.save(apeOrder);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑订单 */
    @Log(name = "编辑订单", type = BusinessType.UPDATE)
    @PostMapping("editApeOrder")
    @Transactional(rollbackFor = Exception.class)
    public Result editApeOrder(@RequestBody ApeOrder apeOrder) {
        ApeOrder order = apeOrderService.getById(apeOrder.getId());
        if (apeOrder.getState() == 2) {
            if (order.getState() == 2 || order.getState() != 1) {
                return Result.fail();
            }
        }
        if (apeOrder.getState() == 1) {
            if (order.getState() == 1 || order.getState() != 0) {
                return Result.fail();
            }
        }
        if (apeOrder.getState() == 6) {
            if (order.getState() == 6 || order.getState() != 0) {
                return Result.fail();
            }
        }
        if (apeOrder.getState() == 3) {
            if (order.getState() == 3 || !(order.getState() == 1 || order.getState() == 2)) {
                return Result.fail();
            }
        }
        boolean save = apeOrderService.updateById(apeOrder);
        if (apeOrder.getState() == 6) {
            //回库存
            ApeCommodity apeCommodity = apeCommodityService.getById(order.getCommodityId());
            apeCommodity.setNum(apeCommodity.getNum() + order.getNum());
            apeCommodityService.updateById(apeCommodity);
        }
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除订单 */
    @GetMapping("removeApeOrder")
    @Log(name = "删除订单", type = BusinessType.DELETE)
    public Result removeApeOrder(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeOrderService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("订单id不能为空！");
        }
    }

    @GetMapping("getOldOrderCount")
    public Result getOldOrderCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date time = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(time);
        String start = format + " 00:00:00";
        String end = format + " 23:59:59";
        QueryWrapper<ApeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ge(ApeOrder::getCreateTime,start)
                .eq(ApeOrder::getCreateTime,end);
        int count = apeOrderService.count(queryWrapper);
        return Result.success(count);
    }

    @GetMapping("getIndexChart")
    public Result getIndexChart() {
        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<Object> nums = new ArrayList<>();
        IntStream.rangeClosed(1, 15)
                .mapToObj(days -> today.minusDays(days))
                .forEach(date -> dates.add(date.format(DateTimeFormatter.ISO_DATE)));
        for (String s : dates) {
            float num = 0;
            QueryWrapper<ApeOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().like(ApeOrder::getCreateTime,s);
            List<ApeOrder> orderList = apeOrderService.list(queryWrapper);
            for (ApeOrder order : orderList) {
                num += order.getTotalPrice();
            }
            nums.add(num);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dates",dates);
        jsonObject.put("nums",nums);
        return Result.success(jsonObject);
    }

}
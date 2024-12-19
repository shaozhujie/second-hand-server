package com.ape.apeadmin.controller.consulting;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeCommodity;
import com.ape.apesystem.domain.ApeConsulting;
import com.ape.apesystem.domain.ApeConsultingItem;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeConsultingItemService;
import com.ape.apesystem.service.ApeConsultingService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 用户咨询controller
 * @date 2024/07/01 05:01
 */
@Controller
@ResponseBody
@RequestMapping("consulting")
public class ApeConsultingController {

    @Autowired
    private ApeConsultingService apeConsultingService;
    @Autowired
    private ApeCommodityService apeCommodityService;
    @Autowired
    private ApeConsultingItemService apeConsultingItemService;
    @Autowired
    private ApeUserService apeUserService;

    /** 分页获取用户咨询 */
    @Log(name = "分页获取用户咨询", type = BusinessType.OTHER)
    @PostMapping("getApeConsultingPage")
    public Result getApeConsultingPage(@RequestBody ApeConsulting apeConsulting) {
        ApeUser apeUser = ShiroUtils.getUserInfo();
        Page<ApeConsulting> page = new Page<>(apeConsulting.getPageNumber(),apeConsulting.getPageSize());
        QueryWrapper<ApeConsulting> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ApeConsulting::getUserId,apeUser.getId())
                .or()
                .eq(ApeConsulting::getFromId,apeUser.getId());
        Page<ApeConsulting> apeConsultingPage = apeConsultingService.page(page, queryWrapper);
        for (ApeConsulting consulting : apeConsultingPage.getRecords()) {
            ApeUser from = apeUserService.getById(consulting.getFromId());
            consulting.setFromImage(from.getAvatar());
            QueryWrapper<ApeConsultingItem> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeConsultingItem::getConsultingId,consulting.getId()).orderByDesc(ApeConsultingItem::getCreateTime);
            List<ApeConsultingItem> itemList = apeConsultingItemService.list(wrapper);
            consulting.setApeConsultingItemList(itemList);
        }
        return Result.success(apeConsultingPage);
    }

    /** 根据id获取用户咨询 */
    @Log(name = "根据id获取用户咨询", type = BusinessType.OTHER)
    @GetMapping("getApeConsultingById")
    public Result getApeConsultingById(@RequestParam("id")String id) {
        ApeConsulting apeConsulting = apeConsultingService.getById(id);
        return Result.success(apeConsulting);
    }

    /** 保存用户咨询 */
    @Log(name = "保存用户咨询", type = BusinessType.INSERT)
    @PostMapping("saveApeConsulting")
    @Transactional(rollbackFor = Exception.class)
    public Result saveApeConsulting(@RequestBody ApeConsulting apeConsulting) {
        //查一下有没有咨询过
        QueryWrapper<ApeConsulting> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeConsulting::getCommodityId,apeConsulting.getCommodityId())
                .eq(ApeConsulting::getFromId,apeConsulting.getFromId()).last("limit 1");
        ApeConsulting one = apeConsultingService.getOne(queryWrapper);
        ApeCommodity commodity1 = apeCommodityService.getById(apeConsulting.getCommodityId());
        if (commodity1.getState() == 1) {
            return Result.fail("商品已下架,暂无法联系卖家");
        }
        if (one != null) {
            return Result.success(one);
        } else {
            String idStr = IdWorker.getIdStr();
            apeConsulting.setId(idStr);
            ApeCommodity commodity = apeCommodityService.getById(apeConsulting.getCommodityId());
            apeConsulting.setCommodityName(commodity.getName());
            apeConsulting.setUserId(commodity.getUserId());
            boolean save = apeConsultingService.save(apeConsulting);
            ApeCommodity apeCommodity = apeCommodityService.getById(commodity.getId());
            apeCommodity.setLook(apeCommodity.getWant() + 1);
            apeCommodityService.updateById(apeCommodity);
            if (save) {
                return Result.success(apeConsulting);
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        }
    }

    /** 编辑用户咨询 */
    @Log(name = "编辑用户咨询", type = BusinessType.UPDATE)
    @PostMapping("editApeConsulting")
    public Result editApeConsulting(@RequestBody ApeConsulting apeConsulting) {
        boolean save = apeConsultingService.updateById(apeConsulting);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除用户咨询 */
    @GetMapping("removeApeConsulting")
    @Transactional(rollbackFor = Exception.class)
    @Log(name = "删除用户咨询", type = BusinessType.DELETE)
    public Result removeApeConsulting(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeConsultingService.removeById(id);
                QueryWrapper<ApeConsultingItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ApeConsultingItem::getConsultingId,id);
                apeConsultingItemService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("用户咨询id不能为空！");
        }
    }

}
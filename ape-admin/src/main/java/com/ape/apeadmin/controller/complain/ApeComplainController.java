package com.ape.apeadmin.controller.complain;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ResponseBody
@RequestMapping("complain")
public class ApeComplainController {

    @Autowired
    private ApeComplainService apeComplainService;
    @Autowired
    private ApeCommodityService apeCommodityService;
    @Autowired
    private ApeOrderService apeOrderService;
    @Autowired
    private ApeUserService apeUserService;

    @PostMapping("getApeComplainPage")
    public Result getApeCityPage(@RequestBody ApeComplain apeComplain) {
        Page<ApeComplain> page = new Page<>(apeComplain.getPageNumber(),apeComplain.getPageSize());
        QueryWrapper<ApeComplain> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeComplain.getContent()),ApeComplain::getContent,apeComplain.getContent())
                .like(StringUtils.isNotBlank(apeComplain.getFromName()),ApeComplain::getFromName,apeComplain.getFromName());
        Page<ApeComplain> apeComplainPage = apeComplainService.page(page, queryWrapper);
        return Result.success(apeComplainPage);
    }

    @GetMapping("getApeComplainList")
    public Result getApeComplainList() {
        ApeUser apeUser = ShiroUtils.getUserInfo();
        QueryWrapper<ApeComplain> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeComplain::getUserId,apeUser.getId());
        List<ApeComplain> list = apeComplainService.list(queryWrapper);
        return Result.success(list);
    }

    @GetMapping("getApeComplainById")
    public Result getApeComplainById(@RequestParam("id")String id) {
        ApeComplain apeComplain = apeComplainService.getById(id);
        return Result.success(apeComplain);
    }

    @PostMapping("saveComplain")
    public Result saveComplain(@RequestBody ApeComplain apeComplain) {
        ApeUser apeUser = ShiroUtils.getUserInfo();
        apeComplain.setUserId(apeUser.getId());
        ApeOrder apeOrder = apeOrderService.getById(apeComplain.getOrderNumber());
        ApeCommodity apeCommodity = apeCommodityService.getById(apeOrder.getCommodityId());
        apeComplain.setName(apeCommodity.getName());
        ApeUser user = apeUserService.getById(apeCommodity.getUserId());
        apeComplain.setFromName(user.getUserName());
        boolean save = apeComplainService.save(apeComplain);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    @PostMapping("editApeComplain")
    public Result editApeComplain(@RequestBody ApeComplain apeComplain) {
        boolean save = apeComplainService.updateById(apeComplain);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    @GetMapping("removeApeComplain")
    public Result removeApeComplain(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeComplainService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("id不能为空！");
        }
    }

}

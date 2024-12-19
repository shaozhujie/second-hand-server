package com.ape.apeadmin.controller.address;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeAddress;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * @description: 收货地址controller
 * @date 2024/06/25 10:30
 */
@Controller
@ResponseBody
@RequestMapping("address")
public class ApeAddressController {

    @Autowired
    private ApeAddressService apeAddressService;

    /** 分页获取收货地址 */
    @Log(name = "分页获取收货地址", type = BusinessType.OTHER)
    @PostMapping("getApeAddressList")
    public Result getApeAddressList(@RequestBody ApeAddress apeAddress) {
        QueryWrapper<ApeAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeAddress.getUserId()),ApeAddress::getUserId,apeAddress.getUserId())
                .eq(StringUtils.isNotBlank(apeAddress.getName()),ApeAddress::getName,apeAddress.getName())
                .eq(StringUtils.isNotBlank(apeAddress.getTel()),ApeAddress::getTel,apeAddress.getTel())
                .eq(StringUtils.isNotBlank(apeAddress.getAddress()),ApeAddress::getAddress,apeAddress.getAddress());
        List<ApeAddress> apeAddressPage = apeAddressService.list(queryWrapper);
        return Result.success(apeAddressPage);
    }

    /** 根据id获取收货地址 */
    @Log(name = "根据id获取收货地址", type = BusinessType.OTHER)
    @GetMapping("getApeAddressById")
    public Result getApeAddressById(@RequestParam("id")String id) {
        ApeAddress apeAddress = apeAddressService.getById(id);
        return Result.success(apeAddress);
    }

    /** 保存收货地址 */
    @Log(name = "保存收货地址", type = BusinessType.INSERT)
    @PostMapping("saveApeAddress")
    public Result saveApeAddress(@RequestBody ApeAddress apeAddress) {
        ApeUser user = ShiroUtils.getUserInfo();
        apeAddress.setUserId(user.getId());
        boolean save;
        if (StringUtils.isNotBlank(apeAddress.getId())) {
            save = apeAddressService.updateById(apeAddress);
        } else {
            save = apeAddressService.save(apeAddress);
        }
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑收货地址 */
    @Log(name = "编辑收货地址", type = BusinessType.UPDATE)
    @PostMapping("editApeAddress")
    public Result editApeAddress(@RequestBody ApeAddress apeAddress) {
        boolean save = apeAddressService.updateById(apeAddress);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除收货地址 */
    @GetMapping("removeApeAddress")
    @Log(name = "删除收货地址", type = BusinessType.DELETE)
    public Result removeApeAddress(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeAddressService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("收货地址id不能为空！");
        }
    }

}

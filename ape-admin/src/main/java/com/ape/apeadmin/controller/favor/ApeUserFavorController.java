package com.ape.apeadmin.controller.favor;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeCommodity;
import com.ape.apesystem.domain.ApeRecord;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.domain.ApeUserFavor;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeUserFavorService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 收藏controller
 * @date 2024/07/01 10:46
 */
@Controller
@ResponseBody
@RequestMapping("favor")
public class ApeUserFavorController {

    @Autowired
    private ApeUserFavorService apeUserFavorService;
    @Autowired
    private ApeCommodityService apeCommodityService;

    /** 分页获取收藏 */
    @Log(name = "分页获取收藏", type = BusinessType.OTHER)
    @PostMapping("getApeUserFavorPage")
    public Result getApeUserFavorPage(@RequestBody ApeUserFavor apeUserFavor) {
        Page<ApeUserFavor> page = new Page<>(apeUserFavor.getPageNumber(),apeUserFavor.getPageSize());
        QueryWrapper<ApeUserFavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeUserFavor.getCommodityId()),ApeUserFavor::getCommodityId,apeUserFavor.getCommodityId())
                .like(StringUtils.isNotBlank(apeUserFavor.getName()),ApeUserFavor::getName,apeUserFavor.getName())
                .eq(StringUtils.isNotBlank(apeUserFavor.getImage()),ApeUserFavor::getImage,apeUserFavor.getImage())
                .eq(apeUserFavor.getPrice() != null,ApeUserFavor::getPrice,apeUserFavor.getPrice())
                .eq(StringUtils.isNotBlank(apeUserFavor.getUserId()),ApeUserFavor::getUserId,apeUserFavor.getUserId())
                .eq(StringUtils.isNotBlank(apeUserFavor.getUserAvatar()),ApeUserFavor::getUserAvatar,apeUserFavor.getUserAvatar())
                .eq(StringUtils.isNotBlank(apeUserFavor.getCreateBy()),ApeUserFavor::getCreateBy,apeUserFavor.getCreateBy())
                .eq(apeUserFavor.getCreateTime() != null,ApeUserFavor::getCreateTime,apeUserFavor.getCreateTime())
                .eq(StringUtils.isNotBlank(apeUserFavor.getUpdateBy()),ApeUserFavor::getUpdateBy,apeUserFavor.getUpdateBy())
                .eq(apeUserFavor.getUpdateTime() != null,ApeUserFavor::getUpdateTime,apeUserFavor.getUpdateTime())
                .eq(StringUtils.isNotBlank(apeUserFavor.getRemark()),ApeUserFavor::getRemark,apeUserFavor.getRemark());
        Page<ApeUserFavor> apeUserFavorPage = apeUserFavorService.page(page, queryWrapper);
        for (ApeUserFavor favor : apeUserFavorPage.getRecords()) {
            ApeCommodity apeCommodity = apeCommodityService.getById(favor.getCommodityId());
            favor.setImage(apeCommodity.getImage());
            favor.setPrice(apeCommodity.getPrice());
            favor.setName(apeCommodity.getName());
        }
        return Result.success(apeUserFavorPage);
    }

    /** 根据id获取收藏 */
    @Log(name = "根据id获取收藏", type = BusinessType.OTHER)
    @GetMapping("getApeUserFavorById")
    public Result getApeUserFavorById(@RequestParam("id")String id) {
        ApeUserFavor apeUserFavor = apeUserFavorService.getById(id);
        return Result.success(apeUserFavor);
    }

    @GetMapping("getUserFavor")
    public Result getUserFavor(@RequestParam("id")String id) {
        ApeUser user = ShiroUtils.getUserInfo();
        QueryWrapper<ApeUserFavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUserFavor::getCommodityId,id)
                .eq(ApeUserFavor::getUserId,user.getId());
        int count = apeUserFavorService.count(queryWrapper);
        return Result.success(count);
    }

    /** 保存收藏 */
    @Log(name = "保存收藏", type = BusinessType.INSERT)
    @PostMapping("saveApeUserFavor")
    public Result saveApeUserFavor(@RequestBody ApeUserFavor apeUserFavor) {
        ApeUser user = ShiroUtils.getUserInfo();
        apeUserFavor.setUserId(user.getId());
        QueryWrapper<ApeUserFavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUserFavor::getCommodityId,apeUserFavor.getCommodityId())
                .eq(ApeUserFavor::getUserId,user.getId());
        int count = apeUserFavorService.count(queryWrapper);
        if (count <= 0) {
            ApeCommodity commodity = apeCommodityService.getById(apeUserFavor.getCommodityId());
            apeUserFavor.setName(commodity.getName());
            apeUserFavor.setPrice(commodity.getPrice());
            apeUserFavor.setUserAvatar(commodity.getUserAvatar());
            apeUserFavor.setImage(commodity.getImage());
            boolean save = apeUserFavorService.save(apeUserFavor);
            if (save) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } else {
            return Result.success();
        }
    }

    /** 编辑收藏 */
    @Log(name = "编辑收藏", type = BusinessType.UPDATE)
    @PostMapping("editApeUserFavor")
    public Result editApeUserFavor(@RequestBody ApeUserFavor apeUserFavor) {
        boolean save = apeUserFavorService.updateById(apeUserFavor);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除收藏 */
    @GetMapping("removeApeUserFavor")
    @Log(name = "删除收藏", type = BusinessType.DELETE)
    public Result removeApeUserFavor(@RequestParam("id")String id) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (StringUtils.isNotBlank(id)) {
            QueryWrapper<ApeUserFavor> query = new QueryWrapper<>();
            query.lambda().eq(ApeUserFavor::getCommodityId,id).eq(ApeUserFavor::getUserId,user.getId());
            boolean remove = apeUserFavorService.remove(query);
            if (remove) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } else {
            return Result.fail("收藏id不能为空！");
        }
    }

}
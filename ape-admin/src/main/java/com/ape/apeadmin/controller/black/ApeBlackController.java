package com.ape.apeadmin.controller.black;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeBlack;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeBlackService;
import com.ape.apesystem.service.ApeUserService;
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
 * @description: 黑名单controller
 * @date 2024/11/28 10:19
 */
@Controller
@ResponseBody
@RequestMapping("black")
public class ApeBlackController {

    @Autowired
    private ApeBlackService apeBlackService;
    @Autowired
    private ApeUserService apeUserService;

    /** 分页获取黑名单 */
    @Log(name = "分页获取黑名单", type = BusinessType.OTHER)
    @PostMapping("getApeBlackPage")
    public Result getApeBlackPage(@RequestBody ApeBlack apeBlack) {
        QueryWrapper<ApeBlack> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeBlack.getUserId()),ApeBlack::getUserId,apeBlack.getUserId())
                .eq(StringUtils.isNotBlank(apeBlack.getFromId()),ApeBlack::getFromId,apeBlack.getFromId())
                .eq(StringUtils.isNotBlank(apeBlack.getCreateBy()),ApeBlack::getCreateBy,apeBlack.getCreateBy())
                .eq(apeBlack.getCreateTime() != null,ApeBlack::getCreateTime,apeBlack.getCreateTime())
                .eq(StringUtils.isNotBlank(apeBlack.getUpdateBy()),ApeBlack::getUpdateBy,apeBlack.getUpdateBy())
                .eq(apeBlack.getUpdateTime() != null,ApeBlack::getUpdateTime,apeBlack.getUpdateTime());
        List<ApeBlack> apeBlackPage = apeBlackService.list(queryWrapper);
        for (ApeBlack black : apeBlackPage) {
            ApeUser user = apeUserService.getById(black.getFromId());
            black.setAvatar(user.getAvatar());
            black.setUserName(user.getUserName());
        }
        return Result.success(apeBlackPage);
    }

    /** 根据id获取黑名单 */
    @Log(name = "根据id获取黑名单", type = BusinessType.OTHER)
    @GetMapping("getApeBlackById")
    public Result getApeBlackById(@RequestParam("id")String id) {
        ApeBlack apeBlack = apeBlackService.getById(id);
        return Result.success(apeBlack);
    }

    /** 保存黑名单 */
    @Log(name = "保存黑名单", type = BusinessType.INSERT)
    @PostMapping("saveApeBlack")
    public Result saveApeBlack(@RequestBody ApeBlack apeBlack) {
        apeBlack.setUserId(ShiroUtils.getUserInfo().getId());
        boolean save = apeBlackService.save(apeBlack);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑黑名单 */
    @Log(name = "编辑黑名单", type = BusinessType.UPDATE)
    @PostMapping("editApeBlack")
    public Result editApeBlack(@RequestBody ApeBlack apeBlack) {
        boolean save = apeBlackService.updateById(apeBlack);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除黑名单 */
    @GetMapping("removeApeBlack")
    @Log(name = "删除黑名单", type = BusinessType.DELETE)
    public Result removeApeBlack(@RequestParam("userId")String userId) {
        QueryWrapper<ApeBlack> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeBlack::getFromId,userId)
                .eq(ApeBlack::getUserId,ShiroUtils.getUserInfo().getId());
        boolean removed = apeBlackService.remove(queryWrapper);
        if (removed) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    @GetMapping("getUserBlackBySeller")
    public Result getUserBlackBySeller(@RequestParam("userId") String userId) {
        ApeUser apeUser = ShiroUtils.getUserInfo();
        QueryWrapper<ApeBlack> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeBlack::getUserId,apeUser.getId())
                .eq(ApeBlack::getFromId,userId).last("limit 1");
        ApeBlack black = apeBlackService.getOne(queryWrapper);
        if (black != null) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

}
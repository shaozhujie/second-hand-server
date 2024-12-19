package com.ape.apeadmin.controller.follow;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeBlack;
import com.ape.apesystem.domain.ApeFollow;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeFollowService;
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
 * @description: 关注controller
 * @date 2024/12/05 10:55
 */
@Controller
@ResponseBody
@RequestMapping("follow")
public class ApeFollowController {

    @Autowired
    private ApeFollowService apeFollowService;
    @Autowired
    private ApeUserService apeUserService;

    /** 分页获取关注 */
    @Log(name = "分页获取关注", type = BusinessType.OTHER)
    @PostMapping("getApeFollowPage")
    public Result getApeFollowPage(@RequestBody ApeFollow apeFollow) {
        QueryWrapper<ApeFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeFollow.getUserId()),ApeFollow::getUserId,apeFollow.getUserId())
                .eq(StringUtils.isNotBlank(apeFollow.getFromId()),ApeFollow::getFromId,apeFollow.getFromId())
                .eq(StringUtils.isNotBlank(apeFollow.getCreateBy()),ApeFollow::getCreateBy,apeFollow.getCreateBy())
                .eq(apeFollow.getCreateTime() != null,ApeFollow::getCreateTime,apeFollow.getCreateTime())
                .eq(StringUtils.isNotBlank(apeFollow.getUpdateBy()),ApeFollow::getUpdateBy,apeFollow.getUpdateBy())
                .eq(apeFollow.getUpdateTime() != null,ApeFollow::getUpdateTime,apeFollow.getUpdateTime());
        List<ApeFollow> apeFollowPage = apeFollowService.list(queryWrapper);
        for (ApeFollow black : apeFollowPage) {
            ApeUser user = apeUserService.getById(black.getFromId());
            black.setAvatar(user.getAvatar());
            black.setUserName(user.getUserName());
        }
        return Result.success(apeFollowPage);
    }

    /** 根据id获取关注 */
    @Log(name = "根据id获取关注", type = BusinessType.OTHER)
    @GetMapping("getApeFollowById")
    public Result getApeFollowById(@RequestParam("id")String id) {
        ApeFollow apeFollow = apeFollowService.getById(id);
        return Result.success(apeFollow);
    }

    /** 保存关注 */
    @Log(name = "保存关注", type = BusinessType.INSERT)
    @PostMapping("saveApeFollow")
    public Result saveApeFollow(@RequestBody ApeFollow apeFollow) {
        apeFollow.setUserId(ShiroUtils.getUserInfo().getId());
        boolean save = apeFollowService.save(apeFollow);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑关注 */
    @Log(name = "编辑关注", type = BusinessType.UPDATE)
    @PostMapping("editApeFollow")
    public Result editApeFollow(@RequestBody ApeFollow apeFollow) {
        boolean save = apeFollowService.updateById(apeFollow);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除关注 */
    @GetMapping("removeApeFollow")
    @Log(name = "删除关注", type = BusinessType.DELETE)
    public Result removeApeFollow(@RequestParam("userId")String userId) {
        QueryWrapper<ApeFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeFollow::getFromId,userId)
                .eq(ApeFollow::getUserId,ShiroUtils.getUserInfo().getId());
        boolean removed = apeFollowService.remove(queryWrapper);
        if (removed) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    @GetMapping("getUserFollowBySeller")
    public Result getUserBlackBySeller(@RequestParam("userId") String userId) {
        ApeUser apeUser = ShiroUtils.getUserInfo();
        QueryWrapper<ApeFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeFollow::getUserId,apeUser.getId())
                .eq(ApeFollow::getFromId,userId).last("limit 1");
        ApeFollow black = apeFollowService.getOne(queryWrapper);
        if (black != null) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

}
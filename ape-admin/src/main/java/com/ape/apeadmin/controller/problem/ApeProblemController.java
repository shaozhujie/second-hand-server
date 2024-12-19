package com.ape.apeadmin.controller.problem;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeBlack;
import com.ape.apesystem.domain.ApeCommodity;
import com.ape.apesystem.domain.ApeProblem;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeBlackService;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeProblemService;
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
 * @description: 提问controller
 * @date 2024/07/01 02:55
 */
@Controller
@ResponseBody
@RequestMapping("problem")
public class ApeProblemController {

    @Autowired
    private ApeProblemService apeProblemService;
    @Autowired
    private ApeCommodityService apeCommodityService;
    @Autowired
    private ApeBlackService apeBlackService;

    /** 分页获取提问 */
    @Log(name = "分页获取提问", type = BusinessType.OTHER)
    @PostMapping("getApeProblemPage")
    public Result getApeProblemPage(@RequestBody ApeProblem apeProblem) {
        Page<ApeProblem> page = new Page<>(apeProblem.getPageNumber(),apeProblem.getPageSize());
        QueryWrapper<ApeProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeProblem.getContent()),ApeProblem::getContent,apeProblem.getContent())
                .eq(StringUtils.isNotBlank(apeProblem.getReply()),ApeProblem::getReply,apeProblem.getReply())
                .eq(StringUtils.isNotBlank(apeProblem.getUserId()),ApeProblem::getUserId,apeProblem.getUserId())
                .eq(StringUtils.isNotBlank(apeProblem.getFromId()),ApeProblem::getFromId,apeProblem.getFromId())
                .eq(StringUtils.isNotBlank(apeProblem.getCommodityId()),ApeProblem::getCommodityId,apeProblem.getCommodityId())
                .eq(StringUtils.isNotBlank(apeProblem.getName()),ApeProblem::getName,apeProblem.getName());
        Page<ApeProblem> apeProblemPage = apeProblemService.page(page, queryWrapper);
        return Result.success(apeProblemPage);
    }

    @PostMapping("getApeProblemList")
    public Result getApeProblemList(@RequestBody ApeProblem apeProblem) {
        QueryWrapper<ApeProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeProblem.getContent()),ApeProblem::getContent,apeProblem.getContent())
                .eq(StringUtils.isNotBlank(apeProblem.getReply()),ApeProblem::getReply,apeProblem.getReply())
                .eq(StringUtils.isNotBlank(apeProblem.getUserId()),ApeProblem::getUserId,apeProblem.getUserId())
                .eq(StringUtils.isNotBlank(apeProblem.getFromId()),ApeProblem::getFromId,apeProblem.getFromId())
                .eq(StringUtils.isNotBlank(apeProblem.getCommodityId()),ApeProblem::getCommodityId,apeProblem.getCommodityId())
                .eq(StringUtils.isNotBlank(apeProblem.getName()),ApeProblem::getName,apeProblem.getName());
        List<ApeProblem> apeProblemList = apeProblemService.list(queryWrapper);
        return Result.success(apeProblemList);
    }

    /** 根据id获取提问 */
    @Log(name = "根据id获取提问", type = BusinessType.OTHER)
    @GetMapping("getApeProblemById")
    public Result getApeProblemById(@RequestParam("id")String id) {
        ApeProblem apeProblem = apeProblemService.getById(id);
        return Result.success(apeProblem);
    }

    /** 保存提问 */
    @Log(name = "保存提问", type = BusinessType.INSERT)
    @PostMapping("saveApeProblem")
    public Result saveApeProblem(@RequestBody ApeProblem apeProblem) {
        ApeCommodity commodity = apeCommodityService.getById(apeProblem.getCommodityId());
        ApeUser apeUser = ShiroUtils.getUserInfo();
        QueryWrapper<ApeBlack> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeBlack::getUserId,commodity.getUserId())
                .eq(ApeBlack::getFromId,apeUser.getId());
        int count = apeBlackService.count(queryWrapper);
        if (count > 0) {
            return Result.fail("您已被卖家加入黑名单");
        }
        apeProblem.setUserId(commodity.getUserId());
        apeProblem.setName(commodity.getName());
        apeProblem.setFromId(ShiroUtils.getUserInfo().getId());
        apeProblem.setImage(commodity.getImage().split(",")[0]);
        boolean save = apeProblemService.save(apeProblem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑提问 */
    @Log(name = "编辑提问", type = BusinessType.UPDATE)
    @PostMapping("editApeProblem")
    public Result editApeProblem(@RequestBody ApeProblem apeProblem) {
        boolean save = apeProblemService.updateById(apeProblem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除提问 */
    @GetMapping("removeApeProblem")
    @Log(name = "删除提问", type = BusinessType.DELETE)
    public Result removeApeProblem(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeProblemService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("提问id不能为空！");
        }
    }

}
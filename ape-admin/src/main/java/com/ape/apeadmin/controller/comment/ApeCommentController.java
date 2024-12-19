package com.ape.apeadmin.controller.comment;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apesystem.domain.ApeComment;
import com.ape.apesystem.domain.ApeOrder;
import com.ape.apesystem.service.ApeCommentService;
import com.ape.apesystem.service.ApeOrderService;
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
 * @description: 评论controller
 * @date 2024/11/28 10:16
 */
@Controller
@ResponseBody
@RequestMapping("comment")
public class ApeCommentController {

    @Autowired
    private ApeCommentService apeCommentService;
    @Autowired
    private ApeOrderService apeOrderService;

    /** 分页获取评论 */
    @Log(name = "分页获取评论", type = BusinessType.OTHER)
    @PostMapping("getApeCommentList")
    public Result getApeCommentList(@RequestBody ApeComment apeComment) {
        QueryWrapper<ApeComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeComment.getContent()),ApeComment::getContent,apeComment.getContent())
                .eq(apeComment.getType() != null,ApeComment::getType,apeComment.getType())
                .eq(StringUtils.isNotBlank(apeComment.getUserId()),ApeComment::getUserId,apeComment.getUserId())
                .eq(StringUtils.isNotBlank(apeComment.getCreateBy()),ApeComment::getCreateBy,apeComment.getCreateBy())
                .eq(apeComment.getCreateTime() != null,ApeComment::getCreateTime,apeComment.getCreateTime())
                .eq(StringUtils.isNotBlank(apeComment.getUpdateBy()),ApeComment::getUpdateBy,apeComment.getUpdateBy())
                .eq(apeComment.getUpdateTime() != null,ApeComment::getUpdateTime,apeComment.getUpdateTime());
        List<ApeComment> apeCommentPage = apeCommentService.list(queryWrapper);
        return Result.success(apeCommentPage);
    }

    /** 根据id获取评论 */
    @Log(name = "根据id获取评论", type = BusinessType.OTHER)
    @GetMapping("getApeCommentById")
    public Result getApeCommentById(@RequestParam("id")String id) {
        ApeComment apeComment = apeCommentService.getById(id);
        return Result.success(apeComment);
    }

    /** 保存评论 */
    @Log(name = "保存评论", type = BusinessType.INSERT)
    @PostMapping("saveApeComment")
    public Result saveApeComment(@RequestBody ApeComment apeComment) {
        ApeComment comment = new ApeComment();
        comment.setContent(apeComment.getContent());
        ApeOrder apeOrder = apeOrderService.getById(apeComment.getId());
        comment.setUserId(apeOrder.getFromId());
        comment.setCommodityName(apeOrder.getCommodityName());
        boolean save = apeCommentService.save(comment);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑评论 */
    @Log(name = "编辑评论", type = BusinessType.UPDATE)
    @PostMapping("editApeComment")
    public Result editApeComment(@RequestBody ApeComment apeComment) {
        boolean save = apeCommentService.updateById(apeComment);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除评论 */
    @GetMapping("removeApeComment")
    @Log(name = "删除评论", type = BusinessType.DELETE)
    public Result removeApeComment(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeCommentService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("评论id不能为空！");
        }
    }

}
package com.ape.apeadmin.controller.message;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apesystem.domain.ApeMessage;
import com.ape.apesystem.service.ApeMessageService;
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
 * @description: 咨询controller
 * @date 2024/07/01 04:59
 */
@Controller
@ResponseBody
@RequestMapping("message")
public class ApeMessageController {

    @Autowired
    private ApeMessageService apeMessageService;

    /** 分页获取咨询 */
    @Log(name = "分页获取咨询", type = BusinessType.OTHER)
    @PostMapping("getApeMessageList")
    public Result getApeMessageList(@RequestBody ApeMessage apeMessage) {
        QueryWrapper<ApeMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeMessage.getContent()),ApeMessage::getContent,apeMessage.getContent())
                .eq(StringUtils.isNotBlank(apeMessage.getConsultingId()),ApeMessage::getConsultingId,apeMessage.getConsultingId())
                .eq(apeMessage.getCreateTime() != null,ApeMessage::getCreateTime,apeMessage.getCreateTime());
        List<ApeMessage> apeMessageList = apeMessageService.list(queryWrapper);
        return Result.success(apeMessageList);
    }

    /** 根据id获取咨询 */
    @Log(name = "根据id获取咨询", type = BusinessType.OTHER)
    @GetMapping("getApeMessageById")
    public Result getApeMessageById(@RequestParam("id")String id) {
        ApeMessage apeMessage = apeMessageService.getById(id);
        return Result.success(apeMessage);
    }

    /** 保存咨询 */
    @Log(name = "保存咨询", type = BusinessType.INSERT)
    @PostMapping("saveApeMessage")
    public Result saveApeMessage(@RequestBody ApeMessage apeMessage) {
        boolean save = apeMessageService.save(apeMessage);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑咨询 */
    @Log(name = "编辑咨询", type = BusinessType.UPDATE)
    @PostMapping("editApeMessage")
    public Result editApeMessage(@RequestBody ApeMessage apeMessage) {
        boolean save = apeMessageService.updateById(apeMessage);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除咨询 */
    @GetMapping("removeApeMessage")
    @Log(name = "删除咨询", type = BusinessType.DELETE)
    public Result removeApeMessage(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeMessageService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("咨询id不能为空！");
        }
    }

}
package com.ape.apeadmin.controller.consulting;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.ApeBlackService;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeConsultingItemService;
import com.ape.apesystem.service.ApeConsultingService;
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
 * @description: 咨询内容controller
 * @date 2024/07/09 09:07
 */
@Controller
@ResponseBody
@RequestMapping("item")
public class ApeConsultingItemController {

    @Autowired
    private ApeConsultingItemService apeConsultingItemService;
    @Autowired
    private ApeConsultingService apeConsultingService;
    @Autowired
    private ApeBlackService apeBlackService;
    @Autowired
    private ApeCommodityService apeCommodityService;

    /** 分页获取咨询内容 */
    @Log(name = "分页获取咨询内容", type = BusinessType.OTHER)
    @PostMapping("getApeConsultingItemList")
    public Result getApeConsultingItemList(@RequestBody ApeConsultingItem apeConsultingItem) {
        QueryWrapper<ApeConsultingItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeConsultingItem.getConsultingId()),ApeConsultingItem::getConsultingId,apeConsultingItem.getConsultingId())
                .eq(StringUtils.isNotBlank(apeConsultingItem.getContent()),ApeConsultingItem::getContent,apeConsultingItem.getContent())
                .eq(apeConsultingItem.getUserId() != null,ApeConsultingItem::getUserId,apeConsultingItem.getUserId())
                .eq(StringUtils.isNotBlank(apeConsultingItem.getCreateBy()),ApeConsultingItem::getCreateBy,apeConsultingItem.getCreateBy())
                .eq(apeConsultingItem.getCreateTime() != null,ApeConsultingItem::getCreateTime,apeConsultingItem.getCreateTime())
                .eq(StringUtils.isNotBlank(apeConsultingItem.getUpdateBy()),ApeConsultingItem::getUpdateBy,apeConsultingItem.getUpdateBy())
                .eq(apeConsultingItem.getUpdateTime() != null,ApeConsultingItem::getUpdateTime,apeConsultingItem.getUpdateTime());
        List<ApeConsultingItem> apeConsultingItemList = apeConsultingItemService.list(queryWrapper);
        return Result.success(apeConsultingItemList);
    }

    /** 根据id获取咨询内容 */
    @Log(name = "根据id获取咨询内容", type = BusinessType.OTHER)
    @GetMapping("getApeConsultingItemById")
    public Result getApeConsultingItemById(@RequestParam("id")String id) {
        ApeConsultingItem apeConsultingItem = apeConsultingItemService.getById(id);
        return Result.success(apeConsultingItem);
    }

    /** 保存咨询内容 */
    @Log(name = "保存咨询内容", type = BusinessType.INSERT)
    @PostMapping("saveApeConsultingItem")
    public Result saveApeConsultingItem(@RequestBody ApeConsultingItem apeConsultingItem) {
        ApeConsulting consulting = apeConsultingService.getById(apeConsultingItem.getConsultingId());
        ApeCommodity apeCommodity = apeCommodityService.getById(consulting.getCommodityId());
        ApeUser apeUser = ShiroUtils.getUserInfo();
        QueryWrapper<ApeBlack> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeBlack::getUserId,apeUser.getId())
                .eq(ApeBlack::getFromId,apeCommodity.getUserId());
        int count = apeBlackService.count(queryWrapper);
        if (count > 0) {
            return Result.fail("您已被卖家加入黑名单");
        }
        apeConsultingItem.setUserId(apeUser.getId());
        boolean save = apeConsultingItemService.save(apeConsultingItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑咨询内容 */
    @Log(name = "编辑咨询内容", type = BusinessType.UPDATE)
    @PostMapping("editApeConsultingItem")
    public Result editApeConsultingItem(@RequestBody ApeConsultingItem apeConsultingItem) {
        boolean save = apeConsultingItemService.updateById(apeConsultingItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除咨询内容 */
    @GetMapping("removeApeConsultingItem")
    @Log(name = "删除咨询内容", type = BusinessType.DELETE)
    public Result removeApeConsultingItem(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeConsultingItemService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("咨询内容id不能为空！");
        }
    }

}
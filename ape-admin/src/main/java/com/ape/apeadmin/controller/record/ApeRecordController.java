package com.ape.apeadmin.controller.record;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeCommodity;
import com.ape.apesystem.domain.ApeRecord;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeRecordService;
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
 * @description: 浏览记录controller
 * @date 2024/06/28 03:33
 */
@Controller
@ResponseBody
@RequestMapping("record")
public class ApeRecordController {

    @Autowired
    private ApeRecordService apeRecordService;
    @Autowired
    private ApeCommodityService apeCommodityService;

    /** 分页获取浏览记录 */
    @Log(name = "分页获取浏览记录", type = BusinessType.OTHER)
    @PostMapping("getApeRecordPage")
    public Result getApeRecordPage(@RequestBody ApeRecord apeRecord) {
        Page<ApeRecord> page = new Page<>(apeRecord.getPageNumber(),apeRecord.getPageSize());
        QueryWrapper<ApeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeRecord.getName()),ApeRecord::getName,apeRecord.getName())
                .eq(apeRecord.getPrice() != null,ApeRecord::getPrice,apeRecord.getPrice())
                .eq(StringUtils.isNotBlank(apeRecord.getUserId()),ApeRecord::getUserId,apeRecord.getUserId())
                .eq(StringUtils.isNotBlank(apeRecord.getUserAvatar()),ApeRecord::getUserAvatar,apeRecord.getUserAvatar())
                .eq(StringUtils.isNotBlank(apeRecord.getCreateBy()),ApeRecord::getCreateBy,apeRecord.getCreateBy())
                .eq(apeRecord.getCreateTime() != null,ApeRecord::getCreateTime,apeRecord.getCreateTime())
                .eq(StringUtils.isNotBlank(apeRecord.getUpdateBy()),ApeRecord::getUpdateBy,apeRecord.getUpdateBy())
                .eq(apeRecord.getUpdateTime() != null,ApeRecord::getUpdateTime,apeRecord.getUpdateTime())
                .eq(StringUtils.isNotBlank(apeRecord.getRemark()),ApeRecord::getRemark,apeRecord.getRemark());
        Page<ApeRecord> apeRecordPage = apeRecordService.page(page, queryWrapper);
        for (ApeRecord record : apeRecordPage.getRecords()) {
            ApeCommodity apeCommodity = apeCommodityService.getById(record.getCommodityId());
            record.setImage(apeCommodity.getImage());
            record.setPrice(apeCommodity.getPrice());
            record.setName(apeCommodity.getName());
        }
        return Result.success(apeRecordPage);
    }

    /** 根据id获取浏览记录 */
    @Log(name = "根据id获取浏览记录", type = BusinessType.OTHER)
    @GetMapping("getApeRecordById")
    public Result getApeRecordById(@RequestParam("id")String id) {
        ApeRecord apeRecord = apeRecordService.getById(id);
        return Result.success(apeRecord);
    }

    /** 保存浏览记录 */
    @Log(name = "保存浏览记录", type = BusinessType.INSERT)
    @PostMapping("saveApeRecord")
    public Result saveApeRecord(@RequestBody ApeRecord apeRecord) {
        ApeUser user = ShiroUtils.getUserInfo();
        apeRecord.setUserId(user.getId());
        QueryWrapper<ApeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeRecord::getCommodityId,apeRecord.getCommodityId())
                .eq(ApeRecord::getUserId,user.getId());
        int count = apeRecordService.count(queryWrapper);
        if (count <= 0) {
            ApeCommodity commodity = apeCommodityService.getById(apeRecord.getCommodityId());
            apeRecord.setName(commodity.getName());
            apeRecord.setPrice(commodity.getPrice());
            apeRecord.setUserAvatar(commodity.getUserAvatar());
            apeRecord.setImage(commodity.getImage());
            boolean save = apeRecordService.save(apeRecord);
            if (save) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } else {
            return Result.success();
        }
    }

    /** 编辑浏览记录 */
    @Log(name = "编辑浏览记录", type = BusinessType.UPDATE)
    @PostMapping("editApeRecord")
    public Result editApeRecord(@RequestBody ApeRecord apeRecord) {
        boolean save = apeRecordService.updateById(apeRecord);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除浏览记录 */
    @GetMapping("removeApeRecord")
    @Log(name = "删除浏览记录", type = BusinessType.DELETE)
    public Result removeApeRecord(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeRecordService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("浏览记录id不能为空！");
        }
    }

}

package com.ape.apeadmin.controller.commodity;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apesystem.domain.ApeCommodityType;
import com.ape.apesystem.service.ApeCommodityTypeService;
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
 * @description: 商品类型controller
 * @date 2024/02/22 10:06
 */
@Controller
@ResponseBody
@RequestMapping("type")
public class ApeCommodityTypeController {

    @Autowired
    private ApeCommodityTypeService apeCommodityTypeService;

    /** 分页获取商品类型 */
    @Log(name = "分页获取商品类型", type = BusinessType.OTHER)
    @PostMapping("getApeCommodityTypePage")
    public Result getApeCommodityTypePage(@RequestBody ApeCommodityType apeCommodityType) {
        Page<ApeCommodityType> page = new Page<>(apeCommodityType.getPageNumber(),apeCommodityType.getPageSize());
        QueryWrapper<ApeCommodityType> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeCommodityType.getName()),ApeCommodityType::getName,apeCommodityType.getName());
        Page<ApeCommodityType> apeCommodityTypePage = apeCommodityTypeService.page(page, queryWrapper);
        return Result.success(apeCommodityTypePage);
    }

    @GetMapping("getApeCommodityTypeList")
    public Result getApeCommodityTypeList() {
        List<ApeCommodityType> list = apeCommodityTypeService.list();
        return Result.success(list);
    }

    /** 根据id获取商品类型 */
    @Log(name = "根据id获取商品类型", type = BusinessType.OTHER)
    @GetMapping("getApeCommodityTypeById")
    public Result getApeCommodityTypeById(@RequestParam("id")String id) {
        ApeCommodityType apeCommodityType = apeCommodityTypeService.getById(id);
        return Result.success(apeCommodityType);
    }

    /** 保存商品类型 */
    @Log(name = "保存商品类型", type = BusinessType.INSERT)
    @PostMapping("saveApeCommodityType")
    public Result saveApeCommodityType(@RequestBody ApeCommodityType apeCommodityType) {
        boolean save = apeCommodityTypeService.save(apeCommodityType);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑商品类型 */
    @Log(name = "编辑商品类型", type = BusinessType.UPDATE)
    @PostMapping("editApeCommodityType")
    public Result editApeCommodityType(@RequestBody ApeCommodityType apeCommodityType) {
        boolean save = apeCommodityTypeService.updateById(apeCommodityType);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除商品类型 */
    @GetMapping("removeApeCommodityType")
    @Log(name = "删除商品类型", type = BusinessType.DELETE)
    public Result removeApeCommodityType(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeCommodityTypeService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("商品类型id不能为空！");
        }
    }

}
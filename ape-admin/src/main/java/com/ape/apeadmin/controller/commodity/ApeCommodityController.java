package com.ape.apeadmin.controller.commodity;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 商品controller
 * @date 2024/02/20 10:49
 */
@Controller
@ResponseBody
@RequestMapping("commodity")
public class ApeCommodityController {

    @Autowired
    private ApeCommodityService apeCommodityService;
    @Autowired
    private ApeRecordService apeRecordService;
    @Autowired
    private ApeConsultingService apeConsultingService;
    @Autowired
    private ApeConsultingItemService apeConsultingItemService;
    @Autowired
    private ApeProblemService apeProblemService;
    @Autowired
    private ApeUserFavorService apeUserFavorService;
    @Autowired
    private CollaborativeFiltering collaborativeFiltering;

    /** 分页获取商品 */
    @Log(name = "分页获取商品", type = BusinessType.OTHER)
    @PostMapping("getApeCommodityPage")
    public Result getApeCommodityPage(@RequestBody ApeCommodity apeCommodity) {
        Page<ApeCommodity> page = new Page<>(apeCommodity.getPageNumber(),apeCommodity.getPageSize());
        QueryWrapper<ApeCommodity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeCommodity.getName()),ApeCommodity::getName,apeCommodity.getName())
                .eq(apeCommodity.getState() != null,ApeCommodity::getState,apeCommodity.getState())
                .like(StringUtils.isNotBlank(apeCommodity.getCreateBy()),ApeCommodity::getCreateBy,apeCommodity.getCreateBy())
                .eq(StringUtils.isNotBlank(apeCommodity.getClassification()),ApeCommodity::getClassification,apeCommodity.getClassification())
                .eq(StringUtils.isNotBlank(apeCommodity.getProvince()),ApeCommodity::getProvince,apeCommodity.getProvince())
                .eq(StringUtils.isNotBlank(apeCommodity.getCity()),ApeCommodity::getCity,apeCommodity.getCity())
                .like(StringUtils.isNotBlank(apeCommodity.getType()),ApeCommodity::getType,apeCommodity.getType())
                .like(StringUtils.isNotBlank(apeCommodity.getServer()),ApeCommodity::getServer,apeCommodity.getServer())
                .ge(apeCommodity.getMin() != null,ApeCommodity::getPrice,apeCommodity.getMin())
                .le(apeCommodity.getMax() != null,ApeCommodity::getPrice,apeCommodity.getMax())
                .gt(apeCommodity.getNum() != null,ApeCommodity::getNum,apeCommodity.getNum());
        if (apeCommodity.getFlag() != null) {
            queryWrapper.lambda().gt(ApeCommodity::getNum,0);
        }
        if (apeCommodity.getSort() == null) {
            queryWrapper.lambda().orderByDesc(ApeCommodity::getCreateTime);
        } else {
            if (apeCommodity.getSort() == 0) {
                queryWrapper.lambda().orderByAsc(ApeCommodity::getPrice);
            } else if (apeCommodity.getSort() == 1) {
                queryWrapper.lambda().orderByDesc(ApeCommodity::getPrice);
            } else {
                queryWrapper.lambda().orderByDesc(ApeCommodity::getCreateTime);
            }
        }
        Page<ApeCommodity> apeCommodityPage = apeCommodityService.page(page, queryWrapper);
        return Result.success(apeCommodityPage);
    }

    @GetMapping("getOtherCommodity")
    public Result getOtherCommodity(@RequestParam("id")String id,@RequestParam("classification")String classification) {
        QueryWrapper<ApeCommodity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeCommodity::getClassification,classification)
                .ne(ApeCommodity::getId,id).last("limit 3");
        List<ApeCommodity> commodities = apeCommodityService.list(queryWrapper);
        return Result.success(commodities);
    }

    @GetMapping("getApeCommodityListIndex")
    public Result getApeCommodityListIndex() {
        //查询浏览记录
        ApeUser apeUser = ShiroUtils.getUserInfo();
        return Result.success(collaborativeFiltering.recommendItems(apeUser,apeUser.getId()));
    }

    @GetMapping("getApeCommodityListByUserId")
    public Result getApeCommodityListByUserId(@RequestParam("userId") String userId) {
        QueryWrapper<ApeCommodity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeCommodity::getUserId,userId).orderByDesc(ApeCommodity::getCreateTime);
        List<ApeCommodity> commodityList = apeCommodityService.list(queryWrapper);
        return Result.success(commodityList);
    }

    /** 根据id获取商品 */
    @Log(name = "根据id获取商品", type = BusinessType.OTHER)
    @GetMapping("getApeCommodityById")
    public Result getApeCommodityById(@RequestParam("id")String id) {
        ApeCommodity apeCommodity = apeCommodityService.getById(id);
        return Result.success(apeCommodity);
    }

    /** 保存商品 */
    @Log(name = "保存商品", type = BusinessType.INSERT)
    @PostMapping("saveApeCommodity")
    public Result saveApeCommodity(@RequestBody ApeCommodity apeCommodity) {
        ApeUser user = ShiroUtils.getUserInfo();
        apeCommodity.setUserId(user.getId());
        apeCommodity.setUserAvatar(user.getAvatar());
        boolean save = apeCommodityService.save(apeCommodity);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑商品 */
    @Log(name = "编辑商品", type = BusinessType.UPDATE)
    @PostMapping("editApeCommodity")
    public Result editApeCommodity(@RequestBody ApeCommodity apeCommodity) {
        boolean save = apeCommodityService.updateById(apeCommodity);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除商品 */
    @GetMapping("removeApeCommodity")
    @Transactional(rollbackFor = Exception.class)
    @Log(name = "删除商品", type = BusinessType.DELETE)
    public Result removeApeCommodity(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeCommodityService.removeById(id);
                //删除浏览记录
                QueryWrapper<ApeRecord> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ApeRecord::getCommodityId,id);
                apeRecordService.remove(queryWrapper);
                //删除收藏
                QueryWrapper<ApeUserFavor> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(ApeUserFavor::getCommodityId,id);
                apeUserFavorService.remove(queryWrapper1);
                //删除提问
                QueryWrapper<ApeProblem> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.lambda().eq(ApeProblem::getCommodityId,id);
                apeProblemService.remove(queryWrapper2);
                //删除咨询
                QueryWrapper<ApeConsulting> queryWrapper3 = new QueryWrapper<>();
                queryWrapper3.lambda().eq(ApeConsulting::getCommodityId,id);
                List<ApeConsulting> list = apeConsultingService.list(queryWrapper3);
                apeConsultingService.remove(queryWrapper3);
                for (ApeConsulting apeConsult : list) {
                    QueryWrapper<ApeConsultingItem> queryWrapper4 = new QueryWrapper<>();
                    queryWrapper4.lambda().eq(ApeConsultingItem::getConsultingId,apeConsult.getId());
                    apeConsultingItemService.remove(queryWrapper4);
                }
            }
            return Result.success();
        } else {
            return Result.fail("商品id不能为空！");
        }
    }

    @GetMapping("addLook")
    public Result addLook(@RequestParam("id")String id) {
        ApeCommodity apeCommodity = apeCommodityService.getById(id);
        apeCommodity.setLook(apeCommodity.getLook() + 1);
        boolean update = apeCommodityService.updateById(apeCommodity);
        if (update) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    @GetMapping("addWant")
    public Result addWant(@RequestParam("id")String id) {
        ApeCommodity apeCommodity = apeCommodityService.getById(id);
        apeCommodity.setLook(apeCommodity.getWant() + 1);
        boolean update = apeCommodityService.updateById(apeCommodity);
        if (update) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

}
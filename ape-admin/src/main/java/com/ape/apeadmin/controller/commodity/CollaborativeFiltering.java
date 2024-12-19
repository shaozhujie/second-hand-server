package com.ape.apeadmin.controller.commodity;

import com.ape.apesystem.domain.ApeCommodity;
import com.ape.apesystem.domain.ApeRecord;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.domain.ApeUserFavor;
import com.ape.apesystem.service.ApeCommodityService;
import com.ape.apesystem.service.ApeRecordService;
import com.ape.apesystem.service.ApeUserFavorService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CollaborativeFiltering {

    @Autowired
    private ApeCommodityService apeCommodityService;
    @Autowired
    private ApeUserFavorService apeUserFavorService;
    @Autowired
    private ApeRecordService apeRecordService;

    // 用户评分矩阵
    private Map<String, Map<String, Integer>> ratings;

    public Map<String, Map<String, Integer>> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Map<String, Integer>> ratings) {
        this.ratings = ratings;
    }

    // 计算相似度
    private Map<String, Map<String, Integer>> cosineSimilarity(ApeUser apeUser) {
        QueryWrapper<ApeRecord> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeRecord::getUserId,apeUser.getId());
        List<ApeRecord> apeRecords = apeRecordService.list(wrapper);
        //查询收藏
        QueryWrapper<ApeUserFavor> wrapper1 = new QueryWrapper<>();
        wrapper1.lambda().eq(ApeUserFavor::getUserId,apeUser.getId());
        List<ApeUserFavor> apeUserFavors = apeUserFavorService.list(wrapper1);
        Map<String,Integer> map = new HashMap<>();
        for (ApeRecord apeRecord : apeRecords) {
            ApeCommodity apeCommodity = apeCommodityService.getById(apeRecord.getCommodityId());
            if (map.containsKey(apeCommodity.getClassification())) {
                Integer integer = map.get(apeCommodity.getClassification());
                map.put(apeCommodity.getClassification(), integer + 1);
            } else {
                map.put(apeCommodity.getClassification(), 1);
            }
        }
        for (ApeUserFavor apeUserFavor : apeUserFavors) {
            ApeCommodity apeCommodity = apeCommodityService.getById(apeUserFavor.getCommodityId());
            if (map.containsKey(apeCommodity.getClassification())) {
                Integer integer = map.get(apeCommodity.getClassification());
                map.put(apeCommodity.getClassification(), integer + 1);
            } else {
                map.put(apeCommodity.getClassification(), 1);
            }
        }
        Map<String, Map<String, Integer>> ratings = new HashMap<>();
        ratings.put(apeUser.getId(),map);
        return ratings;
    }

    // 为用户推荐物品
    public List<ApeCommodity> recommendItems(ApeUser apeUser,String user) {
        Map<String, Map<String, Integer>> stringMapMap = cosineSimilarity(apeUser);
        Map<String, Integer> userRatings = stringMapMap.get(user);
        Set<String> set = userRatings.keySet();
        QueryWrapper<ApeCommodity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeCommodity::getState,0)
                        .in(set.size() > 0,ApeCommodity::getClassification,set).
                gt(ApeCommodity::getNum,0).last("ORDER BY RAND() limit 12");
        List<ApeCommodity> commodities = apeCommodityService.list(queryWrapper);
        if (commodities.size() <= 0) {
            QueryWrapper<ApeCommodity> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeCommodity::getState,0).
                    gt(ApeCommodity::getNum,0).last("ORDER BY RAND() limit 12");
            List<ApeCommodity> commodityList = apeCommodityService.list(wrapper);
            return commodityList;
        } else {
            return commodities;
        }
    }
}

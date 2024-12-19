package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeCity;
import com.ape.apesystem.mapper.ApeCityMapper;
import com.ape.apesystem.service.ApeCityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 城市service实现类
 * @date 2024/02/20 03:00
 */
@Service
public class ApeCityServiceImpl extends ServiceImpl<ApeCityMapper, ApeCity> implements ApeCityService {
}
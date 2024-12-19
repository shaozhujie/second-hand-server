package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeComment;
import com.ape.apesystem.mapper.ApeCommentMapper;
import com.ape.apesystem.service.ApeCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 评论service实现类
 * @date 2024/11/28 10:16
 */
@Service
public class ApeCommentServiceImpl extends ServiceImpl<ApeCommentMapper, ApeComment> implements ApeCommentService {
}
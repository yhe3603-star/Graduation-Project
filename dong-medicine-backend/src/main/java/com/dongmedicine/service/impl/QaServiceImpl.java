package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.service.QaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QaServiceImpl extends ServiceImpl<QaMapper, Qa> implements QaService {

    private static final Logger log = LoggerFactory.getLogger(QaServiceImpl.class);

    @Autowired
    private QaMapper qaMapper;

    @Override
    public List<Qa> listByCategory(String category) {
        LambdaQueryWrapper<Qa> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            qw.eq(Qa::getCategory, category);
        }
        qw.orderByDesc(Qa::getPopularity);
        return list(qw);
    }

    @Override
    public Page<Qa> pageByCategory(String category, Integer page, Integer size) {
        Page<Qa> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Qa> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            qw.eq(Qa::getCategory, category);
        }
        qw.orderByDesc(Qa::getPopularity);
        return page(pageParam, qw);
    }

    @Override
    public List<Qa> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw BusinessException.badRequest("搜索关键词不能为空");
        }
        String escapedKeyword = PageUtils.escapeLike(keyword);
        return list(new LambdaQueryWrapper<Qa>()
                .like(Qa::getQuestion, escapedKeyword)
                .or().like(Qa::getAnswer, escapedKeyword)
                .orderByDesc(Qa::getPopularity));
    }

    @Override
    public Page<Qa> searchPaged(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            throw BusinessException.badRequest("搜索关键词不能为空");
        }
        String escapedKeyword = PageUtils.escapeLike(keyword);
        Page<Qa> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Qa> qw = new LambdaQueryWrapper<Qa>()
                .like(Qa::getQuestion, escapedKeyword)
                .or().like(Qa::getAnswer, escapedKeyword)
                .orderByDesc(Qa::getPopularity);
        return page(pageParam, qw);
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            qaMapper.incrementViewCount(id);
            log.debug("Qa view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for qa id: {}", id, e);
        }
    }
}

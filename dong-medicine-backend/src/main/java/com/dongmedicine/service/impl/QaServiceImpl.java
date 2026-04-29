package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.service.QaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaServiceImpl extends ServiceImpl<QaMapper, Qa> implements QaService {

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
            baseMapper.incrementViewCount(id);
            log.debug("Qa view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for qa id: {}", id, e);
        }
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", count());
        stats.put("categoryCount", baseMapper.countDistinctCategory());
        stats.put("totalViews", baseMapper.sumViewCount());
        stats.put("totalFavorites", baseMapper.sumFavoriteCount());
        return stats;
    }

    @Override
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("category", baseMapper.selectDistinctCategory());
        return map;
    }
}

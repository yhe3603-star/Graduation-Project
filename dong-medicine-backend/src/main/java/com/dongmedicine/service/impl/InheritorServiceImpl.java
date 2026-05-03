package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.PopularityAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InheritorServiceImpl extends ServiceImpl<InheritorMapper, Inheritor> implements InheritorService {

    private static final Logger log = LoggerFactory.getLogger(InheritorServiceImpl.class);

    @Autowired
    private InheritorMapper inheritorMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;
    @Autowired
    private PopularityAsyncService popularityAsyncService;

    @Override
    public List<Inheritor> getAllInheritors() {
        return list(new LambdaQueryWrapper<Inheritor>()
                .orderByDesc(Inheritor::getExperienceYears)
                .orderByAsc(Inheritor::getName));
    }

    @Override
    public Inheritor getDetailById(Integer id) {
        return getById(id);
    }

    @Override
    public List<Inheritor> getByLevel(String level) {
        if (!StringUtils.hasText(level)) {
            return getAllInheritors();
        }
        return list(new LambdaQueryWrapper<Inheritor>()
                .eq(Inheritor::getLevel, level)
                .orderByDesc(Inheritor::getExperienceYears));
    }

    @Override
    @Cacheable(value = "inheritors", key = "'list:' + #level + ':' + #sortBy")
    public List<Inheritor> listByLevel(String level, String sortBy) {
        LambdaQueryWrapper<Inheritor> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            qw.eq(Inheritor::getLevel, level);
        }
        qw.orderByDesc("experience".equals(sortBy), Inheritor::getExperienceYears)
          .orderByAsc(!"experience".equals(sortBy), Inheritor::getName);
        return list(qw);
    }

    @Override
    public Page<Inheritor> pageByLevel(String level, String sortBy, Integer page, Integer size) {
        Page<Inheritor> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Inheritor> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            qw.eq(Inheritor::getLevel, level);
        }
        qw.orderByDesc("experience".equals(sortBy), Inheritor::getExperienceYears)
          .orderByAsc(!"experience".equals(sortBy), Inheritor::getName);
        return page(pageParam, qw);
    }

    @Override
    public Page<Inheritor> advancedSearchPaged(String keyword, String level, String sortBy, Integer page, Integer size) {
        Page<Inheritor> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Inheritor> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            qw.eq(Inheritor::getLevel, level);
        }
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(w -> w.like(Inheritor::getName, escapedKeyword)
                    .or().like(Inheritor::getSpecialties, escapedKeyword));
        }
        qw.orderByDesc("experience".equals(sortBy), Inheritor::getExperienceYears)
          .orderByAsc(!"experience".equals(sortBy), Inheritor::getName);
        return page(pageParam, qw);
    }

    @Override
    public Page<Inheritor> searchPaged(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            throw BusinessException.badRequest("搜索关键词不能为空");
        }
        String escapedKeyword = PageUtils.escapeLike(keyword);
        Page<Inheritor> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Inheritor> qw = new LambdaQueryWrapper<Inheritor>()
                .like(Inheritor::getName, escapedKeyword)
                .or().like(Inheritor::getSpecialties, escapedKeyword)
                .orderByDesc(Inheritor::getExperienceYears);
        return page(pageParam, qw);
    }

    @Override
    public Inheritor getDetailWithExtras(Integer id) {
        return getById(id);
    }

    @Override
    public List<Inheritor> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw BusinessException.badRequest("搜索关键词不能为空");
        }
        String escaped = PageUtils.escapeLike(keyword);
        return list(new LambdaQueryWrapper<Inheritor>()
                .like(Inheritor::getName, escaped)
                .or().like(Inheritor::getSpecialties, escaped)
                .orderByDesc(Inheritor::getExperienceYears));
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            inheritorMapper.incrementViewCount(id);
            log.debug("Inheritor view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for inheritor id: {}", id, e);
        }
    }

    @Override
    @CacheEvict(value = "inheritors", allEntries = true)
    public void clearCache() {
        log.info("Inheritor cache cleared");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "inheritors", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Inheritor inheritor = getById(id);
        if (inheritor == null) {
            return;
        }
        removeById(id);
        fileCleanupHelper.deleteFilesFromJson(inheritor.getImages());
        fileCleanupHelper.deleteFilesFromJson(inheritor.getVideos());
        fileCleanupHelper.deleteFilesFromJson(inheritor.getDocuments());
        log.info("Deleted inheritor {} with associated files", id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", count());
        stats.put("regionLevelCount", inheritorMapper.countByLevel("自治区级"));
        stats.put("cityLevelCount", inheritorMapper.countByLevel("市级"));
        stats.put("countyLevelCount", inheritorMapper.countByLevel("县级"));
        stats.put("totalViews", inheritorMapper.sumViewCount());
        stats.put("totalFavorites", inheritorMapper.sumFavoriteCount());
        return stats;
    }

    @Override
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("level", inheritorMapper.selectDistinctLevel());
        return map;
    }

    @Override
    public List<Map<String, Object>> topByViewCount(int limit) {
        return inheritorMapper.topByViewCount(limit);
    }
}

package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.PlantService;
import com.dongmedicine.service.PopularityAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_SEARCH_LIMIT = 50;

    private final FileCleanupHelper fileCleanupHelper;
    private final PopularityAsyncService popularityAsyncService;

    @Value("${app.search.use-fulltext:true}")
    private boolean useFullTextSearch;

    @Override
    @Cacheable(value = "searchResults", key = "'plants:' + (#keyword ?: 'all') + ':' + (#category ?: 'all') + ':' + (#usageWay ?: 'all')")
    public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(wrapper -> wrapper.like(Plant::getNameCn, escapedKeyword)
                                     .or().like(Plant::getNameDong, escapedKeyword)
                                     .or().like(Plant::getEfficacy, escapedKeyword)
                                     .or().like(Plant::getStory, escapedKeyword));
        }
        if (StringUtils.hasText(category)) {
            qw.eq(Plant::getCategory, category);
        }
        if (StringUtils.hasText(usageWay)) {
            qw.eq(Plant::getUsageWay, usageWay);
        }
        qw.orderByAsc(Plant::getNameCn);
        return list(qw);
    }

    @Override
    public Page<Plant> advancedSearchPaged(String keyword, String category, String usageWay, Integer page, Integer size) {
        Page<Plant> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(wrapper -> wrapper.like(Plant::getNameCn, escapedKeyword)
                                     .or().like(Plant::getNameDong, escapedKeyword)
                                     .or().like(Plant::getEfficacy, escapedKeyword)
                                     .or().like(Plant::getStory, escapedKeyword));
        }
        if (StringUtils.hasText(category)) {
            qw.eq(Plant::getCategory, category);
        }
        if (StringUtils.hasText(usageWay)) {
            qw.eq(Plant::getUsageWay, usageWay);
        }
        qw.orderByAsc(Plant::getNameCn);
        return page(pageParam, qw);
    }

    @Override
    public List<Plant> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
        }
        return search(keyword, DEFAULT_SEARCH_LIMIT);
    }

    @Override
    public Page<Plant> searchPaged(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
        }
        String escapedKeyword = PageUtils.escapeLike(keyword);
        Page<Plant> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<Plant>()
                .like(Plant::getNameCn, escapedKeyword)
                .or().like(Plant::getNameDong, escapedKeyword)
                .or().like(Plant::getEfficacy, escapedKeyword)
                .orderByAsc(Plant::getNameCn);
        return page(pageParam, qw);
    }

    @Override
    public List<Plant> search(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
        }
        if (limit < MIN_PAGE_SIZE || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    String.format("限制数量必须在%d-%d之间", MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }

        String escapedKeyword = PageUtils.escapeLike(keyword);

        try {
            if (useFullTextSearch) {
                List<Plant> results = baseMapper.searchByFullText(keyword, limit);
                if (!results.isEmpty()) {
                    log.debug("全文搜索找到 {} 条结果: {}", results.size(), keyword);
                    return results;
                }
            }
        } catch (Exception e) {
            log.warn("全文搜索失败，回退到LIKE搜索: {}", e.getMessage());
        }

        List<Plant> results = baseMapper.searchByLike(escapedKeyword, limit);
        log.debug("LIKE搜索找到 {} 条结果: {}", results.size(), keyword);
        return results;
    }

    @Override
    public List<Plant> getSimilarPlants(Integer id) {
        Plant current = getById(id);
        if (current == null) return List.of();
        return list(new LambdaQueryWrapper<Plant>()
                .eq(Plant::getCategory, current.getCategory())
                .ne(Plant::getId, id)
                .orderByDesc(Plant::getId)
                .last("LIMIT 4"));
    }

    @Override
    public Plant getDetailWithStory(Integer id) {
        Plant plant = getById(id);
        if (plant != null) {
            popularityAsyncService.incrementPlantViewAndPopularity(id);
        }
        return plant;
    }

    @Override
    public List<Plant> getRandomPlants(int limit) {
        if (limit < MIN_PAGE_SIZE || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    String.format("限制数量必须在%d-%d之间", MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }
        return baseMapper.selectRandomPlants(limit);
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            baseMapper.incrementViewCount(id);
            log.debug("Plant view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for plant id: {}", id, e);
        }
    }

    @Override
    @CacheEvict(value = "plants", allEntries = true)
    public void clearCache() {
        log.info("Plant cache cleared");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "plants", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Plant plant = getById(id);
        if (plant == null) {
            return;
        }
        removeById(id);
        fileCleanupHelper.deleteFilesFromJson(plant.getImages());
        fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
        fileCleanupHelper.deleteFilesFromJson(plant.getDocuments());
        log.info("Deleted plant {} with associated files", id);
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
        map.put("usageWay", baseMapper.selectDistinctUsageWay());
        return map;
    }

    @Override
    public List<Map<String, Object>> topByViewCount(int limit) {
        return baseMapper.topByViewCount(limit);
    }
}

package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    private static final Logger log = LoggerFactory.getLogger(PlantServiceImpl.class);

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_SEARCH_LIMIT = 50;

    @Autowired
    private PlantMapper plantMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

    @Value("${app.search.use-fulltext:true}")
    private boolean useFullTextSearch;

    @Override
    @Cacheable(value = "plants", key = "'list:' + (#category ?: 'all') + ':' + (#usageWay ?: 'all')")
    public List<Plant> listByDoubleFilter(String category, String usageWay) {
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
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
    public List<Plant> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
        }
        return search(keyword, DEFAULT_SEARCH_LIMIT);
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

        String escapedKeyword = escapeLikeSpecialChars(keyword);

        try {
            if (useFullTextSearch) {
                List<Plant> results = plantMapper.searchByFullText(keyword, limit);
                if (!results.isEmpty()) {
                    log.debug("全文搜索找到 {} 条结果: {}", results.size(), keyword);
                    return results;
                }
            }
        } catch (Exception e) {
            log.warn("全文搜索失败，回退到LIKE搜索: {}", e.getMessage());
        }

        List<Plant> results = plantMapper.searchByLike(escapedKeyword, limit);
        log.debug("LIKE搜索找到 {} 条结果: {}", results.size(), keyword);
        return results;
    }

    private String escapeLikeSpecialChars(String keyword) {
        if (keyword == null) {
            return null;
        }
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
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
        return getById(id);
    }

    @Override
    public List<Plant> getRandomByDifficulty(String difficulty, int limit) {
        if (!StringUtils.hasText(difficulty)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "难度不能为空");
        }
        if (limit < MIN_PAGE_SIZE || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    String.format("限制数量必须在%d-%d之间", MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }
        return plantMapper.selectRandomByDifficulty(difficulty, limit);
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            plantMapper.incrementViewCount(id);
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
    @CacheEvict(value = "plants", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Plant plant = getById(id);
        if (plant == null) {
            return;
        }
        fileCleanupHelper.deleteFilesFromJson(plant.getImages());
        fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
        fileCleanupHelper.deleteFilesFromJson(plant.getDocuments());
        removeById(id);
        log.info("Deleted plant {} with associated files", id);
    }
}

package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    private static final Logger log = LoggerFactory.getLogger(PlantServiceImpl.class);

    @Autowired
    private PlantMapper plantMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

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
            throw new RuntimeException("搜索关键词不能为空");
        }
        return list(new LambdaQueryWrapper<Plant>()
                .like(Plant::getNameCn, keyword)
                .or().like(Plant::getNameDong, keyword)
                .or().like(Plant::getEfficacy, keyword)
                .or().like(Plant::getStory, keyword)
                .orderByDesc(Plant::getId));
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
            throw new RuntimeException("难度不能为空");
        }
        if (limit <= 0 || limit > 100) {
            throw new RuntimeException("限制数量必须在1-100之间");
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

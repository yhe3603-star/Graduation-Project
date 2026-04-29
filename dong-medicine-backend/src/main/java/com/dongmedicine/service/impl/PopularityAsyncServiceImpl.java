package com.dongmedicine.service.impl;

import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.PopularityAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularityAsyncServiceImpl implements PopularityAsyncService {

    private final PlantMapper plantMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final InheritorMapper inheritorMapper;

    @Override
    @Async("popularityExecutor")
    public void incrementPlantPopularity(Integer id) {
        try {
            plantMapper.incrementPopularity(id);
            log.debug("Plant popularity incremented async for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment popularity for plant id: {}", id, e);
        }
    }

    @Override
    @Async("popularityExecutor")
    public void incrementKnowledgePopularity(Integer id) {
        try {
            knowledgeMapper.incrementPopularity(id);
            log.debug("Knowledge popularity incremented async for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment popularity for knowledge id: {}", id, e);
        }
    }

    @Override
    @Async("popularityExecutor")
    public void incrementInheritorPopularity(Integer id) {
        try {
            inheritorMapper.incrementPopularity(id);
            log.debug("Inheritor popularity incremented async for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment popularity for inheritor id: {}", id, e);
        }
    }
}

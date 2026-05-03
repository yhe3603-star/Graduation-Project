package com.dongmedicine.service.impl;

import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.mapper.ResourceMapper;
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
    private final QaMapper qaMapper;
    private final ResourceMapper resourceMapper;

    @Override
    @Async("popularityExecutor")
    public void incrementPlantViewAndPopularity(Integer id) {
        try {
            plantMapper.incrementViewCount3AndPopularity(id);
            log.debug("Plant view+3 and popularity+1 for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment plant view/popularity for id: {}", id, e);
        }
    }

    @Override
    @Async("popularityExecutor")
    public void incrementKnowledgeViewAndPopularity(Integer id) {
        try {
            knowledgeMapper.incrementViewCount3AndPopularity(id);
            log.debug("Knowledge view+3 and popularity+1 for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment knowledge view/popularity for id: {}", id, e);
        }
    }

    @Override
    @Async("popularityExecutor")
    public void incrementInheritorViewAndPopularity(Integer id) {
        try {
            inheritorMapper.incrementViewCount3AndPopularity(id);
            log.debug("Inheritor view+3 and popularity+1 for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment inheritor view/popularity for id: {}", id, e);
        }
    }

    @Override
    @Async("popularityExecutor")
    public void incrementQaViewAndPopularity(Integer id) {
        try {
            qaMapper.incrementViewCount3AndPopularity(id);
            log.debug("QA view+3 and popularity+1 for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment QA view/popularity for id: {}", id, e);
        }
    }

    @Override
    @Async("popularityExecutor")
    public void incrementResourceViewAndPopularity(Integer id) {
        try {
            resourceMapper.incrementViewCount3AndPopularity(id);
            log.debug("Resource view+3 and popularity+1 for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment resource view/popularity for id: {}", id, e);
        }
    }
}

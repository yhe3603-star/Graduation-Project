package com.dongmedicine.service.impl;

import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.mapper.ResourceMapper;
import com.dongmedicine.service.PopularityAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void incrementPlantViewAndPopularity(Integer id) {
        plantMapper.incrementViewCount3AndPopularity(id);
    }

    @Override
    public void incrementKnowledgeViewAndPopularity(Integer id) {
        knowledgeMapper.incrementViewCount3AndPopularity(id);
    }

    @Override
    public void incrementInheritorViewAndPopularity(Integer id) {
        inheritorMapper.incrementViewCount3AndPopularity(id);
    }

    @Override
    public void incrementQaViewAndPopularity(Integer id) {
        qaMapper.incrementViewCount3AndPopularity(id);
    }

    @Override
    public void incrementResourceViewAndPopularity(Integer id) {
        resourceMapper.incrementViewCount3AndPopularity(id);
    }
}

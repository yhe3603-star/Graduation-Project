package com.dongmedicine.service.impl;

import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.mapper.ResourceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PopularityAsyncServiceImplTest {

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private InheritorMapper inheritorMapper;

    @Mock
    private QaMapper qaMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private PopularityAsyncServiceImpl popularityAsyncService;

    @Test
    @DisplayName("增加植物浏览量和热度 - 成功委托给plantMapper")
    void testIncrementPlantViewAndPopularity() {
        doNothing().when(plantMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> popularityAsyncService.incrementPlantViewAndPopularity(1));
        verify(plantMapper, times(1)).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加知识浏览量和热度 - 成功委托给knowledgeMapper")
    void testIncrementKnowledgeViewAndPopularity() {
        doNothing().when(knowledgeMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> popularityAsyncService.incrementKnowledgeViewAndPopularity(1));
        verify(knowledgeMapper, times(1)).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加传承人浏览量和热度 - 成功委托给inheritorMapper")
    void testIncrementInheritorViewAndPopularity() {
        doNothing().when(inheritorMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> popularityAsyncService.incrementInheritorViewAndPopularity(1));
        verify(inheritorMapper, times(1)).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加问答浏览量和热度 - 成功委托给qaMapper")
    void testIncrementQaViewAndPopularity() {
        doNothing().when(qaMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> popularityAsyncService.incrementQaViewAndPopularity(1));
        verify(qaMapper, times(1)).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加资源浏览量和热度 - 成功委托给resourceMapper")
    void testIncrementResourceViewAndPopularity() {
        doNothing().when(resourceMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> popularityAsyncService.incrementResourceViewAndPopularity(1));
        verify(resourceMapper, times(1)).incrementViewCount3AndPopularity(1);
    }
}

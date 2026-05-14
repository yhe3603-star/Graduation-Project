package com.dongmedicine.service.impl;

import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.mapper.ResourceMapper;
import com.dongmedicine.service.OperationLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("数据统计服务测试")
class StatisticsServiceImplTest {

    @Mock
    private OperationLogService logService;
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
    private StatisticsServiceImpl statisticsService;

    @Test
    @DisplayName("获取总览数据 - 返回正确的计数")
    void testGetOverview() {
        when(plantMapper.selectCount(any())).thenReturn(100L);
        when(knowledgeMapper.selectCount(any())).thenReturn(50L);
        when(inheritorMapper.selectCount(any())).thenReturn(20L);
        when(qaMapper.selectCount(any())).thenReturn(200L);
        when(resourceMapper.selectCount(any())).thenReturn(30L);
        when(knowledgeMapper.countDistinctTherapyCategory()).thenReturn(8);

        Map<String, Object> overview = statisticsService.getOverview();

        assertNotNull(overview);
        assertEquals(100L, overview.get("plants"));
        assertEquals(50L, overview.get("formulas"));
        assertEquals(20L, overview.get("inheritors"));
        assertEquals(8, overview.get("therapies"));
    }

    @Test
    @DisplayName("获取图表数据 - 包含所有必要字段")
    void testGetChartData() {
        when(plantMapper.selectCount(any())).thenReturn(10L);
        when(knowledgeMapper.selectCount(any())).thenReturn(5L);
        when(inheritorMapper.selectCount(any())).thenReturn(3L);
        when(qaMapper.selectCount(any())).thenReturn(20L);
        when(resourceMapper.selectCount(any())).thenReturn(8L);
        when(knowledgeMapper.countByTherapyCategory(8)).thenReturn(List.of());
        when(inheritorMapper.countByLevel(anyString())).thenReturn(0);
        when(plantMapper.countByCategory(8)).thenReturn(List.of());
        when(qaMapper.topCategoryByPopularity(6)).thenReturn(List.of());
        when(plantMapper.countByDistribution(100)).thenReturn(List.of());
        when(knowledgeMapper.topByPopularity(10)).thenReturn(List.of());
        when(knowledgeMapper.topFormulaByViewCount(8)).thenReturn(List.of());
        when(knowledgeMapper.topByViewCount(8)).thenReturn(List.of());

        Map<String, Object> data = statisticsService.getChartData();

        assertNotNull(data);
        assertTrue(data.containsKey("counts"));
        assertTrue(data.containsKey("therapyCategories"));
        assertTrue(data.containsKey("inheritorLevels"));
    }
}

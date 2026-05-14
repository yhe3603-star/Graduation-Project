package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("数据统计Controller测试")
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private PlantService plantService;

    @Mock
    private KnowledgeService knowledgeService;

    @Mock
    private QaService qaService;

    @Mock
    private InheritorService inheritorService;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private StatisticsController statisticsController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("数据概览 - 成功")
    void testGetOverview_Success() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("plants", 10L);
        overview.put("formulas", 20L);
        overview.put("inheritors", 5L);
        overview.put("therapies", 3);
        when(statisticsService.getOverview()).thenReturn(overview);

        R<Map<String, Object>> result = statisticsController.getOverview();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(10L, result.getData().get("plants"));
        assertEquals(20L, result.getData().get("formulas"));
        assertEquals(5L, result.getData().get("inheritors"));
        assertEquals(3, result.getData().get("therapies"));
    }

    @Test
    @DisplayName("图表数据 - 成功")
    void testGetChartData_Success() {
        Map<String, Object> chartData = new LinkedHashMap<>();
        chartData.put("counts", Map.of("plants", 10L));
        chartData.put("therapyCategories", Collections.emptyList());
        chartData.put("inheritorLevels", List.of(2, 3, 1, 0));
        chartData.put("plantCategoryNames", List.of("清热药"));
        chartData.put("plantCategories", List.of(8L));
        chartData.put("qaCategoryNames", List.of("用药"));
        chartData.put("qaCategories", List.of(10L));
        chartData.put("plantDistribution", Collections.emptyList());
        chartData.put("knowledgePopularity", Collections.emptyList());
        chartData.put("formulaNames", List.of("药方"));
        chartData.put("formulaFreq", List.of(50L));
        when(statisticsService.getChartData()).thenReturn(chartData);

        R<Map<String, Object>> result = statisticsController.getChartData();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("counts"));
        assertTrue(result.getData().containsKey("therapyCategories"));
        assertTrue(result.getData().containsKey("inheritorLevels"));
        assertTrue(result.getData().containsKey("plantCategories"));
        assertTrue(result.getData().containsKey("qaCategories"));
        assertTrue(result.getData().containsKey("plantDistribution"));
        assertTrue(result.getData().containsKey("knowledgePopularity"));
        assertTrue(result.getData().containsKey("formulaNames"));
        assertTrue(result.getData().containsKey("formulaFreq"));
    }

    @Test
    @DisplayName("图表数据 - 药方数据为空时回退到topByViewCount")
    void testGetChartData_FormulaFallback() {
        Map<String, Object> chartData = new LinkedHashMap<>();
        chartData.put("counts", Map.of());
        chartData.put("therapyCategories", Collections.emptyList());
        chartData.put("inheritorLevels", List.of(0, 0, 0, 0));
        chartData.put("plantCategoryNames", Collections.emptyList());
        chartData.put("plantCategories", Collections.emptyList());
        chartData.put("qaCategoryNames", Collections.emptyList());
        chartData.put("qaCategories", Collections.emptyList());
        chartData.put("plantDistribution", Collections.emptyList());
        chartData.put("knowledgePopularity", Collections.emptyList());
        chartData.put("formulaNames", List.of("知识"));
        chartData.put("formulaFreq", List.of(30L));
        when(statisticsService.getChartData()).thenReturn(chartData);

        R<Map<String, Object>> result = statisticsController.getChartData();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("访问趋势 - 成功")
    void testGetVisitTrend_Success() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDate.now().format(formatter);

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("M/d");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(displayFormatter));
            counts.add(date.format(formatter).equals(today) ? 42L : 0L);
        }
        Map<String, Object> trendData = new LinkedHashMap<>();
        trendData.put("dates", dates);
        trendData.put("counts", counts);
        when(statisticsService.getVisitTrend()).thenReturn(trendData);

        R<Map<String, Object>> result = statisticsController.getVisitTrend();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("dates"));
        assertTrue(result.getData().containsKey("counts"));

        List<String> resultDates = (List<String>) result.getData().get("dates");
        List<Long> resultCounts = (List<Long>) result.getData().get("counts");
        assertEquals(7, resultDates.size());
        assertEquals(7, resultCounts.size());
    }

    @Test
    @DisplayName("访问趋势 - 空数据")
    void testGetVisitTrend_Empty() {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("M/d");
        for (int i = 6; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(displayFormatter));
            counts.add(0L);
        }
        Map<String, Object> trendData = new LinkedHashMap<>();
        trendData.put("dates", dates);
        trendData.put("counts", counts);
        when(statisticsService.getVisitTrend()).thenReturn(trendData);

        R<Map<String, Object>> result = statisticsController.getVisitTrend();

        assertEquals(200, result.getCode());
        List<Long> resultCounts = (List<Long>) result.getData().get("counts");
        assertEquals(7, resultCounts.size());
        assertTrue(resultCounts.stream().allMatch(c -> c == 0L));
    }

    @Test
    @DisplayName("植物统计 - 成功")
    void testPlantStats_Success() {
        Map<String, Object> stats = Map.of("total", 100, "categoryCount", 10);
        when(plantService.getStats()).thenReturn(stats);

        R<Map<String, Object>> result = statisticsController.plantStats();

        assertEquals(200, result.getCode());
        assertEquals(stats, result.getData());
    }

    @Test
    @DisplayName("知识统计 - 成功")
    void testKnowledgeStats_Success() {
        Map<String, Object> stats = Map.of("total", 50, "therapyCategoryCount", 5);
        when(knowledgeService.getStats()).thenReturn(stats);

        R<Map<String, Object>> result = statisticsController.knowledgeStats();

        assertEquals(200, result.getCode());
        assertEquals(stats, result.getData());
    }

    @Test
    @DisplayName("问答统计 - 成功")
    void testQaStats_Success() {
        Map<String, Object> stats = Map.of("total", 200, "categoryCount", 8);
        when(qaService.getStats()).thenReturn(stats);

        R<Map<String, Object>> result = statisticsController.qaStats();

        assertEquals(200, result.getCode());
        assertEquals(stats, result.getData());
    }

    @Test
    @DisplayName("传承人统计 - 成功")
    void testInheritorStats_Success() {
        Map<String, Object> stats = Map.of("total", 30, "regionLevelCount", 10);
        when(inheritorService.getStats()).thenReturn(stats);

        R<Map<String, Object>> result = statisticsController.inheritorStats();

        assertEquals(200, result.getCode());
        assertEquals(stats, result.getData());
    }

    @Test
    @DisplayName("资源统计 - 成功")
    void testResourceStats_Success() {
        Map<String, Object> stats = Map.of("total", 60, "videoCount", 20);
        when(resourceService.getStats()).thenReturn(stats);

        R<Map<String, Object>> result = statisticsController.resourceStats();

        assertEquals(200, result.getCode());
        assertEquals(stats, result.getData());
    }
}

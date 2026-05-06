package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.mapper.*;
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
    private OperationLogService logService;

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
        when(plantMapper.selectCount(isNull())).thenReturn(10L);
        when(knowledgeMapper.selectCount(isNull())).thenReturn(20L);
        when(inheritorMapper.selectCount(isNull())).thenReturn(5L);
        when(qaMapper.selectCount(isNull())).thenReturn(15L);
        when(resourceMapper.selectCount(isNull())).thenReturn(8L);
        when(knowledgeMapper.countDistinctTherapyCategory()).thenReturn(3);

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
        when(plantMapper.selectCount(isNull())).thenReturn(10L);
        when(knowledgeMapper.selectCount(isNull())).thenReturn(20L);
        when(inheritorMapper.selectCount(isNull())).thenReturn(5L);
        when(qaMapper.selectCount(isNull())).thenReturn(15L);
        when(resourceMapper.selectCount(isNull())).thenReturn(8L);

        List<Map<String, Object>> therapyCategories = new ArrayList<>();
        Map<String, Object> tc = new LinkedHashMap<>();
        tc.put("name", "推拿");
        tc.put("value", 5);
        therapyCategories.add(tc);
        when(knowledgeMapper.countByTherapyCategory(8)).thenReturn(therapyCategories);

        when(inheritorMapper.countByLevel("国家级")).thenReturn(2);
        when(inheritorMapper.countByLevel("省级")).thenReturn(3);
        when(inheritorMapper.countByLevel("市级")).thenReturn(1);
        when(inheritorMapper.countByLevel("县级")).thenReturn(0);

        List<Map<String, Object>> plantCategories = new ArrayList<>();
        Map<String, Object> pc = new LinkedHashMap<>();
        pc.put("name", "清热药");
        pc.put("value", 8L);
        plantCategories.add(pc);
        when(plantMapper.countByCategory(8)).thenReturn(plantCategories);

        List<Map<String, Object>> qaCategories = new ArrayList<>();
        Map<String, Object> qc = new LinkedHashMap<>();
        qc.put("name", "用药");
        qc.put("value", 10L);
        qaCategories.add(qc);
        when(qaMapper.topCategoryByPopularity(6)).thenReturn(qaCategories);

        when(plantMapper.countByDistribution(100)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> knowledgePop = new ArrayList<>();
        Map<String, Object> kp = new LinkedHashMap<>();
        kp.put("name", "侗药");
        kp.put("value", 100);
        knowledgePop.add(kp);
        when(knowledgeMapper.topByPopularity(10)).thenReturn(knowledgePop);

        List<Map<String, Object>> formulaData = new ArrayList<>();
        Map<String, Object> fd = new LinkedHashMap<>();
        fd.put("name", "药方");
        fd.put("value", 50L);
        formulaData.add(fd);
        when(knowledgeMapper.topFormulaByViewCount(8)).thenReturn(formulaData);

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
        when(plantMapper.selectCount(isNull())).thenReturn(0L);
        when(knowledgeMapper.selectCount(isNull())).thenReturn(0L);
        when(inheritorMapper.selectCount(isNull())).thenReturn(0L);
        when(qaMapper.selectCount(isNull())).thenReturn(0L);
        when(resourceMapper.selectCount(isNull())).thenReturn(0L);
        when(knowledgeMapper.countByTherapyCategory(8)).thenReturn(Collections.emptyList());
        when(inheritorMapper.countByLevel("国家级")).thenReturn(0);
        when(inheritorMapper.countByLevel("省级")).thenReturn(0);
        when(inheritorMapper.countByLevel("市级")).thenReturn(0);
        when(inheritorMapper.countByLevel("县级")).thenReturn(0);
        when(plantMapper.countByCategory(8)).thenReturn(Collections.emptyList());
        when(qaMapper.topCategoryByPopularity(6)).thenReturn(Collections.emptyList());
        when(plantMapper.countByDistribution(100)).thenReturn(Collections.emptyList());
        when(knowledgeMapper.topByPopularity(10)).thenReturn(Collections.emptyList());

        when(knowledgeMapper.topFormulaByViewCount(8)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> fallbackData = new ArrayList<>();
        Map<String, Object> fb = new LinkedHashMap<>();
        fb.put("name", "知识");
        fb.put("value", 30L);
        fallbackData.add(fb);
        when(knowledgeMapper.topByViewCount(8)).thenReturn(fallbackData);

        R<Map<String, Object>> result = statisticsController.getChartData();

        assertEquals(200, result.getCode());
        verify(knowledgeMapper).topFormulaByViewCount(8);
        verify(knowledgeMapper).topByViewCount(8);
    }

    @Test
    @DisplayName("访问趋势 - 成功")
    void testGetVisitTrend_Success() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDate.now().format(formatter);

        List<Map<String, Object>> dbData = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("date", today);
        row.put("count", 42);
        dbData.add(row);

        when(logService.getTrendLast7Days()).thenReturn(dbData);

        R<Map<String, Object>> result = statisticsController.getVisitTrend();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("dates"));
        assertTrue(result.getData().containsKey("counts"));

        List<String> dates = (List<String>) result.getData().get("dates");
        List<Long> counts = (List<Long>) result.getData().get("counts");
        assertEquals(7, dates.size());
        assertEquals(7, counts.size());
    }

    @Test
    @DisplayName("访问趋势 - 空数据")
    void testGetVisitTrend_Empty() {
        when(logService.getTrendLast7Days()).thenReturn(Collections.emptyList());

        R<Map<String, Object>> result = statisticsController.getVisitTrend();

        assertEquals(200, result.getCode());
        List<Long> counts = (List<Long>) result.getData().get("counts");
        assertEquals(7, counts.size());
        assertTrue(counts.stream().allMatch(c -> c == 0L));
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

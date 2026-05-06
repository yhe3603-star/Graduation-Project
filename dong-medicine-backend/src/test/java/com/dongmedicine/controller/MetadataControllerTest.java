package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("元数据Controller测试")
class MetadataControllerTest {

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
    private MetadataController self;

    @InjectMocks
    private MetadataController metadataController;

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
    @DisplayName("获取全部筛选条件 - 成功")
    void testGetAllFilters_Success() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("plants", Map.of("category", List.of("清热药")));
        filters.put("knowledge", Map.of("therapyCategory", List.of("推拿")));
        filters.put("qa", Map.of("category", List.of("用药")));
        filters.put("inheritors", Map.of("level", List.of("国家级")));
        filters.put("resources", Map.of("category", List.of("视频")));
        when(self.getAllFiltersData()).thenReturn(filters);

        R<Map<String, Object>> result = metadataController.getAllFilters();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("plants"));
        assertTrue(result.getData().containsKey("knowledge"));
        assertTrue(result.getData().containsKey("qa"));
        assertTrue(result.getData().containsKey("inheritors"));
        assertTrue(result.getData().containsKey("resources"));
    }

    @Test
    @DisplayName("获取精选内容 - 成功（有数据）")
    void testGetFeatured_Success() {
        Plant plant = new Plant();
        plant.setId(1);
        plant.setNameCn("钩藤");
        plant.setEfficacy("清热");
        plant.setViewCount(100);
        plant.setFavoriteCount(50);
        plant.setPopularity(200);

        Knowledge knowledge = new Knowledge();
        knowledge.setId(2);
        knowledge.setTitle("侗医药疗法");
        knowledge.setContent("详细内容");
        knowledge.setViewCount(80);
        knowledge.setFavoriteCount(30);
        knowledge.setPopularity(150);

        Inheritor inheritor = new Inheritor();
        inheritor.setId(3);
        inheritor.setName("传承人甲");
        inheritor.setBio("传承人简介");
        inheritor.setViewCount(60);
        inheritor.setFavoriteCount(20);
        inheritor.setPopularity(100);

        Map<String, Object> featured = new LinkedHashMap<>();
        featured.put("plant", Map.of("id", 1, "title", "钩藤"));
        featured.put("knowledge", Map.of("id", 2, "title", "侗医药疗法"));
        featured.put("inheritor", Map.of("id", 3, "title", "传承人甲"));
        featured.put("top", Map.of("id", 1, "title", "钩藤"));

        when(self.getFeaturedData()).thenReturn(featured);

        R<Map<String, Object>> result = metadataController.getFeatured();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("top"));
    }

    @Test
    @DisplayName("获取精选内容 - 直接调用getAllFiltersData")
    void testGetAllFiltersData_Success() {
        when(plantService.getFilterOptions()).thenReturn(Map.of("category", List.of("清热药")));
        when(knowledgeService.getFilterOptions()).thenReturn(Map.of("therapyCategory", List.of("推拿")));
        when(qaService.getFilterOptions()).thenReturn(Map.of("category", List.of("用药")));
        when(inheritorService.getFilterOptions()).thenReturn(Map.of("level", List.of("国家级")));
        when(resourceService.getFilterOptions()).thenReturn(Map.of("category", List.of("视频")));

        Map<String, Object> result = metadataController.getAllFiltersData();

        assertNotNull(result);
        assertTrue(result.containsKey("plants"));
        assertTrue(result.containsKey("knowledge"));
        assertTrue(result.containsKey("qa"));
        assertTrue(result.containsKey("inheritors"));
        assertTrue(result.containsKey("resources"));
        verify(plantService).getFilterOptions();
        verify(knowledgeService).getFilterOptions();
        verify(qaService).getFilterOptions();
        verify(inheritorService).getFilterOptions();
        verify(resourceService).getFilterOptions();
    }

    @Test
    @DisplayName("获取精选内容 - 直接调用getFeaturedData（有植物、知识、传承人）")
    void testGetFeaturedData_AllPresent() {
        Plant plant = new Plant();
        plant.setId(1);
        plant.setNameCn("钩藤");
        plant.setEfficacy("清热");
        plant.setViewCount(100);
        plant.setFavoriteCount(50);
        plant.setPopularity(200);

        when(plantMapper.selectRandomPlants(10)).thenReturn(List.of(plant));

        Knowledge knowledge = new Knowledge();
        knowledge.setId(2);
        knowledge.setTitle("侗医药疗法");
        knowledge.setContent("详细内容");
        knowledge.setViewCount(80);
        knowledge.setFavoriteCount(30);
        knowledge.setPopularity(150);
        when(knowledgeService.list()).thenReturn(List.of(knowledge));

        Inheritor inheritor = new Inheritor();
        inheritor.setId(3);
        inheritor.setName("传承人甲");
        inheritor.setBio("简介");
        inheritor.setViewCount(60);
        inheritor.setFavoriteCount(20);
        inheritor.setPopularity(100);
        when(inheritorMapper.selectList(isNull())).thenReturn(List.of(inheritor));

        Map<String, Object> result = metadataController.getFeaturedData();

        assertNotNull(result);
        assertTrue(result.containsKey("plant"));
        assertTrue(result.containsKey("knowledge"));
        assertTrue(result.containsKey("inheritor"));
        assertTrue(result.containsKey("top"));
    }

    @Test
    @DisplayName("获取精选内容 - 空数据")
    void testGetFeaturedData_Empty() {
        when(plantMapper.selectRandomPlants(10)).thenReturn(Collections.emptyList());
        when(knowledgeService.list()).thenReturn(Collections.emptyList());
        when(inheritorMapper.selectList(isNull())).thenReturn(Collections.emptyList());

        Map<String, Object> result = metadataController.getFeaturedData();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

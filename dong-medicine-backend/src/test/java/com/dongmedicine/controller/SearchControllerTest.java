package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.SearchHistory;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("搜索建议Controller测试")
class SearchControllerTest {

    @Mock
    private PlantService plantService;

    @Mock
    private KnowledgeService knowledgeService;

    @Mock
    private InheritorService inheritorService;

    @Mock
    private SearchHistoryMapper searchHistoryMapper;

    @InjectMocks
    private SearchController searchController;

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
    @DisplayName("搜索建议 - 有关键词返回结果")
    void testSuggest_WithKeyword() {
        Plant plant = new Plant();
        plant.setId(1);
        plant.setNameCn("钩藤");
        plant.setCategory("清热药");

        Knowledge knowledge = new Knowledge();
        knowledge.setId(10);
        knowledge.setTitle("侗医药疗法");
        knowledge.setType("therapy");

        Inheritor inheritor = new Inheritor();
        inheritor.setId(20);
        inheritor.setName("传承人甲");
        inheritor.setLevel("国家级");

        Page<Plant> plantPage = new Page<>(1, 5, 1);
        plantPage.setRecords(List.of(plant));

        Page<Knowledge> knowledgePage = new Page<>(1, 5, 1);
        knowledgePage.setRecords(List.of(knowledge));

        Page<Inheritor> inheritorPage = new Page<>(1, 5, 1);
        inheritorPage.setRecords(List.of(inheritor));

        when(plantService.searchPaged(eq("侗"), eq(1), eq(5))).thenReturn(plantPage);
        when(knowledgeService.advancedSearchPaged(eq("侗"), isNull(), isNull(), isNull(), eq("popularity"), eq(1), eq(5))).thenReturn(knowledgePage);
        when(inheritorService.searchPaged(eq("侗"), eq(1), eq(5))).thenReturn(inheritorPage);

        R<List<Map<String, Object>>> result = searchController.suggest("侗");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().size() > 0);
    }

    @Test
    @DisplayName("搜索建议 - 空关键词返回空列表")
    void testSuggest_EmptyKeyword() {
        R<List<Map<String, Object>>> result = searchController.suggest("");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("搜索建议 - null关键词返回空列表")
    void testSuggest_NullKeyword() {
        R<List<Map<String, Object>>> result = searchController.suggest(null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("搜索建议 - 空白关键词返回空列表")
    void testSuggest_BlankKeyword() {
        R<List<Map<String, Object>>> result = searchController.suggest("   ");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("搜索建议 - 结果限制在15条以内")
    void testSuggest_LimitTo15() {
        List<Plant> plants = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Plant p = new Plant();
            p.setId(i);
            p.setNameCn("植物" + i);
            p.setCategory("分类");
            plants.add(p);
        }

        List<Knowledge> knowledges = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Knowledge k = new Knowledge();
            k.setId(100 + i);
            k.setTitle("知识" + i);
            k.setType("therapy");
            knowledges.add(k);
        }

        List<Inheritor> inheritors = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Inheritor h = new Inheritor();
            h.setId(200 + i);
            h.setName("传承人" + i);
            h.setLevel("省级");
            inheritors.add(h);
        }

        Page<Plant> plantPage = new Page<>(1, 5, 15);
        plantPage.setRecords(plants);
        Page<Knowledge> knowledgePage = new Page<>(1, 5, 15);
        knowledgePage.setRecords(knowledges);
        Page<Inheritor> inheritorPage = new Page<>(1, 5, 15);
        inheritorPage.setRecords(inheritors);

        when(plantService.searchPaged(eq("药"), eq(1), eq(5))).thenReturn(plantPage);
        when(knowledgeService.advancedSearchPaged(eq("药"), isNull(), isNull(), isNull(), eq("popularity"), eq(1), eq(5))).thenReturn(knowledgePage);
        when(inheritorService.searchPaged(eq("药"), eq(1), eq(5))).thenReturn(inheritorPage);

        R<List<Map<String, Object>>> result = searchController.suggest("药");

        assertEquals(200, result.getCode());
        assertTrue(result.getData().size() <= 15);
    }

    @Test
    @DisplayName("搜索建议 - 记录搜索历史失败不影响结果")
    void testSuggest_SearchHistoryInsertFails() {
        Page<Plant> emptyPlantPage = new Page<>(1, 5, 0);
        emptyPlantPage.setRecords(Collections.emptyList());
        Page<Knowledge> emptyKnowledgePage = new Page<>(1, 5, 0);
        emptyKnowledgePage.setRecords(Collections.emptyList());
        Page<Inheritor> emptyInheritorPage = new Page<>(1, 5, 0);
        emptyInheritorPage.setRecords(Collections.emptyList());

        when(searchHistoryMapper.insert(any(SearchHistory.class))).thenThrow(new RuntimeException("DB error"));
        when(plantService.searchPaged(eq("测试"), eq(1), eq(5))).thenReturn(emptyPlantPage);
        when(knowledgeService.advancedSearchPaged(eq("测试"), isNull(), isNull(), isNull(), eq("popularity"), eq(1), eq(5))).thenReturn(emptyKnowledgePage);
        when(inheritorService.searchPaged(eq("测试"), eq(1), eq(5))).thenReturn(emptyInheritorPage);

        R<List<Map<String, Object>>> result = searchController.suggest("测试");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("热门关键词 - 返回成功")
    void testHotKeywords_Success() {
        List<Map<String, Object>> hotKeywords = new ArrayList<>();
        Map<String, Object> kw1 = new LinkedHashMap<>();
        kw1.put("name", "侗药");
        kw1.put("value", 50);
        hotKeywords.add(kw1);

        when(searchHistoryMapper.topKeywords(20)).thenReturn(hotKeywords);

        R<List<Map<String, Object>>> result = searchController.hotKeywords(20);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(searchHistoryMapper).topKeywords(20);
    }

    @Test
    @DisplayName("热门关键词 - limit超过50限制为50")
    void testHotKeywords_LimitExceeded() {
        when(searchHistoryMapper.topKeywords(50)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = searchController.hotKeywords(100);

        assertEquals(200, result.getCode());
        verify(searchHistoryMapper).topKeywords(50);
    }

    @Test
    @DisplayName("热门关键词 - 默认limit为20")
    void testHotKeywords_DefaultLimit() {
        when(searchHistoryMapper.topKeywords(20)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = searchController.hotKeywords(20);

        assertEquals(200, result.getCode());
        verify(searchHistoryMapper).topKeywords(20);
    }
}

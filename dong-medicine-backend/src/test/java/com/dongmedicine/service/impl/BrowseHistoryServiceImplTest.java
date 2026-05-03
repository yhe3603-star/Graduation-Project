package com.dongmedicine.service.impl;

import com.dongmedicine.entity.*;
import com.dongmedicine.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BrowseHistoryServiceImplTest {

    @Mock
    private BrowseHistoryMapper browseHistoryMapper;

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private InheritorMapper inheritorMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private QaMapper qaMapper;

    private BrowseHistoryServiceImpl browseHistoryService;

    @BeforeEach
    void setUp() throws Exception {
        browseHistoryService = new BrowseHistoryServiceImpl(
                plantMapper, knowledgeMapper, inheritorMapper,
                resourceMapper, qaMapper);
        setBaseMapper(browseHistoryService, browseHistoryMapper);
    }

    private void setBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Test
    @DisplayName("record - new browse history calls insert")
    void recordNew() {
        when(browseHistoryMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(browseHistoryMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        browseHistoryService.record(1, "plant", 10);

        verify(browseHistoryMapper).insert(any(BrowseHistory.class));
    }

    @Test
    @DisplayName("record - should delete expired history before recording")
    void recordDeletesExpiredHistory() {
        when(browseHistoryMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(browseHistoryMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        browseHistoryService.record(1, "knowledge", 20);

        verify(browseHistoryMapper).delete(any());
    }

    @Test
    @DisplayName("record - should trim history to 30 records")
    void recordTrimsTo30Records() {
        List<BrowseHistory> manyHistories = new ArrayList<>();
        for (int i = 1; i <= 35; i++) {
            BrowseHistory h = createHistory((long) i, 1, "plant", i);
            h.setCreatedAt(LocalDateTime.now().minusHours(i));
            manyHistories.add(h);
        }
        
        when(browseHistoryMapper.selectList(any())).thenReturn(manyHistories);
        when(browseHistoryMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(browseHistoryMapper.deleteByIds(anyList())).thenReturn(5);

        browseHistoryService.record(1, "plant", 100);

        verify(browseHistoryMapper).deleteByIds(anyList());
    }

    @Test
    @DisplayName("getMyHistory - should return deduplicated results")
    void getMyHistoryDedup() {
        BrowseHistory h1 = createHistory(1L, 1, "plant", 10);
        BrowseHistory h2 = createHistory(2L, 1, "plant", 10);
        BrowseHistory h3 = createHistory(3L, 1, "knowledge", 20);

        when(browseHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h2, h3));
        mockPlantForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals(2, result.size());
        assertEquals("plant", result.get(0).get("targetType"));
    }

    @Test
    @DisplayName("getMyHistory - should respect limit and max 30")
    void getMyHistoryLimit() {
        BrowseHistory h1 = createHistory(1L, 1, "plant", 1);
        BrowseHistory h2 = createHistory(2L, 1, "plant", 2);
        BrowseHistory h3 = createHistory(3L, 1, "plant", 3);

        when(browseHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h2, h3));
        mockPlantForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 2);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getMyHistory - empty result returns empty list")
    void getMyHistoryEmpty() {
        when(browseHistoryMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMyHistory - should return proper fields")
    void getMyHistoryFields() {
        BrowseHistory h = createHistory(1L, 1, "inheritor", 5);
        h.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 30));

        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        mockInheritorForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);
        Map<String, Object> entry = result.get(0);

        assertEquals(1L, entry.get("id"));
        assertEquals(1, entry.get("userId"));
        assertEquals("inheritor", entry.get("targetType"));
        assertEquals(5, entry.get("targetId"));
        assertEquals(h.getCreatedAt(), entry.get("browseTime"));
        assertNotNull(entry.get("title"));
        assertNotNull(entry.get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should delete expired history")
    void getMyHistoryDeletesExpired() {
        BrowseHistory valid = createHistory(2L, 1, "knowledge", 20);
        valid.setCreatedAt(LocalDateTime.now());

        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(valid));

        browseHistoryService.getMyHistory(1, 10);

        verify(browseHistoryMapper).delete(any());
    }

    @Test
    @DisplayName("getMyHistory - should limit to 30 records max")
    void getMyHistoryMax30Records() {
        List<BrowseHistory> manyHistories = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            BrowseHistory h = createHistory((long) i, 1, "plant", i);
            h.setCreatedAt(LocalDateTime.now().minusHours(i));
            manyHistories.add(h);
        }

        when(browseHistoryMapper.selectList(any())).thenReturn(manyHistories);
        mockPlantForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 100);

        assertTrue(result.size() <= 30);
    }

    @Test
    @DisplayName("getMyHistory - should return title and description for plant")
    void getMyHistoryPlantTitleDesc() {
        BrowseHistory h = createHistory(1L, 1, "plant", 10);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        mockPlantForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals("测试药材", result.get(0).get("title"));
        assertEquals("测试功效", result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should return title and description for knowledge")
    void getMyHistoryKnowledgeTitleDesc() {
        BrowseHistory h = createHistory(1L, 1, "knowledge", 10);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        mockKnowledgeForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals("测试知识标题", result.get(0).get("title"));
        assertEquals("测试知识内容", result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should return title and description for inheritor")
    void getMyHistoryInheritorTitleDesc() {
        BrowseHistory h = createHistory(1L, 1, "inheritor", 10);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        mockInheritorForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals("测试传承人", result.get(0).get("title"));
        assertEquals("测试简介", result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should return title and description for resource")
    void getMyHistoryResourceTitleDesc() {
        BrowseHistory h = createHistory(1L, 1, "resource", 10);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        mockResourceForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals("测试资源标题", result.get(0).get("title"));
        assertEquals("测试资源描述", result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should return title and description for qa")
    void getMyHistoryQaTitleDesc() {
        BrowseHistory h = createHistory(1L, 1, "qa", 10);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        mockQaForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals("测试问题?", result.get(0).get("title"));
        assertEquals("测试答案", result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should handle null entity gracefully")
    void getMyHistoryNullEntity() {
        BrowseHistory h = createHistory(1L, 1, "plant", 999);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));
        when(plantMapper.selectById(999)).thenReturn(null);

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertNull(result.get(0).get("title"));
        assertNull(result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should handle unknown target type")
    void getMyHistoryUnknownType() {
        BrowseHistory h = createHistory(1L, 1, "unknown", 10);
        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertNull(result.get(0).get("title"));
        assertNull(result.get(0).get("description"));
    }

    @Test
    @DisplayName("getMyHistory - should handle exception gracefully")
    void getMyHistoryException() {
        when(browseHistoryMapper.selectList(any())).thenThrow(new RuntimeException("DB error"));

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMyHistory - should preserve order from database")
    void getMyHistoryPreservesOrder() {
        BrowseHistory h1 = createHistory(1L, 1, "plant", 1);
        h1.setCreatedAt(LocalDateTime.now().minusHours(1));
        BrowseHistory h2 = createHistory(2L, 1, "plant", 2);
        h2.setCreatedAt(LocalDateTime.now().minusHours(2));
        BrowseHistory h3 = createHistory(3L, 1, "plant", 3);
        h3.setCreatedAt(LocalDateTime.now().minusHours(3));

        when(browseHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h2, h3));
        mockPlantForTitle();

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).get("targetId"));
        assertEquals(2, result.get(1).get("targetId"));
        assertEquals(3, result.get(2).get("targetId"));
    }

    private BrowseHistory createHistory(Long id, Integer userId, String targetType, Integer targetId) {
        BrowseHistory h = new BrowseHistory();
        h.setId(id);
        h.setUserId(userId);
        h.setTargetType(targetType);
        h.setTargetId(targetId);
        h.setCreatedAt(LocalDateTime.now().minusHours(id));
        return h;
    }

    private void mockPlantForTitle() {
        Plant plant = new Plant();
        plant.setNameCn("测试药材");
        plant.setEfficacy("测试功效");
        when(plantMapper.selectById(anyInt())).thenReturn(plant);
    }

    private void mockKnowledgeForTitle() {
        Knowledge knowledge = new Knowledge();
        knowledge.setTitle("测试知识标题");
        knowledge.setContent("测试知识内容");
        when(knowledgeMapper.selectById(anyInt())).thenReturn(knowledge);
    }

    private void mockInheritorForTitle() {
        Inheritor inheritor = new Inheritor();
        inheritor.setName("测试传承人");
        inheritor.setBio("测试简介");
        when(inheritorMapper.selectById(anyInt())).thenReturn(inheritor);
    }

    private void mockResourceForTitle() {
        Resource resource = new Resource();
        resource.setTitle("测试资源标题");
        resource.setDescription("测试资源描述");
        when(resourceMapper.selectById(anyInt())).thenReturn(resource);
    }

    private void mockQaForTitle() {
        Qa qa = new Qa();
        qa.setQuestion("测试问题?");
        qa.setAnswer("测试答案");
        when(qaMapper.selectById(anyInt())).thenReturn(qa);
    }
}

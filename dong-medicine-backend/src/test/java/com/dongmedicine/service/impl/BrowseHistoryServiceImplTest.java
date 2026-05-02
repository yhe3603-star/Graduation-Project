package com.dongmedicine.service.impl;

import com.dongmedicine.entity.BrowseHistory;
import com.dongmedicine.mapper.BrowseHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
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

    private BrowseHistoryServiceImpl browseHistoryService;

    @BeforeEach
    void setUp() throws Exception {
        browseHistoryService = new BrowseHistoryServiceImpl();
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
        when(browseHistoryMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        browseHistoryService.record(1, "plant", 10);

        verify(browseHistoryMapper).insert(any(BrowseHistory.class));
    }

    @Test
    @DisplayName("getMyHistory - should return deduplicated results")
    void getMyHistoryDedup() {
        BrowseHistory h1 = createHistory(1L, 1, "plant", 10);
        BrowseHistory h2 = createHistory(2L, 1, "plant", 10); // same target
        BrowseHistory h3 = createHistory(3L, 1, "knowledge", 20);

        when(browseHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h2, h3));

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);

        assertEquals(2, result.size());
        assertEquals("plant", result.get(0).get("targetType"));
    }

    @Test
    @DisplayName("getMyHistory - should respect limit")
    void getMyHistoryLimit() {
        BrowseHistory h1 = createHistory(1L, 1, "plant", 1);
        BrowseHistory h2 = createHistory(2L, 1, "plant", 2);
        BrowseHistory h3 = createHistory(3L, 1, "plant", 3);

        when(browseHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h2, h3));

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
        h.setCreatedAt(java.time.LocalDateTime.of(2025, 1, 15, 10, 30));

        when(browseHistoryMapper.selectList(any())).thenReturn(List.of(h));

        List<Map<String, Object>> result = browseHistoryService.getMyHistory(1, 10);
        Map<String, Object> entry = result.get(0);

        assertEquals(1L, entry.get("id"));
        assertEquals(1, entry.get("userId"));
        assertEquals("inheritor", entry.get("targetType"));
        assertEquals(5, entry.get("targetId"));
        assertEquals(h.getCreatedAt(), entry.get("browseTime"));
    }

    private BrowseHistory createHistory(Long id, Integer userId, String targetType, Integer targetId) {
        BrowseHistory h = new BrowseHistory();
        h.setId(id);
        h.setUserId(userId);
        h.setTargetType(targetType);
        h.setTargetId(targetId);
        h.setCreatedAt(java.time.LocalDateTime.now().minusHours(id));
        return h;
    }
}

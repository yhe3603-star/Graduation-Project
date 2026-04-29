package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.service.FavoriteService;
import com.dongmedicine.service.FeedbackService;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.service.PopularityAsyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeServiceImplTest {

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private FileCleanupHelper fileCleanupHelper;

    @Mock
    private PopularityAsyncService popularityAsyncService;

    private KnowledgeServiceImpl knowledgeService;

    @BeforeEach
    void setUp() throws Exception {
        knowledgeService = new KnowledgeServiceImpl(favoriteService, feedbackService, fileCleanupHelper, popularityAsyncService);
        setBaseMapper(knowledgeService, knowledgeMapper);
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
    @DisplayName("搜索知识 - 返回空列表")
    void searchEmpty() {
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Knowledge> result = knowledgeService.search("不存在的关键词");
        assertNotNull(result);
    }

    @Test
    @DisplayName("获取详情 - 存在的ID")
    void getDetailByIdExists() {
        Knowledge knowledge = new Knowledge();
        knowledge.setId(1);
        knowledge.setTitle("药浴疗法");
        when(knowledgeMapper.selectById(1)).thenReturn(knowledge);

        Knowledge result = knowledgeService.getDetailById(1);
        assertNotNull(result);
        assertEquals("药浴疗法", result.getTitle());
    }

    @Test
    @DisplayName("获取详情 - 不存在的ID")
    void getDetailByIdNotExists() {
        when(knowledgeMapper.selectById(999)).thenReturn(null);

        Knowledge result = knowledgeService.getDetailById(999);
        assertNull(result);
    }

    @Test
    @DisplayName("增加浏览次数")
    void incrementViewCount() {
        doNothing().when(knowledgeMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> {
            knowledgeService.incrementViewCount(1);
        });
    }

    @Test
    @DisplayName("增加浏览次数 - 异常被静默处理")
    void incrementViewCountExceptionHandled() {
        doThrow(new RuntimeException("DB error")).when(knowledgeMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> {
            knowledgeService.incrementViewCount(1);
        });
    }

    @Test
    @DisplayName("获取所有疗法 - 空列表")
    void getAllTherapiesEmpty() {
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Knowledge> result = knowledgeService.getAllTherapies();
        assertNotNull(result);
    }

    @Test
    @DisplayName("获取所有疾病 - 空列表")
    void getAllDiseasesEmpty() {
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Knowledge> result = knowledgeService.getAllDiseases();
        assertNotNull(result);
    }

    @Test
    @DisplayName("搜索知识 - 空关键词返回全部")
    void searchEmptyKeyword() {
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Knowledge> result = knowledgeService.search("");
        assertNotNull(result);
    }

    @Test
    @DisplayName("获取详情并增加人气")
    void getDetailWithRelated() {
        Knowledge knowledge = new Knowledge();
        knowledge.setId(1);
        knowledge.setTitle("药浴疗法");
        when(knowledgeMapper.selectById(1)).thenReturn(knowledge);
        doNothing().when(popularityAsyncService).incrementKnowledgePopularity(anyInt());

        Knowledge result = knowledgeService.getDetailWithRelated(1);
        assertNotNull(result);
        assertEquals("药浴疗法", result.getTitle());
        verify(popularityAsyncService).incrementKnowledgePopularity(1);
    }

    @Test
    @DisplayName("获取详情并增加人气 - 知识不存在")
    void getDetailWithRelatedNotFound() {
        when(knowledgeMapper.selectById(999)).thenReturn(null);

        Knowledge result = knowledgeService.getDetailWithRelated(999);
        assertNull(result);
        verify(popularityAsyncService, never()).incrementKnowledgePopularity(anyInt());
    }
}

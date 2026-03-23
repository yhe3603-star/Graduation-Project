package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.service.FavoriteService;
import com.dongmedicine.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("知识库服务测试")
class KnowledgeServiceImplTest {

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private KnowledgeServiceImpl knowledgeService;

    private Knowledge testKnowledge;

    @BeforeEach
    void setUp() {
        testKnowledge = new Knowledge();
        testKnowledge.setId(1);
        testKnowledge.setTitle("侗医火攻疗法");
        testKnowledge.setType("therapy");
        testKnowledge.setTherapyCategory("火攻");
        testKnowledge.setDiseaseCategory("风湿");
        testKnowledge.setContent("测试内容");
        testKnowledge.setViewCount(100);
        testKnowledge.setPopularity(50);
    }

    @Test
    @DisplayName("高级搜索 - 成功")
    void testAdvancedSearch_Success() {
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testKnowledge));

        List<Knowledge> result = knowledgeService.advancedSearch("侗医", "火攻", "风湿", null, "popularity");

        assertNotNull(result);
        verify(knowledgeMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("高级搜索 - 空参数")
    void testAdvancedSearch_EmptyParams() {
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Knowledge> result = knowledgeService.advancedSearch(null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("添加收藏 - 用户未登录抛出异常")
    void testAddFavorite_UserNotLogin_ThrowsException() {
        assertThrows(BusinessException.class, () -> knowledgeService.addFavorite(null, 1));
    }

    @Test
    @DisplayName("添加收藏 - 知识ID为空抛出异常")
    void testAddFavorite_KnowledgeIdNull_ThrowsException() {
        assertThrows(BusinessException.class, () -> knowledgeService.addFavorite(1, null));
    }

    @Test
    @DisplayName("提交反馈 - 内容为空抛出异常")
    void testSubmitFeedback_EmptyContent_ThrowsException() {
        assertThrows(BusinessException.class, () -> knowledgeService.submitFeedback(1, ""));
        assertThrows(BusinessException.class, () -> knowledgeService.submitFeedback(1, null));
    }

    @Test
    @DisplayName("提交反馈 - 内容过长抛出异常")
    void testSubmitFeedback_ContentTooLong_ThrowsException() {
        String longContent = "a".repeat(1001);
        assertThrows(BusinessException.class, () -> knowledgeService.submitFeedback(1, longContent));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementViewCount_Success() {
        doNothing().when(knowledgeMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> knowledgeService.incrementViewCount(1));
        verify(knowledgeMapper).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加浏览量 - 异常不抛出")
    void testIncrementViewCount_ExceptionNotThrown() {
        doThrow(new RuntimeException("DB Error")).when(knowledgeMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> knowledgeService.incrementViewCount(1));
    }

    @Test
    @DisplayName("清除缓存 - 成功")
    void testClearCache_Success() {
        assertDoesNotThrow(() -> knowledgeService.clearCache());
    }
}

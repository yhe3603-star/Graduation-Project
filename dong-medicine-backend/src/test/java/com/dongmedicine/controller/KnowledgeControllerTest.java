package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PopularityAsyncService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("知识库Controller测试")
class KnowledgeControllerTest {

    @Mock
    private KnowledgeService service;

    @Mock
    private BrowseHistoryService browseHistoryService;

    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private KnowledgeController knowledgeController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private Knowledge testKnowledge;
    private Page<Knowledge> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testKnowledge = new Knowledge();
        testKnowledge.setId(1);
        testKnowledge.setTitle("侗族药浴疗法");
        testKnowledge.setType("therapy");
        testKnowledge.setContent("药浴是侗族传统疗法之一");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testKnowledge));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = knowledgeController.list(1, 12, null, null, null, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).advancedSearchPaged(isNull(), isNull(), isNull(), isNull(), isNull(), eq(1), eq(12));
    }

    @Test
    @DisplayName("列表查询 - 带筛选条件")
    void testList_WithFilters() {
        when(service.advancedSearchPaged(eq("药浴"), eq("外治法"), isNull(), isNull(), eq("popularity"), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = knowledgeController.list(1, 12, "popularity", "药浴", "外治法", null, null);

        assertEquals(200, result.getCode());
        verify(service).advancedSearchPaged("药浴", "外治法", null, null, "popularity", 1, 12);
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.advancedSearchPaged(eq("药浴"), isNull(), isNull(), isNull(), eq("popularity"), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = knowledgeController.search("药浴", null, null, null, "popularity", 1, 12);

        assertEquals(200, result.getCode());
        verify(service).advancedSearchPaged("药浴", null, null, null, "popularity", 1, 12);
    }

    @Test
    @DisplayName("详情 - 成功（未登录）")
    void testDetail_Success_NotLoggedIn() {
        when(service.getDetailWithRelated(1)).thenReturn(testKnowledge);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        R<Knowledge> result = knowledgeController.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("侗族药浴疗法", result.getData().getTitle());
        verify(popularityAsyncService).incrementKnowledgeViewAndPopularity(1);
        verify(browseHistoryService, never()).record(anyInt(), anyString(), anyInt());
    }

    @Test
    @DisplayName("详情 - 成功（已登录记录浏览历史）")
    void testDetail_Success_LoggedIn() {
        when(service.getDetailWithRelated(1)).thenReturn(testKnowledge);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(100);

        R<Knowledge> result = knowledgeController.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("侗族药浴疗法", result.getData().getTitle());
        verify(popularityAsyncService).incrementKnowledgeViewAndPopularity(1);
        verify(browseHistoryService).record(100, "knowledge", 1);
    }

    @Test
    @DisplayName("详情 - 不存在抛异常")
    void testDetail_NotFound() {
        when(service.getDetailWithRelated(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> knowledgeController.detail(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        doNothing().when(service).incrementViewCount(1);

        R<String> result = knowledgeController.incrementView(1);

        assertEquals(200, result.getCode());
        verify(service).incrementViewCount(1);
    }

    @Test
    @DisplayName("收藏 - 成功")
    void testFavorite_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(100);
        doNothing().when(service).addFavorite(100, 1);

        R<String> result = knowledgeController.favorite(1);

        assertEquals(200, result.getCode());
        verify(service).addFavorite(100, 1);
    }

    @Test
    @DisplayName("反馈 - 成功")
    void testFeedback_Success() {
        doNothing().when(service).submitFeedback(1, "非常好的知识条目");

        R<String> result = knowledgeController.feedback(1, "非常好的知识条目");

        assertEquals(200, result.getCode());
        verify(service).submitFeedback(1, "非常好的知识条目");
    }

    @Test
    @DisplayName("反馈 - 内容超过500字符抛异常")
    void testFeedback_ContentTooLong() {
        String longContent = "a".repeat(501);

        assertThrows(BusinessException.class, () -> knowledgeController.feedback(1, longContent));
        verify(service, never()).submitFeedback(anyInt(), anyString());
    }
}

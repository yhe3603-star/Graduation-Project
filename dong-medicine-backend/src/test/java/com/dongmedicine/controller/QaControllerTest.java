package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.service.PopularityAsyncService;
import com.dongmedicine.service.QaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("非遗问答Controller测试")
class QaControllerTest {

    @Mock
    private QaService service;

    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private QaController qaController;

    private Qa testQa;
    private Page<Qa> testPage;

    @BeforeEach
    void setUp() {
        testQa = new Qa();
        testQa.setId(1);
        testQa.setCategory("药材知识");
        testQa.setQuestion("什么是侗族药浴？");
        testQa.setAnswer("侗族药浴是侗族传统疗法之一");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testQa));
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = qaController.list(1, 12, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).advancedSearchPaged(isNull(), isNull(), eq(1), eq(12));
    }

    @Test
    @DisplayName("列表查询 - 带分类和关键词")
    void testList_WithFilters() {
        when(service.advancedSearchPaged(eq("药浴"), eq("药材知识"), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = qaController.list(1, 12, "药材知识", "药浴");

        assertEquals(200, result.getCode());
        verify(service).advancedSearchPaged("药浴", "药材知识", 1, 12);
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPaged(eq("药浴"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = qaController.search("药浴", 1, 12);

        assertEquals(200, result.getCode());
        verify(service).searchPaged("药浴", 1, 12);
    }

    @Test
    @DisplayName("详情 - 成功")
    void testDetail_Success() {
        when(service.getDetail(1)).thenReturn(testQa);

        R<Qa> result = qaController.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("什么是侗族药浴？", result.getData().getQuestion());
        verify(popularityAsyncService).incrementQaViewAndPopularity(1);
    }

    @Test
    @DisplayName("详情 - 不存在抛异常")
    void testDetail_NotFound() {
        when(service.getDetail(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> qaController.detail(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        doNothing().when(service).incrementViewCount(1);

        R<String> result = qaController.incrementView(1);

        assertEquals(200, result.getCode());
        verify(service).incrementViewCount(1);
    }
}

package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.InheritorService;
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
@DisplayName("传承人Controller测试")
class InheritorControllerTest {

    @Mock
    private InheritorService service;

    @Mock
    private BrowseHistoryService browseHistoryService;

    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private InheritorController inheritorController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private Inheritor testInheritor;
    private Page<Inheritor> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testInheritor = new Inheritor();
        testInheritor.setId(1);
        testInheritor.setName("吴某某");
        testInheritor.setLevel("国家级");
        testInheritor.setBio("侗族医药传承人");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testInheritor));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(isNull(), isNull(), eq("name"), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = inheritorController.list(1, 12, null, null, "name");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).advancedSearchPaged(isNull(), isNull(), eq("name"), eq(1), eq(12));
    }

    @Test
    @DisplayName("列表查询 - 带筛选条件")
    void testList_WithFilters() {
        when(service.advancedSearchPaged(eq("吴"), eq("国家级"), eq("name"), anyInt(), anyInt()))
                .thenReturn(testPage);

        R<Map<String, Object>> result = inheritorController.list(1, 12, "国家级", "吴", "name");

        assertEquals(200, result.getCode());
        verify(service).advancedSearchPaged("吴", "国家级", "name", 1, 12);
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPaged(eq("侗族"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = inheritorController.search("侗族", 1, 12);

        assertEquals(200, result.getCode());
        verify(service).searchPaged("侗族", 1, 12);
    }

    @Test
    @DisplayName("详情 - 成功（未登录）")
    void testDetail_Success_NotLoggedIn() {
        when(service.getDetailWithExtras(1)).thenReturn(testInheritor);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        R<Inheritor> result = inheritorController.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("吴某某", result.getData().getName());
        verify(popularityAsyncService).incrementInheritorViewAndPopularity(1);
        verify(browseHistoryService, never()).record(anyInt(), anyString(), anyInt());
    }

    @Test
    @DisplayName("详情 - 成功（已登录记录浏览历史）")
    void testDetail_Success_LoggedIn() {
        when(service.getDetailWithExtras(1)).thenReturn(testInheritor);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(100);

        R<Inheritor> result = inheritorController.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("吴某某", result.getData().getName());
        verify(popularityAsyncService).incrementInheritorViewAndPopularity(1);
        verify(browseHistoryService).record(100, "inheritor", 1);
    }

    @Test
    @DisplayName("详情 - 不存在抛异常")
    void testDetail_NotFound() {
        when(service.getDetailWithExtras(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> inheritorController.detail(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        doNothing().when(service).incrementViewCount(1);

        R<String> result = inheritorController.incrementView(1);

        assertEquals(200, result.getCode());
        verify(service).incrementViewCount(1);
    }
}

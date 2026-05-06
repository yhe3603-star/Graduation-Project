package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.service.BrowseHistoryService;
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
@DisplayName("BrowseHistoryController测试")
class BrowseHistoryControllerTest {

    @Mock
    private BrowseHistoryService browseHistoryService;

    @InjectMocks
    private BrowseHistoryController browseHistoryController;

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

    // ==================== 获取我的浏览历史测试 ====================

    @Test
    @DisplayName("获取我的浏览历史 - 已登录成功")
    void testMyHistory_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("targetType", "plant");
        item.put("targetId", 10);
        history.add(item);

        when(browseHistoryService.getMyHistory(eq(1), eq(50))).thenReturn(history);

        R<List<Map<String, Object>>> result = browseHistoryController.myHistory(50);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("plant", result.getData().get(0).get("targetType"));
    }

    @Test
    @DisplayName("获取我的浏览历史 - 默认limit")
    void testMyHistory_DefaultLimit() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        when(browseHistoryService.getMyHistory(eq(1), eq(50)))
                .thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = browseHistoryController.myHistory(50);

        assertEquals(200, result.getCode());
        verify(browseHistoryService).getMyHistory(1, 50);
    }

    @Test
    @DisplayName("获取我的浏览历史 - limit上限截断")
    void testMyHistory_LimitClamped() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        when(browseHistoryService.getMyHistory(eq(1), eq(200)))
                .thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = browseHistoryController.myHistory(500);

        assertEquals(200, result.getCode());
        verify(browseHistoryService).getMyHistory(1, 200);
    }

    @Test
    @DisplayName("获取我的浏览历史 - 未登录抛异常")
    void testMyHistory_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> browseHistoryController.myHistory(50));
    }

    // ==================== 记录浏览历史测试 ====================

    @Test
    @DisplayName("记录浏览历史 - 已登录成功")
    void testRecord_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(browseHistoryService).record(eq(1), eq("plant"), eq(10));

        R<String> result = browseHistoryController.record("plant", 10);

        assertEquals(200, result.getCode());
        assertEquals("ok", result.getData());
        verify(browseHistoryService).record(1, "plant", 10);
    }

    @Test
    @DisplayName("记录浏览历史 - 未登录抛异常")
    void testRecord_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> browseHistoryController.record("plant", 10));
        verify(browseHistoryService, never()).record(anyInt(), anyString(), anyInt());
    }
}

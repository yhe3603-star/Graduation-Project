package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.service.ChatHistoryService;
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
@DisplayName("ChatHistoryController测试")
class ChatHistoryControllerTest {

    @Mock
    private ChatHistoryService chatHistoryService;

    @InjectMocks
    private ChatHistoryController chatHistoryController;

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

    // ==================== 获取会话列表测试 ====================

    @Test
    @DisplayName("获取会话列表 - 已登录成功")
    void testListSessions_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        List<Map<String, Object>> sessions = new ArrayList<>();
        Map<String, Object> session = new HashMap<>();
        session.put("sessionId", "abc-123");
        session.put("lastMessage", "你好");
        sessions.add(session);

        when(chatHistoryService.getUserSessions(eq(1))).thenReturn(sessions);

        R<List<Map<String, Object>>> result = chatHistoryController.listSessions();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("abc-123", result.getData().get(0).get("sessionId"));
    }

    @Test
    @DisplayName("获取会话列表 - 未登录抛异常")
    void testListSessions_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> chatHistoryController.listSessions());
    }

    // ==================== 获取会话消息测试 ====================

    @Test
    @DisplayName("获取会话消息 - 已登录成功")
    void testGetSessionMessages_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        ChatHistory history = new ChatHistory();
        history.setId(1L);
        history.setUserId(1);
        history.setSessionId("abc-123");
        history.setRole("user");
        history.setContent("你好");

        when(chatHistoryService.getSessionHistory(eq(1), eq("abc-123")))
                .thenReturn(Collections.singletonList(history));

        R<List<ChatHistory>> result = chatHistoryController.getSessionMessages("abc-123");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("abc-123", result.getData().get(0).getSessionId());
    }

    @Test
    @DisplayName("获取会话消息 - 未登录抛异常")
    void testGetSessionMessages_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> chatHistoryController.getSessionMessages("abc-123"));
    }

    // ==================== 删除会话测试 ====================

    @Test
    @DisplayName("删除会话 - 已登录成功")
    void testDeleteSession_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(chatHistoryService).deleteSession(eq(1), eq("abc-123"));

        R<String> result = chatHistoryController.deleteSession("abc-123");

        assertEquals(200, result.getCode());
        assertEquals("删除成功", result.getData());
        verify(chatHistoryService).deleteSession(1, "abc-123");
    }

    @Test
    @DisplayName("删除会话 - 未登录抛异常")
    void testDeleteSession_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> chatHistoryController.deleteSession("abc-123"));
        verify(chatHistoryService, never()).deleteSession(anyInt(), anyString());
    }
}

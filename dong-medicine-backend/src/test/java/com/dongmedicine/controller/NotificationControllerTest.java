package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("消息通知Controller测试")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("获取通知列表 - 未登录返回空列表")
    void testList_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        R<List<String>> result = notificationController.list();
        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("获取通知列表 - 已登录有数据")
    void testList_LoggedInWithData() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(notificationService.getNotifications(1)).thenReturn(List.of("通知1", "通知2"));
        R<List<String>> result = notificationController.list();
        assertEquals(2, result.getData().size());
    }

    @Test
    @DisplayName("获取通知列表 - 已登录无数据")
    void testList_LoggedInNoData() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(notificationService.getNotifications(1)).thenReturn(Collections.emptyList());
        R<List<String>> result = notificationController.list();
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("获取未读数量 - 未登录返回0")
    void testUnreadCount_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        R<Map<String, Object>> result = notificationController.unreadCount();
        assertEquals(0, result.getData().get("count"));
    }

    @Test
    @DisplayName("获取未读数量 - 已登录有未读消息")
    void testUnreadCount_LoggedInWithUnread() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(notificationService.getUnreadCount(1)).thenReturn(5);
        R<Map<String, Object>> result = notificationController.unreadCount();
        assertEquals(5, result.getData().get("count"));
    }

    @Test
    @DisplayName("获取未读数量 - 已登录无未读消息")
    void testUnreadCount_LoggedInNoUnread() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(notificationService.getUnreadCount(1)).thenReturn(0);
        R<Map<String, Object>> result = notificationController.unreadCount();
        assertEquals(0, result.getData().get("count"));
    }

    @Test
    @DisplayName("标记全部已读 - 未登录返回ok")
    void testMarkAllRead_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        R<String> result = notificationController.markAllRead();
        assertEquals("ok", result.getData());
    }

    @Test
    @DisplayName("标记全部已读 - 已登录调用service")
    void testMarkAllRead_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        R<String> result = notificationController.markAllRead();
        assertEquals("ok", result.getData());
        verify(notificationService).markAllRead(1);
    }
}

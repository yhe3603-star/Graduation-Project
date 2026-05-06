package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.service.FavoriteService;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("收藏Controller测试")
class FavoriteControllerTest {

    @Mock
    private FavoriteService service;

    @InjectMocks
    private FavoriteController favoriteController;

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

    // ==================== 添加收藏 tests ====================

    @Test
    @DisplayName("添加收藏 - 已登录成功")
    void testAdd_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(service).addFavorite(eq(1), eq("plant"), eq(10));

        R<String> result = favoriteController.add("plant", 10);

        assertEquals(200, result.getCode());
        assertEquals("收藏成功", result.getData());
        verify(service).addFavorite(1, "plant", 10);
    }

    @Test
    @DisplayName("添加收藏 - knowledge类型")
    void testAdd_KnowledgeType() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(service).addFavorite(eq(1), eq("knowledge"), eq(5));

        R<String> result = favoriteController.add("knowledge", 5);

        assertEquals(200, result.getCode());
        assertEquals("收藏成功", result.getData());
        verify(service).addFavorite(1, "knowledge", 5);
    }

    @Test
    @DisplayName("添加收藏 - 未登录抛异常")
    void testAdd_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> favoriteController.add("plant", 10));
        verify(service, never()).addFavorite(anyInt(), anyString(), anyInt());
    }

    // ==================== 取消收藏 tests ====================

    @Test
    @DisplayName("取消收藏 - 已登录成功")
    void testRemove_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(service).removeFavorite(eq(1), eq("plant"), eq(10));

        R<String> result = favoriteController.remove("plant", 10);

        assertEquals(200, result.getCode());
        assertEquals("取消收藏成功", result.getData());
        verify(service).removeFavorite(1, "plant", 10);
    }

    @Test
    @DisplayName("取消收藏 - knowledge类型")
    void testRemove_KnowledgeType() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(service).removeFavorite(eq(1), eq("knowledge"), eq(5));

        R<String> result = favoriteController.remove("knowledge", 5);

        assertEquals(200, result.getCode());
        assertEquals("取消收藏成功", result.getData());
        verify(service).removeFavorite(1, "knowledge", 5);
    }

    @Test
    @DisplayName("取消收藏 - 未登录抛异常")
    void testRemove_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> favoriteController.remove("plant", 10));
        verify(service, never()).removeFavorite(anyInt(), anyString(), anyInt());
    }

    // ==================== 我的收藏 tests ====================

    @Test
    @DisplayName("我的收藏 - 已登录成功")
    void testMyFavorites_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("id", 1);
        favoriteData.put("targetType", "plant");
        favoriteData.put("targetId", 10);
        favoriteData.put("name", "钩藤");

        Page<Map<String, Object>> testPage = new Page<>(1, 20, 1);
        testPage.setRecords(Arrays.asList(favoriteData));

        when(service.getMyFavoritesPaged(eq(1), eq(1), eq(20))).thenReturn(testPage);

        R<Map<String, Object>> result = favoriteController.myFavorites(1, 20);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).getMyFavoritesPaged(1, 1, 20);
    }

    @Test
    @DisplayName("我的收藏 - 自定义分页")
    void testMyFavorites_CustomPagination() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

        Page<Map<String, Object>> customPage = new Page<>(2, 5, 10);
        customPage.setRecords(Arrays.asList(new HashMap<>()));

        when(service.getMyFavoritesPaged(eq(1), eq(2), eq(5))).thenReturn(customPage);

        R<Map<String, Object>> result = favoriteController.myFavorites(2, 5);

        assertEquals(200, result.getCode());
        verify(service).getMyFavoritesPaged(1, 2, 5);
    }

    @Test
    @DisplayName("我的收藏 - 未登录抛异常")
    void testMyFavorites_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> favoriteController.myFavorites(1, 20));
        verify(service, never()).getMyFavoritesPaged(anyInt(), anyInt(), anyInt());
    }
}

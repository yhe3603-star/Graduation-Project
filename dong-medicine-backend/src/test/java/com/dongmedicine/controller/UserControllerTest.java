package com.dongmedicine.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.dto.ChangePasswordDTO;
import com.dongmedicine.dto.LoginDTO;
import com.dongmedicine.dto.RegisterDTO;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.CaptchaService;
import com.dongmedicine.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController测试")
class UserControllerTest {

    @Mock
    private UserService service;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private UserController userController;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockedStatic<StpUtil> stpUtilMock;

    private LoginDTO loginDTO;
    private RegisterDTO registerDTO;
    private ChangePasswordDTO changePasswordDTO;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
        loginDTO.setCaptchaKey("key1");
        loginDTO.setCaptchaCode("1234");

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setConfirmPassword("password123");
        registerDTO.setCaptchaKey("key2");
        registerDTO.setCaptchaCode("5678");

        changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("oldpass123");
        changePasswordDTO.setNewPassword("newpass123");
        changePasswordDTO.setCaptchaKey("key3");
        changePasswordDTO.setCaptchaCode("9999");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("登录 - 成功")
    void testLogin_Success() {
        doNothing().when(captchaService).validateCaptchaOrThrow(eq("key1"), eq("1234"));
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "jwt-token");
        loginResult.put("id", 1);
        when(service.login(eq("testuser"), eq("password123"))).thenReturn(loginResult);

        R<Map<String, Object>> result = userController.login(loginDTO);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt-token", result.getData().get("token"));
        verify(service).login("testuser", "password123");
    }

    @Test
    @DisplayName("登录 - 验证码失败")
    void testLogin_CaptchaFail() {
        doThrow(new BusinessException(com.dongmedicine.common.exception.ErrorCode.PARAM_ERROR, "验证码错误或已过期"))
                .when(captchaService).validateCaptchaOrThrow(eq("key1"), eq("1234"));

        assertThrows(BusinessException.class, () -> userController.login(loginDTO));
        verify(service, never()).login(anyString(), anyString());
    }

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("注册 - 成功")
    void testRegister_Success() {
        doNothing().when(captchaService).validateCaptchaOrThrow(eq("key2"), eq("5678"));
        doNothing().when(service).register(eq("newuser"), eq("password123"));

        R<String> result = userController.register(registerDTO);

        assertEquals(200, result.getCode());
        assertEquals("注册成功", result.getData());
        verify(service).register("newuser", "password123");
    }

    @Test
    @DisplayName("注册 - 两次密码不一致")
    void testRegister_PasswordMismatch() {
        registerDTO.setConfirmPassword("differentpassword");

        assertThrows(BusinessException.class, () -> userController.register(registerDTO));
        verify(service, never()).register(anyString(), anyString());
    }

    // ==================== 获取当前用户信息测试 ====================

    @Test
    @DisplayName("获取当前用户信息 - 已登录成功")
    void testMe_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setRole("user");
        when(service.getUserInfo(eq(1))).thenReturn(user);

        R<User> result = userController.me();

        assertEquals(200, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
    }

    @Test
    @DisplayName("获取当前用户信息 - 未登录抛异常")
    void testMe_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(com.dongmedicine.common.exception.ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> userController.me());
    }

    // ==================== 修改密码测试 ====================

    @Test
    @DisplayName("修改密码 - 已登录成功")
    void testChangePassword_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        doNothing().when(captchaService).validateCaptchaOrThrow(eq("key3"), eq("9999"));
        doNothing().when(service).changePassword(eq(1), eq("oldpass123"), eq("newpass123"));

        stpUtilMock = mockStatic(StpUtil.class);
        try {
            stpUtilMock.when(StpUtil::logout).then(invocation -> null);

            R<String> result = userController.changePassword(changePasswordDTO);

            assertEquals(200, result.getCode());
            assertEquals("密码修改成功，请重新登录", result.getData());
            verify(service).changePassword(1, "oldpass123", "newpass123");
        } finally {
            stpUtilMock.close();
        }
    }

    @Test
    @DisplayName("修改密码 - 未登录抛异常")
    void testChangePassword_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(com.dongmedicine.common.exception.ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> userController.changePassword(changePasswordDTO));
    }

    // ==================== 退出登录测试 ====================

    @Test
    @DisplayName("退出登录 - 成功")
    void testLogout_Success() {
        stpUtilMock = mockStatic(StpUtil.class);
        try {
            stpUtilMock.when(StpUtil::logout).then(invocation -> null);

            R<String> result = userController.logout();

            assertEquals(200, result.getCode());
            assertEquals("退出成功", result.getData());
        } finally {
            stpUtilMock.close();
        }
    }

    // ==================== 验证登录状态测试 ====================

    @Test
    @DisplayName("验证登录状态 - 已登录")
    void testValidate_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        securityUtilsMock.when(SecurityUtils::getCurrentUsername).thenReturn("testuser");
        securityUtilsMock.when(SecurityUtils::getCurrentUserRole).thenReturn("user");

        R<Map<String, Object>> result = userController.validate();

        assertEquals(200, result.getCode());
        assertTrue((Boolean) result.getData().get("valid"));
        assertEquals(1, result.getData().get("id"));
        assertEquals("testuser", result.getData().get("username"));
        assertEquals("user", result.getData().get("role"));
    }

    @Test
    @DisplayName("验证登录状态 - 未登录")
    void testValidate_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(com.dongmedicine.common.exception.ErrorCode.LOGIN_REQUIRED, "请先登录"));

        R<Map<String, Object>> result = userController.validate();

        assertEquals(200, result.getCode());
        assertFalse((Boolean) result.getData().get("valid"));
    }

    // ==================== 刷新Token测试 ====================

    @Test
    @DisplayName("刷新Token - 已登录成功")
    void testRefreshToken_LoggedIn() {
        stpUtilMock = mockStatic(StpUtil.class);
        try {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(7200L);
            stpUtilMock.when(() -> StpUtil.renewTimeout(7200L)).then(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn("new-jwt-token");

            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);

            R<Map<String, Object>> result = userController.refreshToken();

            assertEquals(200, result.getCode());
            assertEquals("new-jwt-token", result.getData().get("token"));
            assertEquals(1, result.getData().get("id"));
        } finally {
            stpUtilMock.close();
        }
    }

    @Test
    @DisplayName("刷新Token - 未登录抛异常")
    void testRefreshToken_NotLoggedIn() {
        stpUtilMock = mockStatic(StpUtil.class);
        try {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

            assertThrows(BusinessException.class, () -> userController.refreshToken());
        } finally {
            stpUtilMock.close();
        }
    }
}

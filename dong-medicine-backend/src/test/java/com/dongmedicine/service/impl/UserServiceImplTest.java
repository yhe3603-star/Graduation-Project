package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.config.JwtUtil;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode("Test123456"));
        testUser.setRole("USER");
    }

    @Test
    @DisplayName("登录成功测试")
    void testLoginSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyInt(), anyString())).thenReturn("mock-token");

        String token = userService.login("testuser", "Test123456");

        assertNotNull(token);
        assertEquals("mock-token", token);
        
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(jwtUtil, times(1)).generateToken("testuser", 1, "USER");
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginFailUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("nonexistent", "password");
        });

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginFailWrongPassword() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });

        assertEquals(ErrorCode.PASSWORD_WRONG.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("登录失败 - 用户名为空")
    void testLoginFailEmptyUsername() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("", "password");
        });

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("注册成功测试")
    void testRegisterSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            userService.register("newuser", "Test123456");
        });
        
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void testRegisterFailUsernameExists() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register("testuser", "Test123456");
        });

        assertEquals(ErrorCode.USER_ALREADY_EXISTS.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("注册失败 - 用户名太短")
    void testRegisterFailUsernameTooShort() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register("ab", "Test123456");
        });

        assertEquals(ErrorCode.USERNAME_TOO_SHORT.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("注册失败 - 密码太弱")
    void testRegisterFailPasswordTooWeak() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register("newuser", "123456");
        });

        assertEquals(ErrorCode.PASSWORD_TOO_WEAK.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("密码验证测试 - 有效密码")
    void testPasswordValidationValid() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test123456");
        
        assertTrue(result.isValid());
        assertEquals("密码符合要求", result.getMessage());
    }

    @Test
    @DisplayName("密码验证测试 - 太短")
    void testPasswordValidationTooShort() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test1");
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("6"));
    }

    @Test
    @DisplayName("密码验证测试 - 缺少数字")
    void testPasswordValidationNoDigit() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("TestPassword");
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("数字"));
    }

    @Test
    @DisplayName("密码验证测试 - 缺少字母")
    void testPasswordValidationNoLetter() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("12345678");
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("字母"));
    }

    @Test
    @DisplayName("密码验证测试 - 包含空格")
    void testPasswordValidationHasWhitespace() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test 123456");
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("空格"));
    }

    @Test
    @DisplayName("密码强度测试 - 弱密码")
    void testPasswordStrengthWeak() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test12");
        
        assertTrue(result.isValid());
        assertNotNull(result.getStrengthLabel());
    }

    @Test
    @DisplayName("密码强度测试 - 强密码")
    void testPasswordStrengthStrong() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test123456!@#$");
        
        assertTrue(result.isValid());
        assertNotNull(result.getStrengthLabel());
    }

    @Test
    @DisplayName("修改密码成功测试")
    void testChangePasswordSuccess() {
        when(userMapper.selectById(1)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            userService.changePassword(1, "Test123456", "NewTest123");
        });
        
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("修改密码失败 - 用户不存在")
    void testChangePasswordFailUserNotFound() {
        when(userMapper.selectById(999)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(999, "Test123456", "NewTest123");
        });

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("修改密码失败 - 当前密码错误")
    void testChangePasswordFailWrongCurrentPassword() {
        when(userMapper.selectById(1)).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(1, "wrongpassword", "NewTest123");
        });

        assertEquals(ErrorCode.PASSWORD_WRONG.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("修改密码失败 - 新密码太弱")
    void testChangePasswordFailNewPasswordTooWeak() {
        when(userMapper.selectById(1)).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(1, "Test123456", "123456");
        });

        assertEquals(ErrorCode.PASSWORD_TOO_WEAK.getCode(), exception.getErrorCode().getCode());
    }
}

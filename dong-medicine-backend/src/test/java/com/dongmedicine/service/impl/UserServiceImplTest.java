package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.common.util.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl();
        setBaseMapper(userService, userMapper);

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode("Test123456"));
        testUser.setRole("user");
        testUser.setStatus("active");
    }

    private void setBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginFailUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

        assertThrows(Exception.class, () -> {
            userService.login("nonexistent", "password");
        });
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginFailWrongPassword() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(testUser);

        assertThrows(Exception.class, () -> {
            userService.login("testuser", "wrongpassword");
        });
    }

    @Test
    @DisplayName("登录失败 - 用户名为空")
    void testLoginFailEmptyUsername() {
        assertThrows(Exception.class, () -> {
            userService.login("", "password");
        });
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void testRegisterFailUsernameExists() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(testUser);

        assertThrows(Exception.class, () -> {
            userService.register("testuser", "Test123456");
        });
    }

    @Test
    @DisplayName("注册失败 - 用户名太短")
    void testRegisterFailUsernameTooShort() {
        assertThrows(Exception.class, () -> {
            userService.register("ab", "Test123456");
        });
    }

    @Test
    @DisplayName("密码验证 - 有效密码")
    void testPasswordValidationValid() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test123456");
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("密码验证 - 太短")
    void testPasswordValidationTooShort() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Test1");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("修改密码失败 - 用户不存在")
    void testChangePasswordFailUserNotFound() {
        when(userMapper.selectById(999)).thenReturn(null);

        assertThrows(Exception.class, () -> {
            userService.changePassword(999, "Test123456", "NewTest123");
        });
    }

    @Test
    @DisplayName("修改密码失败 - 当前密码错误")
    void testChangePasswordFailWrongCurrentPassword() {
        when(userMapper.selectById(1)).thenReturn(testUser);

        assertThrows(Exception.class, () -> {
            userService.changePassword(1, "wrongpassword", "NewTest123");
        });
    }

    @Test
    @DisplayName("注册成功 - 新用户")
    void testRegisterSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            userService.register("newuser", "NewUser123456");
        });
    }
}

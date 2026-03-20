package com.dongmedicine.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.impl.UserServiceImpl;
import com.dongmedicine.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        testUser.setRole("user");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("登录成功测试")
    void testLoginSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), any(), anyString())).thenReturn("test-token");

        String token = userService.login("testuser", rawPassword);

        assertNotNull(token);
        assertEquals("test-token", token);
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("登录失败-用户不存在")
    void testLoginFailUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            userService.login("nonexistent", "password");
        });
    }

    @Test
    @DisplayName("登录失败-密码错误")
    void testLoginFailWrongPassword() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        assertThrows(RuntimeException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });
    }

    @Test
    @DisplayName("注册成功测试")
    void testRegisterSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            userService.register("newuser", "password123");
        });

        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败-用户已存在")
    void testRegisterFailUserExists() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        assertThrows(RuntimeException.class, () -> {
            userService.register("testuser", "password123");
        });
    }

    @Test
    @DisplayName("根据用户名获取用户")
    void testGetUserByUsername() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNull(result.getPasswordHash());
    }

    @Test
    @DisplayName("根据ID获取用户信息")
    void testGetUserInfo() {
        when(userMapper.selectById(1)).thenReturn(testUser);

        User result = userService.getUserInfo(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getPasswordHash());
    }
}

package com.dongmedicine.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("登录尝试服务测试")
class LoginAttemptServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private LoginAttemptServiceImpl loginAttemptService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("记录登录失败 - 首次失败设置过期时间")
    void testRecordFailure_FirstTime() {
        when(valueOps.increment("login:attempts:admin")).thenReturn(1L);

        loginAttemptService.recordFailure("admin");

        verify(valueOps).increment("login:attempts:admin");
        verify(redisTemplate).expire("login:attempts:admin", 900, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("记录登录失败 - 非首次失败不重置过期时间")
    void testRecordFailure_Subsequent() {
        when(valueOps.increment("login:attempts:admin")).thenReturn(3L);

        loginAttemptService.recordFailure("admin");

        verify(valueOps).increment("login:attempts:admin");
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("记录登录成功 - 清除失败计数")
    void testRecordSuccess() {
        loginAttemptService.recordSuccess("admin");
        verify(redisTemplate).delete("login:attempts:admin");
    }

    @Test
    @DisplayName("检查锁定状态 - 未锁定")
    void testIsLocked_NotLocked() {
        when(valueOps.get("login:attempts:admin")).thenReturn("3");
        assertFalse(loginAttemptService.isLocked("admin"));
    }

    @Test
    @DisplayName("检查锁定状态 - 已锁定")
    void testIsLocked_Locked() {
        when(valueOps.get("login:attempts:admin")).thenReturn("5");
        assertTrue(loginAttemptService.isLocked("admin"));
    }

    @Test
    @DisplayName("检查锁定状态 - Redis不可用时返回false")
    void testIsLocked_RedisUnavailable() {
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("Redis down"));
        assertFalse(loginAttemptService.isLocked("admin"));
    }

    @Test
    @DisplayName("获取剩余尝试次数")
    void testGetRemainingAttempts() {
        when(valueOps.get("login:attempts:admin")).thenReturn("2");
        assertEquals(3, loginAttemptService.getRemainingAttempts("admin"));
    }

    @Test
    @DisplayName("获取剩余尝试次数 - 无记录时返回最大值")
    void testGetRemainingAttempts_NoRecord() {
        when(valueOps.get("login:attempts:admin")).thenReturn(null);
        assertEquals(5, loginAttemptService.getRemainingAttempts("admin"));
    }

    @Test
    @DisplayName("Redis不可用时优雅降级")
    void testGracefulDegradation() {
        LoginAttemptServiceImpl service = new LoginAttemptServiceImpl();
        assertDoesNotThrow(() -> service.recordFailure("admin"));
        assertFalse(service.isLocked("admin"));
        assertEquals(5, service.getRemainingAttempts("admin"));
    }
}

package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController测试")
class ChatControllerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private ChatController chatController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        chatController = new ChatController(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("统计信息 - 初始状态")
    void testStats_InitialState() {
        when(valueOps.get("chat:stats:total")).thenReturn(null);
        when(valueOps.get("chat:stats:success")).thenReturn(null);
        when(valueOps.get("chat:stats:failed")).thenReturn(null);

        R<ChatController.ChatStats> result = chatController.stats();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().totalRequests());
        assertEquals(0, result.getData().successRequests());
        assertEquals(0, result.getData().failedRequests());
    }

    @Test
    @DisplayName("统计信息 - 记录成功请求后")
    void testStats_AfterSuccessRequest() {
        when(valueOps.increment("chat:stats:total")).thenReturn(1L);
        when(valueOps.increment("chat:stats:success")).thenReturn(1L);
        when(valueOps.get("chat:stats:total")).thenReturn("1");
        when(valueOps.get("chat:stats:success")).thenReturn("1");
        when(valueOps.get("chat:stats:failed")).thenReturn("0");

        chatController.recordRequest(true);
        R<ChatController.ChatStats> result = chatController.stats();

        assertEquals(1, result.getData().totalRequests());
        assertEquals(1, result.getData().successRequests());
        assertEquals(0, result.getData().failedRequests());
    }

    @Test
    @DisplayName("统计信息 - 记录失败请求后")
    void testStats_AfterFailedRequest() {
        when(valueOps.increment("chat:stats:total")).thenReturn(1L);
        when(valueOps.increment("chat:stats:failed")).thenReturn(1L);
        when(valueOps.get("chat:stats:total")).thenReturn("1");
        when(valueOps.get("chat:stats:success")).thenReturn("0");
        when(valueOps.get("chat:stats:failed")).thenReturn("1");

        chatController.recordRequest(false);
        R<ChatController.ChatStats> result = chatController.stats();

        assertEquals(1, result.getData().totalRequests());
        assertEquals(0, result.getData().successRequests());
        assertEquals(1, result.getData().failedRequests());
    }
}

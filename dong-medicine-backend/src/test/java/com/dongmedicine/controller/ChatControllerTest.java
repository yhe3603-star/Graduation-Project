package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController测试")
class ChatControllerTest {

    @InjectMocks
    private ChatController chatController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() throws Exception {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        // 重置静态计数器
        resetAtomicField("totalRequests", 0);
        resetAtomicField("successRequests", 0);
        resetAtomicField("failedRequests", 0);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("统计信息 - 初始状态")
    void testStats_InitialState() {
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
        ChatController.recordRequest(true);

        R<ChatController.ChatStats> result = chatController.stats();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().totalRequests());
        assertEquals(1, result.getData().successRequests());
        assertEquals(0, result.getData().failedRequests());
    }

    @Test
    @DisplayName("统计信息 - 记录失败请求后")
    void testStats_AfterFailedRequest() {
        ChatController.recordRequest(false);

        R<ChatController.ChatStats> result = chatController.stats();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().totalRequests());
        assertEquals(0, result.getData().successRequests());
        assertEquals(1, result.getData().failedRequests());
    }

    @Test
    @DisplayName("统计信息 - 混合请求记录")
    void testStats_MixedRequests() {
        ChatController.recordRequest(true);
        ChatController.recordRequest(true);
        ChatController.recordRequest(false);

        R<ChatController.ChatStats> result = chatController.stats();

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().totalRequests());
        assertEquals(2, result.getData().successRequests());
        assertEquals(1, result.getData().failedRequests());
    }

    private void resetAtomicField(String fieldName, long value) throws Exception {
        Field field = ChatController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        AtomicLong atomic = (AtomicLong) field.get(null);
        atomic.set(value);
    }
}

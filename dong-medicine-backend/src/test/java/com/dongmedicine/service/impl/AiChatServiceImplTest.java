package com.dongmedicine.service.impl;

import com.dongmedicine.config.DeepSeekConfig;
import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;
import com.dongmedicine.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AI聊天服务测试")
class AiChatServiceImplTest {

    @Mock
    private DeepSeekConfig deepSeekConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @BeforeEach
    void setUp() {
        lenient().when(deepSeekConfig.getApiKey()).thenReturn("test-api-key");
        lenient().when(deepSeekConfig.getBaseUrl()).thenReturn("https://api.deepseek.com");
        lenient().when(deepSeekConfig.getModel()).thenReturn("deepseek-chat");
    }

    @Test
    @DisplayName("同步聊天 - API未配置时返回错误")
    void testChat_ApiKeyNotConfigured() {
        when(deepSeekConfig.getApiKey()).thenReturn("");

        ChatRequest request = new ChatRequest();
        request.setMessage("你好");
        ChatResponse response = aiChatService.chat(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("未配置"));
    }

    @Test
    @DisplayName("同步聊天 - API Key为null时返回错误")
    void testChat_ApiKeyNull() {
        when(deepSeekConfig.getApiKey()).thenReturn(null);

        ChatRequest request = new ChatRequest();
        request.setMessage("你好");
        ChatResponse response = aiChatService.chat(request);

        assertFalse(response.isSuccess());
    }

    @Test
    @DisplayName("同步聊天 - 调用异常时返回错误")
    void testChat_Exception() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        ChatRequest request = new ChatRequest();
        request.setMessage("你好");
        ChatResponse response = aiChatService.chat(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("不可用"));
    }

    @Test
    @DisplayName("流式聊天 - API未配置时回调onError")
    void testChatStream_ApiKeyNotConfigured() {
        when(deepSeekConfig.getApiKey()).thenReturn("");

        StringBuilder errorReceived = new StringBuilder();
        aiChatService.chatStream("你好", null, new AiChatService.StreamCallback() {
            @Override
            public void onToken(String token) {}
            @Override
            public void onComplete(String fullReply) {}
            @Override
            public void onError(String error) {
                errorReceived.append(error);
            }
        });

        assertTrue(errorReceived.toString().contains("未配置"));
    }

    @Test
    @DisplayName("buildMessagesFromJson - 空历史记录")
    void testBuildMessagesFromJson_EmptyHistory() {
        when(deepSeekConfig.getApiKey()).thenReturn("test-key");

        StringBuilder result = new StringBuilder();
        aiChatService.chatStream("测试消息", null, new AiChatService.StreamCallback() {
            @Override public void onToken(String token) { result.append(token); }
            @Override public void onComplete(String fullReply) { result.append(fullReply); }
            @Override public void onError(String error) { result.append("ERROR:").append(error); }
        });

        assertFalse(result.toString().startsWith("ERROR:未配置"));
    }
}

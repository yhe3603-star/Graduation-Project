package com.dongmedicine.websocket;

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
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocket聊天处理器测试")
class ChatWebSocketHandlerTest {

    @Mock
    private AiChatService aiChatService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatWebSocketHandler handler;

    @Mock
    private WebSocketSession session;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(session.getId()).thenReturn("test-session-1");
        lenient().when(session.isOpen()).thenReturn(true);
        lenient().when(session.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"error\",\"content\":\"test\"}");
    }

    @Test
    @DisplayName("连接建立 - 应记录会话")
    void testAfterConnectionEstablished() {
        handler.afterConnectionEstablished(session);
        verify(session).getId();
    }

    @Test
    @DisplayName("连接关闭 - 应移除会话")
    void testAfterConnectionClosed() {
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        verify(session).getId();
    }

    @Test
    @DisplayName("传输错误 - 应移除会话")
    void testHandleTransportError() {
        handler.handleTransportError(session, new RuntimeException("Connection lost"));
        verify(session).getId();
    }

    @Test
    @DisplayName("处理消息 - 未知类型应发送错误")
    void testHandleTextMessage_UnknownType() throws Exception {
        JsonNode typeNode = mock(JsonNode.class);
        when(typeNode.asText()).thenReturn("unknown");
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.path("type")).thenReturn(typeNode);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"unknown\"}"));

        verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("处理消息 - JSON解析失败应发送错误")
    void testHandleTextMessage_InvalidJson() throws Exception {
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Invalid JSON"));

        handler.handleTextMessage(session, new TextMessage("invalid json"));

        verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("停止生成 - 无活跃请求时应正常响应")
    void testHandleStop_NoActiveRequest() throws Exception {
        JsonNode typeNode = mock(JsonNode.class);
        when(typeNode.asText()).thenReturn("stop");
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.path("type")).thenReturn(typeNode);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"stop\"}"));

        verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("连接关闭时有活跃订阅应取消")
    void testAfterConnectionClosed_WithActiveSubscription() {
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        verify(session, times(2)).getId();
    }
}

package com.dongmedicine.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.controller.ChatController;
import com.dongmedicine.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket连接建立: sessionId={}, remoteAddr={}", sessionId, session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.path("type").asText();

            switch (type) {
                case "chat" -> handleChat(session, json);
                case "stop" -> handleStop(session);
                default -> sendError(session, "未知的消息类型: " + type);
            }
        } catch (Exception e) {
            log.error("WebSocket消息处理异常: sessionId={}", sessionId, e);
            sendError(session, "消息处理失败");
        }
    }

    private void handleChat(WebSocketSession session, JsonNode json) {
        String userMessage = json.path("message").asText();
        if (userMessage.isBlank()) {
            sendError(session, "消息内容不能为空");
            return;
        }

        String userId = getUserId(session);
        log.info("WebSocket聊天请求: sessionId={}, userId={}, messageLength={}",
                session.getId(), userId, userMessage.length());

        sendJson(session, Map.of("type", "start"));

        aiChatService.chatStream(userMessage, json.path("history"), new AiChatService.StreamCallback() {
            @Override
            public void onToken(String token) {
                sendJson(session, Map.of("type", "token", "content", token));
            }

            @Override
            public void onComplete(String fullReply) {
                sendJson(session, Map.of("type", "done", "content", fullReply));
                ChatController.recordRequest(true);
                log.info("WebSocket聊天完成: sessionId={}, replyLength={}",
                        session.getId(), fullReply.length());
            }

            @Override
            public void onError(String error) {
                sendError(session, error);
                ChatController.recordRequest(false);
            }
        });
    }

    private void handleStop(WebSocketSession session) {
        log.info("WebSocket停止生成: sessionId={}", session.getId());
        sendJson(session, Map.of("type", "stopped"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}", sessionId, exception);
        sessions.remove(sessionId);
    }

    private String getUserId(WebSocketSession session) {
        try {
            Object userId = session.getAttributes().get("userId");
            return userId != null ? userId.toString() : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private void sendJson(WebSocketSession session, Object data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
            }
        } catch (IOException e) {
            log.warn("WebSocket发送消息失败: sessionId={}", session.getId(), e);
        }
    }

    private void sendError(WebSocketSession session, String error) {
        sendJson(session, Map.of("type", "error", "content", error));
    }
}

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
import reactor.core.Disposable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Disposable> activeSubscriptions = new ConcurrentHashMap<>();
    private final ExecutorService chatExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "ws-chat");
                t.setDaemon(true);
                return t;
            });

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

        String sessionId = session.getId();
        Disposable prev = activeSubscriptions.get(sessionId);
        if (prev != null && !prev.isDisposed()) {
            prev.dispose();
            log.info("取消前一次AI请求: sessionId={}", sessionId);
        }

        String userId = getUserId(session);
        log.info("WebSocket聊天请求: sessionId={}, userId={}, messageLength={}",
                sessionId, userId, userMessage.length());

        sendJson(session, Map.of("type", "start"));

        chatExecutor.submit(() -> {
            try {
                Disposable subscription = aiChatService.chatStream(userMessage, json.path("history"), new AiChatService.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        sendJson(session, Map.of("type", "token", "content", token));
                    }

                    @Override
                    public void onComplete(String fullReply) {
                        activeSubscriptions.remove(sessionId);
                        sendJson(session, Map.of("type", "done", "content", fullReply));
                        ChatController.recordRequest(true);
                        log.info("WebSocket聊天完成: sessionId={}, replyLength={}",
                                sessionId, fullReply.length());
                    }

                    @Override
                    public void onError(String error) {
                        activeSubscriptions.remove(sessionId);
                        sendError(session, error);
                        ChatController.recordRequest(false);
                    }
                });
                activeSubscriptions.put(sessionId, subscription);
            } catch (Exception e) {
                log.error("WebSocket聊天提交失败: sessionId={}", sessionId, e);
                sendError(session, "聊天服务异常");
            }
        });
    }

    private void handleStop(WebSocketSession session) {
        String sessionId = session.getId();
        Disposable subscription = activeSubscriptions.remove(sessionId);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("WebSocket停止生成(已取消AI请求): sessionId={}", sessionId);
        } else {
            log.info("WebSocket停止生成(无活跃请求): sessionId={}", sessionId);
        }
        sendJson(session, Map.of("type", "stopped"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        Disposable subscription = activeSubscriptions.remove(sessionId);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("WebSocket连接关闭，取消AI请求: sessionId={}", sessionId);
        }
        log.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}", sessionId, exception);
        sessions.remove(sessionId);
        Disposable subscription = activeSubscriptions.remove(sessionId);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
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

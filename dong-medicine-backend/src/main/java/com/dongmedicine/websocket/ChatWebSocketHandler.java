package com.dongmedicine.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.controller.ChatController;
import com.dongmedicine.service.AiChatService;
import com.dongmedicine.service.ChatHistoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_CHAT_THREADS = 50;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final AiChatService aiChatService;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Disposable> activeSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, String> chatSessionIds = new ConcurrentHashMap<>();
    private final ExecutorService chatExecutor = Executors.newFixedThreadPool(
            MAX_CHAT_THREADS,
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "ws-chat-" + counter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            });

    public ChatWebSocketHandler(AiChatService aiChatService, ChatHistoryService chatHistoryService,
                                ObjectMapper objectMapper) {
        this.aiChatService = aiChatService;
        this.chatHistoryService = chatHistoryService;
        this.objectMapper = objectMapper;
    }

    @PreDestroy
    public void destroy() {
        chatExecutor.shutdown();
        try {
            if (!chatExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                chatExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            chatExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        activeSubscriptions.values().forEach(d -> {
            if (d != null && !d.isDisposed()) d.dispose();
        });
        log.info("ChatWebSocketHandler 已关闭");
    }

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
        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            sendError(session, "消息内容不能超过" + MAX_MESSAGE_LENGTH + "字符");
            return;
        }

        String wsSessionId = session.getId();
        Disposable prev = activeSubscriptions.get(wsSessionId);
        if (prev != null && !prev.isDisposed()) {
            prev.dispose();
            log.info("取消前一次AI请求: sessionId={}", wsSessionId);
        }

        // Generate or reuse chat session ID
        String chatSessionId = json.has("sessionId") && !json.path("sessionId").asText().isBlank()
                ? json.path("sessionId").asText()
                : chatSessionIds.computeIfAbsent(wsSessionId, k -> UUID.randomUUID().toString());
        // Always update mapping so disconnect handler flushes the correct session
        chatSessionIds.put(wsSessionId, chatSessionId);

        String userIdStr = getUserId(session);
        Integer userId = "anonymous".equals(userIdStr) ? null : Integer.parseInt(userIdStr);

        log.info("WebSocket聊天请求: wsSessionId={}, chatSessionId={}, userId={}, messageLength={}",
                wsSessionId, chatSessionId, userId, userMessage.length());

        // Save user message to Redis (deferred MySQL persist on disconnect)
        if (userId != null) {
            try {
                chatHistoryService.saveMessageToRedis(userId, chatSessionId, "user", userMessage);
            } catch (Exception e) {
                log.warn("保存用户消息到Redis失败: userId={}, chatSessionId={}", userId, chatSessionId, e);
            }
        }

        sendJson(session, Map.of("type", "start", "sessionId", chatSessionId));

        chatExecutor.submit(() -> {
            try {
                Disposable subscription = aiChatService.chatStream(userMessage, json.path("history"), new AiChatService.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        sendJson(session, Map.of("type", "token", "content", token));
                    }

                    @Override
                    public void onComplete(String fullReply) {
                        activeSubscriptions.remove(wsSessionId);
                        sendJson(session, Map.of("type", "done", "content", fullReply, "sessionId", chatSessionId));
                        ChatController.recordRequest(true);

                        // Save assistant response to Redis
                        if (userId != null) {
                            try {
                                chatHistoryService.saveMessageToRedis(userId, chatSessionId, "assistant", fullReply);
                            } catch (Exception e) {
                                log.warn("保存AI回复到Redis失败: userId={}, chatSessionId={}", userId, chatSessionId, e);
                            }
                        }

                        log.info("WebSocket聊天完成: chatSessionId={}, replyLength={}",
                                chatSessionId, fullReply.length());
                    }

                    @Override
                    public void onError(String error) {
                        activeSubscriptions.remove(wsSessionId);
                        sendError(session, error);
                        ChatController.recordRequest(false);
                    }
                });
                activeSubscriptions.put(wsSessionId, subscription);
            } catch (Exception e) {
                log.error("WebSocket聊天提交失败: chatSessionId={}", chatSessionId, e);
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
        String wsSessionId = session.getId();
        String chatSessionId = chatSessionIds.remove(wsSessionId);
        sessions.remove(wsSessionId);
        Disposable subscription = activeSubscriptions.remove(wsSessionId);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }

        // Flush Redis messages to MySQL on disconnect
        if (chatSessionId != null) {
            String userIdStr = getUserId(session);
            if (!"anonymous".equals(userIdStr)) {
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    chatHistoryService.flushSessionToMysql(userId, chatSessionId);
                    log.info("WebSocket断开，会话已持久化: userId={}, chatSessionId={}", userId, chatSessionId);
                } catch (Exception e) {
                    log.warn("WebSocket断开持久化失败: chatSessionId={}", chatSessionId, e);
                }
            }
        }
        log.info("WebSocket连接关闭: wsSessionId={}, chatSessionId={}, status={}", wsSessionId, chatSessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}", sessionId, exception);
        sessions.remove(sessionId);
        chatSessionIds.remove(sessionId);
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

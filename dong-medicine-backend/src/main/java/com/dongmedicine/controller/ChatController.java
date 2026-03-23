package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;
import com.dongmedicine.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong successRequests = new AtomicLong(0);
    private static final AtomicLong failedRequests = new AtomicLong(0);

    @Autowired
    private AiChatService aiChatService;

    @PostMapping
    @RateLimit(value = 10, key = "chat")
    public R<String> chat(@Valid @RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        totalRequests.incrementAndGet();
        
        String clientIp = getClientIp(httpRequest);
        String userId = SecurityUtils.getCurrentUserId() != null 
                ? SecurityUtils.getCurrentUserId().toString() 
                : "anonymous";
        String referer = httpRequest.getHeader("Referer");
        String origin = httpRequest.getHeader("Origin");
        
        if (!isValidOrigin(origin, referer)) {
            log.warn("Chat请求来源校验失败: origin={}, referer={}, ip={}, userId={}", 
                    origin, referer, clientIp, userId);
            failedRequests.incrementAndGet();
            return R.error("请求来源不合法");
        }
        
        log.info("Chat请求: userId={}, ip={}, messageLength={}", 
                userId, clientIp, 
                request.getMessage() != null ? request.getMessage().length() : 0);
        
        ChatResponse response = aiChatService.chat(request);
        
        if (response.isSuccess()) {
            successRequests.incrementAndGet();
            log.info("Chat响应成功: userId={}, replyLength={}", 
                    userId, response.getReply() != null ? response.getReply().length() : 0);
            return R.ok(response.getReply());
        } else {
            failedRequests.incrementAndGet();
            log.warn("Chat响应失败: userId={}, error={}", userId, response.getError());
            return R.error(response.getError());
        }
    }

    @GetMapping("/stats")
    public R<ChatStats> stats() {
        return R.ok(new ChatStats(
                totalRequests.get(),
                successRequests.get(),
                failedRequests.get()
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isValidOrigin(String origin, String referer) {
        if (origin == null && referer == null) {
            return true;
        }
        
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            allowedOrigins = "http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173";
        }
        
        String[] origins = allowedOrigins.split(",");
        String checkValue = origin != null ? origin : referer;
        
        for (String allowed : origins) {
            String trimmed = allowed.trim();
            if (checkValue != null && checkValue.startsWith(trimmed)) {
                return true;
            }
        }
        
        return checkValue == null || checkValue.contains("localhost") || checkValue.contains("127.0.0.1");
    }

    public record ChatStats(long totalRequests, long successRequests, long failedRequests) {}
}

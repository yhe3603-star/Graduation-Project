package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@Tag(name = "智能问答", description = "AI智能对话服务")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final StringRedisTemplate redisTemplate;

    private static final String STATS_PREFIX = "chat:stats:";

    public void recordRequest(boolean success) {
        redisTemplate.opsForValue().increment(STATS_PREFIX + "total");
        if (success) {
            redisTemplate.opsForValue().increment(STATS_PREFIX + "success");
        } else {
            redisTemplate.opsForValue().increment(STATS_PREFIX + "failed");
        }
    }

    @Operation(summary = "获取聊天统计")
    @GetMapping("/stats")
    public R<ChatStats> stats() {
        long total = getCounter(STATS_PREFIX + "total");
        long success = getCounter(STATS_PREFIX + "success");
        long failed = getCounter(STATS_PREFIX + "failed");
        return R.ok(new ChatStats(total, success, failed));
    }

    private long getCounter(String key) {
        String val = redisTemplate.opsForValue().get(key);
        if (val == null) return 0;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public record ChatStats(long totalRequests, long successRequests, long failedRequests) {}
}

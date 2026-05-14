package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "消息通知", description = "用户消息通知接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final StringRedisTemplate redisTemplate;

    private static final String NOTIFICATION_KEY_PREFIX = "notification:user:";

    @Operation(summary = "获取通知列表")
    @GetMapping
    public R<List<String>> list() {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(Collections.emptyList());
        String key = NOTIFICATION_KEY_PREFIX + userId;
        List<String> notifications = redisTemplate.opsForList().range(key, 0, 49);
        return R.ok(notifications != null ? notifications : Collections.emptyList());
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public R<Map<String, Object>> unreadCount() {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(Map.of("count", 0));
        String unreadKey = NOTIFICATION_KEY_PREFIX + userId + ":unread";
        String val = redisTemplate.opsForValue().get(unreadKey);
        int count = val != null ? Integer.parseInt(val) : 0;
        return R.ok(Map.of("count", count));
    }

    @Operation(summary = "标记全部通知已读")
    @PostMapping("/read")
    public R<String> markAllRead() {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok("ok");
        String unreadKey = NOTIFICATION_KEY_PREFIX + userId + ":unread";
        redisTemplate.delete(unreadKey);
        return R.ok("ok");
    }
}

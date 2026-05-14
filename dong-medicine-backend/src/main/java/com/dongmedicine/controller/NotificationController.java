package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "消息通知", description = "用户消息通知接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public R<List<String>> list() {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(Collections.emptyList());
        return R.ok(notificationService.getNotifications(userId));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public R<Map<String, Object>> unreadCount() {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(Map.of("count", 0));
        return R.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @Operation(summary = "标记全部通知已读")
    @PostMapping("/read")
    public R<String> markAllRead() {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok("ok");
        notificationService.markAllRead(userId);
        return R.ok("ok");
    }
}

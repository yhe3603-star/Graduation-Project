package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "AI聊天历史", description = "AI聊天记录的持久化存储与查询")
@Slf4j
@RestController
@RequestMapping("/api/chat-history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @Operation(summary = "获取聊天会话列表")
    @GetMapping("/sessions")
    @SaCheckLogin
    public R<List<Map<String, Object>>> listSessions() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(chatHistoryService.getUserSessions(userId));
    }

    @Operation(summary = "获取会话消息记录")
    @GetMapping("/sessions/{sessionId}")
    @SaCheckLogin
    public R<List<ChatHistory>> getSessionMessages(@Parameter(name = "sessionId", description = "会话ID") @PathVariable String sessionId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(chatHistoryService.getSessionHistory(userId, sessionId));
    }

    @Operation(summary = "删除聊天会话")
    @DeleteMapping("/sessions/{sessionId}")
    @SaCheckLogin
    public R<String> deleteSession(@Parameter(name = "sessionId", description = "会话ID") @PathVariable String sessionId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        chatHistoryService.deleteSession(userId, sessionId);
        return R.ok("删除成功");
    }
}

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

@Tag(name = "AI聊天历史", description = "AI聊天记录的持久化存储与查询")
@Slf4j
@RestController
@RequestMapping("/api/chat-history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @GetMapping("/sessions")
    @SaCheckLogin
    public R<List<Map<String, Object>>> listSessions() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(chatHistoryService.getUserSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}")
    @SaCheckLogin
    public R<List<ChatHistory>> getSessionMessages(@PathVariable String sessionId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(chatHistoryService.getSessionHistory(userId, sessionId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @SaCheckLogin
    public R<String> deleteSession(@PathVariable String sessionId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        chatHistoryService.deleteSession(userId, sessionId);
        return R.ok("删除成功");
    }
}

package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.mapper.ChatHistoryMapper;
import com.dongmedicine.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(Integer userId, String sessionId, String role, String content) {
        ChatHistory record = new ChatHistory();
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setRole(role);
        record.setContent(content);
        save(record);
    }

    @Override
    public List<ChatHistory> getSessionHistory(Integer userId, String sessionId) {
        return list(new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getSessionId, sessionId)
                .orderByAsc(ChatHistory::getCreatedAt));
    }

    @Override
    public List<Map<String, Object>> getUserSessions(Integer userId) {
        // Get recent messages grouped by session_id
        List<ChatHistory> all = list(new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, userId)
                .orderByDesc(ChatHistory::getCreatedAt)
                .last("LIMIT 500"));

        Map<String, List<ChatHistory>> grouped = all.stream()
                .collect(Collectors.groupingBy(ChatHistory::getSessionId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<ChatHistory>> entry : grouped.entrySet()) {
            String sessionId = entry.getKey();
            List<ChatHistory> messages = entry.getValue();
            ChatHistory first = messages.get(messages.size() - 1); // oldest message
            ChatHistory last = messages.get(0); // newest message

            Map<String, Object> session = new LinkedHashMap<>();
            session.put("sessionId", sessionId);
            session.put("preview", first.getContent().length() > 50
                    ? first.getContent().substring(0, 50) + "..."
                    : first.getContent());
            session.put("messageCount", messages.size());
            session.put("lastMessageAt", last.getCreatedAt());
            result.add(session);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Integer userId, String sessionId) {
        remove(new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getSessionId, sessionId));
    }
}

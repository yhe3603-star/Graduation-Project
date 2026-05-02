package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.mapper.ChatHistoryMapper;
import com.dongmedicine.service.ChatHistoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    private final StringRedisTemplate redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String REDIS_SESSION_KEY = "chat:session:%d:%s";
    private static final String REDIS_ACTIVE_SET = "chat:active:%d";
    private static final Duration REDIS_TTL = Duration.ofHours(2);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(Integer userId, String sessionId, String role, String content) {
        try {
            ChatHistory record = new ChatHistory();
            record.setUserId(userId);
            record.setSessionId(sessionId);
            record.setRole(role);
            record.setContent(content);
            save(record);
        } catch (Exception e) {
            log.warn("保存AI聊天消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void saveMessageToRedis(Integer userId, String sessionId, String role, String content) {
        try {
            String hashKey = String.format(REDIS_SESSION_KEY, userId, sessionId);
            String activeKey = String.format(REDIS_ACTIVE_SET, userId);
            String msgId = UUID.randomUUID().toString().substring(0, 8);
            Map<String, String> msg = new LinkedHashMap<>();
            msg.put("role", role);
            msg.put("content", content);
            msg.put("createdAt", LocalDateTime.now().toString());
            redisTemplate.opsForHash().put(hashKey, msgId, objectMapper.writeValueAsString(msg));
            redisTemplate.expire(hashKey, REDIS_TTL);
            redisTemplate.opsForSet().add(activeKey, sessionId);
            redisTemplate.expire(activeKey, REDIS_TTL);
        } catch (Exception e) {
            log.warn("保存消息到Redis失败: {}", e.getMessage());
        }
    }

    @Override
    public List<ChatHistory> getSessionHistory(Integer userId, String sessionId) {
        // Check Redis first for active session messages
        List<ChatHistory> redisMessages = getSessionHistoryFromRedis(userId, sessionId);
        if (!redisMessages.isEmpty()) {
            return redisMessages;
        }
        // Fall back to MySQL
        try {
            return list(new LambdaQueryWrapper<ChatHistory>()
                    .eq(ChatHistory::getUserId, userId)
                    .eq(ChatHistory::getSessionId, sessionId)
                    .orderByAsc(ChatHistory::getCreatedAt));
        } catch (Exception e) {
            log.warn("查询AI会话消息失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getUserSessions(Integer userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> seenSessionIds = new HashSet<>();

        // 1. Get active Redis sessions first
        try {
            String activeKey = String.format(REDIS_ACTIVE_SET, userId);
            Set<String> activeSessionIds = redisTemplate.opsForSet().members(activeKey);
            if (activeSessionIds != null) {
                for (String sid : activeSessionIds) {
                    List<ChatHistory> msgs = getSessionHistoryFromRedis(userId, sid);
                    if (!msgs.isEmpty()) {
                        seenSessionIds.add(sid);
                        ChatHistory first = msgs.get(0);
                        ChatHistory last = msgs.get(msgs.size() - 1);
                        Map<String, Object> session = new LinkedHashMap<>();
                        session.put("sessionId", sid);
                        session.put("preview", first.getContent().length() > 50
                                ? first.getContent().substring(0, 50) + "..."
                                : first.getContent());
                        session.put("messageCount", msgs.size());
                        session.put("lastMessageAt", last.getCreatedAt());
                        result.add(session);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询Redis活跃会话失败: {}", e.getMessage());
        }

        // 2. Get MySQL sessions (excluding those already from Redis)
        try {
            List<ChatHistory> all = list(new LambdaQueryWrapper<ChatHistory>()
                    .eq(ChatHistory::getUserId, userId)
                    .orderByDesc(ChatHistory::getCreatedAt)
                    .last("LIMIT 500"));

            Map<String, List<ChatHistory>> grouped = all.stream()
                    .filter(h -> !seenSessionIds.contains(h.getSessionId()))
                    .collect(Collectors.groupingBy(ChatHistory::getSessionId, LinkedHashMap::new, Collectors.toList()));

            for (Map.Entry<String, List<ChatHistory>> entry : grouped.entrySet()) {
                String sessionId = entry.getKey();
                List<ChatHistory> messages = entry.getValue();
                ChatHistory first = messages.get(messages.size() - 1);
                ChatHistory last = messages.get(0);

                Map<String, Object> session = new LinkedHashMap<>();
                session.put("sessionId", sessionId);
                session.put("preview", first.getContent().length() > 50
                        ? first.getContent().substring(0, 50) + "..."
                        : first.getContent());
                session.put("messageCount", messages.size());
                session.put("lastMessageAt", last.getCreatedAt());
                result.add(session);
            }
        } catch (Exception e) {
            log.warn("查询MySQL会话列表失败: {}", e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Integer userId, String sessionId) {
        // Delete from MySQL
        remove(new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getSessionId, sessionId));
        // Delete from Redis
        try {
            String hashKey = String.format(REDIS_SESSION_KEY, userId, sessionId);
            String activeKey = String.format(REDIS_ACTIVE_SET, userId);
            redisTemplate.delete(hashKey);
            redisTemplate.opsForSet().remove(activeKey, sessionId);
        } catch (Exception e) {
            log.warn("删除Redis会话失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void flushSessionToMysql(Integer userId, String sessionId) {
        List<ChatHistory> messages = getSessionHistoryFromRedis(userId, sessionId);
        if (messages.isEmpty()) {
            return;
        }
        try {
            for (ChatHistory msg : messages) {
                save(msg);
            }
            // Clean Redis after successful MySQL persist
            String hashKey = String.format(REDIS_SESSION_KEY, userId, sessionId);
            String activeKey = String.format(REDIS_ACTIVE_SET, userId);
            redisTemplate.delete(hashKey);
            redisTemplate.opsForSet().remove(activeKey, sessionId);
            log.info("会话已从Redis持久化到MySQL: userId={}, sessionId={}, messageCount={}",
                    userId, sessionId, messages.size());
        } catch (Exception e) {
            log.warn("持久化会话到MySQL失败: userId={}, sessionId={}", userId, sessionId, e);
        }
    }

    private List<ChatHistory> getSessionHistoryFromRedis(Integer userId, String sessionId) {
        try {
            String hashKey = String.format(REDIS_SESSION_KEY, userId, sessionId);
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
            if (entries.isEmpty()) {
                return Collections.emptyList();
            }
            List<ChatHistory> messages = new ArrayList<>();
            for (Object value : entries.values()) {
                Map<String, String> msg = objectMapper.readValue(
                        value.toString(), new TypeReference<Map<String, String>>() {});
                ChatHistory h = new ChatHistory();
                h.setUserId(userId);
                h.setSessionId(sessionId);
                h.setRole(msg.get("role"));
                h.setContent(msg.get("content"));
                h.setCreatedAt(LocalDateTime.parse(msg.get("createdAt")));
                messages.add(h);
            }
            messages.sort(Comparator.comparing(ChatHistory::getCreatedAt));
            return messages;
        } catch (Exception e) {
            log.warn("从Redis读取会话消息失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

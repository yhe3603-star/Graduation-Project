package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.mapper.ChatHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ChatHistoryMapper chatHistoryMapper;

    @InjectMocks
    private ChatHistoryServiceImpl chatHistoryService;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private void setBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(chatHistoryService, chatHistoryMapper);
        // Common Redis mocks used across multiple tests
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("保存聊天消息到MySQL - 成功")
    void testSaveMessageSuccess() {
        when(chatHistoryMapper.insert(any(ChatHistory.class))).thenReturn(1);

        assertDoesNotThrow(() ->
                chatHistoryService.saveMessage(1, "session1", "user", "你好"));

        verify(chatHistoryMapper, times(1)).insert(any(ChatHistory.class));
    }

    @Test
    @DisplayName("保存聊天消息到MySQL - 失败静默处理不抛异常")
    void testSaveMessageFailure() {
        when(chatHistoryMapper.insert(any(ChatHistory.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() ->
                chatHistoryService.saveMessage(1, "session1", "user", "你好"));
    }

    @Test
    @DisplayName("保存消息到Redis - 成功")
    void testSaveMessageToRedis() {
        doNothing().when(hashOperations).put(anyString(), any(), any());
        when(redisTemplate.expire(anyString(), any())).thenReturn(true);

        assertDoesNotThrow(() ->
                chatHistoryService.saveMessageToRedis(1, "session1", "user", "你好"));

        verify(hashOperations, times(1)).put(anyString(), any(), any());
        verify(redisTemplate, times(2)).expire(anyString(), any());
        verify(setOperations, times(1)).add(anyString(), anyString());
    }

    @Test
    @DisplayName("保存消息到Redis - 失败静默处理不抛异常")
    void testSaveMessageToRedisFailure() {
        doThrow(new RuntimeException("Redis error"))
                .when(hashOperations).put(anyString(), any(), any());

        assertDoesNotThrow(() ->
                chatHistoryService.saveMessageToRedis(1, "session1", "user", "你好"));
    }

    @Test
    @DisplayName("获取会话历史 - Redis无数据回退到MySQL")
    void testGetSessionHistoryFromMysql() {
        ChatHistory history = new ChatHistory();
        history.setId(1L);
        history.setUserId(1);
        history.setSessionId("session1");
        history.setRole("user");
        history.setContent("你好");
        history.setCreatedAt(LocalDateTime.now());

        when(hashOperations.entries(anyString())).thenReturn(Collections.emptyMap());
        when(chatHistoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(history));

        List<ChatHistory> result = chatHistoryService.getSessionHistory(1, "session1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("你好", result.get(0).getContent());
    }

    @Test
    @DisplayName("获取会话历史 - 异常返回空列表")
    void testGetSessionHistoryException() {
        when(hashOperations.entries(anyString())).thenReturn(Collections.emptyMap());
        when(chatHistoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("DB error"));

        List<ChatHistory> result = chatHistoryService.getSessionHistory(1, "session1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("删除会话 - 成功从MySQL和Redis删除")
    void testDeleteSession() {
        when(chatHistoryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        assertDoesNotThrow(() ->
                chatHistoryService.deleteSession(1, "session1"));

        verify(chatHistoryMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(setOperations, times(1)).remove(anyString(), any());
    }

    @Test
    @DisplayName("获取用户会话列表 - MySQL有数据")
    void testGetUserSessionsFromMysql() {
        ChatHistory history1 = new ChatHistory();
        history1.setId(1L);
        history1.setUserId(1);
        history1.setSessionId("session1");
        history1.setRole("user");
        history1.setContent("你好啊，我想了解侗药");
        history1.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        ChatHistory history2 = new ChatHistory();
        history2.setId(2L);
        history2.setUserId(1);
        history2.setSessionId("session1");
        history2.setRole("assistant");
        history2.setContent("好的，让我为您介绍侗药知识");
        history2.setCreatedAt(LocalDateTime.now());

        when(setOperations.members(anyString())).thenReturn(Collections.emptySet());
        when(chatHistoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(history2, history1));

        List<Map<String, Object>> result = chatHistoryService.getUserSessions(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("session1", result.get(0).get("sessionId"));
        assertEquals(2, result.get(0).get("messageCount"));
    }

    @Test
    @DisplayName("获取用户会话列表 - 无数据返回空列表")
    void testGetUserSessionsEmpty() {
        when(setOperations.members(anyString())).thenReturn(Collections.emptySet());
        when(chatHistoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = chatHistoryService.getUserSessions(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

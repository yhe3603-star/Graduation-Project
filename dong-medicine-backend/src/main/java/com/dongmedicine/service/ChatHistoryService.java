package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.ChatHistory;

import java.util.List;
import java.util.Map;

public interface ChatHistoryService extends IService<ChatHistory> {

    void saveMessage(Integer userId, String sessionId, String role, String content);

    void saveMessageToRedis(Integer userId, String sessionId, String role, String content);

    List<ChatHistory> getSessionHistory(Integer userId, String sessionId);

    List<Map<String, Object>> getUserSessions(Integer userId);

    void deleteSession(Integer userId, String sessionId);

    void flushSessionToMysql(Integer userId, String sessionId);
}

package com.dongmedicine.service.impl;

import com.dongmedicine.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final StringRedisTemplate redisTemplate;

    private static final String NOTIFICATION_KEY_PREFIX = "notification:user:";

    @Override
    public List<String> getNotifications(Integer userId) {
        String key = NOTIFICATION_KEY_PREFIX + userId;
        List<String> notifications = redisTemplate.opsForList().range(key, 0, 49);
        return notifications != null ? notifications : Collections.emptyList();
    }

    @Override
    public int getUnreadCount(Integer userId) {
        String unreadKey = NOTIFICATION_KEY_PREFIX + userId + ":unread";
        String val = redisTemplate.opsForValue().get(unreadKey);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            log.warn("无效的未读通知计数: userId={}, val={}", userId, val);
            redisTemplate.delete(unreadKey);
            return 0;
        }
    }

    @Override
    public void markAllRead(Integer userId) {
        String unreadKey = NOTIFICATION_KEY_PREFIX + userId + ":unread";
        redisTemplate.delete(unreadKey);
    }
}

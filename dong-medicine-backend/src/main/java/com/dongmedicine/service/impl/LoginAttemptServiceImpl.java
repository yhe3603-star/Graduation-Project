package com.dongmedicine.service.impl;

import com.dongmedicine.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务实现
 *
 * <p>使用 Redis 跟踪登录失败次数。超过阈值后账户被临时锁定。
 * 当 Redis 不可用时优雅降级，允许登录请求通过。</p>
 */
@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_SECONDS = 900; // 15 minutes
    private static final String KEY_PREFIX = "login:attempts:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Override
    public void recordFailure(String username) {
        if (redisTemplate == null) return;
        try {
            String key = KEY_PREFIX + username;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, LOCKOUT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: {}", e.getMessage());
        }
    }

    @Override
    public void recordSuccess(String username) {
        if (redisTemplate == null) return;
        try {
            redisTemplate.delete(KEY_PREFIX + username);
        } catch (Exception e) {
            log.warn("清除登录失败次数异常: {}", e.getMessage());
        }
    }

    @Override
    public boolean isLocked(String username) {
        if (redisTemplate == null) return false;
        try {
            String val = redisTemplate.opsForValue().get(KEY_PREFIX + username);
            return val != null && Integer.parseInt(val) >= MAX_ATTEMPTS;
        } catch (Exception e) {
            log.warn("检查账户锁定状态异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getRemainingAttempts(String username) {
        if (redisTemplate == null) return MAX_ATTEMPTS;
        try {
            String val = redisTemplate.opsForValue().get(KEY_PREFIX + username);
            int count = val != null ? Integer.parseInt(val) : 0;
            return Math.max(0, MAX_ATTEMPTS - count);
        } catch (Exception e) {
            return MAX_ATTEMPTS;
        }
    }

    @Override
    public long getLockoutRemainingSeconds(String username) {
        if (redisTemplate == null) return 0;
        try {
            Long ttl = redisTemplate.getExpire(KEY_PREFIX + username, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

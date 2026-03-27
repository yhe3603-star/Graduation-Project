package com.dongmedicine.service.impl;

import com.dongmedicine.config.JwtUtil;
import com.dongmedicine.service.TokenBlacklistService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final int LOCAL_CACHE_MAX_SIZE = 10000;
    private static final long MAX_TOKEN_EXPIRY_HOURS = 2;
    
    private final AtomicBoolean redisAvailable = new AtomicBoolean(true);
    private long lastRedisCheckTime = 0;
    private static final long REDIS_CHECK_INTERVAL = 60000;

    private final Cache<String, Boolean> localBlacklist = Caffeine.newBuilder()
            .maximumSize(LOCAL_CACHE_MAX_SIZE)
            .expireAfterWrite(MAX_TOKEN_EXPIRY_HOURS, TimeUnit.HOURS)
            .recordStats()
            .build();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        String cleanToken = token.replace("Bearer ", "");
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseToken(cleanToken);
        if (tokenInfo != null && tokenInfo.getClaims() != null && tokenInfo.getExpiration() != null) {
            Long expirationTime = tokenInfo.getExpiration().getTime();
            long ttl = expirationTime - System.currentTimeMillis();
            if (ttl > 0) {
                long ttlMinutes = TimeUnit.MILLISECONDS.toMinutes(ttl);
                long expireMinutes = Math.min(ttlMinutes, TimeUnit.HOURS.toMinutes(MAX_TOKEN_EXPIRY_HOURS));
                
                boolean addedToRedis = false;
                
                if (isRedisAvailable()) {
                    try {
                        String key = BLACKLIST_PREFIX + cleanToken;
                        stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
                        log.debug("Token added to Redis blacklist, expires in {} ms", ttl);
                        addedToRedis = true;
                        markRedisAvailable();
                    } catch (Exception e) {
                        log.warn("Redis 不可用，降级到本地缓存: {}", e.getMessage());
                        markRedisUnavailable();
                    }
                }
                
                if (!addedToRedis) {
                    localBlacklist.put(cleanToken, Boolean.TRUE);
                    log.debug("Token added to local cache blacklist, expires in {} minutes", expireMinutes);
                }
            }
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String cleanToken = token.replace("Bearer ", "");
        
        Boolean localResult = localBlacklist.getIfPresent(cleanToken);
        if (Boolean.TRUE.equals(localResult)) {
            return true;
        }
        
        if (isRedisAvailable()) {
            String key = BLACKLIST_PREFIX + cleanToken;
            try {
                boolean result = Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
                markRedisAvailable();
                return result;
            } catch (Exception e) {
                log.warn("Redis 不可用，降级到本地缓存查询: {}", e.getMessage());
                markRedisUnavailable();
            }
        }
        
        return Boolean.TRUE.equals(localBlacklist.getIfPresent(cleanToken));
    }

    @Override
    public void cleanExpiredTokens() {
        log.debug("Redis and Caffeine automatically handle expired tokens");
    }

    public long getBlacklistSize() {
        long redisCount = 0;
        if (isRedisAvailable()) {
            try {
                try (var cursor = stringRedisTemplate.scan(
                        org.springframework.data.redis.core.ScanOptions.scanOptions()
                                .match(BLACKLIST_PREFIX + "*")
                                .count(1000)
                                .build())) {
                    while (cursor.hasNext()) {
                        cursor.next();
                        redisCount++;
                    }
                }
                markRedisAvailable();
            } catch (Exception e) {
                log.warn("Redis 不可用，无法获取Redis黑名单大小: {}", e.getMessage());
                markRedisUnavailable();
            }
        }
        
        long localCount = localBlacklist.estimatedSize();
        return redisCount > 0 ? redisCount : localCount;
    }
    
    private boolean isRedisAvailable() {
        long now = System.currentTimeMillis();
        if (!redisAvailable.get() && (now - lastRedisCheckTime) < REDIS_CHECK_INTERVAL) {
            return false;
        }
        return true;
    }
    
    private void markRedisAvailable() {
        redisAvailable.set(true);
        lastRedisCheckTime = System.currentTimeMillis();
    }
    
    private void markRedisUnavailable() {
        redisAvailable.set(false);
        lastRedisCheckTime = System.currentTimeMillis();
    }
}

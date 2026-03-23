package com.dongmedicine.service.impl;

import com.dongmedicine.config.JwtUtil;
import com.dongmedicine.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

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
                try {
                    String key = BLACKLIST_PREFIX + cleanToken;
                    stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
                    log.info("Token added to blacklist, expires in {} ms", ttl);
                } catch (Exception e) {
                    log.warn("Redis 不可用，跳过黑名单写入: {}", e.getMessage());
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
        String key = BLACKLIST_PREFIX + cleanToken;
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过黑名单查询: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void cleanExpiredTokens() {
        log.debug("Redis automatically handles expired tokens");
    }

    public long getBlacklistSize() {
        long count = 0;
        try (var cursor = stringRedisTemplate.scan(
                org.springframework.data.redis.core.ScanOptions.scanOptions()
                        .match(BLACKLIST_PREFIX + "*")
                        .count(1000)
                        .build())) {
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        }
        return count;
    }
}

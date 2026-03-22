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
                String key = BLACKLIST_PREFIX + cleanToken;
                stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
                log.info("Token added to blacklist, expires in {} ms", ttl);
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
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void cleanExpiredTokens() {
        log.debug("Redis automatically handles expired tokens");
    }

    public long getBlacklistSize() {
        var keys = stringRedisTemplate.keys(BLACKLIST_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
}

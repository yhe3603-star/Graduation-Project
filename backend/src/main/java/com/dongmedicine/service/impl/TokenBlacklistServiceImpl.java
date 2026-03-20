package com.dongmedicine.service.impl;

import com.dongmedicine.config.JwtUtil;
import com.dongmedicine.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    @Autowired
    private JwtUtil jwtUtil;

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        String cleanToken = token.replace("Bearer ", "");
        Claims claims = jwtUtil.getAllClaimsFromToken(cleanToken);
        if (claims != null) {
            Date expiration = claims.getExpiration();
            blacklist.put(cleanToken, expiration.getTime());
            log.info("Token added to blacklist, expires at: {}", expiration);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String cleanToken = token.replace("Bearer ", "");
        Long expirationTime = blacklist.get(cleanToken);
        if (expirationTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expirationTime) {
            blacklist.remove(cleanToken);
            return false;
        }
        return true;
    }

    @Override
    @Scheduled(fixedRate = 300000)
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        int removedCount = 0;
        for (Map.Entry<String, Long> entry : blacklist.entrySet()) {
            if (now > entry.getValue()) {
                blacklist.remove(entry.getKey());
                removedCount++;
            }
        }
        if (removedCount > 0) {
            log.info("Cleaned {} expired tokens from blacklist", removedCount);
        }
    }

    public int getBlacklistSize() {
        return blacklist.size();
    }
}

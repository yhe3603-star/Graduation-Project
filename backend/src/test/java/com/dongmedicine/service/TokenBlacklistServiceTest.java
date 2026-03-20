package com.dongmedicine.service;

import com.dongmedicine.service.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistServiceImpl();
    }

    @Test
    @DisplayName("添加Token到黑名单")
    void testAddToBlacklist() {
        String token = "test-token-123";
        
        assertFalse(tokenBlacklistService.isBlacklisted(token));
        
        tokenBlacklistService.addToBlacklist(token);
        
        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("检查未在黑名单的Token")
    void testIsNotBlacklisted() {
        String token = "non-blacklisted-token";
        
        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("清理过期Token")
    void testCleanExpiredTokens() {
        String token = "expired-token";
        
        tokenBlacklistService.addToBlacklist(token);
        assertTrue(tokenBlacklistService.isBlacklisted(token));
        
        tokenBlacklistService.cleanExpiredTokens();
        
        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("添加多个Token到黑名单")
    void testAddMultipleTokens() {
        String token1 = "token-1";
        String token2 = "token-2";
        String token3 = "token-3";
        
        tokenBlacklistService.addToBlacklist(token1);
        tokenBlacklistService.addToBlacklist(token2);
        tokenBlacklistService.addToBlacklist(token3);
        
        assertTrue(tokenBlacklistService.isBlacklisted(token1));
        assertTrue(tokenBlacklistService.isBlacklisted(token2));
        assertTrue(tokenBlacklistService.isBlacklisted(token3));
    }

    @Test
    @DisplayName("重复添加相同Token")
    void testAddSameTokenTwice() {
        String token = "duplicate-token";
        
        tokenBlacklistService.addToBlacklist(token);
        tokenBlacklistService.addToBlacklist(token);
        
        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }
}

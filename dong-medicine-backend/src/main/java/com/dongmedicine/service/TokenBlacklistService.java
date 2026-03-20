package com.dongmedicine.service;

public interface TokenBlacklistService {

    void addToBlacklist(String token);

    boolean isBlacklisted(String token);

    void cleanExpiredTokens();
}

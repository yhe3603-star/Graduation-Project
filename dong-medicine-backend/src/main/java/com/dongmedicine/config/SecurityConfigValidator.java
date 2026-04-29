package com.dongmedicine.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityConfigValidator {

    private static final int MIN_JWT_SECRET_LENGTH = 32;

    @Value("${sa-token.jwt-secret-key:}")
    private String saTokenJwtSecret;

    @Value("${app.security.jwt-secret:}")
    private String appJwtSecret;

    @PostConstruct
    public void validateSecurityConfig() {
        String effectiveSecret = saTokenJwtSecret != null && !saTokenJwtSecret.isBlank()
                ? saTokenJwtSecret : appJwtSecret;

        if (effectiveSecret == null || effectiveSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "【严重安全警告】JWT密钥未配置！\n" +
                "请通过环境变量 JWT_SECRET 设置安全的密钥（至少32字符）\n" +
                "开发环境可在 application-dev.yml 中配置默认密钥"
            );
        }

        if (effectiveSecret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("JWT密钥长度不足！当前: %d，最小: %d",
                    effectiveSecret.length(), MIN_JWT_SECRET_LENGTH)
            );
        }

        log.info("安全配置校验通过 - JWT密钥长度: {}", effectiveSecret.length());
    }
}

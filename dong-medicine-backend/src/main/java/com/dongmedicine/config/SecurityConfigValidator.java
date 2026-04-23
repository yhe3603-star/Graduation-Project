package com.dongmedicine.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityConfigValidator {

    private static final String DEFAULT_JWT_SECRET = "dev-secret-key-for-jwt-token-generation-must-be-at-least-64-characters-long-for-hs512";
    private static final int MIN_JWT_SECRET_LENGTH = 32;

    @Value("${sa-token.jwt-secret-key:" + DEFAULT_JWT_SECRET + "}")
    private String jwtSecret;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void validateSecurityConfig() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置！请在配置文件或环境变量中设置 sa-token.jwt-secret-key");
        }

        boolean isProduction = !environment.acceptsProfiles("dev");

        if (jwtSecret.equals(DEFAULT_JWT_SECRET)) {
            if (isProduction) {
                throw new IllegalStateException(
                    "【严重安全警告】生产环境使用了默认JWT密钥！\n" +
                    "请立即通过环境变量设置安全的JWT_SECRET"
                );
            } else {
                log.warn("========================================");
                log.warn("【安全警告】正在使用默认JWT密钥");
                log.warn("生产环境必须设置自定义JWT_SECRET环境变量");
                log.warn("========================================");
            }
        }

        if (jwtSecret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("JWT密钥长度不足！当前长度: %d，最小要求: %d",
                    jwtSecret.length(), MIN_JWT_SECRET_LENGTH)
            );
        }

        log.info("安全配置校验通过 - JWT密钥长度: {}, 环境: {}",
            jwtSecret.length(),
            isProduction ? "生产环境" : "开发环境");
    }
}

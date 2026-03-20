package com.dongmedicine.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityConfigValidator {

    private static final String DEFAULT_JWT_SECRET = "default-dev-secret-key-for-jwt-token-generation-must-be-at-least-64-chars-long";
    private static final int MIN_JWT_SECRET_LENGTH = 64;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void validateSecurityConfig() {
        String jwtSecret = appProperties.getSecurity().getJwtSecret();
        
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置！请在配置文件或环境变量中设置 JWT_SECRET");
        }

        boolean isProduction = !environment.acceptsProfiles("dev");
        
        if (jwtSecret.equals(DEFAULT_JWT_SECRET)) {
            if (isProduction) {
                throw new IllegalStateException(
                    "【严重安全警告】生产环境使用了默认JWT密钥！\n" +
                    "请立即通过环境变量设置安全的JWT_SECRET：\n" +
                    "export JWT_SECRET=\"your-secure-random-key-at-least-64-characters-long-with-special-chars-!@#$%\""
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
        
        if (isProduction) {
            if (isWeakSecret(jwtSecret)) {
                throw new IllegalStateException(
                    "【严重安全警告】JWT密钥强度不足！\n" +
                    "密钥应包含大小写字母、数字和特殊字符"
                );
            }
        }
        
        log.info("安全配置校验通过 - JWT密钥长度: {}, 环境: {}", 
            jwtSecret.length(), 
            isProduction ? "生产环境" : "开发环境");
    }

    private boolean isWeakSecret(String secret) {
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : secret.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        int complexityCount = 0;
        if (hasLower) complexityCount++;
        if (hasUpper) complexityCount++;
        if (hasDigit) complexityCount++;
        if (hasSpecial) complexityCount++;

        return complexityCount < 3;
    }

    public void logSecurityRecommendations() {
        log.info("========== 安全建议 ==========");
        log.info("1. 使用环境变量存储敏感信息");
        log.info("2. 定期更换JWT密钥");
        log.info("3. 启用HTTPS加密传输");
        log.info("4. 配置CORS白名单");
        log.info("5. 启用请求日志审计");
    }
}

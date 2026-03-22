package com.dongmedicine.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class JwtSecretValidator {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private Environment environment;

    private static final int MIN_SECRET_LENGTH = 64;
    private static final String[] INSECURE_DEFAULTS = {
            "default-dev-secret",
            "secret",
            "password",
            "changeme",
            "default"
    };

    private static String generatedDevSecret = null;

    public static String getGeneratedDevSecret() {
        return generatedDevSecret;
    }

    @PostConstruct
    public void validateJwtSecret() {
        String jwtSecret = appProperties.getSecurity().getJwtSecret();
        String[] activeProfiles = environment.getActiveProfiles();

        boolean isProduction = Arrays.stream(activeProfiles)
                .anyMatch(profile -> profile.equals("prod") || profile.equals("production"));

        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            if (isProduction) {
                throw new IllegalStateException("JWT_SECRET 环境变量未设置！生产环境必须配置 JWT_SECRET 环境变量");
            } else {
                jwtSecret = generateSecureDevSecret();
                appProperties.getSecurity().setJwtSecret(jwtSecret);
                log.warn("========================================");
                log.warn("开发环境：已自动生成 JWT 密钥");
                log.warn("生产环境请设置 JWT_SECRET 环境变量");
                log.warn("========================================");
            }
        }

        if (isProduction) {
            validateProductionSecret(jwtSecret);
        } else {
            validateDevelopmentSecret(jwtSecret);
        }

        log.info("JWT 密钥校验通过，当前环境: {}", Arrays.toString(activeProfiles));
    }

    private String generateSecureDevSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        generatedDevSecret = Base64.getEncoder().encodeToString(bytes);
        return generatedDevSecret;
    }

    private void validateProductionSecret(String secret) {
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    String.format("生产环境 JWT 密钥长度不足！当前长度: %d，最小要求: %d 字符",
                            secret.length(), MIN_SECRET_LENGTH));
        }

        for (String insecure : INSECURE_DEFAULTS) {
            if (secret.toLowerCase().contains(insecure.toLowerCase())) {
                throw new IllegalStateException(
                        "生产环境 JWT 密钥包含不安全的默认值: " + insecure + "，请使用强密钥！");
            }
        }

        if (!isStrongSecret(secret)) {
            throw new IllegalStateException(
                    "生产环境 JWT 密钥强度不足！请使用包含大小写字母、数字和特殊字符的强密钥");
        }
    }

    private void validateDevelopmentSecret(String secret) {
        if (secret.length() < 32) {
            log.warn("JWT 密钥长度较短 ({} 字符)，建议在生产环境使用至少 {} 字符的密钥",
                    secret.length(), MIN_SECRET_LENGTH);
        }

        for (String insecure : INSECURE_DEFAULTS) {
            if (secret.toLowerCase().contains(insecure.toLowerCase())) {
                log.warn("JWT 密钥包含默认值 '{}'，生产环境请务必更换！", insecure);
                break;
            }
        }
    }

    private boolean isStrongSecret(String secret) {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : secret.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}

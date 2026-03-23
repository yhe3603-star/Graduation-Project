package com.dongmedicine.config.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis健康检查指示器
 * 提供Redis连接状态的健康检查，当Redis连接失败时返回警告状态而非错误状态
 */
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            // 尝试连接Redis并执行ping命令
            redisConnectionFactory.getConnection().ping();
            return Health.up()
                    .withDetail("status", "Redis connection successful")
                    .withDetail("host", redisConnectionFactory.getConnection().getNativeConnection().toString())
                    .build();
        } catch (Exception e) {
            // Redis连接失败时返回警告状态，而非错误状态
            // 这样即使Redis不可用，应用仍能正常运行（使用内存缓存）
            return Health.status("WARNING")
                    .withDetail("status", "Redis connection failed")
                    .withDetail("message", e.getMessage())
                    .withDetail("fallback", "Using in-memory cache instead")
                    .build();
        }
    }
}

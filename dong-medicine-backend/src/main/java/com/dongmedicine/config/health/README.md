# 健康检查目录 (health)

本目录存放健康检查相关的配置类，用于监控服务状态。

## 文件列表

| 文件 | 功能描述 |
|------|----------|
| CacheHealthIndicator.java | 缓存健康检查 |
| DatabaseHealthIndicator.java | 数据库健康检查 |
| RedisHealthIndicator.java | Redis健康检查 |

---

## 什么是健康检查？

健康检查用于监控服务的运行状态，Spring Boot Actuator 提供了健康检查端点 `/actuator/health`。

```
访问 /actuator/health 返回：
{
  "status": "UP",
  "components": {
    "cache": { "status": "UP" },
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

---

## 自定义健康检查

```java
/**
 * 缓存健康检查
 */
@Component
public class CacheHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // 检查缓存是否正常
        boolean isHealthy = checkCache();
        
        if (isHealthy) {
            return Health.up()
                .withDetail("cache", "Caffeine + Redis")
                .build();
        } else {
            return Health.down()
                .withDetail("error", "缓存不可用")
                .build();
        }
    }
}
```

---

## 健康状态说明

| 状态 | 说明 |
|------|------|
| UP | 服务正常 |
| DOWN | 服务不可用 |
| OUT_OF_SERVICE | 服务暂停 |
| UNKNOWN | 状态未知 |

---

**最后更新时间**：2026年4月3日

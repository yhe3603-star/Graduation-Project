# 健康检查目录 (config/health/)

> 类比：健康检查就像**定期体检** -- 医院里体检会检查你的心脏、肺、肝等各个器官是否正常，健康检查就是检查系统的各个"器官"（数据库、缓存、Redis等）是否正常工作。

## 什么是健康检查？

在项目上线后，运维人员需要随时知道系统是否正常运行。健康检查就是让系统**自己检查自己**，然后汇报"我哪里有问题"。这就像侗药铺的老药师每天早上检查药柜 -- 药材够不够、有没有过期、称有没有坏。

## 什么是 Spring Boot Actuator？

Spring Boot Actuator 是 Spring Boot 自带的**监控工具包**，它提供了一组 HTTP 接口，让你可以查看应用的运行状态。其中最重要的就是健康检查接口。

```yaml
# application.yml 中配置 Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info    # 只暴露 health 和 info 端点
  endpoint:
    health:
      show-details: always      # 显示详细健康信息
```

---

## 目录结构

```
health/
├── CacheHealthIndicator.java      # 缓存健康指标
├── DatabaseHealthIndicator.java   # 数据库健康指标
└── RedisHealthIndicator.java      # Redis健康指标
```

---

## 三个健康指标详解

### 1. DatabaseHealthIndicator -- 数据库体检

> 类比：检查药铺的**账本**能不能翻开（数据库能不能连上）。

数据库是整个系统的核心，如果连不上数据库，几乎所有功能都会瘫痪。

```java
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;  // Spring自动注入数据源

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            // 尝试获取数据库连接，执行简单的验证查询
            if (connection.isValid(2)) {  // 2秒超时
                // 连接成功 --> 状态为 UP
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("validationQuery", "SELECT 1")
                        .build();
            }
            // 连接存在但无效 --> 状态为 DOWN
            return Health.down()
                    .withDetail("error", "Database connection is not valid")
                    .build();
        } catch (Exception e) {
            // 连接失败 --> 状态为 DOWN
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

**检查逻辑**：
1. 从数据源获取一个数据库连接
2. 用 `isValid(2)` 验证连接是否有效（2秒超时）
3. 有效返回 UP，无效或异常返回 DOWN

---

### 2. RedisHealthIndicator -- Redis体检

> 类比：检查药铺的**速取药架**（Redis缓存）能不能用。不能用也不怕，还有大药柜（数据库）兜底。

Redis 是缓存服务器，即使挂了也不会导致系统崩溃（因为有降级方案），所以这里返回的是 WARNING 而不是 DOWN。

```java
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            // 尝试连接Redis，发送ping命令
            redisConnectionFactory.getConnection().ping();
            // ping成功 --> 状态为 UP
            return Health.up()
                    .withDetail("status", "Redis connection successful")
                    .build();
        } catch (Exception e) {
            // ping失败 --> 状态为 WARNING（不是DOWN！）
            // 因为Redis挂了系统还能用内存缓存继续运行
            return Health.status("WARNING")
                    .withDetail("status", "Redis connection failed")
                    .withDetail("message", e.getMessage())
                    .withDetail("fallback", "Using in-memory cache instead")
                    .build();
        }
    }
}
```

**为什么 Redis 挂了是 WARNING 而不是 DOWN？**

因为系统设计了降级方案：Redis 不可用时会自动切换到 Caffeine 内存缓存。系统仍然可以正常运行，只是性能可能下降。这就像速取药架坏了，药师还可以去大药柜取药，只是慢一点。

---

### 3. CacheHealthIndicator -- 缓存体检

> 类比：检查药铺**药架的利用率** -- 药架上的药有没有经常被取到？如果命中率太低，说明药架摆放不合理。

CacheHealthIndicator 检查的是 Caffeine 本地缓存的运行状况，包括缓存大小、命中率等统计数据。

```java
@Component
@RequiredArgsConstructor
public class CacheHealthIndicator implements HealthIndicator {

    private final CacheManager cacheManager;

    @Override
    public Health health() {
        Map<String, Object> cacheDetails = new HashMap<>();
        boolean allHealthy = true;

        // 遍历所有缓存，收集统计信息
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats stats = nativeCache.stats();

                Map<String, Object> details = new HashMap<>();
                details.put("size", nativeCache.estimatedSize());     // 缓存条目数
                details.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));  // 命中率
                details.put("hitCount", stats.hitCount());           // 命中次数
                details.put("missCount", stats.missCount());         // 未命中次数
                details.put("evictionCount", stats.evictionCount()); // 淘汰次数

                cacheDetails.put(cacheName, details);

                // 命中率低于10%且请求超过100次 --> 不健康
                if (stats.hitRate() < 0.1 && stats.requestCount() > 100) {
                    allHealthy = false;
                }
            }
        }

        if (allHealthy) {
            return Health.up().withDetails(cacheDetails).build();
        } else {
            return Health.status("WARNING")
                    .withDetail("message", "Some caches have low hit rates")
                    .withDetails(cacheDetails)
                    .build();
        }
    }
}
```

**缓存统计指标含义**：

| 指标 | 含义 | 理想值 |
|------|------|--------|
| size | 缓存中的条目数量 | 适中，不是0也不是爆满 |
| hitRate | 命中率（找到数据的比例） | > 50% |
| hitCount | 命中次数（从缓存直接拿到数据） | 越高越好 |
| missCount | 未命中次数（缓存没有，去数据库查了） | 越低越好 |
| evictionCount | 淘汰次数（缓存满了，旧数据被挤出去） | 不太高 |

---

## 如何访问健康检查

### 访问地址

```
GET /actuator/health
```

### 返回示例

当一切正常时：

```json
{
    "status": "UP",
    "components": {
        "cache": {
            "status": "UP",
            "details": {
                "plants": {
                    "size": 45,
                    "hitRate": "78.50%",
                    "hitCount": 312,
                    "missCount": 86,
                    "evictionCount": 5
                }
            }
        },
        "database": {
            "status": "UP",
            "details": {
                "database": "MySQL",
                "validationQuery": "SELECT 1"
            }
        },
        "redis": {
            "status": "UP",
            "details": {
                "status": "Redis connection successful"
            }
        }
    }
}
```

当 Redis 挂了时：

```json
{
    "status": "UP",
    "components": {
        "cache": {
            "status": "UP"
        },
        "database": {
            "status": "UP"
        },
        "redis": {
            "status": "WARNING",
            "details": {
                "status": "Redis connection failed",
                "message": "Unable to connect to Redis",
                "fallback": "Using in-memory cache instead"
            }
        }
    }
}
```

---

## 健康状态含义

| 状态 | 含义 | 类比 |
|------|------|------|
| **UP** | 一切正常 | 体检各项指标正常 |
| **DOWN** | 严重故障，功能不可用 | 某个器官出了大问题 |
| **WARNING** | 有问题但可以降级运行 | 某个指标偏高，需要注意 |
| **UNKNOWN** | 无法判断状态 | 检查结果还没出来 |
| **OUT_OF_SERVICE** | 服务暂停（如维护中） | 体检中心今天休息 |

### 状态优先级

整体状态 = 所有组件中**最严重**的状态。即：
- 如果数据库 DOWN，整体就是 DOWN
- 如果只是 Redis WARNING，整体仍然是 UP（因为系统还能用）

---

## 权限说明

在 SecurityConfig 中，健康检查接口的访问权限如下：

```java
.requestMatchers("/actuator/health").permitAll()    // 健康检查接口公开访问
.requestMatchers("/actuator/**").hasRole("ADMIN")    // 其他Actuator接口需要管理员
```

这意味着：
- `/actuator/health` -- 任何人都可以访问（方便监控工具调用）
- `/actuator/beans`、`/actuator/env` 等 -- 只有管理员可以访问（因为包含敏感信息）

---

## 常见错误

**Q: 健康检查接口返回404？**
A: 检查 `application.yml` 中是否配置了 Actuator 端点暴露。默认只暴露 `health` 和 `info`。

**Q: Redis 明明挂了，为什么整体状态还是 UP？**
A: 因为 RedisHealthIndicator 返回的是 WARNING 而不是 DOWN。这是设计决策 -- Redis 不是核心依赖，挂了系统还能用。

**Q: 缓存命中率很低怎么办？**
A: 可能是缓存时间设置太短，或者缓存Key设计不合理。参考 CacheConfig 中的 TTL 配置表，适当延长缓存时间。

**Q: 数据库健康检查超时？**
A: `connection.isValid(2)` 设置了2秒超时。如果数据库响应很慢，健康检查可能误报 DOWN。可以适当增加超时时间，但不要设太长，否则健康检查本身会拖慢系统。

---

## 代码审查与改进建议

- [性能] RedisHealthIndicator每次健康检查创建两个Redis连接且未关闭，应复用同一个连接并确保关闭

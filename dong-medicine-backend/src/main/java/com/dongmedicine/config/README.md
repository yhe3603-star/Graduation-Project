# 配置模块目录说明

## 文件夹结构

本目录包含项目的所有配置类，包括安全配置、缓存配置、切面配置等。

```
config/
├── health/                     # 健康检查
│   ├── CacheHealthIndicator.java
│   ├── DatabaseHealthIndicator.java
│   └── RedisHealthIndicator.java
├── logging/                    # 日志配置
│   └── SensitiveDataConverter.java
├── AdminDataInitializer.java   # 管理员数据初始化
├── AppProperties.java          # 应用配置属性
├── AsyncConfig.java            # 异步配置
├── CacheConfig.java            # 缓存配置
├── CustomUserDetails.java      # 自定义用户详情
├── DeepSeekConfig.java         # DeepSeek AI配置
├── FileUploadProperties.java   # 文件上传配置
├── JwtAuthenticationFilter.java# JWT认证过滤器
├── JwtSecretValidator.java     # JWT密钥验证器
├── JwtUtil.java                # JWT工具类
├── LoggingAspect.java          # 日志切面
├── MybatisPlusConfig.java      # MyBatis-Plus配置
├── OpenApiConfig.java          # OpenAPI配置
├── OperationLogAspect.java     # 操作日志切面
├── RateLimit.java              # 限流注解
├── RateLimitAspect.java        # 限流切面
├── RequestIdFilter.java        # 请求ID过滤器
├── RequestSizeFilter.java      # 请求大小过滤器
├── SecurityConfig.java         # 安全配置
├── SecurityConfigValidator.java# 安全配置验证器
├── StartupInfoPrinter.java     # 启动信息打印
├── WebMvcConfig.java           # Web MVC配置
├── XssFilter.java              # XSS过滤器
└── README.md                   # 说明文档
```

## 详细说明

### 1. SecurityConfig.java - 安全配置

**功能**：配置Spring Security安全框架。

**主要配置**：
- 禁用CSRF（使用JWT认证）
- 配置CORS跨域
- 无状态Session管理
- JWT过滤器配置
- 请求授权规则

**权限配置**：
| 路径 | 权限 |
|------|------|
| `/api/user/login`, `/api/user/register` | 公开 |
| `/api/plants/**`, `/api/knowledge/**` 等 | GET公开 |
| `/api/admin/**` | 需要ADMIN角色 |
| `/api/upload/**` | 需要ADMIN角色 |
| `/api/favorites/**` | 需要认证 |
| `/actuator/health` | 公开 |
| `/actuator/**` | 需要ADMIN角色 |

### 2. JwtUtil.java - JWT工具类

**功能**：处理JWT令牌的生成、解析和验证。

**主要方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `generateToken(username, userId, role)` | String | 生成JWT令牌 |
| `refreshToken(oldToken)` | String | 刷新令牌 |
| `parseToken(token)` | TokenInfo | 解析令牌 |
| `validateToken(token)` | boolean | 验证令牌有效性 |
| `shouldRefresh(token)` | boolean | 判断是否需要刷新 |
| `canRefresh(token)` | boolean | 判断是否可以刷新 |
| `isTokenExpired(token)` | boolean | 判断令牌是否过期 |

**TokenInfo 内部类属性**：
- `username`：用户名
- `userId`：用户ID
- `role`：角色
- `expiration`：过期时间
- `claims`：JWT Claims

**配置项**：
- `app.security.jwt-refresh-threshold-minutes`：刷新阈值（默认30分钟）
- `app.security.jwt-refresh-grace-days`：刷新宽限期（默认7天）

### 3. JwtAuthenticationFilter.java - JWT认证过滤器

**功能**：拦截请求，验证JWT令牌并设置认证信息。

**处理流程**：
1. 从请求头获取Authorization
2. 解析JWT令牌
3. 验证令牌有效性
4. 设置Spring Security上下文
5. 判断是否需要刷新令牌

### 4. RateLimit.java - 限流注解

**功能**：定义接口限流注解。

**注解属性**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| `value` | int | 10 | 每秒允许的请求数 |
| `key` | String | "" | 自定义限流key |

**使用示例**：
```java
@RateLimit(value = 5, key = "login")
@PostMapping("/login")
public R<UserVO> login(@RequestBody LoginDTO dto) {
    // ...
}
```

### 5. RateLimitAspect.java - 限流切面

**功能**：实现接口限流功能，支持Redis降级。

**实现方式**：
- **主要方式**：使用Redis的increment操作实现计数器，基于滑动窗口算法
- **降级方式**：Redis不可用时自动降级到本地令牌桶算法
- **支持级别**：用户级别和IP级别限流

**限流Key生成规则**：
- 已登录用户：`方法名:用户名`
- 未登录用户：`方法名:IP地址`

**本地令牌桶降级**：
```java
// LocalTokenBucket 内部类
- 容量：100个令牌
- 补充速率：每秒补充一次
- Redis检查间隔：30秒
```

**降级策略**：
1. Redis正常时使用Redis计数器限流
2. Redis异常时自动切换到本地令牌桶
3. 定期检查Redis是否恢复（30秒间隔）
4. Redis恢复后自动切回Redis限流

### 6. OperationLogAspect.java - 操作日志切面

**功能**：自动记录控制器操作日志。

**记录内容**：
- 操作用户
- 操作模块
- 操作类型（CREATE/UPDATE/DELETE/QUERY）
- 操作方法
- 请求参数
- 执行时长
- 操作结果（成功/失败）
- 错误信息
- 客户端IP
- 操作时间

**模块映射**：
| 控制器名 | 模块 |
|---------|------|
| UserController | USER |
| PlantController | PLANT |
| KnowledgeController | KNOWLEDGE |
| InheritorController | INHERITOR |
| ResourceController | RESOURCE |
| QaController/QuizController | QA |
| FeedbackController | FEEDBACK |
| CommentController | COMMENT |
| FavoriteController | FAVORITE |
| 其他 | SYSTEM |

### 7. LoggingAspect.java - 日志切面

**功能**：记录请求日志，便于调试和问题追踪。

**记录内容**：
- 追踪ID（traceId）
- 请求方法和路径
- 客户端IP
- 用户ID
- 方法名
- 请求参数（敏感参数脱敏）
- 响应结果
- 执行时长
- 错误信息

**敏感参数列表**：
- password, passwordHash, currentPassword, newPassword
- token, authorization, secret

**慢请求警告**：执行时间超过1000ms的请求会记录WARN级别日志

### 8. XssFilter.java - XSS过滤器

**功能**：防止XSS（跨站脚本）攻击。

**处理方式**：
- 对表单参数进行XSS过滤
- 对JSON请求体进行XSS过滤
- 对请求头进行XSS过滤
- 检测到XSS攻击时记录警告日志

**过滤规则**：
- 检测危险模式（script标签、javascript协议、事件处理器等）
- 对危险内容进行HTML转义
- 递归处理JSON对象和数组

### 9. CacheConfig.java - 缓存配置

**功能**：配置Redis缓存管理器。

**缓存区域及TTL**：
| 缓存区域 | TTL | 说明 |
|---------|-----|------|
| plants | 6小时 | 药材数据 |
| knowledges | 6小时 | 知识数据 |
| inheritors | 6小时 | 传承人数据 |
| resources | 4小时 | 资源数据 |
| users | 30分钟 | 用户数据 |
| quizQuestions | 12小时 | 答题题目 |
| searchResults | 5分钟 | 搜索结果 |
| hotData | 1小时 | 热门数据 |

**降级策略**：Redis不可用时自动切换到内存缓存

### 10. MybatisPlusConfig.java - MyBatis-Plus配置

**功能**：配置MyBatis-Plus插件。

**配置内容**：
- 分页插件（PaginationInnerInterceptor）
- 数据库类型：MySQL

### 11. WebMvcConfig.java - Web MVC配置

**功能**：配置Spring MVC。

**配置内容**：
- URL路径解码配置
- 静态资源映射（/images/**, /videos/**, /documents/**, /public/**）

**配置项**：
- `file.upload.base-path`：文件上传基础路径（默认：public）

### 12. OpenApiConfig.java - OpenAPI配置

**功能**：配置Swagger/OpenAPI文档。

**配置内容**：
- API标题：侗乡医药数字展示平台 API
- API版本：1.0.0
- JWT认证配置
- 服务器列表（开发服务器、生产服务器）

**访问地址**：
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- API Docs：`http://localhost:8080/v3/api-docs`

### 13. AppProperties.java - 应用配置属性

**功能**：定义应用配置属性类。

**配置项**：
- `app.security.jwt-secret`：JWT密钥
- `app.security.jwt-expiration`：JWT过期时间
- `app.security.cors-allowed-origins`：CORS允许的源
- `app.cache.enabled`：是否启用缓存

### 14. AsyncConfig.java - 异步配置

**功能**：配置异步任务执行器。

**配置内容**：
- 核心线程数
- 最大线程数
- 队列容量
- 线程名称前缀

### 15. DeepSeekConfig.java - DeepSeek AI配置

**功能**：配置DeepSeek AI服务。

**配置项**：
- API密钥
- API地址
- 模型名称

### 16. FileUploadProperties.java - 文件上传配置

**功能**：配置文件上传相关属性。

**配置项**：
- 上传路径
- 最大文件大小
- 允许的文件类型

### 17. RequestIdFilter.java - 请求ID过滤器

**功能**：为每个请求生成唯一ID，便于日志追踪。

**实现方式**：
- 使用UUID生成唯一请求ID
- 将请求ID存入MDC（Mapped Diagnostic Context）
- 在响应头中返回请求ID（`X-Request-Id`）

**配置常量**：
```java
public static final String REQUEST_ID_HEADER = "X-Request-Id";
public static final String REQUEST_ID_MDC_KEY = "requestId";
```

**使用场景**：
- 日志追踪：通过requestId关联同一请求的所有日志
- 问题排查：快速定位特定请求的处理过程
- 响应关联：前端可通过响应头获取请求ID

### 18. RequestSizeFilter.java - 请求大小过滤器

**功能**：限制请求体大小，防止大文件攻击。

### 19. AdminDataInitializer.java - 管理员数据初始化

**功能**：应用启动时初始化管理员账号。

### 20. StartupInfoPrinter.java - 启动信息打印

**功能**：应用启动时打印启动信息。

### 21. CustomUserDetails.java - 自定义用户详情

**功能**：实现Spring Security的UserDetails接口。

### 22. JwtSecretValidator.java - JWT密钥验证器

**功能**：验证JWT密钥的安全性和有效性。

### 23. SecurityConfigValidator.java - 安全配置验证器

**功能**：验证安全配置的正确性。

### 24-26. health/ 目录 - 健康检查

**CacheHealthIndicator.java**：缓存健康检查
**DatabaseHealthIndicator.java**：数据库健康检查
**RedisHealthIndicator.java**：Redis健康检查

### 27. logging/SensitiveDataConverter.java - 敏感数据转换器

**功能**：在日志输出时对敏感数据进行脱敏处理。

## 文件统计

| 目录/文件 | 文件数 | 主要用途 |
|----------|-------|---------|
| health/ | 3 | 健康检查 |
| logging/ | 1 | 日志配置 |
| 核心配置 | 23 | 框架配置 |
| **总计** | **27** | - |

## 开发规范

1. **配置类规范**：使用`@Configuration`注解，方法使用`@Bean`注解
2. **属性配置**：使用`@Value`或`@ConfigurationProperties`注入配置
3. **安全配置**：敏感配置应使用环境变量或配置中心
4. **切面规范**：使用`@Aspect`注解，定义清晰的切点
5. **过滤器规范**：实现`Filter`接口，使用`@Component`注解

### 配置类模板

```java
@Configuration
@EnableConfigurationProperties(MyProperties.class)
public class MyConfig {
    
    @Bean
    public MyBean myBean(MyProperties properties) {
        return new MyBean(properties);
    }
}

@ConfigurationProperties(prefix = "app.my")
public class MyProperties {
    private String property1;
    private int property2 = 10;
    // getters and setters
}
```

### 切面类模板

```java
@Aspect
@Component
@Slf4j
public class MyAspect {
    
    @Pointcut("@annotation(com.dongmedicine.config.MyAnnotation)")
    public void myPointcut() {}
    
    @Around("myPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = point.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("执行耗时: {}ms", duration);
        }
    }
}
```

---

## 性能优化建议

### 1. JWT令牌优化

```yaml
app:
  security:
    jwt-expiration: 86400000  # 24小时
    jwt-refresh-threshold-minutes: 30  # 过期前30分钟自动刷新
    jwt-refresh-grace-days: 7  # 刷新宽限期
```

**优化建议**：
- 令牌过期时间不宜过长（建议24小时内）
- 启用自动刷新机制，减少用户重新登录
- 使用Redis缓存已撤销的令牌

### 2. 缓存策略优化

| 缓存区域 | 默认TTL | 优化建议 |
|---------|--------|---------|
| plants | 6小时 | 热门数据可延长至12小时 |
| knowledges | 6小时 | 高频访问延长至24小时 |
| users | 30分钟 | 敏感数据保持短TTL |
| searchResults | 5分钟 | 根据访问频率调整 |

**缓存预热建议**：
```java
@PostConstruct
public void warmUpCache() {
    // 应用启动时预热热门数据
    plantService.getHotPlants();
    knowledgeService.getHotKnowledges();
}
```

### 3. 限流配置优化

```java
// 登录接口：严格限流
@RateLimit(value = 5, key = "login")

// 查询接口：适度限流
@RateLimit(value = 50, key = "query")

// 上传接口：宽松限流
@RateLimit(value = 10, key = "upload")
```

### 4. 异步任务优化

```yaml
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 20
        queue-capacity: 100
        thread-name-prefix: async-
```

### 5. 数据库连接池优化

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 30000
      max-lifetime: 1800000
```

---

## 已知限制

| 配置项 | 限制 | 影响 |
|--------|------|------|
| JwtUtil | 令牌刷新依赖Redis | Redis不可用时无法刷新 |
| RateLimitAspect | 本地令牌桶容量100 | 高并发时可能不够 |
| XssFilter | 不处理文件上传 | 文件内容需单独处理 |
| CacheConfig | 内存缓存无过期 | 长时间运行可能OOM |
| RequestSizeFilter | 默认限制10MB | 大文件需单独处理 |
| SecurityConfig | 不支持OAuth2 | 第三方登录需扩展 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **安全增强**
   - 添加IP白名单功能
   - 实现请求签名验证
   - 添加设备指纹识别

2. **限流优化**
   - 支持分布式限流
   - 添加限流统计面板
   - 实现动态限流配置

3. **缓存优化**
   - 实现多级缓存
   - 添加缓存监控
   - 支持缓存预热

### 中期改进 (1-2月)

1. **OAuth2支持**
   - 集成第三方登录
   - 实现单点登录
   - 支持多因素认证

2. **可观测性**
   - 集成分布式追踪
   - 添加性能监控
   - 实现告警机制

3. **配置管理**
   - 支持动态配置
   - 集成配置中心
   - 实现配置版本管理

### 长期规划 (3-6月)

1. **微服务架构**
   - 服务拆分
   - 网关配置
   - 服务发现

2. **云原生支持**
   - Kubernetes配置
   - 容器化部署
   - 自动扩缩容

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2+ | 框架基础 |
| Spring Security | 6.2+ | 安全框架 |
| Spring Data Redis | 3.2+ | 缓存 |
| SpringDoc OpenAPI | 2.3+ | API文档 |
| MyBatis-Plus | 3.5+ | ORM框架 |
| JJWT | 0.12+ | JWT处理 |
| Bucket4j | 8.x | 本地限流 |

---

## 常见问题

### 1. JWT令牌过期如何处理？

```java
// 前端处理
if (response.code === 401) {
    // 清除本地token
    localStorage.removeItem('token')
    // 跳转登录页
    router.push('/login')
}

// 后端处理
// JwtAuthenticationFilter会自动刷新即将过期的令牌
// 响应头中会返回新令牌：X-New-Token
```

### 2. 如何自定义限流策略？

```java
// 创建自定义限流注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomRateLimit {
    int value() default 10;
    String key() default "";
    TimeUnit unit() default TimeUnit.SECONDS;
}

// 在切面中处理
@Around("@annotation(customRateLimit)")
public Object around(ProceedingJoinPoint point, CustomRateLimit customRateLimit) {
    // 自定义限流逻辑
}
```

### 3. 如何添加新的缓存区域？

```java
// 在CacheConfig中添加
@Bean
public CacheManager cacheManager(RedisConnectionFactory factory) {
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
    
    // 添加新的缓存区域
    cacheConfigurations.put("myCache", 
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)));
    
    return RedisCacheManager.builder(factory)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
}
```

### 4. 如何配置CORS？

```java
// 在SecurityConfig中配置
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 5. 如何添加自定义健康检查？

```java
@Component
public class MyHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // 检查逻辑
        boolean healthy = checkMyService();
        if (healthy) {
            return Health.up()
                .withDetail("service", "my-service")
                .build();
        }
        return Health.down()
            .withDetail("error", "Service unavailable")
            .build();
    }
}
```

### 6. 如何禁用某些安全检查？

```java
// 仅用于开发环境
@Configuration
@Profile("dev")
public class DevSecurityConfig {
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers("/h2-console/**");
    }
}
```

---

**最后更新时间**：2026年3月30日

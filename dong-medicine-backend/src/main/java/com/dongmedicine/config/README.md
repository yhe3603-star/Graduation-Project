# Config 层 -- 配置类（20+个）

> 配置类是Spring Boot应用的基础设施，决定认证怎么工作、缓存怎么管理、限流怎么控制。
> 使用 `@Configuration` + `@Bean` 模式，由Spring容器管理生命周期。

---

## 一、配置类清单

```
config/
├── health/                          # 健康检查
│   ├── CacheHealthIndicator.java    #   缓存健康指标（Actuator）
│   ├── DatabaseHealthIndicator.java #   数据库健康指标
│   └── RedisHealthIndicator.java    #   Redis健康指标
├── logging/                         # 日志配置
│   └── SensitiveDataConverter.java  #   日志敏感数据脱敏
├── AdminDataInitializer.java        # 管理员账号自动初始化（CommandLineRunner）
├── AppProperties.java               # 自定义配置属性（@ConfigurationProperties "app"）
├── AsyncConfig.java                 # 异步线程池（viewCountExecutor + popularityExecutor）
├── CacheConfig.java                 # 双层缓存（Redis + Caffeine降级）
├── DeepSeekConfig.java              # DeepSeek AI配置（apiKey/baseUrl/model）
├── FileUploadProperties.java        # 文件上传属性
├── LoggingAspect.java               # 请求日志AOP（方法入参/出参/耗时）
├── MyMetaObjectHandler.java         # MyBatis-Plus自动填充（createdAt/updatedAt）
├── MybatisPlusConfig.java           # MyBatis-Plus分页插件
├── OpenApiConfig.java               # Swagger/OpenAPI文档配置
├── OperationLogAspect.java          # 操作审计AOP（自动记录Controller写操作日志）
├── RabbitMQConfig.java              # RabbitMQ配置（5个Exchange + 5个Queue + DLQ）
├── RateLimit.java                   # @RateLimit 自定义注解
├── RateLimitAspect.java             # 限流AOP（Redis Lua脚本 + 本地令牌桶降级）
├── RequestIdFilter.java             # 请求ID过滤器（16位UUID，放入MDC + 响应Header）
├── RequestSizeFilter.java           # 请求体大小限制过滤器
├── SaTokenConfig.java               # Sa-Token路由拦截 + CORS跨域配置
├── StpInterfaceImpl.java            # Sa-Token权限接口实现（角色查询）
├── SecurityConfigValidator.java     # 安全配置校验
├── StartupInfoPrinter.java          # 启动信息打印
├── WebMvcConfig.java                # 静态资源映射（images/videos/documents）
├── WebSocketConfig.java             # WebSocket端点注册 + 握手认证
└── XssFilter.java                   # XSS攻击防护过滤器
```

---

## 二、核心配置详解

### 2.1 SaTokenConfig -- 路由拦截 + CORS

文件：`config/SaTokenConfig.java`

```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final List<String> WRITE_METHODS = List.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 检查请求方法
            String method = getRequestMethod();
            if (WRITE_METHODS.contains(method)) {
                StpUtil.checkLogin();  // 写操作必须登录
            }
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            // 公开接口白名单（50+个路径）
            "/api/user/login", "/api/user/register",
            "/api/plants/list", "/api/plants/search", "/api/plants/{id}",
            // ... 更多公开路径
            "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Authorization", "Cache-Control", "Content-Type", ...)
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

**拦截规则**：
- GET请求：一律放行（通过 `excludePathPatterns` 白名单）
- POST/PUT/DELETE/PATCH：默认需要登录（通过 `SaInterceptor` 检查）
- 特殊情况：登录、注册等POST接口在 `excludePathPatterns` 中白名单放行

### 2.2 StpInterfaceImpl -- 角色权限实现

文件：`config/StpInterfaceImpl.java`

```java
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();  // 本项目不使用细粒度权限
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从数据库查询用户角色，供 @SaCheckRole("admin") 验证
        User user = userService.getUserInfo(Integer.parseInt(loginId.toString()));
        if (user != null && user.getRole() != null) {
            return List.of(user.getRole());
        }
        return new ArrayList<>();
    }
}
```

### 2.3 CacheConfig -- 双层缓存

文件：`config/CacheConfig.java`

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            // 尝试连接Redis
            connectionFactory.getConnection().ping();
            return createRedisCacheManager(connectionFactory);  // Redis缓存
        } catch (Exception e) {
            log.warn("Redis连接失败，降级到内存缓存");
            return createFallbackCacheManager();  // Caffeine本地缓存
        }
    }

    // Redis缓存TTL配置
    private RedisCacheManager createRedisCacheManager(...) {
        cacheConfigurations.put("plants",        defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("knowledges",    defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("inheritors",    defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("resources",     defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("users",         defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("quizQuestions", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("searchResults", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("hotData",       defaultConfig.entryTtl(Duration.ofHours(1)));
        // ...
    }

    // Redis降级：Caffeine本地缓存（maxSize=1000, expire=60min）
    private CacheManager createFallbackCacheManager() { ... }

    // Redis恢复检测：每60秒ping一次
    private void scheduleRedisRecoveryCheck(RedisConnectionFactory factory) { ... }
}
```

**RedisTemplate配置**：
```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
    // ...
}
```

### 2.4 RateLimitAspect -- API限流

文件：`config/RateLimitAspect.java`

```java
@Aspect
@Component
public class RateLimitAspect {
    private static final String RATE_LIMIT_LUA_SCRIPT =
        "local current = redis.call('INCR', KEYS[1]) " +
        "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
        "return current";

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = generateKey(point, rateLimit);  // 类名.方法名:userId或IP

        try {
            // 优先使用Redis Lua脚本（原子操作）
            Long count = stringRedisTemplate.execute(
                new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, Long.class),
                List.of("rate_limit:" + key), String.valueOf(1));
            if (count != null && count > rateLimit.value()) {
                throw new BusinessException(ErrorCode.OPERATION_TOO_FREQUENT);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis不可用时降级到本地令牌桶
            LocalTokenBucket bucket = localBuckets.computeIfAbsent(key, ...);
            if (!bucket.tryAcquire()) {
                throw new BusinessException(ErrorCode.OPERATION_TOO_FREQUENT);
            }
        }
        return point.proceed();
    }
}
```

**@RateLimit 注解使用**：
```java
@RateLimit(value = 5, key = "user_login")    // 每秒5次
@RateLimit(value = 3, key = "user_register")  // 每秒3次
@RateLimit(value = 10)  // 默认每秒10次
```

**限流Key生成规则**：
- 已登录用户：`{Controller}.{method}:{userId}`
- 未登录用户：`{Controller}.{method}:{IP地址}`

### 2.5 OperationLogAspect -- 操作审计

文件：`config/OperationLogAspect.java`

```java
@Aspect
@Component
public class OperationLogAspect {

    // 切点：拦截所有Controller方法（排除登录和注册）
    @Pointcut("execution(* com.dongmedicine.controller.*Controller.*(..)) && " +
              "!execution(* com.dongmedicine.controller.UserController.login(..)) && " +
              "!execution(* com.dongmedicine.controller.UserController.register(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // GET请求跳过审计
        if ("GET".equalsIgnoreCase(getMethod())) return point.proceed();

        long start = System.currentTimeMillis();
        try {
            return point.proceed();
        } finally {
            // 异步通过RabbitMQ保存操作日志
            saveLog(point, System.currentTimeMillis() - start, success, errorMsg);
        }
    }
}
```

**模块识别**：根据Controller类名自动识别模块（User/Plant/Knowledge/Inheritor/Resource/Qa/Feedback/Comment/Favorite）。

**操作类型**：根据HTTP方法自动归类（POST→CREATE, PUT/PATCH→UPDATE, DELETE→DELETE, GET→QUERY）。

### 2.6 LoggingAspect -- 请求日志

文件：`config/LoggingAspect.java`

```java
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* com.dongmedicine.controller.*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 生成traceId → 记录请求信息(IP/用户/方法/参数) → 执行 → 记录响应(耗时)
        // 敏感参数(password, token等)自动脱敏: "***MASKED***"
        // 超1秒的请求标为SLOW
    }
}
```

### 2.7 XssFilter -- XSS防护

文件：`config/XssFilter.java`

```java
@Component
public class XssFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String contentType = httpRequest.getContentType();

        if (contentType.contains("application/json")) {
            // JSON请求体：递归遍历所有文本节点，清洗XSS
            chain.doFilter(new XssJsonRequestWrapper(httpRequest), response);
        } else {
            // 表单参数：逐个清洗参数值
            chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
        }
    }
}
```

- 检测30+种危险模式（`<script>`, `javascript:`, `onerror=`, `<iframe>`, `eval()` 等）
- 发现危险内容时转义HTML特殊字符（`<` → `&lt;`, `>` → `&gt;` 等）
- 不使用黑名单模式拒绝请求，而是"清洗"危险内容后放行

### 2.8 AdminDataInitializer -- 管理员初始化

文件：`config/AdminDataInitializer.java`

```java
@Component
public class AdminDataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 检查admin用户是否存在
        // 不存在 → 从环境变量 ADMIN_INIT_PASSWORD 读取密码
        // BCrypt加密 → 创建admin用户（role=admin, status=active）
        // 环境变量未设置 → 跳过创建，记录warn日志
    }
}
```

### 2.9 AsyncConfig -- 异步线程池

文件：`config/AsyncConfig.java`

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("viewCountExecutor")
    public Executor viewCountExecutor() {
        // corePoolSize=2, maxPoolSize=5, queueCapacity=100
        // 拒绝策略：静默丢弃（不影响主流程）
    }

    @Bean("popularityExecutor")
    public Executor popularityExecutor() {
        // corePoolSize=1, maxPoolSize=3, queueCapacity=50
    }
}
```

---

## 三、RabbitMQ 配置

文件：`config/RabbitMQConfig.java`

详见 [mq/README.md](../mq/README.md)，核心内容：

- **5个Exchange**：1个Direct(dong.medicine.direct) + 1个Topic(dong.medicine.topic) + 1个DLX
- **5个Queue**：operation.log, feedback, file.process, statistics, notification
- **5个DLQ**：对应每个Queue的死信队列
- **Jackson2JsonMessageConverter**：JSON序列化
- **ConfirmCallback + ReturnsCallback**：消息发送确认
- **指数退避重试**：初始1秒，倍数2.0，最大10秒，最多3次
- **条件启用**：`@ConditionalOnProperty(app.rabbitmq.enabled)`

---

## 四、WebMvcConfig -- 静态资源映射

文件：`config/WebMvcConfig.java`

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/**")    → "file:{basePath}/images/"
    registry.addResourceHandler("/videos/**")    → "file:{basePath}/videos/"
    registry.addResourceHandler("/documents/**") → "file:{basePath}/documents/"
    registry.addResourceHandler("/public/**")    → "file:{basePath}/"
}
```

---

## 五、其他配置

### RequestIdFilter

为每个HTTP请求生成16位UUID作为requestId，放入MDC（日志追踪）和响应Header（`X-Request-ID`）。

### MyMetaObjectHandler

实现MyBatis-Plus的 `MetaObjectHandler`，自动填充Entity的 `createdAt` 和 `updatedAt` 时间字段。

### OpenApiConfig

生成Swagger/OpenAPI 3.0文档，访问地址：`http://localhost:8080/swagger-ui.html`

### DeepSeekConfig

配置DeepSeek AI的API密钥、基础URL、模型名称、连接/读取超时时间。

### StartupInfoPrinter

应用启动完成后打印访问地址、API文档地址等信息。

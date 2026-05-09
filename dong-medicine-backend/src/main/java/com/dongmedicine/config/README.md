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
├── AsyncConfig.java                 # 异步线程池（viewCountExecutor）
├── CacheConfig.java                 # 双层缓存（Caffeine L1 + Redis L2，自动降级）
├── DeepSeekConfig.java              # DeepSeek AI配置（apiKey/baseUrl/model + RestTemplate）
├── FileUploadProperties.java        # 文件上传属性（大小限制/扩展名白名单）
├── LoggingAspect.java               # 请求日志AOP（方法入参/出参/耗时/traceId）
├── MyMetaObjectHandler.java         # MyBatis-Plus自动填充（createdAt/updatedAt）
├── MybatisPlusConfig.java           # MyBatis-Plus分页插件（MySQL方言）
├── OpenApiConfig.java               # Swagger/OpenAPI文档配置（JWT认证说明）
├── OperationLogAspect.java          # 操作审计AOP（自动记录Controller写操作日志）
├── RabbitMQConfig.java              # RabbitMQ配置（3个Exchange + 5个Queue + 5个DLQ）
├── RateLimit.java                   # @RateLimit 自定义注解
├── RateLimitAspect.java             # 限流AOP（Redis Lua脚本 + 本地令牌桶降级）
├── RequestIdFilter.java             # 请求ID过滤器（16位UUID，放入MDC + 响应Header）
├── RequestSizeFilter.java           # 请求体大小限制过滤器（默认10MB）
├── SaTokenConfig.java               # Sa-Token路由拦截 + CORS跨域配置
├── StpInterfaceImpl.java            # Sa-Token权限接口实现（角色查询 + 5分钟本地缓存）
├── SecurityConfigValidator.java     # 安全配置校验（JWT密钥长度 >= 32字符）
├── StartupInfoPrinter.java          # 启动信息打印（ASCII艺术 + 端口/Profile/Swagger地址）
├── WebMvcConfig.java                # 静态资源映射 + URL解码配置
├── WebSocketConfig.java             # WebSocket端点注册 + 握手认证
└── XssFilter.java                   # XSS攻击防护过滤器（JSON/表单双模式）
```

---

## 二、核心配置详解

### 2.1 SaTokenConfig -- 路由拦截 + CORS

文件：`config/SaTokenConfig.java`

```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    private static final List<String> WRITE_METHODS = List.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            String method = request.getMethod().toUpperCase();
            if (WRITE_METHODS.contains(method)) {
                StpUtil.checkLogin();  // 写操作必须登录
            }
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            // 认证相关（无需登录）
            "/api/auth/**", "/api/user/login", "/api/user/register",
            "/api/user/validate", "/api/user/refresh-token", "/api/captcha/**",
            // 药用植物-只读接口
            "/api/plants/list", "/api/plants/search", "/api/plants/random",
            "/api/plants/{id}", "/api/plants/{id}/similar", "/api/plants/{id}/view",
            "/api/plants/batch",
            // 知识库-只读接口
            "/api/knowledge/list", "/api/knowledge/search", "/api/knowledge/{id}",
            "/api/knowledge/{id}/view",
            // 传承人-只读接口
            "/api/inheritors/list", "/api/inheritors/search", "/api/inheritors/{id}",
            "/api/inheritors/{id}/view",
            // 问答-只读接口
            "/api/qa/list", "/api/qa/search", "/api/qa/{id}/view",
            // 测验接口（允许未登录答题）
            "/api/quiz/questions", "/api/quiz/list", "/api/quiz/records", "/api/quiz/submit",
            // 学习资源-只读接口
            "/api/resources/list", "/api/resources/search", "/api/resources/hot",
            "/api/resources/categories", "/api/resources/types", "/api/resources/{id}",
            "/api/resources/download/{id}", "/api/resources/{id}/view",
            // 评论/排行榜/反馈-只读接口
            "/api/comments/list/**", "/api/leaderboard/**",
            "/api/feedback/stats", "/api/feedback",
            // 文档与元数据
            "/api/documents/**", "/api/metadata/**", "/api/stats/**",
            // 静态资源
            "/public/**", "/images/**", "/videos/**", "/favicon.ico", "/error"
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins.toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

**拦截规则**：
- GET请求：一律放行（通过 `excludePathPatterns` 白名单）
- POST/PUT/DELETE/PATCH：默认需要登录（通过 `SaInterceptor` 检查）
- 特殊情况：登录、注册等POST接口在 `excludePathPatterns` 中白名单放行

**CORS配置**：
- 允许的源从配置文件 `app.cors.allowed-origins` 读取，默认 `http://localhost:3000,http://localhost:5173`
- 允许所有请求头，支持Cookie传递，预检请求缓存3600秒

### 2.2 StpInterfaceImpl -- 角色权限实现

文件：`config/StpInterfaceImpl.java`

```java
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    private static final long ROLE_CACHE_TTL = 5 * 60 * 1000;  // 5分钟缓存
    private final ConcurrentHashMap<String, CacheEntry> roleCache = new ConcurrentHashMap<>();

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();  // 本项目不使用细粒度权限
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 先查本地缓存（5分钟TTL），避免频繁查库
        String key = loginId.toString();
        CacheEntry cached = roleCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.roles;
        }
        // 缓存未命中 → 查数据库 → 写入缓存
        User user = userService.getUserInfo(Integer.parseInt(key));
        List<String> roleList = (user != null && user.getRole() != null)
            ? List.of(user.getRole()) : new ArrayList<>();
        roleCache.put(key, new CacheEntry(roleList, System.currentTimeMillis() + ROLE_CACHE_TTL));
        return roleList;
    }
}
```

**设计要点**：
- 使用 `ConcurrentHashMap` + 自定义 `CacheEntry` 实现5分钟本地角色缓存，避免每次权限校验都查数据库
- 构造器注入 `UserService`，符合Spring推荐的最佳实践

### 2.3 CacheConfig -- 双层缓存

文件：`config/CacheConfig.java`

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.max-size:1000}")
    private int maxCacheSize;

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        CaffeineCacheManager caffeineManager = createCaffeineCacheManager();
        try {
            connectionFactory.getConnection().ping();
            log.info("Redis available — Caffeine(L1) + Redis(L2) two-level cache active");
            CacheManager redisManager = createRedisCacheManager(connectionFactory);
            return new TwoLevelCacheManager(caffeineManager, redisManager);  // 双层缓存
        } catch (Exception e) {
            log.warn("Redis unavailable — Caffeine-only cache active");
            return caffeineManager;  // 降级为纯内存缓存
        }
    }

    // Caffeine L1 缓存配置
    private CaffeineCacheManager createCaffeineCacheManager() {
        // maxSize=1000, expireAfterWrite=10min, expireAfterAccess=30min, recordStats
    }

    // Redis L2 缓存TTL配置
    private CacheManager createRedisCacheManager(...) {
        cacheConfigurations.put("plants",        defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("knowledges",    defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("inheritors",    defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("resources",     defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("users",         defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("quizQuestions", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("searchResults", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("hotData",       defaultConfig.entryTtl(Duration.ofHours(1)));
    }
}
```

**TwoLevelCacheManager 双层缓存架构**：
- **L1 Caffeine（本地内存）**：读取最快，写入时同步更新
- **L2 Redis（分布式）**：跨实例共享，持久化存储
- **读取流程**：先查L1 -> 未命中查L2 -> L2命中回填L1
- **写入流程**：同时写入L1和L2，保持双层数据一致
- **降级机制**：Redis不可用时自动降级为纯Caffeine缓存
- **NoOpCache**：当某一层缓存不存在时使用空实现，保证双层结构正常工作

**RedisTemplate配置**：
```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    // Key: StringRedisSerializer
    // Value: Jackson2JsonRedisSerializer（支持JavaTimeModule + 多态类型）
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

        // 定期清理过期的本地令牌桶，防止内存泄漏
        if (++checkCount % CLEANUP_INTERVAL == 0) cleanupStaleBuckets();

        if (redisAvailable && shouldTryRedis()) {
            try {
                // 优先使用Redis Lua脚本（原子操作）
                Long count = stringRedisTemplate.execute(...);
                if (count != null && count > rateLimit.value()) {
                    throw new BusinessException(ErrorCode.OPERATION_TOO_FREQUENT);
                }
                markRedisAvailable();
            } catch (BusinessException e) { throw e; }
              catch (Exception e) {
                // Redis不可用时降级到本地令牌桶
                markRedisUnavailable();
                useLocalFallback = true;
            }
        } else {
            useLocalFallback = true;
        }

        if (useLocalFallback) {
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

**Redis可用性检测**：
- 每次Redis操作失败后标记为不可用，30秒后自动重试
- 本地令牌桶每100次检查清理一次超过30分钟未访问的桶，防止内存泄漏

**LocalTokenBucket 本地令牌桶**：
- 容量等于限流值，补充速率1000ms/令牌
- 使用 `AtomicLong` + `compareAndSet` 实现无锁化令牌获取
- 支持余数时间片累积（`leftoverMs`），确保令牌补充精确

### 2.5 OperationLogAspect -- 操作审计

文件：`config/OperationLogAspect.java`

```java
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService logService;
    private final RabbitMQOperationLogService rabbitMQOperationLogService;  // 可选依赖
    private final ObjectMapper objectMapper;

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
            saveLog(point, System.currentTimeMillis() - start, success, errorMsg);
        }
    }

    private void saveLog(...) {
        // 优先通过RabbitMQ异步保存
        if (rabbitMQOperationLogService != null) {
            rabbitMQOperationLogService.saveLogAsync(operationLog);
        } else {
            logService.save(operationLog);  // RabbitMQ不可用时同步保存
        }
        // RabbitMQ发送失败也降级为同步保存
    }
}
```

**模块识别**：根据Controller类名自动识别模块（User/Plant/Knowledge/Inheritor/Resource/Qa/Feedback/Comment/Favorite）。

**操作类型**：根据HTTP方法自动归类（POST -> CREATE, PUT/PATCH -> UPDATE, DELETE -> DELETE, GET -> QUERY）。

**降级策略**：`RabbitMQOperationLogService` 使用 `@Autowired(required = false)` 注入，RabbitMQ未启用或发送失败时自动降级为同步数据库写入。

### 2.6 LoggingAspect -- 请求日志

文件：`config/LoggingAspect.java`

```java
@Aspect
@Component
public class LoggingAspect {
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
        "password", "passwordHash", "currentPassword", "newPassword",
        "confirmPassword", "token", "authorization", "secret"
    );

    @Around("execution(* com.dongmedicine.controller.*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String traceId = generateTraceId();  // 16位UUID
        MDC.put("traceId", traceId);
        // 记录请求信息(IP/用户/方法/参数) -> 执行 -> 记录响应(耗时)
        // 敏感参数(password, token等)自动脱敏: "***MASKED***"
        // MultipartFile参数只记录文件名和大小
        // 超1秒的请求标为SLOW
        // 响应体超1000字符自动截断
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

        if (contentType != null && contentType.contains("application/json")) {
            // JSON请求体：递归遍历所有文本节点（Object/Array/Text），清洗XSS
            chain.doFilter(new XssJsonRequestWrapper(httpRequest), response);
        } else {
            // 表单参数：逐个清洗参数值
            chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
        }
    }
}
```

- 检测30+种危险模式（`<script>`, `javascript:`, `onerror=`, `<iframe>`, `eval()` 等）
- 发现危险内容时转义HTML特殊字符（`<` -> `&lt;`, `>` -> `&gt;` 等）
- 不使用黑名单模式拒绝请求，而是"清洗"危险内容后放行
- JSON请求体使用 `XssJsonRequestWrapper` 缓存清洗后的body，支持重复读取

### 2.8 AdminDataInitializer -- 管理员初始化

文件：`config/AdminDataInitializer.java`

```java
@Component
public class AdminDataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 检查admin用户是否已存在
        User existing = userService.getOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (existing != null) return;

        // 从环境变量 ADMIN_INIT_PASSWORD 读取密码
        String initPassword = System.getenv("ADMIN_INIT_PASSWORD");
        if (initPassword == null || initPassword.isBlank()) {
            log.warn("未设置 ADMIN_INIT_PASSWORD 环境变量，跳过默认管理员创建");
            return;
        }
        // BCrypt加密 -> 创建admin用户（role=ROLE_ADMIN, status=STATUS_ACTIVE）
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(encoder.encode(initPassword));
        admin.setRole(RoleConstants.ROLE_ADMIN);
        admin.setStatus(User.STATUS_ACTIVE);
        userService.save(admin);
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
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("view-count-");
        executor.setRejectedExecutionHandler((r, e) -> {});  // 静默丢弃
        executor.initialize();
        return executor;
    }
}
```

### 2.10 SecurityConfigValidator -- 安全配置校验

文件：`config/SecurityConfigValidator.java`

```java
@Component
public class SecurityConfigValidator {
    private static final int MIN_JWT_SECRET_LENGTH = 32;

    @PostConstruct
    public void validateSecurityConfig() {
        // 优先检查 sa-token.jwt-secret-key，其次 app.security.jwt-secret
        // 未配置 -> 抛出 IllegalStateException 阻止应用启动
        // 长度不足32字符 -> 抛出 IllegalStateException
    }
}
```

**设计意图**：防止生产环境使用弱密钥或空密钥启动，在应用启动阶段就暴露安全问题。

### 2.11 FileUploadProperties -- 文件上传属性

文件：`config/FileUploadProperties.java`

```java
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    private String basePath = "public";
    private Long imageMaxSize = 10 * 1024 * 1024L;       // 10MB
    private Long videoMaxSize = 100 * 1024 * 1024L;      // 100MB
    private Long documentMaxSize = 50 * 1024 * 1024L;    // 50MB
    private String allowedImageExtensions = "jpg,jpeg,png,gif,bmp,webp";
    private String allowedVideoExtensions = "mp4,avi,mov,wmv,flv,mkv";
    private String allowedDocumentExtensions = "pdf,docx,doc,xlsx,xls,pptx,ppt,txt";

    // 扩展名自动归一化（小写、去空格、过滤空值）
    public List<String> getAllowedImageExtensionsList() { ... }
    public List<String> getAllowedVideoExtensionsList() { ... }
    public List<String> getAllowedDocumentExtensionsList() { ... }
}
```

### 2.12 RequestSizeFilter -- 请求体大小限制

文件：`config/RequestSizeFilter.java`

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestSizeFilter implements Filter {
    @Value("${app.request.max-body-size:10485760}")  // 默认10MB
    private long maxBodySize;

    @Override
    public void doFilter(...) {
        // 仅对 POST/PUT/PATCH + application/json 请求检查
        // Content-Length > maxBodySize -> 返回413错误
        // Content-Length未知 -> 使用ContentCachingRequestWrapper包装
    }
}
```

---

## 三、RabbitMQ 配置

文件：`config/RabbitMQConfig.java`

详见 [mq/README.md](../mq/README.md)，核心内容：

- **3个Exchange**：Direct(dong.medicine.direct) + Topic(dong.medicine.topic) + DLX(dong.medicine.dlx)
- **5个Queue**：operation.log, feedback, file.process, statistics, notification
- **5个DLQ**：对应每个Queue的死信队列
- **Jackson2JsonMessageConverter**：JSON序列化
- **ConfirmCallback + ReturnsCallback**：消息发送确认与路由失败回调
- **指数退避重试**：初始1秒，倍数2.0，最大10秒，最多3次
- **条件启用**：`@ConditionalOnProperty(app.rabbitmq.enabled)`，默认启用
- **所有队列启用Lazy模式**：消息尽量存磁盘，减少内存占用

---

## 四、WebMvcConfig -- 静态资源映射 + URL解码

文件：`config/WebMvcConfig.java`

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${file.upload.base-path:public}")
    private String uploadBasePath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 启用URL解码，设置UTF-8编码，支持中文路径
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(true);
        urlPathHelper.setDefaultEncoding("UTF-8");
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")    -> "file:{basePath}/images/"
        registry.addResourceHandler("/videos/**")    -> "file:{basePath}/videos/"
        registry.addResourceHandler("/documents/**") -> "file:{basePath}/documents/"
        registry.addResourceHandler("/public/**")    -> "file:{basePath}/"
    }
}
```

---

## 五、其他配置

### RequestIdFilter

为每个HTTP请求生成16位UUID作为requestId，放入MDC（日志追踪）和响应Header（`X-Request-ID`）。优先使用客户端传入的requestId，未传入则自动生成。执行优先级最高（`@Order(Ordered.HIGHEST_PRECEDENCE)`）。

### MyMetaObjectHandler

实现MyBatis-Plus的 `MetaObjectHandler`，自动填充Entity的时间字段：
- 插入时：填充 `createdAt` 和 `updatedAt`
- 更新时：填充 `updatedAt`

### OpenApiConfig

生成Swagger/OpenAPI 3.0文档，配置内容：
- API标题：侗乡医药数字展示平台 API
- 服务器列表：开发服务器(localhost) + 生产服务器
- JWT Bearer认证安全方案
- 全局安全要求：所有接口默认需要JWT认证

### DeepSeekConfig

配置DeepSeek AI的API密钥、基础URL、模型名称，并创建带超时设置的 `RestTemplate` Bean：
- 连接超时：默认10秒
- 读取超时：默认60秒

### StartupInfoPrinter

应用启动完成后（`ApplicationReadyEvent`），延迟500ms打印ASCII艺术启动信息，包含Profile、API地址、Swagger文档地址。

### AppProperties

自定义配置属性类，绑定 `app` 前缀，包含三个内部类：
- `Cors`：跨域允许源列表
- `Cache`：缓存开关和默认过期时间
- `Logging`：性能监控、请求日志、数据库监控开关

# 配置类目录 (config/)

> 类比：配置类就像**装修方案** -- 搬进新房子之前，你需要决定哪里放沙发、哪里装灯、门锁用什么类型。配置类就是在应用"搬进"服务器之前，告诉 Spring 框架"这个项目要怎么运行"。

## 什么是配置类？

在 Spring Boot 项目中，配置类是用 `@Configuration` 标注的特殊类。它们不是处理业务逻辑的，而是负责**搭建运行环境**的。就像开一家侗药铺，你得先把柜台、药柜、收银台布置好，才能开始卖药。

### 三个核心注解

| 注解 | 作用 | 类比 |
|------|------|------|
| `@Configuration` | 告诉 Spring "这个类是配置类" | 挂上"装修方案"的牌子 |
| `@Bean` | 在配置类中创建一个对象交给 Spring 管理 | 装修方案里写"这里放一个药柜" |
| `@Value` | 从配置文件中读取一个值 | 从设计图纸上读取尺寸数据 |

```java
@Configuration  // 标记这是配置类
public class CacheConfig {

    @Value("${app.cache.max-size:1000}")  // 从 application.yml 读取，默认1000
    private int maxCacheSize;

    @Bean  // 创建一个对象交给 Spring 管理
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // ... 创建缓存管理器
    }
}
```

---

## 目录结构

```
config/
├── health/                        # 健康检查配置（体检中心）
│   ├── CacheHealthIndicator.java  #   缓存健康指标
│   ├── DatabaseHealthIndicator.java # 数据库健康指标
│   └── RedisHealthIndicator.java  #   Redis健康指标
├── logging/                       # 日志配置（日记本）
│   └── SensitiveDataConverter.java # 日志敏感数据脱敏
├── AdminDataInitializer.java      # 管理员账号初始化
├── AppProperties.java             # 自定义配置属性
├── AsyncConfig.java               # 异步线程池配置
├── CacheConfig.java               # 多级缓存配置
├── DeepSeekConfig.java            # AI对话配置
├── FileUploadProperties.java      # 文件上传配置
├── LoggingAspect.java             # 请求日志切面
├── MyMetaObjectHandler.java       # MyBatis-Plus 自动填充
├── MybatisPlusConfig.java         # 数据库分页插件
├── OpenApiConfig.java             # API文档配置
├── OperationLogAspect.java        # 操作审计切面
├── RateLimit.java                 # 限流注解
├── RateLimitAspect.java           # 限流切面
├── RequestIdFilter.java           # 请求ID过滤器
├── RequestSizeFilter.java         # 请求大小限制
├── SaTokenConfig.java             # Sa-Token 路由拦截 + CORS
├── StpInterfaceImpl.java          # Sa-Token 权限认证接口
├── SecurityConfigValidator.java   # 安全配置校验
├── StartupInfoPrinter.java        # 启动信息打印
├── WebMvcConfig.java              # 静态资源映射
└── XssFilter.java                 # XSS防护过滤器
```

---

## 核心配置类详解

### 1. SaTokenConfig + StpInterfaceImpl -- 安全大门的守卫长

> 类比：SaTokenConfig 就像侗药铺的**保安队长**，决定谁能进哪个房间。StpInterfaceImpl 是保安队长的**花名册**，告诉他每个人是什么角色。

SaTokenConfig 配置了：
- **路由拦截规则**：哪些接口公开，哪些需要登录，哪些需要管理员
- **CORS 跨域**：允许前端（如 localhost:5173）访问后端

StpInterfaceImpl 实现了 Sa-Token 的权限接口：
- **getRoleList()**：返回用户角色列表（从数据库查询）
- **getPermissionList()**：返回用户权限列表（本项目暂未使用）

```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 可在此处添加全局路由拦截规则
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/user/login",       // 登录接口公开
            "/api/user/register",    // 注册接口公开
            "/api/plants/**",        // 植物数据公开浏览
            "/api/knowledge/**",     // 知识数据公开浏览
            // ... 更多公开接口
        );
    }
}

@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 根据 userId 查询角色
        User user = userService.getUserInfo((Integer) loginId);
        if (user != null && user.getRole() != null) {
            return List.of(user.getRole());
        }
        return new ArrayList<>();
    }
}
```

**Sa-Token 权限注解（在 Controller 上使用）：**

| 注解 | 作用 | 示例 |
|------|------|------|
| `@SaCheckLogin` | 验证是否登录 | `@SaCheckLogin` 加在需要登录的接口上 |
| `@SaCheckRole("admin")` | 验证角色 | `@SaCheckRole("admin")` 加在管理员接口上 |
| `@SaCheckPermission("user:add")` | 验证权限 | 本项目暂未使用 |

**常见错误**：
- 忘记在 SaTokenConfig 中排除公开接口，导致未登录用户也无法访问
- StpInterfaceImpl 中查询数据库没有做缓存，每次请求都查库，性能差
- 把 `@SaCheckRole("admin")` 写成 `@SaCheckRole("ADMIN")`，角色名大小写不一致

---

### 2. Sa-Token 登录认证 -- 令牌工厂 + 门卫

> 类比：Sa-Token 就像一个**智能门禁系统**，既能发通行证（登录），也能随时作废通行证（踢人下线），还能查验通行证（鉴权）。

Sa-Token 整合了 JWT 模式，Token 格式为 JWT，同时具备会话管理能力：

```java
// 登录 -- 相当于"制作通行证"
StpUtil.login(userId);                    // 生成 JWT Token
StpUtil.getSession().set("username", username);  // 存储额外信息
StpUtil.getSession().set("role", role);
String token = StpUtil.getTokenValue();   // 获取生成的 Token

// 验证 -- 相当于"查验通行证"
StpUtil.isLogin();                        // 是否已登录
StpUtil.getLoginIdAsInt();                // 获取当前用户ID

// 踢人下线 -- 相当于"作废通行证"
StpUtil.kickout(userId);                  // 一行代码踢人下线

// 退出登录
StpUtil.logout();                         // 清除当前会话
```

**Sa-Token 相比纯 JWT 的优势：**

| 功能 | 纯 JWT | Sa-Token + JWT |
|------|--------|----------------|
| 踢人下线 | 需手动实现黑名单 | ✅ `StpUtil.kickout()` |
| 同端互斥登录 | ❌ 不支持 | ✅ 配置 `is-concurrent: false` |
| Token 自动续期 | 需手动实现 | ✅ `StpUtil.renewTimeout()` |
| 会话信息存储 | ❌ 无 | ✅ `StpUtil.getSession()` |
| 权限注解 | 需配合 Spring Security | ✅ `@SaCheckRole("admin")` |

**常见错误**：
- 在 Token 中存储敏感信息（如密码）-- JWT 只是 Base64 编码，不是加密
- 密钥太短（如 "123456"）-- 容易被暴力破解，生产环境必须 >= 64 字符
- 忘记在登录时设置 Session 信息，后续取不到用户名和角色

---

### 3. MyMetaObjectHandler -- 自动填充时间字段

> 类比：就像侗药铺的**自动盖章机**，每份新文件进来自动盖上"创建时间"章，修改时自动盖上"更新时间"章。

MyBatis-Plus 的自动填充功能，在插入和更新数据时自动设置时间字段，不需要手动写 `setCreatedAt(LocalDateTime.now())`。

```java
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

**实体类中使用 `@TableField` 注解标记自动填充字段：**

```java
@TableField(fill = FieldFill.INSERT)       // 仅插入时填充
private LocalDateTime createdAt;

@TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都填充
private LocalDateTime updatedAt;
```

**`strictInsertFill` 的特点**：只在字段为 `null` 时才填充，如果手动设置了值，会保留你的值。

---

### 4. CacheConfig -- 双层保险柜

> 类比：CacheConfig 就像药铺的**双层药柜** -- 外层是柜台上的常用药（Caffeine内存缓存，快但容量小），内层是仓库（Redis，慢但容量大）。柜台没药了去仓库拿，仓库也没了才去药田采（查数据库）。

```java
@Configuration
@EnableCaching  // 开启缓存功能
public class CacheConfig {

    @Bean
    @Primary  // 默认使用这个缓存管理器
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            // 尝试连接Redis
            connectionFactory.getConnection().ping();
            log.info("Redis连接成功，使用Redis缓存");
            return createRedisCacheManager(connectionFactory);
        } catch (Exception e) {
            // Redis挂了？没关系，降级到内存缓存
            log.warn("Redis连接失败，降级到内存缓存: {}", e.getMessage());
            return createFallbackCacheManager();
        }
    }
}
```

**缓存TTL（存活时间）配置表**：

| 缓存名 | Redis TTL | 说明 | 类比 |
|--------|-----------|------|------|
| plants | 6小时 | 药用植物数据，变更少 | 常用药材，半天补一次货 |
| knowledges | 6小时 | 知识条目，变更少 | 同上 |
| inheritors | 6小时 | 传承人信息，变更少 | 同上 |
| resources | 4小时 | 资源数据 | 较常更新的药材 |
| users | 30分钟 | 用户数据，变更较频繁 | 热门药材，频繁补货 |
| quizQuestions | 12小时 | 测验题目，很稳定 | 珍藏药方，很少变 |
| searchResults | 5分钟 | 搜索结果，时效性短 | 时令药材，很快就换 |
| hotData | 1小时 | 热门数据 | 当日推荐 |

**常见错误**：
- 缓存时间设置太长，数据更新后用户看不到变化
- 缓存时间设置太短，起不到缓存效果，数据库压力大
- 忘记在更新数据时清除缓存，导致数据不一致

---

### 5. RateLimitAspect + RateLimit -- 客流控制

> 类比：RateLimitAspect 就像药铺门口的**客流控制器**，防止一下子涌进太多人把店挤爆。

```java
// 使用方式：在Controller方法上加注解
@RateLimit(value = 5)  // 每秒最多5次请求
@PostMapping("/api/feedback")
public R<?> submitFeedback(@RequestBody FeedbackDTO dto) {
    // ...
}
```

**限流流程**：

```
请求到达 @RateLimit 注解的方法
  |
  v
生成限流Key（方法名 + 用户名 或 IP地址）
  |
  v
Redis 可用吗？
  |-- 是 --> Redis INCR 计数 + EXPIRE 设置1秒过期
  |          计数超过限制？--> 抛出 "操作过于频繁" 异常
  |-- 否 --> 降级到本地令牌桶（LocalTokenBucket）
             桶里没令牌了？--> 抛出 "操作过于频繁" 异常
  |
  v
通过限流检查，继续执行业务逻辑
```

**常见错误**：
- `value` 设太小（如1），正常用户也会被限流
- 忘记考虑集群环境：本地令牌桶只能限单机，多台服务器各限各的
- 限流Key设计不合理，导致不同用户共享限额

---

### 6. XssFilter -- 危险品检测仪

> 类比：XssFilter 就像药铺入口的**安检仪**，检查每个人带进来的东西有没有危险品（恶意脚本）。

XSS（跨站脚本攻击）是黑客在输入框中注入恶意 JavaScript 代码的攻击方式。比如有人在评论框里输入 `<script>alert('hack')</script>`，如果不过滤，其他用户看到这条评论时就会执行这段脚本。

XssFilter 处理两种请求：

```java
@Component
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();

        if (contentType != null && contentType.contains("application/json")) {
            // JSON请求体 --> 递归遍历JSON所有文本节点，清洗危险内容
            chain.doFilter(new XssJsonRequestWrapper(httpRequest), response);
        } else {
            // 表单参数 --> 逐个清洗参数值
            chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
        }
    }
}
```

**XSS清洗过程**：

```
输入: "<script>alert('hack')</script>你好"
  |
  v  检测到XSS模式
清洗: "&lt;script&gt;alert('hack')&lt;/script&gt;你好"
  |
  v  特殊字符被转义，浏览器不会执行脚本
```

**常见错误**：
- 只过滤表单参数，忘记过滤 JSON 请求体
- 过滤后忘记缓存请求体，导致 Controller 中 `@RequestBody` 读不到数据
- 过于激进的过滤，把正常内容也改了（如用户输入 `<` 作为数学符号）

---

## JWT 认证完整流程图

```
                          JWT 认证流程
                          ============

  前端                          后端
  ----                          ----
                                |
  1. POST /api/user/login       |
     {username, password}  ----> |
                                | 2. 验证用户名密码
                                | 3. 生成 JWT Token
                                |    (包含 userId, role, 过期时间)
  4. 收到 Token           <---- |    返回 {token: "eyJhbG..."}
                                |
  5. 后续请求带上 Token         |
     Authorization: Bearer eyJhbG...  ----> |
                                | 6. JwtAuthenticationFilter 拦截
                                |    - 提取 Token
                                |    - 检查黑名单
                                |    - 解析验证签名
                                |    - 检查用户状态
                                |    - 设置 SecurityContext
                                |
                                | 7. 到达 Controller 处理业务
                                |
  8. 收到响应数据          <---- |
                                |
  ---- Token快过期时 ----       |
                                |
  9. POST /api/user/refresh-token
     Authorization: Bearer 旧Token  ----> |
                                | 10. canRefresh() 检查
                                |     (过期7天内仍可刷新)
                                | 11. 生成新 Token
  12. 收到新Token         <---- |
```

---

## 配置文件参考 (application.yml)

```yaml
app:
  security:
    jwt-secret: "你的密钥-生产环境至少64字符"  # JWT签名密钥
    jwt-expiration: 86400000                   # Token过期时间(毫秒)，默认24小时
    cors-allowed-origins:                      # 允许跨域的前端地址
      - http://localhost:5173
      - http://localhost:3000
  cache:
    enabled: true       # 是否启用缓存
    max-size: 1000      # 本地缓存最大条目数
    expire-minutes: 60  # 本地缓存默认过期时间(分钟)
```

---

## 常见问题

**Q: 为什么禁用 CSRF？**
A: CSRF（跨站请求伪造）是利用浏览器自动携带 Cookie 的特性发起攻击。我们使用 JWT 而不是 Cookie，所以不需要 CSRF 保护。

**Q: Redis 挂了系统会崩溃吗？**
A: 不会。CacheConfig 会自动检测 Redis 连接，失败时降级到 Caffeine 内存缓存。RateLimitAspect 也会降级到本地令牌桶。系统可以正常运行，只是缓存和限流效果会打折扣。

**Q: 为什么 JWT 过期了还能刷新？**
A: 设计了7天宽限期（`refreshGraceDays`）。Token 过期后7天内，用户可以用旧 Token 换新 Token，避免用户突然掉线需要重新登录。但超过7天就必须重新登录了。

---

## 代码审查与改进建议

- [严重-安全] SaTokenConfig中大量API路径绕过认证：/api/quiz/submit、/api/plant-game/submit等写操作免认证，攻击者可无限刷分；/api/chat免认证可被滥用消耗DeepSeek API额度
- [严重-安全] SaInterceptor的lambda处理器为空，整个认证体系仅依赖路径排除列表，没有实际权限检查逻辑
- [严重-安全] XssFilter对管理员路径(/api/admin/)完全跳过XSS过滤，攻击者可注入恶意脚本
- [严重-安全] XssFilter对所有HTTP Header都做XSS清洗，可能破坏Authorization头中的Bearer Token
- [严重-线程安全] RateLimitAspect中Redis的increment和expire不是原子操作，应用崩溃时key将永不过期
- [严重-线程安全] RateLimitAspect中localBuckets只有put没有remove，导致内存泄漏
- [中等-配置] AsyncConfig线程池拒绝策略静默丢弃任务，没有任何日志记录
- [中等-配置] CacheConfig同时设置expireAfterWrite和expireAfterAccess且值相同是冗余的
- [中等-安全] CacheConfig中Redis序列化使用LaissezFaireSubTypeValidator允许所有子类型反序列化，存在远程代码执行风险
- [中等-性能] RedisHealthIndicator每次健康检查创建两个Redis连接且未关闭
- [中等-错误] StpInterfaceImpl中获取用户角色时异常被完全忽略(// ignore)，应至少记录warn日志
- [中等-代码重复] getClientIp方法在LoggingAspect、OperationLogAspect、RateLimitAspect中重复实现3次
- [低-配置] OpenApiConfig中生产服务器IP硬编码在Swagger配置中
- [低-配置] SecurityConfigValidator中环境判断逻辑有误：!acceptsProfiles("dev")不等于isProduction

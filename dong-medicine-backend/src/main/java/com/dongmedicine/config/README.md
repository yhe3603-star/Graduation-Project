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

**功能**：实现接口限流功能。

**实现方式**：
- 使用Redis的increment操作实现计数器
- 基于滑动窗口算法
- 支持用户级别和IP级别限流
- Redis不可用时自动降级（不限流）

**限流Key生成规则**：
- 已登录用户：`方法名:用户名`
- 未登录用户：`方法名:IP地址`

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

---

**最后更新时间**：2026年3月25日

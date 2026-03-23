# 配置目录说明

## 文件夹结构

本目录包含项目的各种配置类，包括应用配置、安全配置、缓存配置等。

```
config/
├── health/         # 健康检查配置
├── logging/        # 日志配置
├── AdminDataInitializer.java    # 管理员数据初始化
├── AppProperties.java           # 应用属性配置
├── AsyncConfig.java             # 异步配置
├── CacheConfig.java             # 缓存配置
├── CustomUserDetails.java       # 自定义用户详情
├── DeepSeekConfig.java          # DeepSeek AI配置
├── FileUploadProperties.java    # 文件上传配置
├── JwtAuthenticationFilter.java # JWT认证过滤器
├── JwtSecretValidator.java      # JWT密钥验证器
├── JwtUtil.java                 # JWT工具类
├── LoggingAspect.java           # 日志切面
├── MybatisPlusConfig.java       # MyBatis Plus配置
├── OpenApiConfig.java           # OpenAPI配置
├── OperationLogAspect.java      # 操作日志切面
├── RateLimit.java               # 速率限制注解
├── RateLimitAspect.java         # 速率限制切面
├── RequestIdFilter.java         # 请求ID过滤器
├── RequestSizeFilter.java       # 请求大小过滤器
├── SecurityConfig.java          # 安全配置
├── SecurityConfigValidator.java # 安全配置验证器
├── StartupInfoPrinter.java      # 启动信息打印
├── WebMvcConfig.java            # Web MVC配置
└── XssFilter.java               # XSS过滤器
```

## 详细说明

### 1. health/ 目录

**用途**：健康检查配置。

**文件列表**：

- **CacheHealthIndicator.java**
  - 功能：缓存健康检查指示器
  - 检查内容：缓存连接状态

- **DatabaseHealthIndicator.java**
  - 功能：数据库健康检查指示器
  - 检查内容：数据库连接状态

### 2. logging/ 目录

**用途**：日志配置。

**文件列表**：

- **SensitiveDataConverter.java**
  - 功能：敏感数据转换器
  - 作用：在日志中脱敏敏感数据

### 3. AdminDataInitializer.java

**功能**：管理员数据初始化。

**主要功能**：
- 在应用启动时初始化管理员账号
- 检查管理员账号是否存在，不存在则创建
- 设置默认管理员密码

### 4. AppProperties.java

**功能**：应用属性配置。

**主要属性**：
- 应用基本信息
- 服务配置
- 第三方服务配置

### 5. AsyncConfig.java

**功能**：异步配置。

**主要配置**：
- 线程池配置
- 异步任务执行器

### 6. CacheConfig.java

**功能**：缓存配置。

**主要配置**：
- Redis缓存配置
- 缓存管理器
- 缓存键生成策略

### 7. CustomUserDetails.java

**功能**：自定义用户详情。

**主要功能**：
- 实现UserDetails接口
- 提供用户权限信息
- 支持JWT认证

### 8. DeepSeekConfig.java

**功能**：DeepSeek AI配置。

**主要配置**：
- API密钥配置
- 请求超时设置
- 模型参数配置

### 9. FileUploadProperties.java

**功能**：文件上传配置。

**主要配置**：
- 上传路径
- 文件大小限制
- 允许的文件类型
- 临时文件清理

### 10. JwtAuthenticationFilter.java

**功能**：JWT认证过滤器。

**主要功能**：
- 从请求头获取JWT令牌
- 验证JWT令牌
- 设置认证信息到安全上下文

### 11. JwtSecretValidator.java

**功能**：JWT密钥验证器。

**主要功能**：
- 验证JWT密钥的安全性
- 检查密钥长度和复杂度

### 12. JwtUtil.java

**功能**：JWT工具类。

**主要方法**：
- `generateToken`：生成JWT令牌
- `parseToken`：解析JWT令牌
- `validateToken`：验证JWT令牌
- `getUserIdFromToken`：从令牌中获取用户ID

### 13. LoggingAspect.java

**功能**：日志切面。

**主要功能**：
- 记录方法执行时间
- 记录方法参数和返回值
- 异常日志记录

### 14. MybatisPlusConfig.java

**功能**：MyBatis Plus配置。

**主要配置**：
- 分页插件
- 性能分析插件
- 乐观锁插件
- SQL注入器

### 15. OpenApiConfig.java

**功能**：OpenAPI配置。

**主要配置**：
- API文档标题和描述
- 版本信息
- 安全配置
- 路径分组

### 16. OperationLogAspect.java

**功能**：操作日志切面。

**主要功能**：
- 记录用户操作
- 记录操作类型、模块、IP地址等信息
- 持久化操作日志

### 17. RateLimit.java

**功能**：速率限制注解。

**主要属性**：
- `value`：限制次数
- `timeUnit`：时间单位
- `key`：限制键

### 18. RateLimitAspect.java

**功能**：速率限制切面。

**主要功能**：
- 实现接口速率限制
- 防止API滥用
- 返回429状态码（Too Many Requests）

### 19. RequestIdFilter.java

**功能**：请求ID过滤器。

**主要功能**：
- 为每个请求生成唯一ID
- 将请求ID添加到响应头
- 便于请求跟踪和日志关联

### 20. RequestSizeFilter.java

**功能**：请求大小过滤器。

**主要功能**：
- 限制请求体大小
- 防止大请求攻击
- 返回413状态码（Payload Too Large）

### 21. SecurityConfig.java

**功能**：安全配置。

**主要配置**：
- 认证管理器
- 授权规则
- 安全过滤器链
- 密码编码器

### 22. SecurityConfigValidator.java

**功能**：安全配置验证器。

**主要功能**：
- 验证安全配置的有效性
- 检查密钥配置
- 检查权限规则

### 23. StartupInfoPrinter.java

**功能**：启动信息打印。

**主要功能**：
- 在应用启动时打印服务信息
- 打印配置信息
- 打印健康状态

### 24. WebMvcConfig.java

**功能**：Web MVC配置。

**主要配置**：
- 拦截器配置
- 资源处理器
- 消息转换器
- 跨域配置

### 25. XssFilter.java

**功能**：XSS过滤器。

**主要功能**：
- 过滤请求参数中的XSS攻击代码
- 保护应用免受XSS攻击

## 配置文件

应用的配置文件位于 `src/main/resources` 目录下：

- **application.yml**：主配置文件
- **application-dev.yml**：开发环境配置
- **application-prod.yml**：生产环境配置
- **application.properties**：属性配置

## 环境变量

应用支持以下环境变量：

- `SPRING_PROFILES_ACTIVE`：激活的环境配置
- `JWT_SECRET`：JWT密钥
- `DATABASE_URL`：数据库URL
- `DATABASE_USERNAME`：数据库用户名
- `DATABASE_PASSWORD`：数据库密码
- `REDIS_URL`：Redis URL
- `DEEPSEEK_API_KEY`：DeepSeek API密钥

## 开发规范

1. **命名规范**：配置类名使用大驼峰命名法，以`Config`结尾
2. **配置管理**：配置项应该在配置类中集中管理
3. **环境分离**：不同环境的配置应该分离到不同的配置文件
4. **安全配置**：敏感配置应该使用环境变量或加密存储
5. **配置验证**：配置项应该有适当的验证

## 注意事项

- 配置类应该使用`@Configuration`注解
- 环境特定的配置应该放在对应的环境配置文件中
- 敏感配置不应该硬编码在代码中
- 配置变更应该考虑向后兼容性
- 定期检查配置的安全性和有效性

---

**最后更新时间**：2026年3月23日
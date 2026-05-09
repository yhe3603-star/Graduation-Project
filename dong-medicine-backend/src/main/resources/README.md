# Resources -- 配置资源目录

> 本目录存放Spring Boot应用的所有配置文件和数据库迁移脚本，是项目的"总控台"。
> 类比：药铺的"规章制度册" -- 营业时间、药材存储方式、员工权限、安全规范等全部写在这里，药铺按章运行。

---

## 一、目录结构

```
resources/
├── application.yml              # 主配置文件（通用配置 + 默认值）
├── application-dev.yml          # 开发环境配置（覆盖主配置）
├── application-prod.yml         # 生产环境配置（覆盖主配置）
├── application.properties       # 基础属性（仅应用名称）
├── logback-spring.xml           # 日志框架配置（按Profile区分输出策略）
└── db/
    └── migration/
        ├── V1__init_schema.sql  # Flyway数据库初始化脚本（14张表）
        └── V2__seed_data.sql    # Flyway种子数据脚本
```

---

## 二、配置文件加载机制

Spring Boot配置文件采用"主配置 + Profile覆盖"的分层机制：

```
┌──────────────────────────────────────────────────────────────────────┐
│                     配置加载优先级（由高到低）                          │
│                                                                      │
│  1. application-{profile}.yml   ← Profile特定配置（覆盖主配置）       │
│  2. application.yml             ← 主配置（通用默认值）                │
│  3. application.properties      ← 基础属性（仅spring.application.name）│
│  4. 环境变量 ${XXX}             ← 运行时覆盖（最高优先级）            │
└──────────────────────────────────────────────────────────────────────┘
```

**Profile切换方式**：

```bash
# 方式1：环境变量
export SPRING_PROFILES_ACTIVE=prod

# 方式2：JVM参数
java -jar app.jar -Dspring.profiles.active=prod

# 方式3：application.yml中默认值
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}   # 默认dev
```

---

## 三、各文件详解

### 3.1 application.yml -- 主配置文件

> 类比：药铺的"通用规章制度" -- 适用于所有分店的共同规定，各分店可在自己的补充规定中覆盖。

**核心职责**：定义所有环境的通用配置和默认值，Profile特定配置可覆盖。

**配置模块总览**：

| 模块 | 配置项 | 说明 |
|------|--------|------|
| 服务端口 | `server.port: 8080` | HTTP服务端口 |
| 字符编码 | `server.servlet.encoding: UTF-8` | 强制UTF-8编码 |
| 数据源 | `spring.datasource` | MySQL连接（支持环境变量覆盖） |
| Redis | `spring.data.redis` | Redis连接（localhost:6379） |
| RabbitMQ | `spring.rabbitmq` | 消息队列连接（localhost:5672） |
| Sa-Token | `sa-token` | 认证框架配置 |
| CORS | `app.cors.allowed-origins` | 跨域白名单 |
| 缓存 | `app.cache` | Redis缓存策略 |
| 日志 | `logging.level` | 日志级别 |
| 监控 | `management` | Actuator健康检查 |
| MyBatis-Plus | `mybatis-plus` | ORM配置 |

**环境变量覆盖**：

```yaml
# 所有敏感配置都支持环境变量覆盖，格式: ${ENV_VAR:defaultValue}
spring:
  datasource:
    username: ${DB_USERNAME:root}         # 默认root
    password: ${DB_PASSWORD:}             # 默认空
  data:
    redis:
      host: ${REDIS_HOST:localhost}       # 默认localhost
      password: ${REDIS_PASSWORD:}        # 默认空
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}      # 默认localhost
    username: ${RABBITMQ_USER:guest}      # 默认guest
    password: ${RABBITMQ_PASSWORD:guest}  # 默认guest

jwt:
  expiration: ${JWT_EXPIRATION:86400000}  # 默认24小时

sa-token:
  timeout: ${SA_TOKEN_TIMEOUT:2592000}    # 默认30天
  jwt-secret-key: ${JWT_SECRET:}          # 必须通过环境变量设置
```

**Sa-Token认证配置**：

```yaml
sa-token:
  token-name: Authorization     # Header中Token字段名
  timeout: 2592000              # Token有效期（30天，单位秒）
  active-timeout: -1            # 活跃超时（-1=不限制）
  is-concurrent: true           # 允许同一账号多端登录
  is-share: false               # 不共享Token
  token-style: uuid             # Token生成风格
  is-log: false                 # 不打印登录日志
  is-read-cookie: false         # 不从Cookie读取Token
  is-read-body: false           # 不从请求体读取Token
  is-read-header: true          # 从Header读取Token
  token-prefix: Bearer          # Token前缀（Bearer xxx）
  jwt-secret-key: ${JWT_SECRET:}  # JWT签名密钥
```

**RabbitMQ消费者配置**：

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        prefetch: 10      # 每个消费者预取10条消息（流控）
        retry:
          enabled: true   # 消费失败自动重试
```

**Actuator监控配置**：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info    # 仅暴露health和info端点
      base-path: /actuator
  endpoint:
    health:
      show-details: when_authorized   # 仅授权用户可见详情
      show-components: when_authorized
      probes:
        enabled: true                 # 启用K8s探针
```

---

### 3.2 application-dev.yml -- 开发环境配置

> 类比：药铺总店的"本地补充规定" -- 调试模式全开，日志详细，方便掌柜查看每个细节。

**与主配置的差异**：

| 配置项 | 主配置默认值 | dev覆盖值 | 原因 |
|--------|------------|----------|------|
| `spring.flyway.enabled` | 未设置 | `true` | 开发环境启用Flyway自动迁移 |
| `spring.datasource.hikari.maximum-pool-size` | 未设置 | `20` | 开发环境适中连接池 |
| `spring.data.redis.lettuce.pool` | 未设置 | 配置连接池 | 开发环境需要连接池 |
| `logging.level.com.dongmedicine` | `INFO` | `DEBUG` | 开发环境查看详细日志 |
| `logging.level.org.springframework.security` | 未设置 | `DEBUG` | 调试安全框架 |
| `mybatis-plus.configuration.log-impl` | `NoLoggingImpl` | `StdOutImpl` | 开发环境打印SQL语句 |
| `management.endpoint.health.show-details` | `when_authorized` | `always` | 开发环境始终显示健康详情 |

**开发环境特有配置**：

```yaml
# Flyway数据库迁移（仅dev启用）
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration

# HikariCP连接池
spring:
  datasource:
    hikari:
      maximum-pool-size: 20       # 最大连接数
      minimum-idle: 5             # 最小空闲连接
      idle-timeout: 300000        # 空闲超时5分钟
      connection-timeout: 30000   # 连接超时30秒
      max-lifetime: 1200000       # 连接最大生命周期20分钟
      connection-test-query: SELECT 1
      pool-name: DongMedicineHikariPool

# 文件上传限制（开发环境更宽松）
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

# Sa-Token JWT密钥（开发环境使用默认值）
sa-token:
  jwt-secret-key: dev-only-secret-key-please-change-in-production-32chars

# DeepSeek AI配置
deepseek:
  api-key: ${DEEPSEEK_API_KEY:}
  base-url: https://api.deepseek.com
  model: deepseek-chat
  connect-timeout: 10
  read-timeout: 60
```

**文件上传配置**：

```yaml
file:
  upload:
    base-path: ${user.dir}/public     # 上传根目录（项目运行目录/public）
    image-max-size: 10485760          # 图片最大10MB
    video-max-size: 104857600         # 视频最大100MB
    document-max-size: 52428800       # 文档最大50MB
    allowed-image-extensions: jpg,jpeg,png,gif,bmp,webp
    allowed-video-extensions: mp4,avi,mov,wmv,flv,mkv
    allowed-document-extensions: pdf,docx,doc,xlsx,xls,pptx,ppt,txt
```

---

### 3.3 application-prod.yml -- 生产环境配置

> 类比：药铺分店的"正式运营规定" -- 安全优先、性能优化、日志精简、监控严格。

**与主配置/dev的关键差异**：

| 配置项 | dev值 | prod值 | 原因 |
|--------|-------|--------|------|
| `spring.flyway.enabled` | `true` | `false` | 生产环境手动执行迁移，避免自动DDL |
| `spring.datasource.hikari.maximum-pool-size` | `20` | `15` | 生产环境更保守的连接池 |
| `spring.datasource.hikari.minimum-idle` | `5` | `2` | 减少空闲连接占用 |
| `spring.servlet.multipart.max-file-size` | `100MB` | `50MB` | 生产环境限制上传大小 |
| `spring.data.redis.lettuce.pool.max-wait` | `-1ms` | `5000ms` | 生产环境限制等待时间 |
| `spring.rabbitmq.publisher-confirm-type` | 未设置 | `correlated` | 生产环境启用发布确认 |
| `spring.rabbitmq.listener.simple.retry.max-attempts` | 未设置 | `3` | 生产环境限制重试次数 |
| `logging.level.com.dongmedicine` | `DEBUG` | `INFO` | 生产环境减少日志量 |
| `logging.level.cn.dev33.satoken` | 未设置 | `ERROR` | 生产环境仅记录Sa-Token错误 |
| `mybatis-plus.configuration.log-impl` | `StdOutImpl` | `NoLoggingImpl` | 生产环境不打印SQL |
| `management.endpoint.health.show-details` | `always` | `when_authorized` | 生产环境仅授权可见 |
| `sa-token.jwt-secret-key` | 默认值 | `${JWT_SECRET}` | 生产环境必须设置密钥 |
| `sa-token.timeout` | `2592000`(30天) | `604800`(7天) | 生产环境缩短Token有效期 |
| `springdoc.api-docs.enabled` | 未设置 | `false` | 生产环境关闭Swagger文档 |
| `file.upload.base-path` | `${user.dir}/public` | `/app/public` | 生产环境使用容器路径 |

**生产环境安全配置**：

```yaml
# Sa-Token（生产环境必须设置JWT密钥）
sa-token:
  jwt-secret-key: ${JWT_SECRET}    # 无默认值，必须通过环境变量设置
  timeout: 604800                   # Token有效期7天（比dev的30天更短）

# RabbitMQ（生产环境启用发布确认和重试）
spring:
  rabbitmq:
    username: ${RABBITMQ_USER:admin}        # 非默认guest
    password: ${RABBITMQ_PASSWORD}           # 无默认值，必须设置
    publisher-confirm-type: correlated       # 发布确认
    publisher-returns: true                  # 发布返回
    listener:
      simple:
        retry:
          max-attempts: 3                    # 最多重试3次
          initial-interval: 1000             # 初始间隔1秒
          multiplier: 2.0                    # 指数退避倍数

# 关闭Swagger文档
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

**生产环境日志配置**：

```yaml
logging:
  level:
    com.dongmedicine: INFO           # 业务日志INFO级别
    cn.dev33.satoken: ERROR          # Sa-Token仅记录ERROR
  file:
    name: /app/logs/dong-medicine.log  # 日志文件路径
  logback:
    rollingpolicy:
      max-file-size: 50MB            # 单文件最大50MB
      max-history: 30                # 保留30天
      total-size-cap: 1GB            # 总大小上限1GB
```

**生产环境健康检查**：

```yaml
management:
  endpoint:
    health:
      show-details: when_authorized   # 仅授权用户可见
      show-components: when_authorized
  health:
    db:
      enabled: true                   # 数据库健康检查
    redis:
      enabled: false                  # 关闭Redis健康检查（避免误报）
    rabbit:
      enabled: false                  # 关闭RabbitMQ健康检查
    diskspace:
      enabled: true                   # 磁盘空间检查
```

---

### 3.4 application.properties -- 基础属性

```properties
spring.application.name=dong-medicine-backend
```

仅定义应用名称，所有其他配置均使用YAML格式。`application.properties` 优先级高于 `application.yml`，但本项目仅在properties中定义应用名称，避免配置分散。

---

### 3.5 logback-spring.xml -- 日志框架配置

> 类比：药铺的"记录簿管理制度" -- 日常流水记在普通簿（CONSOLE），重要记录归档到正式簿（FILE），严重事故单独记在警示簿（ERROR_FILE）。

**核心职责**：定义日志输出格式、输出目标、滚动策略，按Spring Profile区分日志级别。

**3个Appender**：

| Appender | 类型 | 输出目标 | 说明 |
|----------|------|---------|------|
| `CONSOLE` | ConsoleAppender | 控制台 | 所有环境使用 |
| `FILE` | RollingFileAppender | `logs/dong-medicine.log` | 按日期+大小滚动，单文件50MB，保留30天，总上限1GB |
| `ERROR_FILE` | RollingFileAppender | `logs/dong-medicine-error.log` | 仅ERROR级别，单文件20MB，保留60天，总上限500MB |

**日志格式**：

```xml
<!-- 使用自定义 %masked 转换词，对敏感数据脱敏 -->
<property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{requestId}] [%thread] %-5level %logger{36} - %masked%n"/>
```

| 格式元素 | 说明 |
|---------|------|
| `%d{yyyy-MM-dd HH:mm:ss.SSS}` | 时间戳（精确到毫秒） |
| `[%X{requestId}]` | MDC请求ID（链路追踪） |
| `[%thread]` | 线程名 |
| `%-5level` | 日志级别（左对齐5字符） |
| `%logger{36}` | Logger名（最长36字符） |
| `%masked` | 自定义转换词，敏感数据脱敏 |

**敏感数据脱敏**：

```xml
<conversionRule conversionWord="masked" converterClass="com.dongmedicine.config.logging.SensitiveDataConverter"/>
```

`SensitiveDataConverter` 是项目自定义的日志转换器，自动对日志中的手机号、身份证号、邮箱等敏感信息进行脱敏处理。

**异步日志（ASYNC_FILE）**：

```xml
<appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE"/>
    <queueSize>512</queueSize>               <!-- 异步队列容量 -->
    <discardingThreshold>0</discardingThreshold>  <!-- 队列满时不丢弃日志 -->
</appender>
```

**按Profile区分日志策略**：

| Profile | 日志级别 | Appender | 说明 |
|---------|---------|----------|------|
| `dev` | `DEBUG` | CONSOLE | 开发环境：DEBUG级别 + 控制台输出 |
| `prod` | `INFO` | CONSOLE + ASYNC_FILE + ERROR_FILE | 生产环境：INFO级别 + 控制台 + 异步文件 + 错误文件 |
| 其他 | `INFO` | CONSOLE + FILE | 默认：INFO级别 + 控制台 + 同步文件 |

```xml
<!-- dev环境 -->
<springProfile name="dev">
    <root level="DEBUG">
        <appender-ref ref="CONSOLE"/>
    </root>
    <logger name="com.dongmedicine" level="DEBUG"/>
    <logger name="org.springframework.security" level="DEBUG"/>
</springProfile>

<!-- prod环境 -->
<springProfile name="prod">
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>      <!-- 异步写文件，不阻塞业务线程 -->
        <appender-ref ref="ERROR_FILE"/>      <!-- 错误日志单独归档 -->
    </root>
    <logger name="com.dongmedicine" level="INFO"/>
    <logger name="org.springframework" level="WARN"/>
    <logger name="org.hibernate" level="WARN"/>
</springProfile>
```

**日志文件滚动策略**：

```
logs/
├── dong-medicine.log                     ← 当前日志
├── dong-medicine.2025-05-08.0.log        ← 历史日志（按日期+序号）
├── dong-medicine.2025-05-07.0.log
├── dong-medicine-error.log               ← 当前错误日志
├── dong-medicine-error.2025-05-08.0.log  ← 历史错误日志
└── ...
```

---

### 3.6 db/migration/ -- 数据库迁移脚本

> 类比：药铺的"建店图纸" -- V1是初建时的完整图纸（14张表），V2是开业时的药材清单（种子数据）。

**Flyway迁移脚本**：

| 文件 | 版本 | 说明 |
|------|------|------|
| `V1__init_schema.sql` | V1 | 初始化14张数据库表 |
| `V2__seed_data.sql` | V2 | 种子数据（侗医药初始数据） |

**V1__init_schema.sql 包含的表**：

| 表名 | 说明 | 关键索引 |
|------|------|---------|
| `users` | 用户表 | `uk_username`（唯一） |
| `plants` | 植物药材表 | `ft_plants_search`（全文索引） |
| `knowledge` | 知识库表 | `ft_knowledge_search`（全文索引） |
| `inheritors` | 传承人表 | `ft_inheritors_search`（全文索引） |
| `qa` | 问答表 | `ft_qa_search`（全文索引） |
| `resources` | 学习资源表 | `ft_resources_search`（全文索引） |
| `comments` | 评论表 | `idx_target` + `idx_status` |
| `favorites` | 收藏表 | `uk_user_target`（唯一） |
| `feedback` | 反馈表 | `idx_status` |
| `operation_log` | 操作日志表 | `idx_module` + `idx_created_at` |
| `quiz_questions` | 测验题目表 | - |
| `quiz_record` | 测验记录表 | `idx_user_id` |
| `plant_game_record` | 植物游戏记录表 | `idx_user_id` |
| `chat_history` | AI聊天历史表 | `idx_user_session` |
| `browse_history` | 浏览历史表 | `idx_target` |
| `search_history` | 搜索历史表 | `idx_keyword` |

**全文索引（ngram解析器）**：

```sql
-- 支持中文全文搜索
FULLTEXT INDEX `ft_plants_search`(`name_cn`, `name_dong`, `efficacy`, `story`) WITH PARSER `ngram`
FULLTEXT INDEX `ft_knowledge_search`(`title`, `content`) WITH PARSER `ngram`
FULLTEXT INDEX `ft_inheritors_search`(`name`, `specialties`, `bio`) WITH PARSER `ngram`
```

> **注意**：Flyway仅在开发环境（`application-dev.yml`中`spring.flyway.enabled=true`）自动执行。生产环境（`application-prod.yml`中`spring.flyway.enabled=false`）需手动执行SQL脚本。

---

## 四、配置项跨环境对照表

### 数据源配置

| 配置项 | dev | prod |
|--------|-----|------|
| 最大连接数 | 20 | 15 |
| 最小空闲连接 | 5 | 2 |
| 空闲超时 | 300000ms(5min) | 600000ms(10min) |
| 连接超时 | 30000ms | 30000ms |
| 最大生命周期 | 1200000ms(20min) | 1800000ms(30min) |

### Redis配置

| 配置项 | dev | prod |
|--------|-----|------|
| max-active | 8 | 8 |
| max-wait | -1ms(无限等待) | 5000ms(5秒超时) |
| max-idle | 8 | 4 |
| min-idle | 0 | 2 |

### RabbitMQ配置

| 配置项 | dev | prod |
|--------|-----|------|
| publisher-confirm-type | 未设置 | correlated |
| publisher-returns | 未设置 | true |
| retry.max-attempts | 未设置(默认1) | 3 |
| retry.initial-interval | 未设置 | 1000ms |
| retry.multiplier | 未设置 | 2.0 |

### 安全配置

| 配置项 | dev | prod |
|--------|-----|------|
| JWT密钥 | 默认值(不安全) | `${JWT_SECRET}`(必须设置) |
| Token有效期 | 2592000s(30天) | 604800s(7天) |
| Swagger文档 | 默认启用 | 关闭 |
| 健康检查详情 | always | when_authorized |

### 日志配置

| 配置项 | dev | prod |
|--------|-----|------|
| 业务日志级别 | DEBUG | INFO |
| Sa-Token日志 | 未设置 | ERROR |
| SQL日志 | StdOutImpl(打印) | NoLoggingImpl(不打印) |
| 日志文件 | 未设置 | `/app/logs/dong-medicine.log` |
| 日志滚动 | 未设置 | 50MB/文件, 30天, 1GB上限 |

---

## 五、文件间依赖关系

```
application.yml（主配置）
    ├── 被引用 ──► application-dev.yml（dev Profile覆盖）
    ├── 被引用 ──► application-prod.yml（prod Profile覆盖）
    └── 引用 ──► 环境变量（${DB_USERNAME}, ${JWT_SECRET} 等）

application.properties
    └── 定义 ──► spring.application.name（被logback-spring.xml引用）

logback-spring.xml
    ├── 引用 ──► spring.application.name（日志文件名）
    ├── 引用 ──► logging.level.root（日志级别）
    ├── 引用 ──► SensitiveDataConverter（自定义脱敏转换器）
    └── 依赖 ──► Spring Profile（dev/prod区分日志策略）

db/migration/V1__init_schema.sql
    └── 被 ──► application-dev.yml 中 Flyway 自动执行

db/migration/V2__seed_data.sql
    └── 被 ──► application-dev.yml 中 Flyway 自动执行
```

---

## 六、环境变量清单

所有支持环境变量覆盖的配置项：

| 环境变量 | 默认值 | 说明 | 是否生产必填 |
|---------|--------|------|------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | 激活的Profile | 否 |
| `DB_HOST` | `localhost` | 数据库主机 | 否 |
| `DB_PORT` | `3306` | 数据库端口 | 否 |
| `DB_USERNAME` | `root` | 数据库用户名 | 否 |
| `DB_PASSWORD` | 空 | 数据库密码 | 是 |
| `REDIS_HOST` | `localhost` | Redis主机 | 否 |
| `REDIS_PORT` | `6379` | Redis端口 | 否 |
| `REDIS_PASSWORD` | 空 | Redis密码 | 否 |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ主机 | 否 |
| `RABBITMQ_PORT` | `5672` | RabbitMQ端口 | 否 |
| `RABBITMQ_USER` | `guest`/`admin` | RabbitMQ用户名 | 否 |
| `RABBITMQ_PASSWORD` | `guest` | RabbitMQ密码 | 是 |
| `JWT_SECRET` | 空/dev默认值 | JWT签名密钥 | 是 |
| `JWT_EXPIRATION` | `86400000` | JWT过期时间(ms) | 否 |
| `SA_TOKEN_TIMEOUT` | `2592000`/`604800` | Sa-Token超时(秒) | 否 |
| `APP_RABBITMQ_ENABLED` | `true` | 是否启用RabbitMQ | 否 |
| `CORS_ORIGIN_1/2/3` | `localhost:5173`等 | CORS允许的源 | 否 |
| `DEEPSEEK_API_KEY` | 空 | DeepSeek AI密钥 | 否 |
| `KKFILEVIEW_URL` | `localhost:8012` | KKFileView服务地址 | 否 |
| `MAX_BODY_SIZE` | `10485760` | 请求体最大大小 | 否 |
| `WS_ALLOWED_ORIGINS` | `localhost` | WebSocket允许的源 | 否 |

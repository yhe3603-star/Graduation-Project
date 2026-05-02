# 侗乡医药数字展示平台 - 后端服务

> Spring Boot 3.1.12 + MyBatis-Plus 3.5.9 + Sa-Token 1.37.0 构建的侗族医药文化遗产数字化展示平台后端
> 为前端Vue 3应用提供RESTful API、WebSocket实时通信、文件管理、AI智能问答等服务

---

## 技术栈一览

| 技术 | 版本 | 用途 |
|------|------|------|
| **Spring Boot** | 3.1.12 | Web框架，内嵌Tomcat，自动配置 |
| **MyBatis-Plus** | 3.5.9 | ORM框架，LambdaQueryWrapper类型安全查询 |
| **Sa-Token** | 1.37.0 | 权限认证（整合JWT + Redis） |
| **MySQL** | 8.0+ | 关系型数据库，存储所有持久化数据 |
| **Redis** | 7.0+ | 分布式缓存 + Session共享 + 限流计数器 |
| **RabbitMQ** | 3.x | 异步消息队列（操作日志、热度计算、统计等） |
| **Caffeine** | 3.x | 本地内存缓存（Redis不可用时自动降级） |
| **Spring WebSocket** | 3.1.12 | AI聊天流式推送，支持双向实时通信 |
| **Spring WebFlux** | 3.1.12 | 反应式HTTP客户端（DeepSeek API流式调用） |
| **SpringDoc OpenAPI** | 2.2.0 | 自动生成Swagger API文档 |
| **Lombok** | 1.18.38 | 减少样板代码（@Data, @RequiredArgsConstructor等） |
| **BCrypt** | (spring-security-crypto) | 密码加密，不可逆 |
| **JDK** | 17 | Java开发工具包 |

### 依赖清单（pom.xml核心依赖）

```xml
<!-- Web + WebSocket + WebFlux -->
spring-boot-starter-web / spring-boot-starter-websocket / spring-boot-starter-webflux

<!-- 数据库 -->
mysql-connector-j (runtime)
mybatis-plus-boot-starter 3.5.9
mybatis-plus-jsqlparser 3.5.9

<!-- 认证 -->
sa-token-spring-boot3-starter 1.37.0
sa-token-jwt 1.37.0
sa-token-redis-jackson 1.37.0
spring-security-crypto (BCrypt)

<!-- 缓存 -->
spring-boot-starter-data-redis
caffeine (本地缓存降级)

<!-- 消息队列 -->
spring-boot-starter-amqp (RabbitMQ)

<!-- 工具 -->
lombok 1.18.38
spring-boot-starter-validation (参数校验)
spring-boot-starter-aop (切面编程)
springdoc-openapi-starter-webmvc-ui 2.2.0
dotenv-java 3.0.0 (.env文件支持)
```

---

## 目录结构

```
dong-medicine-backend/
├── pom.xml                              # Maven依赖配置
├── sql/                                 # 数据库脚本
│   └── dong_medicine.sql               # 建表语句 + 种子数据（50+种侗药）
├── src/
│   ├── main/
│   │   ├── java/com/dongmedicine/
│   │   │   ├── DongMedicineBackendApplication.java  # 启动入口（加载.env + @EnableCaching）
│   │   │   ├── controller/              # 控制器层（24个Controller）
│   │   │   ├── service/                 # 服务层（18个Service接口 + 实现类）
│   │   │   ├── mapper/                  # 数据访问层（15个Mapper接口）
│   │   │   ├── entity/                  # 实体类（15个Entity + BaseEntity基类）
│   │   │   ├── dto/                     # 数据传输对象（23个DTO）
│   │   │   ├── config/                  # 配置类（20+个，含health/logging子包）
│   │   │   ├── mq/                      # RabbitMQ生产者/消费者
│   │   │   │   ├── producer/            #   5个消息生产者
│   │   │   │   └── consumer/            #   5个消息消费者
│   │   │   ├── websocket/               # WebSocket处理（ChatWebSocketHandler）
│   │   │   └── common/                  # 通用模块
│   │   │       ├── constant/            #   常量定义（ApiPaths, RabbitMQConstants, RoleConstants）
│   │   │       ├── exception/           #   异常体系（BusinessException, ErrorCode, GlobalExceptionHandler）
│   │   │       └── util/                #   工具类（XssUtils, PageUtils, PasswordValidator, FileCleanupHelper等）
│   │   └── resources/
│   │       └── application.yml          # 主配置文件（环境变量注入 + Sa-Token + 缓存 + RabbitMQ配置）
│   └── test/                            # 测试代码（JUnit + H2内存数据库）
└── public/                              # 文件上传存储根目录
    ├── images/                          #   图片文件
    ├── videos/                          #   视频文件
    └── documents/                       #   文档文件
```

---

## 三层架构详解

```
┌──────────────────────────────────────────────────────────────────┐
│  Controller 层（接收HTTP请求，参数校验，调用Service，返回R<T>）    │
│  @RestController + @RequestMapping + @Validated                  │
│  示例: PlantController.java - 7个接口（list/search/detail/similar/│
│        random/batch/incrementView）                               │
├──────────────────────────────────────────────────────────────────┤
│  Service 层（业务逻辑，缓存，事务）                                │
│  接口 extends IService<Entity> + 实现类 extends ServiceImpl        │
│  @Service + @Transactional + @Cacheable/@CacheEvict               │
│  示例: PlantServiceImpl.java - 搜索(MySQL全文索引降级LIKE)、缓存、 │
│        随机查询、文件清理                                          │
├──────────────────────────────────────────────────────────────────┤
│  Mapper 层（数据访问，自定义SQL）                                  │
│  接口 extends BaseMapper<Entity>                                  │
│  @Mapper + @Select/@Update（自定义SQL注解）                       │
│  示例: PlantMapper.java - incrementViewCount, selectRandomPlants, │
│        searchByFullText, searchByLike, 统计聚合查询               │
└──────────────────────────────────────────────────────────────────┘
```

### 完整请求示例：查看植物详情

```
GET /api/plants/1
  │
  ├── SaTokenConfig 拦截器 → 检查该路径在excludePathPatterns中 → 放行（GET请求免登录）
  │
  ├── PlantController.detail(@PathVariable Integer id=1)
  │   ├── plantService.getDetailWithStory(1)
  │   │   └── PlantMapper.selectById(1)  [MyBatis-Plus自带方法]
  │   │       生成SQL: SELECT * FROM plants WHERE id=1
  │   ├── BrowseHistoryService.record(userId, "plant", 1)  [记录浏览历史]
  │   └── return R.ok(plant)  [统一响应格式]
  │
  └── 响应JSON:
      {
        "code": 200,
        "msg": "success",
        "data": {
          "id": 1,
          "nameCn": "钩藤",
          "nameDong": "jis xenc",
          "scientificName": "Uncaria rhynchophylla",
          "category": "清热药",
          "efficacy": "清热平肝，息风止痉",
          "story": "侗族老人常说...",
          "images": "[\"/images/plants/gouteng1.jpg\"]",
          "viewCount": 128,
          "favoriteCount": 15,
          "popularity": 42
        },
        "requestId": "a1b2c3d4e5f6g7h8"
      }
```

### 统一响应格式 R\<T\>

类路径：`com.dongmedicine.common.R<T>`

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 200=成功，400=参数错误，401=未登录，403=无权限，404=不存在，500=服务器错误 |
| `msg` | String | 提示信息 |
| `data` | T (泛型) | 实际数据 |
| `requestId` | String | 请求追踪ID（由RequestIdFilter生成，16位UUID） |

```java
// 常用静态工厂方法
R.ok()                          // 200 + "success" + null
R.ok(data)                      // 200 + "success" + data
R.ok("msg", data)              // 200 + custom_msg + data
R.error("msg")                  // 500 + msg
R.error(ErrorCode.USER_NOT_FOUND)  // 1001 + "用户不存在"
R.badRequest("msg")             // 400
R.unauthorized("msg")           // 401
R.forbidden("msg")              // 403
R.notFound("msg")               // 404
```

---

## 认证与权限体系

### Sa-Token + JWT 认证流程

```
1. 用户登录 POST /api/user/login
   ├── CaptchaService.validateCaptchaOrThrow()  → Redis验证码校验
   ├── UserServiceImpl.login(username, password)
   │   ├── 查询用户 + BCrypt密码比对
   │   ├── StpUtil.login(userId)  → 生成JWT Token
   │   └── Session存储 username + role
   └── 返回 {token, id, username, role}

2. 后续请求携带: Authorization: Bearer {token}
   ├── SaTokenConfig 拦截 /api/**
   │   ├── GET请求 → 默认放行（excludePathPatterns中配置的公开接口）
   │   └── POST/PUT/DELETE/PATCH → 检查登录状态（WRITE_METHODS列表）
   └── @SaCheckRole("admin") → 额外检查admin角色（AdminController类级别注解）

3. Token续期: POST /api/user/refresh-token
   └── StpUtil.renewTimeout()  → Token有效期延长

4. 退出登录: POST /api/user/logout
   └── StpUtil.logout()  → 清除当前会话
```

### 权限注解使用

| 位置 | 注解 | 作用 |
|------|------|------|
| 类级别 | `@SaCheckRole("admin")` | AdminController整个类需要admin角色 |
| 方法级别 | `@SaCheckLogin` | user/me, change-password等需要登录 |
| 默认 | 无注解 | 公开接口，GET请求免登录访问 |

### JWT配置（application.yml）

```yaml
sa-token:
  token-name: Authorization          # Header名称
  timeout: 2592000                   # Token有效期30天（秒）
  active-timeout: -1                 # 不限制无操作自动过期
  is-concurrent: true                # 允许同一账号多地登录
  token-style: uuid                  # Token风格
  is-read-header: true               # 从Header读取Token
  is-read-cookie: false              # 不从Cookie读取
  token-prefix: Bearer               # Bearer前缀
  jwt-secret-key: ${JWT_SECRET}      # JWT签名密钥（环境变量注入）
```

---

## 缓存架构

### 双层缓存（Redis + Caffeine降级）

```
CacheConfig.cacheManager()
  │
  ├── 尝试连接Redis → ping()成功
  │   └── 使用RedisCacheManager（分布式缓存）
  │       ├── plants: 6小时     ├── inheritors: 6小时
  │       ├── knowledges: 6小时  ├── resources: 4小时
  │       ├── users: 30分钟     ├── quizQuestions: 12小时
  │       ├── searchResults: 5分钟  └── hotData: 1小时
  │
  └── Redis连接失败 → 降级
      └── CaffeineCacheManager（本地内存缓存）
          └── maxSize=1000, 默认过期60分钟
          └── 每60秒检查一次Redis是否恢复
```

### 缓存注解使用

```java
// 查询方法：先查缓存，未命中再查数据库
@Cacheable(value = "searchResults", key = "'plants:' + (#keyword ?: 'all')")
public List<Plant> advancedSearch(...) { ... }

// 写操作：清除相关缓存
@CacheEvict(value = "plants", allEntries = true)
public void clearCache() { ... }

// 删除操作：事务 + 清除缓存
@Transactional(rollbackFor = Exception.class)
@CacheEvict(value = "plants", allEntries = true)
public void deleteWithFiles(Integer id) { ... }
```

---

## RabbitMQ 消息队列

### 5个队列及其职责

| 队列 | Exchange | 生产者 | 消费者 | 说明 |
|------|----------|--------|--------|------|
| `operation.log.queue` | Direct | OperationLogProducer | OperationLogConsumer | 异步保存操作日志到MySQL |
| `feedback.queue` | Direct | FeedbackProducer | FeedbackConsumer | 异步保存用户反馈 |
| `file.process.queue` | Direct | FileProcessProducer | FileProcessConsumer | 异步文件处理 |
| `statistics.queue` | Topic | StatisticsProducer | StatisticsConsumer | 异步统计数据(浏览/点赞/分享/日统计) |
| `notification.queue` | Direct | NotificationProducer | NotificationConsumer | 异步消息通知 |

### 特性
- **Lazy队列**：所有队列使用 `x-queue-mode: lazy` 模式，消息存储在磁盘
- **死信队列（DLQ）**：statistics和notification队列配置了死信交换器
- **发送确认**：RabbitTemplate配置了ConfirmCallback和ReturnsCallback
- **重试机制**：指数退避重试（初始1秒，最大10秒，最多3次）
- **条件启用**：`@ConditionalOnProperty(app.rabbitmq.enabled)`，可通过配置关闭

---

## 安全机制一览

| 组件 | 作用 | 实现方式 |
|------|------|---------|
| **SaTokenConfig** | 路由拦截 | SaInterceptor + 路径白名单 |
| **StpInterfaceImpl** | 角色查询 | 实现StpInterface，从数据库查user.role |
| **XssFilter** | XSS防护 | 过滤表单参数和JSON请求体中的危险标签 |
| **XssUtils** | XSS检测 | 30+种危险模式正则匹配 |
| **RateLimitAspect** | 接口限流 | Redis Lua脚本 + 本地令牌桶降级 |
| **RequestIdFilter** | 请求追踪 | 16位UUID，放入MDC + 响应Header |
| **RequestSizeFilter** | 请求大小限制 | 限制请求体大小 |
| **BCrypt** | 密码加密 | BCryptPasswordEncoder，不可逆 |
| **PageUtils.escapeLike()** | LIKE注入防护 | 转义 `%`、`_`、`\` 特殊字符 |

---

## 应用配置（application.yml 核心项）

```yaml
server.port: 8080                              # 服务端口
spring.datasource.url: jdbc:mysql://...        # 数据库连接
spring.data.redis.host/port/password           # Redis连接
spring.rabbitmq.host/port/username/password    # RabbitMQ连接
spring.servlet.multipart.max-file-size: 100MB  # 文件上传大小限制

sa-token:
  timeout: ${SA_TOKEN_TIMEOUT:2592000}         # Token过期（30天）
  jwt-secret-key: ${JWT_SECRET:}               # JWT密钥（必须通过环境变量设置）

app:
  rabbitmq.enabled: ${APP_RABBITMQ_ENABLED:true}  # RabbitMQ开关
  search.use-fulltext: true                    # MySQL全文搜索开关
  request.max-body-size: 10485760             # 请求体最大10MB

file.upload.base-path: ${user.dir}/public      # 文件存储根路径
```

所有敏感配置（数据库密码、JWT密钥、API密钥等）均通过环境变量注入，不硬编码。

---

## 构建与运行

### 环境要求

- JDK 17+
- MySQL 8.0+（需先执行 `sql/dong_medicine.sql`）
- Redis 7.0+（可选，不可用时自动降级）
- RabbitMQ 3.x（可选，可通过配置关闭）

### 本地开发运行

```bash
# 1. 初始化数据库
mysql -u root -p < sql/dong_medicine.sql

# 2. 配置环境变量（创建.env文件）
cp .env.example .env
# 编辑.env: DB_PASSWORD, JWT_SECRET, ADMIN_INIT_PASSWORD

# 3. 启动
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

### 打包部署

```bash
mvn clean package -DskipTests
java -jar target/dong-medicine-backend-1.0.0.jar
```

### Docker Compose一键部署（项目根目录）

```bash
docker-compose up -d
# 自动启动: MySQL + Redis + RabbitMQ + Backend + Frontend(Nginx) + KKFileView
```

### 验证

```bash
# API文档: http://localhost:8080/swagger-ui.html
# 健康检查: http://localhost:8080/actuator/health
# 测试接口: curl http://localhost:8080/api/plants/list?page=1&size=10
```

---

## 默认管理员账号

| 字段 | 值 |
|------|-----|
| 用户名 | admin |
| 密码 | 环境变量 `ADMIN_INIT_PASSWORD` 的值（由AdminDataInitializer在首次启动时创建） |

---

## 模块文档导航

| 模块 | 文档 | 核心内容 |
|------|------|---------|
| 源码根包 | [src/.../dongmedicine/README.md](src/main/java/com/dongmedicine/README.md) | 三层架构详解、新模块开发指南 |
| 控制器 | [controller/README.md](src/main/java/com/dongmedicine/controller/README.md) | 24个Controller、全部API端点 |
| 服务层 | [service/README.md](src/main/java/com/dongmedicine/service/README.md) | 18个Service、缓存/事务/异步 |
| 数据层 | [mapper/README.md](src/main/java/com/dongmedicine/mapper/README.md) | 15个Mapper、BaseMapper继承 |
| 实体类 | [entity/README.md](src/main/java/com/dongmedicine/entity/README.md) | 15个Entity、BaseEntity基类 |
| DTO | [dto/README.md](src/main/java/com/dongmedicine/dto/README.md) | 23个DTO、参数校验 |
| 配置 | [config/README.md](src/main/java/com/dongmedicine/config/README.md) | 20+配置类、认证/缓存/安全 |
| 消息队列 | [mq/README.md](src/main/java/com/dongmedicine/mq/README.md) | 5个Producer + 5个Consumer |
| WebSocket | [websocket/README.md](src/main/java/com/dongmedicine/websocket/README.md) | AI聊天流式通信 |
| 通用模块 | [common/README.md](src/main/java/com/dongmedicine/common/README.md) | R类、异常体系、工具类 |

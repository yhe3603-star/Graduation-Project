# 侗乡医药数字展示平台 -- 后端服务

> 基于 Spring Boot 3.1.12 + Java 17 的侗族医药文化遗产数字化展示平台后端

---

## 一、什么是 Spring Boot？

### 生活类比：精装修的房子

想象你要开一家侗族药铺：

| 方式 | 类比 | 说明 |
|------|------|------|
| 纯 Java Web | 买一块空地，自己打地基、砌墙、布线、装修 | 从零搭建 Tomcat、配置依赖、处理各种集成 |
| **Spring Boot** | **买一套精装修的房子，拎包入住** | 内置服务器、自动配置、开箱即用 |

Spring Boot 帮你做了三件最重要的事：

1. **内嵌 Tomcat 服务器** -- 不用自己安装和配置 Web 服务器，运行 `main()` 方法就能启动
2. **自动配置（Auto Configuration）** -- 引入 MySQL 依赖就自动配置数据库连接，引入 Redis 依赖就自动配置缓存
3. **起步依赖（Starter）** -- 一个依赖包搞定一整套功能，比如 `spring-boot-starter-web` 包含了 JSON、Tomcat、Spring MVC

### 我们的项目用 Spring Boot 做了什么？

```
+-------------------------------------------------------+
|                  Spring Boot 应用                       |
|                                                       |
|  +-----------+  +-----------+  +-----------+          |
|  | 接收请求   |  | 处理业务   |  | 存取数据   |          |
|  | Controller |  | Service   |  | Mapper    |          |
|  +-----------+  +-----------+  +-----------+          |
|       |              |              |                  |
|  +----|--------------|--------------|----+            |
|  |    v              v              v    |            |
|  |  Sa-Token认证     Redis缓存    MySQL  |            |
|  |  JWT模式          Caffeine降级  MyBatis |            |
|  +---------------------------------------+            |
+-------------------------------------------------------+
```

---

## 二、什么是三层架构？

### 生活类比：餐厅的运作

```
顾客点餐                厨房做菜               仓库取食材
========               =======               =========
服务员                  厨师                  仓库管理员
(Controller)           (Service)             (Mapper)

"我要一份钩藤茶"  -->  "钩藤茶？让我查查配方" --> "钩藤在第3号货架"
                     "配方需要钩藤+蜂蜜"     --> "蜂蜜在第7号货架"
                     "做好了，上菜！"    <--  "食材拿来了"
"这是您的钩藤茶" <--  "做好了"
```

| 层 | 类比 | 职责 | 项目中的包 |
|----|------|------|-----------|
| **Controller** | 服务员 | 接收请求、返回响应，不做业务逻辑 | `controller/` |
| **Service** | 厨师 | 处理业务逻辑、缓存、事务 | `service/` + `service/impl/` |
| **Mapper** | 仓库管理员 | 只负责和数据库交互（增删改查） | `mapper/` |

### 为什么不把所有代码写在一起？

**反面教材：所有代码塞在 Controller 里**

```java
// 错误示范！不要这样写！
@RestController
public class PlantController {
    @GetMapping("/plants/{id}")
    public R<Plant> getPlant(@PathVariable Integer id) {
        // 连数据库、做缓存、处理逻辑...全混在一起
        // 如果另一个接口也要查植物，这段代码就要复制一遍
        // 如果数据库换了，每个 Controller 都要改
        // 代码又臭又长，维护噩梦！
    }
}
```

**正确做法：每层只做自己该做的事**

```java
// Controller：只负责接收请求、返回结果
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable Integer id) {
    Plant plant = service.getDetailWithStory(id);  // 交给厨师
    return plant == null ? R.error("植物不存在") : R.ok(plant);
}

// Service：只负责业务逻辑
@Override
@Cacheable(value = "plants", key = "#id")  // 缓存注解
public Plant getDetailWithStory(Integer id) {
    return getById(id);  // 交给仓库管理员
}

// Mapper：只负责数据库操作
@Mapper
public interface PlantMapper extends BaseMapper<Plant> {
    // 继承 BaseMapper 就自动有了增删改查方法
}
```

---

## 三、技术栈一览

| 技术 | 版本 | 用途 | 生活类比 |
|------|------|------|---------|
| Spring Boot | 3.1.12 | 核心框架 | 精装修的房子 |
| Sa-Token | 1.37.0 | 权限认证框架 | 门口的保安 |
| Sa-Token JWT | 1.37.0 | JWT模式 | 会员卡 |
| MyBatis-Plus | 3.5.9 | 数据库操作 | 仓库管理员 |
| MySQL | 8.0+ | 关系型数据库 | 仓库 |
| Redis | 7.0+ | 分布式缓存 | 厨房旁边的调料架 |
| RabbitMQ | 3.x | 消息队列 | 快递中转站（异步处理） |
| Caffeine | -- | 本地缓存（降级用） | 厨师口袋里的小本子 |
| SpringDoc | 2.2.0 | API文档 | 菜单 |
| Lombok | 1.18.38 | 简化代码 | 自动写手 |
| DeepSeek API | -- | AI智能问答 | 智能客服 |

---

## 四、项目结构

```
dong-medicine-backend/
|
+-- sql/                          # 数据库脚本
|   +-- dong_medicine.sql         # 完整建表与种子数据
|   +-- migrations/               # 数据库迁移脚本
|
+-- src/main/
|   +-- java/com/dongmedicine/    # Java 源代码（详见 src README）
|   |   +-- common/               # 通用工具（R、异常、工具类）
|   |   +-- config/               # 配置类（安全、缓存、限流、MQ等）
|   |   +-- controller/           # 控制器层（17个控制器）
|   |   +-- dto/                  # 数据传输对象
|   |   +-- entity/               # 数据库实体类（含BaseEntity公共基类）
|   |   +-- mapper/               # 数据库映射接口
|   |   +-- mq/                   # RabbitMQ消息消费者
|   |   +-- service/              # 业务逻辑层
|   |   |   +-- impl/             # 业务实现类
|   |   +-- DongMedicineBackendApplication.java  # 启动入口
|   |
|   +-- resources/
|       +-- application.yml       # 主配置文件
|       +-- application-dev.yml   # 开发环境配置
|       +-- application-prod.yml  # 生产环境配置
|       +-- logback-spring.xml    # 日志配置
|
+-- public/                       # 上传文件存储目录
|   +-- images/                   # 图片文件
|   +-- documents/                # 文档文件
|
+-- pom.xml                       # Maven 依赖配置
+-- Dockerfile                    # Docker 镜像构建
+-- .env.example                  # 环境变量模板
```

---

## 五、完整 API 文档

### 5.1 公开接口（无需登录）

#### 药用植物

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|---------|
| GET | `/api/plants/list` | 植物列表（分页+筛选） | `page`, `size`, `category`, `usageWay`, `keyword` |
| GET | `/api/plants/search` | 搜索植物 | `keyword`(必填), `page`, `size` |
| GET | `/api/plants/{id}` | 植物详情 | 路径参数 `id` |
| GET | `/api/plants/{id}/similar` | 相似植物 | 路径参数 `id` |
| GET | `/api/plants/random` | 随机植物 | `limit`(1-100, 默认20) |
| POST | `/api/plants/{id}/view` | 增加浏览量 | 路径参数 `id` |
| POST | `/api/plants/batch` | 批量获取植物 | Body: `{ids: [1,2,3]}` |

**请求示例：获取植物列表**

```
GET /api/plants/list?page=1&size=12&category=清热药&keyword=钩藤
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "nameCn": "钩藤",
        "nameDong": "jis xenc",
        "scientificName": "Uncaria rhynchophylla",
        "category": "清热药",
        "usageWay": "煎服",
        "habitat": "山谷林中",
        "efficacy": "清热平肝，息风止痉",
        "story": "侗族老人常说...",
        "images": "[\"/images/plants/gouteng1.jpg\"]",
        "viewCount": 128,
        "favoriteCount": 15,
        "createdAt": "2025-01-15 10:30:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 12
  },
  "requestId": "a1b2c3d4"
}
```

#### 用户认证

| 方法 | 路径 | 说明 | 限流 |
|------|------|------|------|
| POST | `/api/user/login` | 用户登录 | 5次/分 |
| POST | `/api/user/register` | 用户注册 | 3次/分 |
| GET | `/api/captcha` | 获取验证码 | -- |
| GET | `/api/user/validate` | 验证Token | -- |
| POST | `/api/user/refresh-token` | 刷新Token | -- |

**请求示例：登录**

```json
POST /api/user/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin123456",
  "captchaKey": "captcha:uuid-xxx",
  "captchaCode": "1234"
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "id": 1,
    "username": "admin",
    "role": "admin"
  },
  "requestId": "e5f6g7h8"
}
```

#### 其他公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/knowledge/list` | 侗医药知识列表 |
| GET | `/api/knowledge/{id}` | 知识详情 |
| POST | `/api/knowledge/{id}/view` | 增加知识浏览量 |
| GET | `/api/inheritors/list` | 传承人列表 |
| GET | `/api/inheritors/{id}` | 传承人详情 |
| POST | `/api/inheritors/{id}/view` | 增加传承人浏览量 |
| GET | `/api/qa/list` | 问答列表 |
| POST | `/api/qa/{id}/view` | 增加问答浏览量 |
| GET | `/api/resources/list` | 学习资源列表 |
| GET | `/api/resources/hot` | 热门资源 |
| POST | `/api/resources/{id}/view` | 增加资源浏览量 |
| GET | `/api/comments/list/{type}/{id}` | 评论列表 |
| GET | `/api/leaderboard/**` | 排行榜 |
| GET | `/api/stats/**` | 统计数据 |
| POST | `/api/chat` | AI聊天（10次/分） |
| POST | `/api/quiz/submit` | 提交测验 |
| POST | `/api/feedback` | 提交反馈（匿名可访问） |

### 5.2 认证接口（需要登录）

请求头需携带：`Authorization: Bearer <token>`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/me` | 获取当前用户信息 |
| POST | `/api/user/change-password` | 修改密码 |
| POST | `/api/user/logout` | 退出登录 |
| POST | `/api/comments` | 发表评论 |
| POST/DELETE | `/api/favorites/{type}/{id}` | 添加/取消收藏 |
| GET | `/api/favorites/my` | 我的收藏 |
| GET | `/api/quiz/records` | 我的测验记录 |
| GET | `/api/feedback/my` | 我的反馈 |

### 5.3 管理接口（需要 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表 |
| DELETE | `/api/admin/users/{id}` | 删除用户 |
| PUT | `/api/admin/users/{id}/role` | 修改用户角色 |
| PUT | `/api/admin/users/{id}/ban` | 封禁用户 |
| PUT | `/api/admin/users/{id}/unban` | 解封用户 |
| CRUD | `/api/admin/plants/*` | 植物管理 |
| CRUD | `/api/admin/knowledge/*` | 知识管理 |
| CRUD | `/api/admin/inheritors/*` | 传承人管理 |
| CRUD | `/api/admin/qa/*` | 问答管理 |
| CRUD | `/api/admin/resources/*` | 资源管理 |
| PUT | `/api/admin/feedback/{id}/reply` | 回复反馈 |
| PUT | `/api/admin/comments/{id}/approve` | 审核通过评论 |
| GET | `/api/admin/stats` | 仪表盘统计 |

> 完整 API 文档：启动后访问 http://localhost:8080/swagger-ui.html

---

## 六、配置文件详解

### 6.1 三个配置文件的关系

```
application.yml          <-- 主配置（公共部分，所有环境共享）
    |
    +-- application-dev.yml    <-- 开发环境（覆盖主配置的某些项）
    +-- application-prod.yml   <-- 生产环境（覆盖主配置的某些项）
```

就像一份菜单模板：
- **application.yml** = 基础菜单（所有分店都有）
- **application-dev.yml** = 开发分店的特色菜（调试模式、详细日志）
- **application-prod.yml** = 生产分店的特色菜（安全模式、精简日志）

### 6.2 application.yml -- 主配置

```yaml
server:
  port: 8080                    # 服务端口号，浏览器访问用

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}   # 默认使用 dev 环境
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}            # 数据库用户名，优先读环境变量
    password: ${DB_PASSWORD:}                # 数据库密码，优先读环境变量
  data:
    redis:
      host: ${REDIS_HOST:localhost}          # Redis 地址
      port: ${REDIS_PORT:6379}               # Redis 端口
      password: ${REDIS_PASSWORD:}           # Redis 密码

jwt:
  expiration: ${JWT_EXPIRATION:86400000}     # JWT 过期时间（毫秒）

sa-token:
  token-name: Authorization                  # Token 名称
  timeout: ${SA_TOKEN_TIMEOUT:2592000}       # Token 有效期（秒），默认30天
  is-concurrent: true                        # 是否允许同一账号多地同时登录
  is-share: false                            # 是否共用 Token
  is-read-cookie: false                      # 不从 Cookie 读取
  is-read-header: true                       # 从 Header 读取
  token-prefix: Bearer                       # Token 前缀
  jwt-secret-key: ${JWT_SECRET:dev-secret-...}  # JWT 签名密钥

app:
  cors:
    allowed-origins:

file:
  upload:
    base-path: ${user.dir}/public             # 上传文件存储路径
```

> `${DB_USERNAME:root}` 是什么意思？-- 如果环境变量 `DB_USERNAME` 存在就用它的值，否则用默认值 `root`。就像"如果有自带调料就用自带的，没有就用厨房默认的"。

### 6.3 application-dev.yml -- 开发环境

```yaml
# 开发环境的特殊配置
logging:
  level:
    com.dongmedicine: DEBUG              # 显示详细日志，方便调试
    cn.dev33.satoken: DEBUG  # Sa-Token 框架日志

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印SQL语句到控制台

management:
  endpoint:
    health:
      show-details: always               # 健康检查显示所有细节

deepseek:
  api-key: ${DEEPSEEK_API_KEY:}          # AI接口密钥
```

### 6.4 application-prod.yml -- 生产环境

```yaml
# 生产环境的特殊配置（更安全、更精简）
logging:
  level:
    com.dongmedicine: INFO               # 只显示重要日志
    cn.dev33.satoken: WARN   # Sa-Token 只显示警告

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl  # 不打印SQL

management:
  endpoint:
    health:
      show-details: never                # 不暴露健康检查细节（安全！）
      show-components: never

app:
  cors:
    allowed-origins:
      - ${CORS_ALLOWED_ORIGIN:https://your-domain.com}  # 只允许指定域名访问
```

### 6.5 三个环境的关键差异对比

| 配置项 | dev（开发） | prod（生产） |
|--------|------------|-------------|
| 日志级别 | DEBUG（详细） | INFO（精简） |
| SQL输出 | 打印到控制台 | 不输出 |
| 健康检查详情 | 全部显示 | 不显示 |
| JWT密钥 | 有默认值 | 必须设环境变量 |
| CORS域名 | localhost | 正式域名 |
| 文件存储路径 | 项目目录/public | /app/public |

---

## 七、安全机制详解

### 7.1 Sa-Token + JWT 认证 -- "会员卡 + 会话管理"

**生活类比：** 你去侗族药铺办了张会员卡（JWT Token），以后每次来出示会员卡就行，不用每次都登记。Sa-Token 是管理这些会员卡的系统，能随时作废某张卡（踢人下线）。

```
注册/登录
   |
   v
Sa-Token 执行 StpUtil.login(userId) 生成 JWT Token
   |
   v
前端保存 Token，每次请求带上：Authorization: Bearer eyJhbG...
   |
   v
SaInterceptor 拦截请求，验证 Token 是否有效
   |
   +-- 有效 --> 放行，Controller 可通过 StpUtil 获取用户信息
   +-- 无效 --> 返回 401 未授权
```

**Sa-Token 常用 API：**

| API | 说明 |
|-----|------|
| `StpUtil.login(userId)` | 登录，生成 Token |
| `StpUtil.logout()` | 退出登录 |
| `StpUtil.kickout(userId)` | 踢人下线 |
| `StpUtil.getLoginIdAsInt()` | 获取当前登录用户ID |
| `StpUtil.isLogin()` | 是否已登录 |
| `@SaCheckLogin` | 注解：验证登录 |
| `@SaCheckRole("admin")` | 注解：验证角色 |

### 7.2 BCrypt 密码加密 -- "不可逆的保险箱"

**生活类比：** 密码就像你家的钥匙，BCrypt 是一个特殊的保险箱 -- 你能把钥匙放进去，但永远拿不出来。验证时，你把新钥匙放进去，保险箱会告诉你"这两把钥匙是不是同一把"。

```java
// 注册时：加密存储
user.setPasswordHash(passwordEncoder.encode("Admin123456"));
// 数据库里存的是：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxep68lN4EtLcifFm

// 登录时：验证密码
passwordEncoder.matches("Admin123456", user.getPasswordHash());  // true
passwordEncoder.matches("wrong_password", user.getPasswordHash()); // false
```

> 为什么不用 MD5？MD5 是"快"的加密，黑客可以用彩虹表秒破。BCrypt 每次加密结果不同，而且故意很"慢"，让暴力破解变得不现实。

### 7.3 XSS 防护 -- "安检门"

**XSS 是什么？** 黑客在输入框里写恶意脚本（如 `<script>alert('hack')</script>`），其他用户看到时脚本就会执行。

**我们的防护：** `XssFilter` + `XssUtils` 对所有用户输入进行过滤，识别并清除 30+ 种危险模式。

### 7.4 速率限制 -- "排队叫号"

**生活类比：** 药铺门口放个叫号机，每分钟只叫5个号，防止有人恶意刷号。

```java
@RateLimit(value = 5, key = "user_login")   // 登录：每秒最多5次
@RateLimit(value = 3, key = "user_register") // 注册：每秒最多3次
```

**实现原理：** Redis + Lua脚本实现原子化计数器，确保并发安全。Redis 不可用时自动降级到本地令牌桶。

### 7.5 RabbitMQ 异步处理 -- "快递中转站"

**生活类比：** 操作日志和热度计算就像快递包裹，不需要立即处理，放到中转站（RabbitMQ），由专门的快递员（消费者）慢慢处理，不阻塞主流程。

```
用户浏览植物 → Controller处理请求 → 同时发送消息到RabbitMQ
                                        ↓
                              消费者异步处理：
                              ├── 保存操作日志到数据库
                              └── 更新热度分数
```

**优势：** 主接口响应速度不受日志写入和热度计算影响，提升用户体验。

### 7.6 BaseEntity 公共基类 -- "统一表单格式"

所有实体类继承 `BaseEntity`，自动拥有 `id`、`createdAt`、`updatedAt` 三个公共字段，`MyMetaObjectHandler` 自动填充时间戳：

```java
@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### 7.7 SQL 注入防护 -- "仓库管理员只认表格"

MyBatis-Plus 的 `LambdaQueryWrapper` 使用参数化查询，用户输入永远不会被拼接到 SQL 语句中：

```java
// 安全！参数化查询，用户输入被当作数据处理
qw.like(Plant::getNameCn, escapedKeyword);

// 危险！不要这样写！字符串拼接会导致SQL注入
// "SELECT * FROM plants WHERE name_cn LIKE '%" + keyword + "%'"
```

---

## 八、如何运行项目

### 8.1 环境要求

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 运行 Java 程序 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| Maven | 3.8+ | 依赖管理（项目自带 mvnw） |

### 8.2 启动步骤

```bash
# 第1步：初始化数据库
mysql -u root -p < sql/dong_medicine.sql

# 第2步：配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入以下内容：
# DB_PASSWORD=你的MySQL密码
# JWT_SECRET=一个至少64字符的随机字符串
# REDIS_PASSWORD=你的Redis密码（如果有的话）

# 第3步：启动 Redis
redis-server

# 第4步：启动项目（使用项目自带的 Maven Wrapper，无需安装 Maven）
./mvnw spring-boot:run

# Windows 用户使用：
mvnw.cmd spring-boot:run
```

### 8.3 验证启动成功

```
看到以下日志说明启动成功：

  ____             _                          ____              _
 |  _ \ ___  _ __ | |_ ___ _ __ _ __ _   _   |  _ \  ___   ___ | |_
 | | | / _ \| '_ \| __/ _ \ '__| '__| | | |  | | | |/ _ \ / _ \| __|
 | |_| | (_) | | | | ||  __/ |  | |  | |_| |  | |_| | (_) | (_) | |_
 |____/ \___/|_| |_|\__\___|_|  |_|   \__, |  |____/ \___/ \___/ \__|
                                       |___/
侗乡医药数字展示平台后端服务启动成功！
访问地址: http://localhost:8080
API文档: http://localhost:8080/swagger-ui.html
```

### 8.4 Docker 方式启动

```bash
# 构建镜像
docker build -t dong-medicine-backend .

# 运行容器
docker run -p 8080:8080 --env-file .env dong-medicine-backend
```

---

## 九、常见问题

### Q1: 启动报错 "Communications link failure"
**原因：** MySQL 没启动或连接配置不对。
**解决：** 检查 MySQL 是否运行，检查 `application-dev.yml` 中的数据库地址和密码。

### Q2: 启动报错 "Unable to connect to Redis"
**原因：** Redis 没启动。
**解决：** 项目会自动降级到 Caffeine 本地缓存，功能正常但性能略差。建议启动 Redis。

### Q3: 接口返回 401 Unauthorized
**原因：** Token 过期或没带。
**解决：** 检查请求头是否有 `Authorization: Bearer <token>`，Token 是否过期。

### Q4: 接口返回 403 Forbidden
**原因：** 权限不足。比如普通用户访问了 ADMIN 接口。
**解决：** 确认当前用户角色是否匹配接口要求的角色。

### Q5: 文件上传失败
**原因：** 文件大小超限或格式不支持。
**解决：** 图片最大 10MB，视频最大 100MB，文档最大 50MB。支持的格式见配置文件。

# 侗乡医药数字展示平台 — 后端服务

## 项目简介

侗乡医药数字展示平台（dong-medicine-backend）是一个面向侗族传统医药文化的数字化展示系统后端，采用前后端分离架构。后端基于 **Spring Boot 3** 构建，提供 RESTful API、WebSocket 实时通信、AI 智能问答（DeepSeek）、文件管理等能力，为 Vue 3 前端提供数据支撑。

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行时 |
| Spring Boot | 3.1.12 | Web 框架，内嵌 Tomcat |
| MyBatis-Plus | 3.5.9 | ORM，Lambda 类型安全查询 |
| Sa-Token | 1.37.0 | 认证授权（JWT 模式 + Redis 共享） |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 分布式缓存 + Session 共享 + 限流 |
| Caffeine | 3.x | 本地内存缓存，Redis 不可用时自动降级 |
| RabbitMQ | 3.12 | 异步消息队列（日志、统计、通知等） |
| Spring WebSocket | 3.1.12 | AI 聊天流式双向通信 |
| Spring WebFlux | 3.1.12 | 响应式 HTTP 客户端（DeepSeek API 流式调用） |
| SpringDoc OpenAPI | 2.2.0 | Swagger API 文档自动生成 |
| Lombok | 1.18.38 | 减少样板代码 |
| BCrypt | spring-security-crypto | 密码不可逆加密 |
| dotenv-java | 3.0.0 | .env 文件加载 |
| H2 Database | test | 测试用内存数据库（MySQL 兼容模式） |

---

## 环境要求

| 依赖 | 最低版本 | 说明 |
|------|----------|------|
| JDK | 17+ | 推荐 Eclipse Temurin |
| MySQL | 8.0+ | 需支持全文索引 |
| Redis | 7.0+ | 可选，不可用时自动降级为 Caffeine |
| RabbitMQ | 3.x | 可选，可通过 `APP_RABBITMQ_ENABLED=false` 关闭 |
| Maven | 3.8+ | 构建工具（或使用项目内 `mvnw`） |

---

## 快速开始

### 1. 克隆项目

```bash
git clone <repo-url>
cd dong-medicine-backend
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 填写必要配置（见下方[配置说明](#配置说明)）：

```env
DB_USERNAME=root
DB_PASSWORD=your_db_password
JWT_SECRET=your-secret-key-at-least-32-chars
ADMIN_INIT_PASSWORD=admin123456
DEEPSEEK_API_KEY=sk-xxx
```

### 3. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p dong_medicine < sql/dong_medicine.sql
```

### 4. 启动服务

```bash
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

### 5. 验证

| 服务 | 地址 |
|------|------|
| API 根路径 | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| 健康检查 | http://localhost:8080/actuator/health |

### 默认管理员账号

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | 环境变量 `ADMIN_INIT_PASSWORD` 的值（首次启动时由 `AdminDataInitializer` 自动创建） |

---

## Docker 部署

项目根目录提供 `docker-compose.yml`，一键启动全部 6 个服务：

```bash
# 在项目根目录执行
cp .env.example .env   # 填写密钥等配置
docker compose up -d
```

### 服务清单

| 服务 | 容器名 | 端口（宿主） | 说明 |
|------|--------|-------------|------|
| MySQL 8.0 | dong-medicine-mysql | 3307 | 数据库，自动导入 sql 初始化脚本 |
| Redis 7 | dong-medicine-redis | 内部 | 缓存，AOF 持久化 |
| RabbitMQ 3.12 | dong-medicine-rabbitmq | 15672（管理界面） | 消息队列 |
| KKFileView 4.1 | dong-medicine-kkfileview | 内部 | 文档在线预览 |
| Backend | dong-medicine-backend | 8080 | Spring Boot 应用 |
| Frontend | dong-medicine-frontend | 80 | Nginx + Vue SPA |

后端服务依赖 MySQL、Redis、RabbitMQ 健康检查通过后才启动。所有容器通过 `dong-medicine-network` 桥接网络通信。

---

## 项目结构

```
dong-medicine-backend/
├── pom.xml                          # Maven 依赖配置
├── Dockerfile                       # Docker 构建文件
├── .env.example                     # 环境变量模板
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── sql/
│   └── dong_medicine.sql            # 建表语句 + 种子数据（50+ 种侗药）
├── public/                          # 文件上传存储根目录
│   ├── images/
│   ├── videos/
│   └── documents/
└── src/
    ├── main/
    │   ├── java/com/dongmedicine/
    │   │   ├── DongMedicineBackendApplication.java  # 启动入口
    │   │   ├── controller/          # 控制器层（REST API）
    │   │   │   └── admin/           #   管理后台接口（需 admin 角色）
    │   │   ├── service/             # 业务逻辑层
    │   │   │   └── impl/            #   Service 实现类
    │   │   ├── mapper/              # MyBatis-Plus Mapper 接口
    │   │   ├── entity/              # 实体类（继承 BaseEntity）
    │   │   ├── dto/                 # 数据传输对象（CreateDTO / UpdateDTO）
    │   │   ├── config/              # 配置类
    │   │   │   ├── health/          #   健康检查
    │   │   │   └── logging/         #   日志配置
    │   │   ├── mq/                  # RabbitMQ 消息队列
    │   │   │   ├── producer/        #   消息生产者（5 个）
    │   │   │   └── consumer/        #   消息消费者（5 个）
    │   │   ├── websocket/           # WebSocket 处理器（AI 聊天）
    │   │   └── common/              # 通用模块
    │   │       ├── constant/        #   常量定义
    │   │       ├── exception/       #   异常体系 + 全局异常处理
    │   │       └── util/            #   工具类（XSS、分页、密码校验等）
    │   └── resources/
    │       ├── application.yml      # 主配置
    │       ├── application-dev.yml  # 开发环境配置
    │       ├── application-prod.yml # 生产环境配置
    │       └── logback-spring.xml   # 日志配置
    └── test/                        # 测试代码
        ├── java/com/dongmedicine/
        │   ├── common/              # 工具类单元测试
        │   ├── service/impl/        # Service 层单元测试
        │   ├── controller/          # Controller 层单元测试
        │   ├── integration/         # 集成测试（H2 内存数据库）
        │   ├── regression/          # Bug 回归测试
        │   └── websocket/           # WebSocket 单元测试
        └── resources/               # 测试配置（H2、application-test.yml）
```

---

## API 文档

启动后访问 Swagger UI：**http://localhost:8080/swagger-ui/index.html**

### 主要接口模块

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 用户 | `/api/user` | 注册、登录、个人信息、修改密码 |
| 植物 | `/api/plants` | 列表、搜索、详情、随机推荐 |
| 知识 | `/api/knowledge` | 侗医药知识文章浏览 |
| 传承人 | `/api/inheritors` | 非遗传承人信息 |
| 资源 | `/api/resources` | 图片/视频/文档资源管理 |
| 评论 | `/api/comments` | 文章评论 |
| 收藏 | `/api/favorites` | 内容收藏/取消 |
| 测验 | `/api/quiz` | 知识问答题库、答题记录 |
| AI 聊天 | WebSocket `/ws/chat` | DeepSeek 流式对话 |
| 管理后台 | `/api/admin/*` | 用户/内容/互动/统计管理（需 admin 角色） |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "requestId": "a1b2c3d4e5f6"
}
```

---

## 测试

测试使用 H2 内存数据库（MySQL 兼容模式），无需外部 MySQL、Redis、RabbitMQ。

```bash
# 运行全部测试
./mvnw test -B

# 运行单个测试类
./mvnw test -B -Dtest=PlantServiceImplTest

# 运行单个测试方法
./mvnw test -B -Dtest=PlantServiceImplTest#testSearch
```

### 测试分层

| 层级 | 目录 | 说明 |
|------|------|------|
| 单元测试 | `common/`, `service/impl/`, `controller/` | 工具类、Service、Controller 逻辑 |
| 集成测试 | `integration/` | H2 内存数据库端到端验证 |
| 回归测试 | `regression/` | 已修复 Bug 的防止回退用例 |
| WebSocket 测试 | `websocket/` | 聊天 WebSocket 处理器单元测试 |

---

## 配置说明

### 环境变量（.env）

所有敏感配置通过环境变量注入，不硬编码在代码中。从 `.env.example` 复制后填写：

| 变量 | 说明 | 示例 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | 激活的 Profile | `dev` / `prod` |
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | `your_password` |
| `JWT_SECRET` | JWT 签名密钥（>=32 位） | `your-secret-key-32chars-min` |
| `JWT_EXPIRATION` | Token 过期时间（毫秒） | `86400000`（24 小时） |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | `sk-xxx` |
| `DEEPSEEK_BASE_URL` | DeepSeek API 地址 | `https://api.deepseek.com` |
| `ADMIN_INIT_PASSWORD` | 管理员初始密码（首次启动创建） | `admin123456` |
| `KKFILEVIEW_URL` | KKFileView 文档预览地址 | `http://localhost:8012` |
| `REDIS_HOST` | Redis 地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | （可选） |
| `RABBITMQ_HOST` | RabbitMQ 地址 | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ 端口 | `5672` |
| `RABBITMQ_USER` | RabbitMQ 用户名 | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 | `guest` |
| `APP_RABBITMQ_ENABLED` | 是否启用 RabbitMQ | `true` / `false` |
| `CORS_ALLOWED_ORIGIN` | CORS 允许的源 | `http://localhost:3000` |

### 数据库配置

连接地址格式：`jdbc:mysql://localhost:3306/dong_medicine?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai`

HikariCP 连接池默认配置：最大 20 连接，最小空闲 5，空闲超时 300s。

### Redis 配置

默认使用 `database 0`，超时 10s，Lettuce 连接池最大 8 连接。Redis 不可用时自动降级为 Caffeine 本地缓存（最大 1000 条，默认 60 分钟过期）。

### 缓存 TTL

| 缓存名 | 过期时间 | 说明 |
|--------|----------|------|
| `plants` / `knowledges` / `inheritors` | 6 小时 | 核心数据 |
| `resources` | 4 小时 | 资源列表 |
| `users` | 30 分钟 | 用户信息 |
| `quizQuestions` | 12 小时 | 题库 |
| `searchResults` | 5 分钟 | 搜索结果 |
| `hotData` | 1 小时 | 热门数据 |

---

## CI/CD

项目使用 GitHub Actions 实现自动化流水线（`.github/workflows/ci-cd.yml`）：

```
push/PR to main  ──>  测试  ──>  构建 & 推送镜像  ──>  SSH 部署
```

### 流水线阶段

| 阶段 | 触发条件 | 说明 |
|------|----------|------|
| **Test Backend** | push / PR | JDK 17，运行 `./mvnw test -B` |
| **Test Frontend** | push / PR | Node.js 20，运行 `npm run test` |
| **Build & Push** | push / dispatch（测试通过后） | Docker Buildx 构建前后端镜像，推送至 GHCR |
| **Deploy** | main/master 分支或 tag（构建通过后） | SSH 到服务器，拉取镜像，`docker compose up -d` |

部署脚本会等待 MySQL/Redis/RabbitMQ 健康检查通过后再启动后端，后端就绪后再启动前端。支持回滚（旧容器保留，`docker compose down` 时自动清理）。

### 部署脚本

`deploy/` 目录提供辅助脚本：

| 脚本 | 说明 |
|------|------|
| `docker-deploy.sh` | Docker 部署（含回滚支持） |
| `init-server.sh` | 服务器初始化（安装 Docker 等） |
| `cleanup.sh` | 清理无用 Docker 资源 |

---

## 安全特性

| 组件 | 说明 |
|------|------|
| **Sa-Token + JWT** | 路由拦截认证，所有 POST/PUT/DELETE/PATCH 请求需登录，GET 请求默认放行 |
| **RBAC 角色控制** | `@SaCheckRole("admin")` 注解保护管理后台接口，角色查询结果本地缓存 5 分钟 |
| **BCrypt 密码加密** | 密码不可逆加密存储，使用 `spring-security-crypto` |
| **XSS 防护** | `XssFilter` 过滤请求中的危险标签，30+ 种模式正则匹配 |
| **接口限流** | `RateLimitAspect` 基于 Redis Lua 脚本，本地令牌桶降级 |
| **请求体大小限制** | `RequestSizeFilter` 限制请求体大小（默认 10MB） |
| **SQL 注入防护** | MyBatis-Plus 参数化查询 + `PageUtils.escapeLike()` 转义 LIKE 特殊字符 |
| **CORS 白名单** | 仅允许配置的源访问，防止跨域滥用 |
| **请求追踪** | `RequestIdFilter` 为每个请求生成 16 位 UUID，贯穿日志和响应 |
| **Swagger 生产禁用** | `application-prod.yml` 中默认关闭 Swagger 文档 |

# 侗乡医药数字展示平台

> 基于 **Vue 3 + Spring Boot** 的前后端分离毕业设计项目 —— 数字化展示侗族传统医药非物质文化遗产

[![CI/CD](https://github.com/yhe3603-star/dong-medicine-platform/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/yhe3603-star/dong-medicine-platform/actions/workflows/ci-cd.yml)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.12-green)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.4-brightgreen)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue)](#许可证)

---

## 目录

- [项目简介](#项目简介)
- [核心功能](#核心功能)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [环境搭建](#环境搭建)
- [Docker 部署](#docker-部署)
- [API 文档](#api-文档)
- [测试](#测试)
- [CI/CD](#cicd)
- [安全机制](#安全机制)
- [数据库设计](#数据库设计)
- [监控与运维](#监控与运维)
- [常见问题](#常见问题)
- [学习路线](#学习路线)
- [许可证](#许可证)

---

## 项目简介

侗乡医药数字展示平台是一个面向侗族传统医药文化的数字化展示系统，旨在通过现代 Web 技术将侗族医药这一非物质文化遗产进行数字化保存、展示和传播。平台提供药用植物图鉴、侗医药知识库、非遗传承人介绍、互动答题、AI 智能问答等功能，为用户提供沉浸式的侗医药文化体验。

### 项目亮点

- **前后端分离架构**：Vue 3 SPA + Spring Boot RESTful API，独立开发部署
- **两级缓存体系**：Caffeine L1 本地缓存 + Redis L2 分布式缓存，自动降级
- **异步消息队列**：RabbitMQ 处理操作日志、热度计算、文件处理等异步任务
- **AI 智能问答**：WebSocket 实时流式通信 + DeepSeek API，支持 Markdown 渲染
- **全面安全防护**：Sa-Token + JWT 认证、XSS/SQL 注入防护、CORS 白名单、CSP/HSTS 安全头
- **容器化部署**：Docker Compose 8 服务编排，GitHub Actions CI/CD 自动化
- **监控体系**：Prometheus + Grafana 可视化监控

---

## 核心功能

### 用户端

| 模块 | 功能说明 |
|------|---------|
| **首页** | 平台概览、热门内容推荐、快速导航 |
| **植物图鉴** | 50+ 种侗族药用植物详情浏览、搜索筛选、相似推荐 |
| **知识库** | 侗医药理论知识、疗法介绍、分类检索 |
| **非遗传承人** | 传承人档案展示、技艺介绍、成就记录 |
| **资源中心** | 视频教程、文档资料在线预览与下载 |
| **互动答题** | 侗医药知识竞答、答题记录、排行榜 |
| **植物游戏** | 药用植物识别互动游戏 |
| **AI 智能问答** | 基于 DeepSeek 的实时流式 AI 问答 |
| **个人中心** | 收藏管理、评论管理、浏览历史、学习统计、反馈提交 |
| **数据可视化** | 植物分布、用药频次、知识统计等 ECharts 图表 |
| **全局搜索** | 跨内容类型全文搜索 |

### 管理后台

| 模块 | 功能说明 |
|------|---------|
| **数据仪表盘** | 平台运营数据概览、趋势分析 |
| **内容管理** | 植物/知识/传承人/资源/题库的 CRUD |
| **用户管理** | 用户列表、角色设置、封禁/解封 |
| **互动管理** | 评论审核、反馈处理、举报管理 |
| **操作日志** | 系统操作审计日志查看与导出 |

---

## 技术架构

### 整体架构

```
                        ┌─────────────────────┐
                        │    用户浏览器 / 移动端   │
                        └──────────┬──────────┘
                                   │
                        ┌──────────▼──────────┐
                        │   Nginx (80/443)    │
                        │   反向代理 + 静态资源  │
                        └──┬───────┬───────┬──┘
                           │       │       │
              ┌────────────┘       │       └────────────┐
              ▼                    ▼                     ▼
     ┌────────────────┐  ┌────────────────┐   ┌────────────────┐
     │  Vue 3 SPA     │  │ Spring Boot    │   │  KKFileView    │
     │  (静态文件)      │  │ (8080)         │   │  文档预览服务    │
     └────────────────┘  └───┬────┬────┬──┘   └────────────────┘
                             │    │    │
              ┌──────────────┤    │    ├──────────────┐
              ▼              ▼    │    ▼              ▼
        ┌──────────┐  ┌──────────┐│┌──────────┐ ┌──────────┐
        │ MySQL 8  │  │ Redis 7  │││ RabbitMQ │ │ DeepSeek │
        │ 持久存储   │  │ 缓存     │││ 消息队列  │ │ AI API   │
        └──────────┘  └──────────┘│└──────────┘ └──────────┘
                                  │
                          ┌───────▼───────┐
                          │  Prometheus   │
                          │  + Grafana    │
                          │  监控看板      │
                          └───────────────┘
```

### 请求处理流程

```
用户操作 → Vue 组件 → Axios HTTP 请求
    → Nginx 反向代理（/api/* → 后端）
    → Sa-Token 拦截器（认证鉴权）
    → Controller（参数校验）
    → Service（业务逻辑 + 两级缓存）
    → MyBatis-Plus → MySQL
    → R<T> 统一响应 → JSON → Vue 组件渲染
```

### 后端分层架构

```
Controller 层    ← 接收请求、参数校验、返回 R<T>
    ↓
Service 层       ← 业务逻辑、缓存策略、事务管理
    ↓
Mapper 层        ← MyBatis-Plus 数据访问
    ↓
Entity 层        ← 数据库表映射（@TableName、@TableField）
```

---

## 项目结构

```
dong-medicine-platform/
│
├── dong-medicine-frontend/        # 前端项目 (Vue 3 + Vite)
│   ├── src/
│   │   ├── views/                 # 页面组件 (14 个路由页面)
│   │   ├── components/            # 组件
│   │   │   ├── base/              #   基础通用组件
│   │   │   ├── common/            #   通用业务组件
│   │   │   └── business/          #   业务组件 (~35 个，barrel 导出)
│   │   ├── composables/           # 组合式函数 (20 个)
│   │   ├── router/                # 路由配置 (14 路由 + 守卫)
│   │   ├── stores/                # Pinia 状态管理
│   │   ├── utils/                 # 工具函数 (请求/缓存/XSS/图表等)
│   │   ├── styles/                # 样式 (CSS 变量 + SCSS + Element Plus 覆盖)
│   │   ├── directives/            # 8 个自定义指令 (懒加载/防抖/节流等)
│   │   └── App.vue                # 根组件 (登录弹窗/状态注入)
│   ├── e2e/                       # Playwright E2E 测试 (9 个 spec)
│   ├── __tests__/                 # Vitest 单元测试
│   ├── package.json
│   └── vite.config.js
│
├── dong-medicine-backend/         # 后端项目 (Spring Boot 3.1)
│   ├── src/main/java/com/dongmedicine/
│   │   ├── controller/            # 控制器 (27 个，含 admin/ 子包)
│   │   ├── service/               # 服务层 (业务逻辑)
│   │   │   └── impl/              #   服务实现
│   │   ├── mapper/                # 数据访问层 (MyBatis-Plus)
│   │   ├── entity/                # 实体类 (18 个，含 BaseEntity)
│   │   ├── dto/                   # 数据传输对象 (33 个)
│   │   ├── config/                # 配置类 (27 个)
│   │   │   ├── health/            #   健康检查指示器
│   │   │   └── logging/           #   日志脱敏
│   │   ├── mq/                    # RabbitMQ 消息队列
│   │   │   ├── producer/          #   5 个消息生产者
│   │   │   └── consumer/          #   5 个消息消费者
│   │   ├── websocket/             # WebSocket (AI 聊天)
│   │   └── common/                # 通用模块
│   │       ├── constant/          #   常量 (API路径/MQ/Roles/TargetType)
│   │       ├── exception/         #   异常体系 + 全局异常处理
│   │       └── util/              #   工具类 (XSS/分页/密码/文件等)
│   ├── src/test/                  # 测试代码 (80 个测试类)
│   │   ├── common/                #   公共模块测试
│   │   ├── controller/            #   控制器测试
│   │   ├── service/impl/          #   服务实现测试
│   │   ├── integration/           #   集成测试 (H2)
│   │   ├── regression/            #   回归测试
│   │   ├── performance/           #   性能测试
│   │   └── websocket/             #   WebSocket 测试
│   ├── sql/                       # 数据库脚本 (建表 + 种子数据)
│   └── pom.xml
│
├── deploy/                        # 部署脚本与监控配置
│   ├── docker-deploy.sh           # Docker 部署脚本 (含回滚)
│   ├── init-server.sh             # 服务器初始化
│   ├── cleanup.sh                 # Docker 资源清理
│   ├── setup-ssl.sh               # HTTPS 证书申请
│   ├── backup-db.sh               # 数据库备份 (保留 7 天)
│   └── monitoring/                # Prometheus + Grafana 配置
│
├── .github/workflows/ci-cd.yml    # CI/CD 流水线
├── docker-compose.yml             # Docker 编排 (8 个服务)
├── .env.example                   # 环境变量模板
└── CLAUDE.md                      # 项目开发指南
```

---

## 环境搭建

### 前置要求

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Node.js | 18+ | 前端构建工具 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| Maven | 可选（项目自带 mvnw） | 后端构建 |

### 快速开始

#### 1. 克隆项目

```bash
git clone https://github.com/yhe3603-star/dong-medicine-platform.git
cd dong-medicine-platform
```

#### 2. 初始化数据库

```bash
mysql -u root -p < dong-medicine-backend/sql/dong_medicine.sql
```

> 首次启动后端时 Flyway 也会自动执行迁移脚本，此步骤可选。

#### 3. 配置环境变量

```bash
cd dong-medicine-backend
cp .env.example .env
# 编辑 .env，设置 DB_PASSWORD、JWT_SECRET 等
```

#### 4. 启动后端

```bash
cd dong-medicine-backend
./mvnw spring-boot:run                    # Linux/macOS
# mvnw.cmd spring-boot:run               # Windows
```

启动成功后访问 http://localhost:8080/swagger-ui/index.html 查看 API 文档。

#### 5. 启动前端

```bash
cd dong-medicine-frontend
npm install
npm run dev
```

访问 http://localhost:5173 查看前端页面。

### 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | 前端开发服务器 |
| http://localhost:8080/swagger-ui/index.html | Swagger API 文档 |
| http://localhost:8080/actuator/health | 后端健康检查 |

### 默认管理员账号

管理员账号由 `AdminDataInitializer` 在首次启动时自动创建，密码由环境变量 `ADMIN_INIT_PASSWORD` 决定。

---

## Docker 部署

### 一键启动

```bash
cp .env.example .env
# 编辑 .env 填入配置

docker compose up -d
docker compose ps    # 查看服务状态
```

### 服务列表

| 服务 | 容器名 | 宿主机端口 | 资源限制 |
|------|--------|-----------|---------|
| MySQL 8.0 | dong-medicine-mysql | 3307 | 384M / 0.8 CPU |
| Redis 7 | dong-medicine-redis | - | 128M / 0.3 CPU |
| RabbitMQ 3.12 | dong-medicine-rabbitmq | 5672, 15672 | 512M / 0.5 CPU |
| KKFileView 4.1 | dong-medicine-kkfileview | - | 512M / 1 CPU |
| Spring Boot 后端 | dong-medicine-backend | 8080 | 512M / 1 CPU |
| Nginx 前端 | dong-medicine-frontend | 80, 443 | 128M / 0.3 CPU |
| Prometheus 2.52 | dong-medicine-prometheus | - | 128M / 0.2 CPU |
| Grafana 11 | dong-medicine-grafana | 3001 | 128M / 0.2 CPU |

> 所有服务通过 `dong-medicine-network` 桥接网络通信。连接 MySQL 使用宿主机端口 3307，Grafana 默认账号 `admin/admin`。

### 部署脚本

```bash
deploy/setup-ssl.sh <域名>    # 申请 Let's Encrypt HTTPS 证书
deploy/backup-db.sh            # 数据库备份 (保留 7 天)
deploy/docker-deploy.sh        # Docker 部署 (含回滚)
deploy/cleanup.sh              # Docker 资源清理
```

---

## API 文档

### Swagger UI

启动后端后访问：http://localhost:8080/swagger-ui/index.html

### 主要 API 模块

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 用户 | `/api/user` | 登录、注册、个人信息、密码修改 |
| 植物 | `/api/plants` | 植物列表、搜索、详情、相似推荐、随机 |
| 知识 | `/api/knowledges` | 知识列表、搜索、筛选、统计 |
| 传承人 | `/api/inheritors` | 传承人列表、搜索、热门 |
| 资源 | `/api/resources` | 资源列表、搜索、预览、下载、上传 |
| 问答 | `/api/qa` | QA 列表、搜索、分类、反馈 |
| 答题 | `/api/quiz` | 随机题目、提交答案、答题记录、排行榜 |
| 植物游戏 | `/api/plant-game` | 游戏提交、游戏记录 |
| 评论 | `/api/comments` | 发表、回复、点赞、举报 |
| 收藏 | `/api/favorites` | 收藏/取消收藏、我的收藏 |
| 反馈 | `/api/feedback` | 提交反馈、我的反馈 |
| 搜索 | `/api/search` | 跨类型全文搜索 |
| AI 聊天 | `/api/chat` | 消息发送（WebSocket 流式响应） |
| 数据可视化 | `/api/visual` | 植物分布、用药频次、知识统计 |
| 管理后台 | `/api/admin/*` | 仪表盘、内容/用户/互动管理、操作日志 |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { },
  "requestId": "a1b2c3d4"
}
```

状态码：`200` 成功 | `401` 未登录 | `403` 无权限 | `404` 未找到 | `500` 服务器错误

---

## 测试

### 后端测试

项目包含 **80 个测试类**，覆盖多层次：

| 层次 | 目录 | 说明 |
|------|------|------|
| 单元测试 | `common/`、`service/impl/`、`controller/` | 工具类、业务逻辑、控制器 |
| 集成测试 | `integration/` | H2 内存数据库集成测试 |
| 回归测试 | `regression/` | Bug 回归验证 |
| 性能测试 | `performance/` | 接口响应时间、并发测试 |
| WebSocket 测试 | `websocket/` | AI 聊天 WebSocket 单元测试 |

```bash
cd dong-medicine-backend
./mvnw test -B                              # 运行全部测试
./mvnw test -B -Dtest=ClassName             # 运行单个测试类
./mvnw test -B -Dtest=ClassName#method      # 运行单个测试方法
```

测试使用 H2 内存数据库（MySQL 兼容模式），无需外部依赖。

### 前端测试

```bash
cd dong-medicine-frontend
npm run test           # Vitest 监听模式
npm run test:run       # Vitest 单次运行
npm run test:coverage  # 测试覆盖率报告
npm run test:e2e       # Playwright E2E 测试
npm run lint           # ESLint 代码检查
```

E2E 测试覆盖 9 个功能领域：认证、导航、页面加载、API 调用、搜索、AI 聊天、WebSocket、响应式布局、回归。

---

## CI/CD

GitHub Actions 流水线自动执行：

```
push/PR → test-backend + test-frontend (并行)
    → E2E 测试 (push only)
    → 安全扫描 (Trivy) + SonarQube 代码分析
    → 构建 Docker 镜像 → 推送至 GHCR
    → SSH 部署至生产服务器
```

流水线特性：
- 后端 JaCoCo 覆盖率报告
- 前端 npm 安全审计
- Playwright 测试报告自动上传（失败时）
- Docker 构建缓存加速
- 生产部署含健康检查和自动回滚

---

## 安全机制

| 机制 | 实现方式 |
|------|---------|
| **认证** | Sa-Token + JWT，支持踢人下线、同端互斥、自动续期 |
| **授权** | `@SaCheckRole("admin")` 注解 + 路由守卫 |
| **XSS 防护** | 后端 `XssFilter` + 前端 DOMPurify |
| **SQL 注入** | MyBatis-Plus 参数化查询 |
| **CORS** | 白名单配置，不使用通配符 |
| **安全头** | CSP、HSTS、Permissions-Policy |
| **验证码** | 5 位字母数字混合，忽略大小写验证 |
| **速率限制** | `@RateLimit` 注解，Redis 优先 + 本地令牌桶降级 |
| **敏感数据** | 日志脱敏 (`SensitiveDataConverter`)、敏感词过滤 |
| **请求限制** | `RequestSizeFilter` 限制请求体大小 |

---

## 数据库设计

16 张核心表，按领域划分：

| 领域 | 表名 | 说明 |
|------|------|------|
| **用户** | `users` | 用户账号、角色、状态 |
| **内容** | `plants` | 药用植物信息 |
| | `knowledge` | 侗医药知识文章 |
| | `inheritors` | 非遗传承人档案 |
| | `resources` | 视频/文档/图片资源 |
| | `qa` | 问答条目 |
| **互动** | `comments` | 评论（支持回复，多态 target） |
| | `comment_likes` | 评论点赞 |
| | `favorites` | 收藏（多态 target） |
| | `feedback` | 用户反馈 |
| **答题** | `quiz_questions` | 题库 |
| | `quiz_record` | 答题记录 |
| | `plant_game_record` | 植物游戏记录 |
| **追踪** | `operation_log` | 操作审计日志 |
| | `browse_history` | 浏览历史 |
| | `chat_history` | AI 聊天记录 |
| | `search_history` | 搜索历史 |

所有表继承 `BaseEntity`（自增 `id`、`created_at`、`updated_at` 自动填充）。评论、收藏、浏览历史使用 `target_type` + `target_id` 实现多态关联。

---

## 监控与运维

### Prometheus + Grafana

- Prometheus 采集后端 Actuator 指标（JVM、HTTP、缓存命中率等）
- Grafana 提供可视化看板，访问 http://localhost:3001
- 配置文件位于 `deploy/monitoring/`

### 健康检查

后端提供 3 个自定义健康指示器：
- `DatabaseHealthIndicator` — MySQL 连接状态
- `RedisHealthIndicator` — Redis 连接状态
- `CacheHealthIndicator` — 两级缓存状态

访问 http://localhost:8080/actuator/health 查看。

### 日志

- Logback 日志配置，支持按日期和大小滚动
- 生产环境日志挂载至 Docker volume `backend_logs`
- 敏感数据自动脱敏

---

## 常见问题

### 后端启动失败

| 错误 | 原因 | 解决 |
|------|------|------|
| `Communications link failure` | MySQL 未启动或密码错误 | 检查 MySQL 服务和 `.env` 中的 `DB_PASSWORD` |
| `Unable to connect to Redis` | Redis 未启动 | 检查 Redis 服务和密码配置 |
| `JWT secret is not secure` | JWT 密钥太短 | `.env` 中设置 64 字符以上的 `JWT_SECRET` |
| `Port 8080 already in use` | 端口被占用 | 关闭占用进程或修改 `BACKEND_PORT` |

### 前端启动失败

| 错误 | 原因 | 解决 |
|------|------|------|
| `npm ERR! code ERESOLVE` | 依赖冲突 | 删除 `node_modules` 和 `package-lock.json`，重新 `npm install` |
| 页面空白 | 后端未启动 | 先启动后端，前端代理需要后端运行 |
| 404 API 请求 | 代理配置错误 | 检查 `vite.config.js` 中的代理配置 |

### Docker 部署问题

| 问题 | 解决 |
|------|------|
| 容器启动后立即退出 | `docker compose logs <service>` 查看日志 |
| 数据库连接超时 | 等待 MySQL 健康检查通过，或增加 `start_period` |
| 内存不足 | 调整 `.env` 中的 `JAVA_OPTS` 和 Docker 资源限制 |

---

## 学习路线

### 第一周：理解整体架构

1. 阅读本 README，建立项目全景认知
2. 阅读 [后端 README](dong-medicine-backend/README.md)，理解三层架构
3. 阅读 [前端 README](dong-medicine-frontend/README.md)，理解 Vue 组件化

### 第二周：理解后端

1. [entity/](dong-medicine-backend/src/main/java/com/dongmedicine/entity/README.md) — 数据模型
2. [mapper/](dong-medicine-backend/src/main/java/com/dongmedicine/mapper/README.md) — 数据访问
3. [service/](dong-medicine-backend/src/main/java/com/dongmedicine/service/README.md) — 业务逻辑
4. [controller/](dong-medicine-backend/src/main/java/com/dongmedicine/controller/README.md) — API 设计

### 第三周：理解前端

1. [router/](dong-medicine-frontend/src/router/README.md) — 路由与守卫
2. [stores/](dong-medicine-frontend/src/stores/README.md) — 状态管理
3. [utils/](dong-medicine-frontend/src/utils/README.md) — 请求封装与缓存
4. [views/](dong-medicine-frontend/src/views/README.md) — 页面开发

### 第四周：理解安全与部署

1. [config/](dong-medicine-backend/src/main/java/com/dongmedicine/config/README.md) — Sa-Token 认证
2. [deploy/](deploy/README.md) — Docker 部署与 CI/CD

### 各目录文档导航

| 目录 | 文档 |
|------|------|
| 后端主目录 | [dong-medicine-backend/README.md](dong-medicine-backend/README.md) |
| 后端源码 | [src/.../dongmedicine/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/README.md) |
| 后端配置 | [config/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/config/README.md) |
| 后端控制器 | [controller/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/controller/README.md) |
| 后端服务层 | [service/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/service/README.md) |
| 后端数据层 | [mapper/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/mapper/README.md) |
| 后端实体 | [entity/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/entity/README.md) |
| 数据库 | [sql/README.md](dong-medicine-backend/sql/README.md) |
| 前端主目录 | [dong-medicine-frontend/README.md](dong-medicine-frontend/README.md) |
| 前端源码 | [src/README.md](dong-medicine-frontend/src/README.md) |
| 前端页面 | [views/README.md](dong-medicine-frontend/src/views/README.md) |
| 前端工具 | [utils/README.md](dong-medicine-frontend/src/utils/README.md) |
| 部署 | [deploy/README.md](deploy/README.md) |

---

## 许可证

本项目仅用于毕业设计学习交流。

# 侗乡医药数字展示平台

> 一个基于 Vue 3 + Spring Boot 的侗族医药文化遗产数字化展示平台

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)
- [CI/CD 自动部署](#ci-cd-自动部署)
- [API 文档](#api-文档)
- [数据库设计](#数据库设计)
- [安全机制](#安全机制)
- [性能优化](#性能优化)
- [常见问题](#常见问题)

---

## 项目简介

本项目是一个毕业设计项目，旨在通过数字化手段保护和传承侗族医药文化遗产。平台提供了药用植物展示、非遗传承人介绍、知识库、互动问答等功能模块，让用户能够深入了解侗族传统医药文化。

### 核心价值

- **文化传承**：数字化保存侗族传统医药知识
- **知识普及**：让更多人了解侗族医药文化
- **互动学习**：通过游戏化方式增加学习趣味性
- **智能问答**：AI 助手解答侗医药相关问题

---

## 功能特性

### 用户端功能

| 模块 | 功能 | 说明 |
|------|------|------|
| **首页** | 数据概览 | 展示平台统计数据、传承人风采、快速导航 |
| **药用植物** | 植物图鉴 | 展示侗族传统药用植物，支持分类筛选、搜索、收藏 |
| **传承人** | 传承人档案 | 展示各级非遗传承人信息、技艺特色、代表案例 |
| **知识库** | 文献资料 | 侗医药知识条目，支持分类浏览、在线预览 |
| **问答社区** | 知识问答 | 用户提问、AI 智能回答、互动交流 |
| **学习资源** | 多媒体资源 | 视频、文档、图片资源库，支持在线预览下载 |
| **互动专区** | 游戏化学习 | 趣味答题、植物识别游戏、评论交流 |
| **数据可视化** | 统计图表 | ECharts 图表展示平台数据统计 |
| **个人中心** | 用户管理 | 个人信息、收藏管理、答题记录、账号设置 |
| **意见反馈** | 反馈系统 | 用户反馈提交与记录查看 |

### 管理端功能

| 模块 | 功能 |
|------|------|
| **仪表盘** | 数据统计概览 |
| **用户管理** | 用户列表、角色分配、状态管理 |
| **内容管理** | 植物、知识、传承人、问答、资源管理 |
| **互动管理** | 评论审核、反馈处理 |
| **系统管理** | 操作日志、题库管理 |

---

## 技术架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户浏览器                               │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Nginx 反向代理 (Docker)                     │
│   - 静态资源服务 (前端 dist)                                    │
│   - API 请求转发 (后端 8080)                                    │
└─────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│    前端 (Vue 3 + Vite)     │   │   后端 (Spring Boot 3)    │
│   - Element Plus UI       │   │   - Spring Security       │
│   - Pinia 状态管理         │   │   - MyBatis Plus 3.5.9    │
│   - Vue Router 路由        │   │   - JWT 认证              │
│   - Axios HTTP 请求        │   │   - Redis + Caffeine 缓存 │
│   - ECharts 图表           │   │   - DeepSeek AI 集成      │
└───────────────────────────┘   └───────────────────────────┘
                                            │
                                ┌───────────┴───────────┐
                                ▼                       ▼
                    ┌───────────────────┐   ┌───────────────────┐
                    │   MySQL 8.0       │   │   Redis 7         │
                    │   (Docker容器)    │   │   (Docker容器)    │
                    └───────────────────┘   └───────────────────┘
```

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 渐进式 JavaScript 框架，采用 Composition API |
| Vite | 5.0+ | 下一代前端构建工具，开发启动快 |
| Vue Router | 4.2+ | 官方路由管理器，支持路由守卫 |
| Pinia | 2.3+ | Vue 3 状态管理，替代 Vuex |
| Element Plus | 2.4+ | Vue 3 UI 组件库 |
| Axios | 1.6+ | HTTP 客户端，支持拦截器 |
| ECharts | 5.4+ | 数据可视化图表库 |
| Vitest | 1.0+ | 单元测试框架 |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.12 | Java Web 框架 |
| Spring Security | - | 安全框架，处理认证授权 |
| MyBatis Plus | 3.5.9 | ORM 框架，简化数据库操作 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 高性能缓存数据库 |
| Caffeine | 3.1+ | 高性能本地缓存 |
| JWT (jjwt) | 0.11.5 | JSON Web Token 认证 |
| SpringDoc | 2.2.0 | API 文档生成 (Swagger) |

### DevOps 技术栈

| 技术 | 说明 |
|------|------|
| Docker | 容器化部署 |
| Docker Compose | 多容器编排 |
| GitHub Actions | CI/CD 自动化 |
| Nginx | 反向代理、静态资源服务 |

---

## 项目结构

```
Graduation Project/
│
├── .github/workflows/                  # GitHub Actions
│   └── ci-cd.yml                       # CI/CD 工作流
│
├── deploy/                             # 部署配置
│   ├── ci-deploy.sh                    # CI 部署脚本
│   ├── docker-deploy.sh                # Docker 部署脚本
│   ├── init-server.sh                  # 服务器初始化脚本
│   ├── nginx.conf                      # Nginx 配置
│   └── dong-medicine-backend.service   # Systemd 服务
│
├── dong-medicine-backend/              # 后端项目
│   ├── src/main/java/com/dongmedicine/
│   │   ├── common/                     # 公共模块
│   │   │   ├── constant/               # 常量定义
│   │   │   │   └── RoleConstants.java  # 角色常量
│   │   │   ├── exception/              # 异常处理
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── util/                   # 工具类
│   │   │   │   ├── FileCleanupHelper.java
│   │   │   │   ├── FileTypeUtils.java
│   │   │   │   ├── PageUtils.java      # 分页 + LIKE转义
│   │   │   │   ├── PasswordValidator.java
│   │   │   │   ├── SensitiveDataUtils.java
│   │   │   │   └── XssUtils.java
│   │   │   ├── R.java                  # 统一响应封装(含requestId)
│   │   │   └── SecurityUtils.java      # 安全工具类
│   │   │
│   │   ├── config/                     # 配置模块
│   │   │   ├── health/                 # 健康检查
│   │   │   │   ├── CacheHealthIndicator.java
│   │   │   │   ├── DatabaseHealthIndicator.java
│   │   │   │   └── RedisHealthIndicator.java
│   │   │   ├── logging/                # 日志配置
│   │   │   │   └── SensitiveDataConverter.java
│   │   │   ├── AdminDataInitializer.java
│   │   │   ├── AppProperties.java
│   │   │   ├── AsyncConfig.java
│   │   │   ├── CacheConfig.java        # Redis + Caffeine 缓存
│   │   │   ├── CustomUserDetails.java
│   │   │   ├── DeepSeekConfig.java     # AI 配置
│   │   │   ├── FileUploadProperties.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtSecretValidator.java
│   │   │   ├── JwtUtil.java
│   │   │   ├── LoggingAspect.java
│   │   │   ├── MybatisPlusConfig.java
│   │   │   ├── OpenApiConfig.java
│   │   │   ├── OperationLogAspect.java
│   │   │   ├── RateLimit.java          # 限流注解
│   │   │   ├── RateLimitAspect.java    # 限流切面(含本地令牌桶)
│   │   │   ├── RequestIdFilter.java    # 请求追踪ID
│   │   │   ├── RequestSizeFilter.java  # 请求大小限制
│   │   │   ├── SecurityConfig.java
│   │   │   ├── SecurityConfigValidator.java
│   │   │   ├── StartupInfoPrinter.java
│   │   │   ├── WebMvcConfig.java
│   │   │   └── XssFilter.java
│   │   │
│   │   ├── controller/                 # 控制器层 (17个)
│   │   │   ├── AdminController.java
│   │   │   ├── CaptchaController.java  # 验证码
│   │   │   ├── ChatController.java     # AI聊天
│   │   │   ├── CommentController.java
│   │   │   ├── FavoriteController.java
│   │   │   ├── FeedbackController.java
│   │   │   ├── FileUploadController.java
│   │   │   ├── InheritorController.java
│   │   │   ├── KnowledgeController.java
│   │   │   ├── LeaderboardController.java
│   │   │   ├── OperationLogController.java
│   │   │   ├── PlantController.java
│   │   │   ├── PlantGameController.java
│   │   │   ├── QaController.java
│   │   │   ├── QuizController.java
│   │   │   ├── ResourceController.java
│   │   │   └── UserController.java
│   │   │
│   │   ├── dto/                        # 数据传输对象 (18个)
│   │   ├── entity/                     # 实体类 (13个)
│   │   ├── mapper/                     # 数据访问层 (13个)
│   │   ├── service/                    # 服务层
│   │   │   ├── impl/                   # 服务实现 (15个)
│   │   │   └── *Service.java           # 服务接口
│   │   └── DongMedicineBackendApplication.java
│   │
│   ├── src/main/resources/
│   │   ├── application.yml             # 主配置文件
│   │   ├── application-dev.yml         # 开发环境配置
│   │   ├── application-prod.yml        # 生产环境配置
│   │   └── logback-spring.xml          # 日志配置
│   │
│   ├── sql/                            # 数据库脚本
│   │   ├── dong_medicine.sql           # 完整数据库
│   │   ├── fulltext_index.sql          # 全文索引
│   │   └── optimize_indexes.sql        # 索引优化
│   │
│   ├── public/                         # 静态资源目录
│   │   ├── images/                     # 图片资源
│   │   │   ├── plants/                 # 植物图片 (70+张)
│   │   │   ├── inheritors/             # 传承人图片
│   │   │   ├── knowledge/              # 知识图片
│   │   │   └── common/                 # 公共图片
│   │   └── documents/                  # 文档资源
│   │       ├── xlsx/                   # Excel文档
│   │       └── common/                 # 公共文档
│   │
│   ├── Dockerfile
│   └── pom.xml
│
├── dong-medicine-frontend/             # 前端项目
│   ├── src/
│   │   ├── views/                      # 页面组件 (15个)
│   │   │   ├── Home.vue
│   │   │   ├── Plants.vue
│   │   │   ├── Inheritors.vue
│   │   │   ├── Knowledge.vue
│   │   │   ├── Qa.vue
│   │   │   ├── Resources.vue
│   │   │   ├── Interact.vue
│   │   │   ├── Visual.vue
│   │   │   ├── PersonalCenter.vue
│   │   │   ├── Admin.vue
│   │   │   ├── About.vue
│   │   │   ├── Feedback.vue
│   │   │   ├── GlobalSearch.vue
│   │   │   └── NotFound.vue
│   │   │
│   │   ├── components/                 # 组件目录
│   │   │   ├── base/                   # 基础组件
│   │   │   │   ├── ErrorBoundary.vue
│   │   │   │   └── VirtualList.vue
│   │   │   ├── common/                 # 通用组件
│   │   │   │   └── SkeletonGrid.vue
│   │   │   └── business/               # 业务组件
│   │   │       ├── layout/             # 布局组件
│   │   │       ├── display/            # 展示组件
│   │   │       ├── interact/           # 交互组件
│   │   │       ├── media/              # 媒体组件
│   │   │       ├── upload/             # 上传组件
│   │   │       ├── dialogs/            # 详情对话框
│   │   │       └── admin/              # 管理后台组件
│   │   │
│   │   ├── composables/                # 组合式函数 (11个)
│   │   ├── router/                     # 路由配置
│   │   ├── stores/                     # 状态管理
│   │   ├── utils/                      # 工具函数
│   │   ├── styles/                     # 样式文件
│   │   ├── config/                     # 配置文件
│   │   ├── directives/                 # 自定义指令
│   │   └── __tests__/                  # 测试文件
│   │
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── default.conf
│   └── package.json
│
├── docker-compose.yml                  # Docker Compose 配置
├── .env.example                        # 环境变量示例
└── README.md                           # 项目文档
```

---

## 快速开始

### 环境要求

| 环境 | 版本要求 | 检查命令 |
|------|----------|----------|
| JDK | 17+ | `java -version` |
| Node.js | 18+ | `node -v` |
| MySQL | 8.0+ | `mysql --version` |
| Redis | 7.0+ | `redis-cli --version` |
| Maven | 3.6+ | `mvn -v` |
| npm | 9+ | `npm -v` |

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yhe3603-star/Graduation-Project.git
cd Graduation-Project
```

#### 2. 配置数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
USE dong_medicine;
SOURCE dong-medicine-backend/sql/dong_medicine.sql;

# 创建全文索引（可选，提升搜索性能）
SOURCE dong-medicine-backend/sql/fulltext_index.sql;
```

#### 3. 启动后端

```bash
cd dong-medicine-backend

# 方式1：使用 Maven（推荐开发环境）
./mvnw spring-boot:run

# 方式2：打包后运行
./mvnw clean package -DskipTests
java -jar target/dong-medicine-backend-1.0.0.jar
```

后端服务地址：http://localhost:8080

#### 4. 启动前端

```bash
cd dong-medicine-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务地址：http://localhost:5173

---

## Docker 部署

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+

### 快速部署

#### 1. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置（修改密码等敏感信息）
vim .env
```

`.env` 文件配置说明：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=your_root_password    # MySQL root密码
MYSQL_USER=dongmedicine                   # 应用数据库用户
MYSQL_PASSWORD=your_password              # 应用数据库密码
MYSQL_PORT=3306                           # MySQL端口

# Redis配置
REDIS_PASSWORD=your_redis_password        # Redis密码
REDIS_PORT=6379                           # Redis端口

# JWT配置（至少64字符，含大小写字母、数字、特殊字符）
JWT_SECRET=YourStrongJwtSecretKey123!@#MustBe64Chars...
JWT_EXPIRATION=86400000                   # Token有效期(毫秒)

# 服务端口
BACKEND_PORT=8080                         # 后端端口
FRONTEND_PORT=3000                       # 前端端口

# CORS配置
CORS_ALLOWED_ORIGIN=http://your-domain.com

# AI配置（可选）
DEEPSEEK_API_KEY=sk-xxx
```

#### 2. 一键启动

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 3. 访问服务

- **前端**: http://localhost
- **后端 API**: http://localhost:8080/api/
- **Swagger 文档**: http://localhost:8080/swagger-ui/
- **健康检查**: http://localhost:8080/actuator/health

### Docker 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 重新构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 进入容器
docker exec -it dong-medicine-backend sh
docker exec -it dong-medicine-mysql mysql -u root -p

# 清理无用镜像
docker image prune -f
```

---

## CI/CD 自动部署

本项目已配置 GitHub Actions 自动部署，推送代码到 `main` 分支即可自动触发部署。

### 部署流程图

```
代码推送 → GitHub Actions
              │
    ┌─────────┴─────────┐
    ▼                   ▼
构建后端            构建前端
(Java 17)          (Node 20)
    │                   │
    └─────────┬─────────┘
              ▼
         SSH 部署到服务器
              │
    ┌─────────┴─────────┐
    ▼                   ▼
检查/安装Docker     上传部署文件
              │
              ▼
    ┌─────────────────────┐
    │   Docker Compose    │
    │   - 构建镜像        │
    │   - 启动容器        │
    │   - 健康检查        │
    └─────────────────────┘
              │
              ▼
           部署完成
```

### GitHub Secrets 配置

在 GitHub 仓库 `Settings` → `Secrets and variables` → `Actions` 中配置：

| Secret 名称 | 说明 | 示例 |
|------------|------|------|
| `SERVER_HOST` | 服务器 IP 地址 | `192.168.1.100` |
| `SERVER_USER` | SSH 用户名 | `root` |
| `SSH_PRIVATE_KEY` | SSH 私钥内容 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | `YourRootPassword123!` |
| `MYSQL_USER` | MySQL 用户名 | `dongmedicine` |
| `MYSQL_PASSWORD` | MySQL 用户密码 | `YourPassword123!` |
| `REDIS_PASSWORD` | Redis 密码 | `YourRedisPassword123!` |
| `JWT_SECRET` | JWT 密钥（64+字符） | `YourStrongJwtSecretKey...` |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | `sk-xxx...` |

---

## API 文档

启动后端服务后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui/index.html
```

### 主要 API 端点

| 模块 | 端点 | 方法 | 说明 |
|------|------|------|------|
| **用户** | `/api/user/login` | POST | 用户登录 |
| | `/api/user/register` | POST | 用户注册 |
| | `/api/user/me` | GET | 获取用户信息 |
| **验证码** | `/api/captcha/image` | GET | 获取图形验证码 |
| **植物** | `/api/plants/list` | GET | 植物列表 |
| | `/api/plants/{id}` | GET | 植物详情 |
| **知识** | `/api/knowledge/list` | GET | 知识列表 |
| | `/api/knowledge/{id}` | GET | 知识详情 |
| **传承人** | `/api/inheritors/list` | GET | 传承人列表 |
| **资源** | `/api/resources/list` | GET | 资源列表 |
| | `/api/resources/download/{id}` | GET | 下载资源 |
| **收藏** | `/api/favorites/my` | GET | 我的收藏 |
| | `/api/favorites/{type}/{id}` | POST | 添加收藏 |
| **评论** | `/api/comments/list/{type}/{id}` | GET | 评论列表 |
| | `/api/comments` | POST | 发表评论 |
| **测验** | `/api/quiz/questions` | GET | 获取题目 |
| | `/api/quiz/submit` | POST | 提交答案 |
| **AI** | `/api/chat` | POST | AI 问答 |

### 统一响应格式

所有 API 返回统一的 JSON 格式：

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "requestId": "a1b2c3d4e5f6g7h8"
}
```

| 字段 | 说明 |
|------|------|
| `code` | 状态码，200 表示成功 |
| `msg` | 消息描述 |
| `data` | 响应数据 |
| `requestId` | 请求追踪ID，用于日志排查 |

---

## 数据库设计

### 核心业务表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `users` | 用户表 | id, username, password_hash, role, status |
| `plants` | 药用植物表 | id, name_cn, name_dong, category, efficacy |
| `knowledge` | 知识库表 | id, title, type, therapy_category, content |
| `inheritors` | 传承人表 | id, name, level, bio, specialties |
| `resources` | 学习资源表 | id, title, category, files |
| `qa` | 问答表 | id, question, answer, category |

### 交互功能表

| 表名 | 说明 |
|------|------|
| `comments` | 评论表，支持嵌套回复 |
| `favorites` | 收藏表，支持多种资源类型 |
| `feedback` | 用户反馈表 |
| `quiz_questions` | 测验题目表 |
| `quiz_record` | 测验记录表 |
| `plant_game_record` | 植物游戏记录表 |
| `operation_log` | 操作日志表 |

### 索引优化

项目提供全文索引脚本，支持中文分词搜索：

```sql
-- 执行全文索引脚本
SOURCE sql/fulltext_index.sql;
```

全文索引覆盖的表：
- `plants` - 植物搜索
- `knowledge` - 知识搜索
- `qa` - 问答搜索
- `inheritors` - 传承人搜索
- `resources` - 资源搜索

---

## 安全机制

### 1. 认证与授权

| 机制 | 说明 |
|------|------|
| JWT Token | 无状态认证，支持自动刷新 |
| Token 黑名单 | 用户登出后 Token 立即失效（Caffeine本地缓存降级） |
| 角色权限 | USER / ADMIN 角色区分 |
| 用户状态检查 | 封禁用户无法访问受保护资源 |

### 2. 密码安全

```java
// BCrypt 加密存储
String hashedPassword = passwordEncoder.encode(rawPassword);

// 密码规则验证
// - 长度 8-50 位
// - 必须包含字母和数字
// - 不能包含空格
```

### 3. XSS 防护

前端和后端双重防护，覆盖 30+ 危险模式：

```javascript
// 前端 XSS 检测
const XSS_PATTERNS = [
  /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
  /javascript\s*:/gi,
  /on\w+\s*=/gi,
  /&#x?[0-9a-f]+;?/gi,  // HTML实体编码
  // ... 更多模式
]
```

### 4. SQL 注入防护

```java
// LIKE 查询特殊字符转义
public static String escapeLike(String keyword) {
    return keyword
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
}
```

### 5. 请求限流

| 接口 | 限制 | 说明 |
|------|------|------|
| 登录 | 5次/分钟 | 防止暴力破解 |
| 注册 | 3次/分钟 | 防止恶意注册 |
| AI聊天 | 10次/秒 | 防止API滥用 |
| 通用接口 | 60次/分钟 | 全局限流 |

**降级策略**：Redis 不可用时自动降级到本地令牌桶算法。

### 6. 文件上传安全

| 安全措施 | 说明 |
|----------|------|
| 文件类型校验 | 白名单机制，只允许指定类型 |
| 文件大小限制 | 图片10MB、视频100MB、文档50MB |
| 路径遍历防护 | 禁止 `..` 等危险路径 |
| 文件名清理 | 移除特殊字符，防止命令注入 |

### 7. 请求追踪

每个请求分配唯一 `requestId`，便于日志追踪：

```
2024-01-15 10:30:45.123 [a1b2c3d4e5f6g7h8] INFO  c.d.controller.UserController - 用户登录成功
```

### 8. 敏感信息保护

生产环境自动脱敏：

| 信息类型 | 脱敏规则 |
|----------|----------|
| 手机号 | 138****8888 |
| 邮箱 | ab***@example.com |
| 身份证 | 110***********1234 |
| JWT Token | 显示前后各10位 |
| SQL参数 | 密码替换为 `***` |

---

## 性能优化

### 1. 多级缓存

```
请求 → Caffeine本地缓存 → Redis分布式缓存 → MySQL数据库
```

| 缓存层 | 技术 | 用途 |
|--------|------|------|
| L1 缓存 | Caffeine | 本地高速缓存，毫秒级响应 |
| L2 缓存 | Redis | 分布式缓存，支持集群 |

### 2. 缓存策略

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 最大缓存数量 | 1000 | 防止内存溢出 |
| 默认过期时间 | 60分钟 | 平衡性能与数据新鲜度 |
| 访问后过期 | 60分钟 | 热点数据自动续期 |

### 3. 数据库优化

| 优化项 | 说明 |
|--------|------|
| 全文索引 | 支持中文分词搜索 |
| 组合索引 | 优化常用查询条件 |
| 连接池 | HikariCP 高性能连接池 |
| 分页查询 | 防止大数据量查询 |

### 4. 前端优化

| 优化项 | 说明 |
|--------|------|
| 路由懒加载 | 按需加载页面组件 |
| 虚拟列表 | 大数据列表性能优化 |
| 图片懒加载 | 延迟加载可视区域图片 |
| 请求防抖 | 防止重复请求 |

---

## 常见问题

### 1. Docker 镜像拉取失败

配置 Docker 镜像加速器（Docker Desktop → Settings → Docker Engine）：

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me"
  ]
}
```

### 2. 后端启动失败

**检查数据库连接**：
```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'dong_medicine';"
```

**检查端口占用**：
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### 3. JWT 密钥验证失败

生产环境 JWT 密钥要求：
- 至少 64 个字符
- 包含大写字母、小写字母、数字、特殊字符
- 不能包含 "secret" 等常见单词

### 4. 前端启动失败

**清除依赖重装**：
```bash
rm -rf node_modules package-lock.json
npm install
```

### 5. 容器健康检查失败

```bash
# 查看容器日志
docker-compose logs backend

# 进入容器排查
docker exec -it dong-medicine-backend sh

# 手动健康检查
curl http://localhost:8080/actuator/health
```

### 6. Redis 连接失败

**检查 Redis 状态**：
```bash
redis-cli ping
```

**降级说明**：Redis 不可用时，系统会自动降级到本地缓存，核心功能不受影响。

---

## 许可证

本项目仅供学习和研究使用。

---

**最后更新时间**：2026年3月30日

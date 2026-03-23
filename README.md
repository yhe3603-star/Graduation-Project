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
│   - Pinia 状态管理         │   │   - MyBatis Plus          │
│   - Vue Router 路由        │   │   - JWT 认证              │
│   - Axios HTTP 请求        │   │   - Redis 缓存            │
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

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.12 | Java Web 框架 |
| Spring Security | - | 安全框架，处理认证授权 |
| MyBatis Plus | 3.5.9 | ORM 框架，简化数据库操作 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 高性能缓存数据库 |
| JWT | 0.11.5 | JSON Web Token 认证 |
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
├── dong-medicine-backend/              # 后端项目
│   ├── src/main/java/com/dongmedicine/
│   │   ├── common/                     # 公共模块
│   │   │   ├── exception/              # 自定义异常
│   │   │   └── util/                   # 工具类
│   │   ├── config/                     # 配置模块
│   │   ├── controller/                 # 控制器层
│   │   ├── service/                    # 服务层
│   │   ├── mapper/                     # 数据访问层
│   │   ├── entity/                     # 实体类
│   │   └── dto/                        # 数据传输对象
│   ├── src/main/resources/
│   │   ├── application.yml             # 主配置文件
│   │   ├── application-dev.yml         # 开发环境配置
│   │   └── application-prod.yml        # 生产环境配置
│   ├── sql/                            # 数据库脚本
│   ├── public/                         # 静态资源目录
│   ├── Dockerfile                      # Docker 构建文件
│   └── pom.xml                         # Maven 配置
│
├── dong-medicine-frontend/             # 前端项目
│   ├── src/
│   │   ├── views/                      # 页面组件
│   │   ├── components/                 # 业务组件
│   │   ├── composables/                # 组合式函数
│   │   ├── router/                     # 路由配置
│   │   ├── stores/                     # 状态管理
│   │   ├── utils/                      # 工具函数
│   │   └── styles/                     # 样式文件
│   ├── Dockerfile                      # Docker 构建文件
│   ├── nginx.conf                      # Nginx 配置
│   └── package.json                    # 项目配置
│
├── deploy/                             # 部署配置
│   ├── docker-deploy.sh                # Docker 部署脚本
│   ├── init-server.sh                  # 服务器初始化脚本
│   ├── nginx.conf                      # Nginx 配置
│   └── dong-medicine-backend.service   # Systemd 服务
│
├── .github/workflows/                  # GitHub Actions
│   └── ci-cd.yml                       # CI/CD 工作流
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
FRONTEND_PORT=80                          # 前端端口

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

### 生成 SSH 密钥

```bash
# 生成密钥对
ssh-keygen -t ed25519 -C "github-actions" -f github-actions-key

# 将公钥添加到服务器
ssh-copy-id -i github-actions-key.pub root@你的服务器IP

# 将私钥内容复制到 GitHub Secrets (SSH_PRIVATE_KEY)
cat github-actions-key
```

### 触发部署

```bash
# 方式1：推送代码自动触发
git add .
git commit -m "feat: 更新功能"
git push origin main

# 方式2：创建版本标签
git tag v1.0.0
git push origin v1.0.0

# 方式3：手动触发（GitHub Actions 页面点击 "Run workflow"）
```

### 服务器常用命令

```bash
# 查看服务状态
cd /opt/dong-medicine && docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 手动重新部署
cd /tmp/deploy && sudo ./docker-deploy.sh

# 查看备份
ls -la /opt/dong-medicine/backups/
```

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

---

## 数据库设计

### 核心业务表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `users` | 用户表 | id, username, password_hash, role |
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

---

## 开发团队

- 开发者：毕业设计项目组

## 许可证

本项目仅供学习和研究使用。

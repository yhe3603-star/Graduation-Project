# 侗乡医药数字展示平台

> 一个基于 Vue 3 + Spring Boot 的侗族医药文化遗产数字化展示平台

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [环境配置](#环境配置)
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
│                      Nginx 反向代理                              │
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
│   - Axios HTTP 请求        │   │   - Caffeine 缓存         │
└───────────────────────────┘   └───────────────────────────┘
                                            │
                                            ▼
                                ┌───────────────────────────┐
                                │      MySQL 8.0 数据库      │
                                │   - 13 张业务表            │
                                │   - 用户、植物、知识等      │
                                └───────────────────────────┘
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
| JWT | 0.11.5 | JSON Web Token 认证 |
| Caffeine | - | 高性能本地缓存 |
| SpringDoc | 2.2.0 | API 文档生成 (Swagger) |

---

## 项目结构

```
Graduation Project/
│
├── dong-medicine-backend/              # 后端项目
│   ├── src/main/java/com/dongmedicine/
│   │   ├── common/                     # 公共模块
│   │   │   ├── exception/              # 自定义异常
│   │   │   │   ├── BusinessException.java    # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   └── util/                   # 工具类
│   │   │       ├── XssUtils.java       # XSS 防护工具
│   │   │       └── IpUtils.java        # IP 获取工具
│   │   │
│   │   ├── config/                     # 配置模块
│   │   │   ├── SecurityConfig.java     # Spring Security 配置
│   │   │   ├── JwtUtil.java            # JWT 工具类
│   │   │   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   │   │   ├── CacheConfig.java        # 缓存配置
│   │   │   ├── CorsConfig.java         # 跨域配置
│   │   │   ├── RateLimitAspect.java    # 限流切面
│   │   │   └── OperationLogAspect.java # 操作日志切面
│   │   │
│   │   ├── controller/                 # 控制器层 (16个)
│   │   │   ├── UserController.java     # 用户接口
│   │   │   ├── PlantController.java    # 药用植物接口
│   │   │   ├── KnowledgeController.java # 知识库接口
│   │   │   ├── InheritorController.java # 传承人接口
│   │   │   ├── ResourceController.java # 学习资源接口
│   │   │   ├── CommentController.java  # 评论接口
│   │   │   ├── FavoriteController.java # 收藏接口
│   │   │   ├── FeedbackController.java # 反馈接口
│   │   │   ├── QuizController.java     # 测验接口
│   │   │   ├── PlantGameController.java # 植物游戏接口
│   │   │   ├── AiChatController.java   # AI 聊天接口
│   │   │   ├── LeaderboardController.java # 排行榜接口
│   │   │   ├── UploadController.java   # 文件上传接口
│   │   │   └── AdminController.java    # 管理后台接口
│   │   │
│   │   ├── service/                    # 服务层接口 (14个)
│   │   │   └── impl/                   # 服务层实现 (14个)
│   │   │
│   │   ├── mapper/                     # 数据访问层 (13个)
│   │   │
│   │   ├── entity/                     # 实体类 (13个)
│   │   │   ├── User.java               # 用户实体
│   │   │   ├── Plant.java              # 药用植物实体
│   │   │   ├── Knowledge.java          # 知识库实体
│   │   │   ├── Inheritor.java          # 传承人实体
│   │   │   ├── Resource.java           # 学习资源实体
│   │   │   ├── Comment.java            # 评论实体
│   │   │   ├── Favorite.java           # 收藏实体
│   │   │   ├── Feedback.java           # 反馈实体
│   │   │   ├── Qa.java                 # 问答实体
│   │   │   ├── QuizQuestion.java       # 测验题目实体
│   │   │   ├── QuizRecord.java         # 测验记录实体
│   │   │   ├── PlantGameRecord.java    # 植物游戏记录实体
│   │   │   └── OperationLog.java       # 操作日志实体
│   │   │
│   │   └── dto/                        # 数据传输对象 (11个)
│   │
│   ├── src/main/resources/
│   │   ├── application.yml             # 主配置文件
│   │   ├── application-dev.yml         # 开发环境配置
│   │   ├── application-prod.yml        # 生产环境配置
│   │   └── logback-spring.xml          # 日志配置
│   │
│   ├── sql/                            # 数据库脚本
│   │   ├── dong_medicine.sql           # 完整数据库脚本
│   │   └── update_*.sql                # 更新脚本
│   │
│   ├── public/                         # 静态资源目录
│   │   ├── images/                     # 图片资源
│   │   ├── videos/                     # 视频资源
│   │   └── documents/                  # 文档资源
│   │
│   └── pom.xml                         # Maven 配置
│
├── dong-medicine-frontend/             # 前端项目
│   ├── src/
│   │   ├── views/                      # 页面组件 (13个)
│   │   │   ├── Home.vue                # 首页
│   │   │   ├── Plants.vue              # 药用植物
│   │   │   ├── Inheritors.vue          # 传承人
│   │   │   ├── Knowledge.vue           # 知识库
│   │   │   ├── Qa.vue                  # 问答社区
│   │   │   ├── Resources.vue           # 学习资源
│   │   │   ├── Interact.vue            # 互动专区
│   │   │   ├── Visual.vue              # 数据可视化
│   │   │   ├── PersonalCenter.vue      # 个人中心
│   │   │   ├── Admin.vue               # 管理后台
│   │   │   ├── About.vue               # 关于页面
│   │   │   ├── Feedback.vue            # 意见反馈
│   │   │   └── GlobalSearch.vue        # 全局搜索
│   │   │
│   │   ├── components/                 # 组件目录
│   │   │   ├── base/                   # 基础组件
│   │   │   │   └── ErrorBoundary.vue   # 错误边界
│   │   │   │
│   │   │   └── business/               # 业务组件 (60+)
│   │   │       ├── layout/             # 布局组件
│   │   │       │   ├── AppHeader.vue   # 顶部导航
│   │   │       │   └── AppFooter.vue   # 底部版权
│   │   │       │
│   │   │       ├── display/            # 展示组件
│   │   │       │   ├── CardGrid.vue    # 卡片网格
│   │   │       │   ├── ChartCard.vue   # 图表卡片
│   │   │       │   ├── Pagination.vue  # 分页组件
│   │   │       │   └── SearchFilter.vue # 搜索过滤
│   │   │       │
│   │   │       ├── interact/           # 交互组件
│   │   │       │   ├── CommentSection.vue # 评论区域
│   │   │       │   ├── PlantGame.vue   # 植物识别游戏
│   │   │       │   └── QuizSection.vue # 趣味答题
│   │   │       │
│   │   │       ├── media/              # 媒体组件
│   │   │       │   ├── ImageCarousel.vue # 图片轮播
│   │   │       │   ├── VideoPlayer.vue # 视频播放
│   │   │       │   └── DocumentPreview.vue # 文档预览
│   │   │       │
│   │   │       ├── upload/             # 上传组件
│   │   │       │   ├── ImageUploader.vue
│   │   │       │   ├── VideoUploader.vue
│   │   │       │   └── DocumentUploader.vue
│   │   │       │
│   │   │       ├── dialogs/            # 详情对话框
│   │   │       │   ├── PlantDetailDialog.vue
│   │   │       │   ├── KnowledgeDetailDialog.vue
│   │   │       │   └── InheritorDetailDialog.vue
│   │   │       │
│   │   │       └── admin/              # 管理后台组件
│   │   │           ├── AdminDashboard.vue
│   │   │           ├── AdminSidebar.vue
│   │   │           ├── AdminDataTable.vue
│   │   │           ├── dialogs/        # 详情对话框
│   │   │           └── forms/          # 表单对话框
│   │   │
│   │   ├── composables/                # 组合式函数
│   │   │   ├── useAdminData.js         # 管理后台数据
│   │   │   ├── useQuiz.js              # 答题逻辑
│   │   │   ├── usePlantGame.js         # 游戏逻辑
│   │   │   ├── useInteraction.js       # 交互功能
│   │   │   ├── useFavorite.js          # 收藏功能
│   │   │   └── useMedia.js             # 媒体处理
│   │   │
│   │   ├── router/                     # 路由配置
│   │   │   └── index.js                # 路由定义 + 守卫
│   │   │
│   │   ├── stores/                     # 状态管理
│   │   │   └── user.js                 # 用户状态
│   │   │
│   │   ├── utils/                      # 工具函数
│   │   │   ├── index.js                # 工具函数导出
│   │   │   ├── request.js              # Axios 封装
│   │   │   ├── media.js                # 媒体工具
│   │   │   ├── xss.js                  # XSS 防护
│   │   │   └── logger.js               # 日志工具
│   │   │
│   │   ├── styles/                     # 样式文件
│   │   │   ├── index.css               # 样式入口
│   │   │   ├── variables.css           # CSS 变量
│   │   │   ├── base.css                # 基础样式
│   │   │   ├── components.css          # 组件样式
│   │   │   └── pages.css               # 页面样式
│   │   │
│   │   ├── config/                     # 配置文件
│   │   │   └── index.js                # 应用配置
│   │   │
│   │   ├── App.vue                     # 根组件
│   │   └── main.js                     # 入口文件
│   │
│   ├── public/                         # 静态资源
│   ├── index.html                      # HTML 模板
│   ├── package.json                    # 项目配置
│   ├── vite.config.js                  # Vite 配置
│   └── vitest.config.js                # 测试配置
│
├── deploy/                             # 部署配置
│   ├── ci-deploy.sh                    # CI/CD 部署脚本
│   ├── nginx.conf                      # Nginx 配置
│   ├── dong-medicine-backend.service   # Systemd 服务
│   └── *.sql                           # 数据库脚本
│
├── .github/workflows/                  # GitHub Actions
│   └── ci-cd.yml                       # CI/CD 工作流
│
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

## 环境配置

### 后端环境变量

创建 `dong-medicine-backend/.env` 文件：

```bash
# 运行环境
SPRING_PROFILES_ACTIVE=prod

# 数据库配置
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT 配置（至少64字符）
JWT_SECRET=your-very-long-jwt-secret-key-must-be-at-least-64-characters-long
JWT_EXPIRATION=86400000

# CORS 配置
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080

# DeepSeek AI 配置
DEEPSEEK_API_KEY=sk-your-api-key
DEEPSEEK_BASE_URL=https://api.deepseek.com

# 管理员初始密码
ADMIN_INIT_PASSWORD=Admin@123

# 文件上传路径
FILE_UPLOAD_BASE_PATH=/opt/dong-medicine/backend/public
```

### 前端环境变量

创建 `dong-medicine-frontend/.env.local` 文件：

```bash
# API 基础路径
VITE_API_BASE_URL=http://localhost:8080/api
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
(Java 17)          (Node 18)
    │                   │
    └─────────┬─────────┘
              ▼
         SSH 部署到服务器
              │
    ┌─────────┴─────────┐
    ▼                   ▼
部署后端            部署前端
(JAR + Systemd)    (dist + Nginx)
              │
              ▼
         健康检查验证
              │
              ▼
           部署完成
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

# 方式3：手动触发（空提交）
git commit --allow-empty -m "chore: 触发部署"
git push origin main
```

### GitHub Secrets 配置

在 GitHub 仓库 `Settings` → `Secrets and variables` → `Actions` 中配置：

| Secret 名称 | 说明 |
|------------|------|
| `SERVER_HOST` | 服务器 IP 地址 |
| `SERVER_USER` | SSH 用户名 |
| `SSH_PRIVATE_KEY` | SSH 私钥 |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | JWT 密钥 |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 |
| `ADMIN_INIT_PASSWORD` | 管理员密码 |

### 服务器常用命令

```bash
# 查看后端日志
journalctl -u dong-medicine-backend -f

# 查看服务状态
systemctl status dong-medicine-backend

# 重启后端服务
systemctl restart dong-medicine-backend

# 重启 Nginx
systemctl restart nginx

# 查看备份
ls -la /opt/dong-medicine/backups/

# 回滚版本
cp /opt/dong-medicine/backups/dong-medicine-backend-YYYYMMDD_HHMMSS.jar /opt/dong-medicine/backend/
systemctl restart dong-medicine-backend
```

---

## API 文档

启动后端服务后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
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

### 1. 后端启动失败

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

### 2. 前端启动失败

**清除依赖重装**：
```bash
rm -rf node_modules package-lock.json
npm install
```

### 3. 登录 Token 失效

Token 有效期为 24 小时，过期后需要重新登录。

### 4. 文件上传失败

检查后端配置的文件上传路径是否存在且有写入权限：
```bash
mkdir -p /opt/dong-medicine/backend/public
chmod 755 /opt/dong-medicine/backend/public
```

---

## 开发团队

- 开发者：毕业设计项目组

## 许可证

本项目仅供学习和研究使用。

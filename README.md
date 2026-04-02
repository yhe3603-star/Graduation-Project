# 侗乡医药数字展示平台

> 一个基于 Vue 3 + Spring Boot 的侗族医药文化遗产数字化展示平台

## 目录

- [项目简介](#项目简介)
- [新手入门指南](#新手入门指南)
- [基础概念解释](#基础概念解释)
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
- [术语表](#术语表)
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

## 新手入门指南

### 我应该从哪里开始？

如果你是第一次接触这个项目，建议按照以下顺序学习：

```
第1步：了解基础概念 → 阅读下面的"基础概念解释"
第2步：理解项目架构 → 阅读"技术架构"和"项目结构"
第3步：搭建开发环境 → 按照"快速开始"操作
第4步：运行项目 → 启动前后端，访问网站
第5步：阅读代码 → 从简单模块开始，逐步深入
```

### 学习路线图

```
┌─────────────────────────────────────────────────────────────────┐
│                        学习路线图                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  前端学习路线:                                                   │
│  HTML/CSS基础 → JavaScript基础 → Vue 3基础 → 本项目前端代码       │
│                                                                 │
│  后端学习路线:                                                   │
│  Java基础 → Spring Boot基础 → MyBatis基础 → 本项目后端代码        │
│                                                                 │
│  数据库学习路线:                                                 │
│  SQL基础 → MySQL使用 → 数据库设计 → 本项目数据库结构               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 推荐学习资源

| 技术领域 | 推荐资源 | 链接 |
|---------|---------|------|
| Vue 3 | Vue.js 官方中文教程 | https://cn.vuejs.org/tutorial/ |
| Spring Boot | Spring Boot 官方文档 | https://spring.io/guides |
| MySQL | MySQL 教程 | https://www.runoob.com/mysql/mysql-tutorial.html |
| Docker | Docker 入门教程 | https://www.runoob.com/docker/docker-tutorial.html |

---

## 基础概念解释

### 什么是前端和后端？

想象一个餐厅：
- **前端**就像餐厅的**大厅和菜单**，顾客（用户）在这里点餐、看菜品展示
- **后端**就像餐厅的**厨房**，在这里处理订单、准备食物（数据）

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户（顾客）                              │
│                            ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    前端（大厅）                           │   │
│  │  - 网页界面展示                                          │   │
│  │  - 用户交互（点击、输入）                                 │   │
│  │  - 数据展示                                              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            ↓ HTTP请求                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    后端（厨房）                           │   │
│  │  - 处理业务逻辑                                          │   │
│  │  - 数据处理和存储                                        │   │
│  │  - API接口提供                                           │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  数据库（仓库）                           │   │
│  │  - 存储所有数据                                          │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 什么是 Vue 3？

**Vue** 是一个用于构建用户界面的 JavaScript 框架。

**通俗理解**：Vue 就像是一个"智能模板"，你可以告诉它"这里显示用户名"，当用户名变化时，它会自动更新显示，你不需要手动去修改。

**示例**：
```html
<!-- 传统方式：需要手动更新 -->
<div id="username"></div>
<script>
  document.getElementById('username').textContent = '张三'
  // 数据变化时需要再次手动更新
  document.getElementById('username').textContent = '李四'
</script>

<!-- Vue方式：自动更新 -->
<template>
  <div>{{ username }}</div>
</template>
<script setup>
import { ref } from 'vue'
const username = ref('张三')
// 修改数据，页面自动更新
username.value = '李四'
</script>
```

### 什么是 Spring Boot？

**Spring Boot** 是一个用于构建后端应用的 Java 框架。

**通俗理解**：Spring Boot 就像是一个"预制好的房子框架"，它已经帮你搭好了地基、墙壁、屋顶，你只需要往里面放家具（业务代码）就行了。

**示例**：
```java
// 一个最简单的Spring Boot应用
@RestController  // 告诉Spring这是一个处理网页请求的类
public class HelloController {
    
    @GetMapping("/hello")  // 当用户访问 /hello 时执行这个方法
    public String hello() {
        return "你好，世界！";  // 返回给用户的内容
    }
}
```

### 什么是数据库？

**数据库**是用于存储和管理数据的系统。

**通俗理解**：数据库就像一个超级强大的 Excel 表格，可以存储海量数据，并且支持快速查找、添加、修改、删除。

**本项目使用的数据库是 MySQL**，它是最流行的关系型数据库之一。

**示例**：
```sql
-- 创建一个用户表（类似Excel创建一个工作表）
CREATE TABLE users (
  id INT PRIMARY KEY,        -- 用户ID，主键（唯一标识）
  username VARCHAR(50),      -- 用户名，最多50个字符
  password VARCHAR(100),     -- 密码
  created_at DATETIME        -- 创建时间
);

-- 插入数据（类似Excel添加一行）
INSERT INTO users VALUES (1, '张三', '123456', '2024-01-01');

-- 查询数据（类似Excel筛选）
SELECT * FROM users WHERE username = '张三';
```

### 什么是 API？

**API**（Application Programming Interface，应用程序接口）是前后端通信的桥梁。

**通俗理解**：API 就像餐厅的"点菜单"，前端（顾客）看着菜单点菜，后端（厨房）根据菜单准备菜品。

**示例**：
```
前端发起请求：
GET /api/plants/list?page=1&size=10

后端返回数据：
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 1, "name": "钩藤", "efficacy": "清热平肝" },
    { "id": 2, "name": "透骨草", "efficacy": "祛风除湿" }
  ]
}
```

### 什么是 Docker？

**Docker** 是一种容器化技术，可以将应用及其依赖打包成一个独立的"容器"。

**通俗理解**：Docker 就像"集装箱"，把你的应用和它需要的所有东西打包在一起，这样无论在哪里运行都能保证环境一致。

**为什么需要 Docker？**
- 不用担心"在我电脑上能运行，在你电脑上不行"的问题
- 一键部署，不用手动配置环境
- 方便团队协作

### 什么是 JWT？

**JWT**（JSON Web Token）是一种用户身份认证方式。

**通俗理解**：JWT 就像一张"电子身份证"，用户登录后服务器发给他一张，之后每次请求都带上这张证，服务器就能识别是谁在请求。

**工作流程**：
```
1. 用户登录 → 服务器验证账号密码
2. 验证成功 → 服务器生成JWT Token返回给用户
3. 用户保存Token → 存储在浏览器中
4. 后续请求 → 自动带上Token
5. 服务器验证Token → 识别用户身份
```

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
│                    (Chrome/Edge/Firefox等)                       │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ 用户访问网站
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Nginx 反向代理 (Docker)                     │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  功能说明：                                               │   │
│   │  1. 静态资源服务 - 提供前端页面文件                        │   │
│   │  2. API 请求转发 - 将 /api/* 请求转发到后端               │   │
│   │  3. 负载均衡 - 分发请求到多个服务器                        │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
                    ▼                       ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│    前端 (Vue 3 + Vite)     │   │   后端 (Spring Boot 3)    │
│   ┌───────────────────┐   │   │   ┌───────────────────┐   │
│   │ Element Plus UI   │   │   │   │ Spring Security   │   │
│   │ - 按钮表格等组件   │   │   │   │ - 用户认证授权     │   │
│   ├───────────────────┤   │   │   ├───────────────────┤   │
│   │ Pinia 状态管理     │   │   │   │ MyBatis Plus      │   │
│   │ - 存储用户状态     │   │   │   │ - 数据库操作       │   │
│   ├───────────────────┤   │   │   ├───────────────────┤   │
│   │ Vue Router 路由    │   │   │   │ JWT 认证          │   │
│   │ - 页面跳转控制     │   │   │   │ - Token生成验证    │   │
│   ├───────────────────┤   │   │   ├───────────────────┤   │
│   │ Axios HTTP 请求    │   │   │   │ Redis + Caffeine  │   │
│   │ - 与后端通信       │   │   │   │ - 多级缓存         │   │
│   ├───────────────────┤   │   │   ├───────────────────┤   │
│   │ ECharts 图表       │   │   │   │ DeepSeek AI       │   │
│   │ - 数据可视化       │   │   │   │ - 智能问答         │   │
│   └───────────────────┘   │   │   └───────────────────┘   │
└───────────────────────────┘   └───────────────────────────┘
                                            │
                                ┌───────────┴───────────┐
                                │                       │
                                ▼                       ▼
                    ┌───────────────────┐   ┌───────────────────┐
                    │   MySQL 8.0       │   │   Redis 7         │
                    │   (Docker容器)    │   │   (Docker容器)    │
                    │   ┌───────────┐   │   │   ┌───────────┐   │
                    │   │ 存储用户   │   │   │   │ 存储缓存   │   │
                    │   │ 存储植物   │   │   │   │ 存储Session│   │
                    │   │ 存储知识   │   │   │   │ 存储Token  │   │
                    │   │ 存储评论   │   │   │   │ 黑名单     │   │
                    │   │ ...       │   │   │   └───────────┘   │
                    │   └───────────┘   │   │                   │
                    └───────────────────┘   └───────────────────┘
```

### 前端技术栈

| 技术 | 版本 | 说明 | 通俗解释 |
|------|------|------|----------|
| Vue | 3.4+ | 渐进式 JavaScript 框架 | 帮你构建网页应用的工具 |
| Vite | 5.0+ | 下一代前端构建工具 | 帮你打包和运行代码的工具 |
| Vue Router | 4.2+ | 官方路由管理器 | 控制页面跳转的工具 |
| Pinia | 2.3+ | Vue 3 状态管理 | 在组件间共享数据的工具 |
| Element Plus | 2.4+ | Vue 3 UI 组件库 | 提供现成的按钮、表格等组件 |
| Axios | 1.6+ | HTTP 客户端 | 与后端通信的工具 |
| ECharts | 5.4+ | 数据可视化图表库 | 绘制图表的工具 |
| Vitest | 1.0+ | 单元测试框架 | 测试代码的工具 |

### 后端技术栈

| 技术 | 版本 | 说明 | 通俗解释 |
|------|------|------|----------|
| Spring Boot | 3.1.12 | Java Web 框架 | 快速构建后端应用的工具 |
| Spring Security | - | 安全框架 | 处理用户登录权限的工具 |
| MyBatis Plus | 3.5.9 | ORM 框架 | 操作数据库的工具 |
| MySQL | 8.0+ | 关系型数据库 | 存储数据的地方 |
| Redis | 7.0+ | 高性能缓存数据库 | 快速存取临时数据的地方 |
| Caffeine | 3.1+ | 高性能本地缓存 | 内存中的快速缓存 |
| JWT (jjwt) | 0.11.5 | JSON Web Token | 用户身份认证工具 |
| SpringDoc | 2.2.0 | API 文档生成 | 自动生成API文档的工具 |

### DevOps 技术栈

| 技术 | 说明 | 通俗解释 |
|------|------|----------|
| Docker | 容器化部署 | 把应用打包成"集装箱" |
| Docker Compose | 多容器编排 | 管理多个"集装箱" |
| GitHub Actions | CI/CD 自动化 | 自动部署代码 |
| Nginx | 反向代理、静态资源服务 | 网站服务器 |

---

## 项目结构

```
Graduation Project/
│
├── .github/workflows/                  # GitHub Actions
│   └── ci-cd.yml                       # CI/CD 工作流配置
│
├── deploy/                             # 部署配置
│   ├── ci-deploy.sh                    # CI 部署脚本
│   ├── docker-deploy.sh                # Docker 部署脚本
│   ├── init-server.sh                  # 服务器初始化脚本
│   ├── nginx.conf                      # Nginx 配置
│   └── dong-medicine-backend.service   # Systemd 服务配置
│
├── dong-medicine-backend/              # 后端项目
│   │
│   ├── src/main/java/com/dongmedicine/
│   │   │
│   │   ├── common/                     # 公共模块
│   │   │   ├── constant/               # 常量定义
│   │   │   │   └── RoleConstants.java  # 角色常量
│   │   │   ├── exception/              # 异常处理
│   │   │   │   ├── BusinessException.java      # 业务异常类
│   │   │   │   ├── ErrorCode.java              # 错误码定义
│   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │   │   ├── util/                   # 工具类
│   │   │   │   ├── FileCleanupHelper.java      # 文件清理助手
│   │   │   │   ├── FileTypeUtils.java          # 文件类型工具
│   │   │   │   ├── PageUtils.java              # 分页工具
│   │   │   │   ├── PasswordValidator.java      # 密码验证器
│   │   │   │   ├── SensitiveDataUtils.java     # 敏感信息脱敏
│   │   │   │   └── XssUtils.java               # XSS防护工具
│   │   │   ├── R.java                  # 统一响应封装
│   │   │   └── SecurityUtils.java      # 安全工具类
│   │   │
│   │   ├── config/                     # 配置模块
│   │   │   ├── health/                 # 健康检查
│   │   │   ├── logging/                # 日志配置
│   │   │   ├── SecurityConfig.java     # Spring Security 配置
│   │   │   ├── JwtUtil.java            # JWT 工具类
│   │   │   ├── CacheConfig.java        # 缓存配置
│   │   │   └── ...                     # 其他配置类
│   │   │
│   │   ├── controller/                 # 控制器层 (17个)
│   │   │   ├── UserController.java     # 用户接口
│   │   │   ├── PlantController.java    # 植物接口
│   │   │   └── ...                     # 其他控制器
│   │   │
│   │   ├── dto/                        # 数据传输对象 (18个)
│   │   ├── entity/                     # 实体类 (13个)
│   │   ├── mapper/                     # 数据访问层 (13个)
│   │   ├── service/                    # 服务层
│   │   │   ├── impl/                   # 服务实现 (15个)
│   │   │   └── *Service.java           # 服务接口
│   │   └── DongMedicineBackendApplication.java  # 应用入口
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
│   │
│   ├── Dockerfile                      # Docker 构建文件
│   └── pom.xml                         # Maven 配置
│
├── dong-medicine-frontend/             # 前端项目
│   │
│   ├── src/
│   │   │
│   │   ├── views/                      # 页面组件 (14个)
│   │   │   ├── Home.vue                # 首页
│   │   │   ├── Plants.vue              # 药用植物页面
│   │   │   ├── Inheritors.vue          # 传承人页面
│   │   │   ├── Knowledge.vue           # 知识库页面
│   │   │   ├── Qa.vue                  # 问答社区页面
│   │   │   ├── Resources.vue           # 学习资源页面
│   │   │   ├── Interact.vue            # 互动专区页面
│   │   │   ├── Visual.vue              # 数据可视化页面
│   │   │   ├── PersonalCenter.vue      # 个人中心页面
│   │   │   ├── Admin.vue               # 管理后台页面
│   │   │   ├── About.vue               # 关于页面
│   │   │   ├── Feedback.vue            # 意见反馈页面
│   │   │   ├── GlobalSearch.vue        # 全局搜索页面
│   │   │   └── NotFound.vue            # 404页面
│   │   │
│   │   ├── components/                 # 组件目录
│   │   │   ├── base/                   # 基础组件
│   │   │   ├── common/                 # 通用组件
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
│   │   ├── App.vue                     # 根组件
│   │   └── main.js                     # 入口文件
│   │
│   ├── index.html                      # HTML 模板
│   ├── package.json                    # 项目配置
│   ├── vite.config.js                  # Vite 配置
│   ├── Dockerfile                      # Docker 构建文件
│   └── nginx.conf                      # Nginx 配置
│
├── docker-compose.yml                  # Docker Compose 配置
├── .env.example                        # 环境变量示例
└── README.md                           # 项目文档（本文件）
```

### 目录职责说明

| 目录 | 职责 | 通俗解释 |
|------|------|----------|
| `dong-medicine-backend/` | 后端代码 | 处理业务逻辑、数据存储 |
| `dong-medicine-frontend/` | 前端代码 | 用户界面、页面展示 |
| `deploy/` | 部署配置 | 服务器部署相关文件 |
| `sql/` | 数据库脚本 | 创建数据库、表的SQL语句 |

---

## 快速开始

### 环境要求

在开始之前，请确保你的电脑已安装以下软件：

| 软件 | 版本要求 | 检查命令 | 下载地址 |
|------|----------|----------|----------|
| JDK | 17+ | `java -version` | https://adoptium.net/ |
| Node.js | 18+ | `node -v` | https://nodejs.org/ |
| MySQL | 8.0+ | `mysql --version` | https://dev.mysql.com/downloads/ |
| Redis | 7.0+ | `redis-cli --version` | https://redis.io/download |
| Maven | 3.6+ | `mvn -v` | https://maven.apache.org/ |
| npm | 9+ | `npm -v` | 随 Node.js 安装 |

### 安装步骤

#### 1. 克隆项目

```bash
# 从GitHub下载项目代码
git clone https://github.com/yhe3603-star/Graduation-Project.git

# 进入项目目录
cd Graduation-Project
```

#### 2. 配置数据库

```bash
# 1. 登录 MySQL（会提示输入密码）
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 退出 MySQL
exit

# 4. 导入数据（在项目根目录执行）
mysql -u root -p dong_medicine < dong-medicine-backend/sql/dong_medicine.sql

# 5. 创建全文索引（可选，提升搜索性能）
mysql -u root -p dong_medicine < dong-medicine-backend/sql/fulltext_index.sql
```

#### 3. 启动后端

```bash
# 进入后端目录
cd dong-medicine-backend

# 方式1：使用 Maven 直接运行（推荐开发环境）
./mvnw spring-boot:run

# 方式2：打包后运行
./mvnw clean package -DskipTests
java -jar target/dong-medicine-backend-1.0.0.jar
```

**后端启动成功后，你会看到类似输出：**
```
Started DongMedicineBackendApplication in 5.123 seconds
```

后端服务地址：http://localhost:8080

#### 4. 启动前端

**打开新的命令行窗口：**

```bash
# 进入前端目录
cd dong-medicine-frontend

# 安装依赖（第一次运行需要）
npm install

# 启动开发服务器
npm run dev
```

**前端启动成功后，你会看到：**
```
  VITE v5.0.0  ready in 1234 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: http://192.168.1.100:5173/
```

前端服务地址：http://localhost:5173

#### 5. 访问网站

打开浏览器，访问 http://localhost:5173

**默认管理员账号：**
- 用户名：`admin`
- 密码：`admin123`

---

## Docker 部署

### 什么是 Docker 部署？

Docker 部署是将应用打包成"容器"运行的方式，优点是：
- 环境一致，不会出现"在我电脑上能运行"的问题
- 一键部署，无需手动配置环境
- 方便管理多个服务

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+

**检查命令：**
```bash
docker --version
docker-compose --version
```

### 快速部署

#### 1. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置（修改密码等敏感信息）
# Windows: notepad .env
# Mac/Linux: vim .env
```

`.env` 文件配置说明：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=your_root_password    # MySQL root密码（请修改）
MYSQL_USER=dongmedicine                   # 应用数据库用户
MYSQL_PASSWORD=your_password              # 应用数据库密码（请修改）
MYSQL_PORT=3306                           # MySQL端口

# Redis配置
REDIS_PASSWORD=your_redis_password        # Redis密码（请修改）
REDIS_PORT=6379                           # Redis端口

# JWT配置（至少64字符，含大小写字母、数字、特殊字符）
JWT_SECRET=YourStrongJwtSecretKey123!@#MustBe64Chars...
JWT_EXPIRATION=86400000                   # Token有效期(毫秒)，默认24小时

# 服务端口
BACKEND_PORT=8080                         # 后端端口
FRONTEND_PORT=3000                        # 前端端口

# CORS配置（允许的前端地址）
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

- **前端**: http://localhost:3000
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
docker-compose logs -f backend    # 查看后端日志
docker-compose logs -f frontend   # 查看前端日志

# 进入容器
docker exec -it dong-medicine-backend sh        # 进入后端容器
docker exec -it dong-medicine-mysql mysql -u root -p  # 进入MySQL

# 清理无用镜像
docker image prune -f
```

---

## CI/CD 自动部署

本项目已配置 GitHub Actions 自动部署，推送代码到 `main` 分支即可自动触发部署。

### 部署流程图

```
代码推送到GitHub → GitHub Actions 自动触发
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
| `code` | 状态码，200 表示成功，其他表示错误 |
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
| Token 黑名单 | 用户登出后 Token 立即失效 |
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

### 6. 文件上传安全

| 安全措施 | 说明 |
|----------|------|
| 文件类型校验 | 白名单机制，只允许指定类型 |
| 文件大小限制 | 图片10MB、视频100MB、文档50MB |
| 路径遍历防护 | 禁止 `..` 等危险路径 |
| 文件名清理 | 移除特殊字符，防止命令注入 |

---

## 性能优化

### 1. 多级缓存

```
请求 → Caffeine本地缓存 → Redis分布式缓存 → MySQL数据库
         (毫秒级)            (毫秒级)          (秒级)
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

## 术语表

### 前端术语

| 术语 | 英文 | 解释 |
|------|------|------|
| 组件 | Component | 可复用的UI单元，如按钮、表格等 |
| 路由 | Router | 控制页面跳转的机制 |
| 状态管理 | State Management | 管理应用中共享的数据 |
| 组合式函数 | Composable | Vue 3中复用逻辑的方式 |
| Props | Properties | 父组件传递给子组件的数据 |
| Emit | - | 子组件向父组件发送事件 |
| 懒加载 | Lazy Loading | 延迟加载，需要时才加载 |
| 骨架屏 | Skeleton Screen | 加载时显示的占位内容 |

### 后端术语

| 术语 | 英文 | 解释 |
|------|------|------|
| 控制器 | Controller | 接收HTTP请求的类 |
| 服务层 | Service | 处理业务逻辑的类 |
| 数据访问层 | Mapper/DAO | 操作数据库的类 |
| 实体类 | Entity | 对应数据库表的Java类 |
| DTO | Data Transfer Object | 数据传输对象 |
| ORM | Object-Relational Mapping | 对象关系映射 |
| JWT | JSON Web Token | 一种身份认证方式 |
| 缓存 | Cache | 临时存储，提高访问速度 |

### 数据库术语

| 术语 | 英文 | 解释 |
|------|------|------|
| 表 | Table | 存储数据的结构 |
| 字段 | Field/Column | 表中的一列 |
| 记录 | Record/Row | 表中的一行 |
| 主键 | Primary Key | 唯一标识一条记录的字段 |
| 外键 | Foreign Key | 关联其他表的字段 |
| 索引 | Index | 提高查询速度的结构 |
| SQL | Structured Query Language | 结构化查询语言 |

### DevOps术语

| 术语 | 英文 | 解释 |
|------|------|------|
| Docker | - | 容器化技术 |
| 容器 | Container | 独立运行的应用环境 |
| 镜像 | Image | 创建容器的模板 |
| CI/CD | Continuous Integration/Deployment | 持续集成/持续部署 |
| Nginx | - | 高性能Web服务器 |

---

## 常见问题

### 1. Docker 镜像拉取失败

**原因**：国内访问 Docker Hub 较慢

**解决方案**：配置 Docker 镜像加速器

Docker Desktop → Settings → Docker Engine：

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

**检查 Redis 连接**：
```bash
redis-cli ping
# 应该返回 PONG
```

### 3. 前端启动失败

**清除依赖重装**：
```bash
rm -rf node_modules package-lock.json
npm install
```

**检查 Node.js 版本**：
```bash
node -v
# 需要 18.0.0 以上
```

### 4. JWT 密钥验证失败

**原因**：生产环境 JWT 密钥要求较高

**要求**：
- 至少 64 个字符
- 包含大写字母、小写字母、数字、特殊字符
- 不能包含 "secret" 等常见单词

**示例**：
```
MySecretKey123!@#AbcDefGhiJklMnoPqrStuVwxYz0123456789!@#$%
```

### 5. 登录时提示"验证码错误"

**可能原因**：
1. 验证码已过期（5分钟有效期）
2. 验证码输入错误（区分大小写）
3. Redis 连接失败

**解决方案**：
- 刷新验证码重新输入
- 检查 Redis 是否正常运行

### 6. 容器健康检查失败

```bash
# 查看容器日志
docker-compose logs backend

# 进入容器排查
docker exec -it dong-medicine-backend sh

# 手动健康检查
curl http://localhost:8080/actuator/health
```

### 7. Redis 连接失败

**检查 Redis 状态**：
```bash
redis-cli ping
```

**降级说明**：Redis 不可用时，系统会自动降级到本地缓存，核心功能不受影响。

### 8. 文件上传失败

**可能原因**：
1. 文件大小超过限制
2. 文件类型不支持
3. 磁盘空间不足

**解决方案**：
- 图片限制：10MB
- 视频限制：100MB
- 文档限制：50MB

---

## 许可证

本项目仅供学习和研究使用。

---

**最后更新时间**：2026年4月3日

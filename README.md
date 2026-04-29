# 侗乡医药数字展示平台

> 🎓 一个基于 **Vue 3 + Spring Boot** 的前后端分离毕业设计项目
> 📖 本文档将带你从零理解前后端分离开发的完整概念框架

---

## 📚 给初学者的话：什么是前后端分离？

如果你是第一次接触前后端分离项目，别担心，我们从最基础的概念开始。

### 传统网站 vs 前后端分离

**传统网站**（比如用JSP、PHP写的网站）：
```
用户请求 → 服务器生成完整HTML页面 → 返回给浏览器显示
```
前端和后端的代码混在一起，服务器既要处理业务逻辑，又要生成页面。

**前后端分离**（本项目采用的方式）：
```
前端(Vue)：负责页面展示和用户交互 ←→ JSON数据 ←→ 后端(Spring Boot)：负责业务逻辑和数据存储
```
前端和后端是**两个独立的项目**，通过API接口（传递JSON数据）进行通信。

> 💡 **类比理解**：想象一家餐厅。前端是服务员和菜单（负责展示菜品、接受顾客点餐），
> 后端是厨房（负责做菜），API接口是传菜窗口（服务员把订单递进去，厨房把做好的菜递出来）。
> 顾客（用户）只和服务员打交道，不需要知道厨房怎么运作。

### 前后端分离的优势

| 优势 | 说明 |
|------|------|
| **分工明确** | 前端专注页面，后端专注逻辑，互不干扰 |
| **独立开发** | 前后端可以同时开发，只需约定好API接口 |
| **独立部署** | 前端部署在Nginx，后端部署在应用服务器，互不影响 |
| **可扩展性** | 前端可以换成手机App，后端API不用改 |

---

## 🏗️ 技术架构全景

### 整体架构图

```
┌──────────────────────────────────────────────────────────────┐
│                     🌐 用户浏览器                              │
│            （用户看到和操作的界面）                               │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP请求
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                  🔀 Nginx 反向代理服务器                        │
│                                                              │
│  请求类型判断：                                                │
│  ├── 请求页面/JS/CSS/图片？ → 转发给前端（Vue打包后的静态文件）    │
│  ├── 请求 /api/* ？         → 转发给后端（Spring Boot）         │
│  ├── 请求文档预览？          → 转发给KKFileView服务              │
│  └── 请求视频资源？          → 转发给后端（支持Range分片加载）     │
└───────────┬──────────────────────────────────┬───────────────┘
            │                                  │
            ▼                                  ▼
┌───────────────────────┐          ┌───────────────────────────┐
│  📱 前端 (Vue 3)       │          │  ☕ 后端 (Spring Boot)     │
│                       │          │                           │
│  你看到的每一个页面：    │   JSON   │  处理每一个业务请求：       │
│  ├── 首页             │ ←──────→ │  ├── 用户登录/注册         │
│  ├── 植物图鉴         │   数据    │  ├── 植物数据查询          │
│  ├── 知识库           │   交互    │  ├── 知识库管理            │
│  ├── 互动答题         │          │  ├── AI聊天               │
│  └── 管理后台         │          │  └── 文件上传              │
└───────────────────────┘          └───────┬───────────────────┘
                                           │
                          ┌────────────────┼────────────────┐
                          ▼                ▼                ▼
                   ┌────────────┐  ┌────────────┐  ┌────────────┐
                   │ 🐬 MySQL   │  │ 🔴 Redis   │  │ 📄 KKFile  │
                   │            │  │            │  │            │
                   │ 存储所有    │  │ 存储缓存    │  │ 文档在线    │
                   │ 永久数据    │  │ 和临时数据   │  │ 预览服务    │
                   └────────────┘  └────────────┘  └────────────┘
                                           │
                                    ┌──────┴──────┐
                                    │ 🐰 RabbitMQ │
                                    │             │
                                    │ 异步消息队列 │
                                    │ 操作日志/   │
                                    │ 热度计算    │
                                    └─────────────┘
```

### 数据流动示例：用户查看一株药用植物

```
1. 用户在浏览器点击"钩藤"卡片
       ↓
2. Vue Router 跳转到 /plants/1 页面
       ↓
3. Vue 组件调用 Axios 发送 GET /api/plants/1 请求
       ↓
4. Nginx 识别 /api/ 前缀，将请求转发到后端 Spring Boot
       ↓
5. Sa-Token 拦截器验证该接口是否需要登录（此接口不需要）
       ↓
6. PlantController 接收请求，调用 PlantService
       ↓
7. PlantService 先查 Redis 缓存 → 没有则查 MySQL 数据库
       ↓
8. MySQL 返回植物数据 → 存入 Redis 缓存 → 返回给 Controller
       ↓
9. Controller 用 R<Plant> 包装成统一格式：{code:200, msg:"success", data:{...}}
       ↓
10. JSON 响应返回浏览器 → Vue 组件渲染页面 → 用户看到"钩藤"详情
```

---

## 🔧 技术栈详细介绍

### 前端技术栈

| 技术 | 版本 | 是什么 | 为什么用它 | 类比 |
|------|------|--------|-----------|------|
| **Vue.js** | 3.4 | JavaScript前端框架 | 让你用组件化方式构建用户界面 | 像搭积木一样组装页面 |
| **Vite** | 5.0 | 构建工具 | 开发时热更新超快，打包优化 | 高速加工厂 |
| **Element Plus** | 2.4 | UI组件库 | 提供现成的按钮、表格、弹窗等组件 | 装修材料包 |
| **Pinia** | 2.3 | 状态管理 | 管理全局共享数据（如登录状态） | 全局公告板 |
| **Vue Router** | 4.2 | 路由管理 | 实现页面跳转不刷新（SPA） | 餐厅导航图 |
| **Axios** | 1.6 | HTTP客户端 | 发送请求到后端API | 服务员传菜单 |
| **ECharts** | 5.4 | 数据可视化 | 制作统计图表 | 画图工具 |

### 后端技术栈

| 技术 | 版本 | 是什么 | 为什么用它 | 类比 |
|------|------|--------|-----------|------|
| **Spring Boot** | 3.1.12 | Java Web框架 | 快速搭建后端服务，自动配置 | 精装修的房子 |
| **Sa-Token** | 1.37.0 | 权限认证框架 | 处理登录认证、权限控制、踢人下线 | 保安系统 |
| **MyBatis-Plus** | 3.5.9 | ORM框架 | 用Java代码操作数据库，不用写SQL | 翻译官 |
| **MySQL** | 8.0+ | 关系型数据库 | 存储所有永久数据 | 仓库 |
| **Redis** | 7.0+ | 内存数据库 | 缓存热点数据，加速访问 | 展示柜（比仓库快） |
| **RabbitMQ** | 3.x | 消息队列 | 异步处理操作日志、热度计算 | 快递中转站 |
| **Sa-Token JWT** | 1.37.0 | JWT模式 | Sa-Token整合JWT，无状态Token认证 | 电子门禁卡 |
| **SpringDoc** | 2.2.0 | API文档 | 自动生成接口文档 | 说明书 |

### DevOps技术栈

| 技术 | 是什么 | 为什么用它 |
|------|--------|-----------|
| **Docker** | 容器化工具 | 把应用打包成容器，在哪都能运行 |
| **Docker Compose** | 编排工具 | 一条命令启动所有服务 |
| **Nginx** | Web服务器 | 反向代理、静态资源服务、负载均衡、kkfileview代理 |
| **GitHub Actions** | CI/CD工具 | 推送代码自动构建部署 |

---

## 📁 项目结构解析

```
dong-medicine-platform/           ← 项目根目录
│
├── 📂 dong-medicine-frontend/    ← 【前端项目】Vue 3 应用
│   ├── 📂 src/                   ← 源代码（你主要写代码的地方）
│   │   ├── 📂 views/             ← 页面组件（每个页面一个.vue文件）
│   │   ├── 📂 components/        ← 可复用组件（按钮、卡片等）
│   │   ├── 📂 composables/       ← 组合式函数（可复用的逻辑）
│   │   ├── 📂 router/            ← 路由配置（哪个URL显示哪个页面）
│   │   ├── 📂 stores/            ← 状态管理（全局数据）
│   │   ├── 📂 utils/             ← 工具函数（请求封装、缓存等）
│   │   ├── 📂 styles/            ← 样式文件
│   │   └── 📄 App.vue            ← 根组件（整个应用的容器）
│   ├── 📄 package.json           ← 依赖配置（类似后端的pom.xml）
│   └── 📄 vite.config.js         ← Vite构建配置
│
├── 📂 dong-medicine-backend/     ← 【后端项目】Spring Boot 应用
│   ├── 📂 src/main/java/         ← Java源代码
│   │   └── 📂 com/dongmedicine/  ← 主包
│   │       ├── 📂 controller/    ← 控制器（接收请求）
│   │       ├── 📂 service/       ← 服务层（业务逻辑）
│   │       ├── 📂 mapper/        ← 数据访问层（操作数据库）
│   │       ├── 📂 entity/        ← 实体类（对应数据库表）
│   │       ├── 📂 dto/           ← 数据传输对象
│   │       ├── 📂 config/        ← 配置类
│   │       └── 📂 common/        ← 通用工具
│   ├── 📂 src/main/resources/    ← 配置文件
│   │   ├── 📄 application.yml    ← 主配置
│   │   └── 📄 application-dev.yml← 开发环境配置
│   ├── 📂 sql/                   ← 数据库脚本
│   └── 📄 pom.xml                ← Maven依赖配置
│
├── 📂 deploy/                    ← 部署脚本
├── 📄 docker-compose.yml         ← Docker编排配置
└── 📄 .env.example               ← 环境变量模板
```

> 💡 **初学者提示**：不要被这么多文件吓到！你只需要记住：
> - 前端代码在 `dong-medicine-frontend/src/` 里
> - 后端代码在 `dong-medicine-backend/src/main/java/` 里
> - 每个文件夹里都有README.md详细说明

---

## 🚀 环境搭建步骤（从零开始）

### 第一步：安装必备软件

#### 1. 安装 JDK 17

JDK是Java开发工具包，后端项目必须依赖它。

```bash
# 下载地址：https://adoptium.net/
# 选择 JDK 17，LTS版本

# 安装后验证
java -version
# 应输出：openjdk version "17.x.x"
```

> ⚠️ **常见问题**：安装后提示"java不是内部命令"？
> 需要配置环境变量 JAVA_HOME，将JDK安装路径添加到系统PATH中。

#### 2. 安装 Node.js 20

Node.js是JavaScript运行环境，前端项目必须依赖它。npm是Node.js自带的包管理器。

```bash
# 下载地址：https://nodejs.org/
# 选择 LTS 版本（长期支持版）

# 安装后验证
node -v
# 应输出：v20.x.x

npm -v
# 应输出：10.x.x
```

#### 3. 安装 MySQL 8.0

MySQL是关系型数据库，存储项目的所有数据。

```bash
# 下载地址：https://dev.mysql.com/downloads/mysql/
# 安装时记住你设置的root密码

# 安装后验证
mysql -u root -p
# 输入密码后能进入MySQL命令行即成功
```

#### 4. 安装 Redis 7

Redis是内存数据库，用于缓存和临时数据存储。

```bash
# Windows：https://github.com/tporadowski/redis/releases
# 下载 .msi 安装包

# 安装后验证
redis-cli ping
# 应输出：PONG
```

#### 5. 安装 Maven（可选）

Maven是Java项目的依赖管理和构建工具。项目自带了Maven Wrapper（mvnw），可以不单独安装。

```bash
# 下载地址：https://maven.apache.org/download.cgi
# 配置环境变量后验证
mvn -v
```

### 第二步：初始化数据库

```bash
# 1. 打开命令行，进入项目目录
cd dong-medicine-backend/sql

# 2. 登录MySQL
mysql -u root -p

# 3. 在MySQL命令行中执行
source dong_medicine.sql
# 这会创建 dong_medicine 数据库，建表，并插入种子数据

# 4. 验证
USE dong_medicine;
SHOW TABLES;
# 应该看到13张表
```

> 💡 **什么是种子数据？** 种子数据是预先准备好的初始数据，比如50多种侗族药用植物的信息，
> 这样你启动项目后就能看到有内容的页面，而不是空白页面。

### 第三步：启动后端

```bash
# 1. 进入后端项目目录
cd dong-medicine-backend

# 2. 创建环境变量文件
cp .env.example .env

# 3. 编辑 .env 文件，至少修改以下内容：
# DB_PASSWORD=你的MySQL密码
# JWT_SECRET=随便写一个64字符以上的字符串（开发环境）

# 4. 启动后端
# Windows使用：
mvnw.cmd spring-boot:run
# 或如果安装了Maven：
mvn spring-boot:run

# 5. 看到以下输出表示启动成功：
# Started DongMedicineBackendApplication in x.xxx seconds
```

> 💡 **验证后端是否启动成功**：
> 浏览器打开 http://localhost:8080/swagger-ui.html
> 能看到API文档页面就说明后端运行正常。

### 第四步：启动前端

```bash
# 1. 新开一个命令行窗口（后端不要关），进入前端目录
cd dong-medicine-frontend

# 2. 安装依赖（只需要第一次运行）
npm install
# 这会根据 package.json 下载所有需要的库到 node_modules 文件夹

# 3. 启动开发服务器
npm run dev

# 4. 看到以下输出表示启动成功：
# Local: http://localhost:5173/
# 浏览器会自动打开
```

> ⚠️ **常见问题**：`npm install` 报错？
> - 检查Node.js版本是否18+
> - 尝试删除 `node_modules` 文件夹和 `package-lock.json` 后重新安装
> - 使用淘宝镜像：`npm config set registry https://registry.npmmirror.com`

### 第五步：访问项目

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | 前端页面（开发模式） |
| http://localhost:8080/swagger-ui.html | 后端API文档 |
| http://localhost:8080/api/plants/list?page=1&size=10 | 直接测试API |

### Docker Compose 一键部署（推荐）

```bash
# 1. 确保已安装 Docker Desktop
docker --version
docker-compose --version

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入实际配置

# 3. 一键启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps
```

Docker Compose 会自动启动以下6个服务：

| 服务 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| MySQL | dong-medicine-mysql | 3307→3306 | 数据库（宿主机3307，避免与本机MySQL冲突） |
| Redis | dong-medicine-redis | -- | 缓存（仅内部网络访问） |
| RabbitMQ | dong-medicine-rabbitmq | -- | 消息队列（仅内部网络访问） |
| Backend | dong-medicine-backend | -- | Spring Boot后端（仅内部网络访问） |
| Frontend | dong-medicine-frontend | 3000→80 | Nginx前端+反向代理 |
| KKFileView | dong-medicine-kkfileview | -- | 文档预览（通过Nginx代理访问） |

> 💡 所有服务通过Docker内部网络通信，只有前端(3000)和MySQL(3307)暴露到宿主机。
> 使用Navicat连接MySQL时：主机`localhost`，端口`3307`，用户名`root`，密码见`.env`文件。

### 默认管理员账号

| 字段 | 值 |
|------|-----|
| 用户名 | admin |
| 密码 | admin123 |

---

## 🔄 前后端交互流程详解

### 什么是API？

API（Application Programming Interface）是前后端通信的"约定"。前端发送HTTP请求，后端返回JSON数据。

> 💡 **类比**：API就像餐厅的菜单。菜单上写着"宫保鸡丁 - 28元"，
> 你不需要知道厨房怎么做，只需要按菜单点菜（发送请求），厨房做好后端上来（返回数据）。

### HTTP请求方法

| 方法 | 用途 | 本项目示例 |
|------|------|-----------|
| GET | 获取数据 | `GET /api/plants/1` 获取ID为1的植物 |
| POST | 创建数据 | `POST /api/comments` 发表一条评论 |
| PUT | 更新数据 | `PUT /api/admin/plants/1` 修改植物信息 |
| DELETE | 删除数据 | `DELETE /api/admin/plants/1` 删除植物 |

### 统一响应格式

后端所有API都返回统一的JSON格式：

```json
{
  "code": 200,        // 状态码：200成功，401未登录，403无权限，500错误
  "msg": "success",   // 提示信息
  "data": {           // 实际数据
    "id": 1,
    "nameCn": "钩藤",
    "efficacy": "清热平肝，息风止痉"
  },
  "requestId": "a1b2c3d4"  // 请求追踪ID
}
```

### 前后端交互完整示例：用户登录

```
【前端】用户填写用户名密码，点击"登录"按钮
    │
    ▼
【前端】Axios发送POST请求
    POST /api/user/login
    Body: { "username": "admin", "password": "admin123",
            "captchaKey": "xxx", "captchaCode": "1234" }
    │
    ▼
【后端】UserController接收请求
    → 验证验证码是否正确（从Redis取出对比）
    → 验证用户名密码是否匹配（BCrypt解密对比）
    → 生成JWT Token
    │
    ▼
【后端】返回JSON响应
    { "code": 200, "data": { "token": "eyJhbG...", "username": "admin", "role": "admin" } }
    │
    ▼
【前端】收到响应
    → 将Token存入sessionStorage
    → 更新Pinia用户状态（isLoggedIn = true）
    → 后续请求自动在Header中携带Token
    → 跳转到首页
```

---

## 🔐 安全机制通俗解释

### Sa-Token + JWT 认证 —— "电子门禁卡 + 会话管理"

传统方式：服务器记住"谁登录了"（Session），但多台服务器时不好同步。

Sa-Token + JWT 方式：Sa-Token 负责会话管理（踢人下线、同端互斥登录等），JWT 作为 Token 格式实现无状态认证。

```
登录成功 → Sa-Token 生成 JWT Token（包含用户信息+签名）→ 返回给前端
前端保存Token → 每次请求在Header中携带：Authorization: Bearer eyJhbG...
Sa-Token 拦截器验证Token → 签名正确且未过期 → 允许访问
```

**Sa-Token 相比纯 JWT 的优势：**

| 功能 | 纯 JWT | Sa-Token + JWT |
|------|--------|----------------|
| 踢人下线 | 需手动实现黑名单 | ✅ `StpUtil.kickout(userId)` 一行代码 |
| 同端互斥登录 | ❌ 不支持 | ✅ 配置即可 |
| Token 自动续期 | 需手动实现 | ✅ 自动续期 |
| 权限注解 | 需配合 Spring Security | ✅ `@SaCheckRole("admin")` |

### XSS防护 —— "过滤危险内容"

XSS（跨站脚本攻击）是攻击者在输入框中注入恶意JavaScript代码。

```
攻击者输入：<script>stealCookie()</script>
XSS过滤器检测到危险模式 → 清理或拒绝 → 安全！
```

本项目在前后端都做了XSS过滤，后端XssFilter会检查所有请求参数。

### SQL注入防护 —— "防止偷改数据库指令"

SQL注入是攻击者在输入框中注入SQL语句。

```
攻击者输入：' OR '1'='1
MyBatis-Plus参数化查询 → 输入只当作字符串处理 → 安全！
```

---

## 🐛 常见问题解决方案

### 后端启动失败

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| `Communications link failure` | 连不上MySQL | 检查MySQL是否启动、密码是否正确 |
| `Unable to connect to Redis` | 连不上Redis | 检查Redis是否启动 |
| `JWT secret is not secure` | JWT密钥太短 | .env中设置64字符以上的密钥 |
| `Port 8080 already in use` | 端口被占用 | 关闭占用进程或改端口 |

### 前端启动失败

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| `npm ERR! code ERESOLVE` | 依赖冲突 | 删除node_modules和lock文件，重新npm install |
| `VITE_API_BASE_URL is not defined` | 环境变量缺失 | 复制.env.example为.env |
| 页面空白 | 后端未启动 | 先启动后端，前端代理需要后端运行 |

### 数据库问题

| 问题 | 解决方案 |
|------|---------|
| `Unknown database 'dong_medicine'` | 执行 `source dong_medicine.sql` |
| 中文乱码 | 确保MySQL字符集为utf8mb4 |
| 连接超时 | 检查MySQL服务是否启动 |

---

## 📖 学习路线建议

作为初学者，建议按以下顺序阅读项目代码：

### 第一周：理解整体架构
1. 📖 阅读本README，建立前后端分离的整体概念
2. 📖 阅读 [后端README](dong-medicine-backend/README.md)，理解三层架构
3. 📖 阅读 [前端README](dong-medicine-frontend/README.md)，理解Vue组件化

### 第二周：理解后端
1. 📖 阅读 [entity/](dong-medicine-backend/src/main/java/com/dongmedicine/entity/README.md) — 数据是怎么存储的
2. 📖 阅读 [mapper/](dong-medicine-backend/src/main/java/com/dongmedicine/mapper/README.md) — 怎么操作数据库
3. 📖 阅读 [service/](dong-medicine-backend/src/main/java/com/dongmedicine/service/README.md) — 业务逻辑怎么写
4. 📖 阅读 [controller/](dong-medicine-backend/src/main/java/com/dongmedicine/controller/README.md) — API接口怎么定义

### 第三周：理解前端
1. 📖 阅读 [router/](dong-medicine-frontend/src/router/README.md) — 页面怎么跳转
2. 📖 阅读 [stores/](dong-medicine-frontend/src/stores/README.md) — 全局状态怎么管理
3. 📖 阅读 [utils/request.js](dong-medicine-frontend/src/utils/README.md) — 怎么和后端通信
4. 📖 阅读 [views/](dong-medicine-frontend/src/views/README.md) — 页面怎么写

### 第四周：理解安全与部署
1. 📖 阅读 [config/](dong-medicine-backend/src/main/java/com/dongmedicine/config/README.md) — Sa-Token认证怎么实现
2. 📖 阅读 [deploy/](deploy/README.md) — Docker部署是怎么回事

---

## 🗺️ 各目录文档导航

| 目录 | 文档链接 | 核心内容 |
|------|---------|---------|
| 根目录 | 本文件 | 项目全景、前后端分离概念 |
| 后端主目录 | [dong-medicine-backend/README.md](dong-medicine-backend/README.md) | Spring Boot架构、API接口 |
| 后端源码 | [src/.../dongmedicine/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/README.md) | 三层架构详解 |
| 后端配置 | [config/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/config/README.md) | Sa-Token、缓存、安全配置 |
| 后端控制器 | [controller/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/controller/README.md) | REST API设计 |
| 后端服务层 | [service/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/service/README.md) | 业务逻辑实现 |
| 后端数据层 | [mapper/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/mapper/README.md) | MyBatis-Plus用法 |
| 后端实体 | [entity/README.md](dong-medicine-backend/src/main/java/com/dongmedicine/entity/README.md) | 数据库表映射 |
| 数据库 | [sql/README.md](dong-medicine-backend/sql/README.md) | 建表SQL、索引设计 |
| 前端主目录 | [dong-medicine-frontend/README.md](dong-medicine-frontend/README.md) | Vue 3架构、组件体系 |
| 前端源码 | [src/README.md](dong-medicine-frontend/src/README.md) | 目录结构、技术栈 |
| 前端页面 | [views/README.md](dong-medicine-frontend/src/views/README.md) | 页面组件开发 |
| 前端工具 | [utils/README.md](dong-medicine-frontend/src/utils/README.md) | 请求封装、缓存、安全 |
| 部署 | [deploy/README.md](deploy/README.md) | Docker部署、CI/CD |

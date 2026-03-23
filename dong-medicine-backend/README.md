# 侗乡医药数字展示平台后端

> 基于 Spring Boot 3.1 的侗族医药文化遗产数字化展示平台后端服务

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [API 接口](#api-接口)
- [实体类](#实体类)
- [配置说明](#配置说明)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)

---

## 项目概述

本项目是侗乡医药数字展示平台的后端服务，提供用户认证、内容管理、互动功能、AI问答等核心功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 用户系统 | 注册、登录、权限管理、Token 刷新 |
| 内容管理 | 植物、知识、传承人、资源、问答管理 |
| 互动功能 | 评论、收藏、反馈 |
| 游戏化学习 | 测验、植物识别游戏、排行榜 |
| AI问答 | DeepSeek 智能问答 |
| 后台管理 | 数据统计、内容审核、日志管理 |
| 文件上传 | 图片、视频、文档上传管理 |

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.12 | 基础框架 |
| Spring Security | - | 安全认证 |
| MyBatis Plus | 3.5.9 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 分布式缓存 |
| Caffeine | - | 本地缓存 |
| JWT | 0.11.5 | Token认证 |
| SpringDoc | 2.2.0 | API文档 |
| Docker | - | 容器化部署 |

---

## 项目结构

```
dong-medicine-backend/
│
├── src/main/java/com/dongmedicine/
│   │
│   ├── common/                          # 公共模块
│   │   ├── constant/                    # 常量定义
│   │   ├── exception/                   # 异常处理
│   │   │   ├── BusinessException.java        # 业务异常类
│   │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   ├── util/                        # 工具类
│   │   │   ├── XssUtils.java                # XSS防护工具
│   │   │   └── IpUtils.java                 # IP获取工具
│   │   ├── R.java                       # 统一响应封装
│   │   └── SecurityUtils.java           # 安全工具类
│   │
│   ├── config/                          # 配置模块
│   │   ├── SecurityConfig.java          # Spring Security配置
│   │   ├── JwtUtil.java                 # JWT工具类
│   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   ├── CacheConfig.java             # 缓存配置
│   │   ├── CorsConfig.java              # 跨域配置
│   │   ├── RateLimitAspect.java         # 限流切面
│   │   ├── OperationLogAspect.java      # 操作日志切面
│   │   └── OpenApiConfig.java           # API文档配置
│   │
│   ├── controller/                      # 控制器层 (16个)
│   │   ├── UserController.java          # 用户接口
│   │   ├── AdminController.java         # 管理员接口
│   │   ├── PlantController.java         # 药用植物接口
│   │   ├── KnowledgeController.java     # 知识库接口
│   │   ├── InheritorController.java     # 传承人接口
│   │   ├── ResourceController.java      # 学习资源接口
│   │   ├── CommentController.java       # 评论接口
│   │   ├── FavoriteController.java      # 收藏接口
│   │   ├── FeedbackController.java      # 反馈接口
│   │   ├── QuizController.java          # 测验接口
│   │   ├── PlantGameController.java     # 植物游戏接口
│   │   ├── QaController.java            # 常见问答接口
│   │   ├── ChatController.java          # AI聊天接口
│   │   ├── LeaderboardController.java   # 排行榜接口
│   │   ├── FileUploadController.java    # 文件上传接口
│   │   └── OperationLogController.java  # 操作日志接口
│   │
│   ├── service/                         # 服务层接口
│   │   └── impl/                        # 服务层实现
│   │
│   ├── mapper/                          # 数据访问层
│   │
│   ├── entity/                          # 实体类 (13个)
│   │
│   └── dto/                             # 数据传输对象
│
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   ├── application-dev.yml              # 开发环境配置
│   ├── application-prod.yml             # 生产环境配置
│   └── logback-spring.xml               # 日志配置
│
├── sql/                                 # 数据库脚本
│   └── dong_medicine.sql                # 完整数据库脚本
│
├── public/                              # 静态资源目录
│   └── images/
│       ├── plants/                      # 植物图片
│       ├── knowledge/                   # 知识图片
│       └── inheritors/                  # 传承人图片
│
├── Dockerfile                           # Docker构建文件
├── pom.xml                              # Maven配置
└── .dockerignore                        # Docker忽略文件
```

---

## API 接口

### 用户模块 `/api/user`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/login` | 用户登录 | 公开 |
| POST | `/register` | 用户注册 | 公开 |
| GET | `/me` | 获取当前用户信息 | 需登录 |
| POST | `/change-password` | 修改密码 | 需登录 |
| POST | `/logout` | 退出登录 | 需登录 |
| GET | `/validate` | 验证Token | 公开 |
| POST | `/refresh-token` | 刷新Token | 需登录 |

### 药用植物模块 `/api/plants`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 植物列表(支持分类/用法过滤) | 公开 |
| GET | `/search` | 搜索植物 | 公开 |
| GET | `/{id}` | 植物详情 | 公开 |
| GET | `/{id}/similar` | 相似植物 | 公开 |
| GET | `/random` | 按难度随机获取植物 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |

### 知识库模块 `/api/knowledge`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 知识列表 | 公开 |
| GET | `/search` | 高级搜索(支持疗法/疾病分类) | 公开 |
| GET | `/{id}` | 知识详情 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |
| POST | `/favorite/{id}` | 收藏知识 | 需登录 |
| POST | `/feedback` | 提交反馈 | 公开 |

### 传承人模块 `/api/inheritors`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 传承人列表(支持级别过滤) | 公开 |
| GET | `/search` | 搜索传承人 | 公开 |
| GET | `/{id}` | 传承人详情 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |

### 学习资源模块 `/api/resources`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 资源列表(支持分类/类型过滤) | 公开 |
| GET | `/hot` | 热门资源 | 公开 |
| GET | `/{id}` | 资源详情 | 公开 |
| GET | `/download/{id}` | 下载资源文件 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |
| POST | `/{id}/download` | 增加下载次数 | 公开 |

### 评论模块 `/api/comments`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/` | 发表评论 | 需登录 |
| GET | `/list/{targetType}/{targetId}` | 目标评论列表 | 公开 |
| GET | `/list/all` | 所有已审核评论 | 公开 |
| GET | `/my` | 我的评论 | 需登录 |

### 收藏模块 `/api/favorites`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/{targetType}/{targetId}` | 添加收藏 | 需登录 |
| DELETE | `/{targetType}/{targetId}` | 取消收藏 | 需登录 |
| GET | `/my` | 我的收藏列表 | 需登录 |

### 测验模块 `/api/quiz`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/questions` | 获取随机问题 | 公开 |
| POST | `/submit` | 提交答案 | 公开 |
| GET | `/records` | 答题记录 | 需登录 |
| GET | `/list` | 所有问题 | 公开 |
| POST | `/add` | 添加问题 | ADMIN |
| PUT | `/update` | 更新问题 | ADMIN |
| DELETE | `/{id}` | 删除问题 | ADMIN |

### 植物游戏模块 `/api/plant-game`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/submit` | 提交游戏结果 | 公开 |
| GET | `/records` | 游戏记录 | 需登录 |

### AI聊天模块 `/api/chat`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/` | 发送聊天消息(DeepSeek AI) | 公开 |
| GET | `/stats` | 聊天统计 | ADMIN |

### 排行榜模块 `/api/leaderboard`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/combined` | 综合排行榜 | 公开 |
| GET | `/quiz` | 问答排行榜 | 公开 |
| GET | `/game` | 游戏排行榜 | 公开 |

### 文件上传模块 `/api/upload` (需要 ADMIN 角色)

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/image` | 上传单个图片 |
| POST | `/images` | 批量上传图片 |
| POST | `/video` | 上传单个视频 |
| POST | `/videos` | 批量上传视频 |
| POST | `/document` | 上传单个文档 |
| POST | `/documents` | 批量上传文档 |
| POST | `/file` | 通用文件上传 |
| DELETE | `/` | 删除文件 |

### 管理后台 `/api/admin` (需要 ADMIN 角色)

| 模块 | 端点 | 功能 |
|------|------|------|
| 用户管理 | `GET/DELETE/PUT /users/*` | 用户列表、删除、角色更新 |
| 内容管理 | `CRUD /inheritors/*` | 传承人管理 |
| | `CRUD /knowledge/*` | 知识管理 |
| | `CRUD /plants/*` | 植物管理 |
| | `CRUD /qa/*` | 问答管理 |
| | `CRUD /resources/*` | 资源管理 |
| 互动管理 | `GET/PUT/DELETE /feedback/*` | 反馈管理 |
| | `GET/PUT/DELETE /comments/*` | 评论审核 |
| 系统管理 | `GET/DELETE /logs/*` | 操作日志 |

---

## 实体类

### User - 用户

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键（自增） |
| username | String | 用户名 |
| passwordHash | String | 密码哈希(BCrypt) |
| role | String | 角色(USER/ADMIN) |
| createdAt | LocalDateTime | 创建时间 |

### Plant - 药用植物

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| nameCn | String | 中文名称 |
| nameDong | String | 侗语名称 |
| scientificName | String | 学名 |
| category | String | 分类 |
| usageWay | String | 用法方式 |
| habitat | String | 生长环境 |
| efficacy | String | 功效 |
| story | String | 相关故事 |
| images | String | 图片(JSON) |
| videos | String | 视频(JSON) |
| documents | String | 文档(JSON) |
| distribution | String | 分布地区 |
| difficulty | String | 难度级别 |
| viewCount | Integer | 浏览次数 |
| favoriteCount | Integer | 收藏次数 |

### Knowledge - 知识库

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| title | String | 标题 |
| type | String | 类型 |
| therapyCategory | String | 疗法分类 |
| diseaseCategory | String | 疾病分类 |
| content | String | 内容 |
| formula | String | 配方 |
| usageMethod | String | 使用方法 |
| steps | String | 步骤(JSON) |
| images/videos/documents | String | 媒体资源(JSON) |
| relatedPlants | String | 相关植物 |
| viewCount | Integer | 浏览次数 |

### Inheritor - 传承人

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| name | String | 姓名 |
| level | String | 级别(国家级/省级/市级/县级) |
| bio | String | 简介 |
| specialties | String | 专长(JSON) |
| experienceYears | Integer | 从业年限 |
| honors | String | 荣誉(JSON) |
| representativeCases | String | 代表案例 |
| viewCount | Integer | 浏览次数 |

### Resource - 学习资源

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| title | String | 标题 |
| category | String | 分类 |
| files | String | 文件(JSON) |
| description | String | 描述 |
| viewCount | Integer | 浏览次数 |
| downloadCount | Integer | 下载次数 |

### 其他实体

| 实体 | 说明 |
|------|------|
| Comment | 评论，支持嵌套回复 |
| Favorite | 收藏，支持多种资源类型 |
| Feedback | 用户反馈 |
| QuizQuestion | 问答题 |
| QuizRecord | 答题记录 |
| PlantGameRecord | 植物游戏记录 |
| Qa | 常见问答 |
| OperationLog | 操作日志 |

---

## 配置说明

### application.yml - 主配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

jwt:
  expiration: ${JWT_EXPIRATION:86400000}  # 24小时

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
```

### application-dev.yml - 开发环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine
    username: root
    password: 123456

  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.dongmedicine: DEBUG

file:
  upload:
    base-path: ${user.dir}/public
    image-max-size: 10485760      # 10MB
    video-max-size: 104857600     # 100MB
    document-max-size: 52428800   # 50MB

deepseek:
  api-key: ${DEEPSEEK_API_KEY:}
  base-url: https://api.deepseek.com
  model: deepseek-chat
```

### application-prod.yml - 生产环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:dong_medicine}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}

jwt:
  secret: ${JWT_SECRET}

file:
  upload:
    base-path: ${FILE_UPLOAD_PATH:/opt/dong-medicine/backend/public}

logging:
  level:
    com.dongmedicine: INFO
```

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+

### 启动步骤

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4;"

# 2. 导入数据
mysql -u root -p dong_medicine < sql/dong_medicine.sql

# 3. 启动 Redis
redis-server

# 4. 启动后端服务
./mvnw spring-boot:run

# 或指定环境
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 访问地址

- 服务地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui/index.html
- 健康检查: http://localhost:8080/actuator/health

---

## Docker 部署

### 构建镜像

```bash
docker build -t dong-medicine-backend .
```

### 运行容器

```bash
docker run -d \
  --name dong-medicine-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e DB_USERNAME=dongmedicine \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  dong-medicine-backend
```

### 使用 Docker Compose

```bash
# 在项目根目录执行
docker-compose up -d --build
```

---

## 安全机制

### 密码安全

```java
// 注册时加密
String hashedPassword = passwordEncoder.encode(rawPassword);

// 登录时验证
passwordEncoder.matches(rawPassword, user.getPasswordHash())
```

### JWT 认证

```java
// 生成Token
String token = Jwts.builder()
    .setSubject(username)
    .claim("userId", userId)
    .claim("role", role)
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + expiration))
    .signWith(SignatureAlgorithm.HS512, secret)
    .compact();
```

### 限流保护

```java
@RateLimit(value = 5, key = "login")  // 每分钟最多5次
public Result<LoginVO> login(LoginDTO dto) { ... }
```

---

## 缓存策略

| 缓存名称 | 说明 | 过期时间 |
|---------|------|----------|
| users | 用户信息 | 1小时 |
| plants | 植物数据 | 1小时 |
| knowledges | 知识数据 | 1小时 |
| inheritors | 传承人数据 | 1小时 |
| resources | 资源数据 | 1小时 |
| quizQuestions | 测验题目 | 1小时 |

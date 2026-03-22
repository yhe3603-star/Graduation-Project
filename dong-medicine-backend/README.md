# 侗乡医药数字展示平台后端

> 基于 Spring Boot 3.1 的侗族医药文化遗产数字化展示平台后端服务

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构详解](#项目结构详解)
- [核心模块说明](#核心模块说明)
- [实体类详解](#实体类详解)
- [API 接口详解](#api-接口详解)
- [配置文件详解](#配置文件详解)
- [安全机制](#安全机制)
- [缓存策略](#缓存策略)
- [数据库设计](#数据库设计)
- [快速开始](#快速开始)

---

## 项目概述

本项目是侗乡医药数字展示平台的后端服务，提供用户认证、内容管理、互动功能、AI问答等核心功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 用户系统 | 注册、登录、权限管理 |
| 内容管理 | 植物、知识、传承人、资源管理 |
| 互动功能 | 评论、收藏、反馈 |
| 游戏化学习 | 测验、植物识别游戏 |
| AI问答 | DeepSeek智能问答 |
| 后台管理 | 数据统计、内容审核、日志管理 |

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.12 | 基础框架 |
| Spring Security | - | 安全认证 |
| MyBatis Plus | 3.5.9 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| JWT | 0.11.5 | Token认证 |
| Caffeine | - | 本地缓存 |
| SpringDoc | 2.2.0 | API文档 |
| dotenv-java | 3.0.0 | 环境变量 |

---

## 项目结构详解

```
dong-medicine-backend/
│
├── src/main/java/com/dongmedicine/
│   │
│   ├── common/                          # 公共模块
│   │   ├── exception/                   # 异常处理
│   │   │   ├── BusinessException.java        # 业务异常类
│   │   │   │   # 用于业务逻辑中抛出可预期的错误
│   │   │   │   # 示例: throw new BusinessException("用户不存在")
│   │   │   │
│   │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   │       # 使用 @RestControllerAdvice 捕获所有异常
│   │   │       # 统一返回错误响应格式
│   │   │
│   │   └── util/                        # 工具类
│   │       ├── XssUtils.java                 # XSS防护工具
│   │       │   # 过滤危险字符，防止XSS攻击
│   │       │   # 方法: sanitize(String input)
│   │       │
│   │       └── IpUtils.java                  # IP获取工具
│   │           # 从请求中获取真实IP地址
│   │           # 支持代理服务器场景
│   │
│   ├── config/                          # 配置模块
│   │   ├── SecurityConfig.java               # Spring Security配置
│   │   │   # 配置URL权限规则
│   │   │   # 配置JWT过滤器
│   │   │   # 配置密码加密器(BCrypt)
│   │   │   #
│   │   │   # 关键方法:
│   │   │   # - filterChain(): 配置安全过滤链
│   │   │   # - passwordEncoder(): 密码加密Bean
│   │   │   #
│   │   ├── JwtUtil.java                      # JWT工具类
│   │   │   # 生成和验证JWT Token
│   │   │   # Token包含: username, userId, role
│   │   │   #
│   │   │   # 关键方法:
│   │   │   # - generateToken(username, userId, role): 生成Token
│   │   │   # - validateToken(token): 验证Token有效性
│   │   │   # - getUsernameFromToken(token): 从Token获取用户名
│   │   │   #
│   │   ├── JwtAuthenticationFilter.java      # JWT认证过滤器
│   │   │   # 拦截所有HTTP请求
│   │   │   # 从Header中提取Token
│   │   │   # 验证Token并设置SecurityContext
│   │   │   #
│   │   ├── CacheConfig.java                  # 缓存配置
│   │   │   # 配置Caffeine缓存
│   │   │   # 缓存名称: users, plants, knowledges等
│   │   │   # 过期时间: 1小时
│   │   │   #
│   │   ├── CorsConfig.java                   # 跨域配置
│   │   │   # 允许的前端域名
│   │   │   # 允许的HTTP方法
│   │   │   #
│   │   ├── RateLimitAspect.java              # 限流切面
│   │   │   # 使用滑动窗口算法
│   │   │   # 防止接口被恶意调用
│   │   │   # 示例: @RateLimit(value = 5, key = "login")
│   │   │   #
│   │   └── OperationLogAspect.java           # 操作日志切面
│   │       # AOP自动记录所有Controller操作
│   │       # 记录: 用户、模块、操作、参数、IP、耗时
│   │
│   ├── controller/                      # 控制器层
│   │   ├── UserController.java               # 用户接口
│   │   │   # POST /login       - 用户登录
│   │   │   # POST /register    - 用户注册
│   │   │   # GET  /me          - 获取当前用户
│   │   │   # POST /change-password - 修改密码
│   │   │   # POST /logout      - 退出登录
│   │   │   #
│   │   ├── PlantController.java              # 药用植物接口
│   │   │   # GET  /list        - 植物列表(支持分类筛选)
│   │   │   # GET  /search      - 搜索植物
│   │   │   # GET  /{id}        - 植物详情
│   │   │   # GET  /{id}/similar - 相似植物
│   │   │   # GET  /random      - 随机植物(游戏用)
│   │   │   # POST /{id}/view   - 增加浏览量
│   │   │   #
│   │   ├── KnowledgeController.java          # 知识库接口
│   │   │   # GET  /list        - 知识列表
│   │   │   # GET  /search      - 高级搜索
│   │   │   # GET  /{id}        - 知识详情
│   │   │   # POST /{id}/view   - 增加浏览量
│   │   │   #
│   │   ├── InheritorController.java          # 传承人接口
│   │   │   # GET  /list        - 传承人列表
│   │   │   # GET  /search      - 搜索传承人
│   │   │   # GET  /{id}        - 传承人详情
│   │   │   #
│   │   ├── ResourceController.java           # 学习资源接口
│   │   │   # GET  /list        - 资源列表
│   │   │   # GET  /hot         - 热门资源
│   │   │   # GET  /{id}        - 资源详情
│   │   │   # GET  /download/{id} - 下载资源
│   │   │   #
│   │   ├── CommentController.java            # 评论接口
│   │   │   # POST /             - 发表评论
│   │   │   # GET  /list/{type}/{id} - 评论列表
│   │   │   # GET  /my           - 我的评论
│   │   │   #
│   │   ├── FavoriteController.java           # 收藏接口
│   │   │   # POST /{type}/{id}  - 添加收藏
│   │   │   # DELETE /{type}/{id} - 取消收藏
│   │   │   # GET  /my           - 我的收藏
│   │   │   #
│   │   ├── FeedbackController.java           # 反馈接口
│   │   │   # POST /             - 提交反馈
│   │   │   # GET  /my           - 我的反馈
│   │   │   #
│   │   ├── QuizController.java               # 测验接口
│   │   │   # GET  /questions    - 获取随机题目
│   │   │   # POST /submit       - 提交答案
│   │   │   # GET  /records      - 测验记录
│   │   │   #
│   │   ├── PlantGameController.java          # 植物游戏接口
│   │   │   # POST /submit       - 提交游戏成绩
│   │   │   # GET  /records      - 游戏记录
│   │   │   #
│   │   ├── AiChatController.java             # AI聊天接口
│   │   │   # POST /             - AI智能问答
│   │   │   #
│   │   ├── LeaderboardController.java        # 排行榜接口
│   │   │   # GET  /combined     - 综合排行榜
│   │   │   # GET  /quiz         - 测验排行榜
│   │   │   # GET  /game         - 游戏排行榜
│   │   │   #
│   │   ├── UploadController.java             # 文件上传接口
│   │   │   # POST /image        - 上传图片
│   │   │   # POST /images       - 批量上传图片
│   │   │   # POST /video        - 上传视频
│   │   │   # POST /document     - 上传文档
│   │   │   # DELETE /           - 删除文件
│   │   │   #
│   │   └── AdminController.java              # 管理后台接口
│   │       # 用户管理: GET/DELETE/PUT /users/*
│   │       # 内容管理: CRUD /inheritors/*, /knowledge/*, /plants/*
│   │       # 互动管理: GET/PUT/DELETE /feedback/*, /comments/*
│   │       # 系统管理: GET/DELETE /logs/*
│   │
│   ├── service/                         # 服务层接口
│   │   ├── UserService.java                  # 用户服务接口
│   │   ├── PlantService.java                 # 植物服务接口
│   │   ├── KnowledgeService.java             # 知识服务接口
│   │   ├── InheritorService.java             # 传承人服务接口
│   │   ├── ResourceService.java              # 资源服务接口
│   │   ├── CommentService.java               # 评论服务接口
│   │   ├── FavoriteService.java              # 收藏服务接口
│   │   ├── FeedbackService.java              # 反馈服务接口
│   │   ├── QuizService.java                  # 测验服务接口
│   │   ├── PlantGameService.java             # 游戏服务接口
│   │   ├── AiChatService.java                # AI聊天服务接口
│   │   ├── LeaderboardService.java           # 排行榜服务接口
│   │   ├── FileService.java                  # 文件服务接口
│   │   └── OperationLogService.java          # 操作日志服务接口
│   │
│   ├── service/impl/                    # 服务层实现
│   │   │
│   │   ├── UserServiceImpl.java              # 用户服务实现
│   │   │   # login(): BCrypt验证密码，生成JWT
│   │   │   # register(): 密码加密，创建用户
│   │   │   # changePassword(): 验证旧密码，更新新密码
│   │   │   #
│   │   ├── PlantServiceImpl.java             # 植物服务实现
│   │   │   # listByDoubleFilter(): 双重分类筛选
│   │   │   # search(): 关键词搜索
│   │   │   # getSimilarPlants(): 相似植物推荐
│   │   │   # @Cacheable: 缓存植物列表
│   │   │   #
│   │   ├── KnowledgeServiceImpl.java         # 知识服务实现
│   │   │   # 支持疗法分类和疾病分类筛选
│   │   │   # 支持关键词搜索
│   │   │   #
│   │   ├── AiChatServiceImpl.java            # AI聊天服务实现
│   │   │   # 调用DeepSeek API
│   │   │   # 系统提示词: 侗族医药智能助手
│   │   │   # 支持多轮对话历史
│   │   │   #
│   │   └── ...其他服务实现
│   │
│   ├── mapper/                          # 数据访问层
│   │   ├── UserMapper.java                   # 用户Mapper
│   │   ├── PlantMapper.java                  # 植物Mapper
│   │   ├── KnowledgeMapper.java              # 知识Mapper
│   │   ├── InheritorMapper.java              # 传承人Mapper
│   │   ├── ResourceMapper.java               # 资源Mapper
│   │   ├── CommentMapper.java                # 评论Mapper
│   │   ├── FavoriteMapper.java               # 收藏Mapper
│   │   ├── FeedbackMapper.java               # 反馈Mapper
│   │   ├── QuizQuestionMapper.java           # 题目Mapper
│   │   ├── QuizRecordMapper.java             # 测验记录Mapper
│   │   ├── PlantGameRecordMapper.java        # 游戏记录Mapper
│   │   ├── QaMapper.java                     # 问答Mapper
│   │   └── OperationLogMapper.java           # 操作日志Mapper
│   │
│   ├── entity/                          # 实体类
│   │   └── ... (详见实体类详解章节)
│   │
│   └── dto/                             # 数据传输对象
│       ├── LoginDTO.java                     # 登录请求DTO
│       │   # 字段: username, password
│       │   #
│       │   ├── RegisterDTO.java              # 注册请求DTO
│       │   # 字段: username, password, confirmPassword
│       │   #
│       │   ├── ChangePasswordDTO.java        # 修改密码DTO
│       │   # 字段: oldPassword, newPassword
│       │   #
│       │   ├── CommentDTO.java               # 评论DTO
│       │   # 字段: targetType, targetId, content, replyToId
│       │   #
│       │   ├── FeedbackDTO.java              # 反馈DTO
│       │   # 字段: type, title, content
│       │   #
│       │   └── ...其他DTO
│   │
├── src/main/resources/
│   ├── application.yml                       # 主配置文件
│   ├── application-dev.yml                   # 开发环境配置
│   ├── application-prod.yml                  # 生产环境配置
│   └── logback-spring.xml                    # 日志配置
│
├── sql/                                 # 数据库脚本
│   └── dong_medicine.sql                     # 完整数据库脚本
│
├── public/                              # 静态资源目录
│   ├── images/                               # 图片资源
│   ├── videos/                               # 视频资源
│   └── documents/                            # 文档资源
│
└── pom.xml                              # Maven配置
```

---

## 核心模块说明

### 1. 用户认证流程

```
用户登录请求
     │
     ▼
UserController.login()
     │
     ▼
UserServiceImpl.login()
     │
     ├── BCrypt密码验证
     │
     ├── 生成JWT Token
     │   └── JwtUtil.generateToken()
     │
     └── 返回Token和用户信息
```

### 2. 请求认证流程

```
HTTP请求
     │
     ▼
JwtAuthenticationFilter
     │
     ├── 提取Authorization Header
     │
     ├── 验证Token
     │   └── JwtUtil.validateToken()
     │
     ├── 设置SecurityContext
     │
     └── 继续请求处理
```

### 3. 缓存使用示例

```java
// 缓存植物列表
@Cacheable(value = "plants", key = "'list:' + (#category ?: 'all')")
public List<Plant> listByDoubleFilter(String category, String usageWay) {
    return plantMapper.selectList(...);
}

// 清除缓存
@CacheEvict(value = "plants", allEntries = true)
public void clearCache() {
    // 清除plants缓存
}
```

### 4. 限流使用示例

```java
@PostMapping("/login")
@RateLimit(value = 5, key = "user_login")  // 每分钟最多5次
public Result<LoginVO> login(@RequestBody LoginDTO dto) {
    // ...
}
```

---

## 实体类详解

### User.java - 用户实体

```java
@TableName("users")
public class User {
    private Long id;                    // 主键ID
    private String username;            // 用户名（唯一）
    private String passwordHash;        // BCrypt加密后的密码
    private String role;                // 角色: USER, ADMIN
    private LocalDateTime createdAt;    // 创建时间
}
```

### Plant.java - 药用植物实体

```java
@TableName("plants")
public class Plant {
    private Long id;                    // 主键ID
    private String nameCn;              // 中文名
    private String nameDong;            // 侗语名
    private String scientificName;      // 学名
    private String category;            // 分类（根茎类、叶类等）
    private String usageWay;            // 用法方式
    private String efficacy;            // 功效
    private String story;               // 相关故事
    private String images;              // 图片URL（JSON数组）
    private String videos;              // 视频URL（JSON数组）
    private Integer viewCount;          // 浏览次数
    private Integer favoriteCount;      // 收藏次数
}
```

### Knowledge.java - 知识库实体

```java
@TableName("knowledge")
public class Knowledge {
    private Long id;                    // 主键ID
    private String title;               // 标题
    private String type;                // 类型: 药方/疗法
    private String therapyCategory;     // 疗法分类
    private String diseaseCategory;     // 疾病分类
    private String content;             // 内容
    private String formula;             // 配方
    private String usageMethod;         // 用法
    private String steps;               // 步骤（JSON数组）
    private String relatedPlants;       // 相关植物（JSON数组）
    private Integer viewCount;          // 浏览次数
}
```

### Inheritor.java - 传承人实体

```java
@TableName("inheritors")
public class Inheritor {
    private Long id;                    // 主键ID
    private String name;                // 姓名
    private String level;               // 等级: 国家级/省级/市级/县级
    private String bio;                 // 个人简介
    private String specialties;         // 特色技艺（JSON数组）
    private Integer experienceYears;    // 从业年限
    private String honors;              // 荣誉资质（JSON数组）
    private String representativeCases; // 代表案例
    private String photo;               // 照片URL
}
```

---

## API 接口详解

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | /login | 登录 | `{username, password}` | `{token, user}` |
| POST | /register | 注册 | `{username, password}` | `{id, username}` |
| GET | /me | 获取当前用户 | - | `{id, username, role}` |
| POST | /change-password | 修改密码 | `{oldPassword, newPassword}` | `{success}` |
| POST | /logout | 退出登录 | - | `{success}` |
| GET | /validate | 验证Token | - | `{valid, user}` |

### 植物模块 `/api/plants`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /list | 植物列表 | `category`, `usageWay`, `page`, `size` |
| GET | /search | 搜索 | `keyword` |
| GET | /{id} | 详情 | - |
| GET | /{id}/similar | 相似植物 | - |
| GET | /random | 随机植物 | `count` |
| POST | /{id}/view | 增加浏览量 | - |

### 管理后台 `/api/admin`

| 路径 | 说明 |
|------|------|
| /users/* | 用户管理 |
| /plants/* | 植物管理 |
| /knowledge/* | 知识管理 |
| /inheritors/* | 传承人管理 |
| /resources/* | 资源管理 |
| /feedback/* | 反馈管理 |
| /comments/* | 评论审核 |
| /logs/* | 操作日志 |

---

## 配置文件详解

### application.yml - 主配置

```yaml
server:
  port: 8080                          # 服务端口

spring:
  application:
    name: dong-medicine-backend       # 应用名称
  profiles:
    active: dev                       # 激活的环境

jwt:
  expiration: 86400000                # Token有效期(24小时)

file:
  upload:
    base-path: ${user.dir}/public     # 文件上传路径

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches  # Actuator端点
```

### application-dev.yml - 开发环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

  servlet:
    multipart:
      max-file-size: 100MB            # 单文件最大100MB
      max-request-size: 100MB         # 请求最大100MB

deepseek:
  api-key: ${DEEPSEEK_API_KEY:}       # DeepSeek API密钥
  base-url: https://api.deepseek.com
  model: deepseek-chat

logging:
  level:
    com.dongmedicine: DEBUG           # 日志级别
```

### application-prod.yml - 生产环境

```yaml
spring:
  datasource:
    username: ${DB_USERNAME}          # 从环境变量读取
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}               # JWT密钥

file:
  upload:
    base-path: /opt/dong-medicine/backend/public

logging:
  level:
    com.dongmedicine: INFO
```

---

## 安全机制

### 1. 密码安全

```java
// 注册时加密
String hashedPassword = passwordEncoder.encode(rawPassword);

// 登录时验证
passwordEncoder.matches(rawPassword, user.getPasswordHash())
```

### 2. JWT认证

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

### 3. XSS防护

```java
// 过滤危险字符
public static String sanitize(String input) {
    return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
}
```

### 4. 限流保护

```java
@RateLimit(value = 5, key = "login")  // 每分钟最多5次
public Result<LoginVO> login(LoginDTO dto) { ... }
```

---

## 缓存策略

### 缓存配置

| 缓存名称 | 说明 | 过期时间 |
|---------|------|----------|
| users | 用户信息 | 1小时 |
| plants | 植物数据 | 1小时 |
| knowledges | 知识数据 | 1小时 |
| inheritors | 传承人数据 | 1小时 |
| resources | 资源数据 | 1小时 |
| quizQuestions | 测验题目 | 1小时 |

### 缓存使用

```java
// 查询时缓存
@Cacheable(value = "plants", key = "#id")
public Plant getById(Long id) { ... }

// 更新时清除
@CacheEvict(value = "plants", key = "#plant.id")
public void update(Plant plant) { ... }
```

---

## 数据库设计

### 核心表结构

```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 植物表
CREATE TABLE plants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_cn VARCHAR(100) NOT NULL,
    name_dong VARCHAR(100),
    scientific_name VARCHAR(200),
    category VARCHAR(50),
    usage_way VARCHAR(50),
    efficacy TEXT,
    story TEXT,
    images JSON,
    videos JSON,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0
);

-- 知识表
CREATE TABLE knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(20),
    therapy_category VARCHAR(50),
    disease_category VARCHAR(50),
    content TEXT,
    formula TEXT,
    usage_method TEXT,
    steps JSON,
    related_plants JSON,
    view_count INT DEFAULT 0
);
```

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 启动步骤

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4;"

# 2. 导入数据
mysql -u root -p dong_medicine < sql/dong_medicine.sql

# 3. 启动服务
./mvnw spring-boot:run
```

### 访问地址

- 服务地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

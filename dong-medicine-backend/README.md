# 侗乡医药数字展示平台后端

> 基于 Spring Boot 3.1 的侗族医药文化遗产数字化展示平台后端服务

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [API 接口](#api-接口)
- [实体类](#实体类)
- [DTO 数据传输对象](#dto-数据传输对象)
- [配置说明](#配置说明)
- [安全机制](#安全机制)
- [性能优化](#性能优化)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)

---

## 项目概述

本项目是侗乡医药数字展示平台的后端服务，提供用户认证、内容管理、互动功能、AI问答等核心功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 用户系统 | 注册、登录、权限管理、Token 刷新、用户状态检查 |
| 验证码 | 图形验证码生成与验证 |
| 内容管理 | 植物、知识、传承人、资源、问答管理 |
| 互动功能 | 评论、收藏、反馈 |
| 游戏化学习 | 测验、植物识别游戏、排行榜 |
| AI问答 | DeepSeek 智能问答，支持消息长度限制 |
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
| Caffeine | 3.1+ | 本地缓存 |
| JWT (jjwt) | 0.11.5 | Token认证 |
| SpringDoc | 2.2.0 | API文档 |
| Lombok | 1.18.38 | 简化代码 |
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
│   │   │   └── RoleConstants.java       # 角色常量 (USER, ADMIN)
│   │   │
│   │   ├── exception/                   # 异常处理
│   │   │   ├── BusinessException.java        # 业务异常类
│   │   │   ├── ErrorCode.java                # 错误码定义
│   │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   │
│   │   ├── util/                        # 工具类
│   │   │   ├── FileCleanupHelper.java        # 文件清理助手
│   │   │   ├── FileTypeUtils.java            # 文件类型工具
│   │   │   ├── PageUtils.java                # 分页工具 + LIKE转义
│   │   │   ├── PasswordValidator.java        # 密码验证器
│   │   │   ├── SensitiveDataUtils.java       # 敏感信息脱敏
│   │   │   └── XssUtils.java                 # XSS防护工具
│   │   │
│   │   ├── R.java                       # 统一响应封装(含requestId)
│   │   └── SecurityUtils.java           # 安全工具类
│   │
│   ├── config/                          # 配置模块
│   │   ├── health/                      # 健康检查
│   │   │   ├── CacheHealthIndicator.java     # 缓存健康检查
│   │   │   ├── DatabaseHealthIndicator.java  # 数据库健康检查
│   │   │   └── RedisHealthIndicator.java     # Redis健康检查
│   │   │
│   │   ├── logging/                     # 日志配置
│   │   │   └── SensitiveDataConverter.java   # 敏感数据转换器
│   │   │
│   │   ├── AdminDataInitializer.java    # 管理员数据初始化
│   │   ├── AppProperties.java           # 应用属性配置
│   │   ├── AsyncConfig.java             # 异步配置
│   │   ├── CacheConfig.java             # 缓存配置 (Redis + Caffeine)
│   │   ├── CustomUserDetails.java       # 用户详情实现
│   │   ├── DeepSeekConfig.java          # DeepSeek AI 配置
│   │   ├── FileUploadProperties.java    # 文件上传属性
│   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   ├── JwtSecretValidator.java      # JWT密钥验证器
│   │   ├── JwtUtil.java                 # JWT工具类
│   │   ├── LoggingAspect.java           # 日志切面
│   │   ├── MybatisPlusConfig.java       # MyBatis Plus 配置
│   │   ├── OpenApiConfig.java           # OpenAPI 配置
│   │   ├── OperationLogAspect.java      # 操作日志切面
│   │   ├── RateLimit.java               # 限流注解
│   │   ├── RateLimitAspect.java         # 限流切面 (含本地令牌桶降级)
│   │   ├── RequestIdFilter.java         # 请求追踪ID过滤器
│   │   ├── RequestSizeFilter.java       # 请求体大小限制过滤器
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   ├── SecurityConfigValidator.java # 安全配置验证器
│   │   ├── StartupInfoPrinter.java      # 启动信息打印
│   │   ├── WebMvcConfig.java            # Web MVC 配置
│   │   └── XssFilter.java               # XSS 过滤器
│   │
│   ├── controller/                      # 控制器层 (17个)
│   │   ├── AdminController.java         # 管理后台接口
│   │   ├── CaptchaController.java       # 验证码接口
│   │   ├── ChatController.java          # AI聊天接口
│   │   ├── CommentController.java       # 评论接口
│   │   ├── FavoriteController.java      # 收藏接口
│   │   ├── FeedbackController.java      # 反馈接口
│   │   ├── FileUploadController.java    # 文件上传接口
│   │   ├── InheritorController.java     # 传承人接口
│   │   ├── KnowledgeController.java     # 知识库接口
│   │   ├── LeaderboardController.java   # 排行榜接口
│   │   ├── OperationLogController.java  # 操作日志接口
│   │   ├── PlantController.java         # 药用植物接口
│   │   ├── PlantGameController.java     # 植物游戏接口
│   │   ├── QaController.java            # 常见问答接口
│   │   ├── QuizController.java          # 测验接口
│   │   ├── ResourceController.java      # 学习资源接口
│   │   └── UserController.java          # 用户接口
│   │
│   ├── dto/                             # 数据传输对象 (18个)
│   │   ├── AnswerDTO.java               # 答案DTO
│   │   ├── CaptchaDTO.java              # 验证码DTO
│   │   ├── ChangePasswordDTO.java       # 修改密码DTO
│   │   ├── ChatRequest.java             # AI聊天请求(含长度限制)
│   │   ├── ChatResponse.java            # AI聊天响应
│   │   ├── CommentAddDTO.java           # 添加评论DTO
│   │   ├── CommentDTO.java              # 评论DTO
│   │   ├── FeedbackDTO.java             # 反馈DTO
│   │   ├── FeedbackReplyDTO.java        # 反馈回复DTO
│   │   ├── FileUploadResult.java        # 文件上传结果
│   │   ├── InheritorDTO.java            # 传承人DTO
│   │   ├── KnowledgeDTO.java            # 知识DTO
│   │   ├── LoginDTO.java                # 登录DTO
│   │   ├── PlantDTO.java                # 植物DTO
│   │   ├── PlantGameSubmitDTO.java      # 植物游戏提交DTO
│   │   ├── QuizQuestionDTO.java         # 测验问题DTO
│   │   ├── QuizSubmitDTO.java           # 测验提交DTO
│   │   ├── RegisterDTO.java             # 注册DTO
│   │   └── UserUpdateDTO.java           # 用户更新DTO
│   │
│   ├── entity/                          # 实体类 (13个)
│   │   ├── Comment.java                 # 评论
│   │   ├── Favorite.java                # 收藏
│   │   ├── Feedback.java                # 反馈
│   │   ├── Inheritor.java               # 传承人
│   │   ├── Knowledge.java               # 知识
│   │   ├── OperationLog.java            # 操作日志
│   │   ├── Plant.java                   # 药用植物
│   │   ├── PlantGameRecord.java         # 植物游戏记录
│   │   ├── Qa.java                      # 常见问答
│   │   ├── QuizQuestion.java            # 测验问题
│   │   ├── QuizRecord.java              # 测验记录
│   │   ├── Resource.java                # 学习资源
│   │   └── User.java                    # 用户
│   │
│   ├── mapper/                          # 数据访问层 (13个)
│   │   ├── CommentMapper.java
│   │   ├── FavoriteMapper.java
│   │   ├── FeedbackMapper.java
│   │   ├── InheritorMapper.java
│   │   ├── KnowledgeMapper.java
│   │   ├── OperationLogMapper.java
│   │   ├── PlantGameRecordMapper.java
│   │   ├── PlantMapper.java
│   │   ├── QaMapper.java
│   │   ├── QuizQuestionMapper.java
│   │   ├── QuizRecordMapper.java
│   │   ├── ResourceMapper.java
│   │   └── UserMapper.java
│   │
│   ├── service/                         # 服务层
│   │   ├── impl/                        # 服务实现 (15个)
│   │   │   ├── AiChatServiceImpl.java        # AI聊天服务
│   │   │   ├── CommentServiceImpl.java       # 评论服务
│   │   │   ├── FavoriteServiceImpl.java      # 收藏服务
│   │   │   ├── FeedbackServiceImpl.java      # 反馈服务
│   │   │   ├── FileUploadServiceImpl.java    # 文件上传服务
│   │   │   ├── InheritorServiceImpl.java     # 传承人服务
│   │   │   ├── KnowledgeServiceImpl.java     # 知识服务
│   │   │   ├── OperationLogServiceImpl.java  # 操作日志服务
│   │   │   ├── PlantGameServiceImpl.java     # 植物游戏服务
│   │   │   ├── PlantServiceImpl.java         # 植物服务
│   │   │   ├── QaServiceImpl.java            # 问答服务
│   │   │   ├── QuizServiceImpl.java          # 测验服务
│   │   │   ├── ResourceServiceImpl.java      # 资源服务
│   │   │   ├── TokenBlacklistServiceImpl.java # Token黑名单服务
│   │   │   └── UserServiceImpl.java          # 用户服务
│   │   │
│   │   ├── AiChatService.java           # AI聊天服务接口
│   │   ├── CaptchaService.java          # 验证码服务接口
│   │   ├── CommentService.java          # 评论服务接口
│   │   ├── FavoriteService.java         # 收藏服务接口
│   │   ├── FeedbackService.java         # 反馈服务接口
│   │   ├── FileUploadService.java       # 文件上传服务接口
│   │   ├── InheritorService.java        # 传承人服务接口
│   │   ├── KnowledgeService.java        # 知识服务接口
│   │   ├── OperationLogService.java     # 操作日志服务接口
│   │   ├── PlantGameService.java        # 植物游戏服务接口
│   │   ├── PlantService.java            # 植物服务接口
│   │   ├── QaService.java               # 问答服务接口
│   │   ├── QuizService.java             # 测验服务接口
│   │   ├── ResourceService.java         # 资源服务接口
│   │   ├── TokenBlacklistService.java   # Token黑名单服务接口
│   │   └── UserService.java             # 用户服务接口
│   │
│   └── DongMedicineBackendApplication.java  # 应用入口
│
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   ├── application-dev.yml              # 开发环境配置
│   ├── application-prod.yml             # 生产环境配置
│   └── logback-spring.xml               # 日志配置
│
├── sql/                                 # 数据库脚本
│   ├── dong_medicine.sql                # 完整数据库脚本
│   ├── fulltext_index.sql               # 全文索引脚本
│   └── optimize_indexes.sql             # 索引优化脚本
│
├── public/                              # 静态资源目录
│   ├── images/                          # 图片资源
│   │   ├── plants/                      # 植物图片 (70+张)
│   │   ├── inheritors/                  # 传承人图片
│   │   ├── knowledge/                   # 知识图片
│   │   └── common/                      # 公共图片
│   └── documents/                       # 文档资源
│       ├── xlsx/                        # Excel文档
│           ├── plants/
│           ├── inheritors/
│           ├── knowledge/
│           └── resources/
│       └── common/                      # 公共文档
│
├── test/                                # 测试目录
│   └── java/com/dongmedicine/
│       ├── common/                      # 公共模块测试
│       ├── config/                      # 配置测试
│       ├── controller/                  # 控制器测试
│       └── service/                     # 服务测试
│
├── .dockerignore                        # Docker 忽略文件
├── .gitignore                           # Git 忽略文件
├── Dockerfile                           # Docker 构建文件
├── mvnw                                 # Maven Wrapper (Unix)
├── mvnw.cmd                             # Maven Wrapper (Windows)
└── pom.xml                              # Maven 配置
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

### 验证码模块 `/api/captcha`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/image` | 获取图形验证码 | 公开 |

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

**请求限制**：
- 消息长度最大 2000 字符
- 历史消息最多 20 条
- 每秒最多 10 次请求

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
| 仪表盘 | `GET /dashboard` | 数据统计概览 |
| 用户管理 | `GET/DELETE/PUT /users/*` | 用户列表、删除、角色更新、封禁 |
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
| status | Integer | 状态(0正常/1封禁) |
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

## DTO 数据传输对象

### 登录/注册相关

| DTO | 用途 | 主要字段 |
|-----|------|----------|
| LoginDTO | 登录请求 | username, password, captchaKey, captchaCode |
| RegisterDTO | 注册请求 | username, password, confirmPassword, captchaKey, captchaCode |
| CaptchaDTO | 验证码响应 | captchaKey, captchaImage |
| ChangePasswordDTO | 修改密码 | currentPassword, newPassword, confirmPassword, captchaCode |

### 内容相关

| DTO | 用途 |
|-----|------|
| PlantDTO | 植物数据传输 |
| KnowledgeDTO | 知识数据传输 |
| InheritorDTO | 传承人数据传输 |

### 互动相关

| DTO | 用途 |
|-----|------|
| CommentDTO | 评论数据 |
| CommentAddDTO | 添加评论请求 |
| FeedbackDTO | 反馈数据 |
| FeedbackReplyDTO | 反馈回复 |

### 游戏相关

| DTO | 用途 |
|-----|------|
| QuizQuestionDTO | 测验问题 |
| QuizSubmitDTO | 提交答案 |
| AnswerDTO | 答案数据 |
| PlantGameSubmitDTO | 植物游戏提交 |

### AI相关

| DTO | 用途 |
|-----|------|
| ChatRequest | AI聊天请求(含消息长度限制) |
| ChatResponse | AI聊天响应 |

---

## 配置说明

### application.yml - 主配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

app:
  security:
    jwt-secret: ${JWT_SECRET:...}
    jwt-expiration: ${JWT_EXPIRATION:86400000}
    cors-allowed-origins:
      - ${CORS_ORIGIN_1:http://localhost:5173}
  cache:
    enabled: true
    max-size: 1000
    expire-minutes: 60
  request:
    max-body-size: ${MAX_BODY_SIZE:10485760}

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

## 安全机制

### 1. 认证与授权

```java
// JWT认证过滤器
// - Token验证
// - 用户状态检查（封禁用户无法访问）
// - Token黑名单检查

// JwtAuthenticationFilter.java
if (user.isBanned()) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write("{\"code\":403,\"msg\":\"账号已被封禁\"}");
    return;
}
```

### 2. Token黑名单

```java
// TokenBlacklistServiceImpl.java
// - Redis主存储
// - Caffeine本地缓存降级
// - 自动过期（2小时）

private final Cache<String, Boolean> localBlacklist = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(2, TimeUnit.HOURS)
    .build();
```

### 3. 密码安全

```java
// PasswordValidator.java
// - 长度 8-50 位
// - 必须包含字母和数字
// - 不能包含空格
// - BCrypt 加密存储
```

### 4. XSS 防护

```java
// XssUtils.java
// 覆盖 30+ 危险模式：
// - script标签
// - javascript/vbscript协议
// - 事件处理器 (onclick, onerror等)
// - HTML实体编码
// - eval/expression函数
// - 危险标签 (iframe, object, embed等)
```

### 5. SQL 注入防护

```java
// PageUtils.java
public static String escapeLike(String keyword) {
    return keyword
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
}

// 所有LIKE查询前调用
String escapedKeyword = PageUtils.escapeLike(keyword);
wrapper.like(Entity::getField, escapedKeyword);
```

### 6. 请求限流

```java
// RateLimitAspect.java
// - Redis计数器限流
// - 本地令牌桶降级

@RateLimit(value = 5, key = "login")  // 每分钟最多5次
public Result<LoginVO> login(LoginDTO dto) { ... }

// 本地令牌桶降级
private static class LocalTokenBucket {
    public synchronized boolean tryAcquire() { ... }
}
```

### 7. 请求追踪

```java
// RequestIdFilter.java
// - 每个请求分配唯一requestId
// - 通过MDC传递给日志系统
// - 响应头返回X-Request-ID

MDC.put("requestId", requestId);
response.setHeader("X-Request-ID", requestId);
```

### 8. 统一响应格式

```java
// R.java
public class R<T> {
    private int code;
    private String msg;
    private T data;
    private String requestId;  // 请求追踪ID
}
```

### 9. 敏感信息脱敏

```java
// SensitiveDataUtils.java
// 自动脱敏：手机号、邮箱、身份证、银行卡、JWT Token、SQL参数
```

---

## 性能优化

### 1. 多级缓存

```
请求 → Caffeine本地缓存 → Redis分布式缓存 → MySQL数据库
```

### 2. 缓存配置

```yaml
app:
  cache:
    max-size: 1000           # 最大缓存数量
    expire-minutes: 60       # 过期时间
```

### 3. 数据库优化

- 全文索引支持中文分词
- 组合索引优化常用查询
- HikariCP 高性能连接池

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

# 3. 创建全文索引（可选）
mysql -u root -p dong_medicine < sql/fulltext_index.sql

# 4. 启动 Redis
redis-server

# 5. 启动后端服务
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

## 日志示例

```
2024-01-15 10:30:45.123 [a1b2c3d4e5f6g7h8] INFO  c.d.controller.UserController - 用户登录成功: userId=1
2024-01-15 10:30:46.456 [a1b2c3d4e5f6g7h8] WARN  c.d.config.RateLimitAspect - 请求过于频繁: login:192.168.1.100
2024-01-15 10:30:47.789 [b2c3d4e5f6g7h8i9] INFO  c.d.service.impl.AiChatServiceImpl - AI聊天请求: messageLength=50
```

---

**最后更新时间**：2026年3月28日

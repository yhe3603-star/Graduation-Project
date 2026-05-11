# 侗乡医药数字展示平台 — 项目说明文档

> 非遗视角下侗乡医药数字展示平台 — 广西壮族自治区级非物质文化遗产数字保护与传播平台

## 目录

- [一、项目概述](#一项目概述)
  - [1.1 项目定位](#11-项目定位)
  - [1.2 核心理念](#12-核心理念)
  - [1.3 功能模块一览](#13-功能模块一览)
  - [1.4 前后端分离架构详解](#14-前后端分离架构详解)
- [二、技术架构](#二技术架构)
  - [2.1 整体架构图](#21-整体架构图)
  - [2.2 前端技术栈](#22-前端技术栈)
  - [2.3 后端技术栈](#23-后端技术栈)
  - [2.4 DevOps技术栈](#24-devops技术栈)
  - [2.5 数据流示例：用户查看药用植物](#25-数据流示例用户查看药用植物)
  - [2.6 完整登录数据流（全链路）](#26-完整登录数据流全链路)
  - [2.7 前端设计系统](#27-前端设计系统)
  - [2.8 前端应用初始化流程](#28-前端应用初始化流程)
  - [2.9 后端启动类设计](#29-后端启动类设计)
- [三、项目结构](#三项目结构)
  - [3.1 后端目录结构](#31-后端目录结构)
  - [3.2 前端目录结构](#32-前端目录结构)
- [四、环境搭建与安装指南](#四环境搭建与安装指南)
  - [4.1 软件要求](#41-软件要求)
  - [4.2 数据库初始化](#42-数据库初始化)
  - [4.3 启动后端](#43-启动后端)
  - [4.4 启动前端](#44-启动前端)
  - [4.5 Docker一键部署](#45-docker一键部署)
  - [4.6 默认管理员账号](#46-默认管理员账号)
  - [4.7 常见安装问题](#47-常见安装问题)
- [五、功能模块说明](#五功能模块说明)
  - [5.1 后端三层架构详解](#51-后端三层架构详解)
  - [5.2 完整API参考](#52-完整api参考)
  - [5.3 后端Service层详解](#53-后端service层详解)
  - [5.4 后端Mapper层详解](#54-后端mapper层详解)
  - [5.5 后端Entity层详解](#55-后端entity层详解)
  - [5.6 后端Config层详解](#56-后端config层详解)
  - [5.7 后端MQ层详解](#57-后端mq层详解)
  - [5.8 后端Common模块详解](#58-后端common模块详解)
  - [5.9 前端页面详解](#59-前端页面详解)
  - [5.10 前端组件架构详解](#510-前端组件架构详解)
  - [5.11 前端Composable详解](#511-前端composable详解)
  - [5.12 前端工具函数详解](#512-前端工具函数详解)
  - [5.13 前端自定义指令详解](#513-前端自定义指令详解)
- [六、数据库设计](#六数据库设计)
  - [6.1 全部16张表](#61-全部16张表)
  - [6.2 表关系详解](#62-表关系详解)
  - [6.3 JSON字段详解](#63-json字段详解)
  - [6.4 数据库视图](#64-数据库视图)
  - [6.5 索引策略](#65-索引策略)
  - [6.6 迁移脚本](#66-迁移脚本)
- [七、配置详情](#七配置详情)
  - [7.1 环境变量完整列表](#71-环境变量完整列表)
  - [7.2 缓存策略详解](#72-缓存策略详解)
  - [7.3 数据库配置](#73-数据库配置)
  - [7.4 Redis配置](#74-redis配置)
  - [7.5 JVM配置建议](#75-jvm配置建议)
  - [7.6 文件上传限制](#76-文件上传限制)
  - [7.7 CORS与WebSocket配置](#77-cors与websocket配置)
  - [7.8 应用配置文件说明](#78-应用配置文件说明)

---

## 一、项目概述

### 1.1 项目定位

侗乡医药数字展示平台是一个基于 **Vue 3 + Spring Boot** 前后端分离架构的毕业设计项目，旨在通过数字化手段保护和传播广西三江侗族自治县的传统医药文化。平台聚焦于侗族医药这一广西壮族自治区级非物质文化遗产，将濒临失传的侗医药知识以数字化形式永久保存，并通过现代化的Web技术实现知识的组织、检索、互动与传播。

本项目的核心服务对象包括：

- **侗医药文化研究者**：提供系统化的知识库、文献资源和传承人档案
- **侗族地区居民**：提供便捷的侗医药知识查询和健康咨询入口
- **非遗保护工作者**：提供数字化记录工具和数据统计支持
- **普通公众**：通过趣味答题、AI对话、植物识别游戏等互动方式传播侗医药文化
- **平台管理员**：提供完整的内容管理后台，支持CRUD操作和数据导出

技术层面，项目采用业界主流的前后端分离架构，前端使用 Vue 3 + Vite + Element Plus + Pinia 技术栈，后端使用 Spring Boot 3.1 + MyBatis-Plus + Sa-Token + Redis + RabbitMQ 技术栈，数据库使用 MySQL 8.0，通过 RESTful API 进行前后端通信，Nginx 作为反向代理分发流量。项目还集成了 DeepSeek AI 实现智能对话功能，KKFileView 实现文档在线预览，ECharts 实现数据可视化大屏。

### 1.2 核心理念

本平台的核心理念可以概括为三个关键词：**保护、传承、活态传播**。

#### 保护：数字化记录濒临失传的侗医药知识

侗族医药作为口传心授的非物质文化遗产，面临着传承人老龄化、知识断层的严峻挑战。平台通过以下方式实现数字化保护：

- 建立完整的药用植物图鉴数据库，记录中文名、侗语名、学名、功效、生境、采收时节等关键信息
- 系统整理侗医药传统疗法和药方知识，按疗法分类（内治法/外治法/针灸法等）、疾病分类、药材分类三维组织
- 数字化保存传承人口述历史、从业经历、代表案例、荣誉资质等第一手资料
- 所有数据支持多媒体附件（图片/视频/文档），确保信息的完整性和可追溯性

#### 传承：建立传承人数字档案，让非遗技艺可追溯

传承人是非遗保护的核心。平台为每一位侗医药非遗传承人建立数字档案：

- 记录传承人的等级标识（国家级/省级/市级/县级），清晰展示传承谱系
- 保存传承人的技艺特色、从业年限、代表案例等核心信息
- 支持视频、图片、文档等多媒体资料的上传和展示
- 通过浏览量、收藏量、人气值等指标量化传承人的社会影响力

#### 活态传播：通过互动答题、AI对话、游戏化体验让传统文化"活"起来

非遗保护不应止步于"存档"，更要让传统文化走进当代生活。平台通过多种互动方式实现活态传播：

- **趣味答题**：随机抽取10道侗医药知识题目，即时评分和解析，让学习变得有趣
- **植物识别游戏**：看图识药，通过游戏化方式加深对药用植物的认知
- **AI智能对话**：基于DeepSeek大模型的流式对话，7x24小时侗医药知识顾问
- **评论交流**：用户可在植物、知识、传承人等页面发表评论，形成社区氛围
- **数据可视化**：ECharts统计大屏以直观的图表展示平台数据，增强传播效果

### 1.3 功能模块一览

| 序号 | 模块名称 | 功能描述 |
|------|----------|----------|
| 1 | 药用植物图鉴 | 黔东南道地药材和侗医传统药用植物展示，支持中文名/侗语名/学名搜索，分类筛选（根茎类/全草类/花叶类/果实类/皮类），使用方式筛选（外用/内服/药浴），收藏功能，浏览量统计，相似植物推荐，随机浏览 |
| 2 | 非遗医药知识库 | 侗医药传统疗法和药方知识，支持疗法分类（内治法/外治法/针灸法等）、疾病分类、药材分类三维筛选，全文搜索，收藏功能，反馈入口，浏览量统计 |
| 3 | 传承人风采 | 侗医药非遗传承人信息，等级标识（国家级/省级/市级/县级），传承谱系展示，从业履历，技艺特色，代表案例，荣誉资质，多媒体资料（图片/视频/文档） |
| 4 | 问答社区 | 侗医药知识问答平台，按分类浏览（侗药常识/侗医疗法/文化背景），搜索功能，含AI聊天入口，反馈入口，浏览量统计 |
| 5 | 学习资源 | 文件预览（KKFileView集成）/下载/收藏/批量下载（ZIP打包），按分类浏览（入门/进阶/专业），文件类型筛选（视频/文档/图片），热门资源推荐，下载计数 |
| 6 | 文化互动 | 趣味答题（10题随机+评分+解析）+ 植物识别游戏（看图识药+难度分级）+ 评论交流，答题记录和排行榜 |
| 7 | 数据可视化 | ECharts统计大屏（柱状图/饼图/雷达图/地域分布图/热度排行），数据概览（植物数/知识数/传承人数/疗法数），7日访问趋势 |
| 8 | 全局搜索 | 自动补全/搜索历史/热门搜索/分类结果/关键词高亮，跨类型搜索（植物/知识/传承人），相关度排序 |
| 9 | 管理后台 | 仪表盘/用户管理/植物管理/知识管理/传承人管理/问答管理/资源管理/反馈管理/题目管理/评论管理/日志管理/数据导出CSV |
| 10 | 个人中心 | 学习统计/我的收藏/答题记录/浏览历史/修改密码，收藏和浏览历史分页展示 |
| 11 | AI智能对话 | DeepSeek流式对话，WebSocket实时通信，会话管理（创建/切换/删除），Markdown渲染，DOMPurify安全净化 |
| 12 | 意见反馈 | 反馈表单（Bug建议/功能建议/内容纠错/其他）+ 处理状态跟踪（pending/resolved），管理员回复功能 |

### 1.4 前后端分离架构详解

#### 什么是前后端分离？

在传统的Web开发模式中，后端负责渲染HTML页面并返回给浏览器，前端只是后端模板引擎的输出目标。这种模式下，前后端代码高度耦合，开发效率低，维护困难。

前后端分离是一种架构模式，将前端（用户界面展示和交互）和后端（业务逻辑和数据存储）彻底解耦，两者通过 **RESTful API** 传递 **JSON** 数据进行通信。

#### 传统网站 vs 前后端分离

| 对比维度 | 传统网站（SSR） | 前后端分离（SPA + API） |
|----------|-----------------|------------------------|
| 页面渲染 | 后端模板引擎（Thymeleaf/JSP）渲染HTML | 前端JavaScript动态渲染（Vue虚拟DOM） |
| 数据格式 | 返回完整HTML页面 | 返回JSON数据 |
| 通信方式 | 浏览器直接请求后端页面 | 前端通过Axios调用API |
| 开发分工 | 前后端同一项目，耦合度高 | 前后端独立项目，解耦 |
| 部署方式 | 单一War/Jar包 | 前端Nginx + 后端Spring Boot独立部署 |
| 用户体验 | 每次操作刷新整个页面 | 局部更新，无刷新体验 |
| 扩展性 | 难以独立扩展 | 前后端可独立水平扩展 |

#### 本项目的前后端分离实现

```
前端 (Vue 3)                         后端 (Spring Boot)
+---------------------------+        +---------------------------+
|  页面展示 & 用户交互       |        |  业务逻辑 & 数据存储       |
|  - 路由管理 (Vue Router)   |  JSON  |  - RESTful API 接口       |
|  - 状态管理 (Pinia)        | <----> |  - 数据库操作 (MyBatis-Plus)|
|  - UI组件 (Element Plus)   |  HTTP  |  - 认证授权 (Sa-Token)     |
|  - HTTP请求 (Axios)        |        |  - 缓存管理 (Redis+Caffeine)|
|  - 数据可视化 (ECharts)    |        |  - 消息队列 (RabbitMQ)     |
+---------------------------+        +---------------------------+
        |                                      |
        v                                      v
   Nginx反向代理                          MySQL 8.0
   (静态资源 + API转发)                   (数据持久化)
```

**关键通信机制**：

1. **前端发起请求**：Axios实例自动附加 `Authorization: Bearer <token>` 请求头
2. **后端验证身份**：Sa-Token拦截器校验token有效性
3. **后端返回数据**：统一使用 `R<T>` 封装，格式为 `{code, msg, data, requestId}`
4. **前端处理响应**：Axios拦截器统一处理错误码、token刷新、XSS防护
5. **Nginx反向代理**：开发环境由Vite proxy转发，生产环境由Nginx统一入口

## 二、技术架构

### 2.1 整体架构图

```
                           +------------------+
                           |   用户浏览器      |
                           |  (Chrome/Firefox)|
                           +--------+---------+
                                    |
                                    v
                        +-----------+-----------+
                        |    Nginx 反向代理      |
                        |  (SSL终止 + 静态资源    |
                        |   + API请求转发)       |
                        +-----+----------+------+
                              |          |
                 +------------+          +------------+
                 |                                     |
        +--------v--------+                  +---------v--------+
        |   前端 (Vue 3)   |                  |  后端(Spring Boot) |
        |   静态资源服务    |                  |   RESTful API     |
        |   HTML/CSS/JS    |                  |   WebSocket       |
        +-----------------+                  +---------+---------+
                                                       |
                              +-----------+------------+-----------+
                              |           |            |           |
                     +--------v---+ +----v----+ +-----v-----+ +--v--------+
                     | MySQL 8.0  | | Redis 7 | | RabbitMQ  | | KKFileView|
                     | (主数据库)  | | (缓存)  | | (消息队列) | | (文档预览) |
                     +------------+ +---------+ +-----------+ +-----------+
```

**请求流转路径**：

1. 用户在浏览器中访问 `https://dong-medicine.cyou`
2. Nginx接收请求，静态资源（HTML/CSS/JS/图片）直接返回
3. API请求（`/api/*`）转发至后端Spring Boot应用
4. WebSocket请求（`/ws/chat`）转发至后端WebSocket处理器
5. KKFileView请求（`/kkfileview/*`）转发至KKFileView服务
6. 后端根据请求查询MySQL/Redis，或通过RabbitMQ异步处理任务

### 2.2 前端技术栈

| 技术 | 版本 | 用途说明 |
|------|------|----------|
| Vue.js | 3.4 | JavaScript前端框架，使用`<script setup>` SFC语法，Composition API组合式编程，响应式数据绑定，虚拟DOM高效渲染 |
| Vite | 5.0 | 新一代构建工具，基于ESM的极速热更新（HMR），Rollup生产打包，manualChunks代码分割（echarts/element-plus/vue独立chunk），SCSS预处理支持 |
| Element Plus | 2.4 | Vue 3 UI组件库，提供表格/表单/对话框/分页/消息提示等60+组件，中文语言包，主题覆盖定制 |
| Pinia | 2.3 | Vue 3官方状态管理库（替代Vuex），Composition API风格，TypeScript友好，模块化Store设计，持久化至localStorage |
| Vue Router | 4.2 | 客户端路由管理，HTML5 History模式，导航守卫（登录验证/权限检查/Token过期检测），路由懒加载，Token验证缓存（60秒TTL） |
| Axios | 1.6 | HTTP请求库，自动Token注入，401自动刷新Token，请求重试（3次指数退避），XSS/SQL注入防护，重复请求取消，统一错误处理 |
| ECharts | 5.4 | 数据可视化图表库，柱状图/饼图/雷达图/地域分布图/折线图，响应式图表，主题定制 |
| Sass | 1.99 | CSS预处理器，变量/嵌套/mixin/函数，全局变量和混入自动注入，模块化样式架构 |
| marked | 18 | Markdown转HTML，用于AI对话消息的Markdown渲染，支持代码高亮、表格、列表等 |
| DOMPurify | 3 | HTML安全净化，防止XSS攻击，对marked渲染的HTML进行安全过滤，移除危险标签和属性 |
| vuedraggable | 4.1 | 拖拽排序组件，基于SortableJS，用于管理后台的列表排序操作 |
| lodash-es | 4.17 | ES Module工具库，使用throttle/debounce/isEmpty等工具函数，tree-shaking友好 |

### 2.3 后端技术栈

| 技术 | 版本 | 用途说明 |
|------|------|----------|
| Java | 17 | 编程语言，使用Record类、Switch表达式、文本块等现代语法 |
| Spring Boot | 3.1.12 | 应用框架，自动配置、内嵌Tomcat、Actuator健康检查、YAML配置 |
| MyBatis-Plus | 3.5.9 | ORM框架，BaseMapper通用CRUD，LambdaQueryWrapper类型安全查询，分页插件，自动填充（createdAt/updatedAt） |
| Sa-Token | 1.37.0 | 认证授权框架，UUID Token风格，JWT集成，Redis会话持久化，注解式权限控制（@SaCheckLogin/@SaCheckRole），路由拦截排除 |
| MySQL | 8.0+ | 关系型数据库，utf8mb4字符集，ngram全文索引，InnoDB引擎，HikariCP连接池 |
| Redis | 7.0+ | 内存数据库，L2缓存层，验证码存储，聊天历史暂存，消息通知，搜索历史，Lettuce客户端 |
| Caffeine | 3.x | 本地缓存，L1缓存层，高性能W-TinyLFU淘汰策略，读写后过期（10min/30min），最大容量1000 |
| RabbitMQ | 3.12 | 消息队列，操作日志异步写入，反馈提交异步处理，文件处理任务，统计更新，通知推送，条件加载（可禁用） |
| Spring WebSocket | - | WebSocket支持，ChatWebSocketHandler处理AI聊天实时通信，TextWebSocketHandler基类 |
| Spring WebFlux | - | 响应式Web客户端，WebClient实现DeepSeek AI流式对话，SSE流式响应 |
| SpringDoc OpenAPI | 2.2.0 | API文档自动生成，Swagger UI界面，@Tag注解分组，生产环境可关闭 |
| Lombok | 1.18.38 | 代码简化，@Data/@RequiredArgsConstructor/@Slf4j等注解，消除getter/setter样板代码 |
| BCrypt | - | 密码加密，Spring Security Crypto模块，自适应成本因子，防止彩虹表攻击 |
| dotenv-java | 3.0.0 | 环境变量加载，从.env文件读取配置，ignoreIfMissing容错，优先于application.yml |
| Flyway | - | 数据库迁移，版本化SQL脚本（V1__init_schema.sql/V2__seed_data.sql），开发环境自动执行 |

### 2.4 DevOps技术栈

| 技术 | 用途说明 |
|------|----------|
| Docker | 容器化部署，前后端及中间件均支持Docker容器运行 |
| Docker Compose | 编排8个服务容器（MySQL/Redis/RabbitMQ/KKFileView/Backend/Frontend/Prometheus/Grafana），健康检查依赖链 |
| Nginx | 反向代理，SSL终止，静态资源服务，API请求转发，WebSocket代理，KKFileView代理 |
| GitHub Actions | CI/CD流水线，自动构建Docker镜像，推送至GHCR镜像仓库 |
| Prometheus | 指标采集，Spring Boot Actuator端点监控，15天数据保留 |
| Grafana | 可视化监控面板，Prometheus数据源，预配置仪表盘 |

### 2.5 数据流示例：用户查看药用植物

以下展示用户在浏览器中点击"钩藤"查看详情时，数据从前端到后端再返回的完整流转过程：

```
步骤1: 用户点击植物卡片
  └─> Vue Router导航至 /plants，触发PlantDetail组件加载

步骤2: 组件调用API
  └─> request.get('/plants/5') 发起HTTP GET请求

步骤3: Axios请求拦截器处理
  └─> 附加 Authorization: Bearer <token> 请求头
  └─> 对请求参数进行XSS/SQL注入检测和净化

步骤4: Vite Proxy转发（开发环境）
  └─> /api/plants/5 → http://localhost:8080/api/plants/5

步骤5: Spring Boot接收请求
  └─> RequestIdFilter 生成唯一请求ID，写入MDC
  └─> XssFilter 对请求体进行XSS过滤
  └─> RequestSizeFilter 检查请求体大小

步骤6: Sa-Token拦截器校验
  └─> GET请求属于只读接口，无需登录（在排除列表中）
  └─> 尝试获取当前用户ID（可能为null，游客也可访问）

步骤7: PlantController.detail() 处理业务
  └─> popularityAsyncService.incrementPlantViewAndPopularity(5) 异步增加浏览量
  └─> service.getDetailWithStory(5) 查询植物详情
       └─> 先查Caffeine L1缓存 → 再查Redis L2缓存 → 最后查MySQL
       └─> MySQL: SELECT * FROM plants WHERE id = 5
  └─> 如果用户已登录，browseHistoryService.record() 记录浏览历史

步骤8: 返回统一响应
  └─> R.ok(plant) 封装为 {code:200, msg:"success", data:{...}, requestId:"a1b2c3d4"}

步骤9: Axios响应拦截器处理
  └─> 检查res.data.code === 200，提取data字段
  └─> 如果code !== 200，显示ElMessage错误提示

步骤10: Vue响应式更新
  └─> 组件的ref/reactive变量更新
  └─> Vue虚拟DOM diff，局部更新页面
  └─> 用户看到"钩藤"的详细信息页面
```

### 2.6 完整登录数据流（全链路）

以下展示用户从输入账号密码到成功登录的完整数据流转，经过所有过滤器、拦截器和AOP切面：

```
步骤1: 用户填写登录表单
  └─> 输入用户名、密码、验证码
  └─> 点击"登录"按钮

步骤2: 前端验证
  └─> 表单校验（用户名非空、密码非空、验证码非空）
  └─> 请求参数XSS净化（sanitizeRequestData）

步骤3: 发起登录请求
  └─> request.post('/user/login', {username, password, captchaKey, captchaCode})
  └─> @RateLimit(value=5, key="user_login") 限流：每秒最多5次登录请求

步骤4: 请求经过过滤器链
  └─> RequestIdFilter: 生成requestId（UUID），写入MDC和响应头
  └─> XssFilter: 对请求体进行HTML标签过滤
  └─> RequestSizeFilter: 检查请求体不超过10MB

步骤5: Sa-Token拦截器
  └─> POST /api/user/login 在排除列表中，跳过登录校验

步骤6: UserController.login() 处理
  └─> @Valid @RequestBody LoginDTO 参数校验
       └─> @NotBlank(username) @NotBlank(password) @NotBlank(captchaKey) @NotBlank(captchaCode)
  └─> captchaService.validateCaptchaOrThrow() 验证码校验
       └─> 从Redis获取captchaKey对应的验证码
       └─> 比对用户输入与Redis中的验证码（忽略大小写）
       └─> 验证后删除Redis中的验证码（一次性使用）

步骤7: UserService.login() 业务处理
  └─> 根据username查询MySQL: SELECT * FROM users WHERE username = ?
  └─> 用户不存在 → 抛出BusinessException(ErrorCode.USER_NOT_FOUND)
  └─> 用户被封禁 → 抛出BusinessException(ErrorCode.ACCOUNT_DISABLED)
  └─> BCryptPasswordEncoder.matches(password, user.passwordHash) 密码校验
  └─> 密码错误 → 抛出BusinessException(ErrorCode.PASSWORD_WRONG)
  └─> StpUtil.login(user.id) Sa-Token登录
       └─> 生成UUID Token
       └─> 存储会话信息至Redis（sa-token:login:session:{id}）
  └─> 返回 {token, id, username, role}

步骤8: OperationLogAspect AOP切面
  └─> 记录操作日志（模块:用户, 操作:用户登录, 类型:QUERY）
  └─> 通过RabbitMQ异步发送日志（如果启用）或直接写入MySQL

步骤9: 返回响应
  └─> R.ok(Map.of("token", tokenValue, "id", userId, "username", username, "role", role))
  └─> requestId从MDC获取并附加到响应

步骤10: 前端处理响应
  └─> useUserStore.setAuth(data) 保存认证信息
       └─> token/userId/userName/role 写入Pinia响应式状态
       └─> 同步写入localStorage持久化
  └─> 路由跳转至首页或之前的目标页面

步骤11: 后续请求自动携带Token
  └─> Axios请求拦截器自动附加 Authorization: Bearer <token>
  └─> Sa-Token从Header中读取Token，验证会话有效性

步骤12: Token过期处理
  └─> 前端检测到401响应 → 自动调用refresh-token接口
  └─> 刷新成功 → 更新localStorage中的token，重发原请求
  └─> 刷新失败 → 清除认证信息，提示用户重新登录
```

### 2.7 前端设计系统

本平台的设计系统融合了侗族传统文化元素与现代UI设计原则，形成独特的视觉语言。

#### 品牌色彩

| 色彩名称 | 色值 | 用途 | 文化寓意 |
|----------|------|------|----------|
| 靛蓝 | `#1A5276` | 主色调，导航栏、标题、重要按钮 | 侗族靛染工艺，传统蓝靛染布的深邃色泽 |
| 青绿 | `#28B463` | 辅助色，成功状态、植物相关元素 | 侗乡山水的葱郁绿意，草药的自然生机 |
| 金铜 | `#c9a227` | 强调色，传承人标识、荣誉标签 | 侗族银饰和铜器的金属光泽，尊贵传承 |
| 暖米 | `#f8f5f0` | 背景色，页面底色、卡片背景 | 侗族手工织布的温暖质感，质朴底色 |

#### 设计原则

- **民族特色**：在现代化界面中融入侗族纹样、色彩和排版风格
- **信息层次**：通过字号、色彩、间距建立清晰的信息层次
- **无障碍**：确保色彩对比度满足WCAG 2.1 AA标准
- **响应式**：适配桌面端（1280px+）和平板端（768px+）

### 2.8 前端应用初始化流程

前端应用通过 `main.js` 入口文件完成初始化，共8个步骤：

```javascript
// 步骤1: 创建Vue应用实例
const app = createApp(App)

// 步骤2: 注册Element Plus组件库（中文语言包）
app.use(ElementPlus, { locale: zhCn })

// 步骤3: 注册自定义指令（lazy/debounce/throttle/permission等8个）
app.use(directives)

// 步骤4: 注册Vue Router路由
app.use(router)

// 步骤5: 注册Pinia状态管理
app.use(pinia)

// 步骤6: 全局注入request服务（可通过inject获取）
app.provide("request", request)

// 步骤7: 挂载全局属性（可通过this.$axios访问）
app.config.globalProperties.$axios = request

// 步骤8: 挂载应用到DOM（#app元素）
app.mount("#app")

// 步骤9: 路由就绪后移除加载动画
router.isReady().then(() => {
  const loading = document.getElementById('app-loading')
  if (loading) {
    loading.classList.add('fade-out')
    setTimeout(() => loading.remove(), 300)
  }
})
```

**初始化顺序说明**：

1. Element Plus必须先于路由注册，确保路由页面中的组件能正确渲染
2. Pinia必须在路由之前注册，因为路由守卫中需要访问userStore
3. request服务通过两种方式提供：`provide/inject`（Composition API推荐）和`globalProperties`（Options API兼容）

### 2.9 后端启动类设计

```java
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableCaching
public class DongMedicineBackendApplication {

    public static void main(String[] args) {
        // 步骤1: 加载.env文件中的环境变量
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()    // .env文件不存在时不报错
            .load();
        
        // 步骤2: 将.env变量设置到系统属性中，覆盖application.yml默认值
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        
        // 步骤3: 启动Spring Boot应用
        SpringApplication.run(DongMedicineBackendApplication.class, args);
    }
}
```

**关键设计决策**：

- **排除UserDetailsServiceAutoConfiguration**：因为项目使用Sa-Token而非Spring Security，需要排除Spring Boot自动配置的默认安全组件，避免启动报错
- **@EnableCaching**：启用Spring Cache注解支持，配合CacheConfig中的双层缓存管理器（Caffeine L1 + Redis L2）
- **Dotenv加载**：优先于Spring上下文初始化，确保环境变量在application.yml解析时已可用。`ignoreIfMissing()`保证在Docker等环境中不依赖.env文件

## 三、项目结构

### 3.1 后端目录结构

```
dong-medicine-backend/
├── src/main/java/com/dongmedicine/
│   ├── DongMedicineBackendApplication.java   # 启动类（Dotenv加载+@EnableCaching）
│   │
│   ├── common/                               # 通用模块
│   │   ├── R.java                            # 统一响应封装（code/msg/data/requestId）
│   │   ├── SecurityUtils.java                # 安全工具（获取当前用户ID/用户名/角色）
│   │   ├── constant/
│   │   │   ├── ApiPaths.java                 # API路径常量
│   │   │   ├── RabbitMQConstants.java        # RabbitMQ队列/交换机常量
│   │   │   ├── RoleConstants.java            # 角色常量（admin/user）
│   │   │   └── TargetType.java               # 目标类型枚举（plant/knowledge/inheritor/resource/qa）
│   │   ├── exception/
│   │   │   ├── BusinessException.java        # 业务异常（静态工厂方法+ErrorCode）
│   │   │   ├── ErrorCode.java                # 错误码枚举（1xxx用户/2xxx资源/3xxx参数/4xxx文件/5xxx操作/6xxx基础设施/7xxx AI/9xxx系统）
│   │   │   └── GlobalExceptionHandler.java   # 全局异常处理器（@ControllerAdvice）
│   │   └── util/
│   │       ├── FileCleanupHelper.java        # 文件清理辅助
│   │       ├── FileTypeUtils.java            # 文件类型判断（MIME/扩展名）
│   │       ├── IpUtils.java                  # IP地址工具
│   │       ├── PageUtils.java                # 分页工具（Page对象转Map）
│   │       ├── PasswordValidator.java        # 密码强度验证
│   │       ├── SensitiveDataUtils.java       # 敏感数据脱敏
│   │       └── XssUtils.java                 # XSS过滤工具
│   │
│   ├── config/                               # 配置层（20+配置类）
│   │   ├── AdminDataInitializer.java         # 管理员数据初始化
│   │   ├── AppProperties.java               # 应用属性配置
│   │   ├── AsyncConfig.java                  # 异步任务配置
│   │   ├── CacheConfig.java                  # 双层缓存配置（Caffeine L1 + Redis L2）
│   │   ├── DeepSeekConfig.java               # DeepSeek AI配置
│   │   ├── FileUploadProperties.java         # 文件上传属性
│   │   ├── LoggingAspect.java                # 日志AOP切面
│   │   ├── MybatisPlusConfig.java            # MyBatis-Plus配置（分页插件）
│   │   ├── MyMetaObjectHandler.java          # 自动填充处理器（createdAt/updatedAt）
│   │   ├── OpenApiConfig.java                # Swagger/OpenAPI配置
│   │   ├── OperationLogAspect.java           # 操作日志AOP切面
│   │   ├── RabbitMQConfig.java               # RabbitMQ队列/交换机/绑定配置
│   │   ├── RateLimit.java                    # 限流注解
│   │   ├── RateLimitAspect.java              # 限流AOP切面
│   │   ├── RequestIdFilter.java              # 请求ID过滤器
│   │   ├── RequestSizeFilter.java            # 请求大小过滤器
│   │   ├── SaTokenConfig.java                # Sa-Token认证配置+路由拦截+CORS
│   │   ├── SecurityConfigValidator.java      # 安全配置校验
│   │   ├── StartupInfoPrinter.java           # 启动信息打印
│   │   ├── StpInterfaceImpl.java             # Sa-Token权限接口实现
│   │   ├── WebMvcConfig.java                 # Web MVC配置
│   │   ├── WebSocketConfig.java              # WebSocket配置
│   │   ├── XssFilter.java                   # XSS过滤器
│   │   ├── health/                           # 健康检查
│   │   │   ├── CacheHealthIndicator.java     # 缓存健康指标
│   │   │   ├── DatabaseHealthIndicator.java  # 数据库健康指标
│   │   │   └── RedisHealthIndicator.java     # Redis健康指标
│   │   └── logging/
│   │       └── SensitiveDataConverter.java   # 敏感数据日志转换器
│   │
│   ├── controller/                           # 控制器层
│   │   ├── PlantController.java              # 药用植物API
│   │   ├── KnowledgeController.java          # 知识库API
│   │   ├── InheritorController.java          # 传承人API
│   │   ├── UserController.java               # 用户管理API
│   │   ├── QaController.java                 # 问答API
│   │   ├── ResourceController.java           # 学习资源API
│   │   ├── CommentController.java            # 评论API
│   │   ├── FavoriteController.java           # 收藏API
│   │   ├── QuizController.java               # 趣味答题API
│   │   ├── FeedbackController.java           # 反馈API
│   │   ├── SearchController.java             # 搜索建议API
│   │   ├── StatisticsController.java         # 数据统计API
│   │   ├── ChatController.java               # AI聊天统计API
│   │   ├── ChatHistoryController.java        # 聊天历史API
│   │   ├── BrowseHistoryController.java       # 浏览历史API
│   │   ├── LeaderboardController.java        # 排行榜API
│   │   ├── PlantGameController.java          # 植物游戏API
│   │   ├── CaptchaController.java            # 验证码API
│   │   ├── FileUploadController.java         # 文件上传API
│   │   ├── MetadataController.java           # 元数据API
│   │   ├── NotificationController.java       # 消息通知API
│   │   ├── ExportController.java             # 数据导出API
│   │   ├── OperationLogController.java       # 操作日志API
│   │   └── admin/                            # 管理后台API
│   │       ├── AdminContentController.java   # 内容CRUD管理
│   │       ├── AdminInteractionController.java # 互动管理
│   │       ├── AdminStatsController.java     # 统计管理
│   │       └── AdminUserController.java      # 用户管理
│   │
│   ├── dto/                                  # 数据传输对象（30+ DTO类）
│   │   ├── LoginDTO / RegisterDTO / ChangePasswordDTO / CaptchaDTO
│   │   ├── CommentAddDTO / CommentDTO / FeedbackDTO / FeedbackReplyDTO
│   │   ├── PlantCreateDTO / PlantDTO / PlantUpdateDTO / PlantGameSubmitDTO
│   │   ├── KnowledgeCreateDTO / KnowledgeDTO / KnowledgeUpdateDTO
│   │   ├── InheritorCreateDTO / InheritorDTO / InheritorUpdateDTO
│   │   ├── QaCreateDTO / QaUpdateDTO
│   │   ├── QuizCreateDTO / QuizQuestionDTO / QuizSubmitDTO / QuizUpdateDTO
│   │   ├── ResourceCreateDTO / ResourceUpdateDTO
│   │   ├── ChatRequest / ChatResponse / FileUploadResult / UserUpdateDTO
│   │
│   ├── entity/                               # 实体层（17个实体类）
│   │   ├── BaseEntity.java                   # 基础实体（id/createdAt/updatedAt）
│   │   ├── User / Plant / Knowledge / Inheritor / Qa / Resource
│   │   ├── Comment / Favorite / Feedback
│   │   ├── QuizQuestion / QuizRecord / PlantGameRecord
│   │   ├── OperationLog / ChatHistory / BrowseHistory / SearchHistory
│   │
│   ├── mapper/                               # 数据访问层（16个Mapper接口）
│   │   ├── UserMapper / PlantMapper / KnowledgeMapper / InheritorMapper
│   │   ├── QaMapper / ResourceMapper / CommentMapper / FavoriteMapper
│   │   ├── FeedbackMapper / QuizQuestionMapper / QuizRecordMapper
│   │   ├── PlantGameRecordMapper / OperationLogMapper
│   │   ├── ChatHistoryMapper / BrowseHistoryMapper / SearchHistoryMapper
│   │
│   ├── service/                              # 服务接口层（19个Service接口）
│   │   └── impl/                             # 服务实现层（18个ServiceImpl）
│   │
│   ├── mq/                                   # 消息队列
│   │   ├── producer/                         # 5个消息生产者
│   │   └── consumer/                         # 5个消息消费者
│   │
│   └── websocket/                            # WebSocket
│       └── ChatWebSocketHandler.java         # AI聊天WebSocket处理器
│
├── src/main/resources/
│   ├── application.yml                       # 主配置文件
│   ├── application-dev.yml                   # 开发环境配置
│   ├── application-prod.yml                  # 生产环境配置
│   ├── application.properties                # 基础属性
│   ├── logback-spring.xml                    # 日志配置
│   └── db/migration/                         # Flyway迁移脚本
│       ├── V1__init_schema.sql               # 建表脚本（16张表）
│       └── V2__seed_data.sql                 # 种子数据
│
├── pom.xml                                   # Maven依赖配置
└── public/                                   # 文件上传目录
```

### 3.2 前端目录结构

```
dong-medicine-frontend/
├── src/
│   ├── main.js                               # 应用入口（8步初始化）
│   ├── App.vue                               # 根组件
│   │
│   ├── components/                           # 组件层（三层架构）
│   │   ├── base/                             # 基础组件（ErrorBoundary/VirtualList）
│   │   ├── common/                           # 通用组件（骨架屏/加载动画）
│   │   └── business/                         # 业务组件
│   │       ├── admin/                        # 管理后台（Dashboard/DataTable/Sidebar/10个Dialog/6个Form）
│   │       ├── dialogs/                      # 前台详情对话框（5个）
│   │       ├── display/                      # 展示组件（CardGrid/ChartCard/SearchFilter/AI Chat 5子组件）
│   │       ├── interact/                     # 互动组件（CaptchaInput/CommentSection/PlantGame/QuizSection）
│   │       ├── layout/                       # 布局组件（AppHeader/AppFooter）
│   │       ├── media/                        # 媒体组件（DocumentList/ImageCarousel/VideoPlayer等6个）
│   │       └── upload/                       # 上传组件（ImageUploader/VideoUploader/DocumentUploader等4个）
│   │
│   ├── composables/                          # 组合式函数（17个）
│   │   ├── useAdminData / useBrowseHistory / useChatSessions / useChatWebSocket
│   │   ├── useDebounce / useErrorHandler / useFavorite / useFileUpload
│   │   ├── useFormDialog / useInteraction / useMedia / usePersonalCenter
│   │   ├── usePlantGame / useQuiz / useStudyStats / useUpdateLog / useVisualData
│   │
│   ├── directives/                           # 自定义指令（8个）
│   │   └── v-lazy / v-lazy-background / v-debounce / v-throttle
│   │       v-click-outside / v-focus / v-permission / v-loading
│   │
│   ├── router/                               # 路由配置（14条路由+导航守卫）
│   ├── stores/                               # Pinia状态管理（user store）
│   ├── styles/                               # 样式文件（13个CSS/SCSS文件）
│   ├── utils/                                # 工具函数（9个工具文件）
│   │   ├── index.js / request.js / cache.js / chartConfig.js
│   │   ├── logger.js / media.js / adminUtils.js / validators.js / xss.js
│   │
│   ├── views/                                # 页面视图（14个页面+6个个人中心子面板）
│   │   ├── Home / Plants / Knowledge / Inheritors / Qa
│   │   ├── Interact / Resources / Visual / PersonalCenter / Admin
│   │   ├── About / Feedback / GlobalSearch / NotFound
│   │   └── personal-center/ (BrowseHistory/Favorites/Profile/QuizHistory/Settings/Stats)
│   │
│   └── __tests__/                            # 单元测试（25个测试文件）
│
├── vite.config.js                            # Vite构建配置
├── package.json                              # NPM依赖配置
└── index.html                                # HTML入口模板
```

## 四、环境搭建与安装指南

### 4.1 软件要求

| 软件 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| JDK | 17 | 17+ | Java开发工具包，需配置JAVA_HOME环境变量，项目使用Java 17语法（Record/Switch表达式） |
| Node.js | 16 | 18+ | JavaScript运行时，npm包管理器，前端构建依赖 |
| MySQL | 8.0 | 8.0+ | 关系型数据库，需支持utf8mb4字符集和ngram全文索引解析器 |
| Redis | 6.0 | 7.0+ | 内存数据库，用于缓存、会话存储、验证码、消息通知 |
| Maven | 3.6 | 3.9+ | Java构建工具，后端项目依赖管理和打包 |
| RabbitMQ | 3.12 | 3.12+ | 消息队列，可选组件（可通过APP_RABBITMQ_ENABLED=false禁用） |

### 4.2 数据库初始化

#### 方式一：Flyway自动迁移（推荐，开发环境）

开发环境（`application-dev.yml`）默认启用Flyway，启动后端时自动执行迁移脚本：

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS dong_medicine DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 启动后端，Flyway自动执行以下脚本：
#    V1__init_schema.sql → 创建16张表+索引+全文索引
#    V2__seed_data.sql   → 插入初始数据（管理员账号、示例植物、知识条目等）
```

**Flyway迁移脚本位置**：`dong-medicine-backend/src/main/resources/db/migration/`

#### 方式二：手动执行SQL

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS dong_medicine DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 执行建表脚本
mysql -u root -p dong_medicine < dong-medicine-backend/sql/dong_medicine.sql

# 3. 或使用Flyway迁移脚本
mysql -u root -p dong_medicine < dong-medicine-backend/src/main/resources/db/migration/V1__init_schema.sql
mysql -u root -p dong_medicine < dong-medicine-backend/src/main/resources/db/migration/V2__seed_data.sql
```

### 4.3 启动后端

#### 步骤1：配置环境变量

复制 `.env.example` 为 `.env` 并修改配置：

```bash
cd "d:/Graduation Project"
cp .env.example .env
```

编辑 `.env` 文件，填入实际配置：

```properties
# MySQL配置
MYSQL_ROOT_PASSWORD=your_mysql_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# JWT密钥（生产环境必须修改，建议32位以上随机字符串）
JWT_SECRET=your-secure-jwt-secret-key-at-least-32-characters

# DeepSeek AI（可选，不填则AI对话功能不可用）
DEEPSEEK_API_KEY=sk-your-deepseek-api-key
```

#### 步骤2：安装依赖并启动

```bash
cd dong-medicine-backend

# 安装Maven依赖（首次或依赖变更时）
mvn clean install -DskipTests

# 启动开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用java -jar启动
mvn clean package -DskipTests
java -jar target/dong-medicine-backend-1.0.0.jar --spring.profiles.active=dev
```

#### 步骤3：验证启动

| 验证项 | URL | 预期结果 |
|--------|-----|----------|
| 健康检查 | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| API文档 | http://localhost:8080/swagger-ui.html | Swagger UI页面 |
| 验证码接口 | http://localhost:8080/api/captcha | 返回captchaKey和captchaImage |
| 植物列表 | http://localhost:8080/api/plants/list | 返回分页JSON数据 |

### 4.4 启动前端

```bash
cd dong-medicine-frontend

# 安装NPM依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build
```

**Vite代理配置**（`vite.config.js`）：

| 代理前缀 | 目标地址 | 说明 |
|----------|----------|------|
| `/api` | http://localhost:8080 | 后端API接口 |
| `/images` | http://localhost:8080 | 图片静态资源 |
| `/videos` | http://localhost:8080 | 视频静态资源 |
| `/documents` | http://localhost:8080 | 文档静态资源 |
| `/public` | http://localhost:8080 | 公共静态资源 |
| `/kkfileview` | http://localhost:8012 | KKFileView文档预览服务 |

前端开发服务器默认运行在 `http://localhost:5173`，Vite会自动打开浏览器。

### 4.5 Docker一键部署

#### 使用Docker Compose启动所有服务

```bash
cd "d:/Graduation Project"

# 启动所有服务（后台运行）
docker compose up -d

# 查看服务状态
docker compose ps

# 查看后端日志
docker compose logs -f backend

# 停止所有服务
docker compose down

# 停止并删除数据卷（重置所有数据）
docker compose down -v
```

#### 8个服务容器

| 容器名 | 镜像 | 端口映射 | 说明 | 资源限制 |
|--------|------|----------|------|----------|
| dong-medicine-mysql | mysql:8.0 | 3307:3306 | MySQL数据库，utf8mb4字符集 | 384M内存/0.8CPU |
| dong-medicine-redis | redis:7-alpine | - | Redis缓存，128MB最大内存，LRU淘汰 | 128M内存/0.3CPU |
| dong-medicine-rabbitmq | rabbitmq:3.12-management-alpine | 15672:15672, 5672:5672 | RabbitMQ消息队列，管理界面 | 512M内存/0.5CPU |
| dong-medicine-kkfileview | keking/kkfileview:4.1.0 | - | KKFileView文档预览 | 384M内存/0.5CPU |
| dong-medicine-backend | 自建镜像 | 8080:8080 | Spring Boot后端 | 512M内存/1CPU |
| dong-medicine-frontend | 自建镜像 | 80:80, 443:443 | Nginx+Vue前端 | 128M内存/0.3CPU |
| dong-medicine-prometheus | prom/prometheus:v2.52.0 | - | Prometheus监控 | 128M内存/0.2CPU |
| dong-medicine-grafana | grafana/grafana:11.0.0 | 3001:3000 | Grafana可视化 | 128M内存/0.2CPU |

**健康检查依赖链**：MySQL(healthy) -> Redis(healthy) -> RabbitMQ(healthy) -> Backend(healthy) -> Frontend

### 4.6 默认管理员账号

| 字段 | 值 |
|------|-----|
| 用户名 | admin |
| 密码 | admin123 |
| 角色 | admin |

> **安全提示**：生产环境部署后请立即修改默认密码。密码要求：包含字母和数字，长度至少6位。

### 4.7 常见安装问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `java: 错误: 不支持发行版本 17` | JDK版本不匹配 | 确认JAVA_HOME指向JDK 17，执行 `java -version` 验证 |
| `Communications link failure` | MySQL未启动或连接配置错误 | 检查MySQL服务状态，确认DB_HOST/DB_PORT/DB_USERNAME/DB_PASSWORD正确 |
| `Unable to connect to Redis` | Redis未启动 | 检查Redis服务状态，确认REDIS_HOST/REDIS_PORT/REDIS_PASSWORD正确 |
| `Table 'dong_medicine.xxx' doesn't exist` | 数据库未初始化 | 执行4.2节数据库初始化步骤，或确认Flyway已启用 |
| `npm ERR! code ERESOLVE` | NPM依赖冲突 | 执行 `npm install --legacy-peer-deps` 或删除node_modules后重试 |
| `Port 8080 already in use` | 端口被占用 | 修改application.yml中的server.port，或终止占用端口的进程 |
| `CORS error` | 跨域配置不匹配 | 确认CORS_ALLOWED_ORIGIN包含前端访问地址 |
| `RabbitMQ连接失败` | RabbitMQ未启动 | 设置 `APP_RABBITMQ_ENABLED=false` 禁用RabbitMQ，或启动RabbitMQ服务 |
| `验证码图片不显示` | Redis未连接 | 验证码存储在Redis中，确保Redis正常运行 |
| `文件上传失败 413` | 请求体大小超限 | 检查application.yml中spring.servlet.multipart.max-file-size配置 |

## 五、功能模块说明

### 5.1 后端三层架构详解

本项目严格遵循三层架构设计模式，将系统分为Controller层、Service层和Mapper层，各层职责清晰、解耦合理。

```
+---------------------------------------------------------------------+
|                        Controller 层                                |
|  +-------------------------------------------------------------+   |
|  | 职责：接收HTTP请求、参数校验、调用Service、返回统一响应       |   |
|  | 注解：@RestController @RequestMapping @Validated @Tag        |   |
|  | 特点：不含业务逻辑，仅做请求路由和响应封装                    |   |
|  | 示例：PlantController.detail(id) -> service.getDetail(id)    |   |
|  +---------------------------+---------------------------------+   |
|                              | 调用                                 |
|                              v                                      |
|  +-------------------------------------------------------------+   |
|  |                        Service 层                            |   |
|  | 职责：核心业务逻辑、缓存管理、事务控制、跨表操作              |   |
|  | 注解：@Service @Cacheable @CacheEvict @Transactional        |   |
|  | 特点：可被多个Controller复用，封装复杂业务规则                |   |
|  | 示例：PlantServiceImpl.getDetailWithStory(id)               |   |
|  |        -> 先查缓存 -> 缓存未命中查数据库 -> 结果写入缓存      |   |
|  +---------------------------+---------------------------------+   |
|                              | 调用                                 |
|                              v                                      |
|  +-------------------------------------------------------------+   |
|  |                        Mapper 层                             |   |
|  | 职责：数据库访问、SQL执行、结果映射                           |   |
|  | 继承：BaseMapper<T>（MyBatis-Plus提供通用CRUD）              |   |
|  | 特点：单表操作无需写SQL，复杂查询使用@Select自定义            |   |
|  | 示例：PlantMapper.selectById(id) -> SELECT * FROM plants     |   |
|  +-------------------------------------------------------------+   |
+---------------------------------------------------------------------+
```

#### 统一响应格式 R\<T\>

所有API接口统一返回 `R<T>` 格式，确保前端处理逻辑一致：

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {
    private int code;        // 状态码：200成功，400参数错误，401未授权，403禁止，404未找到，500服务器错误
    private String msg;      // 消息描述
    private T data;          // 业务数据（泛型）
    private String requestId; // 请求追踪ID（从MDC获取，用于日志排查）
}
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 5,
    "nameCn": "钩藤",
    "nameDong": "gaos jenl",
    "scientificName": "Uncaria rhynchophylla",
    "category": "根茎类",
    "efficacy": "清热平肝，息风止痉",
    "viewCount": 128,
    "favoriteCount": 15
  },
  "requestId": "a1b2c3d4e5f6"
}
```

**错误响应示例**：

```json
{
  "code": 2002,
  "msg": "植物信息不存在",
  "data": null,
  "requestId": "g7h8i9j0k1l2"
}
```

**分页响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [],
    "total": 65,
    "size": 12,
    "current": 1,
    "pages": 6
  },
  "requestId": "m3n4o5p6q7r8"
}
```

### 5.2 完整API参考

#### 5.2.1 药用植物 API (PlantController)

**Base Path**: `/api/plants`
**Tag**: 药用资源图鉴

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /list | 公开 | 分页列表（支持分类/用法/关键词筛选） |
| GET | /search | 公开 | 搜索（关键词必填） |
| GET | /{id} | 公开 | 详情（自动增加浏览量+记录浏览历史） |
| GET | /{id}/similar | 公开 | 相似植物推荐 |
| GET | /random | 公开 | 随机植物列表 |
| POST | /batch | 公开 | 批量查询（最多50条） |
| POST | /{id}/view | 公开 | 增加浏览量（@RateLimit 10次/秒） |

**GET /api/plants/list** - 分页列表

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 12 | 每页条数 |
| category | String | 否 | - | 分类筛选：根茎类/全草类/花叶类/果实类/皮类 |
| usageWay | String | 否 | - | 使用方式筛选：外用/内服/药浴 |
| keyword | String | 否 | - | 搜索关键词（中文名/侗语名/学名/功效） |

返回类型：`R<Map<String, Object>>`

```javascript
// 调用示例
const res = await request.get('/plants/list', {
  params: { page: 1, size: 12, category: '根茎类', keyword: '钩藤' }
})
// res.data = { records: [...], total: 65, current: 1, size: 12, pages: 6 }
```

**GET /api/plants/search** - 搜索

| 参数 | 类型 | 必填 | 默认值 | 校验 |
|------|------|------|--------|------|
| keyword | String | 是 | - | @NotBlank，搜索关键词不能为空 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 12 | 每页条数 |

搜索策略：优先使用MySQL全文索引（ngram解析器），全文索引不可用时降级为LIKE模糊查询。

**GET /api/plants/{id}** - 详情

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | @NotNull，植物ID |

副作用：
1. 异步增加浏览量和人气值（`popularityAsyncService.incrementPlantViewAndPopularity`）
2. 已登录用户自动记录浏览历史（`browseHistoryService.record`）

**GET /api/plants/random** - 随机植物

| 参数 | 类型 | 必填 | 默认值 | 校验 |
|------|------|------|--------|------|
| limit | Integer | 否 | 20 | @Min(1) @Max(100)，数量范围1-100 |

**POST /api/plants/batch** - 批量查询

请求体：`List<Integer>`（ID列表，最多50个）

```javascript
const res = await request.post('/plants/batch', [1, 2, 3, 5, 8])
// res.data = [Plant1, Plant2, Plant3, Plant5, Plant8]
```

#### 5.2.2 非遗知识库 API (KnowledgeController)

**Base Path**: `/api/knowledge`
**Tag**: 非遗知识库

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /list | 公开 | 分页列表（三维筛选+排序） |
| GET | /search | 公开 | 高级搜索 |
| GET | /{id} | 公开 | 详情（自动浏览量+浏览历史） |
| POST | /{id}/view | 公开 | 增加浏览量（@RateLimit 10） |
| POST | /favorite/{id} | @SaCheckLogin | 收藏知识条目 |
| POST | /feedback | @SaCheckLogin | 提交知识反馈 |

**GET /api/knowledge/list** - 分页列表

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 12 | 每页条数 |
| sortBy | String | 否 | - | 排序字段（popularity/viewCount/createdAt） |
| keyword | String | 否 | - | 搜索关键词 |
| therapy | String | 否 | - | 疗法分类筛选 |
| disease | String | 否 | - | 疾病分类筛选 |
| herb | String | 否 | - | 药材分类筛选 |

**POST /api/knowledge/feedback** - 提交反馈

| 参数 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| knowledgeId | Integer | 是 | @NotNull | 知识条目ID |
| content | String | 是 | @NotBlank, max 500 | 反馈内容，最多500字符 |

#### 5.2.3 传承人 API (InheritorController)

**Base Path**: `/api/inheritors`
**Tag**: 传承人风采

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /list | 公开 | 分页列表（等级筛选+排序） |
| GET | /search | 公开 | 搜索 |
| GET | /{id} | 公开 | 详情（自动浏览量+浏览历史） |
| POST | /{id}/view | 公开 | 增加浏览量（@RateLimit 10） |

**GET /api/inheritors/list** - 分页列表

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 12 | 每页条数 |
| level | String | 否 | - | 等级筛选：国家级/省级/市级/县级 |
| keyword | String | 否 | - | 搜索关键词 |
| sortBy | String | 否 | name | 排序字段（name/popularity/viewCount） |

#### 5.2.4 用户管理 API (UserController)

**Base Path**: `/api/user`
**Tag**: 用户管理

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | /login | @RateLimit(5) | 登录（验证码校验+BCrypt密码校验+Sa-Token登录） |
| POST | /register | @RateLimit(3) | 注册（验证码校验+密码强度校验+用户名唯一性校验） |
| GET | /me | @SaCheckLogin | 获取当前用户信息 |
| POST | /change-password | @SaCheckLogin | 修改密码（验证码+旧密码校验+新密码强度校验） |
| POST | /logout | 公开 | 退出登录（StpUtil.logout()） |
| GET | /validate | 公开 | 验证Token有效性 |
| POST | /refresh-token | 公开 | 刷新Token |

**POST /api/user/login** - 登录

请求体（LoginDTO）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| username | String | 是 | @NotBlank | 用户名 |
| password | String | 是 | @NotBlank | 密码 |
| captchaKey | String | 是 | @NotBlank | 验证码Key（从/api/captcha获取） |
| captchaCode | String | 是 | @NotBlank | 验证码内容 |

返回类型：`R<Map<String, Object>>`

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOi...",
    "id": 1,
    "username": "admin",
    "role": "admin"
  }
}
```

**GET /api/user/validate** - 验证Token

返回类型：`R<Map<String, Object>>`

```json
{
  "code": 200,
  "data": {
    "valid": true,
    "id": 1,
    "username": "admin",
    "role": "admin"
  }
}
```

Token无效时返回：`{"code": 200, "data": {"valid": false}}`

#### 5.2.5 问答社区 API (QaController)

**Base Path**: `/api/qa`
**Tag**: 非遗问答

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /list | 公开 | 分页列表（分类筛选） |
| GET | /search | 公开 | 搜索 |
| GET | /{id} | 公开 | 详情（自动浏览量） |
| POST | /{id}/view | 公开 | 增加浏览量（@RateLimit 10） |

**GET /api/qa/list** - 分页列表

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 12 | 每页条数 |
| category | String | 否 | - | 分类：侗药常识/侗医疗法/文化背景 |
| keyword | String | 否 | - | 搜索关键词 |

#### 5.2.6 学习资源 API (ResourceController)

**Base Path**: `/api/resources`
**Tag**: 学习资源

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /list | 公开 | 分页列表（分类+文件类型筛选） |
| GET | /hot | 公开 | 热门资源 |
| GET | /search | 公开 | 搜索 |
| GET | /{id} | 公开 | 详情（自动浏览量） |
| POST | /{id}/view | 公开 | 增加浏览量（@RateLimit 10） |
| GET | /download/{id} | 公开 | 文件下载（路径遍历防护+流式响应） |
| POST | /batch-download | 公开 | 批量下载（ZIP格式打包） |
| POST | /{id}/download | 公开 | 增加下载计数 |
| GET | /types | 公开 | 获取文件类型列表 |
| GET | /categories | 公开 | 获取资源分类列表 |

**GET /api/resources/download/{id}** - 文件下载

安全机制：
1. 解析资源文件的JSON路径列表，取第一个文件的path
2. 如果是HTTP URL，直接302重定向
3. 如果是本地路径，进行路径遍历防护：`filePath.normalize().startsWith(basePath.normalize())`
4. 检测到路径遍历攻击返回403 Forbidden
5. 使用8KB缓冲区流式输出文件，避免大文件OOM
6. 自动设置Content-Type和Content-Disposition（UTF-8编码文件名）

**POST /api/resources/batch-download** - 批量下载

请求体：`{"ids": [1, 2, 3]}`

响应：ZIP格式文件流，每个资源的文件放在 `resource_{id}/` 子目录中避免文件名冲突。

**GET /api/resources/types** - 文件类型

```json
{
  "code": 200,
  "data": [
    {"type": "video", "name": "视频", "extensions": ["mp4", "avi", "mov", "wmv", "flv", "mkv"]},
    {"type": "document", "name": "文档", "extensions": ["docx", "doc", "pdf", "pptx", "ppt", "xlsx", "xls", "txt"]},
    {"type": "image", "name": "图片", "extensions": ["jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"]}
  ]
}
```

#### 5.2.7 评论 API (CommentController)

**Base Path**: `/api/comments`
**Tag**: 用户评论

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | / | @SaCheckLogin | 发表评论（支持嵌套回复） |
| GET | /list/{targetType}/{targetId} | 公开 | 获取目标的评论列表 |
| GET | /list/all | 公开 | 获取所有已审核评论 |
| GET | /my | @SaCheckLogin | 获取我的评论 |

**POST /api/comments** - 发表评论

请求体（CommentAddDTO）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | String | 是 | 目标类型：plant/knowledge/inheritor |
| targetId | Integer | 是 | 目标ID |
| content | String | 是 | 评论内容 |
| replyToId | Integer | 否 | 回复的评论ID |
| replyToUserId | Integer | 否 | 回复的用户ID |
| replyToUsername | String | 否 | 回复的用户名 |

评论状态流转：`pending`（待审核） -> `approved`（审核通过）/ `rejected`（审核拒绝）

#### 5.2.8 收藏 API (FavoriteController)

**Base Path**: `/api/favorites`
**Tag**: 收藏管理

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | /{targetType}/{targetId} | @SaCheckLogin | 添加收藏 |
| DELETE | /{targetType}/{targetId} | @SaCheckLogin | 取消收藏 |
| GET | /my | @SaCheckLogin | 我的收藏列表（分页） |

收藏唯一约束：同一用户对同一目标只能收藏一次（数据库 `uk_user_target` 唯一索引）。

#### 5.2.9 趣味答题 API (QuizController)

**Base Path**: `/api/quiz`
**Tag**: 趣味答题

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /questions | 公开 | 获取随机题目（1-50题） |
| POST | /submit | 公开 | 提交答案（未登录也可答题，登录则记录成绩） |
| GET | /records | 需登录 | 我的答题记录 |
| GET | /list | 公开 | 所有题目列表 |
| POST | /add | @SaCheckRole("admin") | 添加题目 |
| PUT | /update | @SaCheckRole("admin") | 更新题目 |
| DELETE | /{id} | @SaCheckRole("admin") | 删除题目 |

**GET /api/quiz/questions** - 获取随机题目

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| count | int | 否 | 10 | 题目数量（1-50） |
| scorePerQuestion | int | 否 | 10 | 每题分值 |

**POST /api/quiz/submit** - 提交答案

请求体（QuizSubmitDTO）：

```json
{
  "answers": [
    {"questionId": 1, "selectedAnswer": "A"},
    {"questionId": 2, "selectedAnswer": "C"}
  ]
}
```

返回：

```json
{
  "code": 200,
  "data": {
    "score": 80,
    "correct": 8,
    "total": 10
  }
}
```

#### 5.2.10 用户反馈 API (FeedbackController)

**Base Path**: `/api/feedback`
**Tag**: 用户反馈

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | / | @SaCheckLogin | 提交反馈 |
| GET | /my | @SaCheckLogin | 我的反馈列表 |
| GET | /stats | 公开 | 反馈统计 |

**POST /api/feedback** - 提交反馈

请求体（FeedbackDTO）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 类型：Bug建议/功能建议/内容纠错/其他 |
| title | String | 是 | 标题 |
| content | String | 是 | 内容 |
| contact | String | 否 | 联系方式 |

**GET /api/feedback/stats** - 反馈统计

```json
{
  "code": 200,
  "data": {
    "total": 25,
    "pending": 10,
    "resolved": 15
  }
}
```

#### 5.2.11 搜索建议 API (SearchController)

**Base Path**: `/api/search`
**Tag**: 搜索建议

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /suggest | 公开 | 搜索自动补全（跨类型，最多15条） |
| GET | /hot | 公开 | 热门搜索关键词 |

**GET /api/search/suggest** - 搜索建议

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键词 |

返回格式：`List<{type, id, name, category}>`，按相关度排序（精确匹配 > 前缀匹配 > 包含匹配）

```json
{
  "code": 200,
  "data": [
    {"type": "plant", "id": 5, "name": "钩藤", "category": "根茎类"},
    {"type": "knowledge", "id": 12, "name": "钩藤配伍方", "category": "药方"},
    {"type": "inheritor", "id": 3, "name": "杨钩藤", "category": "省级"}
  ]
}
```

副作用：自动记录搜索历史（search_history表），用于热门搜索统计。

**GET /api/search/hot** - 热门搜索

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 20 | 返回数量（最大50） |

#### 5.2.12 数据统计 API (StatisticsController)

**Base Path**: `/api/stats`
**Tag**: 数据统计

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /overview | 公开 | 数据概览 |
| GET | /chart | 公开 | 图表数据 |
| GET | /trend | 公开 | 7日访问趋势 |
| GET | /plants | 公开 | 植物统计 |
| GET | /knowledge | 公开 | 知识统计 |
| GET | /qa | 公开 | 问答统计 |
| GET | /inheritors | 公开 | 传承人统计 |
| GET | /resources | 公开 | 资源统计 |

**GET /api/stats/overview** - 数据概览

```json
{
  "code": 200,
  "data": {
    "plants": 65,
    "formulas": 56,
    "inheritors": 24,
    "therapies": 8
  }
}
```

**GET /api/stats/chart** - 图表数据

返回包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| counts | Map | 各模块数量统计 |
| therapyCategories | List | 疗法分类统计（前8） |
| inheritorLevels | List | 传承人等级分布 [国家级, 省级, 市级, 县级] |
| plantCategoryNames | List | 植物分类名称列表 |
| plantCategories | List | 植物分类数量列表 |
| qaCategoryNames | List | 问答分类名称列表 |
| qaCategories | List | 问答分类数量列表 |
| plantDistribution | List | 植物地域分布（按省份聚合，前15） |
| knowledgePopularity | List | 知识热度排行（前10） |
| formulaNames | List | 药方名称列表（前8，截断8字） |
| formulaFreq | List | 药方频次列表 |

**GET /api/stats/trend** - 7日访问趋势

```json
{
  "code": 200,
  "data": {
    "dates": ["5/4", "5/5", "5/6", "5/7", "5/8", "5/9", "5/10"],
    "counts": [12, 18, 25, 30, 22, 28, 35]
  }
}
```

#### 5.2.13 AI聊天 API (ChatController)

**Base Path**: `/api/chat`
**Tag**: 智能问答

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /stats | 公开 | AI聊天请求统计 |

返回：

```json
{
  "code": 200,
  "data": {
    "totalRequests": 150,
    "successRequests": 142,
    "failedRequests": 8
  }
}
```

> **注意**：实际AI聊天使用WebSocket协议，连接地址为 `/ws/chat`，不通过HTTP API。

#### 5.2.14 AI聊天历史 API (ChatHistoryController)

**Base Path**: `/api/chat-history`
**Tag**: AI聊天历史

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /sessions | @SaCheckLogin | 获取用户的所有会话列表 |
| GET | /sessions/{sessionId} | @SaCheckLogin | 获取指定会话的消息记录 |
| DELETE | /sessions/{sessionId} | @SaCheckLogin | 删除指定会话 |

**GET /api/chat-history/sessions** - 会话列表

```json
{
  "code": 200,
  "data": [
    {
      "sessionId": "uuid-xxxx-xxxx",
      "lastMessage": "钩藤具有清热平肝的功效...",
      "updatedAt": "2026-05-10 14:30:00"
    }
  ]
}
```

#### 5.2.15 浏览历史 API (BrowseHistoryController)

**Base Path**: `/api/browse-history`
**Tag**: 浏览历史

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /my | @SaCheckLogin | 我的浏览历史（limit 1-200） |
| POST | /record | @SaCheckLogin | 记录浏览历史 |

**GET /api/browse-history/my** - 我的浏览历史

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | int | 否 | 50 | 返回数量（1-200） |

#### 5.2.16 排行榜 API (LeaderboardController)

**Base Path**: `/api/leaderboard`
**Tag**: 排行榜

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /combined | @RateLimit(5) | 综合排行榜（答题+游戏） |
| GET | /quiz | @RateLimit(5) | 答题排行榜 |
| GET | /game | @RateLimit(5) | 游戏排行榜 |

**GET /api/leaderboard/combined** - 综合排行榜

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| sortBy | String | 否 | total | 排序方式：total(总分)/quiz(答题)/game(游戏) |
| limit | Integer | 否 | 100 | 返回数量（1-200） |

```json
{
  "code": 200,
  "data": [
    {
      "rank": 1,
      "userId": 5,
      "username": "小明",
      "quizScore": 90,
      "gameScore": 80,
      "totalScore": 170
    }
  ]
}
```

#### 5.2.17 植物识别游戏 API (PlantGameController)

**Base Path**: `/api/plant-game`
**Tag**: 识药小游戏

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | /submit | @RateLimit(10) | 提交游戏结果（未登录也可玩，登录则记录） |
| GET | /records | 公开 | 我的游戏记录 |

**POST /api/plant-game/submit** - 提交游戏结果

请求体（PlantGameSubmitDTO）：包含难度、得分、正确数、总题数等信息。

返回：`R<Integer>`（得分）

#### 5.2.18 验证码 API (CaptchaController)

**Base Path**: `/api/captcha`
**Tag**: 验证码

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | / | @RateLimit(30) | 生成验证码 |

返回（CaptchaDTO）：

```json
{
  "code": 200,
  "data": {
    "captchaKey": "captcha:uuid-xxxx",
    "captchaImage": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

验证码生命周期：
1. 生成：SecureRandom生成4位随机字符，Graphics2D绘制图片，加入干扰线
2. 存储：captchaKey和验证码答案存入Redis，TTL 5分钟
3. 验证：用户提交验证码后从Redis获取答案比对（忽略大小写），验证后删除

#### 5.2.19 文件上传 API (FileUploadController)

**Base Path**: `/api/upload`
**权限**: @SaCheckRole("admin")（所有接口仅管理员可用）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /image | 单张图片上传 |
| POST | /images | 批量图片上传 |
| POST | /video | 单个视频上传 |
| POST | /videos | 批量视频上传 |
| POST | /document | 单个文档上传 |
| POST | /documents | 批量文档上传 |
| POST | /file | 通用文件上传（指定子目录） |
| DELETE | / | 删除文件 |

**POST /api/upload/image** - 图片上传

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| file | MultipartFile | 是 | - | 图片文件（最大10MB） |
| category | String | 否 | common | 分类目录 |

文件上传5层校验：
1. 文件非空校验
2. 文件大小校验（图片10MB/视频100MB/文档50MB）
3. 文件扩展名白名单校验
4. 文件MIME类型校验
5. 文件名安全校验（防止路径遍历）

#### 5.2.20 元数据 API (MetadataController)

**Base Path**: `/api/metadata`
**Tag**: 元数据

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | /filters | 公开 | 获取所有筛选选项（@Cacheable hotData） |
| GET | /featured | 公开 | 获取精选推荐内容（@Cacheable hotData） |

**GET /api/metadata/filters** - 筛选选项

```json
{
  "code": 200,
  "data": {
    "plants": {"categories": ["根茎类", "全草类"], "usageWays": ["外用", "内服", "药浴"]},
    "knowledge": {"therapyCategories": [], "diseaseCategories": [], "herbCategories": []},
    "qa": {"categories": ["侗药常识", "侗医疗法", "文化背景"]},
    "inheritors": {"levels": ["国家级", "省级", "市级", "县级"]},
    "resources": {"categories": ["入门", "进阶", "专业"]}
  }
}
```

**GET /api/metadata/featured** - 精选推荐

返回人气值最高的植物、知识条目、传承人，以及综合排名第一的"top"推荐。

#### 5.2.21 消息通知 API (NotificationController)

**Base Path**: `/api/notifications`
**Tag**: 消息通知

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | / | 公开 | 获取最近50条通知 |
| GET | /unread-count | 公开 | 获取未读数量 |
| POST | /read | 公开 | 标记所有已读 |

通知存储在Redis中，Key格式：`notification:user:{userId}`，未读计数Key：`notification:user:{userId}:unread`

#### 5.2.22 管理后台-内容管理 API (AdminContentController)

**Base Path**: `/api/admin`
**权限**: @SaCheckRole("admin")（所有接口仅管理员可用）

对以下5个实体提供完整CRUD操作：

| 实体 | GET列表 | POST创建 | PUT更新 | DELETE删除 |
|------|---------|----------|---------|-----------|
| 传承人 | /inheritors | /inheritors | /inheritors/{id} | /inheritors/{id} |
| 知识库 | /knowledge | /knowledge | /knowledge/{id} | /knowledge/{id} |
| 药用植物 | /plants | /plants | /plants/{id} | /plants/{id} |
| 问答 | /qa | /qa | /qa/{id} | /qa/{id} |
| 学习资源 | /resources | /resources | /resources/{id} | /resources/{id} |

每个创建/更新/删除操作后自动清除相关缓存（`service.clearCache()`），删除操作同时删除关联文件（`service.deleteWithFiles(id)`）。

#### 5.2.23 管理后台-互动管理 API (AdminInteractionController)

**Base Path**: `/api/admin`
**权限**: @SaCheckRole("admin")

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /feedback | 反馈列表（支持状态筛选） |
| PUT | /feedback/{id}/reply | 回复反馈 |
| DELETE | /feedback/{id} | 删除反馈 |
| GET | /comments | 评论列表（支持状态筛选） |
| PUT | /comments/{id}/approve | 审核通过评论 |
| PUT | /comments/{id}/reject | 审核拒绝评论 |
| DELETE | /comments/{id} | 删除评论 |

#### 5.2.24 管理后台-统计 API (AdminStatsController)

**Base Path**: `/api/admin`
**权限**: @SaCheckRole("admin")

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /stats | 各模块数量统计 |
| GET | /stats/user-growth | 7日用户增长趋势 |
| GET | /stats/content-views | 内容浏览量Top10 |
| GET | /stats/search-keywords | 搜索关键词Top10 |

**GET /api/admin/stats** - 数量统计

```json
{
  "code": 200,
  "data": {
    "users": 156,
    "knowledge": 56,
    "inheritors": 24,
    "plants": 65,
    "qa": 65,
    "resources": 40,
    "quiz": 70,
    "comments": 89,
    "feedback": 25
  }
}
```

#### 5.2.25 管理后台-用户管理 API (AdminUserController)

**Base Path**: `/api/admin`
**权限**: @SaCheckRole("admin")

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /users | 用户列表（passwordHash置null） |
| DELETE | /users/{id} | 删除用户 |
| PUT | /users/{id}/role | 修改用户角色 |
| PUT | /users/{id}/ban | 封禁用户（可选原因） |
| PUT | /users/{id}/unban | 解封用户 |

安全措施：`GET /users` 返回的用户列表中，`passwordHash` 字段强制设为 `null`，防止密码哈希泄露。

#### 5.2.26 数据导出 API (ExportController)

**Base Path**: `/api/admin/export`
**权限**: @SaCheckRole("admin")

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /{entity} | 导出CSV文件 |

支持的实体：`users`、`plants`、`knowledge`、`inheritors`、`resources`、`qa`、`comments`、`feedback`、`quiz-questions`

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| entity | String | 是 | - | 导出实体名称 |
| format | String | 否 | csv | 导出格式（目前仅支持csv） |

安全措施：
- 最多导出10000行数据
- 敏感字段（passwordHash/password/salt/token/secret）自动排除
- CSV文件添加BOM头（0xEF 0xBB 0xBF），确保Excel正确识别UTF-8编码
- 文件名格式：`{entity}_export_{yyyy-MM-dd}.csv`

#### 5.2.27 操作日志 API (OperationLogController)

**Base Path**: `/api/admin/logs`
**权限**: @SaCheckRole("admin")

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /list | 日志列表（支持模块/类型/用户名筛选，limit 1-100） |
| GET | /{id} | 日志详情 |
| DELETE | /{id} | 删除单条日志 |
| DELETE | /batch | 批量删除日志 |
| DELETE | /clear | 清空所有日志 |
| GET | /stats | 日志统计（总数+各类型数量） |

**GET /api/admin/logs/stats** - 日志统计

```json
{
  "code": 200,
  "data": {
    "total": 9504,
    "CREATE": 1200,
    "UPDATE": 3500,
    "DELETE": 804,
    "QUERY": 4000
  }
}
```

### 5.3 后端Service层详解

| 序号 | Service | 核心方法 | 缓存策略 |
|------|---------|----------|----------|
| 1 | PlantService | `advancedSearchPaged(keyword, category, usageWay, page, size)` - 高级分页搜索；`searchPaged(keyword, page, size)` - 全文搜索（ngram+LIKE降级）；`getDetailWithStory(id)` - 详情含故事；`getSimilarPlants(id)` - 同分类相似植物；`getRandomPlants(limit)` - 随机推荐；`getFilterOptions()` - 筛选选项；`getStats()` - 统计数据；`topByViewCount(limit)` - 浏览排行 | @Cacheable("plants")，TTL 6小时 |
| 2 | KnowledgeService | `advancedSearchPaged(keyword, therapy, disease, herb, sortBy, page, size)` - 三维筛选搜索；`getDetailWithRelated(id)` - 详情含关联植物；`addFavorite(userId, id)` - 收藏；`submitFeedback(knowledgeId, content)` - 反馈；`getFilterOptions()` - 筛选选项；`getStats()` - 统计 | @Cacheable("knowledges")，TTL 6小时 |
| 3 | InheritorService | `advancedSearchPaged(keyword, level, sortBy, page, size)` - 等级筛选搜索；`getDetailWithExtras(id)` - 详情含案例和荣誉；`getFilterOptions()` - 筛选选项；`getStats()` - 统计 | @Cacheable("inheritors")，TTL 6小时 |
| 4 | UserService | `login(username, password)` - 登录（BCrypt校验+Sa-Token登录+状态检查）；`register(username, password)` - 注册（用户名唯一+密码强度+BCrypt加密）；`changePassword(userId, current, new)` - 改密；`banUser(id, reason)` / `unbanUser(id)` - 封禁/解封；`updateUserRole(id, role)` - 角色修改 | @Cacheable("users")，TTL 30分钟 |
| 5 | CommentService | `addComment(comment)` - 发表评论（默认approved状态）；`listApprovedPaged(targetType, targetId, page, size)` - 已审核评论分页；`pageAllDTO(status, page, size)` - 管理端评论列表；`approveComment(id)` / `rejectComment(id)` - 审核 | 无缓存（实时性要求高） |
| 6 | FavoriteService | `addFavorite(userId, targetType, targetId)` - 添加收藏（唯一约束防重复）；`removeFavorite(userId, targetType, targetId)` - 取消收藏；`getMyFavoritesPaged(userId, page, size)` - 我的收藏分页（关联查询目标详情） | 无缓存 |
| 7 | FeedbackService | `submitFeedback(feedback, userId)` - 提交反馈（RabbitMQ异步+降级同步）；`replyFeedback(id, reply)` - 管理员回复；`countByStatus(status)` - 按状态计数 | 无缓存 |
| 8 | QuizService | `getRandomQuestions(count)` - 随机题目（ORDER BY RAND()）；`calculateScore(answers, scorePerQuestion)` - 计算得分（未登录）；`submit(userId, answers, scorePerQuestion)` - 提交并记录（已登录）；`addQuestionDirect/updateQuestionDirect/deleteQuestion` - CRUD | @Cacheable("quizQuestions")，TTL 12小时 |
| 9 | ResourceService | `pageByCategoryAndKeywordAndType(category, keyword, fileType, page, size)` - 分类+关键词+类型筛选；`getHotResources()` - 热门资源；`incrementDownload(id)` - 下载计数；`getAllCategories()` - 分类列表 | @Cacheable("resources")，TTL 4小时 |
| 10 | QaService | `advancedSearchPaged(keyword, category, page, size)` - 分类搜索；`getDetail(id)` - 详情；`getFilterOptions()` - 筛选选项；`getStats()` - 统计 | 无缓存 |
| 11 | OperationLogService | `getTrendLast7Days()` - 7日趋势统计；`clearAll()` - 清空日志（自动清理超限日志） | 无缓存 |
| 12 | PlantGameService | `calculateScore(dto)` - 客户端兼容评分；`submit(userId, dto)` - 服务端验证评分+记录；`getUserRecordsPaged(userId, page, size)` - 用户记录 | 无缓存 |
| 13 | AiChatService | `chatSync(message)` - 同步对话；`chatStream(message, sink)` - 流式对话（DeepSeek API）；系统提示词定义AI角色为侗医药专家 | 无缓存 |
| 14 | ChatHistoryService | `getUserSessions(userId)` - 获取用户会话列表；`getSessionHistory(userId, sessionId)` - 获取会话消息；`deleteSession(userId, sessionId)` - 删除会话；Redis暂存+MySQL持久化 | Redis暂存+MySQL |
| 15 | BrowseHistoryService | `record(userId, targetType, targetId)` - 记录浏览历史；`getMyHistory(userId, limit)` - 查询浏览历史 | 无缓存 |
| 16 | FileUploadService | `uploadImage/uploadVideo/uploadDocument/uploadFile` - 5层校验+存储+返回URL；`deleteFile(filePath)` - 安全删除文件；RabbitMQ文件处理任务 | 无缓存 |
| 17 | CaptchaService | `generateCaptcha()` - SecureRandom+Graphics2D生成验证码；`validateCaptchaOrThrow(key, code)` - Redis校验+删除 | Redis存储，TTL 5分钟 |
| 18 | PopularityAsyncService | `incrementPlantViewAndPopularity(id)` - 异步递增浏览量+人气值；同理支持knowledge/inheritor/resource/qa | @Async异步执行 |
| 19 | RabbitMQOperationLogService | `sendLog(log)` - 通过RabbitMQ异步发送操作日志；条件加载（@ConditionalOnProperty） | RabbitMQ |

### 5.4 后端Mapper层详解

所有Mapper接口继承 `BaseMapper<T>`，获得通用CRUD能力（selectById/insert/updateById/deleteById/selectList/selectPage等）。以下列出各Mapper的自定义方法：

| Mapper | 自定义方法 | SQL说明 |
|--------|-----------|---------|
| PlantMapper | `selectRandomPlants(limit)` | `SELECT * FROM plants ORDER BY RAND() LIMIT #{limit}` |
| | `countByCategory(limit)` | `SELECT category AS name, COUNT(*) AS value FROM plants GROUP BY category ORDER BY value DESC LIMIT #{limit}` |
| | `countByDistribution(limit)` | `SELECT distribution AS name, COUNT(*) AS value FROM plants GROUP BY distribution ORDER BY value DESC LIMIT #{limit}` |
| KnowledgeMapper | `countByTherapyCategory(limit)` | `SELECT therapy_category AS name, COUNT(*) AS value FROM knowledge GROUP BY therapy_category ORDER BY value DESC LIMIT #{limit}` |
| | `countDistinctTherapyCategory()` | `SELECT COUNT(DISTINCT therapy_category) FROM knowledge` |
| | `topByPopularity(limit)` | `SELECT id, title AS name, popularity AS value FROM knowledge ORDER BY popularity DESC LIMIT #{limit}` |
| | `topByViewCount(limit)` | `SELECT id, title AS name, view_count AS value FROM knowledge ORDER BY view_count DESC LIMIT #{limit}` |
| | `topFormulaByViewCount(limit)` | `SELECT id, formula AS name, view_count AS value FROM knowledge WHERE formula IS NOT NULL ORDER BY view_count DESC LIMIT #{limit}` |
| InheritorMapper | `countByLevel(level)` | `SELECT COUNT(*) FROM inheritors WHERE level = #{level}` |
| QaMapper | `topCategoryByPopularity(limit)` | `SELECT category AS name, SUM(popularity) AS value FROM qa GROUP BY category ORDER BY value DESC LIMIT #{limit}` |
| UserMapper | `countByDateLast7Days()` | `SELECT DATE(created_at) AS date, COUNT(*) AS count FROM users WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY DATE(created_at) ORDER BY date` |
| SearchHistoryMapper | `topKeywords(limit)` | `SELECT keyword, COUNT(*) AS count FROM search_history GROUP BY keyword ORDER BY count DESC LIMIT #{limit}` |
| ResourceMapper | - | 无自定义方法，全部使用BaseMapper通用方法 |
| OperationLogMapper | - | 无自定义方法 |
| CommentMapper | - | 无自定义方法 |
| FavoriteMapper | - | 无自定义方法 |
| FeedbackMapper | - | 无自定义方法 |
| QuizQuestionMapper | - | 无自定义方法 |
| QuizRecordMapper | - | 无自定义方法 |
| PlantGameRecordMapper | - | 无自定义方法 |
| ChatHistoryMapper | - | 无自定义方法 |
| BrowseHistoryMapper | - | 无自定义方法 |

### 5.5 后端Entity层详解

#### BaseEntity（基础实体）

所有继承BaseEntity的实体自动拥有以下字段：

| 字段 | 类型 | 注解 | 说明 |
|------|------|------|------|
| id | Integer | @TableId(type = IdType.AUTO) | 主键，自增 |
| createdAt | LocalDateTime | @TableField(value = "created_at", fill = FieldFill.INSERT) | 创建时间，插入时自动填充 |
| updatedAt | LocalDateTime | @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) | 更新时间，插入和更新时自动填充 |

#### User（用户）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK, AUTO_INCREMENT | 主键 |
| username | String | username | VARCHAR(20), NOT NULL, UNIQUE | 用户名 |
| passwordHash | String | password_hash | VARCHAR(100), NOT NULL | BCrypt密码哈希 |
| role | String | role | VARCHAR(20), NOT NULL, DEFAULT 'user' | 角色：user/admin |
| status | String | status | VARCHAR(20), DEFAULT 'active' | 状态：active/banned |
| createdAt | LocalDateTime | created_at | DATETIME, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updatedAt | LocalDateTime | updated_at | DATETIME, ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

静态常量：`STATUS_ACTIVE = "active"`, `STATUS_BANNED = "banned"`
方法：`isBanned()` - 判断用户是否被封禁

#### Plant（药用植物）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK, AUTO_INCREMENT | 主键 |
| nameCn | String | name_cn | VARCHAR(100), NOT NULL | 中文名称 |
| nameDong | String | name_dong | VARCHAR(100) | 侗语名称 |
| scientificName | String | scientific_name | VARCHAR(200) | 学名 |
| category | String | category | VARCHAR(20), NOT NULL | 分类：根茎类/全草类/花叶类/果实类/皮类 |
| usageWay | String | usage_way | VARCHAR(20), NOT NULL | 使用方式：外用/内服/药浴 |
| habitat | String | habitat | VARCHAR(200) | 生境 |
| efficacy | String | efficacy | VARCHAR(500) | 功效 |
| story | String | story | VARCHAR(2000) | 药用故事 |
| images | String | images | TEXT | 图片JSON数组 |
| videos | String | videos | TEXT | 视频JSON数组 |
| documents | String | documents | TEXT | 文档JSON数组 |
| distribution | String | distribution | TEXT | 地域分布 |
| difficulty | String | difficulty | VARCHAR(20) | 难度：easy/medium/hard |
| viewCount | Integer | view_count | INT, DEFAULT 0 | 浏览量 |
| favoriteCount | Integer | favorite_count | INT, DEFAULT 0 | 收藏量 |
| popularity | Integer | popularity | INT, DEFAULT 1 | 热度值 |
| updateLog | String | update_log | TEXT | 更新日志 |
| createdAt | LocalDateTime | created_at | DATETIME | 创建时间 |
| updatedAt | LocalDateTime | updated_at | DATETIME | 更新时间 |

校验注解：`@NotBlank(message = "中文名称不能为空")` 在nameCn上

#### Knowledge（知识库）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| title | String | title | VARCHAR(200), NOT NULL | 标题 |
| type | String | type | VARCHAR(20), NOT NULL | 类型：药方/疗法 |
| therapyCategory | String | therapy_category | VARCHAR(50) | 疗法分类 |
| diseaseCategory | String | disease_category | VARCHAR(50) | 疾病分类 |
| herbCategory | String | herb_category | VARCHAR(50) | 药材分类 |
| content | String | content | TEXT, NOT NULL | 内容 |
| formula | String | formula | TEXT | 配方 |
| usageMethod | String | usage_method | TEXT | 用法 |
| steps | String | steps | TEXT | 步骤 |
| images | String | images | TEXT | 图片JSON |
| videos | String | videos | TEXT | 视频JSON |
| documents | String | documents | TEXT | 文档JSON |
| relatedPlants | String | related_plants | TEXT | 关联植物ID列表 |
| updateLog | String | update_log | TEXT | 更新日志 |
| popularity | Integer | popularity | INT, DEFAULT 0 | 热度 |
| viewCount | Integer | view_count | INT, DEFAULT 0 | 浏览量 |
| favoriteCount | Integer | favorite_count | INT, DEFAULT 0 | 收藏量 |

#### Inheritor（传承人）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| name | String | name | VARCHAR(50), NOT NULL | 姓名 |
| level | String | level | VARCHAR(20), NOT NULL | 等级：国家级/省级/市级/县级 |
| bio | String | bio | VARCHAR(1000) | 从业履历 |
| specialties | String | specialties | VARCHAR(500) | 技艺特色 |
| experienceYears | Integer | experience_years | INT, NOT NULL | 经验年限 |
| videos | String | videos | TEXT | 视频JSON |
| images | String | images | TEXT | 图片JSON |
| documents | String | documents | TEXT | 资质文档JSON |
| representativeCases | String | representative_cases | TEXT | 代表案例 |
| honors | String | honors | VARCHAR(1000) | 荣誉资质 |
| updateLog | String | update_log | TEXT | 更新日志 |
| viewCount | Integer | view_count | INT, DEFAULT 0 | 浏览量 |
| favoriteCount | Integer | favorite_count | INT, DEFAULT 0 | 收藏量 |
| popularity | Integer | popularity | INT, DEFAULT 0 | 热度值 |

#### Qa（问答）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| category | String | category | VARCHAR(50) | 分类：侗药常识/侗医疗法/文化背景 |
| question | String | question | TEXT, NOT NULL | 问题 |
| answer | String | answer | TEXT, NOT NULL | 答案 |
| popularity | Integer | popularity | INT, DEFAULT 0 | 热度 |
| viewCount | Integer | view_count | INT, DEFAULT 0 | 浏览量 |
| favoriteCount | Integer | favorite_count | INT, DEFAULT 0 | 收藏量 |

#### Resource（学习资源）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| title | String | title | VARCHAR(200), NOT NULL | 标题 |
| category | String | category | VARCHAR(20), NOT NULL | 分类：入门/进阶/专业 |
| files | String | files | TEXT | 文件列表JSON数组 |
| description | String | description | VARCHAR(1000) | 描述 |
| updateLog | String | update_log | TEXT | 更新日志 |
| viewCount | Integer | view_count | INT, DEFAULT 0 | 浏览量 |
| downloadCount | Integer | download_count | INT, DEFAULT 0 | 下载次数 |
| favoriteCount | Integer | favorite_count | INT, DEFAULT 0 | 收藏量 |
| popularity | Integer | popularity | INT, DEFAULT 0 | 热度 |

#### Comment（评论）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| userId | Integer | user_id | INT, NOT NULL | 用户ID |
| username | String | username | VARCHAR(50), NOT NULL | 用户名 |
| targetType | String | target_type | VARCHAR(50), NOT NULL | 目标类型 |
| targetId | Integer | target_id | INT, NOT NULL | 目标ID |
| content | String | content | VARCHAR(1000), NOT NULL | 评论内容 |
| replyToId | Integer | reply_to_id | INT | 回复的评论ID |
| replyToUserId | Integer | reply_to_user_id | INT | 回复的用户ID |
| replyToUsername | String | reply_to_username | VARCHAR(50) | 回复的用户名 |
| likes | Integer | likes | INT, DEFAULT 0 | 点赞数 |
| replyCount | Integer | reply_count | INT, DEFAULT 0 | 回复数 |
| status | String | status | VARCHAR(20), DEFAULT 'approved' | 状态：pending/approved/rejected |

#### Favorite（收藏）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| userId | Integer | user_id | INT, NOT NULL | 用户ID |
| targetType | String | target_type | VARCHAR(50), NOT NULL | 目标类型 |
| targetId | Integer | target_id | INT, NOT NULL | 目标ID |
| createdAt | LocalDateTime | created_at | DATETIME | 创建时间 |

唯一约束：`uk_user_target(user_id, target_type, target_id)`

#### Feedback（反馈）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| userId | Integer | user_id | INT | 用户ID |
| userName | String | user_name | VARCHAR(50), NOT NULL | 用户名 |
| type | String | type | VARCHAR(20), NOT NULL | 类型：建议/问题/其他 |
| title | String | title | VARCHAR(200), NOT NULL | 标题 |
| content | String | content | TEXT, NOT NULL | 内容 |
| contact | String | contact | VARCHAR(100) | 联系方式 |
| status | String | status | VARCHAR(20), DEFAULT 'pending' | 状态：pending/resolved |
| reply | String | reply | TEXT | 回复内容 |
| createdAt | LocalDateTime | created_at | DATETIME | 创建时间 |

#### QuizQuestion（测验题目）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| question | String | question | TEXT, NOT NULL | 题目内容 |
| options | String | options | TEXT, NOT NULL, @JsonIgnore | 选项JSON数组（内部存储） |
| answer | String | answer | VARCHAR(500), NOT NULL | 正确答案 |
| category | String | category | VARCHAR(50) | 题目分类 |
| correctAnswer | String | correct_answer | VARCHAR(10) | 正确选项：A/B/C/D |
| explanation | String | explanation | TEXT | 答案解析 |

特殊方法：`getOptionList()` / `setOptionList(List<String>)` - JSON数组与List的自动转换

#### QuizRecord（测验记录）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| userId | Integer | user_id | INT, NOT NULL | 用户ID |
| score | Integer | score | INT, NOT NULL | 得分 |
| totalQuestions | Integer | total_questions | INT, NOT NULL | 总题数 |
| correctAnswers | Integer | correct_answers | INT, NOT NULL | 正确数 |
| createdAt | LocalDateTime | created_at | DATETIME | 答题时间 |

#### PlantGameRecord（植物游戏记录）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| userId | Integer | user_id | INT, NOT NULL | 用户ID |
| difficulty | String | difficulty | VARCHAR(20), NOT NULL | 难度：easy/medium/hard |
| score | Integer | score | INT, NOT NULL | 得分 |
| correctCount | Integer | correct_count | INT, NOT NULL | 正确数 |
| totalCount | Integer | total_count | INT, NOT NULL | 总题数 |
| createdAt | LocalDateTime | created_at | DATETIME | 游戏时间 |

#### OperationLog（操作日志）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Integer | id | PK | 主键 |
| userId | Integer | user_id | INT | 用户ID |
| username | String | username | VARCHAR(50) | 用户名 |
| module | String | module | VARCHAR(50), NOT NULL | 模块名称 |
| operation | String | operation | VARCHAR(100), NOT NULL | 操作描述 |
| type | String | type | VARCHAR(20), NOT NULL | 操作类型：CREATE/UPDATE/DELETE/QUERY |
| method | String | method | VARCHAR(200) | 请求方法 |
| params | String | params | TEXT | 请求参数 |
| ip | String | ip | VARCHAR(50) | IP地址 |
| duration | Integer | duration | INT | 执行时长(ms) |
| success | Boolean | success | TINYINT, DEFAULT 1 | 是否成功 |
| errorMsg | String | error_msg | TEXT | 错误信息 |

#### ChatHistory（聊天历史）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Long | id | PK | 主键 |
| userId | Integer | user_id | INT | 用户ID |
| sessionId | String | session_id | VARCHAR(64), NOT NULL | 会话ID(UUID) |
| role | String | role | VARCHAR(20), NOT NULL | 角色：user/assistant |
| content | String | content | TEXT, NOT NULL | 消息内容 |
| createdAt | LocalDateTime | created_at | DATETIME | 创建时间 |

#### BrowseHistory（浏览历史）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Long | id | PK | 主键 |
| userId | Integer | user_id | INT, NOT NULL | 用户ID |
| targetType | String | target_type | VARCHAR(50), NOT NULL | 目标类型 |
| targetId | Integer | target_id | INT, NOT NULL | 目标ID |
| createdAt | LocalDateTime | created_at | DATETIME | 浏览时间 |

#### SearchHistory（搜索历史）

| 字段 | 类型 | 数据库列 | 约束 | 说明 |
|------|------|----------|------|------|
| id | Long | id | PK | 主键 |
| userId | Integer | user_id | INT | 用户ID（可为空，游客搜索） |
| keyword | String | keyword | VARCHAR(200), NOT NULL | 搜索关键词 |
| createdAt | LocalDateTime | created_at | DATETIME | 搜索时间 |

### 5.6 后端Config层详解

| 配置类 | 功能 | 关键配置 |
|--------|------|----------|
| CacheConfig | 双层缓存管理器 | Caffeine L1（maxSize=1000, writeTTL=10min, accessTTL=30min）+ Redis L2（各缓存区不同TTL），Redis不可用时自动降级为Caffeine单层 |
| SaTokenConfig | 认证授权+路由拦截+CORS | POST/PUT/DELETE/PATCH需登录，只读GET接口排除，CORS允许配置的origins |
| MybatisPlusConfig | MyBatis-Plus分页插件 | 注册PaginationInnerInterceptor，数据库类型MySQL |
| MyMetaObjectHandler | 自动填充处理器 | INSERT时填充createdAt，INSERT_UPDATE时填充updatedAt |
| RabbitMQConfig | 消息队列配置 | 定义5个队列/交换机/绑定：operation-log/feedback/file-process/notification/statistics |
| AsyncConfig | 异步任务配置 | 线程池配置，@EnableAsync |
| WebSocketConfig | WebSocket配置 | 注册ChatWebSocketHandler到/ws/chat，允许配置的origins |
| RateLimitAspect | 限流AOP切面 | 基于@RateLimit注解，使用Guava RateLimiter实现令牌桶限流 |
| OperationLogAspect | 操作日志AOP切面 | 拦截管理后台写操作，自动记录操作日志，通过RabbitMQ异步发送 |
| RequestIdFilter | 请求ID过滤器 | 为每个请求生成UUID，写入MDC和响应头X-Request-Id |
| XssFilter | XSS过滤器 | 对请求体进行HTML标签过滤，防止XSS攻击 |
| RequestSizeFilter | 请求大小过滤器 | 检查请求体大小不超过配置的maxBodySize（默认10MB） |
| DeepSeekConfig | DeepSeek AI配置 | API Key、Base URL、Model、超时时间 |
| FileUploadProperties | 文件上传属性 | 图片10MB/视频100MB/文档50MB，扩展名白名单 |
| OpenApiConfig | API文档配置 | 分组信息、标题、描述 |
| AdminDataInitializer | 管理员初始化 | 启动时检查admin用户是否存在，不存在则创建 |
| StartupInfoPrinter | 启动信息打印 | 打印应用启动信息、访问地址、环境变量 |
| SecurityConfigValidator | 安全配置校验 | 校验JWT_SECRET等关键配置是否已设置 |
| StpInterfaceImpl | Sa-Token权限接口 | 实现getPermissionList/getRoleList，从用户角色返回权限列表 |
| WebMvcConfig | Web MVC配置 | 静态资源映射、拦截器配置 |
| LoggingAspect | 日志AOP切面 | 记录Service层方法执行时间和参数 |
| CacheHealthIndicator | 缓存健康指标 | 检查Caffeine和Redis缓存状态 |
| DatabaseHealthIndicator | 数据库健康指标 | 检查数据库连接和查询能力 |
| RedisHealthIndicator | Redis健康指标 | 检查Redis连接和PING响应 |
| SensitiveDataConverter | 敏感数据转换器 | 日志中自动脱敏password/token等字段 |

### 5.7 后端MQ层详解

#### 消息生产者（5个）

| Producer | 队列 | 消息内容 | 触发场景 |
|----------|------|----------|----------|
| OperationLogProducer | dong.medicine.operation.log | OperationLog对象 | 管理后台写操作（创建/更新/删除） |
| FeedbackProducer | dong.medicine.feedback | Feedback对象 | 用户提交反馈 |
| FileProcessProducer | dong.medicine.file.process | 文件处理任务 | 文件上传后需要后处理（缩略图/转码等） |
| NotificationProducer | dong.medicine.notification | 通知消息 | 系统通知（反馈回复/审核结果等） |
| StatisticsProducer | dong.medicine.statistics | 统计更新事件 | 内容浏览/收藏/下载等事件触发统计更新 |

#### 消息消费者（5个）

| Consumer | 队列 | 处理逻辑 | 降级策略 |
|----------|------|----------|----------|
| OperationLogConsumer | dong.medicine.operation.log | 将操作日志写入MySQL operation_log表 | RabbitMQ不可用时由OperationLogAspect直接写入 |
| FeedbackConsumer | dong.medicine.feedback | 将反馈写入MySQL feedback表 | RabbitMQ不可用时由FeedbackService同步写入 |
| FileProcessConsumer | dong.medicine.file.process | 执行文件后处理任务 | 任务丢失不影响核心功能 |
| NotificationConsumer | dong.medicine.notification | 将通知写入Redis List | 通知丢失不影响核心功能 |
| StatisticsConsumer | dong.medicine.statistics | 更新统计计数 | 统计延迟不影响核心功能 |

#### 消息流转图

```
生产者                    RabbitMQ                     消费者
+----------+         +--------------+          +--------------+
| Operation |---send->| operation.log|---consume->| 写入MySQL     |
| LogAspect |         |    Queue     |          | operation_log|
+----------+         +--------------+          +--------------+

+----------+         +--------------+          +--------------+
| Feedback  |---send->|  feedback    |---consume->| 写入MySQL     |
| Service   |         |    Queue     |          |  feedback    |
+----------+         +--------------+          +--------------+

+----------+         +--------------+          +--------------+
| FileUpload|---send->| file.process |---consume->| 文件后处理    |
| Service   |         |    Queue     |          | (缩略图等)   |
+----------+         +--------------+          +--------------+

+----------+         +--------------+          +--------------+
| System    |---send->| notification |---consume->| 写入Redis     |
| Events    |         |    Queue     |          | 通知列表     |
+----------+         +--------------+          +--------------+

+----------+         +--------------+          +--------------+
| Popularity|---send->| statistics   |---consume->| 更新统计计数  |
| Service   |         |    Queue     |          |              |
+----------+         +--------------+          +--------------+
```

### 5.8 后端Common模块详解

#### R\<T\> - 统一响应封装

所有API返回值的统一封装类，包含4个字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码：200成功，400参数错误，401未授权，403禁止，404未找到，500服务器错误 |
| msg | String | 消息描述 |
| data | T | 业务数据（泛型） |
| requestId | String | 请求追踪ID（从MDC获取） |

静态工厂方法：`ok()` / `ok(data)` / `ok(msg, data)` / `error(msg)` / `error(code, msg)` / `error(ErrorCode)` / `unauthorized(msg)` / `forbidden(msg)` / `notFound(msg)` / `badRequest(msg)`

实例方法：`isSuccess()` - 判断是否成功（code == 200）

#### ErrorCode - 错误码枚举

按模块分类的错误码体系：

| 范围 | 模块 | 示例 |
|------|------|------|
| 200 | 成功 | SUCCESS(200, "操作成功") |
| 1xxx | 用户 | USER_NOT_FOUND(1001), USER_ALREADY_EXISTS(1002), PASSWORD_WRONG(1003), PASSWORD_TOO_WEAK(1004), TOKEN_EXPIRED(1005), ACCOUNT_DISABLED(1008) |
| 2xxx | 资源 | RESOURCE_NOT_FOUND(2001), PLANT_NOT_FOUND(2002), KNOWLEDGE_NOT_FOUND(2003) |
| 3xxx | 参数 | PARAM_ERROR(3001), PARAM_MISSING(3002), PARAM_FORMAT_ERROR(3003) |
| 4xxx | 文件 | FILE_UPLOAD_ERROR(4001), FILE_TYPE_NOT_ALLOWED(4002), FILE_SIZE_EXCEEDED(4003) |
| 5xxx | 操作 | DUPLICATE_OPERATION(5001), OPERATION_TOO_FREQUENT(5002) |
| 6xxx | 基础设施 | DATABASE_ERROR(6001), CACHE_ERROR(6002), NETWORK_ERROR(6003) |
| 7xxx | AI | AI_SERVICE_ERROR(7001), AI_RESPONSE_ERROR(7002) |
| 9xxx | 系统 | SYSTEM_ERROR(9001), UNKNOWN_ERROR(9999) |

#### BusinessException - 业务异常

继承RuntimeException，携带ErrorCode。提供静态工厂方法便于快速创建常见异常：

```java
throw BusinessException.notFound("植物不存在");      // 404
throw BusinessException.badRequest("参数错误");       // 400
throw BusinessException.unauthorized("未登录");      // 401
throw BusinessException.forbidden("权限不足");        // 403
throw BusinessException.userAlreadyExists();          // 1002
throw BusinessException.passwordWrong();              // 1003
```

#### GlobalExceptionHandler - 全局异常处理器

使用 `@ControllerAdvice` 统一捕获异常并返回 `R<T>` 格式响应：

| 异常类型 | 处理方式 | 返回码 |
|----------|----------|--------|
| BusinessException | 提取ErrorCode | errorCode.getCode() |
| MethodArgumentNotValidException | 提取校验错误信息 | 400 |
| ConstraintViolationException | 提取约束违反信息 | 400 |
| HttpRequestMethodNotSupportedException | 请求方法不支持 | 405 |
| MissingServletRequestParameterException | 缺少请求参数 | 400 |
| NotLoginException (Sa-Token) | 未登录 | 401 |
| NotRoleException (Sa-Token) | 角色不足 | 403 |
| Exception (兜底) | 未知错误 | 500 |

#### SecurityUtils - 安全工具

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getCurrentUserId() | Integer | 获取当前登录用户ID（未登录抛异常） |
| getCurrentUserIdOrNull() | Integer | 获取当前登录用户ID（未登录返回null） |
| getCurrentUsername() | String | 获取当前用户名 |
| getCurrentUsernameOrNull() | String | 获取当前用户名（未登录返回null） |
| getCurrentUserRole() | String | 获取当前用户角色 |

#### XssUtils - XSS过滤工具

| 方法 | 说明 |
|------|------|
| clean(String) | 清除HTML标签，防止XSS |
| stripHtmlTags(String) | 剥离所有HTML标签 |

#### PageUtils - 分页工具

| 方法 | 说明 |
|------|------|
| getPage(int current, int size) | 创建MyBatis-Plus的Page对象 |
| toMap(Page\<T\> page) | 将Page对象转为Map（包含records/total/current/size/pages） |

#### PasswordValidator - 密码验证器

验证密码强度：必须包含字母和数字，长度至少6位。

#### IpUtils - IP工具

从HttpServletRequest中获取真实IP地址，支持X-Forwarded-For/X-Real-IP代理头解析。

#### FileCleanupHelper - 文件清理辅助

提供文件删除辅助方法，确保文件删除操作的安全性。

#### FileTypeUtils - 文件类型工具

根据文件扩展名判断MIME类型和显示名称，用于文件下载和预览。

#### SensitiveDataUtils - 敏感数据脱敏

对日志和导出数据中的敏感字段进行脱敏处理。

### 5.9 前端页面详解

| 页面 | 路由 | 功能描述 | 关键组件 | 关键Composable |
|------|------|----------|----------|----------------|
| Home | / | 平台首页，展示精选推荐（植物/知识/传承人）、数据概览、快捷入口、AI聊天入口 | AiChatCard, StatCard, CardGrid, UpdateLogCard | useVisualData, useUpdateLog |
| Plants | /plants | 药用植物图鉴，卡片网格展示，支持分类/用法/关键词筛选，点击查看详情 | CardGrid, SearchFilter, Pagination, PlantDetailDialog | useInteraction, useFavorite |
| Knowledge | /knowledge | 非遗知识库，列表/卡片切换，三维筛选（疗法/疾病/药材），收藏和反馈 | SearchFilter, Pagination, KnowledgeDetailDialog | useInteraction, useFavorite |
| Inheritors | /inheritors | 传承人风采，等级筛选，排序，详情查看（案例/荣誉/多媒体） | CardGrid, SearchFilter, InheritorDetailDialog | useInteraction, useFavorite |
| Qa | /qa | 问答社区，分类浏览，搜索，AI聊天入口 | SearchFilter, Pagination, AiChatCard | useInteraction, keepAlive缓存 |
| Interact | /interact | 文化互动，趣味答题+植物识别游戏+评论区 | QuizSection, PlantGame, CommentSection, InteractSidebar | useQuiz, usePlantGame, useInteraction |
| Resources | /resources | 学习资源，分类浏览，文件类型筛选，下载/批量下载/预览 | DocumentList, DocumentPreview, Pagination | useMedia, useInteraction |
| Visual | /visual | 数据可视化大屏，ECharts图表展示 | ChartCard | useVisualData |
| PersonalCenter | /personal | 个人中心（需登录），学习统计/收藏/答题记录/浏览历史/修改密码 | StatsDashboard, FavoritesPanel, QuizHistoryPanel, BrowseHistoryPanel, SettingsPanel | usePersonalCenter, useStudyStats, useBrowseHistory, useFavorite |
| Admin | /admin | 管理后台（需admin角色），仪表盘+内容CRUD+用户管理+互动管理+日志+导出 | AdminDashboard, AdminDataTable, AdminSidebar, FormDialogs, DetailDialogs | useAdminData |
| About | /about | 关于平台，项目介绍、团队信息、技术栈 | - | - |
| Feedback | /feedback | 意见反馈，反馈表单+状态跟踪 | - | - |
| GlobalSearch | /search | 全局搜索，自动补全/热门搜索/分类结果/关键词高亮 | SearchFilter | - |
| NotFound | /404 | 404页面，友好的错误提示和返回首页按钮 | - | - |

### 5.10 前端组件架构详解

前端组件采用三层架构设计：

```
+---------------------------------------------------------+
|                  业务组件层                                |
|  business/ - 与具体业务逻辑紧密耦合的组件                   |
|  +-- admin/     管理后台专用组件                           |
|  +-- dialogs/   详情对话框组件                             |
|  +-- display/   数据展示组件（CardGrid/ChartCard）         |
|  +-- interact/  互动组件（Quiz/PlantGame/Comment）         |
|  +-- layout/    布局组件（Header/Footer）                  |
|  +-- media/     媒体组件（Video/Image/Document）           |
|  +-- upload/    上传组件（Image/Video/Document）           |
+---------------------------------------------------------+
|                  通用组件层                                |
|  common/ - 与业务无关的通用UI组件                           |
|  +-- PageLoading.vue      页面加载动画                     |
|  +-- SkeletonGridCard.vue 卡片骨架屏                       |
|  +-- SkeletonGridImage.vue 图片骨架屏                      |
|  +-- SkeletonListQa.vue   问答骨架屏                       |
|  +-- SkeletonListResource.vue 资源骨架屏                   |
+---------------------------------------------------------+
|                  基础组件层                                |
|  base/ - 最底层的功能组件                                  |
|  +-- ErrorBoundary.vue    错误边界（捕获子组件异常）        |
|  +-- VirtualList.vue      虚拟滚动列表（大数据量）          |
+---------------------------------------------------------+
```

### 5.11 前端Composable详解

| Composable | 功能 | 核心方法/状态 |
|------------|------|---------------|
| useAdminData | 管理后台数据获取与操作 | fetchUsers/fetchPlants/fetchKnowledge等CRUD操作，分页/筛选/表单对话框状态管理 |
| useBrowseHistory | 浏览历史管理 | fetchHistory(), recordHistory(type, id), histories列表 |
| useChatSessions | AI聊天会话管理 | sessions列表, currentSessionId, createSession(), switchSession(), deleteSession() |
| useChatWebSocket | WebSocket聊天通信 | connect(), sendMessage(), disconnect(), messages列表, 连接状态 |
| useDebounce | 防抖函数封装 | debounce(fn, delay), 返回防抖后的函数 |
| useErrorHandler | 统一错误处理 | handleError(error), 根据错误类型显示不同的ElMessage提示 |
| useFavorite | 收藏功能 | toggleFavorite(type, id), isFavorited(type, id), fetchMyFavorites() |
| useFileUpload | 文件上传逻辑 | uploadImage/uploadVideo/uploadDocument, 进度跟踪, 文件校验 |
| useFormDialog | 表单对话框状态 | visible, formData, openDialog(data), closeDialog(), submitForm() |
| useInteraction | 交互功能聚合 | 分页/筛选/排序/评论/统计等交互逻辑的统一管理 |
| useMedia | 媒体处理 | parseMediaList(), getMediaType(), 文档预览URL生成, KKFileView集成 |
| usePersonalCenter | 个人中心功能 | fetchUserInfo(), updateProfile(), changePassword(), 学习统计 |
| usePlantGame | 植物识别游戏 | startGame(), submitAnswer(), score, currentQuestion, difficulty |
| useQuiz | 趣味答题 | fetchQuestions(), submitAnswers(), score, questions, currentQuestion |
| useStudyStats | 学习统计 | fetchStats(), stats（收藏数/答题数/浏览数等） |
| useUpdateLog | 更新日志 | fetchUpdateLogs(), logs列表 |
| useVisualData | 数据可视化 | fetchOverview(), fetchChartData(), fetchTrend(), ECharts数据格式化 |

### 5.12 前端工具函数详解

| 工具文件 | 导出函数 | 说明 |
|----------|----------|------|
| index.js | formatTime(time, options) | 时间格式化，支持相对时间（"3分钟前"）和多种格式（date/time/datetime/full） |
| | extractData(res) | 从API响应中提取数据数组，自动适配多种响应结构 |
| | extractPageData(res) | 提取分页数据（records/total/page/size） |
| | formatFileSize(bytes) | 文件大小格式化（"1.5 MB"） |
| | truncate(str, length, suffix) | 文本截断 |
| | debounce(func, wait) | 防抖函数 |
| | throttle(func, limit) | 节流函数 |
| | deepClone(obj) | 深拷贝 |
| | isEmpty(value) / isNotEmpty(value) | 空值判断 |
| | generateId() | 生成唯一ID |
| | sleep(ms) | 延迟函数 |
| | retry(func, retries, delay) | 重试函数 |
| | getImageUrl(path) | 图片URL处理（相对路径补全） |
| | getFirstImage(images) | 获取第一张图片URL（支持JSON数组/逗号分隔/数组） |
| | getScoreLevel/Emoji/Text(score) | 答题评分等级/表情/文案 |
| request.js | default (Axios实例) | HTTP请求封装，含Token自动注入/401刷新/重试/XSS防护/重复请求取消 |
| | cancelAllRequests() | 取消所有待处理请求 |
| | cancelRequestByUrl(url) | 按URL取消请求 |
| cache.js | CacheManager | 前端两层缓存（内存Map + sessionStorage），TTL过期机制 |
| chartConfig.js | 各种ECharts配置 | 柱状图/饼图/雷达图/折线图等图表配置工厂函数 |
| logger.js | logUploadError/logAuthWarn/logSecurityWarn等 | 分级日志工具，安全警告/上传错误/权限警告等 |
| media.js | parseMediaList/stringifyMediaList | 媒体列表解析/序列化，文件类型判断，下载功能 |
| | downloadMedia/downloadDocument | 媒体文件下载 |
| | getFileType/getFileIcon/getFileColor | 文件类型/图标/颜色映射 |
| adminUtils.js | 管理后台工具函数 | 表格列配置/表单校验规则/状态标签映射 |
| validators.js | 表单验证器 | 用户名/密码/手机号/邮箱等验证规则 |
| xss.js | sanitize/containsXss/containsSqlInjection | XSS/SQL注入检测和净化，stripHtmlTags |

### 5.13 前端自定义指令详解

| 指令 | 用法 | 说明 |
|------|------|------|
| v-lazy | `<img v-lazy="imageUrl">` | 图片懒加载，使用IntersectionObserver，进入视口50px时加载，添加lazy-loading/lazy-loaded类名 |
| v-lazy-background | `<div v-lazy-background="bgUrl">` | 背景图懒加载，同v-lazy机制，设置el.style.backgroundImage |
| v-debounce | `<button v-debounce:click="300">` | 防抖指令，默认300ms，支持自定义事件类型和延迟时间 |
| v-throttle | `<button v-throttle:click="500">` | 节流指令，默认300ms，支持自定义事件类型和间隔时间 |
| v-click-outside | `<div v-click-outside="handleClick">` | 点击外部检测，点击元素外部时触发回调，用于关闭下拉菜单等 |
| v-focus | `<input v-focus>` | 自动聚焦，元素挂载时自动获取焦点 |
| v-permission | `<button v-permission="['admin']">` | 权限控制，根据用户角色决定是否显示元素，admin角色始终可见 |
| v-loading | `<div v-loading="isLoading">` | 加载状态，显示旋转加载动画，支持动态切换 |

## 六、数据库设计

### 6.1 全部16张表

#### users - 用户表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| username | VARCHAR(20) | NOT NULL | - | UNIQUE | 用户名 |
| password_hash | VARCHAR(100) | NOT NULL | - | - | BCrypt密码哈希 |
| role | VARCHAR(20) | NOT NULL | 'user' | - | 角色：user/admin |
| status | VARCHAR(20) | - | 'active' | - | 状态：active/banned |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

#### plants - 药用植物表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| name_cn | VARCHAR(100) | NOT NULL | - | idx_name_cn | 中文名称 |
| name_dong | VARCHAR(100) | - | - | idx_name_dong | 侗语名称 |
| scientific_name | VARCHAR(200) | - | - | - | 学名 |
| category | VARCHAR(20) | NOT NULL | - | idx_plants_category, idx_plants_category_usage | 分类 |
| usage_way | VARCHAR(20) | NOT NULL | - | idx_usage_way, idx_plants_category_usage | 使用方式 |
| habitat | VARCHAR(200) | - | - | - | 生境 |
| efficacy | VARCHAR(500) | - | - | - | 功效 |
| story | VARCHAR(2000) | - | - | - | 药用故事 |
| images | TEXT | - | - | - | 图片JSON数组 |
| videos | TEXT | - | - | - | 视频JSON数组 |
| documents | TEXT | - | - | - | 文档JSON数组 |
| distribution | TEXT | - | - | - | 地域分布 |
| difficulty | VARCHAR(20) | - | - | idx_plants_difficulty | 难度：easy/medium/hard |
| view_count | INT | - | 0 | idx_plants_view_count | 浏览量 |
| favorite_count | INT | - | 0 | - | 收藏量 |
| popularity | INT | - | 1 | idx_popularity | 热度值 |
| update_log | TEXT | - | - | - | 更新日志 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | - | - | 更新时间 |

全文索引：`ft_knowledge_search(title, content) WITH PARSER ngram`

#### inheritors - 传承人表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| name | VARCHAR(50) | NOT NULL | - | - | 姓名 |
| level | VARCHAR(20) | NOT NULL | - | idx_level | 等级：国家级/省级/市级/县级 |
| bio | VARCHAR(1000) | - | - | - | 从业履历 |
| specialties | VARCHAR(500) | - | - | - | 技艺特色 |
| experience_years | INT | NOT NULL | - | - | 经验年限 |
| videos | TEXT | - | - | - | 视频JSON |
| images | TEXT | - | - | - | 图片JSON |
| documents | TEXT | - | - | - | 资质文档JSON |
| representative_cases | TEXT | - | - | - | 代表案例 |
| honors | VARCHAR(1000) | - | - | - | 荣誉资质 |
| update_log | TEXT | - | - | - | 更新日志 |
| view_count | INT | - | 0 | - | 浏览量 |
| favorite_count | INT | - | 0 | - | 收藏量 |
| popularity | INT | - | 0 | idx_popularity | 热度值 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | - | - | 更新时间 |

全文索引：`ft_inheritors_search(name, specialties, bio) WITH PARSER ngram`

#### qa - 问答表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| category | VARCHAR(50) | - | - | idx_category | 分类：侗药常识/侗医疗法/文化背景 |
| question | TEXT | NOT NULL | - | - | 问题 |
| answer | TEXT | NOT NULL | - | - | 答案 |
| popularity | INT | - | 0 | idx_popularity | 热度 |
| view_count | INT | - | 0 | - | 浏览量 |
| favorite_count | INT | - | 0 | - | 收藏量 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | - | - | 更新时间 |

全文索引：`ft_qa_search(question, answer) WITH PARSER ngram`

#### resources - 学习资源表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| title | VARCHAR(200) | NOT NULL | - | - | 标题 |
| category | VARCHAR(20) | NOT NULL | - | idx_category | 分类：入门/进阶/专业 |
| files | TEXT | - | - | - | 文件列表JSON数组 |
| description | VARCHAR(1000) | - | - | - | 描述 |
| download_count | INT | NOT NULL | 0 | - | 下载次数 |
| popularity | INT | NOT NULL | 0 | idx_popularity | 热度 |
| view_count | INT | - | 0 | - | 浏览量 |
| favorite_count | INT | - | 0 | - | 收藏量 |
| update_log | TEXT | - | - | - | 更新日志 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

全文索引：`ft_resources_search(title, description) WITH PARSER ngram`

#### comments - 评论表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | NOT NULL | - | idx_user_id | 用户ID |
| username | VARCHAR(50) | NOT NULL | - | - | 用户名 |
| target_type | VARCHAR(50) | NOT NULL | - | idx_target | 目标类型：plant/knowledge/inheritor |
| target_id | INT | NOT NULL | - | idx_target | 目标ID |
| content | VARCHAR(1000) | NOT NULL | - | - | 评论内容 |
| reply_to_id | INT | - | - | idx_reply_to_id | 回复的评论ID |
| reply_to_user_id | INT | - | - | - | 回复的用户ID |
| reply_to_username | VARCHAR(50) | - | - | - | 回复的用户名 |
| likes | INT | - | 0 | idx_likes | 点赞数 |
| reply_count | INT | - | 0 | - | 回复数 |
| status | VARCHAR(20) | - | 'approved' | idx_status | 状态：pending/approved/rejected |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 创建时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

#### favorites - 收藏表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | NOT NULL | - | idx_user_id, uk_user_target | 用户ID |
| target_type | VARCHAR(50) | NOT NULL | - | uk_user_target | 目标类型 |
| target_id | INT | NOT NULL | - | uk_user_target | 目标ID |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

唯一约束：`uk_user_target(user_id, target_type, target_id)` - 同一用户对同一目标只能收藏一次

#### feedback - 反馈表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | - | - | idx_user_id | 用户ID |
| user_name | VARCHAR(50) | NOT NULL | - | - | 用户名 |
| type | VARCHAR(20) | NOT NULL | - | - | 类型：建议/问题/其他 |
| title | VARCHAR(200) | NOT NULL | - | - | 标题 |
| content | TEXT | NOT NULL | - | - | 内容 |
| contact | VARCHAR(100) | - | - | - | 联系方式 |
| status | VARCHAR(20) | - | 'pending' | idx_status | 状态：pending/resolved |
| reply | TEXT | - | - | - | 回复内容 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

#### quiz_questions - 测验题目表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| question | TEXT | NOT NULL | - | - | 题目内容 |
| options | TEXT | NOT NULL | - | - | 选项JSON数组 `["A","B","C","D"]` |
| answer | VARCHAR(500) | NOT NULL | - | - | 正确答案 |
| category | VARCHAR(50) | - | - | - | 题目分类 |
| difficulty | VARCHAR(20) | - | 'easy' | - | 难度：easy/medium/hard |
| correct_answer | VARCHAR(10) | - | - | - | 正确选项：A/B/C/D |
| explanation | TEXT | - | - | - | 答案解析 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

#### quiz_record - 测验记录表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | NOT NULL | - | idx_user_id | 用户ID |
| score | INT | NOT NULL | - | - | 得分 |
| total_questions | INT | NOT NULL | - | - | 总题数 |
| correct_answers | INT | NOT NULL | - | - | 正确数 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 答题时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

#### plant_game_record - 植物游戏记录表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | NOT NULL | - | idx_user_id | 用户ID |
| difficulty | VARCHAR(20) | NOT NULL | - | - | 难度：easy/medium/hard |
| score | INT | NOT NULL | - | - | 得分 |
| correct_count | INT | NOT NULL | - | - | 正确数 |
| total_count | INT | NOT NULL | - | - | 总题数 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 游戏时间 |
| updated_at | DATETIME | - | ON UPDATE CURRENT_TIMESTAMP | - | 更新时间 |

#### operation_log - 操作日志表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | - | - | idx_user_id | 用户ID |
| username | VARCHAR(50) | - | - | - | 用户名 |
| module | VARCHAR(50) | NOT NULL | - | idx_module | 模块名称 |
| operation | VARCHAR(100) | NOT NULL | - | - | 操作描述 |
| type | VARCHAR(20) | NOT NULL | - | idx_type | 操作类型：CREATE/UPDATE/DELETE/QUERY |
| method | VARCHAR(200) | - | - | - | 请求方法 |
| params | TEXT | - | - | - | 请求参数 |
| ip | VARCHAR(50) | - | - | - | IP地址 |
| duration | INT | - | - | - | 执行时长(ms) |
| success | TINYINT | - | 1 | - | 是否成功：0失败/1成功 |
| error_msg | TEXT | - | - | - | 错误信息 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 操作时间 |
| updated_at | DATETIME | - | - | - | 更新时间 |

#### chat_history - AI聊天历史表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | - | - | idx_user_session | 用户ID |
| session_id | VARCHAR(64) | NOT NULL | - | idx_user_session, idx_session_id | 会话ID(UUID) |
| role | VARCHAR(20) | NOT NULL | - | - | 角色：user/assistant |
| content | TEXT | NOT NULL | - | - | 消息内容 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 创建时间 |

#### browse_history - 浏览历史表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | NOT NULL | - | idx_user_id | 用户ID |
| target_type | VARCHAR(50) | NOT NULL | - | idx_target | 目标类型：plant/knowledge/inheritor |
| target_id | INT | NOT NULL | - | idx_target | 目标ID |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 浏览时间 |

#### search_history - 搜索历史表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| user_id | INT | - | - | idx_user_id | 用户ID（可为空，游客搜索） |
| keyword | VARCHAR(200) | NOT NULL | - | idx_keyword | 搜索关键词 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | idx_created_at | 搜索时间 |

### 6.2 表关系详解

#### 实体关系图（ER Diagram）

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  users   │     │  plants  │     │knowledge │
│──────────│     │──────────│     │──────────│
│ id (PK)  │     │ id (PK)  │     │ id (PK)  │
│ username │     │ name_cn  │     │ title    │
│ role     │     │ category │     │ type     │
└────┬─────┘     │ usage_way│     │ therapy  │
     │           └────┬─────┘     └────┬─────┘
     │                │                │
     │    ┌───────────┼────────────────┤
     │    │           │                │
     │    v           v                v
     │  ┌──────────────────────────────────┐
     │  │           favorites              │
     │  │──────────────────────────────────│
     │  │ user_id (FK) → users.id         │
     │  │ target_type + target_id          │
     │  └──────────────────────────────────┘
     │
     │    ┌──────────────────────────────────┐
     │    │           comments               │
     │    │──────────────────────────────────│
     │    │ user_id (FK) → users.id         │
     │    │ target_type + target_id          │
     │    │ reply_to_id → comments.id (自关联)│
     │    └──────────────────────────────────┘
     │
     │    ┌──────────────────────────────────┐
     │    │        browse_history            │
     │    │──────────────────────────────────│
     │    │ user_id (FK) → users.id         │
     │    │ target_type + target_id          │
     │    └──────────────────────────────────┘
     │
     │    ┌──────────────┐    ┌──────────────┐
     │    │  quiz_record │    │plant_game    │
     │    │──────────────│    │   _record    │
     │    │ user_id (FK) │    │──────────────│
     │    │ score        │    │ user_id (FK) │
     │    └──────────────┘    │ difficulty   │
     │                        └──────────────┘
     │
     │    ┌──────────────┐    ┌──────────────┐
     │    │ chat_history │    │search_history│
     │    │──────────────│    │──────────────│
     │    │ user_id (FK) │    │ user_id (FK) │
     │    │ session_id   │    │ keyword      │
     │    └──────────────┘    └──────────────┘
     │
     │    ┌──────────────┐    ┌──────────────┐
     │    │  feedback    │    │operation_log │
     │    │──────────────│    │──────────────│
     │    │ user_id (FK) │    │ user_id (FK) │
     │    │ type/status  │    │ module/type  │
     │    └──────────────┘    └──────────────┘
     │
┌────┴─────┐
│inheritors│     ┌──────────────┐     ┌──────────────┐
│──────────│     │     qa       │     │  resources   │
│ id (PK)  │     │──────────────│     │──────────────│
│ name     │     │ id (PK)      │     │ id (PK)      │
│ level    │     │ category     │     │ title        │
│ bio      │     │ question     │     │ category     │
└──────────┘     │ answer       │     │ files (JSON) │
                 └──────────────┘     └──────────────┘

┌──────────────────┐
│  quiz_questions  │
│──────────────────│
│ id (PK)          │
│ question         │
│ options (JSON)   │
│ correct_answer   │
│ explanation      │
└──────────────────┘
```

#### 关系类型说明

| 关系 | 类型 | 说明 |
|------|------|------|
| users -> favorites | 一对多 | 一个用户可以有多个收藏记录 |
| users -> comments | 一对多 | 一个用户可以发表多条评论 |
| users -> feedback | 一对多 | 一个用户可以提交多条反馈 |
| users -> quiz_record | 一对多 | 一个用户可以有多条答题记录 |
| users -> plant_game_record | 一对多 | 一个用户可以有多条游戏记录 |
| users -> chat_history | 一对多 | 一个用户可以有多条聊天记录 |
| users -> browse_history | 一对多 | 一个用户可以有多条浏览历史 |
| users -> search_history | 一对多 | 一个用户可以有多条搜索历史 |
| users -> operation_log | 一对多 | 一个用户的操作产生多条日志 |
| plants/knowledge/inheritors/qa -> favorites | 多态一对多 | 通过target_type+target_id实现多态关联 |
| plants/knowledge/inheritors -> comments | 多态一对多 | 通过target_type+target_id实现多态关联 |
| plants/knowledge/inheritors/qa -> browse_history | 多态一对多 | 通过target_type+target_id实现多态关联 |
| comments -> comments | 自关联一对多 | reply_to_id指向同表另一条评论（嵌套回复） |
| knowledge -> plants | 多对多（弱） | knowledge.related_plants存储植物ID列表（JSON） |

#### 多态关联设计

本项目中，`favorites`、`comments`、`browse_history` 三张表采用了**多态关联**（Polymorphic Association）设计，通过 `target_type` + `target_id` 两个字段实现一条记录可以关联不同类型的目标实体：

```sql
-- 收藏表：同一用户可以收藏不同类型的内容
INSERT INTO favorites (user_id, target_type, target_id) VALUES (1, 'plant', 5);       -- 收藏植物
INSERT INTO favorites (user_id, target_type, target_id) VALUES (1, 'knowledge', 12);   -- 收藏知识
INSERT INTO favorites (user_id, target_type, target_id) VALUES (1, 'inheritor', 3);    -- 收藏传承人

-- 评论表：同一目标类型下可以有多条评论，评论支持嵌套回复
INSERT INTO comments (user_id, target_type, target_id, content) VALUES (1, 'plant', 5, '钩藤确实好用');
INSERT INTO comments (user_id, target_type, target_id, content, reply_to_id) VALUES (2, 'plant', 5, '同意', 1);
```

**多态关联的优缺点**：

| 优点 | 缺点 |
|------|------|
| 统一的数据表结构，减少表数量 | 无法使用数据库外键约束保证引用完整性 |
| 新增目标类型无需建新表 | 查询时需要JOIN不同表获取目标详情 |
| 统一的收藏/评论逻辑 | 需要在应用层保证target_type的合法性 |

**应用层完整性保障**：`TargetType` 常量类定义了合法的目标类型枚举（plant/knowledge/inheritor/resource/qa），Service层在写入前校验target_type的合法性。

### 6.3 JSON字段详解

项目中多张表使用TEXT类型存储JSON格式数据，前端负责解析和序列化。以下是所有JSON字段的结构定义：

#### plants 表 JSON字段

**images** - 图片列表

```json
[
  {
    "url": "/images/plants/gouteng_01.jpg",
    "caption": "钩藤植株",
    "order": 1
  },
  {
    "url": "/images/plants/gouteng_02.jpg",
    "caption": "钩藤药材",
    "order": 2
  }
]
```

**videos** - 视频列表

```json
[
  {
    "url": "/videos/plants/gouteng_intro.mp4",
    "title": "钩藤采收方法",
    "duration": 180
  }
]
```

**documents** - 文档列表

```json
[
  {
    "url": "/documents/plants/gouteng_research.pdf",
    "title": "钩藤药理研究",
    "type": "pdf"
  }
]
```

#### knowledge 表 JSON字段

**images / videos / documents** - 同plants表结构

**related_plants** - 关联植物ID列表

```json
[1, 5, 12, 23]
```

#### inheritors 表 JSON字段

**images** - 图片列表

```json
[
  {
    "url": "/images/inheritors/yang_01.jpg",
    "caption": "杨医师在采药",
    "order": 1
  }
]
```

**videos** - 视频列表

```json
[
  {
    "url": "/videos/inheritors/yang_interview.mp4",
    "title": "杨医师访谈",
    "duration": 600
  }
]
```

**documents** - 资质文档

```json
[
  {
    "url": "/documents/inheritors/yang_certificate.pdf",
    "title": "省级非遗传承人证书",
    "type": "pdf"
  }
]
```

**representative_cases** - 代表案例

```json
[
  {
    "title": "风湿骨痛治疗",
    "description": "运用侗族传统药浴疗法...",
    "year": "2018"
  }
]
```

**honors** - 荣誉资质

```json
"省级非物质文化遗产代表性传承人，三江县优秀民族医药工作者"
```

> 注意：honors字段在数据库中为VARCHAR(1000)，存储纯文本或简单JSON字符串，不是标准JSON数组。

#### resources 表 JSON字段

**files** - 文件列表

```json
[
  {
    "path": "/documents/resources/intro_dong_medicine.pdf",
    "name": "侗医药入门指南.pdf",
    "type": "pdf",
    "size": 2048576
  },
  {
    "path": "/videos/resources/herb_collecting.mp4",
    "name": "采药技艺演示.mp4",
    "type": "video",
    "size": 52428800
  }
]
```

#### quiz_questions 表 JSON字段

**options** - 选项列表

```json
["清热平肝，息风止痉", "活血化瘀，通络止痛", "补气健脾，燥湿利水", "温中散寒，回阳救逆"]
```

> 注意：options字段在Entity中使用`@JsonIgnore`标注，通过`getOptionList()`/`setOptionList()`方法实现JSON与List的自动转换。

### 6.4 数据库视图

本项目未使用数据库视图（Database View），所有复杂查询通过MyBatis-Plus的LambdaQueryWrapper和自定义@Select注解在Mapper层实现。这种设计选择的原因：

1. **查询灵活性**：业务查询条件动态变化，视图无法参数化
2. **维护便利**：SQL逻辑集中在Java代码中，便于调试和版本管理
3. **性能可控**：避免视图嵌套导致的性能问题
4. **缓存友好**：Service层可直接控制缓存粒度

### 6.5 索引策略

#### 索引分类统计

| 索引类型 | 数量 | 说明 |
|----------|------|------|
| PRIMARY KEY | 16 | 每张表一个主键索引 |
| UNIQUE INDEX | 2 | users.uk_username, favorites.uk_user_target |
| 普通INDEX | 30+ | 用于加速WHERE/ORDER BY/GROUP BY查询 |
| FULLTEXT INDEX | 5 | ngram全文索引，支持中文分词搜索 |
| 复合INDEX | 3 | 用于多列组合查询 |

#### 全文索引详解

MySQL 8.0的ngram全文索引解析器是本项目搜索功能的核心。ngram将文本按N个字符切分建立倒排索引，适合中文等无空格分隔的语言。

| 全文索引 | 表 | 列 | ngram_token_size | 用途 |
|----------|-----|-----|------------------|------|
| ft_plants_search | plants | name_cn, name_dong, efficacy, story | 2 | 植物搜索 |
| ft_knowledge_search | knowledge | title, content | 2 | 知识搜索 |
| ft_inheritors_search | inheritors | name, specialties, bio | 2 | 传承人搜索 |
| ft_qa_search | qa | question, answer | 2 | 问答搜索 |
| ft_resources_search | resources | title, description | 2 | 资源搜索 |

**全文搜索SQL示例**：

```sql
-- 全文搜索（相关度排序）
SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) AGAINST('钩藤' IN BOOLEAN MODE);

-- LIKE降级搜索（全文索引不可用时）
SELECT * FROM plants WHERE name_cn LIKE '%钩藤%' OR efficacy LIKE '%钩藤%';
```

**搜索降级策略**（PlantServiceImpl.searchPaged）：

```java
public Page<Plant> searchPaged(String keyword, int page, int size) {
    if (appProperties.getSearch().isUseFulltext()) {
        try {
            // 优先使用全文索引
            return fulltextSearch(keyword, page, size);
        } catch (Exception e) {
            log.warn("全文搜索失败，降级为LIKE查询: {}", e.getMessage());
        }
    }
    // 降级为LIKE模糊查询
    return likeSearch(keyword, page, size);
}
```

#### 复合索引详解

| 索引名 | 表 | 列 | 用途 |
|--------|-----|-----|------|
| idx_plants_category_usage | plants | category, usage_way | 分类+用法组合筛选 |
| idx_target | comments | target_type, target_id | 按目标查询评论 |
| idx_target | browse_history | target_type, target_id | 按目标查询浏览历史 |
| idx_user_session | chat_history | user_id, session_id | 按用户和会话查询聊天记录 |

**复合索引最左前缀原则**：查询条件必须从索引最左列开始才能利用索引。例如 `idx_plants_category_usage(category, usage_way)`：
- `WHERE category = '根茎类' AND usage_way = '内服'` -- 使用索引
- `WHERE category = '根茎类'` -- 使用索引（最左前缀）
- `WHERE usage_way = '内服'` -- 不使用索引（跳过了最左列）

#### 索引优化建议

| 场景 | 当前索引 | 优化建议 |
|------|----------|----------|
| 管理后台按状态筛选评论 | idx_status | 已覆盖，无需优化 |
| 按热度排序植物列表 | idx_popularity | 已覆盖，支持ORDER BY popularity DESC |
| 按创建时间排序 | idx_created_at | 已覆盖，支持ORDER BY created_at DESC |
| 搜索热门关键词 | idx_keyword | 已覆盖，支持GROUP BY keyword + ORDER BY COUNT(*) |
| 用户收藏列表查询 | uk_user_target | 已覆盖，唯一索引同时加速查询和去重 |

### 6.6 迁移脚本

#### Flyway迁移管理

项目使用Flyway进行数据库版本化管理，迁移脚本位于 `src/main/resources/db/migration/` 目录。

**命名规则**：`V{版本号}__{描述}.sql`（双下划线分隔版本号和描述）

| 脚本文件 | 版本 | 内容 | 执行时机 |
|----------|------|------|----------|
| V1__init_schema.sql | 1 | 创建16张表+索引+全文索引 | 首次启动 |
| V2__seed_data.sql | 2 | 插入初始数据（管理员账号、示例植物、知识条目等） | V1执行后 |

**Flyway配置**（application-dev.yml）：

```yaml
spring:
  flyway:
    enabled: true                    # 开发环境启用Flyway
    locations: classpath:db/migration # 迁移脚本位置
    baseline-on-migrate: true        # 已有数据库时自动基线化
    validate-on-migrate: true        # 迁移前校验已执行脚本完整性
```

**生产环境建议**：生产环境（application-prod.yml）建议设置 `flyway.enabled: false`，由DBA手动执行迁移脚本，避免自动迁移导致的数据风险。

#### V1__init_schema.sql 执行顺序

```sql
-- 按依赖关系排序，被依赖的表先创建
1. comments         -- 无外键依赖
2. favorites        -- 无外键依赖
3. feedback         -- 无外键依赖
4. inheritors       -- 无外键依赖
5. knowledge        -- 无外键依赖
6. operation_log    -- 无外键依赖
7. plant_game_record -- 无外键依赖
8. plants           -- 无外键依赖
9. qa               -- 无外键依赖
10. quiz_questions  -- 无外键依赖
11. quiz_record     -- 无外键依赖
12. resources       -- 无外键依赖
13. users           -- 无外键依赖
14. chat_history    -- 无外键依赖
15. browse_history  -- 无外键依赖
16. search_history  -- 无外键依赖
```

> 注意：虽然表之间没有数据库层面的外键约束（FK），但应用层通过target_type + target_id实现了逻辑外键。建表顺序不影响功能，因为MySQL的InnoDB引擎在创建索引时不依赖其他表。

#### V2__seed_data.sql 初始数据

种子数据脚本插入以下初始数据：

| 数据类型 | 数量 | 说明 |
|----------|------|------|
| 管理员账号 | 1 | admin/admin123（BCrypt加密） |
| 药用植物 | 65 | 涵盖根茎类/全草类/花叶类/果实类/皮类五大分类 |
| 知识条目 | 56 | 涵盖药方和疗法两大类型 |
| 传承人 | 24 | 国家级/省级/市级/县级四级传承人 |
| 问答条目 | 65 | 侗药常识/侗医疗法/文化背景三个分类 |
| 学习资源 | 40 | 入门/进阶/专业三个级别 |
| 测验题目 | 70 | easy/medium/hard三个难度 |

## 七、配置详情

### 7.1 环境变量完整列表

项目通过 `.env` 文件管理环境变量，`.env.example` 提供模板。所有环境变量均可在Docker Compose中使用。

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| **数据库** | | | |
| DB_HOST | 否 | localhost | MySQL主机地址 |
| DB_PORT | 否 | 3306 | MySQL端口 |
| DB_USERNAME | 否 | root | MySQL用户名 |
| DB_PASSWORD | 是 | - | MySQL密码 |
| MYSQL_ROOT_PASSWORD | 是 | - | Docker中MySQL root密码 |
| MYSQL_DATABASE | 否 | dong_medicine | Docker中MySQL数据库名 |
| **Redis** | | | |
| REDIS_HOST | 否 | localhost | Redis主机地址 |
| REDIS_PORT | 否 | 6379 | Redis端口 |
| REDIS_PASSWORD | 否 | - | Redis密码（为空则无密码） |
| **RabbitMQ** | | | |
| RABBITMQ_HOST | 否 | localhost | RabbitMQ主机地址 |
| RABBITMQ_PORT | 否 | 5672 | RabbitMQ端口 |
| RABBITMQ_USER | 否 | guest | RabbitMQ用户名 |
| RABBITMQ_PASSWORD | 否 | guest | RabbitMQ密码 |
| APP_RABBITMQ_ENABLED | 否 | true | 是否启用RabbitMQ（设false禁用） |
| **安全** | | | |
| JWT_SECRET | 是 | - | JWT密钥（生产环境必须修改，建议32位以上随机字符串） |
| JWT_EXPIRATION | 否 | 86400000 | JWT过期时间（毫秒，默认24小时） |
| SA_TOKEN_TIMEOUT | 否 | 2592000 | Sa-Token会话超时（秒，默认30天） |
| **AI** | | | |
| DEEPSEEK_API_KEY | 否 | - | DeepSeek API密钥（不填则AI对话不可用） |
| DEEPSEEK_BASE_URL | 否 | https://api.deepseek.com | DeepSeek API地址 |
| DEEPSEEK_MODEL | 否 | deepseek-chat | DeepSeek模型名称 |
| **CORS** | | | |
| CORS_ORIGIN_1 | 否 | http://localhost:5173 | 允许的前端源地址1 |
| CORS_ORIGIN_2 | 否 | http://localhost:3000 | 允许的前端源地址2 |
| CORS_ORIGIN_3 | 否 | http://127.0.0.1:5173 | 允许的前端源地址3 |
| **文件** | | | |
| MAX_BODY_SIZE | 否 | 10485760 | 请求体最大大小（字节，默认10MB） |
| KKFILEVIEW_URL | 否 | http://localhost:8012 | KKFileView文档预览服务地址 |
| **其他** | | | |
| SPRING_PROFILES_ACTIVE | 否 | dev | Spring Profile（dev/prod） |
| SERVER_PORT | 否 | 8080 | 后端服务端口 |

### 7.2 缓存策略详解

本项目采用 **Caffeine L1 + Redis L2** 双层缓存架构，由CacheConfig统一管理。

#### 双层缓存架构

```
请求 → Controller → Service
                      │
                      ├─ 1. 查询Caffeine L1本地缓存
                      │     └─ 命中 → 直接返回（纳秒级）
                      │
                      ├─ 2. L1未命中 → 查询Redis L2远程缓存
                      │     └─ 命中 → 返回并回填L1（毫秒级）
                      │
                      └─ 3. L2未命中 → 查询MySQL数据库
                            └─ 结果写入L2 + L1（十毫秒级）
```

#### CacheConfig 核心配置

```java
@Configuration
@EnableCaching
public class CacheConfig {

    // L1缓存 - Caffeine本地缓存
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 1. 创建Caffeine缓存管理器
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 2. 定义各缓存区的TTL策略
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 热数据：6小时过期（植物/知识/传承人）
        cacheConfigurations.put("plants", redisCacheConfig(6, TimeUnit.HOURS));
        cacheConfigurations.put("knowledges", redisCacheConfig(6, TimeUnit.HOURS));
        cacheConfigurations.put("inheritors", redisCacheConfig(6, TimeUnit.HOURS));

        // 温数据：4小时过期（资源/测验题目）
        cacheConfigurations.put("resources", redisCacheConfig(4, TimeUnit.HOURS));
        cacheConfigurations.put("quizQuestions", redisCacheConfig(12, TimeUnit.HOURS));

        // 冷数据：30分钟过期（用户信息）
        cacheConfigurations.put("users", redisCacheConfig(30, TimeUnit.MINUTES));

        // 元数据：1小时过期（筛选选项/精选推荐）
        cacheConfigurations.put("hotData", redisCacheConfig(1, TimeUnit.HOURS));

        // 3. 创建Redis L2缓存
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory)
            .cacheDefaults(redisCacheConfig(1, TimeUnit.HOURS))
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();

        // 4. 创建Caffeine L1缓存
        List<CaffeineCache> caffeineCaches = createCaffeineCaches();

        // 5. 组合双层缓存
        cacheManager.setCaches(caffeineCaches);
        // Redis不可用时自动降级为Caffeine单层
        try {
            factory.getConnection();
            // Redis可用，添加Redis缓存层
        } catch (Exception e) {
            log.warn("Redis不可用，降级为Caffeine单层缓存");
        }

        return cacheManager;
    }
}
```

#### 各缓存区TTL策略

| 缓存区 | Redis TTL | Caffeine TTL | 数据类型 | 说明 |
|--------|-----------|--------------|----------|------|
| plants | 6小时 | 写入10分钟/访问30分钟 | 药用植物 | 植物数据变化频率低，可长时间缓存 |
| knowledges | 6小时 | 写入10分钟/访问30分钟 | 知识条目 | 知识内容稳定，长时间缓存 |
| inheritors | 6小时 | 写入10分钟/访问30分钟 | 传承人 | 传承人信息不常变更 |
| resources | 4小时 | 写入10分钟/访问30分钟 | 学习资源 | 资源可能新增，TTL稍短 |
| quizQuestions | 12小时 | 写入10分钟/访问30分钟 | 测验题目 | 题目极少变更，超长缓存 |
| users | 30分钟 | 写入10分钟/访问30分钟 | 用户信息 | 用户可能修改密码/角色，TTL较短 |
| hotData | 1小时 | 写入10分钟/访问30分钟 | 元数据/精选 | 筛选选项和精选推荐 |

#### Caffeine L1缓存参数

| 参数 | 值 | 说明 |
|------|-----|------|
| maximumSize | 1000 | 最大缓存条目数 |
| expireAfterWrite | 10分钟 | 写入后过期时间 |
| expireAfterAccess | 30分钟 | 最后访问后过期时间 |
| evictionStrategy | W-TinyLFU | 淘汰策略（高性能，兼顾频率和近期性） |

#### 缓存清除策略

数据变更时主动清除缓存，确保数据一致性：

```java
// 创建/更新/删除后清除缓存
@CacheEvict(value = "plants", allEntries = true)
public void clearCache() {
    log.info("清除植物缓存");
}

// 在AdminContentController中调用
@PostMapping("/plants")
public R<Void> createPlant(@Valid @RequestBody PlantCreateDTO dto) {
    plantService.save(dto);
    plantService.clearCache();  // 清除缓存
    return R.ok();
}
```

#### Redis降级机制

当Redis不可用时，系统自动降级为Caffeine单层缓存：

1. **启动时检测**：CacheConfig尝试连接Redis，连接失败则跳过Redis缓存层
2. **运行时容错**：CacheHealthIndicator持续监控Redis状态
3. **功能不受影响**：所有查询仍可正常工作，只是缓存命中率降低

### 7.3 数据库配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 连接池 | HikariCP | Spring Boot默认连接池，性能优异 |
| 字符集 | utf8mb4 | 支持中文、emoji等4字节字符 |
| 排序规则 | utf8mb4_unicode_ci | 不区分大小写排序，支持中文排序 |
| 存储引擎 | InnoDB | 支持事务、行锁、外键（本项目未使用外键） |
| 行格式 | DYNAMIC | 长TEXT字段存储在溢出页，主页存储指针 |
| 时区 | Asia/Shanghai | 数据库连接时区，确保时间字段正确 |
| MyBatis-Plus | map-underscore-to-camel-case: true | 自动将数据库下划线命名转为Java驼峰命名 |

**HikariCP关键参数**（Spring Boot默认值）：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| maximumPoolSize | 10 | 最大连接数 |
| minimumIdle | 10 | 最小空闲连接数 |
| idleTimeout | 600000 | 空闲连接超时（10分钟） |
| maxLifetime | 1800000 | 连接最大生命周期（30分钟） |
| connectionTimeout | 30000 | 连接获取超时（30秒） |

### 7.4 Redis配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| host | ${REDIS_HOST:localhost} | Redis服务器地址 |
| port | ${REDIS_PORT:6379} | Redis端口 |
| password | ${REDIS_PASSWORD:} | Redis密码（为空则无密码） |
| database | 0 | 使用的数据库编号 |
| timeout | 10000ms | 连接超时时间 |
| 客户端 | Lettuce | Spring Boot默认Redis客户端，基于Netty，支持连接复用 |

**Redis Key命名规范**：

| Key模式 | 用途 | TTL |
|---------|------|-----|
| `plants::detail_{id}` | 植物详情缓存 | 6小时 |
| `knowledges::detail_{id}` | 知识详情缓存 | 6小时 |
| `inheritors::detail_{id}` | 传承人详情缓存 | 6小时 |
| `captcha:{uuid}` | 验证码存储 | 5分钟 |
| `sa-token:login:session:{id}` | Sa-Token会话 | 30天 |
| `notification:user:{userId}` | 用户通知列表 | 无过期 |
| `notification:user:{userId}:unread` | 未读通知计数 | 无过期 |
| `chat:session:{sessionId}` | 聊天会话暂存 | 1小时 |

**Docker环境Redis配置**：

```yaml
# docker-compose.yml
redis:
  image: redis:7-alpine
  command: redis-server --maxmemory 128mb --maxmemory-policy allkeys-lru
  deploy:
    resources:
      limits:
        memory: 128M
```

- `maxmemory 128mb`：限制Redis最大使用128MB内存
- `maxmemory-policy allkeys-lru`：内存满时淘汰最久未使用的key

### 7.5 JVM配置建议

#### 开发环境

```bash
# 最小配置（4GB内存机器）
java -jar dong-medicine-backend-1.0.0.jar \
  -Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

#### 生产环境

```bash
# 推荐配置（8GB+内存机器）
java -jar dong-medicine-backend-1.0.0.jar \
  -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/dong-medicine/heapdump.hprof \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=Asia/Shanghai \
  -Dspring.profiles.active=prod
```

**JVM参数说明**：

| 参数 | 说明 |
|------|------|
| -Xms | 初始堆内存大小 |
| -Xmx | 最大堆内存大小（建议与-Xms相同，避免动态扩容开销） |
| -XX:+UseG1GC | 使用G1垃圾收集器（适合大堆内存，低延迟） |
| -XX:MaxGCPauseMillis | GC最大停顿时间目标 |
| -XX:+HeapDumpOnOutOfMemoryError | OOM时自动生成堆转储文件 |
| -XX:HeapDumpPath | 堆转储文件保存路径 |

**Docker环境JVM配置**：

```yaml
# docker-compose.yml
backend:
  environment:
    JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"
  deploy:
    resources:
      limits:
        memory: 512M
        cpus: '1.0'
```

### 7.6 文件上传限制

| 文件类型 | 最大大小 | 允许的扩展名 | 存储路径 |
|----------|----------|-------------|----------|
| 图片 | 10MB | jpg, jpeg, png, gif, bmp, webp, svg | /public/images/{category}/ |
| 视频 | 100MB | mp4, avi, mov, wmv, flv, mkv | /public/videos/{category}/ |
| 文档 | 50MB | docx, doc, pdf, pptx, ppt, xlsx, xls, txt | /public/documents/{category}/ |

**Spring Boot配置**：

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 100MB      # 单个文件最大100MB
      max-request-size: 100MB   # 单次请求最大100MB
      file-size-threshold: 2KB  # 超过2KB的文件写入临时目录
```

**文件上传5层校验**（FileUploadService）：

```
1. 文件非空校验 → file.isEmpty() 检查
2. 文件大小校验 → 图片10MB/视频100MB/文档50MB
3. 扩展名白名单 → 仅允许指定扩展名
4. MIME类型校验 → 文件实际类型与扩展名一致
5. 文件名安全校验 → 防止路径遍历攻击（../ 等）
```

**文件存储路径**：

```
{user.dir}/public/
├── images/
│   ├── plants/        # 植物图片
│   ├── inheritors/    # 传承人图片
│   ├── knowledge/     # 知识配图
│   └── common/        # 通用图片
├── videos/
│   ├── plants/        # 植物视频
│   ├── inheritors/    # 传承人视频
│   └── common/        # 通用视频
└── documents/
    ├── plants/        # 植物文档
    ├── inheritors/    # 传承人资质
    ├── knowledge/     # 知识文档
    ├── resources/     # 学习资源
    └── common/        # 通用文档
```

### 7.7 CORS与WebSocket配置

#### CORS跨域配置

CORS（Cross-Origin Resource Sharing）配置在SaTokenConfig中，允许前端跨域访问后端API：

```java
// SaTokenConfig.java
@Bean
public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    // 允许的源地址（从配置文件读取）
    appProperties.getCors().getAllowedOrigins().forEach(config::addAllowedOrigin);
    config.addAllowedHeader("*");           // 允许所有请求头
    config.addAllowedMethod("*");           // 允许所有HTTP方法
    config.setAllowCredentials(true);       // 允许携带Cookie
    config.setMaxAge(3600L);                // 预检请求缓存1小时

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
}
```

**允许的源地址**（application.yml）：

```yaml
app:
  cors:
    allowed-origins:
      - ${CORS_ORIGIN_1:http://localhost:5173}    # Vite开发服务器
      - ${CORS_ORIGIN_2:http://localhost:3000}    # 备用端口
      - ${CORS_ORIGIN_3:http://127.0.0.1:5173}   # 127.0.0.1访问
```

#### WebSocket配置

WebSocket用于AI聊天实时通信，配置在WebSocketConfig中：

```java
// WebSocketConfig.java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .setAllowedOrigins(appProperties.getCors().getAllowedOrigins()
                .toArray(new String[0]));
    }
}
```

**WebSocket连接流程**：

```
1. 前端创建WebSocket连接：ws://localhost:8080/ws/chat
2. 连接时携带Token：URL参数 ?token=xxx
3. ChatWebSocketHandler验证Token有效性
4. 验证通过后建立长连接
5. 前端发送JSON消息：{"message": "钩藤有什么功效？"}
6. 后端调用DeepSeek API流式响应
7. 后端逐条发送AI回复：{"role": "assistant", "content": "钩藤具有..."}
8. 前端实时渲染Markdown内容
9. 连接关闭时保存聊天历史到MySQL
```

**Nginx WebSocket代理配置**：

```nginx
location /ws/chat {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_read_timeout 86400s;  # WebSocket长连接超时24小时
}
```

### 7.8 应用配置文件说明

#### 配置文件层次

```
src/main/resources/
├── application.yml           # 主配置（通用配置+环境变量占位符）
├── application-dev.yml       # 开发环境配置（Flyway启用/日志级别/等）
├── application-prod.yml      # 生产环境配置（Flyway禁用/日志级别/等）
├── application.properties    # 基础属性（少量固定配置）
├── logback-spring.xml        # 日志配置（控制台+文件滚动）
└── db/migration/             # Flyway迁移脚本
```

#### 配置加载优先级

```
优先级从高到低：
1. 命令行参数          --server.port=8081
2. 系统属性            -Dserver.port=8081（Dotenv设置的系统属性属于此类）
3. application-{profile}.yml   application-prod.yml
4. application.yml     通用配置
5. application.properties     基础属性
```

#### application.yml 核心配置项

```yaml
server:
  port: 8080                          # 服务端口
  servlet:
    encoding:
      charset: UTF-8                  # 请求编码
      enabled: true
      force: true

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}  # 激活的Profile
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}     # 数据库用户名
    password: ${DB_PASSWORD:}         # 数据库密码
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss # JSON日期格式
  servlet:
    multipart:
      max-file-size: 100MB           # 单文件最大大小
      max-request-size: 100MB        # 请求最大大小
  data:
    redis:
      host: ${REDIS_HOST:localhost}   # Redis地址
      port: ${REDIS_PORT:6379}        # Redis端口
      password: ${REDIS_PASSWORD:}    # Redis密码
      database: 0                     # Redis数据库编号
      timeout: 10000ms                # 连接超时
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /
    connection-timeout: 10000
    listener:
      simple:
        prefetch: 10                  # 每个消费者预取10条消息
        retry:
          enabled: true               # 启用重试

# Sa-Token认证配置
sa-token:
  token-name: Authorization           # Token请求头名称
  timeout: ${SA_TOKEN_TIMEOUT:2592000} # 会话超时（秒，默认30天）
  active-timeout: -1                  # 活跃超时（-1表示不限制）
  is-concurrent: true                 # 允许同一账号多端登录
  is-share: false                     # 每次登录生成新Token
  token-style: uuid                   # Token风格：UUID
  is-log: false                       # 不记录登录日志
  is-read-cookie: false               # 不从Cookie读取Token
  is-read-body: false                 # 不从请求体读取Token
  is-read-header: true                # 从请求头读取Token
  token-prefix: Bearer                # Token前缀
  jwt-secret-key: ${JWT_SECRET:}      # JWT密钥

# 应用自定义配置
app:
  rabbitmq:
    enabled: ${APP_RABBITMQ_ENABLED:true}  # RabbitMQ启用开关
  cors:
    allowed-origins:                       # CORS允许的源
      - ${CORS_ORIGIN_1:http://localhost:5173}
      - ${CORS_ORIGIN_2:http://localhost:3000}
      - ${CORS_ORIGIN_3:http://127.0.0.1:5173}
  cache:
    enabled: true                    # 缓存启用开关
    default-expire-time: 3600000     # 默认过期时间（毫秒）
    max-size: 1000                   # Caffeine最大缓存条目
    expire-minutes: 60               # 默认过期分钟数
  logging:
    performance-monitoring: true     # 性能监控
    request-logging: true            # 请求日志
    database-monitoring: true        # 数据库监控
  search:
    use-fulltext: true               # 使用全文搜索
  request:
    max-body-size: ${MAX_BODY_SIZE:10485760}  # 请求体最大大小

# 文件上传配置
file:
  upload:
    base-path: ${user.dir}/public    # 文件存储根路径
  kkfileview:
    url: ${KKFILEVIEW_URL:http://localhost:8012}  # KKFileView地址

# 日志配置
logging:
  level:
    com.dongmedicine: INFO           # 项目日志级别
    cn.dev33.satoken: WARN           # Sa-Token日志级别
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Actuator监控端点
management:
  endpoints:
    web:
      exposure:
        include: health,info         # 暴露health和info端点
      base-path: /actuator
  endpoint:
    health:
      show-details: when_authorized  # 仅授权用户查看详情
      show-components: when_authorized
      probes:
        enabled: true
  health:
    db:
      enabled: true                  # 数据库健康检查
    caches:
      enabled: true                  # 缓存健康检查
    diskspace:
      enabled: true                  # 磁盘空间健康检查

# MyBatis-Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl  # 不打印SQL日志
```

#### application-dev.yml 开发环境配置

```yaml
# 开发环境特有配置
spring:
  flyway:
    enabled: true                    # 启用Flyway自动迁移
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/dong_medicine?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai

logging:
  level:
    com.dongmedicine: DEBUG          # 开发环境DEBUG级别
```

#### application-prod.yml 生产环境配置

```yaml
# 生产环境特有配置
spring:
  flyway:
    enabled: false                   # 生产环境禁用自动迁移

logging:
  level:
    com.dongmedicine: WARN           # 生产环境WARN级别
    cn.dev33.satoken: ERROR

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl  # 禁止SQL日志
```

#### logback-spring.xml 日志配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件滚动输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/dong-medicine.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/dong-medicine.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>           <!-- 保留30天 -->
            <totalSizeCap>1GB</totalSizeCap>      <!-- 总大小上限1GB -->
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 开发环境：控制台+DEBUG -->
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="com.dongmedicine" level="DEBUG"/>
    </springProfile>

    <!-- 生产环境：文件+WARN -->
    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="FILE"/>
        </root>
        <logger name="com.dongmedicine" level="INFO"/>
    </springProfile>
</configuration>
```

---

> **文档说明**：本文档为侗乡医药数字展示平台项目说明文档的第一部分（第1-7章），涵盖项目概述、技术架构、项目结构、环境搭建、功能模块、数据库设计和配置详情。第二部分将涵盖安全认证、WebSocket实时通信、AI集成、数据可视化、前端路由与状态管理、部署运维和开发规范等章节。时间 |

全文索引：`ft_plants_search(name_cn, name_dong, efficacy, story) WITH PARSER ngram`

#### knowledge - 知识库表

| 字段 | 类型 | 约束 | 默认值 | 索引 | 说明 |
|------|------|------|--------|------|------|
| id | INT | PK, AUTO_INCREMENT | - | PRIMARY | 主键 |
| title | VARCHAR(200) | NOT NULL | - | - | 标题 |
| type | VARCHAR(20) | NOT NULL | - | idx_type | 类型：药方/疗法 |
| therapy_category | VARCHAR(50) | - | - | - | 疗法分类 |
| disease_category | VARCHAR(50) | - | - | - | 疾病分类 |
| herb_category | VARCHAR(50) | - | - | - | 药材分类 |
| content | TEXT | NOT NULL | - | - | 内容 |
| formula | TEXT | - | - | - | 配方 |
| usage_method | TEXT | - | - | - | 用法 |
| steps | TEXT | - | - | - | 步骤 |
| images | TEXT | - | - | - | 图片JSON |
| videos | TEXT | - | - | - | 视频JSON |
| documents | TEXT | - | - | - | 文档JSON |
| related_plants | TEXT | - | - | - | 关联植物ID列表 |
| update_log | TEXT | - | - | - | 更新日志 |
| popularity | INT | - | 0 | idx_popularity | 热度 |
| view_count | INT | - | 0 | idx_knowledge_view_count | 浏览量 |
| favorite_count | INT | - | 0 | - | 收藏量 |
| created_at | DATETIME | - | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | - | - | 更新</think>
# 侗乡医药数字展示平台

一个基于 Vue 3 + Spring Boot 的侗族医药文化遗产数字化展示平台

## 项目简介

本项目是一个毕业设计项目，旨在通过数字化手段保护和传承侗族医药文化遗产。平台提供了药用植物展示、非遗传承人介绍、知识库、互动问答等功能模块，让用户能够深入了解侗族传统医药文化。

## 功能特性

- **药用植物展示** - 展示侗族传统药用植物信息，包含植物图片、功效介绍等内容
- **非遗传承人** - 介绍侗医药非遗传承人信息，展示传承人照片、简介和技艺特色
- **知识库** - 侗医药相关文献和资料，支持PDF、Word等文档在线预览
- **互动问答** - 用户提问和AI智能回答，支持草药识别小游戏
- **资源中心** - 视频、文档等多媒体资源
- **用户系统** - 用户注册、登录、个人中心、收藏功能
- **反馈建议** - 用户反馈建议提交
- **后台管理** - 内容管理、用户管理、数据统计

## 技术栈

### 前端技术

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 UI 组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 状态管理
- **Axios** - HTTP 客户端
- **ECharts** - 数据可视化

### 后端技术

- **Spring Boot 3.1** - Java Web 框架
- **Spring Security** - 安全框架
- **MyBatis Plus** - ORM 框架
- **MySQL** - 关系型数据库
- **JWT** - JSON Web Token 认证
- **Swagger** - API 文档工具

## 项目结构

```
Graduation Project/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/dongmedicine/
│   │   │   │       ├── config/        # 配置类
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── mapper/        # 数据访问层
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       └── dto/           # 数据传输对象
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── ...
│   │   └── test/
│   ├── public/                    # 静态资源
│   └── pom.xml
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── styles/             # 样式文件
│   └── package.json
│
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

```bash
cd backend

# 配置数据库连接 (修改 src/main/resources/application.yml)
# 运行项目
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## API 文档

启动后端服务后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

## 开发团队

- 开发者: 毕业设计项目组

## 许可证

本项目仅供学习和研究使用。

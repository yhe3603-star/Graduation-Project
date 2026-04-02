# 后端源码目录 (src/main/java/com/dongmedicine)

本目录存放侗乡医药数字展示平台的所有后端Java源代码。

## 📁 目录结构

```
com/dongmedicine/
├── common/           # 公共模块
│   ├── constant/     # 常量定义
│   ├── exception/    # 异常处理
│   └── util/         # 工具类
├── config/           # 配置类
├── controller/       # 控制器层
├── dto/              # 数据传输对象
├── entity/           # 实体类
├── mapper/           # 数据访问层
└── service/          # 服务层
    └── impl/         # 服务实现类
```

## 🏗️ 分层架构

本项目采用经典的三层架构：

```
┌─────────────────────────────────────────┐
│           Controller 控制器层            │  ← 接收HTTP请求
├─────────────────────────────────────────┤
│            Service 服务层                │  ← 处理业务逻辑
├─────────────────────────────────────────┤
│            Mapper 数据访问层             │  ← 操作数据库
├─────────────────────────────────────────┤
│            Entity 实体类                 │  ← 数据模型
└─────────────────────────────────────────┘
```

### 各层职责

| 层级 | 职责 | 说明 |
|------|------|------|
| Controller | 接收请求、参数校验、返回响应 | 与前端交互的入口 |
| Service | 业务逻辑处理、事务管理 | 核心业务代码 |
| Mapper | 数据库CRUD操作 | SQL执行 |
| Entity | 数据模型定义 | 与数据库表对应 |
| DTO | 数据传输对象 | 前后端数据交换 |

## 📖 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis Plus | 3.5.x | ORM框架 |
| JWT | - | Token认证 |
| MySQL | 8.x | 数据库 |
| Redis | 7.x | 缓存 |

## 🚀 快速开始

### 开发环境运行
```bash
mvn spring-boot:run
```

### 打包部署
```bash
mvn clean package
java -jar target/dong-medicine-backend.jar
```

## 📚 各目录详细说明

请查看各子目录的README文档：
- [common/](./common/README.md) - 公共模块
- [config/](./config/README.md) - 配置类
- [controller/](./controller/README.md) - 控制器层
- [dto/](./dto/README.md) - 数据传输对象
- [entity/](./entity/README.md) - 实体类
- [mapper/](./mapper/README.md) - 数据访问层
- [service/](./service/README.md) - 服务层

## 📖 扩展阅读

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Spring Security 官方文档](https://spring.io/projects/spring-security)

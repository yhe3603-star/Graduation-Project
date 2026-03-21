# 侗乡医药数字展示平台后端

基于 Spring Boot 3.1 的侗族医药文化遗产数字化展示平台后端服务

## 环境要求

| 环境 | 版本 |
|-----|------|
| JDK | 17 |
| Maven | 3.9+ |
| MySQL | 8.0+ |

## 环境变量

复制 `.env.example` 并按部署环境设置：

| 变量 | 说明 |
|-----|------|
| `SPRING_PROFILES_ACTIVE` | 运行环境 (dev/prod) |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | JWT 密钥 (64字符以上) |
| `JWT_EXPIRATION` | JWT 过期时间 (毫秒) |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源 |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 |
| `DEEPSEEK_BASE_URL` | DeepSeek API 地址 |
| `ADMIN_INIT_PASSWORD` | 管理员初始密码 |
| `FILE_UPLOAD_BASE_PATH` | 文件上传路径 |

## 本地启动

```bash
# 安装依赖并启动
./mvnw spring-boot:run
```

默认服务地址：http://localhost:8080

## 常用命令

```bash
# 编译项目
./mvnw -q -DskipTests compile

# 运行测试
./mvnw test

# 打包
./mvnw clean package -DskipTests

# 运行生产环境
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## 目录结构

```
dong-medicine-backend/
├── src/main/java/com/dongmedicine/
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── service/          # 服务层
│   │   └── impl/         # 服务实现
│   ├── mapper/           # MyBatis Mapper
│   ├── entity/           # 实体类
│   ├── dto/              # 数据传输对象
│   └── common/           # 公共类
│       ├── exception/    # 异常处理
│       └── util/         # 工具类
├── src/main/resources/
│   ├── application.yml       # 主配置
│   ├── application-dev.yml   # 开发环境配置
│   └── application-prod.yml  # 生产环境配置
├── sql/                  # 数据库脚本
└── public/               # 静态资源
```

## API 文档

启动服务后访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

## 健康检查

```
http://localhost:8080/actuator/health
```

## 主要功能模块

| 模块 | 说明 |
|-----|------|
| 用户管理 | 用户注册、登录、权限管理 |
| 药用植物 | 植物信息管理、图片上传 |
| 传承人 | 非遗传承人信息管理 |
| 知识库 | 文献资料管理、在线预览 |
| 问答系统 | AI 智能问答、草药识别游戏 |
| 资源中心 | 视频、文档等多媒体资源 |
| 评论收藏 | 用户互动功能 |
| 操作日志 | 系统操作记录 |

# 侗乡医药数字展示平台后端

## 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.0+

## 环境变量

复制 `.env.example` 并按部署环境设置：

- `SPRING_PROFILES_ACTIVE`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `CORS_ALLOWED_ORIGINS`
- `DEEPSEEK_API_KEY`
- `ADMIN_INIT_PASSWORD`

## 本地启动

```bash
./mvnw spring-boot:run
```

默认服务地址：

- `http://localhost:8080`

## 质量命令

```bash
./mvnw -q -DskipTests compile
./mvnw test
```

## 目录说明

- `src/main/java`：业务代码
- `src/main/resources`：配置和静态资源映射
- `sql`：数据库结构与初始化数据
- `src/test`：单元测试

# 部署配置目录

## 目录结构

```
deploy/
├── docker-deploy.sh  # Docker部署脚本
├── init-server.sh    # 服务器初始化脚本
└── README.md         # 说明文档
```

## 部署方式

### Docker + CI/CD 自动部署（推荐）

使用GitHub Actions自动构建并部署到服务器。

**前置条件**：
- 已配置GitHub Secrets
- 服务器已初始化
- 配置了SSH密钥认证

**部署流程**：
1. 推送代码到main分支
2. GitHub Actions自动构建前后端
3. 自动上传部署文件到服务器
4. 执行docker-deploy.sh完成部署

**GitHub Secrets配置**：

| Secret名称 | 说明 |
|-----------|------|
| `SERVER_HOST` | 服务器公网IP |
| `SERVER_USER` | SSH用户名 |
| `SSH_PRIVATE_KEY` | SSH私钥 |
| `MYSQL_ROOT_PASSWORD` | MySQL root密码 |
| `MYSQL_USER` | MySQL用户名 |
| `MYSQL_PASSWORD` | MySQL密码 |
| `REDIS_PASSWORD` | Redis密码 |
| `JWT_SECRET` | JWT密钥 |
| `DEEPSEEK_API_KEY` | DeepSeek API密钥（可选） |

### 手动Docker部署

适合首次部署或手动更新。

**前置条件**：
- Docker 20.10+
- Docker Compose 2.0+

**部署步骤**：
```bash
# 1. 初始化服务器（首次部署）
chmod +x init-server.sh
./init-server.sh

# 2. 配置环境变量
cp .env.example .env
# 编辑.env文件，配置数据库、Redis等

# 3. 执行部署
chmod +x docker-deploy.sh
./docker-deploy.sh
```

## 文件说明

### 1. init-server.sh - 服务器初始化

**功能**：初始化服务器环境，安装必要的软件。

**安装内容**：
| 组件 | 说明 |
|------|------|
| Docker | 容器运行环境 |
| Docker Compose | 容器编排工具 |
| 镜像加速 | 配置阿里云镜像源 |

**开放端口**：
- 22 (SSH)
- 80 (HTTP)
- 443 (HTTPS)
- 8080 (后端API)

**创建目录**：
- `/opt/dong-medicine/data` - 数据目录
- `/opt/dong-medicine/backups` - 备份目录
- `/opt/dong-medicine/logs` - 日志目录

### 2. docker-deploy.sh - Docker部署

**部署流程**：
```
检查环境 → 备份数据 → 停止旧容器 → 清理端口 → 
启动新容器 → 健康检查 → 清理旧备份
```

**健康检查**：
- 后端：`http://localhost:8080/actuator/health`
- 前端：`http://localhost:80`

**备份策略**：
- 自动备份data目录
- 保留最近5个备份文件

## 环境变量配置

创建`.env`文件配置环境变量：

```env
# MySQL配置
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_USER=dongmedicine
MYSQL_PASSWORD=your_password
MYSQL_PORT=3307

# Redis配置
REDIS_PASSWORD=your_redis_password
REDIS_PORT=6379

# JWT配置
JWT_SECRET=your_jwt_secret_key_at_least_64_characters
JWT_EXPIRATION=86400000

# 服务端口
BACKEND_PORT=8080
FRONTEND_PORT=80

# CORS配置
CORS_ALLOWED_ORIGIN=http://your-server-ip

# DeepSeek AI配置（可选）
DEEPSEEK_API_KEY=your_api_key

# kkFileView配置
KKFILEVIEW_PORT=8012
```

## 部署检查清单

### 部署前检查
- [ ] 服务器已初始化（init-server.sh）
- [ ] 环境变量已配置（.env）
- [ ] 防火墙端口已开放
- [ ] GitHub Secrets已配置

### 部署后检查
- [ ] 后端服务健康检查通过
- [ ] 前端页面可访问
- [ ] API接口正常响应
- [ ] 静态资源加载正常
- [ ] 日志无错误信息

## 常见问题

### 1. 端口被占用
```bash
# 查看端口占用
netstat -tuln | grep :8080
lsof -i :8080

# 停止占用进程
kill -9 <PID>
```

### 2. Docker容器启动失败
```bash
# 查看容器日志
docker logs dong-medicine-backend

# 查看容器状态
docker ps -a
```

### 3. MySQL连接失败
```bash
# 检查MySQL容器状态
docker logs dong-medicine-mysql

# 进入MySQL容器
docker exec -it dong-medicine-mysql mysql -uroot -p
```

## 版本回滚

```bash
# 停止当前容器
docker compose down

# 恢复备份数据
tar -xzf /opt/dong-medicine/backups/data-<timestamp>.tar.gz -C /opt/dong-medicine/

# 重新启动
docker compose up -d
```

---

**最后更新时间**：2026年3月28日

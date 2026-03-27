# 部署配置目录

## 目录结构

```
deploy/
├── ci-deploy.sh                  # CI/CD自动部署脚本
├── docker-deploy.sh              # Docker部署脚本
├── init-server.sh                # 服务器初始化脚本
├── nginx.conf                    # Nginx配置文件
├── dong-medicine-backend.service # Systemd服务配置
└── README.md                     # 说明文档
```

## 部署方式

### 方式一：Docker部署（推荐）

使用Docker Compose进行容器化部署，适合快速部署和测试环境。

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

**docker-deploy.sh 功能**：
- 检查Docker环境和部署文件
- 自动备份当前版本数据
- 停止旧容器并清理端口占用
- 构建并启动新容器
- 健康检查（后端/前端）
- 清理旧备份文件（保留最近5个）

### 方式二：CI/CD部署

使用GitHub Actions自动部署，适合生产环境。

**前置条件**：
- 已配置GitHub Actions
- 服务器已初始化
- 配置了SSH密钥认证

**部署流程**：
1. 推送代码到main分支
2. GitHub Actions自动构建
3. 自动上传部署文件到服务器
4. 执行ci-deploy.sh完成部署

**ci-deploy.sh 功能**：
- 检查部署文件完整性
- 备份当前版本
- 部署后端JAR和前端静态文件
- 配置Systemd服务
- 配置Nginx反向代理
- 健康检查

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

### 3. ci-deploy.sh - CI/CD部署

**部署流程**：
```
检查文件 → 备份版本 → 停止服务 → 部署文件 → 
配置服务 → 启动服务 → 健康检查
```

**部署路径**：
- 后端：`/opt/dong-medicine/backend/`
- 前端：`/opt/dong-medicine/frontend/`
- 日志：`/opt/dong-medicine/logs/`

### 4. nginx.conf - Nginx配置

**配置内容**：

| 路径 | 代理目标 | 说明 |
|------|---------|------|
| `/` | 静态文件 | 前端SPA |
| `/api/` | `127.0.0.1:8080/api/` | 后端API |
| `/images/` | `127.0.0.1:8080/images/` | 图片资源 |
| `/videos/` | `127.0.0.1:8080/videos/` | 视频资源 |
| `/documents/` | `127.0.0.1:8080/documents/` | 文档资源 |
| `/public/` | `127.0.0.1:8080/public/` | 公共资源 |
| `/actuator/` | `127.0.0.1:8080/actuator/` | 健康检查 |

**性能优化**：
- Gzip压缩（js, css, xml, json等）
- 静态资源缓存30天
- 最大请求体100MB
- 视频流代理超时300秒

### 5. dong-medicine-backend.service - Systemd服务

**服务配置**：
```ini
[Service]
Type=simple
User=root
WorkingDirectory=/opt/dong-medicine/backend
Environment="SPRING_PROFILES_ACTIVE=prod"
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar dong-medicine-backend.jar
Restart=on-failure
RestartSec=10
```

**JVM参数**：
- 初始内存：512MB
- 最大内存：1024MB

**服务管理命令**：
```bash
# 启动服务
systemctl start dong-medicine-backend

# 停止服务
systemctl stop dong-medicine-backend

# 重启服务
systemctl restart dong-medicine-backend

# 查看状态
systemctl status dong-medicine-backend

# 查看日志
journalctl -u dong-medicine-backend -f
```

## 环境变量配置

创建`.env`文件配置环境变量：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=dong_medicine
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# 文件上传配置
FILE_UPLOAD_PATH=/opt/dong-medicine/backend/public

# DeepSeek AI配置
DEEPSEEK_API_KEY=your_api_key
DEEPSEEK_API_URL=https://api.deepseek.com
```

## 部署检查清单

### 部署前检查
- [ ] 服务器已初始化（init-server.sh）
- [ ] 环境变量已配置（.env）
- [ ] 数据库已创建并初始化
- [ ] Redis服务已启动
- [ ] 防火墙端口已开放

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

### 3. 后端服务启动失败
```bash
# 查看服务日志
journalctl -u dong-medicine-backend -n 100

# 检查配置文件
cat /opt/dong-medicine/backend/.env
```

### 4. Nginx配置错误
```bash
# 测试配置
nginx -t

# 查看错误日志
tail -f /var/log/nginx/dong-medicine-error.log
```

## 版本回滚

### Docker部署回滚
```bash
# 停止当前容器
docker compose down

# 恢复备份数据
tar -xzf /opt/dong-medicine/backups/data-<timestamp>.tar.gz -C /opt/dong-medicine/

# 重新启动
docker compose up -d
```

### CI/CD部署回滚
```bash
# 恢复后端JAR
cp /opt/dong-medicine/backups/dong-medicine-backend-<timestamp>.jar /opt/dong-medicine/backend/

# 恢复前端文件
tar -xzf /opt/dong-medicine/backups/frontend-<timestamp>.tar.gz -C /opt/dong-medicine/frontend/

# 重启服务
systemctl restart dong-medicine-backend
systemctl restart nginx
```

---

**最后更新时间**：2026年3月27日

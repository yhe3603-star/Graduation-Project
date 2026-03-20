# 侗乡医药数字展示平台 - 阿里云部署指南

## 目录结构

```
deploy/
├── install-env.sh           # 服务器环境安装脚本
├── init-database.sql        # 数据库初始化脚本
├── deploy.sh                # 一键部署脚本
├── nginx.conf               # Nginx配置文件
├── dong-medicine-backend.service  # Systemd服务配置
├── .env.production          # 生产环境变量配置
└── vite.config.production.js # 前端生产构建配置
```

## 部署流程

### 第一步：服务器环境安装

1. SSH连接到服务器：
```bash
ssh root@47.112.111.115
```

2. 上传并执行环境安装脚本：
```bash
# 创建目录
mkdir -p /opt/deploy

# 上传 install-env.sh 到服务器后执行
chmod +x install-env.sh
./install-env.sh
```

3. 安装完成后，修改MySQL密码：
```bash
# 查看临时密码
grep 'temporary password' /var/log/mysqld.log

# 登录MySQL
mysql -u root -p

# 修改密码（密码需要包含大小写字母、数字、特殊字符）
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;
EXIT;
```

### 第二步：初始化数据库

1. 上传 `init-database.sql` 到服务器

2. 执行数据库初始化：
```bash
mysql -u root -p < init-database.sql
```

3. 如果需要导入本地数据，先在本地导出：
```bash
# 本地执行
mysqldump -u root -p dong_medicine > dong_medicine_data.sql
```

然后上传到服务器并导入：
```bash
mysql -u root -p dong_medicine < dong_medicine_data.sql
```

### 第三步：本地构建

#### 3.1 构建后端JAR包

在本地项目目录执行：
```bash
cd dong-medicine-backend
./mvnw clean package -DskipTests
```

构建产物位于：`target/dong-medicine-backend-1.0.0.jar`

#### 3.2 构建前端

在本地项目目录执行：
```bash
cd dong-medicine-frontend
npm install
npm run build
```

构建产物位于：`dist/` 目录

### 第四步：配置环境变量

编辑 `.env.production` 文件，修改以下配置：

```bash
SPRING_PROFILES_ACTIVE=prod
DB_USERNAME=root
DB_PASSWORD=你的MySQL密码
JWT_SECRET=生成一个64位以上的随机字符串
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://47.112.111.115
DEEPSEEK_API_KEY=你的DeepSeek API密钥
ADMIN_INIT_PASSWORD=123456
```

生成JWT密钥的方法：
```bash
openssl rand -base64 64
```

### 第五步：上传部署文件

将以下文件上传到服务器的 `/opt/deploy/` 目录：

1. `dong-medicine-backend-1.0.0.jar` 重命名为 `dong-medicine-backend.jar`
2. 前端 `dist/` 目录重命名为 `frontend-dist`
3. `public/` 目录（包含图片、视频、文档等资源）
4. `.env.production`
5. `nginx.conf`
6. `dong-medicine-backend.service`
7. `deploy.sh`

使用scp上传示例：
```bash
# 在本地执行
scp dong-medicine-backend.jar root@47.112.111.115:/opt/deploy/
scp -r frontend-dist root@47.112.111.115:/opt/deploy/
scp -r public root@47.112.111.115:/opt/deploy/
scp .env.production nginx.conf dong-medicine-backend.service deploy.sh root@47.112.111.115:/opt/deploy/
```

### 第六步：执行部署

SSH连接到服务器，执行部署脚本：
```bash
cd /opt/deploy
chmod +x deploy.sh
./deploy.sh
```

### 第七步：验证部署

1. 检查服务状态：
```bash
systemctl status dong-medicine-backend
systemctl status nginx
```

2. 检查后端日志：
```bash
journalctl -u dong-medicine-backend -f
```

3. 访问测试：
   - 前端页面：http://47.112.111.115
   - 后端API：http://47.112.111.115/api

## 阿里云安全组配置

确保在阿里云控制台开放以下端口：

| 端口 | 协议 | 说明 |
|------|------|------|
| 22 | TCP | SSH |
| 80 | TCP | HTTP |
| 443 | TCP | HTTPS |
| 8080 | TCP | 后端服务（可选，调试用） |

## 常用运维命令

### 服务管理
```bash
# 启动后端服务
systemctl start dong-medicine-backend

# 停止后端服务
systemctl stop dong-medicine-backend

# 重启后端服务
systemctl restart dong-medicine-backend

# 查看服务状态
systemctl status dong-medicine-backend

# 查看实时日志
journalctl -u dong-medicine-backend -f

# 查看最近100行日志
journalctl -u dong-medicine-backend -n 100
```

### Nginx管理
```bash
# 测试配置
nginx -t

# 重启Nginx
systemctl restart nginx

# 重新加载配置（不中断服务）
nginx -s reload

# 查看Nginx日志
tail -f /var/log/nginx/dong-medicine-access.log
tail -f /var/log/nginx/dong-medicine-error.log
```

### 数据库管理
```bash
# 登录MySQL
mysql -u root -p

# 备份数据库
mysqldump -u root -p dong_medicine > /opt/dong-medicine/backup/dong_medicine_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p dong_medicine < /opt/dong-medicine/backup/dong_medicine_20260319.sql
```

## 更新部署

### 更新后端
```bash
# 1. 上传新的JAR包到 /opt/deploy/
# 2. 执行更新
cp /opt/deploy/dong-medicine-backend.jar /opt/dong-medicine/backend/
systemctl restart dong-medicine-backend
```

### 更新前端
```bash
# 1. 上传新的前端构建产物到 /opt/deploy/frontend-dist/
# 2. 执行更新
rm -rf /opt/dong-medicine/frontend/*
cp -r /opt/deploy/frontend-dist/* /opt/dong-medicine/frontend/
```

## 故障排查

### 后端服务无法启动
1. 检查日志：`journalctl -u dong-medicine-backend -n 100`
2. 检查环境变量：`cat /opt/dong-medicine/backend/.env`
3. 检查数据库连接：`mysql -u root -p -e "SELECT 1"`
4. 检查端口占用：`netstat -tlnp | grep 8080`

### 前端页面无法访问
1. 检查Nginx状态：`systemctl status nginx`
2. 检查Nginx配置：`nginx -t`
3. 检查前端文件：`ls -la /opt/dong-medicine/frontend/`
4. 检查Nginx日志：`tail -f /var/log/nginx/dong-medicine-error.log`

### API请求失败
1. 检查后端服务状态
2. 检查CORS配置
3. 检查防火墙设置
4. 检查Nginx代理配置

## 默认账户

- 管理员账户：`admin`
- 默认密码：`123456`

**请在首次登录后立即修改密码！**

## 联系支持

如有问题，请检查日志文件或联系开发团队。

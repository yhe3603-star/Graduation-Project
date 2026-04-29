# 部署配置目录 (deploy/)

> 把项目从你的电脑搬到互联网上，让全世界都能访问 -- 这就是部署！

---

## 一、先搞懂几个概念

### 1.1 什么是部署？

**类比：把做好的菜从厨房端到餐桌上让顾客享用。**

你在自己电脑上写好了代码，就像厨师在厨房做好了菜。但只有端到餐厅的餐桌上（服务器），顾客（用户）才能吃到。部署就是把你的代码"端"到服务器上运行的过程。

| 概念 | 类比 | 说明 |
|------|------|------|
| 本地开发 | 厨房试菜 | 你在自己电脑上写代码、调试 |
| 部署 | 端菜上桌 | 把代码放到服务器上运行 |
| 服务器 | 餐厅 | 一台24小时不关机的电脑，用户可以随时访问 |
| 域名/IP | 餐厅地址 | 用户通过这个地址找到你的网站 |

### 1.2 什么是 Docker？

**类比：集装箱，把应用和它需要的所有环境打包在一起。**

你有没有遇到过这种情况 -- 代码在自己电脑上能跑，放到别人电脑上就报错？这是因为两台电脑的环境不同（Java版本不同、MySQL版本不同等等）。

Docker 就是解决这个问题的大杀器。它像一个集装箱，把你应用需要的所有东西（代码、JDK、MySQL驱动、配置文件）全部打包在一起。不管运到哪台服务器上，打开集装箱就能跑，不会出现"在我电脑上没问题"的尴尬。

```
传统部署（容易出问题）：
  服务器A: JDK 17 + MySQL 8.0  --> 你的项目能跑
  服务器B: JDK 11 + MySQL 5.7  --> 你的项目报错！

Docker部署（到处都能跑）：
  你的项目 + JDK 17 + MySQL 8.0  --> 打包成一个"集装箱"
  不管运到哪台服务器  --> 打开就能跑！
```

**Docker 核心概念速记：**

| 术语 | 类比 | 说明 |
|------|------|------|
| Image（镜像） | 菜谱/模具 | 一个只读的模板，比如 "MySQL 8.0 镜像" |
| Container（容器） | 按菜谱做出来的菜 | 镜像运行起来的实例，可以启动、停止、删除 |
| Dockerfile | 写菜谱的纸 | 描述如何构建镜像的脚本 |
| Volume（卷） | 保险柜 | 容器删除后数据不丢失，存在宿主机上 |

### 1.3 什么是 Docker Compose？

**类比：编队航行，一条命令启动所有集装箱。**

我们的项目不是一个应用，而是5个：MySQL、Redis、后端、前端、kkFileView。如果用 Docker 一个个启动，你要敲5条命令，还要注意启动顺序（MySQL没启动，后端就连不上数据库）。

Docker Compose 就像一个总指挥，你只要写一个 `docker-compose.yml` 文件告诉它："先启动MySQL和Redis，等它们健康了再启动后端，最后启动前端"。然后一条命令 `docker compose up -d` 就全部搞定！

```
没有 Docker Compose：
  1. docker run mysql...     (手动启动MySQL)
  2. 等MySQL启动完...
  3. docker run redis...     (手动启动Redis)
  4. 等Redis启动完...
  5. docker run backend...   (手动启动后端)
  6. 等后端启动完...
  7. docker run frontend...  (手动启动前端)
  --> 太麻烦了！

有 Docker Compose：
  docker compose up -d       (一条命令，全部搞定！)
  --> 它会自动按顺序启动，还能自动重连
```

### 1.4 什么是 CI/CD？

**类比：自动流水线，代码提交后自动构建部署。**

CI = Continuous Integration（持续集成）
CD = Continuous Deployment（持续部署）

没有 CI/CD 时，你每次改完代码要手动：提交代码 -> 打包 -> 上传服务器 -> 重启服务。就像手工做衣服，一件一件缝。

有了 CI/CD，你只要把代码推送到 GitHub，剩下的全自动：GitHub Actions 自动打包 -> 自动上传服务器 -> 自动部署。就像服装工厂的流水线，你只管设计，生产交给机器。

```
你推送代码到 GitHub
       |
       v
GitHub Actions 自动触发：
  1. 拉取你的最新代码
  2. 编译后端（mvn clean package）
  3. 编译前端（npm run build）
  4. 通过 SSH 上传到服务器
  5. 在服务器上执行 docker-deploy.sh
  6. 健康检查确认服务正常
       |
       v
用户访问到最新版本！全程无需手动操作
```

### 1.5 什么是 Nginx？

**类比：前台接待，把客人引导到正确的位置。**

用户在浏览器输入你的网址，第一个接待他的就是 Nginx。Nginx 根据请求的地址，把用户引导到正确的服务：

- 访问 `/api/*` 的请求 --> 转发给后端（8080端口）
- 访问其他请求 --> 返回前端页面（静态文件）
- 访问 `/kkfileview/*` --> 转发给文档预览服务

```
用户请求 --> Nginx（前台接待）
                |
                +-- 请求 /api/plants/list  --> 转发给后端 Spring Boot
                +-- 请求 /index.html       --> 返回前端 Vue 页面
                +-- 请求 /kkfileview/       --> 转发给 kkFileView
```

---

## 二、目录结构

```
deploy/
  |-- docker-deploy.sh    # 核心部署脚本（一键部署所有服务）
  |-- init-server.sh      # 服务器初始化脚本（首次使用时运行一次）
  |-- README.md           # 本文件（你正在看的）
```

| 文件 | 一句话说明 | 什么时候用 |
|------|-----------|-----------|
| `init-server.sh` | 给服务器"装修"，装好 Docker 等工具 | **第一次**使用服务器时运行一次 |
| `docker-deploy.sh` | 把项目部署到服务器上 | **每次**更新部署时运行 |

---

## 三、docker-deploy.sh 逐步解析

这个脚本是部署的核心，下面用流程图展示它做了什么：

```
  开始
   |
   v
[1] 检查部署文件是否齐全
    - docker-compose.yml 存在吗？
    - .env 配置文件存在吗？
    - 后端/前端目录存在吗？
    --> 任何一个缺失就报错退出
   |
   v
[2] 检查 Docker 环境
    - Docker 安装了吗？
    - Docker Compose 安装了吗？
   |
   v
[3] 创建应用目录
    - /opt/dong-medicine/        (应用主目录)
    - /opt/dong-medicine/backups/ (备份目录)
   |
   v
[4] 备份当前数据
    - 如果有旧数据，打包成 data-时间戳.tar.gz
    - 存到 /opt/dong-medicine/backups/
   |
   v
[5] 停止旧容器
    - docker stop 停止所有相关容器
    - docker rm 删除容器
    - docker compose down 彻底清理
   |
   v
[6] 清理端口占用
    - 检查 8080 端口（后端）是否被占用
    - 检查 80 端口（前端）是否被占用
    - 如果被占用，kill 掉占用的进程
   |
   v
[7] 清理 Docker 网络
    - 删除旧的 dong-medicine-network
    - 防止网络冲突
   |
   v
[8] 拉取最新镜像
    - docker compose pull
    - 下载 MySQL、Redis 等基础镜像
   |
   v
[9] 构建并启动容器
    - docker compose up -d --build
    - 构建后端和前端镜像
    - 启动所有5个服务
   |
   v
[10] 健康检查（最多重试6次）
     - 后端：访问 /actuator/health，返回200表示正常
     - 前端：访问首页，返回200表示正常
     - 每次间隔10秒重试
   |
   v
[11] 清理工作
     - 只保留最近5个备份文件
     - 清理无用的 Docker 镜像
   |
   v
  完成！输出访问地址
```

**关键代码解读：**

```bash
# set -e 的意思是：任何一条命令失败，整个脚本立刻停止
# 这样可以防止"前面出错了，后面还在继续跑"的灾难
set -e

# 这几行定义了颜色输出，让终端显示更清晰
# 绿色[✓]表示成功，红色[✗]表示失败，黄色[i]表示提示
print_success() { echo -e "${COLOR_GREEN}[✓] $1${COLOR_RESET}"; }
print_error()   { echo -e "${COLOR_RED}[✗] $1${COLOR_RESET}"; }
print_info()    { echo -e "${COLOR_YELLOW}[i] $1${COLOR_RESET}"; }

# 健康检查的核心逻辑：最多尝试6次，每次间隔10秒
for i in 1 2 3 4 5 6; do
    # 用 curl 访问健康检查端点，-s 静默，-w 获取HTTP状态码
    HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$HEALTH_STATUS" = "200" ]; then
        print_success "后端服务健康检查通过"
        break  # 成功了就跳出循环
    fi
    sleep 10  # 等待10秒再试
done
```

---

## 四、init-server.sh：服务器初始化

这个脚本只需要在**第一次使用服务器时**运行一次，它帮你做这些事：

### 4.1 安装 Docker

```bash
# 它会自动检测你的系统类型，选择对应的安装方式
if command -v apt-get &> /dev/null; then
    install_docker_ubuntu    # Ubuntu/Debian 系统
elif command -v yum &> /dev/null; then
    install_docker_centos    # CentOS/RHEL 系统
fi
```

> 为什么用阿里云镜像源？因为 Docker 官方源在国内访问很慢甚至无法访问，阿里云镜像源在国内速度快很多。

### 4.2 配置 Docker 镜像加速

```bash
# 写入 /etc/docker/daemon.json 配置文件
# registry-mirrors 就是镜像加速地址，拉取镜像时自动走加速通道
# log-driver 和 log-opts 限制日志大小，防止日志撑爆磁盘
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",   # 单个日志文件最大100MB
    "max-file": "3"       # 最多保留3个日志文件
  }
}
EOF
```

### 4.3 开放防火墙端口

```bash
# 开放4个端口：
# 22   - SSH端口，你远程连接服务器用的
# 80   - 前端HTTP端口，用户访问网站用的
# 443  - HTTPS端口（预留，以后配SSL证书用）
# 8080 - 后端API端口
```

### 4.4 创建应用目录

```bash
mkdir -p /opt/dong-medicine/{data,backups,logs}
# /opt/dong-medicine/data     --> 存放应用数据
# /opt/dong-medicine/backups  --> 存放备份数据
# /opt/dong-medicine/logs     --> 存放日志文件
```

---

## 五、GitHub Actions CI/CD 流水线

### 5.1 流水线整体流程

我们的 CI/CD 配置在 `.github/workflows/ci-cd.yml`，它分为两个阶段：

```
  代码推送到 main/master 分支
       |
       v
  ======== 阶段一：构建和测试 (build-and-test) ========
       |
       +-- 检出代码 (checkout)
       +-- 安装 JDK 17
       +-- 编译后端 (mvn clean package)
       +-- 安装 Node.js 20
       +-- 安装前端依赖 (npm ci)
       +-- 编译前端 (npm run build)
       +-- 上传前端构建产物
       |
       v
  ======== 阶段二：部署 (deploy) ========
       |
       +-- 下载前端构建产物
       +-- 准备部署包（打包所有文件）
       +-- 创建生产环境 .env 文件（从 Secrets 读取）
       +-- 配置 SSH 密钥
       +-- 上传文件到服务器
       +-- 检查并安装 Docker（首次）
       +-- 预清理端口
       +-- 执行 docker-deploy.sh 部署
       +-- 健康检查（等待90秒后检查）
       +-- 清理 SSH 密钥
       |
       v
  部署完成！输出访问地址
```

### 5.2 触发条件

```yaml
on:
  push:
    branches:
      - main      # 推送到 main 分支时触发
      - master    # 推送到 master 分支时触发
    tags:
      - 'v*'      # 打标签（如 v1.0.0）时触发
  workflow_dispatch:  # 允许手动触发（在GitHub页面点按钮）
```

### 5.3 GitHub Secrets 配置

Secrets 是 GitHub 提供的加密存储，用来存放密码、密钥等敏感信息。配置路径：GitHub仓库 -> Settings -> Secrets and variables -> Actions -> New repository secret

| Secret 名称 | 说明 | 怎么填 | 示例 |
|-------------|------|--------|------|
| `SERVER_HOST` | 服务器公网IP | 你买的服务器的IP地址 | `47.112.111.115` |
| `SERVER_USER` | SSH登录用户名 | 一般是 `root` | `root` |
| `SSH_PRIVATE_KEY` | SSH私钥 | 本地生成的私钥，完整PEM格式 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `MYSQL_ROOT_PASSWORD` | MySQL root密码 | 自己设一个强密码 | `MyStr0ng!Pass` |
| `MYSQL_USER` | MySQL应用用户名 | 给应用创建的数据库用户 | `dongmedicine` |
| `MYSQL_PASSWORD` | MySQL应用用户密码 | 自己设一个强密码 | `AppDb!2024` |
| `REDIS_PASSWORD` | Redis密码 | 自己设一个强密码 | `Redis!2024` |
| `JWT_SECRET` | JWT签名密钥 | 至少64个字符的随机字符串 | 用下面命令生成 |
| `DEEPSEEK_API_KEY` | DeepSeek AI密钥 | 可选，不填AI聊天功能不可用 | `sk-xxxxx` |

**生成 JWT_SECRET 的方法：**

```bash
# 方法1：用 openssl 生成随机字符串（推荐）
openssl rand -base64 64

# 方法2：用 Python 生成
python3 -c "import secrets; print(secrets.token_urlsafe(64))"

# 方法3：用 Java 生成
java -e 'import java.util.UUID; System.out.println(UUID.randomUUID().toString().replace("-","") + UUID.randomUUID().toString().replace("-",""));'
```

**生成 SSH 密钥的方法：**

```bash
# 在你自己的电脑上执行（不是服务器上）
ssh-keygen -t ed25519 -C "github-actions"

# 执行后会生成两个文件：
# ~/.ssh/id_ed25519      --> 私钥（填到 GitHub Secrets 的 SSH_PRIVATE_KEY）
# ~/.ssh/id_ed25519.pub  --> 公钥（添加到服务器的 ~/.ssh/authorized_keys）

# 把公钥添加到服务器：
ssh-copy-id -i ~/.ssh/id_ed25519.pub root@你的服务器IP
```

---

## 六、环境变量（.env 文件）详解

`.env` 文件是整个项目的配置中心，所有密码、端口、密钥都在这里。**千万不要把这个文件提交到 Git！**

```env
# ============ MySQL配置 ============
MYSQL_ROOT_PASSWORD=你的MySQL根密码     # MySQL管理员密码，一定要设强密码
MYSQL_USER=dongmedicine                 # 给应用创建的数据库用户名
MYSQL_PASSWORD=你的MySQL用户密码         # 应用连接数据库的密码
MYSQL_PORT=3307                         # MySQL对外端口（避免和本机3306冲突）

# ============ Redis配置 ============
REDIS_PASSWORD=你的Redis密码            # Redis访问密码
REDIS_PORT=6379                         # Redis端口

# ============ JWT配置 ============
# 生产环境必须使用至少64字符的强密钥！
# JWT 是用户登录凭证的加密密钥，泄露了别人就能伪造登录
JWT_SECRET=你的JWT密钥至少64个字符
JWT_EXPIRATION=86400000                 # Token过期时间（毫秒），86400000=24小时

# ============ 服务端口 ============
BACKEND_PORT=8080                       # 后端API端口
FRONTEND_PORT=80                        # 前端端口（80是HTTP默认端口）

# ============ CORS跨域配置 ============
# 允许哪个前端地址访问后端API，生产环境替换为实际域名
CORS_ALLOWED_ORIGIN=http://你的服务器IP

# ============ AI配置（可选） ============
DEEPSEEK_API_KEY=你的DeepSeek密钥       # 不填则AI聊天功能不可用

# ============ kkFileView配置 ============
KKFILEVIEW_PORT=8012                    # 文档预览服务端口
```

> **新手常见错误**：把 `.env` 文件提交到了 Git，导致密码泄露。项目里已经有 `.gitignore` 忽略了 `.env` 文件，但你要确保不会手动 `git add .env`。

---

## 七、服务器最低配置要求

| 资源 | 最低要求 | 推荐配置 | 为什么 |
|------|---------|---------|--------|
| CPU | 2核 | 4核 | 5个Docker容器同时运行，2核勉强够用 |
| 内存 | 4GB | 8GB | MySQL吃1GB+，Redis吃0.5GB，后端吃1GB+，前端吃0.5GB |
| 磁盘 | 40GB | 80GB SSD | Docker镜像+数据+日志，SSD读写快很多 |
| 带宽 | 5Mbps | 10Mbps | 图片和视频资源需要带宽支撑 |

> **省钱提示**：阿里云/腾讯云的学生机（2核4G）可以跑起来，但会比较吃力。如果预算允许，4核8G体验会好很多。

---

## 八、部署检查清单

### 部署前（确保这些都准备好了）

- [ ] 服务器已通过 `init-server.sh` 初始化（Docker已安装）
- [ ] `.env` 文件已正确配置所有参数（没有空着的密码）
- [ ] 防火墙已开放 22/80/443/8080 端口
- [ ] GitHub Secrets 已全部配置（CI/CD部署时）
- [ ] 数据库SQL文件存在（`dong-medicine-backend/sql/dong_medicine.sql`）
- [ ] SSH密钥已配置（能免密登录服务器）

### 部署后（确认这些都能正常工作）

- [ ] 后端健康检查通过：浏览器访问 `http://服务器IP:8080/actuator/health` 返回 `{"status":"UP"}`
- [ ] 前端页面可正常访问：浏览器访问 `http://服务器IP` 能看到页面
- [ ] API接口正常响应：访问 `http://服务器IP:8080/api/plants/list` 返回数据
- [ ] 静态资源（图片/文档）加载正常
- [ ] 容器日志无错误信息：`docker compose logs` 没有红色报错

---

## 九、常见问题与解决方案

### 9.1 端口被占用

**症状**：启动容器时报错 `port is already allocated` 或容器一直重启。

```bash
# 第一步：查看是谁占用了端口
netstat -tuln | grep :8080
# 输出类似：tcp  0  0  0.0.0.0:8080  0.0.0.0:*  LISTEN  12345/java

# 第二步：查看占用进程的详细信息
lsof -i :8080
# 或者
ss -tulnp | grep :8080

# 第三步：停掉占用端口的进程
kill -9 12345    # 12345 是进程ID（PID），换成你看到的

# 如果不确定能不能kill，先看看是什么进程
ps -ef | grep 12345
```

**预防**：部署脚本已经内置了端口清理逻辑，一般不会遇到这个问题。如果遇到了，按上面步骤手动清理。

### 9.2 Docker 容器启动失败

**症状**：`docker compose ps` 显示容器状态是 `Restarting` 或 `Exited`。

```bash
# 第一步：查看容器日志（最重要的排查手段！）
docker compose logs backend     # 查看后端日志
docker compose logs frontend    # 查看前端日志
docker compose logs mysql       # 查看MySQL日志

# 第二步：查看所有容器状态
docker compose ps -a

# 第三步：如果日志看不懂，尝试重新构建
docker compose down             # 先停止所有容器
docker compose up -d --build    # 重新构建并启动

# 第四步：如果还是不行，彻底清理重来
docker compose down -v          # -v 会删除数据卷，慎用！
docker compose up -d --build
```

**常见原因**：
- MySQL还没启动完，后端就尝试连接 --> 等一会再试
- `.env` 文件配置错误 --> 检查密码、端口等配置
- 内存不足 --> `free -h` 查看内存使用情况

### 9.3 MySQL 连接失败

**症状**：后端日志报错 `Communications link failure` 或 `Access denied`。

```bash
# 第一步：检查MySQL容器是否正常运行
docker compose ps mysql
# 状态应该是 Up (healthy)

# 第二步：查看MySQL日志
docker compose logs mysql

# 第三步：进入MySQL容器手动检查
docker exec -it dong-medicine-mysql mysql -uroot -p
# 输入你在 .env 里设置的 MYSQL_ROOT_PASSWORD

# 第四步：确认数据库已创建
# 在MySQL命令行里执行：
SHOW DATABASES;
# 应该能看到 dong_medicine 数据库

# 第五步：确认用户权限
SELECT user, host FROM mysql.user;
```

**常见原因**：
- 密码输错了 --> 检查 `.env` 里的密码和 `docker-compose.yml` 里的是否一致
- MySQL还在初始化 --> 第一次启动需要1-2分钟初始化，耐心等待
- 端口映射错误 --> 检查 `MYSQL_PORT` 配置

### 9.4 前端页面空白

**症状**：浏览器访问显示空白页。

```bash
# 检查前端容器日志
docker compose logs frontend

# 检查Nginx配置是否正确
docker exec -it dong-medicine-frontend cat /etc/nginx/conf.d/default.conf

# 检查前端构建产物是否存在
docker exec -it dong-medicine-frontend ls -la /usr/share/nginx/html/
```

**常见原因**：
- 前端构建失败 --> 查看CI/CD日志中的 build 步骤
- Nginx配置中API代理地址错误 --> 检查 `proxy_pass` 配置
- 浏览器缓存 --> 强制刷新（Ctrl+Shift+R）

### 9.5 Docker 镜像拉取失败

**症状**：`docker compose pull` 报错或超时。

```bash
# 检查镜像加速是否配置
cat /etc/docker/daemon.json

# 手动测试拉取
docker pull mysql:8.0

# 如果还是不行，重启 Docker
systemctl restart docker
```

---

## 十、版本回滚步骤

部署出了问题？别慌，我们有备份！

```bash
# 第一步：停止当前容器
cd /opt/dong-medicine
docker compose down

# 第二步：查看有哪些备份可用
ls -la /opt/dong-medicine/backups/
# 输出类似：
# data-20260420_143000.tar.gz
# data-20260421_091500.tar.gz
# data-20260422_160000.tar.gz

# 第三步：选择一个备份恢复
# 把 <timestamp> 替换成你想回滚的版本时间戳
tar -xzf /opt/dong-medicine/backups/data-<timestamp>.tar.gz -C /opt/dong-medicine/

# 第四步：重新启动容器
docker compose up -d

# 第五步：确认服务正常
docker compose ps
curl http://localhost:8080/actuator/health
```

> **重要提醒**：回滚只能恢复数据文件，不能恢复代码版本。如果你需要回滚代码，需要用 `git checkout` 切到之前的提交，再重新部署。

---

## 十一、快速部署命令速查

```bash
# === 首次部署 ===
# 1. 初始化服务器（只需执行一次）
chmod +x init-server.sh && ./init-server.sh

# 2. 配置环境变量
cp .env.example .env
vim .env    # 填写所有配置项

# 3. 执行部署
chmod +x docker-deploy.sh && ./docker-deploy.sh

# === 日常运维 ===
# 查看所有容器状态
docker compose ps

# 查看后端日志（实时跟踪）
docker compose logs -f backend

# 重启某个服务
docker compose restart backend

# 重新构建并启动
docker compose up -d --build

# 查看资源使用情况
docker stats
```

---

## 十二、代码审查与改进建议

- [安全] docker-compose.yml中默认密码过于简单：MYSQL_ROOT_PASSWORD默认root123，REDIS_PASSWORD默认redis123，JWT_SECRET默认your-secret-key-change-in-production
- [安全] RabbitMQ端口(5672/15672)、kkfileview端口(8012)、backend端口(8080)不必要地暴露到宿主机，应仅在内网通信
- [安全] CI/CD(ci-cd.yml)中硬编码了所有密码，未使用GitHub Secrets
- [安全] entrypoint.sh中通过命令行参数传递MySQL密码，会出现在ps aux进程列表中
- [配置] docker-compose.yml的depends_on未使用condition: service_healthy，不保证服务就绪
- [配置] 前端Dockerfile HEALTHCHECK依赖curl但nginx:1.25-alpine镜像默认不包含curl
- [配置] 后端Dockerfile的ENTRYPOINT与entrypoint.sh冲突，entrypoint.sh中的数据库初始化逻辑永远不会执行
- [配置] 后端Dockerfile中SQL初始化文件被COPY但未使用
- [安全] init-server.sh防火墙开放了8080端口，后端API应通过Nginx反向代理访问
- [安全] Nginx配置(default.conf)中安全头在location块中被覆盖，导致部分响应丢失X-Frame-Options等安全头
- [安全] Nginx配置缺少HSTS和Referrer-Policy安全头
- [安全] /health端点暴露后端健康信息
- [配置] proxy_cache_valid未配合proxy_cache_path，代理缓存不生效
- [配置] docker-deploy.sh禁用了BuildKit(DOCKER_BUILDKIT=0)，导致构建缓存无法有效利用

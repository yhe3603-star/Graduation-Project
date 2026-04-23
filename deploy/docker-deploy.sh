#!/bin/bash

set -e

echo "=========================================="
echo "  侗乡医药数字展示平台 - Docker 部署"
echo "=========================================="
echo ""

COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_RESET='\033[0m'

print_success() { echo -e "${COLOR_GREEN}[✓] $1${COLOR_RESET}"; }
print_error() { echo -e "${COLOR_RED}[✗] $1${COLOR_RESET}"; }
print_info() { echo -e "${COLOR_YELLOW}[i] $1${COLOR_RESET}"; }
print_step() { echo -e "${COLOR_BLUE}==> $1${COLOR_RESET}"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="/opt/dong-medicine"
BACKUP_DIR="/opt/dong-medicine/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
NO_CACHE=""

for arg in "$@"; do
    case $arg in
        --no-cache) NO_CACHE="--no-cache" ;;
    esac
done

if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    print_error "Docker Compose 未安装"
    exit 1
fi

print_info "使用 Docker Compose: $COMPOSE_CMD"

print_step "检查部署文件..."
[ ! -f "$SCRIPT_DIR/docker-compose.yml" ] && print_error "未找到 docker-compose.yml" && exit 1
[ ! -f "$SCRIPT_DIR/.env" ] && print_error "未找到 .env 配置文件" && exit 1
[ ! -d "$SCRIPT_DIR/dong-medicine-backend" ] && print_error "未找到后端目录" && exit 1
[ ! -d "$SCRIPT_DIR/dong-medicine-frontend" ] && print_error "未找到前端目录" && exit 1
print_success "部署文件检查通过"

print_step "检查 Docker 环境..."
if ! command -v docker &> /dev/null; then
    print_error "Docker 未安装，请先运行 init-server.sh 安装 Docker"
    exit 1
fi
print_success "Docker 环境检查通过"

print_step "创建应用目录..."
mkdir -p "$APP_DIR" "$BACKUP_DIR"
print_success "目录创建完成"

print_step "备份当前版本..."
if [ -d "$APP_DIR/data" ]; then
    tar -czf "$BACKUP_DIR/data-$TIMESTAMP.tar.gz" -C "$APP_DIR" data 2>/dev/null || true
    print_info "数据已备份"
fi
print_success "备份完成"

print_step "部署应用文件..."
cp "$SCRIPT_DIR/docker-compose.yml" "$APP_DIR/"
cp "$SCRIPT_DIR/.env" "$APP_DIR/"
rm -rf "$APP_DIR/dong-medicine-backend"
rm -rf "$APP_DIR/dong-medicine-frontend"
cp -r "$SCRIPT_DIR/dong-medicine-backend" "$APP_DIR/"
cp -r "$SCRIPT_DIR/dong-medicine-frontend" "$APP_DIR/"
print_success "应用文件部署完成"

print_step "停止旧容器..."
cd "$APP_DIR"
# 强制停止所有相关容器
print_info "强制停止所有相关容器..."
docker stop $(docker ps -q --filter name=dong-medicine-) 2>/dev/null || true
docker rm $(docker ps -aq --filter name=dong-medicine-) 2>/dev/null || true
# 执行docker-compose down
$COMPOSE_CMD down --remove-orphans --volumes 2>/dev/null || true
print_success "旧容器已停止"

print_step "检查并清理占用端口的进程..."
# 使用netstat检查端口占用
print_info "检查8080端口占用情况..."
netstat -tuln 2>/dev/null | grep :8080 || echo "8080端口未被占用"

# 检查并停止占用8080端口的进程
if command -v lsof &> /dev/null; then
    PORT_PID=$(lsof -t -i:8080 2>/dev/null || true)
    if [ -n "$PORT_PID" ]; then
        print_info "发现占用8080端口的进程: $PORT_PID"
        # 尝试优雅停止
        kill $PORT_PID 2>/dev/null || true
        sleep 2
        # 强制停止
        kill -9 $PORT_PID 2>/dev/null || true
        print_info "已停止占用8080端口的进程"
    else
        print_info "8080端口未被占用"
    fi
else
    # 尝试使用netstat和killall作为替代
    print_info "lsof 未安装，尝试使用其他方法..."
    if command -v netstat &> /dev/null && command -v awk &> /dev/null; then
        PORT_PID=$(netstat -tuln | grep :8080 | awk '{print $7}' | cut -d'/' -f1 2>/dev/null || true)
        if [ -n "$PORT_PID" ]; then
            print_info "发现占用8080端口的进程: $PORT_PID"
            kill -9 $PORT_PID 2>/dev/null || true
            print_info "已停止占用8080端口的进程"
        fi
    fi
fi

# 检查并停止占用80端口的进程
print_info "检查80端口占用情况..."
netstat -tuln 2>/dev/null | grep :80 || echo "80端口未被占用"

if command -v lsof &> /dev/null; then
    PORT_PID=$(lsof -t -i:80 2>/dev/null || true)
    if [ -n "$PORT_PID" ]; then
        print_info "发现占用80端口的进程: $PORT_PID"
        kill -9 $PORT_PID 2>/dev/null || true
        print_info "已停止占用80端口的进程"
    else
        print_info "80端口未被占用"
    fi
fi

# 等待端口释放
sleep 5
print_success "端口清理完成"

print_step "清理Docker网络..."
# 检查并清理旧的网络
print_info "当前Docker网络状态..."
docker network ls 2>/dev/null || echo "无法列出网络"

if docker network ls | grep -q "dong-medicine-network"; then
    print_info "发现旧的网络 dong-medicine-network，正在清理..."
    # 强制清理网络
    docker network rm dong-medicine-network 2>/dev/null || true
    # 再次尝试清理
    docker network prune -f 2>/dev/null || true
    print_info "网络清理完成"
else
    print_info "网络 dong-medicine-network 不存在，跳过清理"
fi

# 再次检查端口状态
print_step "最终端口状态检查..."
PORT_8080_STATUS=$(netstat -tuln 2>/dev/null | grep :8080 || echo "未被占用")
PORT_80_STATUS=$(netstat -tuln 2>/dev/null | grep :80 || echo "未被占用")
print_info "8080端口状态: $PORT_8080_STATUS"
print_info "80端口状态: $PORT_80_STATUS"

# 尝试使用ss命令检查端口
if command -v ss &> /dev/null; then
    print_info "使用ss命令检查端口..."
    ss -tuln | grep -E ':8080|:80' || echo "8080和80端口均未被占用"
fi

# 检查是否有其他Docker容器占用端口
print_info "检查Docker容器端口占用..."
docker ps -a --format '{{.Names}}: {{.Ports}}' | grep -E '8080|80' || echo "没有Docker容器占用8080或80端口"

# 等待几秒钟确保端口完全释放
sleep 3

print_step "清理Docker构建缓存..."
docker builder prune -af 2>/dev/null || true
print_success "Docker构建缓存清理完成"

print_step "拉取最新镜像..."
$COMPOSE_CMD pull 2>/dev/null || true
print_info "镜像拉取完成"

print_step "构建并启动容器..."
if [ -n "$NO_CACHE" ]; then
    print_info "无缓存构建模式"
    $COMPOSE_CMD build --no-cache
else
    $COMPOSE_CMD build
fi
$COMPOSE_CMD up -d
print_success "容器启动完成"

print_step "等待服务启动..."
sleep 30

print_step "健康检查..."
for i in 1 2 3 4 5 6; do
    HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
    if [ "$HEALTH_STATUS" = "200" ]; then
        print_success "后端服务健康检查通过"
        break
    fi
    if [ $i -lt 6 ]; then
        print_info "后端健康检查返回 $HEALTH_STATUS，等待重试 ($i/6)..."
        sleep 10
    else
        print_error "后端健康检查失败 (HTTP $HEALTH_STATUS)"
        $COMPOSE_CMD logs --tail=50 backend
    fi
done

FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:80 2>/dev/null || echo "000")
if [ "$FRONTEND_STATUS" = "200" ]; then
    print_success "前端服务健康检查通过"
else
    print_info "前端健康检查返回 $FRONTEND_STATUS"
fi

print_step "清理旧备份文件(保留最近5个)..."
cd "$BACKUP_DIR" && ls -t data-*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm -f
print_success "旧备份清理完成"

print_step "清理无用 Docker 资源..."
docker image prune -f 2>/dev/null || true
print_success "Docker 清理完成"

echo ""
echo "=========================================="
print_success "Docker 部署完成！"
echo "=========================================="
echo ""
echo "部署时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "版本标识: $TIMESTAMP"
echo ""
echo "服务状态:"
$COMPOSE_CMD ps
echo ""
echo "访问地址:"
# 尝试获取公网IP
PUBLIC_IP=$(curl -s ifconfig.me || curl -s icanhazip.com || curl -s ipinfo.io/ip || echo "47.112.111.115")
# 如果公网IP获取失败，使用内网IP作为备选
if [ -z "$PUBLIC_IP" ] || [[ "$PUBLIC_IP" == *"127.0.0.1"* ]] || [[ "$PUBLIC_IP" == *"172."* ]] || [[ "$PUBLIC_IP" == *"192.168."* ]]; then
    PUBLIC_IP=$(hostname -I | awk '{print $1}')
fi
echo "  前端: http://$PUBLIC_IP"
echo "  后端: http://$PUBLIC_IP:8080/api/"
echo "  Swagger: http://$PUBLIC_IP:8080/swagger-ui/"
echo "  注意: 请使用服务器公网IP访问: http://47.112.111.115"
echo ""

#!/bin/bash

set -e

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
ROLLBACK=false

for arg in "$@"; do
    case $arg in
        --no-cache) NO_CACHE="--no-cache" ;;
        --rollback) ROLLBACK=true ;;
    esac
done

if $ROLLBACK; then
    print_step "回滚到上一版本..."
    LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/app-*.tar.gz 2>/dev/null | head -1)
    if [ -z "$LATEST_BACKUP" ]; then
        print_error "未找到可回滚的备份"
        exit 1
    fi
    print_info "回滚到: $LATEST_BACKUP"
    cd "$APP_DIR"
    if docker compose version &> /dev/null; then
        docker compose down --remove-orphans 2>/dev/null || true
    elif command -v docker-compose &> /dev/null; then
        docker-compose down --remove-orphans 2>/dev/null || true
    fi
    rm -rf "$APP_DIR/dong-medicine-backend" "$APP_DIR/dong-medicine-frontend"
    tar -xzf "$LATEST_BACKUP" -C "$APP_DIR"
    if docker compose version &> /dev/null; then
        docker compose up -d
    elif command -v docker-compose &> /dev/null; then
        docker-compose up -d
    fi
    print_success "回滚完成"
    exit 0
fi

echo ""
echo "=========================================="
echo "  侗乡医药数字展示平台 - Docker 部署"
echo "=========================================="
echo ""

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
    print_error "Docker 未安装，请先运行 init-server.sh"
    exit 1
fi
print_success "Docker 环境检查通过"

print_step "创建应用目录..."
mkdir -p "$APP_DIR" "$BACKUP_DIR"
print_success "目录创建完成"

print_step "备份当前版本..."
if [ -d "$APP_DIR/dong-medicine-backend" ] || [ -d "$APP_DIR/dong-medicine-frontend" ]; then
    tar -czf "$BACKUP_DIR/app-$TIMESTAMP.tar.gz" \
        -C "$APP_DIR" \
        dong-medicine-backend dong-medicine-frontend 2>/dev/null || true
    print_info "应用已备份: app-$TIMESTAMP.tar.gz"
fi
if [ -d "$APP_DIR/data" ]; then
    tar -czf "$BACKUP_DIR/data-$TIMESTAMP.tar.gz" -C "$APP_DIR" data 2>/dev/null || true
    print_info "数据已备份"
fi
print_success "备份完成"

print_step "部署应用文件..."
cp "$SCRIPT_DIR/docker-compose.yml" "$APP_DIR/"
cp "$SCRIPT_DIR/.env" "$APP_DIR/"
rm -rf "$APP_DIR/dong-medicine-backend" "$APP_DIR/dong-medicine-frontend"
cp -r "$SCRIPT_DIR/dong-medicine-backend" "$APP_DIR/"
cp -r "$SCRIPT_DIR/dong-medicine-frontend" "$APP_DIR/"
print_success "应用文件部署完成"

print_step "停止旧容器..."
cd "$APP_DIR"
docker stop $(docker ps -q --filter name=dong-medicine-) 2>/dev/null || true
docker rm $(docker ps -aq --filter name=dong-medicine-) 2>/dev/null || true
$COMPOSE_CMD down --remove-orphans 2>/dev/null || true
print_success "旧容器已停止"

print_step "清理端口与网络..."
if command -v lsof &> /dev/null; then
    for port in 8080 80; do
        PID=$(lsof -t -i:$port 2>/dev/null || true)
        if [ -n "$PID" ]; then
            print_info "释放端口 $port (PID: $PID)"
            kill $PID 2>/dev/null || true
            sleep 1
            kill -9 $PID 2>/dev/null || true
        fi
    done
fi
docker network rm dong-medicine-network 2>/dev/null || true
docker network prune -f 2>/dev/null || true
sleep 3
print_success "端口与网络清理完成"

print_step "构建并启动容器..."

export DOCKER_BUILDKIT=0
export COMPOSE_DOCKER_CLI_BUILD=0
print_info "已禁用BuildKit以提高兼容性"

BUILD_TIMEOUT=600
print_info "构建超时设置: ${BUILD_TIMEOUT}秒"

if [ -n "$NO_CACHE" ]; then
    print_info "无缓存构建模式"
    timeout $BUILD_TIMEOUT $COMPOSE_CMD build --no-cache 2>&1 || {
        if [ $? -eq 124 ]; then
            print_error "构建超时，尝试使用缓存重试..."
            $COMPOSE_CMD build 2>&1 || {
                print_error "构建失败，请检查网络连接和Docker配置"
                exit 1
            }
        else
            print_error "构建失败"
            exit 1
        fi
    }
else
    timeout $BUILD_TIMEOUT $COMPOSE_CMD build 2>&1 || {
        if [ $? -eq 124 ]; then
            print_error "构建超时，请检查服务器网络和资源"
            exit 1
        else
            print_error "构建失败"
            exit 1
        fi
    }
fi
print_success "镜像构建完成"

$COMPOSE_CMD up -d --wait-timeout 300 2>/dev/null || $COMPOSE_CMD up -d
print_success "容器启动完成"

print_step "等待后端服务就绪..."
BACKEND_READY=false
for i in $(seq 1 30); do
    HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
    if [ "$HEALTH_STATUS" = "200" ]; then
        print_success "后端服务已就绪"
        BACKEND_READY=true
        break
    fi
    [ $i -lt 30 ] && print_info "等待后端启动... ($i/30, HTTP $HEALTH_STATUS)" && sleep 10
done

if ! $BACKEND_READY; then
    print_error "后端服务启动超时，查看日志："
    $COMPOSE_CMD logs --tail=50 backend
    exit 1
fi

FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:80 2>/dev/null || echo "000")
if [ "$FRONTEND_STATUS" = "200" ]; then
    print_success "前端服务健康检查通过"
else
    print_info "前端健康检查返回 $FRONTEND_STATUS"
fi

print_step "清理旧备份(保留最近5个)..."
cd "$BACKUP_DIR" && ls -t app-*.tar.gz data-*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm -f
print_success "旧备份清理完成"

print_step "清理无用 Docker 资源..."
docker image prune -f 2>/dev/null || true
docker builder prune -af 2>/dev/null || true
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
PUBLIC_IP=$(curl -s --connect-timeout 5 ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}')
echo "  前端: http://$PUBLIC_IP"
echo "  后端: http://$PUBLIC_IP:8080/api/"
echo ""
echo "回滚命令: sudo bash docker-deploy.sh --rollback"
echo ""

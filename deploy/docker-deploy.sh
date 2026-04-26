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

print_step "清理网络..."
docker network rm dong-medicine-network 2>/dev/null || true
sleep 2
print_success "网络清理完成"

print_step "构建镜像..."
export DOCKER_BUILDKIT=0
export COMPOSE_DOCKER_CLI_BUILD=0
print_info "已禁用BuildKit"

BUILD_START=$(date +%s)

if [ -n "$NO_CACHE" ]; then
    print_info "无缓存构建模式"
    $COMPOSE_CMD build --no-cache 2>&1 || {
        print_error "构建失败"
        exit 1
    }
else
    $COMPOSE_CMD build 2>&1 || {
        print_error "构建失败"
        exit 1
    }
fi

BUILD_END=$(date +%s)
BUILD_TIME=$((BUILD_END - BUILD_START))
print_success "镜像构建完成 (耗时: ${BUILD_TIME}秒)"

print_step "启动容器..."
$COMPOSE_CMD up -d
print_success "容器已启动"

sleep 5

print_step "检查容器状态..."
$COMPOSE_CMD ps

print_step "清理旧备份(保留最近3个)..."
cd "$BACKUP_DIR" && ls -t app-*.tar.gz 2>/dev/null | tail -n +4 | xargs -r rm -f
print_success "清理完成"

echo ""
echo "=========================================="
print_success "部署完成！"
echo "=========================================="
echo ""
echo "部署时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "构建耗时: ${BUILD_TIME}秒"
echo ""
echo "服务状态:"
$COMPOSE_CMD ps
echo ""
echo "访问地址:"
PUBLIC_IP=$(curl -s --connect-timeout 3 ifconfig.me 2>/dev/null || hostname -I 2>/dev/null | awk '{print $1}')
echo "  前端: http://$PUBLIC_IP"
echo "  后端: http://$PUBLIC_IP:8080"
echo ""
echo "查看日志: cd $APP_DIR && $COMPOSE_CMD logs -f"
echo "回滚命令: sudo bash docker-deploy.sh --rollback"
echo ""

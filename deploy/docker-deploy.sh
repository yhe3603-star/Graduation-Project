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

print_info "使用: $COMPOSE_CMD"

print_step "部署应用文件..."
mkdir -p "$APP_DIR"
cp "$SCRIPT_DIR/docker-compose.yml" "$APP_DIR/"
cp "$SCRIPT_DIR/.env" "$APP_DIR/"
rm -rf "$APP_DIR/dong-medicine-backend" "$APP_DIR/dong-medicine-frontend"
cp -r "$SCRIPT_DIR/dong-medicine-backend" "$APP_DIR/"
cp -r "$SCRIPT_DIR/dong-medicine-frontend" "$APP_DIR/"
print_success "文件部署完成"

print_step "停止旧容器..."
cd "$APP_DIR"
$COMPOSE_CMD down --remove-orphans 2>/dev/null || true
print_success "旧容器已停止"

print_step "构建镜像 (使用缓存)..."
export DOCKER_BUILDKIT=0
$COMPOSE_CMD build 2>&1 || {
    print_error "构建失败"
    exit 1
}
print_success "镜像构建完成"

print_step "启动容器..."
$COMPOSE_CMD up -d
print_success "容器已启动"

sleep 3

echo ""
echo "=========================================="
print_success "部署完成！"
echo "=========================================="
echo ""
$COMPOSE_CMD ps
echo ""
PUBLIC_IP=$(curl -s --connect-timeout 3 ifconfig.me 2>/dev/null || hostname -I 2>/dev/null | awk '{print $1}')
echo "访问地址:"
echo "  前端: http://$PUBLIC_IP"
echo "  后端: http://$PUBLIC_IP:8080"
echo ""

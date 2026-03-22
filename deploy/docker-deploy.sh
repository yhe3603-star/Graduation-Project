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
cp -r "$SCRIPT_DIR/dong-medicine-backend" "$APP_DIR/"
cp -r "$SCRIPT_DIR/dong-medicine-frontend" "$APP_DIR/"
print_success "应用文件部署完成"

print_step "停止旧容器..."
cd "$APP_DIR"
$COMPOSE_CMD down --remove-orphans 2>/dev/null || true
print_success "旧容器已停止"

print_step "拉取最新镜像..."
$COMPOSE_CMD pull 2>/dev/null || true
print_info "镜像拉取完成"

print_step "构建并启动容器..."
$COMPOSE_CMD up -d --build
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
echo "  前端: http://$(hostname -I | awk '{print $1}')"
echo "  后端: http://$(hostname -I | awk '{print $1}'):8080/api/"
echo "  Swagger: http://$(hostname -I | awk '{print $1}'):8080/swagger-ui/"
echo ""

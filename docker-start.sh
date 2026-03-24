#!/bin/bash

set -e

echo "=========================================="
echo "  侗乡医药数字展示平台 - 本地Docker启动"
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
    print_error "Docker 未安装，请先安装 Docker"
    exit 1
fi
print_success "Docker 环境检查通过"

print_step "停止旧容器..."
# 强制停止所有相关容器
print_info "强制停止所有相关容器..."
docker stop $(docker ps -q --filter name=dong-medicine-) 2>/dev/null || true
docker rm $(docker ps -aq --filter name=dong-medicine-) 2>/dev/null || true
# 执行docker-compose down
$COMPOSE_CMD down --remove-orphans --volumes 2>/dev/null || true
print_success "旧容器已停止"

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

print_step "构建并启动容器..."
$COMPOSE_CMD build --no-cache
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

print_step "清理无用 Docker 资源..."
docker image prune -f 2>/dev/null || true
print_success "Docker 清理完成"

echo ""
echo "=========================================="
print_success "Docker 启动完成！"
echo "=========================================="
echo ""
echo "部署时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "版本标识: $TIMESTAMP"
echo ""
echo "服务状态:"
$COMPOSE_CMD ps
echo ""
echo "访问地址:"
echo "  前端: http://localhost"
echo "  后端: http://localhost:8080/api/"
echo "  Swagger: http://localhost:8080/swagger-ui/"
echo ""
echo "注意: 在本地环境中，请使用 localhost 访问服务"
echo ""

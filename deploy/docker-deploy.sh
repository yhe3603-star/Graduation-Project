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
APP_DIR="${APP_DIR:-/opt/dong-medicine}"
BACKUP_DIR="${APP_DIR}/backups"
VERSION="$(date +%Y%m%d_%H%M%S)"

check_dependencies() {
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
    else
        print_error "Docker Compose 未安装"
        exit 1
    fi
}

backup_current() {
    mkdir -p "$BACKUP_DIR"
    [ -f "$APP_DIR/docker-compose.yml" ] && cp "$APP_DIR/docker-compose.yml" "$BACKUP_DIR/docker-compose_$VERSION.yml"
    [ -f "$APP_DIR/.env" ] && cp "$APP_DIR/.env" "$BACKUP_DIR/.env_$VERSION"
}

deploy_files() {
    print_step "部署文件..."
    mkdir -p "$APP_DIR"
    
    [ -f "$SCRIPT_DIR/../docker-compose.yml" ] && cp "$SCRIPT_DIR/../docker-compose.yml" "$APP_DIR/"
    [ -f "$SCRIPT_DIR/../.env" ] && cp "$SCRIPT_DIR/../.env" "$APP_DIR/"
    
    rm -rf "$APP_DIR/dong-medicine-backend" "$APP_DIR/dong-medicine-frontend" 2>/dev/null || true
    
    [ -d "$SCRIPT_DIR/../dong-medicine-backend" ] && cp -r "$SCRIPT_DIR/../dong-medicine-backend" "$APP_DIR/"
    [ -d "$SCRIPT_DIR/../dong-medicine-frontend" ] && cp -r "$SCRIPT_DIR/../dong-medicine-frontend" "$APP_DIR/"
    
    print_success "完成"
}

deploy() {
    cd "$APP_DIR"
    
    print_step "拉取镜像..."
    $COMPOSE_CMD pull 2>/dev/null || true
    
    print_step "启动服务..."
    export DOCKER_BUILDKIT=1
    $COMPOSE_CMD up -d --build
    
    print_step "等待就绪..."
    for i in $(seq 1 30); do
        if docker exec dong-medicine-backend curl -sf http://localhost:8080/actuator/health 2>/dev/null; then
            print_success "服务就绪!"
            return 0
        fi
        sleep 2
    done
    
    print_error "服务未就绪"
    docker logs dong-medicine-backend --tail 30
    return 1
}

rollback() {
    print_error "部署失败，回滚..."
    local backup=$(ls -t "$BACKUP_DIR"/docker-compose_*.yml 2>/dev/null | head -1)
    if [ -n "$backup" ]; then
        cp "$backup" "$APP_DIR/docker-compose.yml"
        cd "$APP_DIR" && $COMPOSE_CMD up -d
    fi
    exit 1
}

main() {
    echo ""
    echo "=========================================="
    echo "  侗乡医药平台部署 v$VERSION"
    echo "=========================================="
    echo ""
    
    check_dependencies
    backup_current
    
    deploy_files || rollback
    deploy || rollback
    
    echo ""
    echo "=========================================="
    print_success "部署完成!"
    echo "=========================================="
    
    cd "$APP_DIR"
    $COMPOSE_CMD ps
    
    IP=$(curl -s --connect-timeout 2 ifconfig.me 2>/dev/null || hostname -I 2>/dev/null | awk '{print $1}')
    echo ""
    echo "访问: http://$IP:${FRONTEND_PORT:-3000}"
    echo ""
}

main "$@"

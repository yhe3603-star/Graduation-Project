#!/bin/bash

set -e

echo "=========================================="
echo "  侗乡医药数字展示平台 - CI/CD 自动部署"
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
BACKEND_DIR="/opt/dong-medicine/backend"
FRONTEND_DIR="/opt/dong-medicine/frontend"
LOGS_DIR="/opt/dong-medicine/logs"
BACKUP_DIR="/opt/dong-medicine/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

if [ "$(id -u)" -ne 0 ]; then
    print_error "请使用root用户执行此脚本"
    exit 1
fi

print_step "检查部署文件..."
[ ! -f "$SCRIPT_DIR/dong-medicine-backend.jar" ] && print_error "未找到后端JAR文件" && exit 1
[ ! -d "$SCRIPT_DIR/frontend-dist" ] && print_error "未找到前端构建目录" && exit 1
[ ! -f "$SCRIPT_DIR/.env" ] && print_error "未找到环境配置文件" && exit 1
print_success "部署文件检查通过"

print_step "创建应用目录..."
mkdir -p "$BACKEND_DIR" "$FRONTEND_DIR" "$LOGS_DIR" "$BACKUP_DIR" "$BACKEND_DIR/public"
print_success "目录创建完成"

print_step "备份当前版本..."
[ -f "$BACKEND_DIR/dong-medicine-backend.jar" ] && cp "$BACKEND_DIR/dong-medicine-backend.jar" "$BACKUP_DIR/dong-medicine-backend-$TIMESTAMP.jar" && print_info "后端已备份"
[ -d "$FRONTEND_DIR" ] && [ "$(ls -A $FRONTEND_DIR 2>/dev/null)" ] && tar -czf "$BACKUP_DIR/frontend-$TIMESTAMP.tar.gz" -C "$FRONTEND_DIR" . && print_info "前端已备份"
print_success "备份完成"

print_step "停止后端服务..."
systemctl is-active --quiet dong-medicine-backend && systemctl stop dong-medicine-backend && print_success "后端服务已停止" || print_info "后端服务未运行"

print_step "部署后端应用..."
cp "$SCRIPT_DIR/dong-medicine-backend.jar" "$BACKEND_DIR/"
cp "$SCRIPT_DIR/.env" "$BACKEND_DIR/.env"
[ -d "$SCRIPT_DIR/public" ] && cp -r "$SCRIPT_DIR/public"/* "$BACKEND_DIR/public/" 2>/dev/null || true
print_success "后端文件部署完成"

print_step "部署前端应用..."
rm -rf "$FRONTEND_DIR"/*
cp -r "$SCRIPT_DIR/frontend-dist"/* "$FRONTEND_DIR/"
print_success "前端文件部署完成"

print_step "配置Systemd服务..."
[ -f "$SCRIPT_DIR/dong-medicine-backend.service" ] && cp "$SCRIPT_DIR/dong-medicine-backend.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable dong-medicine-backend
print_success "Systemd服务配置完成"

print_step "配置Nginx..."
[ -f "$SCRIPT_DIR/nginx.conf" ] && cp "$SCRIPT_DIR/nginx.conf" /etc/nginx/conf.d/dong-medicine.conf
[ -f /etc/nginx/nginx.conf ] && ! grep -q "include /etc/nginx/conf.d/dong-medicine.conf" /etc/nginx/nginx.conf && sed -i '/http {/a \    include /etc/nginx/conf.d/dong-medicine.conf;' /etc/nginx/nginx.conf
nginx -t || { print_error "Nginx配置有误"; exit 1; }
print_success "Nginx配置完成"

print_step "启动后端服务..."
systemctl start dong-medicine-backend
print_info "等待后端服务启动..."
sleep 20
systemctl is-active --quiet dong-medicine-backend && print_success "后端服务启动成功" || { print_error "后端服务启动失败"; journalctl -u dong-medicine-backend --no-pager -n 50; exit 1; }

print_step "重启Nginx..."
systemctl restart nginx
systemctl is-active --quiet nginx && print_success "Nginx启动成功" || { print_error "Nginx启动失败"; exit 1; }

print_step "清理旧备份文件(保留最近5个)..."
cd "$BACKUP_DIR" && ls -t dong-medicine-backend-*.jar 2>/dev/null | tail -n +6 | xargs -r rm -f
cd "$BACKUP_DIR" && ls -t frontend-*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm -f
print_success "旧备份清理完成"

print_step "健康检查..."
print_info "等待应用完全启动..."
sleep 10
for i in 1 2 3 4 5; do
    HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
    if [ "$HEALTH_STATUS" = "200" ]; then
        print_success "健康检查通过"
        break
    fi
    if [ $i -lt 5 ]; then
        print_info "健康检查返回 $HEALTH_STATUS，等待重试 ($i/5)..."
        sleep 5
    else
        print_error "健康检查失败 (HTTP $HEALTH_STATUS)"
    fi
done

echo ""
echo "=========================================="
print_success "CI/CD 部署完成！"
echo "=========================================="
echo ""
echo "部署时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "版本标识: $TIMESTAMP"
echo ""

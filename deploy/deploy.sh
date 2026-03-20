#!/bin/bash

set -e

echo "=========================================="
echo "  侗乡医药数字展示平台 - 一键部署脚本"
echo "=========================================="
echo ""

COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_RESET='\033[0m'

print_success() {
    echo -e "${COLOR_GREEN}[✓] $1${COLOR_RESET}"
}

print_error() {
    echo -e "${COLOR_RED}[✗] $1${COLOR_RESET}"
}

print_info() {
    echo -e "${COLOR_YELLOW}[i] $1${COLOR_RESET}"
}

print_step() {
    echo -e "${COLOR_BLUE}==> $1${COLOR_RESET}"
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="/opt/dong-medicine/backend"
FRONTEND_DIR="/opt/dong-medicine/frontend"
LOGS_DIR="/opt/dong-medicine/logs"

if [ "$(id -u)" -ne 0 ]; then
    print_error "请使用root用户执行此脚本"
    exit 1
fi

print_step "检查部署文件..."
if [ ! -f "$SCRIPT_DIR/dong-medicine-backend.jar" ]; then
    print_error "未找到后端JAR文件: $SCRIPT_DIR/dong-medicine-backend.jar"
    print_info "请先将打包好的JAR文件放到deploy目录"
    exit 1
fi

if [ ! -d "$SCRIPT_DIR/frontend-dist" ]; then
    print_error "未找到前端构建目录: $SCRIPT_DIR/frontend-dist"
    print_info "请先将前端构建产物放到deploy/frontend-dist目录"
    exit 1
fi

if [ ! -f "$SCRIPT_DIR/.env.production" ]; then
    print_error "未找到环境配置文件: $SCRIPT_DIR/.env.production"
    print_info "请先配置.env.production文件"
    exit 1
fi

print_step "创建应用目录..."
mkdir -p "$BACKEND_DIR"
mkdir -p "$FRONTEND_DIR"
mkdir -p "$LOGS_DIR"
mkdir -p "$BACKEND_DIR/public"

print_success "目录创建完成"

print_step "部署后端应用..."
cp "$SCRIPT_DIR/dong-medicine-backend.jar" "$BACKEND_DIR/"
cp "$SCRIPT_DIR/.env.production" "$BACKEND_DIR/.env"

if [ -d "$SCRIPT_DIR/public" ]; then
    cp -r "$SCRIPT_DIR/public"/* "$BACKEND_DIR/public/" 2>/dev/null || true
fi

print_success "后端文件部署完成"

print_step "部署前端应用..."
rm -rf "$FRONTEND_DIR"/*
cp -r "$SCRIPT_DIR/frontend-dist"/* "$FRONTEND_DIR/"

print_success "前端文件部署完成"

print_step "配置Systemd服务..."
cp "$SCRIPT_DIR/dong-medicine-backend.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable dong-medicine-backend

print_success "Systemd服务配置完成"

print_step "配置Nginx..."
cp "$SCRIPT_DIR/nginx.conf" /etc/nginx/conf.d/dong-medicine.conf
if [ -f /etc/nginx/nginx.conf ]; then
    if ! grep -q "include /etc/nginx/conf.d/dong-medicine.conf" /etc/nginx/nginx.conf; then
        sed -i '/http {/a \    include /etc/nginx/conf.d/dong-medicine.conf;' /etc/nginx/nginx.conf
    fi
fi

nginx -t
if [ $? -ne 0 ]; then
    print_error "Nginx配置有误，请检查"
    exit 1
fi

print_success "Nginx配置完成"

print_step "启动服务..."
systemctl restart dong-medicine-backend
systemctl restart nginx

sleep 5

if systemctl is-active --quiet dong-medicine-backend; then
    print_success "后端服务启动成功"
else
    print_error "后端服务启动失败"
    journalctl -u dong-medicine-backend --no-pager -n 50
    exit 1
fi

if systemctl is-active --quiet nginx; then
    print_success "Nginx启动成功"
else
    print_error "Nginx启动失败"
    exit 1
fi

echo ""
echo "=========================================="
print_success "部署完成！"
echo "=========================================="
echo ""
echo "访问地址: http://47.112.111.115"
echo "后端API: http://47.112.111.115/api"
echo ""
echo "常用命令："
echo "  查看后端日志: journalctl -u dong-medicine-backend -f"
echo "  重启后端服务: systemctl restart dong-medicine-backend"
echo "  重启Nginx: systemctl restart nginx"
echo "  查看服务状态: systemctl status dong-medicine-backend"
echo ""

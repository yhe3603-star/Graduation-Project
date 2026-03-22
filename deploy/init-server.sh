#!/bin/bash

set -e

echo "=========================================="
echo "  服务器初始化脚本 - Docker 环境"
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

if [ "$(id -u)" -ne 0 ]; then
    print_error "请使用 root 用户执行此脚本"
    exit 1
fi

print_step "更新系统软件包..."
if command -v apt-get &> /dev/null; then
    apt-get update -y
    apt-get upgrade -y
elif command -v yum &> /dev/null; then
    yum update -y
elif command -v dnf &> /dev/null; then
    dnf update -y
fi
print_success "系统软件包更新完成"

print_step "安装必要工具..."
if command -v apt-get &> /dev/null; then
    apt-get install -y curl wget git vim net-tools
elif command -v yum &> /dev/null; then
    yum install -y curl wget git vim net-tools
elif command -v dnf &> /dev/null; then
    dnf install -y curl wget git vim net-tools
fi
print_success "必要工具安装完成"

print_step "检查 Docker..."
if command -v docker &> /dev/null; then
    print_info "Docker 已安装: $(docker --version)"
else
    print_info "正在安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
    print_success "Docker 安装完成: $(docker --version)"
fi

print_step "检查 Docker Compose..."
if docker compose version &> /dev/null; then
    print_info "Docker Compose 已安装: $(docker compose version)"
elif command -v docker-compose &> /dev/null; then
    print_info "Docker Compose 已安装: $(docker-compose --version)"
else
    print_info "正在安装 Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    print_success "Docker Compose 安装完成"
fi

print_step "配置 Docker 镜像加速..."
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF
systemctl restart docker
print_success "Docker 镜像加速配置完成"

print_step "配置防火墙..."
if command -v ufw &> /dev/null; then
    ufw allow 22/tcp
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow 8080/tcp
    ufw --force enable
    print_success "UFW 防火墙配置完成"
elif command -v firewall-cmd &> /dev/null; then
    systemctl start firewalld
    systemctl enable firewalld
    firewall-cmd --permanent --add-port=22/tcp
    firewall-cmd --permanent --add-port=80/tcp
    firewall-cmd --permanent --add-port=443/tcp
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
    print_success "Firewalld 防火墙配置完成"
else
    print_info "未检测到防火墙，跳过配置"
fi

print_step "创建应用目录..."
mkdir -p /opt/dong-medicine/{data,backups,logs}
print_success "应用目录创建完成"

print_step "配置系统参数..."
cat >> /etc/sysctl.conf << 'EOF'

# Docker 优化参数
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535
net.ipv4.ip_local_port_range = 1024 65535
EOF
sysctl -p
print_success "系统参数配置完成"

echo ""
echo "=========================================="
print_success "服务器初始化完成！"
echo "=========================================="
echo ""
echo "已安装组件:"
echo "  - Docker: $(docker --version)"
echo "  - Docker Compose: $(docker compose version 2>/dev/null || docker-compose --version)"
echo ""
echo "开放端口: 22, 80, 443, 8080"
echo ""
echo "下一步: 配置 GitHub Actions Secrets 并推送代码触发部署"
echo ""

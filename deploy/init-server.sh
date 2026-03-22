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

install_docker_ubuntu() {
    print_info "使用阿里云镜像源安装 Docker (Ubuntu/Debian)..."
    
    apt-get remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true
    
    mkdir -p /etc/apt/keyrings
    
    curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu \
      $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    systemctl enable docker
    systemctl start docker
    
    print_success "Docker 安装完成"
}

install_docker_centos() {
    print_info "使用阿里云镜像源安装 Docker (CentOS/RHEL)..."
    
    yum remove -y docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine 2>/dev/null || true
    
    yum install -y yum-utils
    yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
    
    yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    systemctl enable docker
    systemctl start docker
    
    print_success "Docker 安装完成"
}

if [ "$(id -u)" -ne 0 ]; then
    print_error "请使用 root 用户执行此脚本"
    exit 1
fi

print_step "更新系统软件包..."
if command -v apt-get &> /dev/null; then
    apt-get update -y
    apt-get install -y curl wget git vim net-tools ca-certificates gnupg lsb-release
elif command -v yum &> /dev/null; then
    yum update -y
    yum install -y curl wget git vim net-tools ca-certificates yum-utils
elif command -v dnf &> /dev/null; then
    dnf update -y
    dnf install -y curl wget git vim net-tools ca-certificates dnf-utils
fi
print_success "系统软件包更新完成"

print_step "检查 Docker..."
if command -v docker &> /dev/null; then
    print_info "Docker 已安装: $(docker --version)"
else
    print_info "正在安装 Docker (使用阿里云镜像源)..."
    
    if command -v apt-get &> /dev/null; then
        install_docker_ubuntu
    elif command -v yum &> /dev/null || command -v dnf &> /dev/null; then
        install_docker_centos
    else
        print_error "不支持的系统，请手动安装 Docker"
        exit 1
    fi
fi

print_step "检查 Docker Compose..."
if docker compose version &> /dev/null 2>&1; then
    print_info "Docker Compose 已安装: $(docker compose version)"
elif command -v docker-compose &> /dev/null; then
    print_info "Docker Compose 已安装: $(docker-compose --version)"
else
    print_info "正在安装 Docker Compose..."
    curl -L "https://mirror.ghproxy.com/https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose || {
        curl -L "https://ghproxy.com/https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    }
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
    ufw --force enable 2>/dev/null || true
    print_success "UFW 防火墙配置完成"
elif command -v firewall-cmd &> /dev/null; then
    systemctl start firewalld 2>/dev/null || true
    systemctl enable firewalld 2>/dev/null || true
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

echo ""
echo "=========================================="
print_success "服务器初始化完成！"
echo "=========================================="
echo ""
echo "已安装组件:"
echo "  - Docker: $(docker --version)"
echo "  - Docker Compose: $(docker compose version 2>/dev/null || docker-compose --version 2>/dev/null)"
echo ""
echo "开放端口: 22, 80, 443, 8080"
echo ""

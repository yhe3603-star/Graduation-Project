#!/bin/bash

set -e

echo "=========================================="
echo "  侗乡医药数字展示平台 - 服务器环境安装"
echo "=========================================="
echo ""

COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
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

if [ "$(id -u)" -ne 0 ]; then
    print_error "请使用root用户执行此脚本"
    exit 1
fi

print_info "更新系统软件包..."
apt update && apt upgrade -y

print_info "安装基础工具..."
apt install -y wget curl git vim unzip net-tools software-properties-common

echo ""
print_info "========== 安装 JDK 17 =========="
if java -version 2>&1 | grep -q "17"; then
    print_success "JDK 17 已安装"
else
    print_info "正在安装 JDK 17..."
    apt install -y openjdk-17-jdk openjdk-17-jre
    print_success "JDK 17 安装完成"
fi

java -version

echo ""
print_info "========== 安装 MySQL 8.0 =========="
if command -v mysql &> /dev/null; then
    print_success "MySQL 已安装"
else
    print_info "正在安装 MySQL 8.0..."
    
    DEBIAN_FRONTEND=noninteractive apt install -y mysql-server mysql-client
    
    systemctl start mysql
    systemctl enable mysql
    
    print_info "MySQL 安装完成"
    print_info "MySQL root用户默认使用auth_socket认证，无需密码"
    print_info "如需设置密码，请运行: mysql_secure_installation"
fi

mysql --version

echo ""
print_info "========== 安装 Nginx =========="
if command -v nginx &> /dev/null; then
    print_success "Nginx 已安装"
else
    print_info "正在安装 Nginx..."
    apt install -y nginx
    
    systemctl start nginx
    systemctl enable nginx
fi

nginx -v

echo ""
print_info "========== 配置防火墙 (UFW) =========="
if command -v ufw &> /dev/null; then
    ufw allow 22/tcp
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow 8080/tcp
    
    echo "y" | ufw enable
    
    print_success "防火墙配置完成"
else
    print_info "UFW未安装，跳过防火墙配置"
fi

echo ""
print_info "========== 创建应用目录 =========="
mkdir -p /opt/dong-medicine/{backend,frontend,logs,backup}
mkdir -p /opt/dong-medicine/backend/public

print_success "应用目录创建完成: /opt/dong-medicine/"

echo ""
print_info "========== 配置系统参数 =========="
cat >> /etc/sysctl.conf << EOF

# 应用优化参数
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535
net.ipv4.ip_local_port_range = 1024 65535
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30
EOF

sysctl -p

cat >> /etc/security/limits.conf << EOF

# 应用用户限制
* soft nofile 65535
* hard nofile 65535
* soft nproc 65535
* hard nproc 65535
EOF

print_success "系统参数配置完成"

echo ""
print_info "========== 安装 Node.js (可选，用于前端构建) =========="
if command -v node &> /dev/null; then
    print_success "Node.js 已安装: $(node -v)"
else
    print_info "正在安装 Node.js 20..."
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
    apt install -y nodejs
    print_success "Node.js 安装完成: $(node -v)"
fi

echo ""
echo "=========================================="
print_success "环境安装完成！"
echo "=========================================="
echo ""
echo "后续步骤："
echo "1. 配置MySQL（如需设置root密码）："
echo "   mysql"
echo "   ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '你的密码';"
echo "   FLUSH PRIVILEGES;"
echo ""
echo "2. 上传部署文件到 /opt/deploy 目录"
echo ""
echo "3. 运行部署脚本："
echo "   cd /opt/deploy"
echo "   chmod +x deploy.sh"
echo "   ./deploy.sh"
echo ""

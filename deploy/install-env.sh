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
yum update -y

print_info "安装基础工具..."
yum install -y wget curl git vim unzip net-tools

echo ""
print_info "========== 安装 JDK 17 =========="
if java -version 2>&1 | grep -q "17"; then
    print_success "JDK 17 已安装"
else
    print_info "正在安装 JDK 17..."
    yum install -y java-17-openjdk java-17-openjdk-devel
    print_success "JDK 17 安装完成"
fi

java -version

echo ""
print_info "========== 安装 MySQL 8.0 =========="
if command -v mysql &> /dev/null; then
    print_success "MySQL 已安装"
else
    print_info "正在安装 MySQL 8.0..."
    
    rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023
    
    yum localinstall -y https://repo.mysql.com//mysql80-community-release-el7-11.noarch.rpm || \
    yum localinstall -y https://dev.mysql.com/get/mysql80-community-release-el7-11.noarch.rpm
    
    yum install -y mysql-community-server
    
    systemctl start mysqld
    systemctl enable mysqld
    
    TEMP_PASSWORD=$(grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}')
    print_info "MySQL临时密码: $TEMP_PASSWORD"
    print_info "请记录此密码，稍后需要修改"
fi

mysql --version

echo ""
print_info "========== 安装 Nginx =========="
if command -v nginx &> /dev/null; then
    print_success "Nginx 已安装"
else
    print_info "正在安装 Nginx..."
    yum install -y epel-release
    yum install -y nginx
    
    systemctl start nginx
    systemctl enable nginx
fi

nginx -v

echo ""
print_info "========== 配置防火墙 =========="
if command -v firewall-cmd &> /dev/null; then
    systemctl start firewalld
    systemctl enable firewalld
    
    firewall-cmd --permanent --add-port=22/tcp
    firewall-cmd --permanent --add-port=80/tcp
    firewall-cmd --permanent --add-port=443/tcp
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --permanent --add-port=3306/tcp
    
    firewall-cmd --reload
    
    print_success "防火墙配置完成"
else
    print_info "firewalld未安装，跳过防火墙配置"
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
    curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
    yum install -y nodejs
    print_success "Node.js 安装完成: $(node -v)"
fi

echo ""
echo "=========================================="
print_success "环境安装完成！"
echo "=========================================="
echo ""
echo "后续步骤："
echo "1. 修改MySQL root密码："
echo "   mysql -u root -p"
echo "   ALTER USER 'root'@'localhost' IDENTIFIED BY '你的新密码';"
echo ""
echo "2. 运行部署脚本："
echo "   chmod +x deploy.sh"
echo "   ./deploy.sh"
echo ""

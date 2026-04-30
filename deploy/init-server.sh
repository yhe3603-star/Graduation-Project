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

SWAP_SIZE="${SWAP_SIZE:-2G}"
TIMEZONE="${TIMEZONE:-Asia/Shanghai}"

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

configure_swap() {
    print_step "配置 Swap..."
    
    if [ -f /swapfile ]; then
        print_info "Swap 已存在"
        return
    fi
    
    print_info "创建 ${SWAP_SIZE} Swap..."
    
    fallocate -l "$SWAP_SIZE" /swapfile 2>/dev/null || dd if=/dev/zero of=/swapfile bs=1M count=2048 status=progress
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    
    sysctl vm.swappiness=10
    echo 'vm.swappiness=10' >> /etc/sysctl.conf
    
    print_success "Swap 配置完成"
}

configure_timezone() {
    print_step "配置时区..."
    
    timedatectl set-timezone "$TIMEZONE" 2>/dev/null || ln -sf /usr/share/zoneinfo/"$TIMEZONE" /etc/localtime
    
    print_success "时区设置为 $TIMEZONE"
}

harden_ssh() {
    print_step "SSH 安全加固..."
    
    local sshd_config="/etc/ssh/sshd_config"
    local backup="${sshd_config}.bak.$(date +%Y%m%d%H%M%S)"
    
    if [ -f "$sshd_config" ]; then
        cp "$sshd_config" "$backup"
        print_info "SSH 配置已备份到 $backup"
    fi
    
    sed -i 's/#PermitRootLogin.*/PermitRootLogin prohibit-password/' "$sshd_config" 2>/dev/null || true
    sed -i 's/#MaxAuthTries.*/MaxAuthTries 3/' "$sshd_config" 2>/dev/null || true
    sed -i 's/#MaxSessions.*/MaxSessions 5/' "$sshd_config" 2>/dev/null || true
    sed -i 's/#ClientAliveInterval.*/ClientAliveInterval 300/' "$sshd_config" 2>/dev/null || true
    sed -i 's/#ClientAliveCountMax.*/ClientAliveCountMax 2/' "$sshd_config" 2>/dev/null || true
    sed -i 's/#LoginGraceTime.*/LoginGraceTime 60/' "$sshd_config" 2>/dev/null || true
    
    if command -v systemctl &> /dev/null; then
        systemctl restart sshd 2>/dev/null || systemctl restart ssh 2>/dev/null || true
    fi
    
    print_success "SSH 安全加固完成"
}

configure_kernel() {
    print_step "优化内核参数..."
    
    local sysctl_conf="/etc/sysctl.d/99-dong-medicine.conf"
    
    cat > "$sysctl_conf" << 'EOF'
# 网络优化
net.core.somaxconn = 65535
net.core.netdev_max_backlog = 65535
net.ipv4.tcp_max_syn_backlog = 65535
net.ipv4.tcp_fin_timeout = 30
net.ipv4.tcp_keepalive_time = 300
net.ipv4.tcp_keepalive_probes = 3
net.ipv4.tcp_keepalive_intvl = 30
net.ipv4.tcp_tw_reuse = 1
net.ipv4.ip_local_port_range = 1024 65535

# 内存优化
vm.swappiness = 10
vm.overcommit_memory = 1
vm.max_map_count = 262144

# 文件描述符
fs.file-max = 2097152
fs.nr_open = 2097152
EOF
    
    sysctl -p "$sysctl_conf" 2>/dev/null || true
    
    print_success "内核参数优化完成"
}

configure_limits() {
    print_step "配置文件描述符限制..."
    
    cat > /etc/security/limits.d/99-dong-medicine.conf << 'EOF'
* soft nofile 65535
* hard nofile 65535
* soft nproc 65535
* hard nproc 65535
root soft nofile 65535
root hard nofile 65535
EOF
    
    print_success "文件描述符限制配置完成"
}

install_monitoring_tools() {
    print_step "安装监控工具..."
    
    if command -v apt-get &> /dev/null; then
        apt-get install -y htop iotop ncdu net-tools ca-certificates gnupg lsb-release curl wget git vim 2>/dev/null || true
    elif command -v yum &> /dev/null; then
        yum install -y htop iotop ncdu net-tools ca-certificates curl wget git vim 2>/dev/null || true
    elif command -v dnf &> /dev/null; then
        dnf install -y htop iotop ncdu net-tools ca-certificates curl wget git vim 2>/dev/null || true
    fi
    
    print_success "监控工具安装完成"
}

configure_docker() {
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
  },
  "storage-driver": "overlay2",
  "live-restore": true,
  "default-ulimits": {
    "nofile": {
      "Name": "nofile",
      "Hard": 65535,
      "Soft": 65535
    }
  }
}
EOF
    
    if command -v systemctl &> /dev/null; then
        systemctl restart docker
    fi
    
    print_success "Docker 镜像加速配置完成"
}

configure_firewall() {
    print_step "配置防火墙..."
    
    if command -v ufw &> /dev/null; then
        ufw --force reset 2>/dev/null || true
        ufw default deny incoming
        ufw default allow outgoing
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
}

create_app_directories() {
    print_step "创建应用目录..."
    
    mkdir -p /opt/dong-medicine/{data,backups,logs,sql}
    
    print_success "应用目录创建完成"
}

main() {
    if [ "$(id -u)" -ne 0 ]; then
        print_error "请使用 root 用户执行此脚本"
        exit 1
    fi
    
    echo ""
    echo "=========================================="
    echo "  服务器初始化脚本 - Docker 环境"
    echo "  侗乡医药数字展示平台"
    echo "=========================================="
    echo ""
    
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
    
    configure_timezone
    configure_swap
    configure_kernel
    configure_limits
    
    install_monitoring_tools
    
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
    
    configure_docker
    configure_firewall
    harden_ssh
    create_app_directories
    
    echo ""
    echo "=========================================="
    print_success "服务器初始化完成！"
    echo "=========================================="
    echo ""
    echo "已安装组件:"
    echo "  - Docker: $(docker --version)"
    echo "  - Docker Compose: $(docker compose version 2>/dev/null || docker-compose --version 2>/dev/null)"
    echo "  - 时区: $TIMEZONE"
    echo "  - Swap: $SWAP_SIZE"
    echo ""
    echo "开放端口: 22, 80, 443, 8080"
    echo ""
    echo "下一步:"
    echo "  1. 上传项目文件到 /opt/dong-medicine/"
    echo "  2. 创建 .env 配置文件"
    echo "  3. 运行 docker compose up -d 启动服务"
    echo ""
}

main "$@"

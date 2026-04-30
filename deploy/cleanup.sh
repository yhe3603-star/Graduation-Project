#!/bin/bash

set -e

APP_DIR="${APP_DIR:-/opt/dong-medicine}"
LOG_FILE="/var/log/dong-medicine-cleanup.log"
MAX_IDLE_TIME="${MAX_IDLE_TIME:-3600}"
DRY_RUN="${DRY_RUN:-false}"

log() {
    local level=$1
    local message=$2
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message" | tee -a "$LOG_FILE"
}

log_info() { log "INFO" "$1"; }
log_warn() { log "WARN" "$1"; }
log_error() { log "ERROR" "$1"; }

graceful_kill() {
    local pid=$1
    local process_name=$2
    
    if [ -z "$pid" ] || [ "$pid" -eq 0 ]; then
        return
    fi
    
    if ! kill -0 "$pid" 2>/dev/null; then
        return
    fi
    
    log_info "正在终止进程 $pid ($process_name)..."
    
    kill -TERM "$pid" 2>/dev/null || true
    
    local wait_count=0
    while kill -0 "$pid" 2>/dev/null && [ $wait_count -lt 10 ]; do
        sleep 1
        wait_count=$((wait_count + 1))
    done
    
    if kill -0 "$pid" 2>/dev/null; then
        log_warn "进程 $pid 未响应 SIGTERM，使用 SIGKILL"
        kill -9 "$pid" 2>/dev/null || true
    fi
    
    log_info "进程 $pid 已终止"
}

cleanup_ssh_connections() {
    log_info "========== 清理僵尸 SSH 连接 =========="
    
    local current_connections
    current_connections=$(ss -tnp 2>/dev/null | grep ':22' | grep -c ESTAB || echo "0")
    log_info "当前活跃 SSH 连接数: $current_connections"
    
    local zombie_pids
    zombie_pids=$(ss -tnp 2>/dev/null | grep ':22' | grep -v ESTAB | awk '{print $6}' | cut -d',' -f2 | cut -d'=' -f2 | sort -u)
    
    if [ -n "$zombie_pids" ]; then
        for pid in $zombie_pids; do
            if [ -n "$pid" ] && [ "$pid" -gt 1 ]; then
                if [ "$DRY_RUN" = "true" ]; then
                    log_info "[DRY-RUN] 将终止僵尸连接: PID=$pid"
                else
                    graceful_kill "$pid" "僵尸SSH连接"
                fi
            fi
        done
    else
        log_info "没有发现僵尸 SSH 连接"
    fi
    
    log_info "检查长时间空闲连接 (超过 ${MAX_IDLE_TIME} 秒)..."
    
    local all_pids
    all_pids=$(ss -tnp 2>/dev/null | grep ':22' | awk '{print $6}' | cut -d',' -f2 | cut -d'=' -f2 | sort -u)
    
    for pid in $all_pids; do
        if [ -z "$pid" ] || [ "$pid" -le 1 ]; then
            continue
        fi
        
        local elapsed
        elapsed=$(ps -o etimes= -p "$pid" 2>/dev/null | tr -d ' ' || echo "0")
        
        if [ -n "$elapsed" ] && [ "$elapsed" -gt "$MAX_IDLE_TIME" ]; then
            if [ "$DRY_RUN" = "true" ]; then
                log_info "[DRY-RUN] 将终止长时间连接: PID=$pid, 运行时间=${elapsed}秒"
            else
                log_info "终止长时间连接: PID=$pid, 运行时间=${elapsed}秒"
                graceful_kill "$pid" "长时间SSH连接"
            fi
        fi
    done
    
    local final_connections
    final_connections=$(ss -tnp 2>/dev/null | grep ':22' | grep -c ESTAB || echo "0")
    log_info "清理后 SSH 连接数: $final_connections"
}

cleanup_docker_resources() {
    log_info "========== 清理 Docker 资源 =========="
    
    cd "$APP_DIR" 2>/dev/null || {
        log_warn "应用目录 $APP_DIR 不存在，跳过 Docker 清理"
        return
    }
    
    log_info "停止容器..."
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY-RUN] 将执行: docker compose down --timeout 10"
    else
        docker compose down --timeout 10 2>/dev/null || true
    fi
    
    log_info "清理悬空资源..."
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY-RUN] 将执行: docker system prune -f"
    else
        docker system prune -f 2>/dev/null || true
    fi
    
    log_info "清理旧镜像 (超过 7 天未使用)..."
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY-RUN] 将执行: docker image prune -a -f --filter 'until=168h'"
    else
        docker image prune -a -f --filter "until=168h" 2>/dev/null || true
    fi
    
    log_info "Docker 磁盘使用情况:"
    docker system df 2>/dev/null | tee -a "$LOG_FILE" || true
}

main() {
    mkdir -p "$(dirname "$LOG_FILE")" 2>/dev/null || true
    touch "$LOG_FILE" 2>/dev/null || LOG_FILE="/tmp/dong-medicine-cleanup.log"
    
    log_info "=========================================="
    log_info "  侗乡医药平台清理脚本"
    log_info "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
    log_info "  DRY_RUN: $DRY_RUN"
    log_info "=========================================="
    
    cleanup_ssh_connections
    cleanup_docker_resources
    
    log_info "=========================================="
    log_info "清理完成"
    log_info "=========================================="
}

main "$@"

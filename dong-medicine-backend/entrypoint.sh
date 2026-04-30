#!/bin/sh

set -e

LOG_PREFIX="[DongMedicine]"

log_info() { echo "$LOG_PREFIX [INFO] $1"; }
log_warn() { echo "$LOG_PREFIX [WARN] $1"; }
log_error() { echo "$LOG_PREFIX [ERROR] $1" >&2; }

cleanup() {
    log_info "收到终止信号，正在关闭应用..."
    if [ -n "$JAVA_PID" ] && kill -0 "$JAVA_PID" 2>/dev/null; then
        kill -TERM "$JAVA_PID"
        wait "$JAVA_PID" 2>/dev/null || true
    fi
    log_info "应用已关闭"
    exit 0
}

trap cleanup TERM INT

echo "=========================================="
echo "  侗乡医药后端服务启动"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

log_info "环境: DB=${DB_HOST}:${DB_PORT:-3306}/${DB_NAME:-dong_medicine}"

wait_for_mysql() {
    local max_retries=${1:-30}
    local retry=0
    
    log_info "等待 MySQL..."
    
    while [ $retry -lt $max_retries ]; do
        if mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" --skip-ssl -e "SELECT 1" 2>/dev/null; then
            log_info "MySQL 就绪"
            return 0
        fi
        retry=$((retry + 1))
        sleep 1
    done
    
    log_error "MySQL 连接失败"
    return 1
}

wait_for_service() {
    local host=$1 port=$2 name=$3
    local max_retries=${4:-30}
    local retry=0
    
    while [ $retry -lt $max_retries ]; do
        if nc -z "$host" "$port" 2>/dev/null; then
            log_info "$name 就绪"
            return 0
        fi
        retry=$((retry + 1))
        sleep 1
    done
    log_warn "$name 未就绪"
    return 1
}

init_database() {
    local table_count
    table_count=$(mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" --skip-ssl "${DB_NAME:-dong_medicine}" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME:-dong_medicine}'" -s -N 2>/dev/null || echo "0")
    
    if [ "$table_count" = "0" ] && [ -f /app/init.sql ]; then
        log_info "初始化数据库..."
        mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" --skip-ssl "${DB_NAME:-dong_medicine}" < /app/init.sql 2>&1 && log_info "初始化完成" || log_warn "初始化失败"
    else
        log_info "数据库已存在 ($table_count 表)"
    fi
}

wait_for_mysql || exit 1
init_database

[ -n "$REDIS_HOST" ] && wait_for_service "$REDIS_HOST" "${REDIS_PORT:-6379}" "Redis" 30
if [ "${APP_RABBITMQ_ENABLED:-true}" = "true" ] && [ -n "$RABBITMQ_HOST" ]; then
    wait_for_service "$RABBITMQ_HOST" "${RABBITMQ_PORT:-5672}" "RabbitMQ" 60
else
    log_info "RabbitMQ 已禁用，跳过等待"
fi

echo "=========================================="
log_info "启动应用..."
echo "=========================================="

exec java $JAVA_OPTS -jar app.jar

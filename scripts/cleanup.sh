#!/bin/bash
# 清理僵尸 SSH 连接和 Docker 资源
# 用法: sudo /opt/dong-medicine/cleanup.sh

echo "========== 清理僵尸 SSH 连接 =========="

# 查看当前 SSH 连接数
echo "当前 SSH 连接:"
ss -tnp | grep ':22' | wc -l

# 清理非 ESTABLISHED 状态的连接
echo "清理非 ESTABLISHED 状态的连接..."
ss -tnp | grep ':22' | grep -v ESTAB | awk '{print $6}' | cut -d',' -f2 | cut -d'=' -f2 | xargs -I {} kill -9 {} 2>/dev/null || true

# 清理超过 1 小时的空闲连接
echo "清理长时间空闲的连接..."
for pid in $(ss -tnp | grep ':22' | awk '{print $6}' | cut -d',' -f2 | cut -d'=' -f2); do
    if [ -n "$pid" ]; then
        elapsed=$(ps -o etimes= -p $pid 2>/dev/null | tr -d ' ')
        if [ -n "$elapsed" ] && [ "$elapsed" -gt 3600 ]; then
            echo "终止长时间连接: PID=$pid, 运行时间=${elapsed}秒"
            kill -9 $pid 2>/dev/null || true
        fi
    fi
done

echo "========== 清理 Docker 资源 =========="
cd /opt/dong-medicine 2>/dev/null || exit 0

# 停止所有容器
docker compose down --timeout 10 2>/dev/null || true

# 清理悬空资源
docker system prune -f 2>/dev/null || true

echo "========== 完成 =========="
echo "当前 SSH 连接数: $(ss -tnp | grep ':22' | wc -l)"

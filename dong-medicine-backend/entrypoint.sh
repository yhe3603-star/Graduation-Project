#!/bin/bash

echo "========== 侗乡医药后端启动 =========="
date
echo "DB_HOST=${DB_HOST} DB_PORT=${DB_PORT:-3306} DB_NAME=${DB_NAME:-dong_medicine}"
echo "REDIS_HOST=${REDIS_HOST} REDIS_PORT=${REDIS_PORT:-6379}"
echo "RABBITMQ_HOST=${RABBITMQ_HOST} RABBITMQ_PORT=${RABBITMQ_PORT:-5672}"

echo "========== 等待MySQL =========="
MYSQL_READY=false
for i in $(seq 1 60); do
  if mysqlcheck --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" "${DB_NAME:-dong_medicine}" --silent 2>/dev/null; then
    echo "MySQL就绪!"
    MYSQL_READY=true
    break
  fi
  echo "等待MySQL... ($i/60)"
  sleep 2
done

if [ "$MYSQL_READY" = "false" ]; then
  echo "WARNING: MySQL未就绪, 继续启动应用..."
fi

echo "========== 检查数据库初始化 =========="
TABLE_COUNT=$(mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" "${DB_NAME:-dong_medicine}" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME:-dong_medicine}'" -s -N 2>/dev/null || echo "0")

if [ "$TABLE_COUNT" = "0" ] || [ -z "$TABLE_COUNT" ]; then
  echo "初始化数据库..."
  if mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" "${DB_NAME:-dong_medicine}" < /app/init.sql 2>/dev/null; then
    echo "数据库初始化成功!"
  else
    echo "WARNING: 数据库初始化失败, 继续启动应用..."
  fi
else
  echo "数据库已有 $TABLE_COUNT 张表, 跳过初始化"
fi

echo "========== 启动Spring Boot应用 =========="
exec java $JAVA_OPTS -jar app.jar

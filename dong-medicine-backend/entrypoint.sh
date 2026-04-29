#!/bin/bash
set -e

echo "Waiting for MySQL..."
MYSQL_READY=false
for i in $(seq 1 60); do
  if mysqlcheck --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" "${DB_NAME:-dong_medicine}" --silent 2>/dev/null; then
    echo "MySQL is ready!"
    MYSQL_READY=true
    break
  fi
  echo "Waiting for MySQL... ($i/60)"
  sleep 2
done

if [ "$MYSQL_READY" = "false" ]; then
  echo "ERROR: MySQL is not ready after 120 seconds, aborting."
  exit 1
fi

echo "Checking if database needs initialization..."
TABLE_COUNT=$(mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" "${DB_NAME:-dong_medicine}" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME:-dong_medicine}'" -s -N 2>/dev/null || echo "0")

if [ "$TABLE_COUNT" = "0" ] || [ "$TABLE_COUNT" = "0\n" ]; then
  echo "Initializing database..."
  if mysql --host="${DB_HOST}" --port="${DB_PORT:-3306}" --user="${DB_USERNAME:-root}" --password="${DB_PASSWORD}" "${DB_NAME:-dong_medicine}" < /app/init.sql 2>/dev/null; then
    echo "Database initialized successfully!"
  else
    echo "ERROR: Database initialization failed, aborting."
    exit 1
  fi
else
  echo "Database already has $TABLE_COUNT tables, skipping initialization."
fi

echo "Waiting for RabbitMQ..."
RABBITMQ_READY=false
for i in $(seq 1 30); do
  if curl -sf "http://${RABBITMQ_HOST:-localhost}:15672" >/dev/null 2>&1; then
    echo "RabbitMQ is ready!"
    RABBITMQ_READY=true
    break
  fi
  echo "Waiting for RabbitMQ... ($i/30)"
  sleep 2
done

if [ "$RABBITMQ_READY" = "false" ]; then
  echo "WARNING: RabbitMQ management interface not reachable, continuing anyway..."
fi

echo "Starting application..."
exec java $JAVA_OPTS -jar app.jar

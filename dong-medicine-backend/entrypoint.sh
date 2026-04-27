#!/bin/bash

echo "Waiting for MySQL..."
for i in $(seq 1 60); do
  if mysqlcheck --host=${DB_HOST} --port=${DB_PORT:-3306} --user=${DB_USERNAME:-root} --password=${DB_PASSWORD} ${DB_NAME:-dong_medicine} --silent 2>/dev/null; then
    echo "MySQL is ready!"
    break
  fi
  echo "Waiting for MySQL... ($i/60)"
  sleep 2
done

echo "Checking if database needs initialization..."
TABLE_COUNT=$(mysql --host=${DB_HOST} --port=${DB_PORT:-3306} --user=${DB_USERNAME:-root} --password=${DB_PASSWORD} ${DB_NAME:-dong_medicine} -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME:-dong_medicine}'" -s -N 2>/dev/null || echo "0")

if [ "$TABLE_COUNT" = "0" ] || [ "$TABLE_COUNT" = "0\n" ]; then
  echo "Initializing database..."
  mysql --host=${DB_HOST} --port=${DB_PORT:-3306} --user=${DB_USERNAME:-root} --password=${DB_PASSWORD} ${DB_NAME:-dong_medicine} < /app/init.sql 2>/dev/null
  if [ $? -eq 0 ]; then
    echo "Database initialized successfully!"
  else
    echo "Database initialization failed, continuing anyway..."
  fi
else
  echo "Database already has $TABLE_COUNT tables, skipping initialization."
fi

echo "Starting application..."
exec java $JAVA_OPTS -jar app.jar

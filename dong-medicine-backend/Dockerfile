FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl default-mysql-client && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY target/*.jar app.jar
COPY sql/dong_medicine.sql /app/init.sql
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh && \
    mkdir -p /app/public /app/logs && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=300s --retries=10 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT ["/app/entrypoint.sh"]

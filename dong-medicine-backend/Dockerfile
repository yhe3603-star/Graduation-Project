FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl default-mysql-client && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=builder /app/target/*.jar app.jar
COPY sql/dong_medicine.sql /app/init.sql
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh && \
    mkdir -p /app/public /app/logs && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --start-period=120s --retries=20 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xms384m -Xmx768m -XX:+UseG1GC -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT ["/app/entrypoint.sh"]

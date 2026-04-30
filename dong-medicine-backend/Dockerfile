FROM maven:3.9-eclipse-temurin-17 AS deps

WORKDIR /app

RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"> \
          <mirrors> \
            <mirror> \
              <id>aliyun</id> \
              <mirrorOf>central</mirrorOf> \
              <name>Aliyun Maven Mirror</name> \
              <url>https://maven.aliyun.com/repository/public</url> \
            </mirror> \
          </mirrors> \
        </settings>' > /root/.m2/settings.xml

COPY pom.xml .

RUN mvn dependency:go-offline -B --no-transfer-progress -q

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B --no-transfer-progress -q -T 2C

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof -Djava.security.egd=file:/dev/./urandom"

RUN apk add --no-cache curl mysql-client netcat-openbsd tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    apk del tzdata && \
    addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar
COPY sql/dong_medicine.sql /app/init.sql
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh && \
    mkdir -p /app/public /app/logs && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

LABEL org.opencontainers.image.title="Dong Medicine Backend" \
      org.opencontainers.image.description="侗乡医药数字展示平台后端服务"

ENTRYPOINT ["/app/entrypoint.sh"]

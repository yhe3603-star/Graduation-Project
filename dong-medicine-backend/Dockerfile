# 多阶段构建

# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 \
          http://maven.apache.org/xsd/settings-1.0.0.xsd"> \
          <mirrors> \
            <mirror> \
              <id>aliyun</id> \
              <name>Aliyun Maven</name> \
              <url>https://maven.aliyun.com/repository/public</url> \
              <mirrorOf>central</mirrorOf> \
            </mirror> \
          </mirrors> \
          <profiles> \
            <profile> \
              <id>timeout-profile</id> \
              <properties> \
                <maven.wagon.http.connectionTimeout>120000</maven.wagon.http.connectionTimeout> \
                <maven.wagon.http.readTimeout>300000</maven.wagon.http.readTimeout> \
              </properties> \
            </profile> \
          </profiles> \
          <activeProfiles> \
            <activeProfile>timeout-profile</activeProfile> \
          </activeProfiles> \
        </settings>' > /root/.m2/settings.xml

COPY pom.xml .

RUN mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.http.connectionTimeout=120000 \
    -Dmaven.wagon.http.readTimeout=300000 \
    --fail-at-end || \
    (echo "Retrying dependency download..." && sleep 10 && \
     mvn dependency:go-offline -B \
     -Dmaven.wagon.http.retryHandler.count=5 \
     -Dmaven.wagon.http.connectionTimeout=120000 \
     -Dmaven.wagon.http.readTimeout=300000) || \
    echo "Some dependencies may be missing, will retry during build"

COPY src ./src

ENV MAVEN_OPTS="-Xmx768m -XX:+UseG1GC"

RUN mvn clean package -Dmaven.test.skip=true -B -T 1C \
    -Dmaven.wagon.http.connectionTimeout=120000 \
    -Dmaven.wagon.http.readTimeout=300000 \
    -Dmaven.compile.fork=false \
    --fail-at-end || \
    (echo "Build failed, retrying..." && \
     mvn clean package -Dmaven.test.skip=true -B \
     -Dmaven.wagon.http.connectionTimeout=120000 \
     -Dmaven.wagon.http.readTimeout=300000)

# 运行阶段
FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

RUN mkdir -p /app/public && chown -R appuser:appgroup /app

COPY --from=builder /build/target/*.jar app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=10s --start-period=180s --retries=10 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

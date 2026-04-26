# 多阶段构建

# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 配置Maven国内镜像源
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
        </settings>' > /root/.m2/settings.xml

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.http.retryHandler.retryIntervalSeconds=5 || true

# 复制源代码并构建（完全跳过测试编译和执行，限制内存）
COPY src ./src
ENV MAVEN_OPTS="-Xmx512m"
RUN mvn clean package -Dmaven.test.skip=true -B -T 1C

# 运行阶段
FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai

# 安装 curl 用于健康检查
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

RUN mkdir -p /app/public && chown -R appuser:appgroup /app

# 从构建阶段复制jar文件
COPY --from=builder /build/target/*.jar app.jar

# 切换到非root用户运行
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=15s --timeout=10s --start-period=180s --retries=10 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

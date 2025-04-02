FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Plato Team <plato@example.com>"

WORKDIR /app

# 添加非root用户
RUN addgroup -S plato && adduser -S plato -G plato

# 创建目录结构并设置权限
RUN mkdir -p /app/logs /app/config && \
    chown -R plato:plato /app

# 时区设置
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo ${TZ} > /etc/timezone

# 复制JAR文件和配置
COPY --chown=plato:plato target/*.jar /app/app.jar
COPY --chown=plato:plato src/main/resources/application.yml /app/config/

# 设置应用参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/"
ENV SPRING_PROFILES_ACTIVE="prod"

# 切换到非root用户
USER plato

# 暴露端口
EXPOSE 8080

# 应用启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.config.location=file:/app/config/application.yml"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1 
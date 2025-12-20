# 1. 基础镜像（轻量、兼容 javax.* 包）
FROM openjdk:8-jdk-alpine

# 2. 维护者&元数据（标准化）
LABEL maintainer="hjw.dev" \
      version="1.0.0" \
      description="团购平台Java应用镜像（基于Spring Boot）"

# 3. 设置时区 & 安装必要工具 (合并RUN指令减少层数)
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 4. 创建非Root用户（提升安全性，避免容器以Root运行）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 5. 声明临时目录卷（Spring Boot 默认临时目录）
VOLUME /tmp

# 6. 显式指定工作目录（隔离应用文件）
WORKDIR /app

# 复制CI构建好的Jar包和生产配置文件到镜像内
COPY app.jar /app/
COPY config/application-prod.yml /app/config/
# 暴露应用端口
EXPOSE 8080

# 9. JVM 参数配置
ENV JAVA_OPTS="-Xms512m -Xmx512m \
               -XX:+UseG1GC \
               -XX:MaxMetaspaceSize=128m \
               -Dfile.encoding=UTF-8 \
               -Djava.security.egd=file:/dev/./urandom"
# 注释说明
# -Xms/-Xmx：初始/最大堆内存，适配小内存服务器
# -XX:+UseG1GC：G1垃圾收集器（Java8u20+默认，低停顿）
# -XX:MaxMetaspaceSize：限制元空间大小，避免溢出
# -Dfile.encoding：解决中文乱码
# -Djava.security.egd：优化随机数生成，避免JVM启动阻塞

# 10. 创建日志目录及归档子目录，并授权给 appuser（解决权限问题）
RUN mkdir -p /app/logs /app/logs/archive && \
    chown -R appuser:appgroup /app/logs


# 11. 切换非Root用户（生产环境必做）
USER appuser


# 12. 启动命令（相对路径，简洁且符合WORKDIR规范）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
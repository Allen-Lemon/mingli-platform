# 玄枢 · 后端（Spring Boot）镜像 —— 多阶段构建
# 构建阶段用 Maven 编译，运行阶段仅保留 JRE + jar。

# ---- 1) 构建阶段 ----
FROM maven:3.9-eclipse-temurin-8 AS builder
WORKDIR /build
# 先拷依赖描述再拷源码，可利用 Docker 层缓存
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline || true
COPY . .
RUN mvn -B -q -DskipTests clean package

# ---- 2) 运行阶段 ----
FROM eclipse-temurin:8-jre
WORKDIR /app
# 拷贝编译产物（jar 名以 * 通配，避免与 pom 中 finalName 耦合）
COPY --from=builder /build/target/*.jar /app/app.jar
EXPOSE 8080
# 限制堆内存：容器内存紧张时 JVM 默认会申请过大堆导致启动失败（已在本地复现）
ENTRYPOINT ["java", "-Xmx512m", "-Xms128m", "-jar", "/app/app.jar"]

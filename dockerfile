# 使用官方的maven镜像作为基础镜像
FROM maven:3.8.1-openjdk-8-slim AS build

# 设置工作目录
WORKDIR /opt/software/run/hifini-auto-check-in

# 复制pom.xml文件到工作目录
COPY pom.xml .

# 下载项目依赖
RUN mvn dependency:go-offline -B

# 复制源代码到工作目录
COPY src ./src

# 编译项目
RUN mvn package

# 使用官方的openjdk镜像作为基础镜像
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /opt/software/run/hifini-auto-check-in

# 从build阶段复制编译好的jar包到工作目录
COPY --from=build /opt/software/run/hifini-auto-check-in/target/*.jar ./hifini-auto-check-in.jar

# 设置启动命令
CMD ["java", "-jar", "hifini-auto-check-in.jar"]
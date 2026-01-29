# 构建阶段
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -q

# 运行阶段
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/*-shaded.jar app.jar
CMD ["java", "-jar", "app.jar"]

# Dùng image có sẵn Maven + JDK để build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Image chạy thực tế, chỉ chứa JDK (nhỏ hơn)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/finance-management-0.0.1-SNAPSHOT.jar app.jar

# Chạy ứng dụng
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

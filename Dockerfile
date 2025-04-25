# Stage 1: Build WAR using Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Deploy to Tomcat
FROM tomcat:9.0-jdk17
COPY --from=builder /app/target/EventManagementSystemV2.war /usr/local/tomcat/webapps/
EXPOSE 8080
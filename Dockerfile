# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ---------- runtime stage ----------
FROM openjdk:21-jdk-slim

# Create user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Copy single JAR with correct ownership
COPY --from=build --chown=spring:spring \
    /app/target/url-shortener-*.jar /app.jar

USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
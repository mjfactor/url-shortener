# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy *all* sources in one COPY
COPY . .

# Make wrapper executable and build
RUN chmod +x ./mvnw && \
    ./mvnw -B clean package -DskipTests

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
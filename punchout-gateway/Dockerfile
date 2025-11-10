# Multi-stage Dockerfile for Gateway Service

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom and common module first (for layer caching)
COPY pom.xml ./
COPY punchout-common ./punchout-common
COPY punchout-gateway ./punchout-gateway

# Build the application
RUN mvn clean package -pl punchout-gateway -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/punchout-gateway/target/punchout-gateway-*.jar app.jar

# Expose port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Set active profile via environment variable (default: local)
ENV SPRING_PROFILES_ACTIVE=local

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

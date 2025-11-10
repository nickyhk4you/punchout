# Multi-stage Dockerfile for UI Backend Service

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom and dependencies first (for layer caching)
COPY pom.xml ./
COPY punchout-common ./punchout-common
COPY punchout-order ./punchout-order
COPY punchout-invoice ./punchout-invoice
COPY punchout-ui-backend ./punchout-ui-backend

# Build the application
RUN mvn clean package -pl punchout-ui-backend -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/punchout-ui-backend/target/punchout-ui-backend-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set active profile via environment variable (default: local)
ENV SPRING_PROFILES_ACTIVE=local

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

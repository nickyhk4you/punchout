# Multi-stage build for Punchout Gateway
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy everything needed for build
COPY pom.xml ./
COPY punchout-common ./punchout-common
COPY punchout-gateway ./punchout-gateway

# Build the application
RUN mvn -f punchout-gateway/pom.xml clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre

# Create non-root user
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app

# Copy JAR from build stage
COPY --from=build --chown=app:app /build/punchout-gateway/target/*.jar app.jar

# Switch to non-root user
USER app

# JVM configuration for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Expose port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health/liveness || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

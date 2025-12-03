# Multi-stage build for Punchout Gateway + UI Backend
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy parent POM and all modules
COPY pom.xml ./
COPY punchout-common ./punchout-common
COPY punchout-gateway ./punchout-gateway
COPY punchout-ui-backend ./punchout-ui-backend
COPY punchout-mock-service ./punchout-mock-service

# Build all modules
RUN mvn clean package -DskipTests -B -pl punchout-common,punchout-gateway,punchout-ui-backend

# Runtime stage
FROM eclipse-temurin:17-jre

# Install supervisor to manage multiple processes
RUN apt-get update && apt-get install -y supervisor wget && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app

# Copy JARs from build stage
COPY --from=build /build/punchout-gateway/target/*.jar gateway.jar
COPY --from=build /build/punchout-ui-backend/target/*.jar ui-backend.jar

# Copy supervisor configuration
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# JVM configuration for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=40 -XX:InitialRAMPercentage=25 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Expose both ports
EXPOSE 9090 8080

# Health check on gateway
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Run supervisor
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]

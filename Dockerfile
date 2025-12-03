# Multi-stage build for Punchout Gateway + UI Backend
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy parent POM and all modules
COPY pom.xml ./
COPY punchout-common ./punchout-common
COPY punchout-gateway ./punchout-gateway
COPY punchout-ui-backend ./punchout-ui-backend
COPY punchout-mock-service ./punchout-mock-service
COPY punchout-order ./punchout-order
COPY punchout-invoice ./punchout-invoice

# Build all modules (order and invoice are dependencies of ui-backend)
RUN mvn clean package -DskipTests -B -pl punchout-common,punchout-order,punchout-invoice,punchout-gateway,punchout-ui-backend

# Runtime stage
FROM eclipse-temurin:17-jre

# Install supervisor, nginx, and wget
RUN apt-get update && apt-get install -y supervisor nginx wget && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JARs from build stage
COPY --from=build /build/punchout-gateway/target/*.jar gateway.jar
COPY --from=build /build/punchout-ui-backend/target/*.jar ui-backend.jar

# Copy configuration files
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY nginx.conf /etc/nginx/nginx.conf

# JVM configuration for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=35 -XX:InitialRAMPercentage=20 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Expose nginx port (single entry point)
EXPOSE 80

# Health check via nginx
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/health || exit 1

# Run supervisor
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]

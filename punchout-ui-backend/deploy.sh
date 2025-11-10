#!/bin/bash

# Deployment script for PunchOut UI Backend Service

set -e

ENVIRONMENT=$1

if [ -z "$ENVIRONMENT" ]; then
    echo "❌ Usage: ./deploy.sh <environment>"
    echo ""
    echo "Available environments:"
    echo "  local    - Local development"
    echo "  dev      - Development"
    echo "  stage    - Staging"
    echo "  preprod  - Pre-Production"
    echo "  s4-dev   - S4 Development"
    echo "  prod     - Production"
    echo ""
    exit 1
fi

# Validate environment
case "$ENVIRONMENT" in
    local|dev|stage|preprod|s4-dev|prod)
        echo "✅ Deploying UI Backend to: $ENVIRONMENT"
        ;;
    *)
        echo "❌ Invalid environment: $ENVIRONMENT"
        exit 1
        ;;
esac

echo ""
echo "🔨 Building UI Backend Service..."
mvn clean package -DskipTests

echo ""
echo "✅ Build completed successfully!"
echo ""
echo "📊 Build Info:"
echo "  Environment: $ENVIRONMENT"
echo "  JAR: target/punchout-ui-backend-1.0.0.jar"
echo "  Profile: $ENVIRONMENT"
echo ""
echo "🚀 To run the application:"
echo "  java -jar -Dspring.profiles.active=$ENVIRONMENT target/punchout-ui-backend-1.0.0.jar"
echo ""
echo "🐳 Or using Docker:"
echo "  docker build -t punchout-ui-backend:$ENVIRONMENT ."
echo "  docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=$ENVIRONMENT punchout-ui-backend:$ENVIRONMENT"
echo ""
echo "📝 Deployment checklist:"
echo "  □ Verify application-$ENVIRONMENT.yml exists"
echo "  □ Check MongoDB connection string"
echo "  □ Check PostgreSQL connection (if using)"
echo "  □ Verify database credentials"
echo "  □ Test health endpoint: /actuator/health"
echo "  □ Check logs after startup"
echo ""

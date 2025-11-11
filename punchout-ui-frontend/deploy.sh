#!/bin/bash

# Deployment script for PunchOut UI Frontend

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
        echo "✅ Deploying to: $ENVIRONMENT"
        ;;
    *)
        echo "❌ Invalid environment: $ENVIRONMENT"
        exit 1
        ;;
esac

# Install dependencies
echo ""
echo "📦 Installing dependencies..."
npm install

# Build for specific environment
echo ""
echo "🔨 Building for $ENVIRONMENT environment..."
if [ "$ENVIRONMENT" = "local" ]; then
    npm run build
elif [ "$ENVIRONMENT" = "prod" ]; then
    npm run build:prod
else
    npm run build:$ENVIRONMENT
fi

echo ""
echo "✅ Build completed successfully!"
echo ""
echo "📊 Build Info:"
echo "  Environment: $ENVIRONMENT"
echo "  Build Output: .next/"
echo "  Standalone: .next/standalone/"
echo ""
echo "🚀 To start the application:"
if [ "$ENVIRONMENT" = "local" ]; then
    echo "  npm run start"
else
    echo "  npm run start:$ENVIRONMENT"
fi
echo ""
echo "📝 Deployment checklist:"
echo "  □ Verify .env.$ENVIRONMENT file exists"
echo "  □ Check API_URL points to correct backend"
echo "  □ Test health endpoints"
echo "  □ Verify CORS configuration"
echo "  □ Check MongoDB connectivity"
echo ""

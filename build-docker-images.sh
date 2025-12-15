#!/bin/bash

###############################################
# Build Docker Images for Punchout Platform
###############################################

set -e

# Configuration
VERSION="${VERSION:-1.0.0}"
REGISTRY="${REGISTRY:-localhost:5000}"
TAG="${TAG:-$VERSION}"

echo "========================================="
echo "Building Docker Images"
echo "========================================="
echo "Version: $VERSION"
echo "Registry: $REGISTRY"
echo "Tag: $TAG"
echo "========================================="
echo ""

# Build Gateway
echo "Building punchout-gateway..."
docker build \
  -t $REGISTRY/punchout-gateway:$TAG \
  -t $REGISTRY/punchout-gateway:latest \
  -f punchout-gateway/Dockerfile \
  .
echo "✓ punchout-gateway built successfully"
echo ""

# Build UI Backend
echo "Building punchout-ui-backend..."
docker build \
  -t $REGISTRY/punchout-ui-backend:$TAG \
  -t $REGISTRY/punchout-ui-backend:latest \
  -f punchout-ui-backend/Dockerfile \
  .
echo "✓ punchout-ui-backend built successfully"
echo ""

echo "========================================="
echo "Build Complete!"
echo "========================================="
echo ""
echo "Images built:"
echo "  - $REGISTRY/punchout-gateway:$TAG"
echo "  - $REGISTRY/punchout-ui-backend:$TAG"
echo ""
echo "To push to registry:"
echo "  docker push $REGISTRY/punchout-gateway:$TAG"
echo "  docker push $REGISTRY/punchout-ui-backend:$TAG"
echo ""
echo "To run locally with docker-compose:"
echo "  TAG=$TAG docker-compose up"

#!/bin/bash

###############################################
# Deploy Punchout Platform to Local Docker
# Simulates production deployment on macOS
###############################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
REGISTRY="local"
VERSION="1.0.0"
ENVIRONMENT="prod"
NETWORK_NAME="punchout-prod-network"
MONGO_PASSWORD="${MONGO_PASSWORD:-secure-password-123}"

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Punchout Platform - Local Docker Deployment${NC}"
echo -e "${BLUE}=========================================${NC}"
echo -e "Version: ${GREEN}$VERSION${NC}"
echo -e "Environment: ${GREEN}$ENVIRONMENT${NC}"
echo -e "Network: ${GREEN}$NETWORK_NAME${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Step 1: Clean up existing containers
echo -e "${YELLOW}Step 1: Cleaning up existing containers...${NC}"
docker-compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker network create $NETWORK_NAME 2>/dev/null || echo "Network already exists"
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Step 2: Build Docker images
echo -e "${YELLOW}Step 2: Building Docker images...${NC}"
echo ""

echo "Building punchout-gateway..."
docker build \
  --quiet \
  -t $REGISTRY/punchout-gateway:$VERSION \
  -t $REGISTRY/punchout-gateway:latest \
  -f punchout-gateway/Dockerfile \
  . > /dev/null
echo -e "${GREEN}✓ Gateway image built${NC}"

echo "Building punchout-ui-backend..."
docker build \
  --quiet \
  -t $REGISTRY/punchout-ui-backend:$VERSION \
  -t $REGISTRY/punchout-ui-backend:latest \
  -f punchout-ui-backend/Dockerfile \
  . > /dev/null
echo -e "${GREEN}✓ UI Backend image built${NC}"

echo "Building punchout-ui-frontend..."
docker build \
  --quiet \
  -t $REGISTRY/punchout-ui-frontend:$VERSION \
  -t $REGISTRY/punchout-ui-frontend:latest \
  -f punchout-ui-frontend/Dockerfile \
  ./punchout-ui-frontend > /dev/null
echo -e "${GREEN}✓ Frontend image built${NC}"
echo ""

# Step 3: Start MongoDB
echo -e "${YELLOW}Step 3: Starting MongoDB...${NC}"
docker run -d \
  --name punchout-mongodb \
  --network $NETWORK_NAME \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=$MONGO_PASSWORD \
  -e MONGO_INITDB_DATABASE=punchout \
  -v punchout-mongodb-data:/data/db \
  --health-cmd="mongosh --eval 'db.adminCommand(\"ping\")'" \
  --health-interval=10s \
  --health-timeout=5s \
  --health-retries=5 \
  mongo:7

echo "Waiting for MongoDB to be ready..."
for i in {1..30}; do
  if docker exec punchout-mongodb mongosh --quiet --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ MongoDB is ready${NC}"
    break
  fi
  echo -n "."
  sleep 2
done
echo ""

# Step 4: Initialize MongoDB
echo -e "${YELLOW}Step 4: Initializing MongoDB (indexes + environment configs)...${NC}"

# Create indexes
docker exec punchout-mongodb mongosh punchout --quiet --eval "
db.environment_configs.createIndex({environment: 1}, {unique: true, background: true});
db.punchout_sessions.createIndex({sessionKey: 1}, {unique: true, background: true});
db.punchout_sessions.createIndex({sessionDate: -1}, {background: true});
db.orders.createIndex({orderId: 1}, {unique: true, background: true});
db.orders.createIndex({orderDate: -1}, {background: true});
db.network_requests.createIndex({sessionKey: 1}, {background: true});
db.network_requests.createIndex({orderId: 1}, {background: true});
db.network_requests.createIndex({timestamp: -1}, {background: true});
db.network_requests.createIndex({timestamp: 1}, {expireAfterSeconds: 2592000, background: true});
print('Indexes created successfully');
"
echo -e "${GREEN}✓ Indexes created${NC}"

# Import environment configurations
if [ -f "mongodb-environment-configs-sample-data.json" ]; then
  docker cp mongodb-environment-configs-sample-data.json punchout-mongodb:/tmp/env-configs.json
  docker exec punchout-mongodb mongoimport \
    --db=punchout \
    --collection=environment_configs \
    --file=/tmp/env-configs.json \
    --jsonArray \
    --drop \
    --quiet
  echo -e "${GREEN}✓ Environment configurations imported${NC}"
else
  echo -e "${YELLOW}⚠ Environment config file not found, skipping import${NC}"
fi

# Import sample data (optional)
if [ -f "mongodb-sample-data.json" ]; then
  docker cp mongodb-sample-data.json punchout-mongodb:/tmp/sessions.json
  docker exec punchout-mongodb mongoimport \
    --db=punchout \
    --collection=punchout_sessions \
    --file=/tmp/sessions.json \
    --jsonArray \
    --drop \
    --quiet
  echo -e "${GREEN}✓ Sample session data imported${NC}"
fi

if [ -f "mongodb-orders-sample-data.json" ]; then
  docker cp mongodb-orders-sample-data.json punchout-mongodb:/tmp/orders.json
  docker exec punchout-mongodb mongoimport \
    --db=punchout \
    --collection=orders \
    --file=/tmp/orders.json \
    --jsonArray \
    --drop \
    --quiet
  echo -e "${GREEN}✓ Sample order data imported${NC}"
fi

if [ -f "mongodb-order-network-requests-sample-data.json" ]; then
  docker cp mongodb-order-network-requests-sample-data.json punchout-mongodb:/tmp/network-requests.json
  docker exec punchout-mongodb mongoimport \
    --db=punchout \
    --collection=network_requests \
    --file=/tmp/network-requests.json \
    --jsonArray \
    --quiet
  echo -e "${GREEN}✓ Sample network request data imported${NC}"
fi
echo ""

# Step 5: Start Gateway
echo -e "${YELLOW}Step 5: Starting Punchout Gateway...${NC}"
docker run -d \
  --name punchout-gateway \
  --network $NETWORK_NAME \
  -p 9090:9090 \
  -e APP_ENVIRONMENT=$ENVIRONMENT \
  -e SPRING_DATA_MONGODB_HOST=punchout-mongodb \
  -e SPRING_DATA_MONGODB_PORT=27017 \
  -e SPRING_DATA_MONGODB_DATABASE=punchout \
  -e SPRING_DATA_MONGODB_USERNAME=admin \
  -e SPRING_DATA_MONGODB_PASSWORD=$MONGO_PASSWORD \
  -e SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin \
  --health-cmd="wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1" \
  --health-interval=30s \
  --health-timeout=3s \
  --health-retries=3 \
  --health-start-period=40s \
  $REGISTRY/punchout-gateway:latest

echo "Waiting for Gateway to be ready..."
for i in {1..60}; do
  if docker exec punchout-gateway wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Gateway is ready${NC}"
    break
  fi
  echo -n "."
  sleep 2
done
echo ""

# Step 6: Start UI Backend
echo -e "${YELLOW}Step 6: Starting UI Backend...${NC}"
docker run -d \
  --name punchout-ui-backend \
  --network $NETWORK_NAME \
  -p 8080:8080 \
  -e SPRING_DATA_MONGODB_HOST=punchout-mongodb \
  -e SPRING_DATA_MONGODB_PORT=27017 \
  -e SPRING_DATA_MONGODB_DATABASE=punchout \
  -e SPRING_DATA_MONGODB_USERNAME=admin \
  -e SPRING_DATA_MONGODB_PASSWORD=$MONGO_PASSWORD \
  -e SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin \
  --health-cmd="wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1" \
  --health-interval=30s \
  --health-timeout=3s \
  --health-retries=3 \
  --health-start-period=40s \
  $REGISTRY/punchout-ui-backend:latest

echo "Waiting for UI Backend to be ready..."
for i in {1..60}; do
  if docker exec punchout-ui-backend wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ UI Backend is ready${NC}"
    break
  fi
  echo -n "."
  sleep 2
done
echo ""

# Step 7: Start Frontend
echo -e "${YELLOW}Step 7: Starting Frontend...${NC}"
docker run -d \
  --name punchout-ui-frontend \
  --network $NETWORK_NAME \
  -p 3000:3000 \
  -e NODE_ENV=production \
  -e NEXT_PUBLIC_API_URL=http://localhost:8080/api \
  --health-cmd="wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1" \
  --health-interval=30s \
  --health-timeout=3s \
  --health-retries=3 \
  --health-start-period=10s \
  $REGISTRY/punchout-ui-frontend:latest

echo "Waiting for Frontend to be ready..."
for i in {1..60}; do
  if docker exec punchout-ui-frontend wget --no-verbose --tries=1 --spider http://localhost:3000 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Frontend is ready${NC}"
    break
  fi
  echo -n "."
  sleep 2
done
echo ""

# Step 8: Verify deployment
echo -e "${YELLOW}Step 8: Verifying deployment...${NC}"
echo ""

# Check Gateway
if curl -sf http://localhost:9090/actuator/health > /dev/null; then
  echo -e "${GREEN}✓ Gateway health check passed${NC}"
else
  echo -e "${RED}✗ Gateway health check failed${NC}"
fi

# Check UI Backend
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
  echo -e "${GREEN}✓ UI Backend health check passed${NC}"
else
  echo -e "${RED}✗ UI Backend health check failed${NC}"
fi

# Check Frontend
if curl -sf http://localhost:3000 > /dev/null; then
  echo -e "${GREEN}✓ Frontend health check passed${NC}"
else
  echo -e "${RED}✗ Frontend health check failed${NC}"
fi

# Check MongoDB
MONGO_STATUS=$(docker exec punchout-mongodb mongosh --quiet --eval "db.adminCommand('ping').ok" 2>/dev/null || echo "0")
if [ "$MONGO_STATUS" == "1" ]; then
  echo -e "${GREEN}✓ MongoDB health check passed${NC}"
else
  echo -e "${RED}✗ MongoDB health check failed${NC}"
fi

echo ""

# Step 9: Show status
echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Deployment Complete!${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "${GREEN}Running Containers:${NC}"
docker ps --filter "name=punchout-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo -e "${GREEN}Access URLs:${NC}"
echo -e "  ${BLUE}Frontend:${NC}       http://localhost:3000"
echo -e "  ${BLUE}UI Backend API:${NC} http://localhost:8080/api"
echo -e "  ${BLUE}Gateway:${NC}        http://localhost:9090/punchout"
echo -e "  ${BLUE}Gateway Health:${NC} http://localhost:9090/actuator/health"
echo -e "  ${BLUE}MongoDB:${NC}        mongodb://admin:$MONGO_PASSWORD@localhost:27017"
echo ""

echo -e "${GREEN}Useful Commands:${NC}"
echo -e "  View logs:        ${BLUE}docker logs -f punchout-gateway${NC}"
echo -e "  Check status:     ${BLUE}docker ps --filter \"name=punchout-\"${NC}"
echo -e "  Access MongoDB:   ${BLUE}docker exec -it punchout-mongodb mongosh punchout${NC}"
echo -e "  Stop all:         ${BLUE}./stop-docker-deployment.sh${NC}"
echo -e "  View metrics:     ${BLUE}curl http://localhost:9090/actuator/prometheus${NC}"
echo ""

echo -e "${GREEN}Database Collections:${NC}"
docker exec punchout-mongodb mongosh punchout --quiet --eval "
print('  - environment_configs: ' + db.environment_configs.countDocuments());
print('  - punchout_sessions: ' + db.punchout_sessions.countDocuments());
print('  - orders: ' + db.orders.countDocuments());
print('  - network_requests: ' + db.network_requests.countDocuments());
"
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}🎉 Deployment Successful!${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "Open your browser to: ${BLUE}http://localhost:3000${NC}"
echo ""

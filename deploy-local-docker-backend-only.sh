#!/bin/bash

###############################################
# Deploy Backend Services to Local Docker
# Frontend runs separately with npm run dev
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
echo -e "${BLUE}Punchout Backend - Local Docker Deployment${NC}"
echo -e "${BLUE}=========================================${NC}"
echo -e "Version: ${GREEN}$VERSION${NC}"
echo -e "Environment: ${GREEN}$ENVIRONMENT${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Step 0: Build JARs locally
echo -e "${YELLOW}Step 0: Building Java applications...${NC}"

echo "Building punchout-gateway..."
cd punchout-gateway
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Gateway JAR built${NC}"
else
  echo -e "${RED}✗ Gateway build failed${NC}"
  exit 1
fi
cd ..

echo "Building punchout-ui-backend..."
cd punchout-ui-backend
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ UI Backend JAR built${NC}"
else
  echo -e "${RED}✗ UI Backend build failed${NC}"
  exit 1
fi
cd ..
echo ""

# Step 1: Clean up existing containers
echo -e "${YELLOW}Step 1: Cleaning up existing containers...${NC}"
docker stop punchout-ui-backend punchout-gateway punchout-mongodb 2>/dev/null || true
docker rm punchout-ui-backend punchout-gateway punchout-mongodb 2>/dev/null || true
docker network create $NETWORK_NAME 2>/dev/null || echo "Network already exists"
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Step 2: Build Docker images
echo -e "${YELLOW}Step 2: Building Docker images...${NC}"

echo "Building punchout-gateway image..."
docker build -q \
  -t $REGISTRY/punchout-gateway:$VERSION \
  -t $REGISTRY/punchout-gateway:latest \
  -f punchout-gateway/Dockerfile.simple \
  ./punchout-gateway
echo -e "${GREEN}✓ Gateway image built${NC}"

echo "Building punchout-ui-backend image..."
docker build -q \
  -t $REGISTRY/punchout-ui-backend:$VERSION \
  -t $REGISTRY/punchout-ui-backend:latest \
  -f punchout-ui-backend/Dockerfile.simple \
  ./punchout-ui-backend
echo -e "${GREEN}✓ UI Backend image built${NC}"
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
  mongo:7 > /dev/null

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
echo -e "${YELLOW}Step 4: Initializing MongoDB...${NC}"

docker exec punchout-mongodb mongosh punchout --quiet --eval "
db.environment_configs.createIndex({environment: 1}, {unique: true});
db.punchout_sessions.createIndex({sessionKey: 1}, {unique: true});
db.orders.createIndex({orderId: 1}, {unique: true});
db.network_requests.createIndex({sessionKey: 1});
db.network_requests.createIndex({orderId: 1});
db.network_requests.createIndex({timestamp: 1}, {expireAfterSeconds: 2592000});
print('✓ Indexes created');
" 2>/dev/null
echo -e "${GREEN}✓ Indexes created${NC}"

# Import data
for file in "mongodb-environment-configs-sample-data.json:environment_configs" \
            "mongodb-sample-data.json:punchout_sessions" \
            "mongodb-orders-sample-data.json:orders" \
            "mongodb-order-network-requests-sample-data.json:network_requests"; do
  IFS=':' read -r filename collection <<< "$file"
  if [ -f "$filename" ]; then
    docker cp "$filename" punchout-mongodb:/tmp/data.json > /dev/null
    docker exec punchout-mongodb mongoimport \
      --db=punchout \
      --collection=$collection \
      --file=/tmp/data.json \
      --jsonArray \
      --drop \
      --quiet 2>/dev/null || true
    echo -e "${GREEN}✓ Imported $collection${NC}"
  fi
done
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
  $REGISTRY/punchout-gateway:latest > /dev/null

echo "Waiting for Gateway..."
for i in {1..60}; do
  if curl -sf http://localhost:9090/actuator/health > /dev/null 2>&1; then
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
  $REGISTRY/punchout-ui-backend:latest > /dev/null

echo "Waiting for UI Backend..."
for i in {1..60}; do
  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ UI Backend is ready${NC}"
    break
  fi
  echo -n "."
  sleep 2
done
echo ""

# Verification
echo -e "${YELLOW}Verifying deployment...${NC}"
curl -sf http://localhost:9090/actuator/health > /dev/null && echo -e "${GREEN}✓ Gateway health check passed${NC}"
curl -sf http://localhost:8080/actuator/health > /dev/null && echo -e "${GREEN}✓ UI Backend health check passed${NC}"
docker exec punchout-mongodb mongosh --quiet --eval "db.adminCommand('ping')" > /dev/null 2>&1 && echo -e "${GREEN}✓ MongoDB health check passed${NC}"
echo ""

# Status
echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Backend Deployment Complete!${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "${GREEN}Running Services:${NC}"
docker ps --filter "name=punchout-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo -e "${GREEN}Access URLs:${NC}"
echo -e "  ${BLUE}Gateway:${NC}        http://localhost:9090/punchout"
echo -e "  ${BLUE}Gateway Health:${NC} http://localhost:9090/actuator/health"
echo -e "  ${BLUE}UI Backend API:${NC} http://localhost:8080/api"
echo -e "  ${BLUE}MongoDB:${NC}        mongodb://admin:$MONGO_PASSWORD@localhost:27017"
echo ""

echo -e "${YELLOW}To start the Frontend:${NC}"
echo -e "  cd punchout-ui-frontend"
echo -e "  npm run dev"
echo -e "  ${BLUE}Open: http://localhost:3000${NC}"
echo ""

echo -e "${GREEN}Useful Commands:${NC}"
echo -e "  Logs:    ${BLUE}./view-docker-logs.sh gateway${NC}"
echo -e "  Stop:    ${BLUE}./stop-docker-deployment.sh${NC}"
echo -e "  MongoDB: ${BLUE}docker exec -it punchout-mongodb mongosh punchout${NC}"
echo ""

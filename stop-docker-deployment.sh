#!/bin/bash

###############################################
# Stop Punchout Platform Docker Deployment
###############################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Stopping Punchout Platform${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Option to keep data
KEEP_DATA="${1:-false}"

if [ "$KEEP_DATA" == "--keep-data" ]; then
  echo -e "${YELLOW}Stopping containers (keeping data)...${NC}"
  REMOVE_VOLUMES=""
else
  echo -e "${YELLOW}Stopping containers and removing data...${NC}"
  REMOVE_VOLUMES="-v"
fi

# Stop containers
docker stop punchout-ui-frontend punchout-ui-backend punchout-gateway punchout-mongodb 2>/dev/null || true

# Remove containers
docker rm $REMOVE_VOLUMES punchout-ui-frontend punchout-ui-backend punchout-gateway punchout-mongodb 2>/dev/null || true

# Remove network
docker network rm punchout-prod-network 2>/dev/null || true

echo -e "${GREEN}✓ All containers stopped and removed${NC}"

if [ "$KEEP_DATA" != "--keep-data" ]; then
  echo -e "${GREEN}✓ Data volumes removed${NC}"
else
  echo -e "${YELLOW}ℹ Data volumes preserved${NC}"
  echo -e "  To remove data: ${BLUE}docker volume rm punchout-mongodb-data${NC}"
fi

echo ""
echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}Cleanup Complete!${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "To redeploy: ${BLUE}./deploy-local-docker.sh${NC}"
echo ""

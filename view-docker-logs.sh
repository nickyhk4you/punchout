#!/bin/bash

###############################################
# View Logs for Punchout Platform Services
###############################################

# Colors
BLUE='\033[0;34m'
NC='\033[0m'

SERVICE="${1:-all}"

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Punchout Platform Logs${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

case "$SERVICE" in
  gateway)
    echo "Showing Gateway logs (Ctrl+C to exit)..."
    docker logs -f --tail=100 punchout-gateway
    ;;
  backend)
    echo "Showing UI Backend logs (Ctrl+C to exit)..."
    docker logs -f --tail=100 punchout-ui-backend
    ;;
  frontend)
    echo "Showing Frontend logs (Ctrl+C to exit)..."
    docker logs -f --tail=100 punchout-ui-frontend
    ;;
  mongodb)
    echo "Showing MongoDB logs (Ctrl+C to exit)..."
    docker logs -f --tail=100 punchout-mongodb
    ;;
  all)
    echo "Showing all service logs (Ctrl+C to exit)..."
    docker logs -f --tail=50 punchout-gateway &
    docker logs -f --tail=50 punchout-ui-backend &
    docker logs -f --tail=50 punchout-ui-frontend &
    wait
    ;;
  *)
    echo "Usage: $0 [gateway|backend|frontend|mongodb|all]"
    echo ""
    echo "Examples:"
    echo "  ./view-docker-logs.sh gateway    # View gateway logs only"
    echo "  ./view-docker-logs.sh all        # View all logs"
    exit 1
    ;;
esac

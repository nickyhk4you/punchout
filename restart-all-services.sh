#!/bin/bash

echo "🔄 Restarting All Punchout Services (Backend + Frontend)..."
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service configuration
BACKEND_SERVICES=(
    "punchout-gateway:9090:Gateway Service"
    "punchout-ui-backend:8080:UI Backend Service"
    "punchout-mock-service:8082:Mock Service"
)

FRONTEND_PORT=3000
FRONTEND_DIR="punchout-ui-frontend"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Stopping All Services"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Stop backend services
BACKEND_PORTS=(9090 8080 8082)
SERVICE_NAMES=("Gateway" "UI Backend" "Mock Service")

for i in "${!BACKEND_PORTS[@]}"; do
    port=${BACKEND_PORTS[$i]}
    service_name=${SERVICE_NAMES[$i]}
    
    pid=$(lsof -ti :$port)
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}⏹  Stopping ${service_name} on port ${port} (PID: ${pid})...${NC}"
        kill $pid
        sleep 1
    else
        echo -e "${BLUE}   No process on port ${port} (${service_name})${NC}"
    fi
done

# Stop frontend
pid=$(lsof -ti :$FRONTEND_PORT)
if [ -n "$pid" ]; then
    echo -e "${YELLOW}⏹  Stopping Frontend on port ${FRONTEND_PORT} (PID: ${pid})...${NC}"
    kill $pid
    sleep 2
else
    echo -e "${BLUE}   No process on port ${FRONTEND_PORT} (Frontend)${NC}"
fi

echo ""
echo -e "${GREEN}✅ All services stopped${NC}"
echo ""

# Wait for ports to be released
sleep 3

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Building & Starting Backend Services"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Function to start backend service
start_backend_service() {
    local service_dir=$1
    local port=$2
    local service_name=$3
    
    echo -e "${BLUE}🔧 Starting ${service_name}...${NC}"
    
    # Build from root to include dependencies
    cd /Users/nickhu/dev/java/punchout
    
    if [ "$service_dir" != "punchout-gateway" ]; then
        echo "   Installing common module..."
        mvn clean install -pl punchout-common -am -DskipTests -q > /dev/null 2>&1
    fi
    
    echo "   Building ${service_name}..."
    mvn clean compile -pl $service_dir -am -DskipTests -q
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Build failed for ${service_name}${NC}"
        return 1
    fi
    
    # Navigate to service directory
    cd /Users/nickhu/dev/java/punchout/$service_dir
    
    echo "   Starting ${service_name} on port ${port}..."
    nohup mvn spring-boot:run > /tmp/$service_dir.log 2>&1 &
    
    echo "   Waiting for ${service_name} to start..."
    for i in {1..30}; do
        if lsof -ti :$port > /dev/null 2>&1; then
            echo -e "${GREEN}✅ ${service_name} started on port ${port}${NC}"
            echo ""
            return 0
        fi
        sleep 1
    done
    
    echo -e "${RED}❌ ${service_name} failed to start${NC}"
    echo "   Check logs: tail -f /tmp/$service_dir.log"
    return 1
}

# Start all backend services
for service_config in "${BACKEND_SERVICES[@]}"; do
    IFS=':' read -r service_dir port service_name <<< "$service_config"
    start_backend_service "$service_dir" "$port" "$service_name"
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Starting Frontend"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd /Users/nickhu/dev/java/punchout/$FRONTEND_DIR

echo -e "${BLUE}🎨 Starting Next.js Frontend...${NC}"
nohup npm run dev > /tmp/punchout-ui-frontend.log 2>&1 &

echo "   Waiting for Frontend to start..."
for i in {1..30}; do
    if lsof -ti :$FRONTEND_PORT > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Frontend started on port ${FRONTEND_PORT}${NC}"
        echo ""
        break
    fi
    sleep 1
done

if ! lsof -ti :$FRONTEND_PORT > /dev/null 2>&1; then
    echo -e "${RED}❌ Frontend failed to start${NC}"
    echo "   Check logs: tail -f /tmp/punchout-ui-frontend.log"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Final Service Status"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check all services
lsof -ti :9090 > /dev/null 2>&1 && echo -e "${GREEN}✅ Gateway Service      → http://localhost:9090${NC}" || echo -e "${RED}❌ Gateway Service (port 9090)${NC}"
lsof -ti :8080 > /dev/null 2>&1 && echo -e "${GREEN}✅ UI Backend Service   → http://localhost:8080${NC}" || echo -e "${RED}❌ UI Backend Service (port 8080)${NC}"
lsof -ti :8082 > /dev/null 2>&1 && echo -e "${GREEN}✅ Mock Service         → http://localhost:8082${NC}" || echo -e "${RED}❌ Mock Service (port 8082)${NC}"
lsof -ti :3000 > /dev/null 2>&1 && echo -e "${GREEN}✅ Frontend UI          → http://localhost:3000${NC}" || echo -e "${RED}❌ Frontend UI (port 3000)${NC}"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 Log files:"
echo "   Gateway:    tail -f /tmp/punchout-gateway.log"
echo "   UI Backend: tail -f /tmp/punchout-ui-backend.log"
echo "   Mock:       tail -f /tmp/punchout-mock-service.log"
echo "   Frontend:   tail -f /tmp/punchout-ui-frontend.log"
echo ""
echo "🌐 Access the application:"
echo "   → http://localhost:3000"
echo ""
echo "🛑 To stop all services:"
echo "   ./stop-all-services.sh"
echo "   kill \$(lsof -ti :3000)  # Stop frontend"
echo ""

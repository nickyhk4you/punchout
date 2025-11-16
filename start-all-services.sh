#!/bin/bash

echo "🚀 Starting All Punchout Backend Services..."
echo ""

# Service configuration
SERVICES=(
    "punchout-gateway:9090:Gateway Service"
    "punchout-ui-backend:8080:UI Backend Service"
    "punchout-mock-service:8082:Mock Service"
)

# Function to check if port is in use
check_port() {
    lsof -ti :$1 > /dev/null 2>&1
}

# Function to kill process on port
kill_port() {
    local port=$1
    local pid=$(lsof -ti :$port)
    if [ -n "$pid" ]; then
        echo "   Stopping existing process on port $port (PID: $pid)..."
        kill $pid
        sleep 2
    fi
}

# Function to start service
start_service() {
    local service_dir=$1
    local port=$2
    local service_name=$3
    
    echo "🔧 Starting $service_name..."
    
    # Kill existing process on port
    kill_port $port
    
    # Navigate to service directory
    cd /Users/nickhu/dev/java/punchout/$service_dir
    
    echo "   Building $service_name..."
    # Build from root to include dependencies
    cd /Users/nickhu/dev/java/punchout
    mvn clean install -pl punchout-common -am -DskipTests -q
    mvn clean compile -pl $service_dir -am -DskipTests -q
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed for $service_name"
        return 1
    fi
    
    # Return to service directory
    cd /Users/nickhu/dev/java/punchout/$service_dir
    
    echo "   Starting $service_name on port $port..."
    nohup mvn spring-boot:run > /tmp/$service_dir.log 2>&1 &
    
    echo "   Waiting for $service_name to start..."
    for i in {1..30}; do
        if check_port $port; then
            echo "✅ $service_name started successfully on port $port"
            echo ""
            return 0
        fi
        sleep 1
    done
    
    echo "❌ $service_name failed to start. Check logs: tail -f /tmp/$service_dir.log"
    return 1
}

# Start all services
for service_config in "${SERVICES[@]}"; do
    IFS=':' read -r service_dir port service_name <<< "$service_config"
    start_service "$service_dir" "$port" "$service_name"
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "⚠️  Failed to start $service_name. Continue anyway? (y/n)"
        read -r response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            echo "Aborting startup..."
            exit 1
        fi
    fi
done

echo ""
echo "📊 Service Status Summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_port 9090 && echo "✅ Gateway Service      → http://localhost:9090" || echo "❌ Gateway Service (port 9090)"
check_port 8080 && echo "✅ UI Backend Service   → http://localhost:8080" || echo "❌ UI Backend Service (port 8080)"
check_port 8082 && echo "✅ Mock Service         → http://localhost:8082" || echo "❌ Mock Service (port 8082)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 Log files:"
echo "   Gateway:    tail -f /tmp/punchout-gateway.log"
echo "   UI Backend: tail -f /tmp/punchout-ui-backend.log"
echo "   Mock:       tail -f /tmp/punchout-mock-service.log"
echo ""
echo "🛑 To stop all services: ./stop-all-services.sh"
echo ""

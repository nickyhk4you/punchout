#!/bin/bash

echo "🛑 Stopping All Punchout Services (Backend + Frontend)..."
echo ""

PORTS=(9090 8080 8082 3000)
SERVICE_NAMES=("Gateway Service" "UI Backend Service" "Mock Service" "Frontend UI")

for i in "${!PORTS[@]}"; do
    port=${PORTS[$i]}
    service_name=${SERVICE_NAMES[$i]}
    
    pid=$(lsof -ti :$port)
    if [ -n "$pid" ]; then
        echo "   Stopping $service_name on port $port (PID: $pid)..."
        kill $pid
    else
        echo "   No process found on port $port ($service_name)"
    fi
done

echo ""
echo "✅ All services stopped"

#!/bin/bash

echo "🔄 Restarting Punchout Backend..."

# Find and kill existing backend process
BACKEND_PID=$(lsof -ti :8080)
if [ -n "$BACKEND_PID" ]; then
    echo "   Stopping existing backend (PID: $BACKEND_PID)..."
    kill $BACKEND_PID
    sleep 2
fi

# Navigate to backend directory
cd /Users/nickhu/dev/java/punchout/punchout-ui-backend

echo "   Building backend..."
mvn clean compile -q

echo "   Starting backend on port 8080..."
nohup mvn spring-boot:run > /tmp/punchout-backend.log 2>&1 &

echo "   Waiting for backend to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/v1/sessions > /dev/null 2>&1; then
        echo "✅ Backend started successfully!"
        echo ""
        echo "📊 Testing API endpoints:"
        echo "   Session API: $(curl -s http://localhost:8080/api/v1/sessions/SESSION_005_MNO345 | grep -q sessionKey && echo '✅ Working' || echo '❌ Failed')"
        echo "   Network Requests API: $(curl -s http://localhost:8080/api/v1/sessions/SESSION_005_MNO345/network-requests | grep -q requestId && echo '✅ Working' || echo '❌ Failed')"
        echo ""
        echo "🌐 Frontend: http://localhost:3000"
        echo "🔗 Test URL: http://localhost:3000/sessions/SESSION_005_MNO345"
        exit 0
    fi
    sleep 1
done

echo "❌ Backend failed to start. Check logs: tail -f /tmp/punchout-backend.log"
exit 1

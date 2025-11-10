#!/bin/bash

echo "🧪 Testing Network Requests Feature"
echo "===================================="
echo ""

# Test 1: Backend session API
echo "1️⃣  Testing session API..."
RESULT=$(curl -s http://localhost:8080/api/v1/sessions/SESSION_005_MNO345)
if echo "$RESULT" | grep -q "contactEmail"; then
    echo "   ✅ Session API works (contactEmail field present)"
else
    echo "   ❌ Session API missing contactEmail field"
    echo "   ⚠️  Backend needs to be restarted!"
fi

# Test 2: Backend network requests API
echo ""
echo "2️⃣  Testing network requests API..."
RESULT=$(curl -s http://localhost:8080/api/v1/sessions/SESSION_005_MNO345/network-requests)
COUNT=$(echo "$RESULT" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null)
if [ "$COUNT" = "2" ]; then
    echo "   ✅ Network requests API works ($COUNT requests found)"
    echo "      - REQ_005_001 (INBOUND - OCI Setup)"
    echo "      - REQ_005_002 (OUTBOUND - Catalog API)"
else
    echo "   ❌ Network requests API failed"
fi

# Test 3: Check MongoDB data
echo ""
echo "3️⃣  Checking MongoDB data..."
MONGO_COUNT=$(mongosh punchout --quiet --eval "db.network_requests.countDocuments({sessionKey: 'SESSION_005_MNO345'})" 2>/dev/null)
if [ "$MONGO_COUNT" = "2" ]; then
    echo "   ✅ MongoDB has $MONGO_COUNT network requests for SESSION_005_MNO345"
else
    echo "   ⚠️  MongoDB count: $MONGO_COUNT"
fi

# Test 4: Frontend status
echo ""
echo "4️⃣  Checking frontend..."
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "   ✅ Frontend is running on port 3000"
else
    echo "   ❌ Frontend is not running"
    echo "   Start it with: cd punchout-ui-frontend && npm run dev"
fi

# Summary
echo ""
echo "===================================="
echo "📋 Summary:"
echo "   Backend API: http://localhost:8080"
echo "   Frontend: http://localhost:3000"
echo ""
echo "🔗 Test URLs:"
echo "   Session Detail: http://localhost:3000/sessions/SESSION_005_MNO345"
echo "   API Endpoint: http://localhost:8080/api/v1/sessions/SESSION_005_MNO345/network-requests"
echo ""
echo "💡 If tests fail, restart backend from IntelliJ or run ./restart-backend.sh"

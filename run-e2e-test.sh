#!/bin/bash

echo "🧪 Running Complete E2E Test"
echo "════════════════════════════════════════════════"
echo ""

# Check if services are running
echo "🔍 Checking services..."

SERVICES_OK=true

if ! lsof -ti :9090 > /dev/null 2>&1; then
    echo "❌ Gateway Service not running on port 9090"
    SERVICES_OK=false
fi

if ! lsof -ti :8080 > /dev/null 2>&1; then
    echo "❌ UI Backend Service not running on port 8080"
    SERVICES_OK=false
fi

if ! lsof -ti :8082 > /dev/null 2>&1; then
    echo "❌ Mock Service not running on port 8082"
    SERVICES_OK=false
fi

if [ "$SERVICES_OK" = false ]; then
    echo ""
    echo "⚠️  Some services are not running!"
    echo "   Run: ./start-all-services.sh"
    echo ""
    exit 1
fi

echo "✅ All services running"
echo ""
echo "════════════════════════════════════════════════"
echo ""
echo "📋 Instructions to run the E2E test:"
echo ""
echo "Option 1: Using Postman GUI"
echo "  1. Open Postman"
echo "  2. Import: Punchout_API.postman_collection.json"
echo "  3. Navigate to: 🚀 Complete E2E Test"
echo "  4. Click 'Send' on the request"
echo "  5. Open 'Console' (bottom panel) to see detailed logs"
echo ""
echo "Option 2: Using Newman (CLI)"
echo "  Run: npm install -g newman"
echo "  Then: newman run Punchout_API.postman_collection.json --folder '🚀 Complete E2E Test'"
echo ""
echo "════════════════════════════════════════════════"
echo ""
echo "What the test does:"
echo "  ✓ Sends PunchOut Setup request to Gateway (port 9090)"
echo "  ✓ Gateway calls Mock Service (port 8082)"
echo "  ✓ All requests are logged to MongoDB"
echo "  ✓ UI Backend (port 8080) serves the logged data"
echo "  ✓ Test fetches and displays all network requests"
echo "  ✓ Shows request/response payloads in console"
echo ""
echo "After the test, view results at:"
echo "  http://localhost:3000/sessions/SESSION_TEST_XXXXX"
echo "  (Session key will be shown in test output)"
echo ""

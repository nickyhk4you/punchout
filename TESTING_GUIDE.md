# Testing Guide

## Quick Start

### 1. Start All Services
```bash
./start-all-services.sh
```

This starts:
- **Gateway Service** on port 9090
- **UI Backend Service** on port 8080
- **Mock Service** on port 8082

### 2. Run the E2E Test

#### Option A: Using Postman (Recommended)

1. **Import Collection**
   - Open Postman
   - Click **Import**
   - Select `Punchout_API.postman_collection.json`

2. **Run the Test**
   - Navigate to folder: **🚀 Complete E2E Test**
   - Click on: **Complete PunchOut Flow - E2E Test**
   - Click **Send**

3. **View Results**
   - Open **Console** (View → Show Postman Console or Alt+Cmd+C)
   - You'll see detailed logs of all network requests
   - Example output:
     ```
     📊 NETWORK REQUESTS LOGGED IN UI DASHBOARD:
     ═══════════════════════════════════════════
     
     Request #1: REQ_001
       Direction: INBOUND
       URL: /punchout/setup
       Method: POST
       Status: 200
       📤 Request Payload: <cXML>...</cXML>
       📥 Response Payload: <cXML>...</cXML>
     
     Request #2: REQ_002
       Direction: OUTBOUND
       URL: http://localhost:8082/api/v1/token
       Method: POST
       Status: 200
       📤 Request Payload: {"sessionKey":"SESSION_..."}
       📥 Response Payload: "token-12345..."
     ```

4. **View in UI Dashboard**
   - Copy the URL from the console output
   - Open in browser: `http://localhost:3000/sessions/SESSION_TEST_XXXXX`
   - See all network requests with full payloads

#### Option B: Using Newman (CLI)

```bash
# Install Newman
npm install -g newman

# Run the test
newman run Punchout_API.postman_collection.json --folder "🚀 Complete E2E Test"
```

## What Gets Logged

The E2E test logs **all network activity** to MongoDB:

### Inbound Requests (to Gateway)
- **Request**: cXML PunchOut Setup
- **Headers**: Content-Type, Authorization, etc.
- **Payload**: Full cXML body
- **Response**: cXML response with catalog URL

### Outbound Requests (from Gateway to Mock Service)

1. **Token Generation**
   - URL: `http://localhost:8082/api/v1/token`
   - Payload: `{"sessionKey": "...", "operation": "create"}`
   - Response: One-time token

2. **Token Validation**
   - URL: `http://localhost:8082/api/v1/validate`
   - Payload: `{"token": "..."}`
   - Response: `{"valid": true, "message": "..."}`

3. **Catalog Request**
   - URL: `http://localhost:8082/api/v1/catalog`
   - Headers: `Authorization: Bearer <token>`
   - Payload: `{"sessionKey": "..."}`
   - Response: Catalog URL

## Test Flow

```
┌─────────────┐
│   Postman   │
└──────┬──────┘
       │ 1. POST /punchout/setup
       │    (cXML request)
       ▼
┌─────────────────┐
│ Gateway Service │ ─────┐
│   (port 9090)   │      │ Logs INBOUND request to MongoDB
└────────┬────────┘      │
         │               ▼
         │         ┌──────────┐
         │         │ MongoDB  │
         │         └──────────┘
         │
         │ 2. POST /api/v1/token
         ▼
┌─────────────────┐
│  Mock Service   │ ─────┐
│   (port 8082)   │      │ Logs OUTBOUND request to MongoDB
└────────┬────────┘      │
         │               ▼
         │         ┌──────────┐
         │         │ MongoDB  │
         │         └──────────┘
         │
         │ 3. Return catalog URL
         ▼
┌─────────────────┐
│ Gateway Service │
│   Returns cXML  │
└────────┬────────┘
         │
         │ 4. Postman test script fetches logs
         ▼
┌──────────────────┐
│  UI Backend      │ ─────→ Reads from MongoDB
│   (port 8080)    │        Returns network requests
└──────────────────┘
```

## Viewing Results

### In Postman Console
- All network requests with payloads
- Request/Response timing
- Status codes
- Direction (INBOUND/OUTBOUND)

### In UI Dashboard
1. Open browser: `http://localhost:3000`
2. Navigate to the session (URL shown in Postman Console)
3. View visual timeline of all requests
4. Click on any request to see full details

### In MongoDB
```bash
# View all network requests
mongosh punchout --eval "db.network_requests.find().pretty()"

# View requests for specific session
mongosh punchout --eval "db.network_requests.find({sessionKey: 'SESSION_TEST_12345'}).pretty()"
```

## Troubleshooting

### Services not running
```bash
# Check service status
./run-e2e-test.sh

# Start all services
./start-all-services.sh
```

### Test fails
1. Check Postman Console for error messages
2. Check service logs:
   ```bash
   tail -f /tmp/punchout-gateway.log
   tail -f /tmp/punchout-ui-backend.log
   tail -f /tmp/punchout-mock-service.log
   ```

### No network requests logged
1. Ensure MongoDB is running: `mongosh punchout`
2. Check Gateway logs for errors
3. Restart services: `./stop-all-services.sh && ./start-all-services.sh`

## Other Test Scripts

### Test individual APIs
```bash
# Test session API
curl http://localhost:8080/api/v1/sessions

# Test network requests for a session
curl http://localhost:8080/api/v1/sessions/SESSION_001_ABC123/network-requests

# Test Gateway health
curl http://localhost:9090/punchout/health

# Test Mock Service health
curl http://localhost:8082/api/v1/service-health
curl http://localhost:8082/api/v1/mule-health
```

### Stop all services
```bash
./stop-all-services.sh
```

## Integration with Frontend

After running the test, you can view the logged requests in the React frontend:

1. Start the frontend (if not running):
   ```bash
   cd punchout-ui-frontend
   npm run dev
   ```

2. Open: `http://localhost:3000`

3. Navigate to the session shown in the test output

4. See a visual timeline of all network requests with expandable details

# PunchOut Mock Service

Mock external services (Auth & Catalog) for testing the PunchOut Gateway.

## Services Provided

### 1. Mock Auth Service
- **Endpoint**: `POST /api/v1/token`
- **Port**: 8082
- **Features**:
  - Generates **one-time tokens** (OTT)
  - Token format: `OTT_XXXXXXXXXXXXXXXX`
  - Tokens expire after 30 minutes
  - Each token can only be used once
  - In-memory storage (resets on restart)

### 2. Mock Catalog Service
- **Endpoint**: `POST /api/v1/catalog`
- **Port**: 8082
- **Features**:
  - Returns catalog URL with session key
  - Requires Bearer token authentication
  - Returns catalog location for frontend

## Running the Service

```bash
# From project root
mvn spring-boot:run -pl punchout-mock-service

# Or from module directory
cd punchout-mock-service
mvn spring-boot:run
```

Service will start on **http://localhost:8082**

## Testing

### Check Health
```bash
curl http://localhost:8082/api/v1/health
```

### Generate Token
```bash
curl -X POST http://localhost:8082/api/v1/token \
  -H "Content-Type: application/json" \
  -d '{
    "sessionKey": "SESSION_TEST123",
    "operation": "create"
  }'
```

### Validate Token
```bash
curl -X POST http://localhost:8082/api/v1/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "OTT_XXXXXXXXXXXXXXXX"}'
```

### Get Catalog
```bash
curl -X POST http://localhost:8082/api/v1/catalog \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer OTT_XXXXXXXXXXXXXXXX" \
  -d '{
    "sessionKey": "SESSION_TEST123"
  }'
```

## End-to-End Test Flow

1. **Start Mock Service** (port 8082)
2. **Start Gateway** (port 9090)
3. **Send cXML request** to gateway
4. Gateway will:
   - Parse cXML
   - Call mock auth service for token
   - Call mock catalog service with token
   - Return cXML response

See Postman collection for full examples.

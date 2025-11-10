# PunchOut Testing Platform

A comprehensive Spring Boot platform for testing, monitoring, and debugging PunchOut integrations across multiple environments with full network request logging and customizable cXML templates.

## 🚀 Quick Start

### Start All Services
```bash
./start-all-services.sh
```

This starts:
- **Gateway Service** (port 9090) - cXML request handling
- **UI Backend** (port 8080) - REST APIs for frontend
- **Mock Service** (port 8082) - Mock auth & catalog services

### Start Frontend
```bash
cd punchout-ui-frontend
npm run dev
```
Frontend runs on **port 3000**: http://localhost:3000

### Stop All Services
```bash
./stop-all-services.sh
```

## 📋 Features

### 🧪 Developer PunchOut Testing
- Execute live PunchOut tests from the browser
- Support for **DEV**, **STAGE**, **PROD**, and **S4-DEV** environments
- Customer-specific cXML templates stored in MongoDB
- Editable cXML payloads with modal editor
- Real-time test results with network request logging

### 📊 Session Management
- View all PunchOut sessions across environments
- Filter by operation, environment, contact
- Sortable session list
- Clickable session keys for detailed views

### 🔍 Network Request Logging
- Log all **INBOUND** and **OUTBOUND** requests
- Full request/response payloads
- HTTP headers and status codes
- Request duration tracking
- Visual timeline in UI

### 🔄 cXML/JSON Converter
- Convert between cXML and JSON formats
- Syntax highlighting
- Download converted results

### 📝 Custom Templates
- MongoDB-stored cXML templates
- Environment-specific templates (DEV/STAGE/PROD/S4-DEV)
- Customer-specific overrides
- Placeholder replacement system

## 🏗️ Architecture

### Services

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   UI Backend     │────▶│    MongoDB      │
│  (Next.js)      │     │   (Port 8080)    │     │                 │
│  Port 3000      │     └──────────────────┘     └─────────────────┘
└─────────────────┘              │
                                 │
                    ┌────────────┴────────────┐
                    ▼                         ▼
          ┌──────────────────┐     ┌─────────────────┐
          │    Gateway       │────▶│  Mock Service   │
          │   (Port 9090)    │     │   (Port 8082)   │
          └──────────────────┘     └─────────────────┘
```

### Module Structure

```
punchout/
├── punchout-common/           # Shared models and utilities
├── punchout-gateway/          # cXML Gateway Service (Port 9090)
├── punchout-ui-backend/       # REST API Backend (Port 8080)
├── punchout-ui-frontend/      # Next.js Frontend (Port 3000)
├── punchout-mock-service/     # Mock Services (Port 8082)
├── punchout-order/            # Order processing module
└── punchout-invoice/          # Invoice processing module
```

## 🎯 Key Components

### Gateway Service (Port 9090)
**Purpose**: Handle cXML PunchOut requests

**Responsibilities:**
- Parse incoming cXML requests
- Authenticate with auth service
- Call catalog service
- Log all network requests to MongoDB
- Return cXML responses
- Save session data

**Endpoints:**
- `POST /punchout/setup` - PunchOut setup request
- `POST /punchout/order` - PunchOut order message
- `GET /punchout/health` - Health check
- `GET /actuator/health` - Actuator health endpoint

### UI Backend (Port 8080)
**Purpose**: Serve REST APIs for the frontend

**Responsibilities:**
- Fetch sessions from MongoDB
- Fetch network requests from MongoDB
- Serve cXML templates
- Provide session analytics
- Handle CORS for frontend

**Endpoints:**
- `GET /api/v1/sessions` - Get all sessions
- `GET /api/v1/sessions/{sessionKey}` - Get session by key
- `GET /api/v1/sessions/{sessionKey}/network-requests` - Get network requests for session
- `GET /api/v1/cxml-templates/environment/{env}` - Get templates by environment
- `POST /api/v1/cxml-templates` - Save cXML template

### Mock Service (Port 8082)
**Purpose**: Mock external auth and catalog services

**Endpoints:**
- `POST /api/v1/token` - Generate one-time UUID token
- `POST /api/v1/validate` - Validate token
- `POST /api/v1/catalog` - Return catalog URL
- `GET /api/v1/service-health` - Service health
- `GET /api/v1/mule-health` - Mule health

### Frontend (Port 3000)
**Purpose**: Web UI for testing and monitoring

**Pages:**
- `/` - Dashboard with stats and quick actions
- `/sessions` - List all sessions (sortable, filterable)
- `/sessions/{sessionKey}` - Session detail with network requests
- `/developer/punchout` - Developer testing interface
- `/converter` - cXML/JSON converter tool

## 🧪 Testing

### Option 1: Postman E2E Test

1. Import `Punchout_API.postman_collection.json` into Postman
2. Run the **🚀 Complete E2E Test** folder
3. Check Console for network request logs
4. View session in UI dashboard

### Option 2: Developer PunchOut Interface

1. Open http://localhost:3000/developer/punchout
2. Select environment (DEV/STAGE/PROD/S4-DEV)
3. Choose a customer
4. Click "PunchOut" button
5. View results and session dashboard

### Option 3: Shell Script

```bash
./run-e2e-test.sh
```

## 📊 Data Flow

### Complete PunchOut Flow

```
1. Client/Postman
   └─> POST /punchout/setup (cXML)
       │
2. Gateway Service
   ├─> Parse cXML (extract session key from BuyerCookie)
   ├─> Log INBOUND request to MongoDB
   ├─> POST /api/v1/token (get auth token)
   │   └─> Mock Service returns: OTT-{uuid}
   ├─> Log OUTBOUND token request
   ├─> POST /api/v1/catalog (with Bearer token)
   │   └─> Mock Service returns catalog URL
   ├─> Log OUTBOUND catalog request
   ├─> Save session to MongoDB
   └─> Return cXML response with catalog URL
       │
3. MongoDB
   ├─> punchout collection (sessions)
   ├─> network_requests collection (all requests)
   └─> cxml_templates collection (templates)
       │
4. UI Backend
   └─> Serves session & network request data to frontend
       │
5. Frontend
   └─> Displays session dashboard with all network requests
```

## 🗄️ MongoDB Collections

### `punchout`
Stores PunchOut sessions:
```javascript
{
  sessionKey: "SESSION_DEV_CUST001_1699876543210",
  buyerCookie: "SESSION_DEV_CUST001_1699876543210",
  operation: "CREATE",
  contact: "developer@waters.com",
  environment: "DEVELOPMENT",
  sessionDate: ISODate("2025-11-10T10:54:06.000Z"),
  catalog: "http://localhost:3000/catalog?sessionKey=..."
}
```

### `network_requests`
Stores all network requests:
```javascript
{
  requestId: "REQ_ABC123",
  sessionKey: "SESSION_DEV_CUST001_1699876543210",
  direction: "INBOUND",  // or "OUTBOUND"
  source: "B2B Customer",
  destination: "Punchout Gateway",
  method: "POST",
  url: "/punchout/setup",
  requestBody: "<?xml version=\"1.0\"...",
  responseBody: "<?xml version=\"1.0\"...",
  statusCode: 200,
  duration: 150,
  timestamp: ISODate("2025-11-10T10:54:06.000Z")
}
```

### `cxml_templates`
Stores cXML payload templates:
```javascript
{
  templateName: "Acme DEV Template",
  environment: "dev",
  customerId: "CUST001",
  customerName: "Acme Corporation",
  cxmlTemplate: "<?xml version=\"1.0\"...",
  description: "Development template for Acme",
  isDefault: false,
  createdAt: ISODate("2025-11-10..."),
  updatedAt: ISODate("2025-11-10...")
}
```

## 🔧 Configuration

### Gateway (application.yml)
```yaml
server:
  port: 9090

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/punchout

thirdparty:
  auth:
    url: http://localhost:8082/api/v1/token
  catalog:
    url: http://localhost:8082/api/v1/catalog

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### UI Backend (application.yml)
```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/punchout
```

### Mock Service (application.yml)
```yaml
server:
  port: 8082

catalog:
  base-url: http://localhost:3000/catalog
```

## 📦 Technologies Used

### Backend
- **Java 17** - Modern Java with Records support
- **Spring Boot 2.7.18** - Framework
- **Spring Data MongoDB** - MongoDB integration
- **Spring WebFlux** - Reactive HTTP client
- **Lombok** - Reduce boilerplate
- **Jackson** - JSON/XML processing
- **Apache Commons Text** - XML escaping

### Frontend
- **Next.js 14** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Axios** - HTTP client

### Database
- **MongoDB** - Document storage for sessions, requests, templates

## 🎨 Recent Optimizations

### Code Quality Improvements
✅ Custom exception hierarchy with domain-specific exceptions
✅ Centralized error handling with @ControllerAdvice
✅ XML-escaped error messages for security
✅ Removed @Transactional misuse on MongoDB operations
✅ Centralized WebClient with connection/response timeouts
✅ Type-safe @ConfigurationProperties for third-party URLs
✅ Centralized CORS configuration
✅ Java Records for response DTOs (immutable, type-safe)

### Resilience Improvements
✅ 3-second connection timeout
✅ 10-second response timeout
✅ Read/Write timeout handlers
✅ Prevents hanging requests

### Security Improvements
✅ No stack traces exposed in error responses
✅ XML-escaped error messages
✅ Safe error messages (generic for clients)
✅ Full error logging internally

## 📚 Documentation

- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - How to run E2E tests
- **[DEVELOPER_PUNCHOUT_ENHANCED.md](DEVELOPER_PUNCHOUT_ENHANCED.md)** - Developer PunchOut interface
- **[CXML_TEMPLATE_SYSTEM.md](CXML_TEMPLATE_SYSTEM.md)** - Template management
- **[CXML_TEMPLATES_LOADED.md](CXML_TEMPLATES_LOADED.md)** - Sample template data
- **[CODE_OPTIMIZATION_SUMMARY.md](CODE_OPTIMIZATION_SUMMARY.md)** - Code improvements
- **[E2E_TEST_FIXED.md](E2E_TEST_FIXED.md)** - E2E test fixes

## 🛠️ Development

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- Node.js 18+ (for frontend)

### Environment Setup

1. **Install MongoDB**
   ```bash
   brew install mongodb-community
   brew services start mongodb-community
   ```

2. **Clone Repository**
   ```bash
   git clone https://github.com/nickyhk4you/punchout.git
   cd punchout
   ```

3. **Build Backend**
   ```bash
   mvn clean install
   ```

4. **Install Frontend Dependencies**
   ```bash
   cd punchout-ui-frontend
   npm install
   ```

5. **Import Sample Data**
   ```bash
   # Import sample sessions
   mongoimport --db punchout --collection punchout --file mongodb-sample-data.json --jsonArray --drop
   
   # Import cXML templates
   mongoimport --db punchout --collection cxml_templates --file mongodb-cxml-templates-sample-data.json --jsonArray --drop
   
   # Import network requests
   mongoimport --db punchout --collection network_requests --file mongodb-network-requests-sample-data.json --jsonArray --drop
   ```

6. **Start Services**
   ```bash
   ./start-all-services.sh
   ```

7. **Start Frontend**
   ```bash
   cd punchout-ui-frontend
   npm run dev
   ```

8. **Open Browser**
   - Homepage: http://localhost:3000
   - Developer Testing: http://localhost:3000/developer/punchout
   - Sessions: http://localhost:3000/sessions

## 🧩 Customer List

The platform includes 5 sample customers for testing:

| ID | Name | Domain | Buyer ID |
|----|------|--------|----------|
| CUST001 | Acme Corporation | acme.com | buyer123 |
| CUST002 | TechCorp Industries | techcorp.com | buyer456 |
| CUST003 | Global Solutions Inc | globalsolutions.com | buyer789 |
| CUST004 | Enterprise Partners | enterprise.com | buyer321 |
| CUST005 | Innovation Labs | innovationlabs.com | buyer654 |

Each customer has **4 cXML templates** (one per environment).

## 🔍 Monitoring & Debugging

### View Logs
```bash
# Gateway logs
tail -f /tmp/punchout-gateway.log

# UI Backend logs
tail -f /tmp/punchout-ui-backend.log

# Mock Service logs
tail -f /tmp/punchout-mock-service.log
```

### MongoDB Queries
```bash
# View all sessions
mongosh punchout --eval "db.punchout.find().pretty()"

# View network requests for a session
mongosh punchout --eval "db.network_requests.find({sessionKey: 'SESSION_XXX'}).pretty()"

# View cXML templates
mongosh punchout --eval "db.cxml_templates.find().pretty()"

# Count sessions by environment
mongosh punchout --eval "db.punchout.aggregate([{$group: {_id: '\$environment', count: {$sum: 1}}}])"
```

### Health Checks
```bash
# Gateway health
curl http://localhost:9090/punchout/health
curl http://localhost:9090/actuator/health

# UI Backend health
curl http://localhost:8080/actuator/health

# Mock Service health
curl http://localhost:8082/api/v1/service-health
curl http://localhost:8082/api/v1/mule-health
```

## 📖 API Documentation

### Gateway API

#### PunchOut Setup
```bash
POST http://localhost:9090/punchout/setup
Content-Type: text/xml

<?xml version="1.0" encoding="UTF-8"?>
<cXML payloadID="123456" timestamp="2025-11-10T10:30:00">
  <Header>...</Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>SESSION_TEST_12345</BuyerCookie>
      ...
    </PunchOutSetupRequest>
  </Request>
</cXML>
```

**Response:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<cXML>
  <Response>
    <Status code="200" text="success"/>
    <PunchOutSetupResponse>
      <BuyerCookie>SESSION_TEST_12345</BuyerCookie>
      <StartPage>
        <URL>http://localhost:3000/catalog?sessionKey=SESSION_TEST_12345</URL>
      </StartPage>
    </PunchOutSetupResponse>
  </Response>
</cXML>
```

### UI Backend API

#### Get All Sessions
```bash
GET http://localhost:8080/api/v1/sessions
```

#### Get Session by Key
```bash
GET http://localhost:8080/api/v1/sessions/{sessionKey}
```

#### Get Network Requests for Session
```bash
GET http://localhost:8080/api/v1/sessions/{sessionKey}/network-requests
```

#### Get cXML Templates
```bash
# All templates
GET http://localhost:8080/api/v1/cxml-templates

# By environment
GET http://localhost:8080/api/v1/cxml-templates/environment/dev

# By customer and environment
GET http://localhost:8080/api/v1/cxml-templates/environment/dev/customer/CUST001

# Default template for environment
GET http://localhost:8080/api/v1/cxml-templates/environment/dev/default
```

### Mock Service API

#### Generate Token
```bash
POST http://localhost:8082/api/v1/token
Content-Type: application/json

{
  "sessionKey": "SESSION_TEST_123",
  "operation": "create"
}
```

**Response:** `OTT-51709106-b64e-4dd8-9767-550a47edff9e`

#### Validate Token
```bash
POST http://localhost:8082/api/v1/validate
Content-Type: application/json

{
  "token": "OTT-51709106-b64e-4dd8-9767-550a47edff9e"
}
```

## 🐛 Troubleshooting

### Services Not Starting

**Check if ports are in use:**
```bash
lsof -ti :9090  # Gateway
lsof -ti :8080  # UI Backend
lsof -ti :8082  # Mock Service
```

**Kill processes:**
```bash
./stop-all-services.sh
```

**Check logs:**
```bash
tail -50 /tmp/punchout-gateway.log
tail -50 /tmp/punchout-ui-backend.log
tail -50 /tmp/punchout-mock-service.log
```

### MongoDB Connection Issues

**Check MongoDB is running:**
```bash
mongosh punchout --eval "db.stats()"
```

**Start MongoDB:**
```bash
brew services start mongodb-community
```

### Frontend Not Loading

**Check frontend process:**
```bash
lsof -ti :3000
```

**Restart frontend:**
```bash
cd punchout-ui-frontend
npm run dev
```

### CORS Errors

The UI Backend CORS is configured to allow all origins. If you see CORS errors:

1. Check UI Backend is running on port 8080
2. Check browser console for error details
3. Restart UI Backend: `./stop-all-services.sh && ./start-all-services.sh`

## 🔐 Security Notes

### Development vs Production

**Current Configuration (Development):**
- CORS allows all origins (`allowedOriginPatterns("*")`)
- Mock service with simple tokens
- Secrets in application.yml

**For Production:**
- Restrict CORS to specific domains
- Use real auth service with OAuth2/JWT
- Store secrets in vault (HashiCorp Vault, AWS Secrets Manager)
- Enable HTTPS/TLS
- Add rate limiting
- Enable authentication/authorization

## 📈 Performance

### Optimizations Implemented
- Connection pooling via Reactor Netty
- Request timeouts (3s connect, 10s response)
- Efficient data structures (Records vs Maps)
- MongoDB indexing on sessionKey
- Lazy loading of templates

### Scalability Considerations
- Stateless services (horizontal scaling ready)
- MongoDB for session storage (shared state)
- WebClient with connection pooling
- Async request processing ready

## 🚦 Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| Frontend | 3000 | Next.js web UI |
| UI Backend | 8080 | REST APIs |
| Mock Service | 8082 | Mock auth & catalog |
| Gateway | 9090 | cXML request handler |

## 🎓 Best Practices Implemented

1. **Separation of Concerns** - Clear module boundaries
2. **Exception Hierarchy** - Domain-specific exceptions
3. **Configuration Management** - @ConfigurationProperties
4. **Type Safety** - Java Records for DTOs
5. **Centralized Cross-Cutting** - CORS, WebClient, Error Handling
6. **Observability** - Actuator, logging, network request tracking
7. **Timeout Management** - Prevent hanging requests
8. **Template Pattern** - cXML templates in MongoDB
9. **Modern Java** - Records, Optional, Stream API
10. **Clean Architecture** - Controllers → Services → Repositories

## 📝 Contributing

### Code Style
- Use Lombok for boilerplate reduction
- Follow Spring Boot best practices
- Use Records for immutable DTOs
- Centralize configuration
- Add proper logging
- Write meaningful exception messages

### Testing
- Write unit tests for services
- Integration tests for controllers
- E2E tests via Postman
- Test all environments (DEV/STAGE/PROD/S4-DEV)

## 📄 License

Copyright © 2025 Waters Corporation. All rights reserved.

## 🤝 Support

For issues or questions:
1. Check existing documentation in docs folder
2. Review log files
3. Check MongoDB data
4. Test with Postman collection
5. Contact support team

## 🎉 Quick Links

- **Homepage**: http://localhost:3000
- **Developer Testing**: http://localhost:3000/developer/punchout
- **Sessions**: http://localhost:3000/sessions
- **Converter**: http://localhost:3000/converter
- **Gateway Health**: http://localhost:9090/actuator/health
- **Backend Health**: http://localhost:8080/actuator/health

---

**Built with ❤️ using Spring Boot, Next.js, and MongoDB**

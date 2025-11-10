# PunchOut Testing Platform - Complete Explanation

## 🎯 What Is This Project?

The **PunchOut Testing Platform** is a comprehensive development and testing tool for **PunchOut** integrations in B2B e-commerce. It allows developers to test, monitor, and debug PunchOut catalog integrations across multiple environments (DEV, STAGE, PROD, S4-DEV) with full visibility into all network requests.

### What is PunchOut?

**PunchOut** is a B2B e-commerce protocol where:
1. A **buyer** (customer) clicks a "Shop" button in their procurement system
2. They are redirected to a **supplier's** catalog website
3. They browse and add items to a shopping cart
4. They click "Return" and the cart is sent back to their procurement system
5. The order is placed in their system (not the supplier's)

**Think of it as**: Shopping on Amazon, but the cart goes into your company's SAP system, not Amazon's checkout.

## 🏢 Real-World Use Case

### Scenario: Waters Corporation sells lab equipment to hospitals

**Without PunchOut:**
- Hospital buyer goes to Waters website
- Adds products to cart
- Manually copies items to their SAP/procurement system
- ❌ Time consuming
- ❌ Error-prone
- ❌ No integration

**With PunchOut:**
- Hospital buyer clicks "Shop Waters" in their SAP system
- Automatically redirected to Waters catalog (with authentication)
- Browses and adds items
- Clicks "Return to SAP"
- Cart automatically appears in SAP
- ✅ Seamless
- ✅ Automated
- ✅ Integrated

### This Platform's Role

This platform helps Waters **test and debug** PunchOut integrations with different hospitals/customers across different environments before going live.

## 🧩 System Components

### 1. Frontend (Next.js - Port 3000)

**What it does:**
- Web-based UI for testing PunchOut integrations
- Allows developers to execute PunchOut tests
- Displays all sessions and network requests
- Provides cXML/JSON converter tool

**Key Pages:**
- **Dashboard** (`/`) - Overview, stats, quick actions
- **Developer PunchOut** (`/developer/punchout`) - Execute tests, edit payloads
- **Sessions** (`/sessions`) - View all PunchOut sessions
- **Session Detail** (`/sessions/{key}`) - See network request logs
- **Converter** (`/converter`) - Convert cXML ↔ JSON

**Technology:** Next.js 14, TypeScript, Tailwind CSS

### 2. UI Backend (Spring Boot - Port 8080)

**What it does:**
- Serves REST APIs for the frontend
- Fetches session data from MongoDB
- Fetches network request logs from MongoDB
- Manages cXML templates
- Handles CORS for frontend

**Key APIs:**
- `GET /api/v1/sessions` - List all PunchOut sessions
- `GET /api/v1/sessions/{key}/network-requests` - Get request logs
- `GET /api/v1/cxml-templates/environment/{env}` - Get templates
- `POST /api/v1/cxml-templates` - Save templates

**Technology:** Spring Boot 2.7.18, Spring Data MongoDB, Spring Data JPA

### 3. Gateway (Spring Boot - Port 9090)

**What it does:**
- Handles incoming cXML PunchOut requests
- Acts as middleware between buyer and supplier systems
- Authenticates requests with mock auth service
- Calls mock catalog service to get catalog URL
- Logs ALL network requests (INBOUND and OUTBOUND) to MongoDB
- Saves session information to MongoDB
- Returns cXML responses

**Key Endpoints:**
- `POST /punchout/setup` - PunchOut setup request (main endpoint)
- `POST /punchout/order` - PunchOut order message
- `GET /punchout/health` - Health check

**Technology:** Spring Boot 2.7.18, WebFlux (WebClient), MongoDB

### 4. Mock Service (Spring Boot - Port 8082)

**What it does:**
- Simulates external auth service
- Simulates external catalog service
- Generates one-time UUID tokens
- Validates tokens
- Returns mock catalog URLs

**Key Endpoints:**
- `POST /api/v1/token` - Generate auth token
- `POST /api/v1/validate` - Validate token
- `POST /api/v1/catalog` - Get catalog URL

**Technology:** Spring Boot 2.7.18

### 5. MongoDB (Port 27017)

**What it stores:**

**Collection: `punchout`**
- All PunchOut sessions
- Session metadata (customer, environment, dates)
- Cart return URLs
- Contact information

**Collection: `network_requests`**
- ALL network requests (INBOUND and OUTBOUND)
- Full request/response payloads
- Headers, status codes, durations
- Timestamps and success flags

**Collection: `cxml_templates`**
- cXML payload templates
- Environment-specific (DEV/STAGE/PROD/S4-DEV)
- Customer-specific overrides
- Default templates

## 🔄 Complete Flow Explanation

### Example: Developer Tests Acme Corporation in DEV Environment

```
Step 1: Developer Opens Browser
  └─> http://localhost:3000/developer/punchout

Step 2: Select Environment & Customer
  ├─> Click "DEV" environment button
  └─> Find "Acme Corporation" in customer list

Step 3: Execute PunchOut (Two Options)
  
  Option A: Quick PunchOut
    └─> Click "PunchOut" button
        ├─> System fetches template from MongoDB
        │   └─> GET /api/v1/cxml-templates/environment/dev/customer/CUST001
        ├─> Replace placeholders in template
        │   ├─> {{SESSION_KEY}} → SESSION_DEV_CUST001_1699876543210
        │   ├─> {{BUYER_ID}} → buyer123
        │   ├─> {{DOMAIN}} → acme.com
        │   └─> {{TIMESTAMP}} → 2025-11-10T10:30:00
        └─> Send cXML to Gateway
  
  Option B: Custom PunchOut
    ├─> Click "Edit Payload" button
    ├─> Modal opens with editable cXML
    ├─> Developer modifies XML as needed
    └─> Click "Execute PunchOut"

Step 4: Gateway Processes Request
  ├─> Receives cXML POST request
  ├─> Parses XML to extract:
  │   ├─> BuyerCookie → SESSION_DEV_CUST001_1699876543210
  │   ├─> Contact Email → dev@acme.com
  │   ├─> Operation → create
  │   └─> Cart Return URL → https://dev.acme.com/punchout/return
  │
  ├─> Logs INBOUND Request to MongoDB
  │   └─> Direction: INBOUND
  │       Source: B2B Customer
  │       Destination: Punchout Gateway
  │       Method: POST
  │       URL: /punchout/setup
  │       Payload: Full cXML request
  │
  ├─> Calls Mock Auth Service
  │   ├─> POST http://localhost:8082/api/v1/token
  │   ├─> Payload: {"sessionKey": "SESSION_DEV_CUST001_...", "operation": "create"}
  │   ├─> Mock Service generates: OTT-51709106-b64e-4dd8-9767-550a47edff9e
  │   └─> Logs OUTBOUND Request to MongoDB
  │
  ├─> Calls Mock Catalog Service
  │   ├─> POST http://localhost:8082/api/v1/catalog
  │   ├─> Header: Authorization: Bearer OTT-51709106-...
  │   ├─> Payload: {"sessionKey": "SESSION_DEV_CUST001_..."}
  │   ├─> Mock Service returns: {"catalogUrl": "http://localhost:3000/catalog?sessionKey=..."}
  │   └─> Logs OUTBOUND Request to MongoDB
  │
  ├─> Saves Session to MongoDB
  │   └─> Collection: punchout
  │       sessionKey: SESSION_DEV_CUST001_1699876543210
  │       environment: DEVELOPMENT (extracted from sessionKey)
  │       operation: CREATE
  │       contact: dev@acme.com
  │       sessionDate: 2025-11-10T10:30:00
  │
  └─> Returns cXML Response
      └─> Contains:
          ├─> Status: 200 success
          ├─> BuyerCookie: SESSION_DEV_CUST001_1699876543210
          └─> Catalog URL: http://localhost:3000/catalog?sessionKey=...

Step 5: Frontend Displays Results
  ├─> Success message shown
  ├─> Session key displayed
  ├─> Fetches network requests from Backend API
  │   └─> GET /api/v1/sessions/SESSION_DEV_CUST001_.../network-requests
  │
  └─> Shows 3 Network Requests:
      ├─> Request #1: INBOUND - cXML to Gateway (200, 150ms)
      ├─> Request #2: OUTBOUND - Gateway to Mock (token) (200, 50ms)
      └─> Request #3: OUTBOUND - Gateway to Mock (catalog) (200, 20ms)

Step 6: Developer Views Session
  ├─> Clicks "View Session Dashboard"
  ├─> Navigates to /sessions/SESSION_DEV_CUST001_...
  └─> Sees:
      ├─> Session information card
      ├─> Network requests table with full payloads
      ├─> Request/response bodies
      └─> Headers, status codes, timing
```

## 🎨 Key Features Explained

### Feature 1: Multi-Environment Testing

**Problem:** Need to test PunchOut in different environments without affecting production

**Solution:**
- Templates for DEV, STAGE, PROD, S4-DEV
- Environment auto-detected from session key
- Different configurations per environment
- Isolated testing

**How it works:**
1. Developer selects environment (e.g., "DEV")
2. Session key includes environment: `SESSION_DEV_CUST001_...`
3. Gateway extracts environment from key
4. Session saved with `environment: "DEVELOPMENT"`
5. Can filter sessions by environment in UI

### Feature 2: Network Request Logging

**Problem:** Hard to debug PunchOut issues without seeing all requests

**Solution:**
- Every HTTP request is logged to MongoDB
- Both INBOUND (to Gateway) and OUTBOUND (from Gateway)
- Full request/response payloads stored
- Headers, status codes, durations tracked

**What gets logged:**

**INBOUND Request (from customer to Gateway):**
```javascript
{
  direction: "INBOUND",
  source: "B2B Customer",
  destination: "Punchout Gateway",
  method: "POST",
  url: "/punchout/setup",
  requestBody: "<?xml version=\"1.0\"...",  // Full cXML
  responseBody: "<?xml version=\"1.0\"...", // Full cXML response
  statusCode: 200,
  duration: 150
}
```

**OUTBOUND Request (from Gateway to Mock):**
```javascript
{
  direction: "OUTBOUND",
  source: "Punchout Gateway",
  destination: "Auth Service",
  method: "POST",
  url: "http://localhost:8082/api/v1/token",
  requestBody: "{\"sessionKey\":\"SESSION_...\"}",
  responseBody: "OTT-51709106-b64e-4dd8-9767-550a47edff9e",
  statusCode: 200,
  duration: 50
}
```

### Feature 3: Custom cXML Templates

**Problem:** Different customers need different cXML formats

**Solution:**
- Store templates in MongoDB
- One template per customer per environment
- Placeholder replacement system
- Easy to modify without code changes

**Template Example:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<cXML payloadID="{{PAYLOAD_ID}}" timestamp="{{TIMESTAMP}}">
  <Header>
    <From>
      <Credential domain="NetworkID">
        <Identity>{{BUYER_ID}}</Identity>  <!-- Replaced at runtime -->
      </Credential>
    </From>
    ...
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>{{SESSION_KEY}}</BuyerCookie>  <!-- Auto-generated -->
      <Extrinsic name="Environment">dev</Extrinsic>
      <Contact role="buyer">
        <Email>{{CONTACT_EMAIL}}</Email>
      </Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>
```

**Placeholders:**
- `{{SESSION_KEY}}` → `SESSION_DEV_CUST001_1699876543210`
- `{{BUYER_ID}}` → `buyer123`
- `{{DOMAIN}}` → `acme.com`
- `{{CUSTOMER_NAME}}` → `Acme Corporation`
- `{{PAYLOAD_ID}}` → Random number
- `{{TIMESTAMP}}` → Current ISO timestamp

### Feature 4: Session Dashboard

**Problem:** Need to see what happened during a PunchOut session

**Solution:**
- Each session has a detailed dashboard
- Shows all network requests chronologically
- Full request/response payloads
- Status codes and timing information
- Easy debugging interface

**What you see:**
1. **Session Info Card**
   - Session Key
   - Operation (CREATE/EDIT/INSPECT)
   - Environment
   - Contact email
   - Timestamps

2. **Network Requests Table**
   - All 3 requests in chronological order
   - Direction badges (INBOUND/OUTBOUND)
   - Method, URL, Status
   - Duration in milliseconds
   - Click to see full payload

3. **Request Details**
   - Full HTTP headers
   - Complete request body
   - Complete response body
   - Pretty-printed JSON/XML

## 🔧 Technical Architecture

### Design Patterns Used

1. **Microservices Architecture**
   - Separate services for different concerns
   - Independent deployment
   - Horizontal scaling ready

2. **Gateway Pattern**
   - Gateway acts as entry point
   - Handles routing and orchestration
   - Centralizes logging and monitoring

3. **Repository Pattern**
   - Data access abstraction
   - MongoDB repositories
   - JPA repositories (for future SQL support)

4. **DTO Pattern**
   - Separate domain models from API contracts
   - Type-safe data transfer
   - Java Records for immutability

5. **Template Method Pattern**
   - cXML templates with placeholder replacement
   - Customer-specific overrides
   - Default templates

6. **Strategy Pattern**
   - Different converters for different customers
   - Runtime selection based on customer ID

### Code Organization

```
Gateway Service:
├── controller/         # REST endpoints (handle HTTP)
├── service/           # Business logic (orchestration)
├── client/            # External HTTP calls (Auth, Catalog)
├── converter/         # cXML ↔ JSON conversion
├── logging/           # Network request logging
├── repository/        # MongoDB data access
├── entity/            # MongoDB documents
├── model/             # Domain models
├── dto/               # Data transfer objects (Records)
├── exception/         # Custom exceptions
└── config/            # Configuration beans

UI Backend:
├── controller/        # REST API controllers
├── service/           # Business logic
├── mongo/
│   ├── controller/    # MongoDB-specific controllers
│   ├── service/       # MongoDB services
│   ├── repository/    # MongoDB repositories
│   └── entity/        # MongoDB documents
├── mapper/            # Entity ↔ DTO mapping
├── dto/               # API response objects
├── exception/         # Exception handling
└── config/            # Configuration

Mock Service:
├── controller/        # Mock endpoints
└── service/           # Token management
```

## 🎬 Typical Workflows

### Workflow 1: Developer Tests Acme in DEV

```
1. Open http://localhost:3000/developer/punchout
2. Click "DEV" environment
3. Find "Acme Corporation" in table
4. Click "PunchOut" button
5. Wait 1-2 seconds
6. See success message with:
   - Session Key: SESSION_DEV_CUST001_1699876543210
   - 3 Network Requests logged
   - Link to session dashboard
7. Click "View Session Dashboard"
8. See full session with all request details
```

**Result:**
- Session saved in MongoDB
- 3 network requests logged
- Full visibility into what happened
- Can share session link with team

### Workflow 2: QA Tests Multiple Customers

```
1. Open http://localhost:3000/developer/punchout
2. Click "STAGE" environment
3. Test each customer:
   - Acme → Click PunchOut → View results
   - TechCorp → Click PunchOut → View results
   - Global Solutions → Click PunchOut → View results
4. Go to /sessions
5. Filter by environment: "STAGING"
6. See all test sessions
7. Compare network requests across customers
```

**Result:**
- Multiple sessions in STAGE environment
- Can compare different customer integrations
- Identify differences in cXML formats
- Debug customer-specific issues

### Workflow 3: Customize Template for Customer

```
1. Open http://localhost:3000/developer/punchout
2. Select "PROD" environment
3. Find "TechCorp Industries"
4. Click "Edit Payload"
5. Modal opens with cXML template
6. Modify cXML:
   - Change credential domain
   - Add custom extrinsics
   - Update contact email
7. Click "Execute PunchOut"
8. Session created with custom cXML
```

**Result:**
- Custom cXML tested
- Can verify format works
- Can save as new template in MongoDB

### Workflow 4: Debug Failed PunchOut

```
1. Open http://localhost:3000/sessions
2. Find failed session (red status)
3. Click session key
4. View network requests
5. See which request failed:
   - INBOUND request: ✅ 200 OK
   - OUTBOUND token request: ❌ 500 Error
   - OUTBOUND catalog request: (not reached)
6. Click failed request
7. See error response:
   {
     "error": "Invalid session key format",
     "statusCode": 500
   }
8. Identify issue: session key missing underscore
9. Fix template and retry
```

**Result:**
- Quick identification of failure point
- Full error details
- Easy to fix and retest

## 💡 Why This Platform is Valuable

### For Developers

✅ **Fast Testing** - Test PunchOut in seconds, not hours
✅ **Full Visibility** - See every request and response
✅ **Easy Debugging** - Pinpoint exact failure points
✅ **Environment Isolation** - Test in DEV without touching PROD
✅ **Reusable Templates** - Don't recreate cXML every time

### For QA Teams

✅ **Comprehensive Testing** - Test all customers, all environments
✅ **Regression Testing** - Verify nothing broke after changes
✅ **Documentation** - Session logs serve as test evidence
✅ **Comparison** - Compare customer integrations side-by-side

### For Operations

✅ **Monitoring** - See all PunchOut sessions in production
✅ **Analytics** - Session counts by environment
✅ **Audit Trail** - Full history of all requests
✅ **Troubleshooting** - Debug customer issues quickly

### For Business

✅ **Faster Onboarding** - Test new customers quickly
✅ **Quality Assurance** - Catch issues before production
✅ **Customer Support** - Help customers debug their integration
✅ **Compliance** - Complete audit logs

## 🔑 Key Concepts

### Session Key
- **Unique identifier** for each PunchOut session
- Format: `SESSION_{ENV}_{CUSTOMER}_{TIMESTAMP}`
- Example: `SESSION_DEV_CUST001_1699876543210`
- Used to correlate all requests
- Stored in MongoDB
- Displayed in UI

### BuyerCookie
- **cXML term** for session identifier
- Same as Session Key in this platform
- Sent in cXML request
- Returned in cXML response
- Used to track shopping session

### Network Request
- **Any HTTP request** in or out of the Gateway
- **INBOUND**: Customer → Gateway
- **OUTBOUND**: Gateway → External Service
- All logged to MongoDB
- Displayed in session dashboard

### Environment
- **Where the PunchOut is tested**
- **DEV** - Development (testing, experiments)
- **STAGE** - Staging (QA, pre-production)
- **PROD** - Production (live customer traffic)
- **S4-DEV** - SAP S/4HANA development

### Template
- **cXML payload** with placeholders
- Stored in MongoDB
- One per customer per environment
- Can be edited and saved
- Default fallback available

## 📊 Data Relationships

```
Session (punchout collection)
  ├─> sessionKey: "SESSION_DEV_CUST001_..."
  └─> Has Many: Network Requests
      
Network Request (network_requests collection)
  ├─> sessionKey: "SESSION_DEV_CUST001_..."  (foreign key)
  ├─> direction: "INBOUND" or "OUTBOUND"
  └─> Belongs To: Session

cXML Template (cxml_templates collection)
  ├─> environment: "dev"
  ├─> customerId: "CUST001"
  └─> Used To Generate: cXML Payload
```

## 🎓 Learning Path

### If You're New to This Project

**Step 1: Understand PunchOut**
- Read about PunchOut protocol
- Understand buyer/supplier workflow
- Learn cXML format basics

**Step 2: Run the Platform**
```bash
./start-all-services.sh
cd punchout-ui-frontend && npm run dev
```

**Step 3: Execute Your First Test**
- Open http://localhost:3000/developer/punchout
- Click DEV environment
- Click "PunchOut" on any customer
- See the results

**Step 4: Explore the Data**
```bash
# See the session in MongoDB
mongosh punchout --eval "db.punchout.find().pretty()"

# See the network requests
mongosh punchout --eval "db.network_requests.find().pretty()"
```

**Step 5: Understand the Flow**
- Check Gateway logs: `tail -f /tmp/punchout-gateway.log`
- Check Backend logs: `tail -f /tmp/punchout-ui-backend.log`
- See how requests flow through the system

**Step 6: Customize a Template**
- Click "Edit Payload" in Developer PunchOut
- Modify the cXML
- Execute and see what happens
- Compare with default template

### If You're a Developer

1. **Review the architecture** (see diagrams above)
2. **Explore the code**:
   - Gateway: `punchout-gateway/src/main/java/com/waters/punchout/gateway/`
   - Backend: `punchout-ui-backend/src/main/java/com/waters/punchout/`
   - Frontend: `punchout-ui-frontend/src/`
3. **Understand the data flow** (see sequence diagram)
4. **Read the optimization docs**: [CODE_OPTIMIZATION_SUMMARY.md](CODE_OPTIMIZATION_SUMMARY.md)
5. **Check the API endpoints** (see README.md)

### If You're QA

1. **Run E2E tests** via Postman (see [TESTING_GUIDE.md](TESTING_GUIDE.md))
2. **Use Developer PunchOut** page for manual testing
3. **Filter sessions** by environment
4. **Compare results** across environments
5. **Export session data** for reports

## 🔐 Security Features

### Current (Development)
- Mock authentication (UUID tokens)
- In-memory token storage
- CORS allows all origins
- No real secrets

### Production-Ready Features
- Custom exception handling (no stack traces exposed)
- XML-escaped error messages (prevent injection)
- Secure error responses (generic messages)
- Request/response timeouts (prevent DoS)
- Actuator endpoints for monitoring

### Future Production Enhancements
- OAuth2/JWT authentication
- Secrets in vault
- HTTPS/TLS
- Rate limiting
- API keys
- Request signing

## 🚀 Performance Characteristics

### Request Timeouts
- **Connection Timeout**: 3 seconds
- **Response Timeout**: 10 seconds
- **Read/Write Timeout**: 10 seconds

### Typical Request Times
- **INBOUND cXML**: 100-200ms
- **Token Request**: 20-50ms
- **Catalog Request**: 10-30ms
- **Total Flow**: 150-300ms

### Database Performance
- MongoDB indexed on `sessionKey`
- Async request logging (non-blocking)
- Pagination support (future)

## 🎯 Project Goals

1. **Simplify Testing** - Make PunchOut testing as easy as clicking a button
2. **Full Visibility** - Log everything for debugging
3. **Multi-Environment** - Test safely without affecting production
4. **Customization** - Support different customers and formats
5. **Developer Experience** - Modern UI, fast feedback, clear errors
6. **Maintainability** - Clean code, proper architecture, good docs

## 📈 Roadmap

### Completed ✅
- Multi-service architecture
- Network request logging
- Session management
- Developer PunchOut interface
- cXML template system
- E2E testing
- Code optimization
- Modern home page

### In Progress 🔄
- Template management UI
- Advanced filtering
- Session analytics

### Planned 📋
- Real auth service integration
- Production deployment
- Monitoring dashboards
- Alert system
- Automated testing
- Performance metrics

## 💼 Business Value

### Time Savings
- **Before**: 30-60 minutes to manually test PunchOut
- **After**: 2-3 minutes with this platform
- **Savings**: 90%+ time reduction

### Quality Improvements
- Catch issues before production
- Full audit trail
- Easy reproduction of bugs
- Compare customer integrations

### Customer Onboarding
- Faster customer setup
- Test customer cXML formats
- Validate integrations
- Smoother go-live

## 🤝 Team Roles

### Who Uses This Platform?

**Backend Developers:**
- Test Gateway integration code
- Debug service communication
- Verify logging works
- Test error handling

**Frontend Developers:**
- Test UI integration
- Verify API responses
- Test user workflows
- Debug display issues

**QA Engineers:**
- Execute test cases
- Verify all environments
- Document test results
- Regression testing

**DevOps:**
- Monitor service health
- Check logs
- Deploy updates
- Configure environments

**Product Owners:**
- View session analytics
- Understand usage patterns
- Plan improvements

## 📚 Additional Resources

- **[README.md](README.md)** - Main documentation (this file)
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Complete testing instructions
- **[CODE_OPTIMIZATION_SUMMARY.md](CODE_OPTIMIZATION_SUMMARY.md)** - Recent improvements
- **[CXML_TEMPLATE_SYSTEM.md](CXML_TEMPLATE_SYSTEM.md)** - Template management
- **[DEVELOPER_PUNCHOUT_ENHANCED.md](DEVELOPER_PUNCHOUT_ENHANCED.md)** - Developer interface guide

## ❓ FAQ

**Q: Why do we need a mock service?**
A: To test without calling real external services. Mock Service simulates auth and catalog services.

**Q: What's the difference between Gateway and UI Backend?**
A: Gateway handles cXML protocol (port 9090). UI Backend serves REST APIs for the frontend (port 8080).

**Q: Why MongoDB instead of SQL?**
A: Flexible schema for network requests, easy to store full payloads, good for logging use case.

**Q: Can I test in production?**
A: Yes, but use PROD environment carefully. All sessions are logged.

**Q: How do I add a new customer?**
A: Add to the CUSTOMERS array in `punchout-ui-frontend/src/app/developer/punchout/page.tsx` and create templates in MongoDB.

**Q: Where are templates stored?**
A: MongoDB `cxml_templates` collection. Can be edited via API or MongoDB directly.

**Q: How do I see what went wrong?**
A: Go to session dashboard, click on the failed network request, see full error response.

## 🎉 Summary

This platform provides:
- ✅ **Easy PunchOut testing** via web interface
- ✅ **Full request logging** for debugging
- ✅ **Multi-environment support** (DEV/STAGE/PROD/S4-DEV)
- ✅ **Custom templates** per customer per environment
- ✅ **Modern UI** with Next.js and Tailwind
- ✅ **Robust backend** with Spring Boot and MongoDB
- ✅ **Complete visibility** into PunchOut flows
- ✅ **Professional documentation** for easy onboarding

**Built to make PunchOut testing fast, visible, and reliable!** 🚀

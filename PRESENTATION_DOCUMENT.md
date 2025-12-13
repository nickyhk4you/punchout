# Waters PunchOut Platform
## Complete Project Presentation Document

---

# 🎯 Executive Summary

The **Waters PunchOut Platform** is an enterprise-grade B2B e-commerce integration solution that enables seamless catalog shopping experiences between Waters Corporation and its procurement customers. The platform supports major procurement systems including **SAP Ariba**, **Coupa**, and **Oracle iProcurement**.

### Key Value Propositions

| For Business | For IT/Operations |
|--------------|-------------------|
| ✅ Faster customer onboarding | ✅ Complete request visibility |
| ✅ Multi-platform support | ✅ Real-time monitoring |
| ✅ Seamless procurement integration | ✅ Environment isolation |
| ✅ Reduced manual errors | ✅ Enterprise-grade security |

---

# 📖 What is PunchOut?

## Simple Explanation (Non-Technical)

**PunchOut** is like an "embedded shopping experience" - imagine shopping on a supplier's website, but your cart automatically goes back to your company's purchasing system.

### Real-World Example

```
Without PunchOut:                      With PunchOut:
──────────────────                     ─────────────────
1. Browse Waters website               1. Click "Shop Waters" in SAP
2. Add items to cart                   2. Browse Waters catalog
3. Copy items manually                 3. Add items to cart
4. Enter into SAP system               4. Click "Return to SAP"
5. Risk of errors                      5. Cart appears in SAP automatically
                                       6. No manual entry needed!
```

## Technical Explanation

PunchOut is a **cXML-based B2B protocol** that enables:

1. **Setup Request** - Buyer's procurement system sends a cXML request to supplier
2. **Catalog Session** - Supplier returns a URL to browse their catalog
3. **Cart Return** - Shopping cart data is sent back as cXML to buyer's system
4. **Order Processing** - Buyer creates PO from the returned cart

---

# 🏗️ System Architecture

## High-Level Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Customer Procurement Systems                      │
│   ┌──────────────┐   ┌──────────────┐   ┌──────────────────────────┐   │
│   │  SAP Ariba   │   │    Coupa     │   │   Oracle iProcurement    │   │
│   └──────┬───────┘   └──────┬───────┘   └────────────┬─────────────┘   │
└──────────┼──────────────────┼────────────────────────┼─────────────────┘
           │                  │                        │
           └──────────────────┼────────────────────────┘
                              │ cXML/XML
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        WATERS PUNCHOUT PLATFORM                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      Gateway Service (Port 9090)                   │  │
│  │  • cXML Processing           • Flexible Conversion Engine          │  │
│  │  • Multi-Platform Support    • Network Request Logging             │  │
│  │  • Session Management        • Circuit Breaker & Retry             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                              │                                           │
│              ┌───────────────┼───────────────┐                          │
│              ▼               ▼               ▼                          │
│  ┌──────────────────┐ ┌──────────────┐ ┌──────────────────────────┐   │
│  │   Auth Service   │ │ Mule/Catalog │ │   MongoDB (Data Store)   │   │
│  │   (JWT Tokens)   │ │   Service    │ │  • Sessions • Orders     │   │
│  └──────────────────┘ └──────────────┘ │  • Invoices • Templates  │   │
│                                         │  • Network Requests      │   │
│  ┌───────────────────────────────────┐  └──────────────────────────┘   │
│  │        UI Backend (Port 8080)      │                                 │
│  │  • REST APIs    • Session Viewer   │                                 │
│  │  • Order Mgmt   • Invoice Mgmt     │                                 │
│  └───────────────────────────────────┘                                  │
│                              ▲                                           │
└──────────────────────────────┼───────────────────────────────────────────┘
                              │ REST/JSON
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     Web UI (Next.js - Port 3000)                         │
│  • Dashboard & Analytics    • Session Management    • Developer Tools   │
│  • Order Management         • Invoice Management    • Network Viewer    │
└─────────────────────────────────────────────────────────────────────────┘
```

## Module Structure

| Module | Purpose | Port |
|--------|---------|------|
| **punchout-gateway** | Core cXML processing, orchestration | 9090 |
| **punchout-ui-backend** | REST APIs for web UI | 8080 |
| **punchout-ui-frontend** | Modern web interface | 3000 |
| **punchout-mock-service** | Testing simulation | 8082 |
| **punchout-common** | Shared models and utilities | - |
| **punchout-order** | Order processing logic | - |
| **punchout-invoice** | Invoice processing logic | - |

---

# ⚡ Key Features

## 1. Multi-Platform Support

The platform supports all major procurement systems with dedicated converters:

| Platform | Converter | Detection Method |
|----------|-----------|------------------|
| **SAP Ariba** | AribaV1Converter | `fromDomain: AribaNetworkUserId` |
| **Coupa** | CoupaV1Converter | `UserAgent: "Coupa"` |
| **Oracle iProcurement** | OracleV1Converter | `UserAgent: "Oracle"` |
| **Generic cXML** | DefaultV1Converter | Fallback |
| Custom Customers | AcmeV1, TechCorpV2, GlobalV1 | Identity patterns |

### How It Works

```
Incoming cXML Request
        │
        ▼
┌───────────────────────┐
│   Dialect Detector    │  ◄── Identifies platform (Ariba/Coupa/Oracle)
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│  Customer Resolver    │  ◄── Matches customer configuration
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│  Converter Registry   │  ◄── Selects appropriate converter
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│    Base Converter     │
│  ├── buildCommon()    │  ◄── Extract standard fields
│  ├── customize()      │  ◄── Platform-specific logic
│  └── validate()       │  ◄── Platform validation
└───────────────────────┘
```

## 2. Complete Network Request Logging

Every HTTP request is captured for full observability:

| Direction | What's Logged |
|-----------|---------------|
| **INBOUND** | Customer → Gateway (cXML requests) |
| **OUTBOUND** | Gateway → Auth Service (token requests) |
| **OUTBOUND** | Gateway → Mule/Catalog Service |

### Data Captured per Request

- Request ID & Session Key
- HTTP Method, URL, Headers
- Complete Request Body
- Complete Response Body
- Response Headers
- Status Code
- Duration (ms)
- Success/Failure Flag
- Error Messages

## 3. Customer Onboarding System

Self-service customer configuration without code changes:

```
┌─────────────────────────────────────────────────────────────────┐
│                    Customer Onboarding                           │
├─────────────────────────────────────────────────────────────────┤
│  Customer Name    │  Acme Corporation                           │
│  Network          │  SAP Ariba                                   │
│  Environment      │  PRODUCTION                                  │
│  Sample cXML      │  [Uploaded sample request]                   │
│  Target JSON      │  [Configured JSON mapping]                   │
│  Field Mappings   │  cXML field → JSON field                     │
│  Status           │  DEPLOYED ✓                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Benefits:**
- Add new customers without code deployment
- Environment-specific configurations
- Visual mapping of cXML to JSON
- Deploy/undeploy customers dynamically

## 4. Environment Configuration Management

Centralized configuration per environment:

| Environment | Auth Service | Mule Service | Status |
|-------------|--------------|--------------|--------|
| DEV | dev-auth.waters.com | dev-mule.waters.com | ✅ |
| STAGE | stage-auth.waters.com | stage-mule.waters.com | ✅ |
| PROD | auth.waters.com | mule.waters.com | ✅ |
| S4-DEV | s4-auth.waters.com | s4-mule.waters.com | ✅ |

**Stored in MongoDB:**
- Auth service URLs & credentials
- Mule service URLs
- Timeout configurations
- Health check endpoints

## 5. Order & Invoice Management

### Order Flow
```
cXML Order Request → Parse Order → Get Auth Token → Send to Mule
        │
        ▼
┌─────────────────────────────────────────────────────┐
│  Order Document                                      │
│  • Order ID (Idempotent - SHA-256 based)            │
│  • Session Key                                       │
│  • Customer Info                                     │
│  • Line Items (SKU, Qty, Price)                     │
│  • Addresses (Ship To, Bill To)                     │
│  • Status (PENDING → CONFIRMED → COMPLETED)         │
│  • Mule Order ID                                     │
└─────────────────────────────────────────────────────┘
```

### Invoice Flow
```
Invoice Received → Store in MongoDB → Track Status → Generate PDF
        │
        ▼
┌─────────────────────────────────────────────────────┐
│  Invoice Document                                    │
│  • Invoice Number                                    │
│  • PO Number                                         │
│  • Line Items with Totals                           │
│  • Tax, Shipping, Subtotal                          │
│  • Status (RECEIVED → CONFIRMED → PAID)             │
│  • PDF Download Available                            │
└─────────────────────────────────────────────────────┘
```

## 6. Resilience & Reliability

### Circuit Breaker Pattern

```
┌─────────────────┐   Too many failures   ┌─────────────────┐
│     CLOSED      │ ─────────────────────▶│      OPEN       │
│  (Normal flow)  │                       │ (Fail fast)     │
└─────────────────┘                       └────────┬────────┘
        ▲                                          │
        │                                          │ Wait duration
        │                                          ▼
        │                              ┌─────────────────┐
        └─────── Test successful ──────│   HALF-OPEN    │
                                       │  (Test traffic) │
                                       └─────────────────┘
```

**Configuration:**
- Sliding window: 10 calls
- Failure threshold: 50%
- Wait in open state: 30 seconds
- Retry: 3 attempts with exponential backoff

### Token Caching

- Auth tokens cached with Caffeine cache
- 5-minute TTL
- Reduces auth service load by ~90%
- Automatic refresh on expiry

## 7. Security Features

| Feature | Implementation |
|---------|----------------|
| **JWT Authentication** | HS256 signed tokens |
| **Password Encryption** | Jasypt PBEWithMD5AndDES |
| **Secret Masking** | Automatic in logs |
| **Header Masking** | Authorization headers masked |
| **API Key Support** | Per-customer API keys |
| **Audit Logging** | Security events tracked |

---

# 🔄 Complete PunchOut Flow

## Step-by-Step Process

```
┌───────────────────────────────────────────────────────────────────────┐
│ Step 1: Customer clicks "Shop Waters" in their procurement system     │
└───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────┐
│ Step 2: Procurement system sends cXML PunchOutSetupRequest            │
│         POST /punchout/setup                                          │
│                                                                        │
│ Contains:                                                              │
│   • BuyerCookie (session identifier)                                  │
│   • Credentials (from/to/sender)                                      │
│   • Contact information                                               │
│   • Cart return URL                                                   │
│   • Extrinsics (platform-specific data)                              │
└───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────┐
│ Step 3: Gateway processes the request                                  │
│                                                                        │
│   a) Parse cXML using Flexible Conversion Engine                      │
│   b) Detect customer & platform (Ariba/Coupa/Oracle)                  │
│   c) Log INBOUND request to MongoDB                                   │
│   d) Get authentication token from Auth Service                       │
│   e) Log OUTBOUND auth request                                        │
│   f) Call Mule Service with customer payload                          │
│   g) Log OUTBOUND Mule request                                        │
│   h) Save session to MongoDB                                          │
└───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────┐
│ Step 4: Return cXML Response with Catalog URL                         │
│                                                                        │
│ <?xml version="1.0"?>                                                 │
│ <cXML>                                                                 │
│   <Response>                                                           │
│     <Status code="200" text="success"/>                               │
│     <PunchOutSetupResponse>                                           │
│       <StartPage>                                                      │
│         <URL>https://catalog.waters.com?session=ABC123</URL>          │
│       </StartPage>                                                     │
│     </PunchOutSetupResponse>                                          │
│   </Response>                                                          │
│ </cXML>                                                                │
└───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────┐
│ Step 5: Customer browses Waters catalog                               │
│         Adds items to cart                                             │
│         Clicks "Return to procurement system"                         │
└───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────┐
│ Step 6: Cart data sent back to procurement system                     │
│         Customer creates Purchase Order                                │
│         Order sent via cXML OrderRequest                              │
└───────────────────────────────────────────────────────────────────────┘
```

---

# 🖥️ User Interface

## Dashboard

Modern web interface with:

- **Statistics Cards** - Session counts, success rates, active environments
- **Quick Actions** - Execute PunchOut, view sessions, manage templates
- **Recent Activity** - Latest sessions and orders
- **Environment Status** - Health of all connected services

## Session Management

| Column | Description |
|--------|-------------|
| Session Key | Unique identifier (clickable) |
| Environment | DEV / STAGE / PROD / S4-DEV |
| Operation | CREATE / EDIT / INSPECT |
| Status | Success/Failed badge |
| Customer | Customer name |
| Timestamp | When session was created |

## Network Request Viewer

For each session, view:

- Timeline of all requests (INBOUND + OUTBOUND)
- Full request/response payloads
- Headers (request and response)
- Status codes and durations
- Error messages (if any)

## Order & Invoice Management

- List all orders with filtering
- Order details with line items
- Invoice list with status badges
- PDF download for invoices
- Network request tracking

---

# 🔧 Technology Stack

## Backend

| Technology | Purpose |
|------------|---------|
| **Java 11** | Runtime |
| **Spring Boot 2.7.18** | Framework |
| **Spring WebFlux** | Reactive HTTP client |
| **Spring Data MongoDB** | Database access |
| **Resilience4j** | Circuit breaker, retry |
| **Micrometer** | Metrics |
| **Caffeine** | In-memory caching |
| **Jasypt** | Password encryption |
| **Lombok** | Reduce boilerplate |
| **Jackson** | JSON/XML processing |

## Frontend

| Technology | Purpose |
|------------|---------|
| **Next.js 14** | React framework |
| **TypeScript** | Type safety |
| **Tailwind CSS** | Styling |
| **Axios** | HTTP client |

## Database

| Technology | Purpose |
|------------|---------|
| **MongoDB** | Primary data store |

### MongoDB Collections

| Collection | Data Stored |
|------------|-------------|
| `punchout` | PunchOut sessions |
| `orders` | Order documents |
| `invoices` | Invoice documents |
| `network_requests` | All HTTP requests |
| `cxml_templates` | cXML templates |
| `customer_onboarding` | Customer configurations |
| `environment_configs` | Environment settings |
| `users` | User accounts |
| `api_keys` | API keys |
| `security_audit_log` | Security events |

## Infrastructure

| Technology | Purpose |
|------------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Local orchestration |
| **Maven** | Build tool |

---

# 📊 Metrics & Monitoring

## Application Metrics (Micrometer)

| Metric | Description |
|--------|-------------|
| `punchout.auth.request` | Auth service latency |
| `punchout.mule.request` | Mule service latency |
| `punchout.sessions.total` | Session counts by environment |
| `punchout.orders.total` | Order counts |
| `punchout.cache.access` | Cache hit/miss rates |
| `punchout.circuitbreaker.state` | Circuit breaker events |

## Health Endpoints

| Endpoint | Service |
|----------|---------|
| `GET /actuator/health` | Gateway health |
| `GET /actuator/metrics` | All metrics |
| `GET /api/v1/service-health` | Mock service |

---

# 🌍 Environment Support

| Environment | Purpose | Isolation |
|-------------|---------|-----------|
| **LOCAL** | Developer workstation | Full mock services |
| **DEV** | Development testing | Development credentials |
| **STAGE** | QA & UAT | Staging services |
| **S4-DEV** | SAP S/4HANA testing | S/4HANA integration |
| **PROD** | Production | Live services |

**Each environment has:**
- Dedicated Auth service URL
- Dedicated Mule service URL
- Separate credentials
- Independent configuration

---

# 🔐 Security Architecture

## Authentication Flow

```
┌──────────────┐    1. Session + Credentials    ┌──────────────────┐
│   Gateway    │ ─────────────────────────────▶ │   Auth Service   │
└──────────────┘                                 └────────┬─────────┘
       ▲                                                  │
       │              2. JWT Token (wuser_key)            │
       └──────────────────────────────────────────────────┘
       │
       │  3. Bearer Token in Authorization header
       ▼
┌──────────────────┐
│   Mule Service   │  ◄── Validates JWT, returns catalog URL
└──────────────────┘
```

## Data Protection

- **Passwords**: Encrypted with Jasypt (PBEWithMD5AndDES)
- **Headers**: Authorization values masked in logs
- **Secrets**: Automatically masked in request/response bodies
- **Audit Trail**: All security events logged

---

# 📈 Business Benefits

## Quantified Value

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Manual PunchOut test time | 30-60 min | 2-3 min | **90%+ reduction** |
| Customer onboarding time | Days | Hours | **90%+ reduction** |
| Error visibility | Partial | Complete | **100%** |
| Platform support | Limited | Ariba/Coupa/Oracle | **3x more** |

## Stakeholder Benefits

### For Business/Sales
- Faster customer onboarding
- Support major procurement platforms
- Reduced integration errors
- Better customer satisfaction

### For IT/Development
- Complete request visibility
- Easy debugging with full logs
- Environment isolation
- Modern tech stack

### For Operations
- Real-time monitoring
- Health checks
- Audit trails
- Performance metrics

### For QA
- Automated test execution
- Multi-environment testing
- Complete test evidence
- Regression testing

---

# 🛠️ API Reference

## Gateway API (Port 9090)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/punchout/setup` | PunchOut setup request |
| POST | `/punchout/order` | Order message |
| GET | `/punchout/health` | Health check |
| GET | `/api/datastore/*` | Customer datastore |
| GET | `/api/onboarding/*` | Customer onboarding |

## UI Backend API (Port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/sessions` | List all sessions |
| GET | `/api/v1/sessions/{key}` | Session details |
| GET | `/api/v1/sessions/{key}/network-requests` | Session requests |
| GET | `/api/v1/orders` | List orders |
| GET | `/api/v1/orders/{id}` | Order details |
| GET | `/api/v1/invoices` | List invoices |
| GET | `/api/v1/invoices/{number}/pdf` | Download invoice PDF |
| GET | `/api/v1/cxml-templates/*` | cXML templates |

---

# 🚀 Deployment

## Quick Start

```bash
# 1. Start all backend services
./start-all-services.sh

# 2. Start frontend
cd punchout-ui-frontend
npm run dev

# 3. Access the application
open http://localhost:3000
```

## Docker Deployment

```bash
# Build images
./build-docker-images.sh

# Deploy with Docker Compose
./deploy-local-docker.sh
```

## Service Ports

| Service | Port |
|---------|------|
| Frontend | 3000 |
| UI Backend | 8080 |
| Gateway | 9090 |
| Mock Service | 8082 |
| MongoDB | 27017 |

---

# 📋 Roadmap

## Completed ✅

- [x] Multi-platform support (Ariba, Coupa, Oracle)
- [x] Flexible conversion engine with 7 converters
- [x] Complete network request logging
- [x] Customer onboarding system
- [x] Environment configuration management
- [x] Order processing with idempotency
- [x] Invoice management with PDF download
- [x] JWT authentication with caching
- [x] Circuit breaker and retry patterns
- [x] Metrics collection
- [x] Modern web UI

## In Progress 🔄

- [ ] Enhanced analytics dashboard
- [ ] Email notifications
- [ ] Bulk operations

## Planned 📋

- [ ] OAuth2/SSO integration
- [ ] Advanced search capabilities
- [ ] Scheduled health checks
- [ ] Alert system
- [ ] Performance dashboards

---

# ❓ FAQ

**Q: What procurement platforms are supported?**
A: SAP Ariba, Coupa, Oracle iProcurement, and generic cXML.

**Q: How do I add a new customer?**
A: Use the Customer Onboarding API or add configuration to `application.yml`.

**Q: Where are logs stored?**
A: All network requests are stored in MongoDB `network_requests` collection.

**Q: Can I test without affecting production?**
A: Yes, each environment is isolated with its own configuration.

**Q: How are passwords protected?**
A: Encrypted with Jasypt and masked in all logs.

**Q: What happens if Auth service is down?**
A: Circuit breaker activates after failures, returns cached token if available.

---

# 📞 Support & Resources

## Documentation

- [README.md](README.md) - Quick start guide
- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Testing instructions
- [PUNCHOUT_ARCHITECTURE.md](PUNCHOUT_ARCHITECTURE.md) - Architecture details

## Health Check URLs

- Gateway: http://localhost:9090/actuator/health
- Backend: http://localhost:8080/actuator/health
- Mock: http://localhost:8082/api/v1/service-health

---

# 🎓 Summary

The **Waters PunchOut Platform** is a comprehensive, enterprise-grade solution for B2B e-commerce integration:

| Capability | Status |
|------------|--------|
| Multi-Platform Support | ✅ Ariba, Coupa, Oracle |
| Complete Observability | ✅ All requests logged |
| Self-Service Onboarding | ✅ No-code customer setup |
| Enterprise Security | ✅ JWT, encryption, audit |
| High Availability | ✅ Circuit breaker, retry, cache |
| Modern UI | ✅ Next.js, responsive |
| Production Ready | ✅ Metrics, health checks |

---

**Built with Spring Boot, Next.js, and MongoDB**

*© 2025 Waters Corporation. All rights reserved.*

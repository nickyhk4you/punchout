# 🚀 PunchOut Full Stack Application Guide

Complete guide to running the PunchOut Session Manager - Backend + Frontend

---

## 📋 System Overview

**Backend**: Spring Boot REST API with H2 in-memory database  
**Frontend**: Next.js 14 with TypeScript and Tailwind CSS  
**Integration**: RESTful API communication

---

## ⚡ Quick Start - Run Both Applications

### Terminal 1: Start Backend

```bash
cd punchout-ui-backend
mvn spring-boot:run
```

**Backend Status:**
- ✅ Running on http://localhost:8080
- ✅ H2 Database initialized with mock data
- ✅ REST API available at http://localhost:8080/api
- ✅ H2 Console at http://localhost:8080/h2-console

**Logs will show:**
```
Initializing mock data...
Created 5 PunchOut sessions
Created 4 Order objects
Created 9 Gateway requests
Mock data initialization completed!
```

### Terminal 2: Start Frontend

```bash
cd punchout-ui-frontend
npm install  # First time only
npm run dev
```

**Frontend Status:**
- ✅ Running on http://localhost:3000
- ✅ Connected to backend API
- ✅ Dashboard and pages ready

---

## 🌐 Access Points

| Component | URL | Description |
|-----------|-----|-------------|
| **Frontend Dashboard** | http://localhost:3000 | Main UI - Statistics and recent sessions |
| **Sessions List** | http://localhost:3000/sessions | Browse and filter all sessions |
| **Session Details** | http://localhost:3000/sessions/SESSION-2025-001 | Detailed view of specific session |
| **Backend API** | http://localhost:8080/api | REST API endpoints |
| **H2 Database Console** | http://localhost:8080/h2-console | Direct database access |

---

## 📊 Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Browser (Port 3000)                     │
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐  │
│  │ Dashboard  │  │  Sessions  │  │  Session Details     │  │
│  │   Page     │  │    List    │  │      Page            │  │
│  └────────────┘  └────────────┘  └──────────────────────┘  │
│                          │                                   │
│                    Next.js App                               │
└──────────────────────────┼──────────────────────────────────┘
                           │
                    HTTP REST API
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                 Spring Boot Backend (Port 8080)             │
│                                                              │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────────┐  │
│  │ Controllers  │  │  Services   │  │  Repositories    │  │
│  └──────────────┘  └─────────────┘  └──────────────────┘  │
│                          │                                   │
└──────────────────────────┼──────────────────────────────────┘
                           │
                    JPA / Hibernate
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   H2 In-Memory Database                     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │ PunchOut     │  │ Order        │  │ Gateway         │  │
│  │ Session      │  │ Object       │  │ Request         │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔌 API Integration Flow

### Example: Loading Sessions List

1. **Frontend** - User navigates to `/sessions`
2. **React Component** - Calls `sessionAPI.getAllSessions()`
3. **Axios HTTP Client** - Sends GET request to `http://localhost:8080/api/punchout-sessions`
4. **Spring Boot Controller** - `PunchOutSessionController.getAllSessions()`
5. **Service Layer** - `PunchOutSessionService.getAllSessions()`
6. **JPA Repository** - Queries H2 database
7. **Database** - Returns session records
8. **Response Flow** - Data flows back through layers
9. **Frontend** - Receives JSON, updates state, re-renders UI

---

## 📁 Project Structure

```
punchout/
├── punchout-ui-backend/              # Spring Boot Backend
│   ├── src/main/java/com/waters/punchout/
│   │   ├── entity/                   # JPA Entities
│   │   │   ├── PunchOutSession.java
│   │   │   ├── OrderObject.java
│   │   │   └── GatewayRequest.java
│   │   ├── repository/               # Data Access
│   │   │   ├── PunchOutSessionRepository.java
│   │   │   ├── OrderObjectRepository.java
│   │   │   └── GatewayRequestRepository.java
│   │   ├── service/                  # Business Logic
│   │   │   ├── PunchOutSessionService.java
│   │   │   ├── OrderObjectService.java
│   │   │   └── GatewayRequestService.java
│   │   ├── controller/               # REST Controllers
│   │   │   ├── PunchOutSessionController.java
│   │   │   ├── OrderObjectController.java
│   │   │   └── GatewayRequestController.java
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── mapper/                   # Entity-DTO Mappers
│   │   ├── exception/                # Exception Handling
│   │   └── config/                   # Configuration & Data Init
│   ├── src/main/resources/
│   │   ├── application.yml           # H2 Database Config
│   │   ├── application-dev.yml       # Development Profile
│   │   └── application-postgres.yml  # PostgreSQL Profile
│   └── pom.xml                       # Maven Dependencies
│
├── punchout-ui-frontend/             # Next.js Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── page.tsx             # Dashboard
│   │   │   ├── sessions/
│   │   │   │   ├── page.tsx         # Sessions List
│   │   │   │   └── [sessionKey]/
│   │   │   │       └── page.tsx     # Session Details
│   │   │   └── layout.tsx           # Root Layout
│   │   ├── lib/
│   │   │   └── api.ts               # API Client
│   │   └── types/
│   │       └── index.ts             # TypeScript Interfaces
│   ├── .env.local                    # Environment Variables
│   ├── next.config.js                # Next.js Config
│   └── package.json                  # Dependencies
│
├── FULL_STACK_GUIDE.md              # This file
└── README.md                         # Project overview
```

---

## 🎯 Features Implemented

### Backend (Spring Boot)

✅ **Data Models**
- PunchOutSession entity with validation
- OrderObject entity
- GatewayRequest entity
- Database indexes on key fields

✅ **REST API Endpoints**
- GET `/api/punchout-sessions` - List all sessions
- GET `/api/punchout-sessions/{sessionKey}` - Get session details
- POST `/api/punchout-sessions` - Create session
- PUT `/api/punchout-sessions/{sessionKey}` - Update session
- GET `/api/punchout-sessions/{sessionKey}/order-object` - Get order
- POST `/api/punchout-sessions/{sessionKey}/order-object` - Create order
- GET `/api/punchout-sessions/{sessionKey}/gateway-requests` - Get requests
- POST `/api/gateway-requests` - Create request

✅ **Business Logic**
- Session filtering by operation, environment, route, date
- Validation for mandatory fields
- Transaction management
- SLF4J logging

✅ **Exception Handling**
- Global exception handler
- Custom exceptions (SessionNotFoundException, InvalidDataException)
- Consistent error responses

✅ **Mock Data**
- 5 realistic PunchOut sessions
- 4 order objects
- 9 gateway requests
- Auto-loaded on startup

✅ **Testing**
- Unit tests for services
- Integration tests for controllers
- Mockito for mocking

### Frontend (Next.js)

✅ **Pages**
- Dashboard with statistics
- Sessions list with filtering
- Session details with full information

✅ **Data Integration**
- TypeScript API client
- Axios HTTP client
- Type-safe interfaces
- Error handling

✅ **UI Features**
- Responsive design (mobile, tablet, desktop)
- Loading states
- Color-coded badges
- Data tables
- Filter controls
- Currency and date formatting

✅ **User Experience**
- Smooth transitions
- Hover effects
- Loading spinners
- Error messages
- Breadcrumb navigation

---

## 🧪 Testing the Integration

### 1. Verify Backend is Running

```bash
curl http://localhost:8080/api/punchout-sessions
```

Expected: JSON array with 5 sessions

### 2. Test Frontend Connection

Open http://localhost:3000 in browser

Expected: Dashboard showing statistics

### 3. Test Filtering

1. Go to http://localhost:3000/sessions
2. Select "PRODUCTION" from Environment dropdown
3. Click filter

Expected: Only production sessions displayed

### 4. Test Session Details

1. Click "View Details" on any session
2. Scroll through sections

Expected: Session info, order object, gateway requests

### 5. Test API Directly

```bash
# Get all sessions
curl http://localhost:8080/api/punchout-sessions

# Get specific session
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001

# Get order object
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001/order-object

# Get gateway requests
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001/gateway-requests
```

---

## 📊 Mock Data Details

### Sessions Created

| Session Key | Company | Environment | Operation | Order Value |
|-------------|---------|-------------|-----------|-------------|
| SESSION-2025-001 | ACME Corp | PRODUCTION | CREATE | $2,500.00 |
| SESSION-2025-002 | Globex Corp | STAGING | EDIT | $4,750.50 |
| SESSION-2025-003 | Initech | DEVELOPMENT | INSPECT | $1,200.75 |
| SESSION-2025-004 | Umbrella Corp | PRODUCTION | CREATE | $8,900.00 |
| SESSION-2025-005 | Cyberdyne | PRODUCTION | CREATE | $15,500.25 |

**Total Order Value**: $32,851.50

---

## 🔧 Configuration

### Backend Configuration (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:punchoutdb
    username: sa
    password: 
  h2:
    console:
      enabled: true
server:
  port: 8080
```

### Frontend Configuration (`.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## 🛠️ Development Workflow

### Making Changes to Backend

1. Edit Java files in `punchout-ui-backend/src/`
2. Save file
3. Spring Boot auto-reloads (or restart: `Ctrl+C` then `mvn spring-boot:run`)
4. Test API with curl or frontend

### Making Changes to Frontend

1. Edit TypeScript/React files in `punchout-ui-frontend/src/`
2. Save file
3. Next.js hot-reloads automatically
4. Check browser (changes appear instantly)

---

## 🐛 Troubleshooting

### Backend Won't Start

**Problem**: Port 8080 already in use  
**Solution**: Kill process on port 8080 or change port in `application.yml`

```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Frontend Can't Connect to Backend

**Problem**: CORS or connection errors  
**Solution**: 
1. Verify backend is running on port 8080
2. Check `.env.local` has correct URL
3. Clear browser cache

### Mock Data Not Loading

**Problem**: Database empty  
**Solution**: Check backend logs for "Initializing mock data..." message

### Build Errors

**Backend**:
```bash
mvn clean install
```

**Frontend**:
```bash
rm -rf .next node_modules
npm install
```

---

## 📚 Additional Resources

### Backend Documentation
- [Backend QUICKSTART.md](punchout-ui-backend/QUICKSTART.md)
- [Backend README.md](punchout-ui-backend/README.md)

### Frontend Documentation
- [Frontend QUICKSTART_FRONTEND.md](punchout-ui-frontend/QUICKSTART_FRONTEND.md)
- [Frontend FRONTEND_README.md](punchout-ui-frontend/FRONTEND_README.md)

### API Documentation
- Swagger/OpenAPI (TODO: Add later)
- Postman Collection (TODO: Add later)

---

## 🎯 Next Steps

### Enhancements
- [ ] Add authentication/authorization
- [ ] Add session creation form in UI
- [ ] Add session editing in UI
- [ ] Add data export (CSV, Excel)
- [ ] Add charts and visualizations
- [ ] Add WebSocket for real-time updates
- [ ] Add pagination for large datasets
- [ ] Add advanced search
- [ ] Add user management
- [ ] Deploy to production

### Production Deployment
- [ ] Switch to PostgreSQL database
- [ ] Add Spring Security
- [ ] Add HTTPS
- [ ] Add Docker containers
- [ ] Add CI/CD pipeline
- [ ] Add monitoring and logging
- [ ] Add backup strategy

---

## ✅ Success Checklist

- [x] Backend running on port 8080
- [x] Frontend running on port 3000
- [x] Mock data loaded (5 sessions, 4 orders, 9 requests)
- [x] Dashboard displays statistics
- [x] Sessions list shows all sessions
- [x] Filters work correctly
- [x] Session details page loads
- [x] API integration working
- [x] No console errors
- [x] Responsive design works on mobile

---

**Congratulations! Your full-stack PunchOut Session Manager is ready! 🎉**

For support or questions, refer to the individual README files in each project directory.

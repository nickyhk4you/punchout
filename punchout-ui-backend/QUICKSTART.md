# Quick Start Guide - PunchOut UI Backend

## 🚀 Run in 30 Seconds!

### Step 1: Build the Project
```bash
cd /Users/nickhu/dev/java/punchout/punchout-ui-backend
mvn clean install
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

**That's it!** The application will:
- ✅ Start with H2 in-memory database (no external database needed)
- ✅ Automatically create tables
- ✅ Load mock data (5 sessions, 4 orders, 9 gateway requests)
- ✅ Be ready to use at http://localhost:8080

---

## 📊 Mock Data Loaded

The application automatically loads realistic test data:

### PunchOut Sessions (5 samples)
- `SESSION-2025-001` - ACME Corp (Production)
- `SESSION-2025-002` - Globex Corp (Staging)
- `SESSION-2025-003` - Initech (Development)
- `SESSION-2025-004` - Umbrella Corp (Production)
- `SESSION-2025-005` - Cyberdyne Systems (Production)

### Order Objects (4 samples)
Linked to sessions 001-004

### Gateway Requests (9 samples)
Multiple requests per session showing typical workflow

---

## 🔍 Test the API

### Get All Sessions
```bash
curl http://localhost:8080/api/punchout-sessions
```

### Get Specific Session
```bash
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001
```

### Get Order Object
```bash
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001/order-object
```

### Get Gateway Requests
```bash
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001/gateway-requests
```

### Filter Sessions by Environment
```bash
curl "http://localhost:8080/api/punchout-sessions?environment=PRODUCTION"
```

### Filter Sessions by Operation
```bash
curl "http://localhost:8080/api/punchout-sessions?operation=CREATE"
```

### Create New Session
```bash
curl -X POST http://localhost:8080/api/punchout-sessions \
  -H "Content-Type: application/json" \
  -d '{
    "sessionKey": "SESSION-2025-NEW",
    "operation": "CREATE",
    "contactEmail": "test@example.com",
    "routeName": "test-route",
    "environment": "DEV",
    "sessionDate": "2025-10-31T10:00:00"
  }'
```

---

## 🗄️ H2 Database Console

Access the in-memory database directly:

1. Open browser: **http://localhost:8080/h2-console**
2. Connection settings:
   - **JDBC URL**: `jdbc:h2:mem:punchoutdb`
   - **User Name**: `sa`
   - **Password**: (leave empty)
3. Click "Connect"

### Sample SQL Queries
```sql
-- View all sessions
SELECT * FROM PUNCHOUT_SESSION;

-- View sessions with orders
SELECT s.*, o.* 
FROM PUNCHOUT_SESSION s 
LEFT JOIN ORDER_OBJECT o ON s.SESSION_KEY = o.SESSION_KEY;

-- View sessions with gateway requests count
SELECT s.SESSION_KEY, s.OPERATION, COUNT(g.ID) as REQUEST_COUNT
FROM PUNCHOUT_SESSION s
LEFT JOIN GATEWAY_REQUEST g ON s.SESSION_KEY = g.SESSION_KEY
GROUP BY s.SESSION_KEY, s.OPERATION;
```

---

## 📋 Application Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/punchout-sessions` | Get all sessions (supports filtering) |
| GET | `/api/punchout-sessions/{sessionKey}` | Get specific session |
| POST | `/api/punchout-sessions` | Create new session |
| PUT | `/api/punchout-sessions/{sessionKey}` | Update session |
| GET | `/api/punchout-sessions/{sessionKey}/order-object` | Get order object |
| POST | `/api/punchout-sessions/{sessionKey}/order-object` | Create/update order object |
| GET | `/api/punchout-sessions/{sessionKey}/gateway-requests` | Get gateway requests |
| POST | `/api/gateway-requests` | Create gateway request |

---

## ⚙️ Run with PostgreSQL (Optional)

If you want to use PostgreSQL instead of H2:

1. Create database:
```sql
CREATE DATABASE punchout_db;
```

2. Run with postgres profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

3. Update credentials in `src/main/resources/application-postgres.yml` if needed

---

## 🛠️ Development Tips

### Run Tests
```bash
mvn test
```

### Build JAR
```bash
mvn clean package
java -jar target/punchout-ui-backend-1.0.0.jar
```

### View Logs
The application logs session creation, API requests, and data initialization in the console.

### Stop Application
Press `Ctrl+C` in the terminal

---

## 📝 Mock Data Details

### Sample Session Data Includes:
- Various operations: CREATE, EDIT, INSPECT
- Different environments: PRODUCTION, STAGING, DEVELOPMENT
- Multiple networks: ARIBA, OCI, SAP
- Realistic order values and line items
- Different timestamps (from 5 days ago to 6 hours ago)

### Complete Workflow Example (SESSION-2025-001):
1. PunchOut Setup Request → `/punchout/setup`
2. Add to Cart Request → `/punchout/addToCart`
3. Checkout Request → `/punchout/checkout`
4. Order Object created with buyer details
5. Session completed with order value $2,500.00

---

## 🎯 Next Steps

1. **Explore the API**: Try different endpoints and filters
2. **View Data in H2 Console**: See how data is structured
3. **Test Creating Data**: Use POST endpoints to add new sessions
4. **Build Your Frontend**: Use these APIs to build a UI
5. **Switch to PostgreSQL**: When ready for production

---

## 🆘 Troubleshooting

### Port 8080 Already in Use
```bash
# Change port in application.yml or use:
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Build Errors
```bash
# Clean and rebuild:
mvn clean install
```

### Data Not Loading
- Check console logs for "Initializing mock data..."
- Verify H2 dependency is present
- Check `DataInitializer` class executed

---

**Enjoy your fully functional PunchOut Backend API! 🎉**

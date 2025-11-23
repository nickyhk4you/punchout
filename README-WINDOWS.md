# PunchOut System - Windows Quick Start

Complete setup and deployment for Windows in 5 minutes.

## 🚀 Quick Start

### Step 1: Check Prerequisites
```batch
check-prerequisites-windows.bat
```

### Step 2: Setup MongoDB
```batch
setup-mongodb-windows.bat
```

### Step 3: Import Data
```batch
import-data-windows.bat
```

### Step 4: Start Services
```batch
start-all-services-windows.bat
```

### Step 5: Access the System
- **UI**: http://localhost:8080
- **Gateway API**: http://localhost:9090
- **Health Check**: http://localhost:9090/actuator/health

---

## 📋 Prerequisites

| Software | Version | Download |
|----------|---------|----------|
| **Java JDK** | 17+ | [Adoptium.net](https://adoptium.net/) |
| **Maven** | 3.8+ | [Maven.apache.org](https://maven.apache.org/download.cgi) |
| **MongoDB** | 7.0+ | [MongoDB.com](https://www.mongodb.com/try/download/community) |
| **Git** | Latest | [Git-scm.com](https://git-scm.com/download/win) (optional) |

---

## 📁 Windows Scripts

| Script | Purpose |
|--------|---------|
| `check-prerequisites-windows.bat` | ✅ Verify all requirements installed |
| `setup-mongodb-windows.bat` | 🗄️ Setup MongoDB directories and config |
| `import-data-windows.bat` | 📥 Import sample data and create indexes |
| `start-all-services-windows.bat` | ▶️ Start all backend services |
| `stop-all-services-windows.bat` | ⏹️ Stop all running services |
| `restart-all-services-windows.bat` | 🔄 Restart all services |

---

## 🎯 Service Ports

| Service | Port | URL |
|---------|------|-----|
| Gateway | 9090 | http://localhost:9090 |
| UI Backend | 8080 | http://localhost:8080 |
| Mock Service | 8082 | http://localhost:8082 |
| MongoDB | 27017 | mongodb://localhost:27017 |

---

## 🔧 Common Commands

### Start/Stop Services
```batch
# Start all services
start-all-services-windows.bat

# Stop all services
stop-all-services-windows.bat

# Restart all services
restart-all-services-windows.bat
```

### MongoDB Management
```batch
# Start MongoDB service
net start MongoDB

# Stop MongoDB service
net stop MongoDB

# Connect to MongoDB
mongosh
use punchout
db.environment_configs.find().pretty()
```

### View Logs
```batch
# View service logs
type %TEMP%\punchout-gateway.log
type %TEMP%\punchout-ui-backend.log
type %TEMP%\punchout-mock-service.log

# Real-time log monitoring (PowerShell)
powershell Get-Content %TEMP%\punchout-gateway.log -Wait -Tail 50
```

### Check Service Status
```batch
# Check running Java processes
tasklist | findstr "java"

# Check port usage
netstat -ano | findstr ":9090"
netstat -ano | findstr ":8080"

# Health checks
curl http://localhost:9090/actuator/health
curl http://localhost:8080/api/health
```

---

## 🐛 Troubleshooting

### Port Already in Use
```batch
# Find process using port
netstat -ano | findstr ":9090"

# Kill process by PID
taskkill /F /PID <PID>
```

### MongoDB Won't Start
```batch
# Check service status
sc query MongoDB

# View MongoDB logs
type %USERPROFILE%\mongodb\logs\mongod.log

# Restart service
net stop MongoDB
net start MongoDB
```

### Service Fails to Start
```batch
# Check logs
type %TEMP%\punchout-gateway.log

# Verify Java version
java -version

# Rebuild project
mvn clean install -DskipTests
```

---

## 📚 Documentation

- **[Windows Deployment Guide](WINDOWS_DEPLOYMENT_GUIDE.md)** - Complete setup guide
- **[Testing Guide](TESTING_GUIDE.md)** - API testing and examples
- **[Optimization Complete](OPTIMIZATION_COMPLETE.md)** - Performance features
- **[Security Features](SECURITY_FEATURE_COMPLETE.md)** - Security implementation

---

## 🔒 Security Notes

### Default Encryption Password
The system uses Jasypt for password encryption. Default password: `punchout-secret-key`

**To change:**
```batch
# Set environment variable
setx JASYPT_ENCRYPTOR_PASSWORD "your-secure-password"

# Or for current session only
set JASYPT_ENCRYPTOR_PASSWORD=your-secure-password
```

---

## 📊 Features

### ✅ Implemented Optimizations
- 🔐 **Secret Masking** - Passwords/tokens masked in logs
- ⚡ **Token Caching** - 80-90% reduction in auth calls
- 🗄️ **MongoDB Indexes** - 10-100x faster queries
- 🔄 **Retry Logic** - Automatic recovery from transient failures
- 🛡️ **Circuit Breakers** - Prevents cascading failures
- 🚦 **Rate Limiting** - 100 req/s protection
- 📊 **Metrics** - Full Prometheus/Micrometer integration
- 🏥 **Health Checks** - External service monitoring
- 🔁 **Idempotent Orders** - Duplicate prevention

---

## 🌐 API Endpoints

### Gateway Service (9090)
```
POST /punchout/setup     - PunchOut setup request
POST /punchout/order     - Order submission
GET  /actuator/health    - Health check
GET  /actuator/metrics   - Metrics endpoint
GET  /actuator/prometheus - Prometheus metrics
```

### UI Backend (8080)
```
GET  /api/environment-config       - List all configs
GET  /api/customer-onboarding      - List customers
GET  /api/punchout-sessions        - List sessions
GET  /api/network-requests         - Network logs
GET  /api/health                   - Health check
```

### Mock Service (8082)
```
POST /api/v1/token      - Mock auth token
POST /api/v1/catalog    - Mock catalog response
GET  /actuator/health   - Health check
```

---

## 🎓 Next Steps

After successful deployment:

1. ✅ **Test the API** - Use Postman collection: `Punchout_API.postman_collection.json`
2. ✅ **Configure Environments** - Update environment configs via API
3. ✅ **Monitor Metrics** - Setup Prometheus + Grafana
4. ✅ **Review Security** - Update encryption passwords
5. ✅ **Production Deploy** - See `PRODUCTION_DEPLOYMENT_GUIDE.md`

---

## 💡 Tips

### Environment Variables
```batch
# Set for production
setx APP_ENVIRONMENT "prod"
setx JASYPT_ENCRYPTOR_PASSWORD "strong-password-here"

# MongoDB connection (if remote)
setx MONGO_HOST "mongodb-server"
setx MONGO_PORT "27017"
```

### Running as Windows Service
For production, install services using [WinSW](https://github.com/winsw/winsw):

```xml
<!-- punchout-gateway.xml -->
<service>
  <id>punchout-gateway</id>
  <name>PunchOut Gateway Service</name>
  <description>PunchOut Gateway Service</description>
  <executable>java</executable>
  <arguments>-jar target\punchout-gateway-1.0.0.jar</arguments>
  <logpath>C:\PunchOut\logs</logpath>
</service>
```

---

## 🆘 Support

**Common Issues:**
- MongoDB not starting → Check logs: `%USERPROFILE%\mongodb\logs\mongod.log`
- Port conflicts → Use `netstat -ano | findstr ":PORT"` to find PID
- Build failures → Run `mvn clean install -U -DskipTests`
- Service crashes → Check logs in `%TEMP%\punchout-*.log`

**Full Guide:** See [WINDOWS_DEPLOYMENT_GUIDE.md](WINDOWS_DEPLOYMENT_GUIDE.md)

---

## ✨ Summary

```batch
REM Complete Windows setup in 4 commands:
check-prerequisites-windows.bat
setup-mongodb-windows.bat
import-data-windows.bat
start-all-services-windows.bat
```

**System ready!** 🎉

Access: http://localhost:8080

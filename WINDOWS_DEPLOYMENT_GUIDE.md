# Windows Deployment Guide - PunchOut System

Complete guide to deploy and run the PunchOut system on Windows.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Installation Steps](#installation-steps)
- [Starting the System](#starting-the-system)
- [Verifying the Setup](#verifying-the-setup)
- [Troubleshooting](#troubleshooting)
- [Common Commands](#common-commands)

---

## Prerequisites

### 1. Java Development Kit (JDK) 17
**Download:** https://adoptium.net/

```batch
# Verify installation
java -version
# Should show: openjdk version "17.x.x"
```

**Add to PATH:**
1. Right-click "This PC" → Properties → Advanced System Settings
2. Click "Environment Variables"
3. Add to PATH: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x\bin`

---

### 2. Apache Maven 3.8+
**Download:** https://maven.apache.org/download.cgi

```batch
# Verify installation
mvn -version
# Should show: Apache Maven 3.8.x or higher
```

**Setup:**
1. Extract to `C:\Program Files\Apache\Maven`
2. Add to PATH: `C:\Program Files\Apache\Maven\bin`
3. Set `MAVEN_HOME` environment variable: `C:\Program Files\Apache\Maven`

---

### 3. MongoDB Community Edition 7.0+
**Download:** https://www.mongodb.com/try/download/community

**Installation Options:**

#### Option A: Run as Windows Service (Recommended)
```batch
# During installation, select:
# ✅ Install MongoDB as a Service
# ✅ Run service as Network Service user
# ✅ Install MongoDB Compass (optional GUI)
```

#### Option B: Manual Setup
```batch
# Run the setup script
setup-mongodb-windows.bat

# Then install as service (as Administrator):
mongod --config "%USERPROFILE%\mongodb\mongod.cfg" --install
net start MongoDB
```

**Verify MongoDB:**
```batch
# Check if service is running
sc query MongoDB

# Or check manually
mongod --version
```

---

### 4. Git for Windows (Optional)
**Download:** https://git-scm.com/download/win

Required if cloning the repository.

---

## Installation Steps

### Step 1: Clone or Download Project
```batch
# Using Git
git clone https://github.com/your-org/punchout.git
cd punchout

# Or download and extract ZIP file
```

---

### Step 2: Setup MongoDB
```batch
# Run MongoDB setup script
setup-mongodb-windows.bat
```

This script will:
- ✅ Check if MongoDB is installed
- ✅ Create data and log directories
- ✅ Generate MongoDB configuration
- ✅ Provide instructions for service installation

**Manual MongoDB Service Installation (if needed):**
```batch
# Run as Administrator
mongod --config "%USERPROFILE%\mongodb\mongod.cfg" --install
net start MongoDB
```

---

### Step 3: Import Sample Data
```batch
# Import all MongoDB collections and create indexes
import-data-windows.bat
```

This imports:
- ✅ Environment configurations (dev, stage, prod)
- ✅ Customer onboarding data
- ✅ Customer datastore
- ✅ cXML templates
- ✅ Creates all required indexes

**Verify Data Import:**
```batch
mongosh
use punchout
db.environment_configs.find().pretty()
```

---

### Step 4: Build Project
```batch
# Build all modules
mvn clean install -DskipTests

# Or build specific module
mvn clean install -pl punchout-gateway -am -DskipTests
```

---

## Starting the System

### Method 1: Start All Services (Recommended)
```batch
# Start all backend services
start-all-services-windows.bat
```

This will start:
- ✅ **Gateway Service** (Port 9090)
- ✅ **UI Backend Service** (Port 8080)
- ✅ **Mock Service** (Port 8082)

Services run in background windows. Check logs in `%TEMP%` directory.

---

### Method 2: Start Services Individually
```batch
# Terminal 1 - Gateway Service
cd punchout-gateway
mvn spring-boot:run

# Terminal 2 - UI Backend Service
cd punchout-ui-backend
mvn spring-boot:run

# Terminal 3 - Mock Service
cd punchout-mock-service
mvn spring-boot:run
```

---

### Method 3: Start UI Frontend (if needed)
```batch
# Install Node.js dependencies
cd punchout-ui-frontend
npm install

# Start development server
npm run dev
# Runs on http://localhost:5173
```

---

## Verifying the Setup

### 1. Check Service Health

#### Gateway Service
```batch
curl http://localhost:9090/actuator/health
# or visit in browser
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "externalService": {
      "status": "UP",
      "details": {
        "authService": "reachable",
        "muleService": "reachable",
        "mongodb": "reachable"
      }
    }
  }
}
```

#### UI Backend Service
```batch
curl http://localhost:8080/api/health
```

#### Mock Service
```batch
curl http://localhost:8082/actuator/health
```

---

### 2. Check Running Processes
```batch
# List all Java processes
tasklist | findstr "java"

# Check specific ports
netstat -ano | findstr ":9090"
netstat -ano | findstr ":8080"
netstat -ano | findstr ":8082"
```

---

### 3. View Logs
```batch
# View real-time logs
type %TEMP%\punchout-gateway.log
type %TEMP%\punchout-ui-backend.log
type %TEMP%\punchout-mock-service.log

# Or open in Notepad
notepad %TEMP%\punchout-gateway.log
```

---

### 4. Test API Endpoints

#### Get Environment Configurations
```batch
curl http://localhost:8080/api/environment-config
```

#### Get Customer Onboarding Data
```batch
curl http://localhost:8080/api/customer-onboarding
```

#### Test PunchOut Setup
```batch
curl -X POST http://localhost:9090/punchout/setup ^
  -H "Content-Type: application/xml" ^
  -d @sample-punchout-request.xml
```

---

## Troubleshooting

### MongoDB Not Starting

**Problem:** MongoDB service won't start

**Solution:**
```batch
# Check if port 27017 is in use
netstat -ano | findstr ":27017"

# Stop any conflicting process
taskkill /F /PID <PID>

# Check MongoDB logs
type %USERPROFILE%\mongodb\logs\mongod.log

# Restart service
net stop MongoDB
net start MongoDB
```

---

### Port Already in Use

**Problem:** "Port 9090/8080/8082 already in use"

**Solution:**
```batch
# Find process using the port
netstat -ano | findstr ":9090"

# Kill the process
taskkill /F /PID <PID>

# Or use the stop script
stop-all-services-windows.bat
```

---

### Maven Build Failures

**Problem:** Build fails with compilation errors

**Solution:**
```batch
# Clean and rebuild
mvn clean install -U -DskipTests

# Check Java version
java -version
# Must be Java 17

# Update Maven wrapper (if exists)
mvnw clean install -DskipTests
```

---

### Service Won't Start

**Problem:** Service starts but stops immediately

**Solution:**
```batch
# Check logs for errors
type %TEMP%\punchout-gateway.log

# Common issues:
# 1. MongoDB not running
sc query MongoDB

# 2. Wrong Java version
java -version

# 3. Port conflicts
netstat -ano | findstr ":9090"

# 4. Missing environment variables
echo %JASYPT_ENCRYPTOR_PASSWORD%
```

---

### Cannot Connect to MongoDB

**Problem:** "Connection refused to MongoDB"

**Solution:**
```batch
# 1. Check if MongoDB is running
sc query MongoDB

# 2. Test MongoDB connection
mongosh mongodb://localhost:27017

# 3. Check MongoDB configuration
type %USERPROFILE%\mongodb\mongod.cfg

# 4. Verify data directory exists
dir %USERPROFILE%\mongodb\data
```

---

## Common Commands

### Service Management
```batch
# Start all services
start-all-services-windows.bat

# Stop all services
stop-all-services-windows.bat

# Restart all services
restart-all-services-windows.bat
```

---

### MongoDB Commands
```batch
# Start MongoDB service
net start MongoDB

# Stop MongoDB service
net stop MongoDB

# Connect to MongoDB shell
mongosh
use punchout
db.environment_configs.find().pretty()

# Import data
import-data-windows.bat

# Backup database
mongodump --db=punchout --out=%USERPROFILE%\mongodb\backup

# Restore database
mongorestore --db=punchout %USERPROFILE%\mongodb\backup\punchout
```

---

### Maven Commands
```batch
# Build entire project
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl punchout-gateway -am -DskipTests

# Run tests
mvn test

# Run specific service
cd punchout-gateway
mvn spring-boot:run

# Package for deployment
mvn clean package -DskipTests
```

---

### Monitoring & Debugging
```batch
# View running Java processes
jps -l

# Check service status
curl http://localhost:9090/actuator/health
curl http://localhost:8080/api/health
curl http://localhost:8082/actuator/health

# View metrics
curl http://localhost:9090/actuator/metrics
curl http://localhost:9090/actuator/prometheus

# Check active ports
netstat -ano | findstr "LISTENING"

# Monitor log files
powershell Get-Content %TEMP%\punchout-gateway.log -Wait -Tail 50
```

---

### Environment Variables

**Optional Configuration:**

```batch
# Set Jasypt encryption password (for encrypted configs)
setx JASYPT_ENCRYPTOR_PASSWORD "your-secret-key"

# Set environment (dev, stage, prod)
setx APP_ENVIRONMENT "dev"

# Set MongoDB connection (if non-default)
setx MONGO_HOST "localhost"
setx MONGO_PORT "27017"
```

**For current session only:**
```batch
set JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
set APP_ENVIRONMENT=dev
```

---

## Service Ports Reference

| Service | Port | Health Check |
|---------|------|-------------|
| **Gateway** | 9090 | http://localhost:9090/actuator/health |
| **UI Backend** | 8080 | http://localhost:8080/api/health |
| **Mock Service** | 8082 | http://localhost:8082/actuator/health |
| **UI Frontend** | 5173 | http://localhost:5173 (dev mode) |
| **MongoDB** | 27017 | `mongosh mongodb://localhost:27017` |

---

## File Locations

| Type | Location |
|------|----------|
| **Project Root** | `C:\Users\<username>\punchout` |
| **MongoDB Data** | `%USERPROFILE%\mongodb\data` |
| **MongoDB Logs** | `%USERPROFILE%\mongodb\logs\mongod.log` |
| **MongoDB Config** | `%USERPROFILE%\mongodb\mongod.cfg` |
| **Service Logs** | `%TEMP%\punchout-*.log` |
| **Maven Repository** | `%USERPROFILE%\.m2\repository` |

---

## Next Steps

After successful deployment:

1. **Access UI** → http://localhost:8080
2. **Review API docs** → See `TESTING_GUIDE.md`
3. **Configure environments** → See `PASSWORD_ENCRYPTION_COMPLETE.md`
4. **Monitor metrics** → http://localhost:9090/actuator/prometheus
5. **Production deployment** → See `PRODUCTION_DEPLOYMENT_GUIDE.md`

---

## Production Deployment

For production deployment on Windows Server:

1. **Install as Windows Service** using [winsw](https://github.com/winsw/winsw)
2. **Setup IIS reverse proxy** (optional)
3. **Configure SSL certificates**
4. **Enable Windows Firewall rules**
5. **Setup monitoring** (Prometheus + Grafana)

See `PRODUCTION_DEPLOYMENT_GUIDE.md` for details.

---

## Support

For issues or questions:
- Check logs in `%TEMP%` directory
- Review `TROUBLESHOOTING.md`
- Check MongoDB logs: `%USERPROFILE%\mongodb\logs\mongod.log`

---

## Summary

✅ **MongoDB setup** → `setup-mongodb-windows.bat`  
✅ **Import data** → `import-data-windows.bat`  
✅ **Start services** → `start-all-services-windows.bat`  
✅ **Stop services** → `stop-all-services-windows.bat`  
✅ **Restart services** → `restart-all-services-windows.bat`  

**Quick Start:**
```batch
setup-mongodb-windows.bat
import-data-windows.bat
start-all-services-windows.bat
```

The system is now running on Windows! 🚀

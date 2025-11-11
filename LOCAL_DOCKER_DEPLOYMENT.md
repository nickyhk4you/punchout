# Local Docker Deployment Guide

## Overview
Deploy the entire Punchout platform on your local macOS Docker environment, simulating production setup.

## Prerequisites

- ✅ Docker Desktop for Mac installed and running
- ✅ At least 8GB RAM allocated to Docker
- ✅ At least 20GB free disk space

## Quick Start

### One-Command Deployment

```bash
./deploy-local-docker.sh
```

This single script will:
1. ✅ Clean up any existing containers
2. ✅ Build all Docker images (Gateway, UI Backend, Frontend)
3. ✅ Start MongoDB with persistent storage
4. ✅ Create indexes and import environment configurations
5. ✅ Import sample session and order data
6. ✅ Start all services in production mode
7. ✅ Wait for services to be healthy
8. ✅ Verify all health checks
9. ✅ Display access URLs and useful commands

**Total deployment time:** ~5-10 minutes (includes building images)

## What Gets Deployed

### Services

| Service | Container Name | Port | URL |
|---------|---------------|------|-----|
| **Frontend** | punchout-ui-frontend | 3000 | http://localhost:3000 |
| **UI Backend** | punchout-ui-backend | 8080 | http://localhost:8080/api |
| **Gateway** | punchout-gateway | 9090 | http://localhost:9090/punchout |
| **MongoDB** | punchout-mongodb | 27017 | mongodb://localhost:27017 |

### Data

Automatically imported:
- ✅ Environment configurations (dev, stage, prod, s4-dev)
- ✅ Sample punchout sessions (10 sessions)
- ✅ Sample orders (7 orders)
- ✅ Sample network requests (17 requests)
- ✅ MongoDB indexes with TTL for log cleanup

### Network

All containers run on isolated Docker network: `punchout-prod-network`

## Usage

### Start Deployment

```bash
./deploy-local-docker.sh
```

**Expected Output:**
```
=========================================
Punchout Platform - Local Docker Deployment
=========================================
Version: 1.0.0
Environment: prod
Network: punchout-prod-network
=========================================

Step 1: Cleaning up existing containers...
✓ Cleanup complete

Step 2: Building Docker images...
✓ Gateway image built
✓ UI Backend image built
✓ Frontend image built

Step 3: Starting MongoDB...
✓ MongoDB is ready

Step 4: Initializing MongoDB...
✓ Indexes created
✓ Environment configurations imported
✓ Sample session data imported
✓ Sample order data imported
✓ Sample network request data imported

Step 5: Starting Punchout Gateway...
✓ Gateway is ready

Step 6: Starting UI Backend...
✓ UI Backend is ready

Step 7: Starting Frontend...
✓ Frontend is ready

Step 8: Verifying deployment...
✓ Gateway health check passed
✓ UI Backend health check passed
✓ Frontend health check passed
✓ MongoDB health check passed

=========================================
Deployment Complete!
=========================================

Access URLs:
  Frontend:       http://localhost:3000
  UI Backend API: http://localhost:8080/api
  Gateway:        http://localhost:9090/punchout
  Gateway Health: http://localhost:9090/actuator/health

🎉 Deployment Successful!
=========================================

Open your browser to: http://localhost:3000
```

### Stop Deployment

**Option 1: Stop and remove everything (including data)**
```bash
./stop-docker-deployment.sh
```

**Option 2: Stop but keep data**
```bash
./stop-docker-deployment.sh --keep-data
```

### View Logs

**View specific service logs:**
```bash
# Gateway logs
./view-docker-logs.sh gateway

# UI Backend logs
./view-docker-logs.sh backend

# Frontend logs
./view-docker-logs.sh frontend

# MongoDB logs
./view-docker-logs.sh mongodb

# All logs (split view)
./view-docker-logs.sh all
```

**Or use Docker commands directly:**
```bash
docker logs -f punchout-gateway
docker logs -f --tail=100 punchout-ui-backend
```

## Verification

### Check Container Status

```bash
docker ps --filter "name=punchout-"
```

**Expected output:**
```
NAMES                  STATUS          PORTS
punchout-ui-frontend   Up (healthy)    0.0.0.0:3000->3000/tcp
punchout-ui-backend    Up (healthy)    0.0.0.0:8080->8080/tcp
punchout-gateway       Up (healthy)    0.0.0.0:9090->9090/tcp
punchout-mongodb       Up (healthy)    0.0.0.0:27017->27017/tcp
```

### Test Endpoints

```bash
# Gateway health
curl http://localhost:9090/actuator/health

# UI Backend health
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:3000

# Gateway metrics
curl http://localhost:9090/actuator/prometheus

# Environment configurations
curl http://localhost:9090/api/environment-config/current

# Sessions API
curl http://localhost:8080/api/v1/sessions

# Orders API
curl http://localhost:8080/api/v1/orders
```

### Access MongoDB

```bash
# Using Docker exec
docker exec -it punchout-mongodb mongosh punchout

# From your Mac (if mongosh installed)
mongosh "mongodb://admin:secure-password-123@localhost:27017/punchout?authSource=admin"
```

**Query data:**
```javascript
// List collections
show collections

// Count documents
db.environment_configs.countDocuments()
db.punchout_sessions.countDocuments()
db.orders.countDocuments()
db.network_requests.countDocuments()

// View environment configs
db.environment_configs.find().pretty()

// View recent sessions
db.punchout_sessions.find().sort({sessionDate: -1}).limit(5).pretty()

// View recent orders
db.orders.find().sort({orderDate: -1}).limit(5).pretty()
```

## Troubleshooting

### Build Failures

**Problem:** Docker build fails with "ERROR [internal] load metadata"

**Solution:**
```bash
# Ensure Docker is running
docker ps

# Check Docker resources
# Docker Desktop → Settings → Resources → Advanced
# Recommended: 8GB RAM, 4 CPUs

# Clean Docker build cache
docker builder prune
```

**Problem:** Maven build fails during Docker build

**Solution:**
```bash
# Build locally first to verify
cd punchout-gateway
mvn clean package -DskipTests

# If successful, try Docker build again
```

### Container Won't Start

**Problem:** Container exits immediately

**Solution:**
```bash
# Check logs
docker logs punchout-gateway

# Common issues:
# - MongoDB not ready → Wait longer
# - Wrong credentials → Check MONGO_PASSWORD
# - Port already in use → Stop conflicting service
```

**Problem:** Health check failing

**Solution:**
```bash
# Check if service is actually running
docker exec punchout-gateway wget --spider http://localhost:9090/actuator/health

# View detailed logs
docker logs punchout-gateway | tail -50

# Check if MongoDB connection works
docker exec punchout-gateway wget --spider http://localhost:9090/actuator/health/readiness
```

### Port Conflicts

**Problem:** "Port is already allocated"

**Solution:**
```bash
# Find what's using the port
lsof -i :9090

# Stop the conflicting service or change port
docker run -p 9091:9090 ...  # Use different external port
```

### MongoDB Connection Issues

**Problem:** Services can't connect to MongoDB

**Solution:**
```bash
# Verify MongoDB is running
docker ps | grep punchout-mongodb

# Check MongoDB logs
docker logs punchout-mongodb

# Test connection
docker exec punchout-mongodb mongosh --eval "db.adminCommand('ping')"

# Verify network
docker network inspect punchout-prod-network
```

### Data Not Loading

**Problem:** No sessions or orders showing in UI

**Solution:**
```bash
# Re-import data
docker exec -i punchout-mongodb mongoimport \
  --db=punchout \
  --collection=punchout_sessions \
  --file=/tmp/sessions.json \
  --jsonArray \
  --drop < mongodb-sample-data.json

# Or restart deployment (will reimport)
./stop-docker-deployment.sh
./deploy-local-docker.sh
```

## Advanced Usage

### Custom Configuration

**Change MongoDB password:**
```bash
MONGO_PASSWORD="my-secure-password" ./deploy-local-docker.sh
```

**Use different environment:**
```bash
# Edit deploy-local-docker.sh and change:
ENVIRONMENT="stage"
```

**Custom ports:**
```bash
# Edit the docker run commands in deploy-local-docker.sh
-p 9091:9090  # Change external port from 9090 to 9091
```

### Rebuild Single Service

```bash
# Rebuild and restart just the gateway
docker stop punchout-gateway
docker rm punchout-gateway

docker build -t local/punchout-gateway:latest -f punchout-gateway/Dockerfile .

docker run -d \
  --name punchout-gateway \
  --network punchout-prod-network \
  -p 9090:9090 \
  -e APP_ENVIRONMENT=prod \
  -e SPRING_DATA_MONGODB_HOST=punchout-mongodb \
  -e SPRING_DATA_MONGODB_PORT=27017 \
  -e SPRING_DATA_MONGODB_DATABASE=punchout \
  -e SPRING_DATA_MONGODB_USERNAME=admin \
  -e SPRING_DATA_MONGODB_PASSWORD=secure-password-123 \
  -e SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin \
  local/punchout-gateway:latest
```

### Access Container Shell

```bash
# Gateway
docker exec -it punchout-gateway sh

# UI Backend
docker exec -it punchout-ui-backend sh

# Frontend
docker exec -it punchout-ui-frontend sh

# MongoDB
docker exec -it punchout-mongodb mongosh punchout
```

### Monitor Resources

```bash
# View resource usage
docker stats punchout-gateway punchout-ui-backend punchout-ui-frontend

# View container details
docker inspect punchout-gateway | jq '.[0].State'
```

### Export Data

```bash
# Export orders
docker exec punchout-mongodb mongoexport \
  --db=punchout \
  --collection=orders \
  --out=/tmp/orders-export.json

docker cp punchout-mongodb:/tmp/orders-export.json ./orders-backup.json
```

## Performance Tuning

### Increase Docker Resources

1. Open Docker Desktop
2. Settings → Resources → Advanced
3. Recommended for this platform:
   - **CPUs:** 4
   - **Memory:** 8GB
   - **Swap:** 2GB
   - **Disk:** 60GB

### JVM Tuning

The Dockerfiles already include optimized JVM settings:
```bash
-XX:MaxRAMPercentage=75
-XX:InitialRAMPercentage=50
-XX:+UseContainerSupport
-XX:+ExitOnOutOfMemoryError
```

To customize, edit the Dockerfile or add environment variables:
```bash
docker run -e JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80" ...
```

## Maintenance

### View Deployed Images

```bash
docker images | grep punchout
```

### Clean Up Old Images

```bash
# Remove unused images
docker image prune -a

# Remove specific version
docker rmi local/punchout-gateway:1.0.0
```

### Backup Data

```bash
# Create MongoDB backup
docker exec punchout-mongodb mongodump --out=/tmp/backup
docker cp punchout-mongodb:/tmp/backup ./mongodb-backup-$(date +%Y%m%d)

# Or use Docker volumes
docker run --rm -v punchout-mongodb-data:/data -v $(pwd):/backup alpine \
  tar czf /backup/mongodb-backup-$(date +%Y%m%d).tar.gz /data
```

### Restore Data

```bash
# Restore from backup
docker cp ./mongodb-backup-20251111 punchout-mongodb:/tmp/restore
docker exec punchout-mongodb mongorestore /tmp/restore
```

## Comparison: Dev vs Prod Deployment

| Aspect | Development | Production (Docker) |
|--------|-------------|---------------------|
| Build | Local Maven/npm | Docker multi-stage |
| Startup | `./start-all-services.sh` | `./deploy-local-docker.sh` |
| Isolation | Processes on host | Containers + network |
| Restart | Manual | Automatic restart |
| Health Checks | Manual | Automatic |
| Logging | Console | Structured + aggregated |
| Monitoring | None | Health checks + metrics |
| Data | Local MongoDB | Docker volume (persistent) |
| Configuration | application.yml | Env vars + MongoDB |

## When to Use This vs Kubernetes

### Use Local Docker When:
- ✅ Testing production-like deployment locally
- ✅ Demonstrating to stakeholders
- ✅ Development on prod configuration
- ✅ Learning Docker deployment
- ✅ Single-machine deployment acceptable
- ✅ Don't need auto-scaling

### Use Kubernetes When:
- ✅ Actual production deployment
- ✅ Need auto-scaling (2-10+ pods)
- ✅ Need high availability across multiple nodes
- ✅ Need rolling updates with zero downtime
- ✅ Managing multiple environments
- ✅ Enterprise deployment with team

## Next Steps

### After Local Deployment Success

1. **Test the platform:**
   ```bash
   # Open browser
   open http://localhost:3000
   
   # Test punchout flow
   curl -X POST http://localhost:9090/punchout/setup \
     -H "Content-Type: text/xml" \
     -d @test-data/punchout-sessions/ariba/setup-requests/ariba_create_simple_001.xml
   ```

2. **Run integration tests:**
   ```bash
   GATEWAY_URL=http://localhost:9090 ./run-integration-tests.sh
   ```

3. **Monitor metrics:**
   ```bash
   # View Prometheus metrics
   curl http://localhost:9090/actuator/prometheus
   
   # View health status
   curl http://localhost:9090/actuator/health
   ```

4. **When ready for cloud:**
   - Push images to cloud registry (ACR, ECR, GCR)
   - Deploy Kubernetes manifests
   - See PRODUCTION_DEPLOYMENT_GUIDE.md

## Scripts Reference

### deploy-local-docker.sh
Deploys everything in production mode on local Docker
```bash
./deploy-local-docker.sh
```

### stop-docker-deployment.sh
Stops and removes all containers
```bash
# Stop and remove everything (including data)
./stop-docker-deployment.sh

# Stop but keep data
./stop-docker-deployment.sh --keep-data
```

### view-docker-logs.sh
View logs from running containers
```bash
# View gateway logs
./view-docker-logs.sh gateway

# View all logs
./view-docker-logs.sh all
```

### build-docker-images.sh
Build images without deploying
```bash
./build-docker-images.sh
```

## Summary

With one command, you can now deploy the entire Punchout platform locally in a production-like configuration:

```bash
./deploy-local-docker.sh
```

Then access:
- **UI:** http://localhost:3000
- **API:** http://localhost:8080/api
- **Gateway:** http://localhost:9090/punchout

**Perfect for:**
- ✅ Local testing of prod configuration
- ✅ Demos
- ✅ Development
- ✅ Integration testing
- ✅ Learning Docker deployment

**When ready for cloud, use:**
- PRODUCTION_DEPLOYMENT_GUIDE.md for Kubernetes deployment
- DEPLOYMENT_QUICK_START.md for cloud deployment options

🎉 Production-like deployment on your Mac in one command!

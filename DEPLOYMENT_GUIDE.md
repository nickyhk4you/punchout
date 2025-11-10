# PunchOut Platform - Multi-Environment Deployment Guide

## Overview

The PunchOut Platform supports deployment to 6 different environments with environment-specific configurations.

## Supported Environments

| Environment | Purpose | Profile | URLs |
|-------------|---------|---------|------|
| **local** | Local development | `local` | localhost |
| **dev** | Development server | `dev` | dev.punchout.waters.com |
| **stage** | Staging/QA | `stage` | stage.punchout.waters.com |
| **preprod** | Pre-production | `preprod` | preprod.punchout.waters.com |
| **s4-dev** | S4 Development | `s4-dev` | s4-dev.punchout.waters.com |
| **prod** | Production | `prod` | punchout.waters.com |

## Quick Deployment

### Local Development
```bash
# Start all backend services
./start-all-services.sh

# Start frontend
cd punchout-ui-frontend
npm run dev
```

### Deploy to DEV
```bash
# Gateway
cd punchout-gateway
./deploy.sh dev

# UI Backend
cd punchout-ui-backend
./deploy.sh dev

# Frontend
cd punchout-ui-frontend
./deploy.sh dev
```

### Deploy to STAGE
```bash
# Gateway
cd punchout-gateway
./deploy.sh stage

# UI Backend  
cd punchout-ui-backend
./deploy.sh stage

# Frontend
cd punchout-ui-frontend
./deploy.sh stage
```

### Deploy to PRODUCTION
```bash
# Gateway
cd punchout-gateway
./deploy.sh prod

# UI Backend
cd punchout-ui-backend
./deploy.sh prod

# Frontend
cd punchout-ui-frontend
./deploy.sh prod
```

## Service Configuration by Environment

### Gateway Service

#### Local
```bash
# Run with local profile
java -jar -Dspring.profiles.active=local target/punchout-gateway-1.0.0.jar
```

**Configuration:**
- Port: 9090
- MongoDB: localhost:27017
- Auth URL: http://localhost:8082/api/v1/token
- Catalog URL: http://localhost:8082/api/v1/catalog

#### DEV
```bash
# Run with dev profile
java -jar -Dspring.profiles.active=dev target/punchout-gateway-1.0.0.jar
```

**Configuration:**
- Port: 9090
- MongoDB: dev-mongodb.punchout.waters.com
- Auth URL: https://dev-auth.punchout.waters.com/api/v1/token
- Catalog URL: https://dev-catalog.punchout.waters.com/api/v1/catalog

#### STAGE
```bash
java -jar -Dspring.profiles.active=stage target/punchout-gateway-1.0.0.jar
```

#### PREPROD
```bash
java -jar -Dspring.profiles.active=preprod target/punchout-gateway-1.0.0.jar
```

#### S4-DEV
```bash
java -jar -Dspring.profiles.active=s4-dev target/punchout-gateway-1.0.0.jar
```

#### PRODUCTION
```bash
java -jar -Dspring.profiles.active=prod target/punchout-gateway-1.0.0.jar
```

**Production Configuration:**
- Log to file: /var/log/punchout/gateway.log
- MongoDB with authentication
- Health details hidden
- Reduced logging (INFO level)

### UI Backend Service

Similar configuration pattern as Gateway, running on port 8080.

### Frontend Service

Uses environment-specific .env files, runs on port 3000.

## Docker Deployment

### Build Docker Images

```bash
# Gateway
cd punchout-gateway
docker build -t punchout-gateway:dev .

# UI Backend
cd punchout-ui-backend
docker build -t punchout-ui-backend:dev .

# Frontend
cd punchout-ui-frontend
docker build --build-arg ENVIRONMENT=dev -t punchout-ui-frontend:dev .
```

### Run with Docker

```bash
# Gateway (DEV profile)
docker run -d \
  --name punchout-gateway-dev \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=dev \
  punchout-gateway:dev

# UI Backend (DEV profile)
docker run -d \
  --name punchout-ui-backend-dev \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  punchout-ui-backend:dev

# Frontend (DEV)
docker run -d \
  --name punchout-ui-frontend-dev \
  -p 3000:3000 \
  punchout-ui-frontend:dev
```

### Docker Compose (Full Stack)

Create `docker-compose.{env}.yml` for each environment:

```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  gateway:
    image: punchout-gateway:dev
    container_name: punchout-gateway-dev
    ports:
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - punchout-network
    depends_on:
      - mongodb

  ui-backend:
    image: punchout-ui-backend:dev
    container_name: punchout-ui-backend-dev
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - punchout-network
    depends_on:
      - mongodb

  ui-frontend:
    image: punchout-ui-frontend:dev
    container_name: punchout-ui-frontend-dev
    ports:
      - "3000:3000"
    networks:
      - punchout-network
    depends_on:
      - ui-backend

  mongodb:
    image: mongo:7
    container_name: mongodb-dev
    ports:
      - "27017:27017"
    volumes:
      - mongodb-data:/data/db
    networks:
      - punchout-network

networks:
  punchout-network:
    driver: bridge

volumes:
  mongodb-data:
```

**Run:**
```bash
docker-compose -f docker-compose.dev.yml up -d
```

## Environment Variables

### Gateway Service

**Required:**
- `SPRING_PROFILES_ACTIVE` - Active profile (local, dev, stage, preprod, s4-dev, prod)

**Optional (override application.yml):**
- `SPRING_DATA_MONGODB_URI` - MongoDB connection string
- `THIRDPARTY_AUTH_URL` - Auth service URL
- `THIRDPARTY_CATALOG_URL` - Catalog service URL

**Example:**
```bash
export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATA_MONGODB_URI=mongodb://dev-mongo:27017/punchout
java -jar punchout-gateway-1.0.0.jar
```

### UI Backend Service

**Required:**
- `SPRING_PROFILES_ACTIVE` - Active profile

**Optional:**
- `SPRING_DATA_MONGODB_URI` - MongoDB connection
- `SPRING_DATASOURCE_URL` - PostgreSQL connection
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

**Example:**
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=punchout_prod
export DB_PASSWORD=secure_password
java -jar punchout-ui-backend-1.0.0.jar
```

### Frontend Service

**Required:**
- `NEXT_PUBLIC_ENV` - Environment name
- `NEXT_PUBLIC_API_URL` - Backend API URL
- `NEXT_PUBLIC_GATEWAY_URL` - Gateway URL

## Health Check Endpoints

### After Deployment, Verify:

```bash
# Gateway
curl http://localhost:9090/actuator/health
# Expected: {"status":"UP"}

# UI Backend
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Frontend
curl http://localhost:3000/
# Expected: HTML response
```

## Database Configuration

### MongoDB Connection Strings by Environment

**Local:**
```yaml
mongodb://localhost:27017/punchout
```

**DEV:**
```yaml
mongodb://dev-mongodb.punchout.waters.com:27017/punchout
```

**STAGE:**
```yaml
mongodb://stage-mongodb.punchout.waters.com:27017/punchout
```

**PRODUCTION (with auth):**
```yaml
mongodb://username:password@prod-mongodb.punchout.waters.com:27017/punchout?authSource=admin
```

**MongoDB Atlas (cloud):**
```yaml
mongodb+srv://username:password@cluster.mongodb.net/punchout?retryWrites=true&w=majority
```

## Logging Configuration

### By Environment

| Environment | Log Level | Log File | Details |
|-------------|-----------|----------|---------|
| **local** | DEBUG | Console only | Full debug info |
| **dev** | DEBUG | /var/log/punchout/*.log | Debug enabled |
| **stage** | INFO | /var/log/punchout/*.log | Normal logging |
| **preprod** | INFO | /var/log/punchout/*.log | Normal logging |
| **s4-dev** | DEBUG | /var/log/punchout/*.log | S4 debug |
| **prod** | INFO | /var/log/punchout/*.log | Production logging |

### Log Rotation (Production)
- Max file size: 100MB
- Max history: 30 days
- Compressed old logs

## Deployment Checklist

### Pre-Deployment

- [ ] Code review completed
- [ ] Tests passing
- [ ] Version tagged in Git
- [ ] Environment config files reviewed
- [ ] Database migration scripts ready
- [ ] Secrets configured in environment/vault
- [ ] CORS settings verified
- [ ] Health check endpoints tested

### Deployment Steps

1. **Backup current version**
   ```bash
   # Backup database
   mongodump --uri="mongodb://..." --out=backup-$(date +%Y%m%d)
   ```

2. **Build artifacts**
   ```bash
   # Gateway
   cd punchout-gateway && mvn clean package -DskipTests
   
   # UI Backend
   cd punchout-ui-backend && mvn clean package -DskipTests
   
   # Frontend
   cd punchout-ui-frontend && npm run build:dev
   ```

3. **Deploy services**
   ```bash
   # Use deployment scripts
   ./deploy.sh dev
   ```

4. **Verify deployment**
   ```bash
   # Check health endpoints
   curl https://dev-gateway.punchout.waters.com/actuator/health
   curl https://dev-api.punchout.waters.com/actuator/health
   curl https://dev.punchout.waters.com/
   ```

5. **Smoke tests**
   - Execute PunchOut test in UI
   - Verify session created
   - Check network requests logged
   - Verify dashboard displays correctly

### Post-Deployment

- [ ] Health checks passing
- [ ] Logs show no errors
- [ ] Smoke tests passed
- [ ] Performance within acceptable range
- [ ] Update deployment log
- [ ] Notify team

### Rollback Plan

If deployment fails:

```bash
# Stop new version
docker stop punchout-gateway-dev

# Start previous version
docker start punchout-gateway-dev-previous

# Or redeploy previous tag
git checkout v1.0.0
./deploy.sh dev
```

## CI/CD Pipeline Example

### GitHub Actions Workflow

```yaml
name: Deploy to DEV

on:
  push:
    branches: [develop]

jobs:
  deploy-gateway:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build Gateway
        run: |
          cd punchout-gateway
          mvn clean package -DskipTests
      
      - name: Build Docker Image
        run: |
          cd punchout-gateway
          docker build -t punchout-gateway:dev .
      
      - name: Deploy
        run: |
          # Push to registry and deploy
          # kubectl apply -f k8s/gateway-dev.yaml
          # or ssh and restart service

  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      # Similar steps for UI Backend

  deploy-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Build Frontend
        run: |
          cd punchout-ui-frontend
          npm ci
          npm run build:dev
      
      - name: Build Docker Image
        run: |
          cd punchout-ui-frontend
          docker build --build-arg ENVIRONMENT=dev -t punchout-ui-frontend:dev .
```

## Kubernetes Deployment (Optional)

### Example Kubernetes Manifests

**Gateway Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: punchout-gateway-dev
spec:
  replicas: 2
  selector:
    matchLabels:
      app: punchout-gateway
      env: dev
  template:
    metadata:
      labels:
        app: punchout-gateway
        env: dev
    spec:
      containers:
      - name: gateway
        image: punchout-gateway:dev
        ports:
        - containerPort: 9090
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "dev"
        - name: SPRING_DATA_MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: mongodb-credentials
              key: connection-string
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 9090
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 9090
          initialDelaySeconds: 30
          periodSeconds: 5
```

## Environment URLs Reference

### Gateway Service (Port 9090)

| Environment | URL |
|-------------|-----|
| Local | http://localhost:9090 |
| DEV | https://dev-gateway.punchout.waters.com |
| STAGE | https://stage-gateway.punchout.waters.com |
| PREPROD | https://preprod-gateway.punchout.waters.com |
| S4-DEV | https://s4-dev-gateway.punchout.waters.com |
| PROD | https://gateway.punchout.waters.com |

### UI Backend (Port 8080)

| Environment | URL |
|-------------|-----|
| Local | http://localhost:8080 |
| DEV | https://dev-api.punchout.waters.com |
| STAGE | https://stage-api.punchout.waters.com |
| PREPROD | https://preprod-api.punchout.waters.com |
| S4-DEV | https://s4-dev-api.punchout.waters.com |
| PROD | https://api.punchout.waters.com |

### Frontend (Port 3000)

| Environment | URL |
|-------------|-----|
| Local | http://localhost:3000 |
| DEV | https://dev.punchout.waters.com |
| STAGE | https://stage.punchout.waters.com |
| PREPROD | https://preprod.punchout.waters.com |
| S4-DEV | https://s4-dev.punchout.waters.com |
| PROD | https://punchout.waters.com |

## Configuration Files Summary

### Gateway Service
```
punchout-gateway/src/main/resources/
├── application.yml              # Base configuration
├── application-local.yml        # Local
├── application-dev.yml          # DEV
├── application-stage.yml        # STAGE
├── application-preprod.yml      # PREPROD
├── application-s4-dev.yml       # S4-DEV
└── application-prod.yml         # PRODUCTION
```

### UI Backend Service
```
punchout-ui-backend/src/main/resources/
├── application.yml              # Base configuration
├── application-local.yml        # Local
├── application-dev.yml          # DEV
├── application-stage.yml        # STAGE
├── application-preprod.yml      # PREPROD
├── application-s4-dev.yml       # S4-DEV
└── application-prod.yml         # PRODUCTION
```

### Frontend Service
```
punchout-ui-frontend/
├── .env.local                   # Local
├── .env.dev                     # DEV
├── .env.stage                   # STAGE
├── .env.preprod                 # PREPROD
├── .env.s4-dev                  # S4-DEV
├── .env.production              # PRODUCTION
└── .env.example                 # Template
```

## Secrets Management

### Development/Staging
Use environment variables or config files (acceptable for non-production).

### Production
Use a secrets manager:

**Option 1: Kubernetes Secrets**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mongodb-credentials
type: Opaque
stringData:
  connection-string: mongodb://user:pass@prod-mongo:27017/punchout
```

**Option 2: HashiCorp Vault**
```bash
# Store secret
vault kv put secret/punchout/prod mongodb-uri="mongodb://..."

# Retrieve in application
spring:
  cloud:
    vault:
      uri: https://vault.waters.com
      token: ${VAULT_TOKEN}
```

**Option 3: AWS Secrets Manager**
```yaml
spring:
  cloud:
    aws:
      secretsmanager:
        enabled: true
        region: us-east-1
```

## Monitoring & Alerting

### Health Endpoints

**Gateway:**
```bash
curl https://dev-gateway.punchout.waters.com/actuator/health
```

**UI Backend:**
```bash
curl https://dev-api.punchout.waters.com/actuator/health
```

### Metrics (via Actuator)

```bash
# Metrics endpoint
curl https://dev-gateway.punchout.waters.com/actuator/metrics

# Specific metric
curl https://dev-gateway.punchout.waters.com/actuator/metrics/http.server.requests
```

### Log Aggregation

**Recommended Tools:**
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Splunk
- CloudWatch Logs (AWS)
- Azure Monitor (Azure)

## Troubleshooting

### Service Won't Start

1. **Check profile is set:**
   ```bash
   echo $SPRING_PROFILES_ACTIVE
   ```

2. **Verify config file exists:**
   ```bash
   ls src/main/resources/application-$SPRING_PROFILES_ACTIVE.yml
   ```

3. **Check logs:**
   ```bash
   tail -100 /var/log/punchout/gateway.log
   ```

4. **Test MongoDB connection:**
   ```bash
   mongosh "mongodb://dev-mongodb.punchout.waters.com:27017/punchout"
   ```

### Wrong Configuration Loaded

```bash
# Check which profile is active
curl http://localhost:9090/actuator/env | jq '.activeProfiles'

# Restart with correct profile
SPRING_PROFILES_ACTIVE=dev java -jar app.jar
```

### Database Connection Issues

```bash
# Test MongoDB
mongosh "$SPRING_DATA_MONGODB_URI"

# Test PostgreSQL
psql "$SPRING_DATASOURCE_URL"
```

## Summary

✅ **6 environments configured** - local, dev, stage, preprod, s4-dev, prod
✅ **Spring Profiles** - Environment-specific application.yml files
✅ **Docker support** - Dockerfiles with health checks
✅ **Deployment scripts** - One-command deployment
✅ **Configuration management** - @ConfigurationProperties
✅ **Secrets ready** - Support for vault/secrets manager
✅ **Monitoring ready** - Actuator health & metrics
✅ **Production hardened** - Logging, security, timeouts

All services are now ready for multi-environment deployment! 🚀

# Environment Configuration Management Guide

## Overview

The Punchout Gateway now supports **dynamic, MongoDB-based environment configuration** for managing service endpoints across different environments (dev, stage, prod, s4-dev, etc.). This allows you to:

- Configure different Auth Service and Mule Service URLs for each environment
- Store configurations in MongoDB for easy management
- Change URLs without redeploying the application
- Manage configurations via REST API or developer UI
- Cache configurations for performance

## Architecture

```
┌─────────────────┐
│ Punchout Request│
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ PunchOutOrchestration   │
│      Service            │
└────────┬────────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐ ┌───────┐
│ Auth  │ │ Mule  │
│Client │ │Client │
└───┬───┘ └───┬───┘
    │         │
    └────┬────┘
         ▼
┌──────────────────────┐
│ EnvironmentConfig    │
│      Service         │
└────────┬─────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌──────┐
│MongoDB │ │Cache │
│ Config │ │      │
└────────┘ └──────┘
```

## Components Created

### 1. Entity Layer
- **`EnvironmentConfig.java`** - MongoDB document entity
  - Stores environment name (dev, stage, prod, s4-dev)
  - Auth service URL
  - Mule service URL
  - Catalog base URL
  - Enable/disable flag
  - Audit fields (created/updated by/at)

### 2. Repository Layer
- **`EnvironmentConfigRepository.java`** - Spring Data MongoDB repository
  - Find by environment name
  - Find enabled configurations only

### 3. Service Layer
- **`EnvironmentConfigService.java`** - Configuration management service
  - Load configurations from MongoDB
  - Cache configurations for performance
  - Fallback to application.yml if no DB config exists
  - CRUD operations
  - Cache management

### 4. Controller Layer
- **`EnvironmentConfigController.java`** - REST API endpoints
  - GET `/api/environment-config` - List all configs
  - GET `/api/environment-config/{environment}` - Get specific config
  - GET `/api/environment-config/current` - Get current environment config
  - POST `/api/environment-config` - Create new config
  - PUT `/api/environment-config/{environment}` - Update config
  - DELETE `/api/environment-config/{environment}` - Delete config
  - POST `/api/environment-config/cache/clear/{environment}` - Clear cache
  - GET `/api/environment-config/urls/{environment}` - Get URLs only

### 5. Updated Clients
- **`AuthServiceClient.java`** - Now uses dynamic URLs from config service
- **`MuleServiceClient.java`** - Now uses dynamic URLs from config service

### 6. Configuration
- **`CacheConfig.java`** - Enables caching for environment configurations
- **`application.yml`** - Updated with `app.environment` property

## Environment Configurations

### Imported Configurations

Four environments are pre-configured in MongoDB:

| Environment | Auth Service URL | Mule Service URL | Catalog URL |
|-------------|------------------|------------------|-------------|
| **dev** | http://localhost:8081/api/auth/token | http://localhost:8082/api/catalog | http://localhost:3000 |
| **stage** | https://auth-stage.waters.com/api/auth/token | https://mule-stage.waters.com/api/catalog | https://punchout-stage.waters.com |
| **prod** | https://auth.waters.com/api/auth/token | https://mule.waters.com/api/catalog | https://punchout.waters.com |
| **s4-dev** | https://auth-s4-dev.waters.com/api/auth/token | https://mule-s4-dev.waters.com/api/catalog | https://punchout-s4-dev.waters.com |

## Setup & Usage

### 1. Import Environment Configurations

```bash
# Import the configurations into MongoDB
./import-environment-configs.sh

# Or manually:
mongoimport --uri="mongodb://localhost:27017" \
  --db=punchout \
  --collection=environment_configs \
  --file=mongodb-environment-configs-sample-data.json \
  --jsonArray \
  --drop
```

### 2. Set Active Environment

The active environment is determined by the `APP_ENVIRONMENT` environment variable:

```bash
# For development (default)
export APP_ENVIRONMENT=dev

# For staging
export APP_ENVIRONMENT=stage

# For production
export APP_ENVIRONMENT=prod

# For S4 HANA development
export APP_ENVIRONMENT=s4-dev
```

**Or** set it in your startup script:

```bash
# In start-all-services.sh or docker-compose.yml
APP_ENVIRONMENT=stage java -jar punchout-gateway.jar
```

**Or** in `application.yml` (not recommended for prod):

```yaml
app:
  environment: stage
```

### 3. Start the Gateway

```bash
cd punchout-gateway
mvn spring-boot:run
```

The service will:
1. Read the `APP_ENVIRONMENT` variable (defaults to "dev")
2. Load the configuration from MongoDB for that environment
3. Cache the configuration for performance
4. Use the URLs from the configuration for Auth and Mule services

### 4. Verify Configuration

```bash
# Get current environment configuration
curl http://localhost:9090/api/environment-config/current

# Get all configurations
curl http://localhost:9090/api/environment-config

# Get specific environment
curl http://localhost:9090/api/environment-config/stage

# Get just the URLs
curl http://localhost:9090/api/environment-config/urls/prod
```

## REST API Usage

### List All Configurations

```bash
curl -X GET http://localhost:9090/api/environment-config
```

**Response:**
```json
[
  {
    "id": "...",
    "environment": "dev",
    "authServiceUrl": "http://localhost:8081/api/auth/token",
    "muleServiceUrl": "http://localhost:8082/api/catalog",
    "catalogBaseUrl": "http://localhost:3000",
    "description": "Local development environment",
    "enabled": true,
    "createdAt": "2025-11-11T00:00:00",
    "updatedAt": "2025-11-11T00:00:00",
    "createdBy": "system",
    "updatedBy": "system"
  },
  ...
]
```

### Get Current Environment Configuration

```bash
curl -X GET http://localhost:9090/api/environment-config/current
```

### Get Specific Environment

```bash
curl -X GET http://localhost:9090/api/environment-config/prod
```

### Create New Environment

```bash
curl -X POST http://localhost:9090/api/environment-config \
  -H "Content-Type: application/json" \
  -d '{
    "environment": "qa",
    "authServiceUrl": "https://auth-qa.waters.com/api/auth/token",
    "muleServiceUrl": "https://mule-qa.waters.com/api/catalog",
    "catalogBaseUrl": "https://punchout-qa.waters.com",
    "description": "QA testing environment",
    "enabled": true,
    "createdBy": "admin"
  }'
```

### Update Environment

```bash
curl -X PUT http://localhost:9090/api/environment-config/dev \
  -H "Content-Type: application/json" \
  -d '{
    "authServiceUrl": "http://localhost:8091/api/auth/token",
    "muleServiceUrl": "http://localhost:8092/api/catalog",
    "catalogBaseUrl": "http://localhost:3000",
    "description": "Updated dev environment",
    "enabled": true,
    "updatedBy": "admin"
  }'
```

### Delete Environment

```bash
curl -X DELETE http://localhost:9090/api/environment-config/qa
```

### Clear Cache

After updating a configuration, clear the cache to reload:

```bash
# Clear specific environment cache
curl -X POST http://localhost:9090/api/environment-config/cache/clear/dev

# Clear all caches
curl -X POST http://localhost:9090/api/environment-config/cache/clear-all
```

## UI Integration (Developer Portal)

You can manage configurations via the developer portal at:

**http://localhost:3000/developer/punchout**

### UI Features:
- View all environment configurations in a table
- Switch between environments
- Edit environment configurations inline
- Test connections to Auth and Mule services
- Enable/disable environments
- Clear cache after changes

## How It Works

### 1. Request Flow

```
1. Punchout request received
2. Gateway determines current environment (from APP_ENVIRONMENT)
3. EnvironmentConfigService loads config from cache/MongoDB
4. AuthServiceClient gets auth URL from config
5. Auth request sent to dynamic URL
6. MuleServiceClient gets mule URL from config
7. Catalog request sent to dynamic URL
8. Response returned to client
```

### 2. Configuration Resolution

The service uses this priority order:

1. **MongoDB** (highest priority)
   - If a configuration exists and is enabled in MongoDB, use it
   
2. **application.yml** (fallback)
   - If no MongoDB config exists, use the URLs from application.yml
   - Properties: `thirdparty.auth.url` and `thirdparty.mule.url`

3. **Default** (last resort)
   - If nothing is configured, use hardcoded defaults
   - Auth: `http://localhost:8081/api/auth/token`
   - Mule: `http://localhost:8082/api/catalog`

### 3. Caching Strategy

- Configurations are cached in memory using Spring Cache
- Cache key: environment name (e.g., "dev", "stage", "prod")
- Cache is automatically evicted when configurations are updated
- Manual cache clearing available via API

## Deployment Scenarios

### Local Development

```bash
export APP_ENVIRONMENT=dev
mvn spring-boot:run
```

### Docker Deployment

```yaml
# docker-compose.yml
services:
  punchout-gateway:
    image: punchout-gateway:latest
    environment:
      - APP_ENVIRONMENT=stage
      - SPRING_DATA_MONGODB_HOST=mongodb
      - SPRING_DATA_MONGODB_PORT=27017
      - SPRING_DATA_MONGODB_DATABASE=punchout
    ports:
      - "9090:9090"
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: punchout-gateway
spec:
  template:
    spec:
      containers:
      - name: punchout-gateway
        image: punchout-gateway:latest
        env:
        - name: APP_ENVIRONMENT
          value: "prod"
        - name: SPRING_DATA_MONGODB_HOST
          value: "mongodb-service"
```

### Environment-Specific Configurations

```bash
# Development
APP_ENVIRONMENT=dev

# Staging  
APP_ENVIRONMENT=stage

# Production
APP_ENVIRONMENT=prod

# S4 HANA Dev
APP_ENVIRONMENT=s4-dev
```

## Testing

### 1. Verify Current Configuration

```bash
curl http://localhost:9090/api/environment-config/current
```

### 2. Test Auth Service Connection

The Auth service URL is logged when making requests:

```bash
# Check logs for:
# "Requesting auth token for sessionKey=XXX, environment=dev, url=http://localhost:8081/api/auth/token"
```

### 3. Test Mule Service Connection

The Mule service URL is logged when making requests:

```bash
# Check logs for:
# "Sending Mule request for sessionKey=XXX, environment=dev, url=http://localhost:8082/api/catalog"
```

### 4. Test Configuration Switching

```bash
# Update environment to stage
export APP_ENVIRONMENT=stage

# Restart gateway
mvn spring-boot:run

# Verify URLs changed
curl http://localhost:9090/api/environment-config/current
```

## Monitoring & Logging

The service logs important configuration events:

```
Environment Config Service initialized. Current environment: dev
Fallback Auth URL: http://localhost:8081/api/auth/token
Fallback Mule URL: http://localhost:8082/api/catalog
Loading configuration for environment: dev
Requesting auth token for sessionKey=XXX, environment=dev, url=http://localhost:8081/api/auth/token
Sending Mule request for sessionKey=XXX, environment=dev, url=http://localhost:8082/api/catalog
```

## Troubleshooting

### Configuration Not Loading

**Problem:** Gateway uses wrong URLs

**Solution:**
1. Check `APP_ENVIRONMENT` is set correctly
2. Verify configuration exists in MongoDB:
   ```bash
   mongosh punchout
   db.environment_configs.find({environment: "dev"})
   ```
3. Check configuration is enabled:
   ```json
   { "enabled": true }
   ```
4. Clear cache:
   ```bash
   curl -X POST http://localhost:9090/api/environment-config/cache/clear-all
   ```

### Fallback URLs Used

**Problem:** System uses application.yml instead of MongoDB

**Solution:**
1. Import configurations:
   ```bash
   ./import-environment-configs.sh
   ```
2. Verify MongoDB connection
3. Check logs for warnings:
   ```
   No enabled configuration found for environment: dev. Using fallback values.
   ```

### Cache Not Updating

**Problem:** Changes not reflected after update

**Solution:**
1. Clear specific cache:
   ```bash
   curl -X POST http://localhost:9090/api/environment-config/cache/clear/dev
   ```
2. Or clear all caches:
   ```bash
   curl -X POST http://localhost:9090/api/environment-config/cache/clear-all
   ```

## Best Practices

1. **Use Environment Variables**
   - Set `APP_ENVIRONMENT` via environment variable, not hardcoded
   - Different for each deployment environment

2. **Keep Configurations in MongoDB**
   - Don't hardcode URLs in application.yml for production
   - Use MongoDB as source of truth

3. **Enable Only Active Configs**
   - Set `enabled: false` for deprecated environments
   - Prevents accidental use

4. **Use Descriptions**
   - Add meaningful descriptions to configurations
   - Helps identify purpose and owner

5. **Track Changes**
   - Use `createdBy` and `updatedBy` fields
   - Maintain audit trail

6. **Test Before Deploy**
   - Verify URLs in lower environments first
   - Test connectivity before enabling

7. **Clear Cache After Changes**
   - Always clear cache after updating configurations
   - Ensures changes take effect immediately

## Security Considerations

1. **MongoDB Access Control**
   - Secure MongoDB with authentication
   - Use role-based access control

2. **API Authorization**
   - Add authentication to config management endpoints
   - Restrict to admin users only

3. **Sensitive URLs**
   - Don't expose internal URLs publicly
   - Use VPN or network policies

4. **Environment Separation**
   - Keep production configs separate
   - Use different MongoDB instances per environment

## Future Enhancements

Potential improvements:

1. **UI Integration**
   - Build admin UI for configuration management
   - Real-time configuration testing

2. **Configuration History**
   - Track configuration changes
   - Rollback capability

3. **Health Checks**
   - Test connectivity to configured URLs
   - Automatic failover to backup URLs

4. **Dynamic Timeout Configuration**
   - Configure timeouts per environment
   - Different SLAs for different environments

5. **Multiple Service Support**
   - Add more service types (SAP, Oracle, etc.)
   - Flexible service endpoint management

6. **Configuration Validation**
   - URL format validation
   - Connectivity testing before saving

## Summary

The environment configuration system provides:

✅ **Flexibility** - Change URLs without redeployment
✅ **Scalability** - Support unlimited environments
✅ **Performance** - In-memory caching
✅ **Maintainability** - Central configuration management
✅ **Reliability** - Fallback to application.yml
✅ **Observability** - Comprehensive logging
✅ **API-First** - RESTful management interface

All service endpoints are now configurable per environment and stored in MongoDB for easy management!

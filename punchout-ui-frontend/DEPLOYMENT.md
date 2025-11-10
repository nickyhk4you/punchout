# Frontend Deployment Guide

## Environment Configuration

The frontend supports multiple deployment environments with separate configurations.

## Available Environments

| Environment | Purpose | API URL Pattern |
|-------------|---------|----------------|
| **local** | Local development | http://localhost:8080 |
| **dev** | Development server | https://dev-api.punchout.waters.com |
| **stage** | Staging/QA | https://stage-api.punchout.waters.com |
| **preprod** | Pre-production | https://preprod-api.punchout.waters.com |
| **s4-dev** | S4 Development | https://s4-dev-api.punchout.waters.com |
| **prod** | Production | https://api.punchout.waters.com |

## Environment Variables

Each environment has its own `.env.{environment}` file:

```bash
NEXT_PUBLIC_ENV=dev
NEXT_PUBLIC_API_URL=https://dev-api.punchout.waters.com/api
NEXT_PUBLIC_GATEWAY_URL=https://dev-gateway.punchout.waters.com
NEXT_PUBLIC_APP_NAME=PunchOut Testing Platform - DEV
```

## Local Development

### Setup
```bash
# Copy example env file
cp .env.example .env.local

# Install dependencies
npm install

# Run development server
npm run dev
```

Application runs on http://localhost:3000

### Environment Variables (.env.local)
```bash
NEXT_PUBLIC_ENV=local
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_GATEWAY_URL=http://localhost:9090
NEXT_PUBLIC_APP_NAME=PunchOut Testing Platform - Local
```

## Building for Deployment

### Build Commands

```bash
# Development
npm run build:dev

# Staging
npm run build:stage

# Pre-Production
npm run build:preprod

# S4 Development
npm run build:s4-dev

# Production
npm run build:prod
```

### What Happens During Build

1. Loads environment variables from `.env.{environment}`
2. Compiles TypeScript to JavaScript
3. Optimizes React components
4. Generates static pages where possible
5. Creates production bundle
6. Outputs to `.next/` directory

### Build Output

```
.next/
├── standalone/          # Standalone server (for Docker)
├── static/              # Static assets
└── server/              # Server-side code
```

## Deployment Methods

### Method 1: Node.js (Traditional)

```bash
# Build
npm run build:prod

# Start
npm run start:prod
```

**Pros:**
- Simple deployment
- Direct control
- Easy debugging

**Cons:**
- Requires Node.js on server
- Manual process management

### Method 2: Docker (Recommended)

```bash
# Build Docker image for production
docker build --build-arg ENVIRONMENT=production -t punchout-ui:prod .

# Run container
docker run -p 3000:3000 \
  -e NEXT_PUBLIC_ENV=production \
  -e NEXT_PUBLIC_API_URL=https://api.punchout.waters.com/api \
  -e NEXT_PUBLIC_GATEWAY_URL=https://gateway.punchout.waters.com \
  punchout-ui:prod
```

**Pros:**
- Containerized
- Consistent across environments
- Easy orchestration

**Cons:**
- Requires Docker
- Slightly more complex

### Method 3: Docker Compose (Multi-Environment)

```bash
# Start development environment
docker-compose --profile dev up

# Start staging environment
docker-compose --profile stage up

# Start production environment
docker-compose --profile prod up
```

**Pros:**
- One command deployment
- Environment profiles
- Network isolation

## Environment-Specific Deployment

### Deploy to DEV
```bash
# Option 1: Using deployment script
./deploy.sh dev

# Option 2: Manual
npm run build:dev
npm run start:dev

# Option 3: Docker
docker-compose --profile dev up -d
```

### Deploy to STAGE
```bash
# Option 1: Using deployment script
./deploy.sh stage

# Option 2: Manual
npm run build:stage
npm run start:stage

# Option 3: Docker
docker-compose --profile stage up -d
```

### Deploy to PREPROD
```bash
./deploy.sh preprod
# or
npm run build:preprod && npm run start:preprod
```

### Deploy to S4-DEV
```bash
./deploy.sh s4-dev
# or
npm run build:s4-dev && npm run start:s4-dev
```

### Deploy to PRODUCTION
```bash
# Use deployment script with extra confirmation
./deploy.sh prod

# Or manual with production checks
npm run build:prod
npm run start:prod
```

## Verification

### After Deployment, Verify:

1. **Application Starts**
   ```bash
   curl http://localhost:3000
   # Should return HTML
   ```

2. **Environment Correct**
   - Open browser
   - Check app title shows correct environment
   - Check browser console logs

3. **API Connectivity**
   ```bash
   # Check API is reachable
   curl $NEXT_PUBLIC_API_URL/v1/sessions
   ```

4. **Gateway Connectivity**
   ```bash
   # Check Gateway is reachable
   curl $NEXT_PUBLIC_GATEWAY_URL/punchout/health
   ```

## Configuration by Environment

### Local (.env.local)
```bash
NEXT_PUBLIC_ENV=local
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_GATEWAY_URL=http://localhost:9090
```

### DEV (.env.dev)
```bash
NEXT_PUBLIC_ENV=dev
NEXT_PUBLIC_API_URL=https://dev-api.punchout.waters.com/api
NEXT_PUBLIC_GATEWAY_URL=https://dev-gateway.punchout.waters.com
```

### STAGE (.env.stage)
```bash
NEXT_PUBLIC_ENV=stage
NEXT_PUBLIC_API_URL=https://stage-api.punchout.waters.com/api
NEXT_PUBLIC_GATEWAY_URL=https://stage-gateway.punchout.waters.com
```

### PREPROD (.env.preprod)
```bash
NEXT_PUBLIC_ENV=preprod
NEXT_PUBLIC_API_URL=https://preprod-api.punchout.waters.com/api
NEXT_PUBLIC_GATEWAY_URL=https://preprod-gateway.punchout.waters.com
```

### S4-DEV (.env.s4-dev)
```bash
NEXT_PUBLIC_ENV=s4-dev
NEXT_PUBLIC_API_URL=https://s4-dev-api.punchout.waters.com/api
NEXT_PUBLIC_GATEWAY_URL=https://s4-dev-gateway.punchout.waters.com
```

### PRODUCTION (.env.production)
```bash
NEXT_PUBLIC_ENV=production
NEXT_PUBLIC_API_URL=https://api.punchout.waters.com/api
NEXT_PUBLIC_GATEWAY_URL=https://gateway.punchout.waters.com
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy Frontend

on:
  push:
    branches:
      - main        # → Production
      - develop     # → DEV
      - staging     # → STAGE

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Determine Environment
        id: env
        run: |
          if [[ "${{ github.ref }}" == "refs/heads/main" ]]; then
            echo "environment=prod" >> $GITHUB_OUTPUT
          elif [[ "${{ github.ref }}" == "refs/heads/staging" ]]; then
            echo "environment=stage" >> $GITHUB_OUTPUT
          else
            echo "environment=dev" >> $GITHUB_OUTPUT
          fi
      
      - name: Install Dependencies
        run: npm ci
      
      - name: Build
        run: npm run build:${{ steps.env.outputs.environment }}
      
      - name: Deploy
        run: |
          # Your deployment commands here
          # e.g., scp, rsync, kubectl, etc.
```

## Health Checks

After deployment, verify these endpoints:

```bash
# Frontend health (should return 200)
curl https://dev.punchout.waters.com/

# API health
curl https://dev-api.punchout.waters.com/api/actuator/health

# Gateway health
curl https://dev-gateway.punchout.waters.com/actuator/health
```

## Rollback Procedure

### If Deployment Fails

1. **Check logs**
   ```bash
   docker logs punchout-ui-dev
   ```

2. **Verify environment variables**
   ```bash
   docker exec punchout-ui-dev printenv | grep NEXT_PUBLIC
   ```

3. **Rollback to previous version**
   ```bash
   docker-compose --profile dev down
   docker-compose --profile dev up -d
   ```

4. **Rebuild if needed**
   ```bash
   ./deploy.sh dev
   ```

## Environment URLs

### Local Development
- Frontend: http://localhost:3000
- API: http://localhost:8080
- Gateway: http://localhost:9090

### DEV
- Frontend: https://dev.punchout.waters.com
- API: https://dev-api.punchout.waters.com
- Gateway: https://dev-gateway.punchout.waters.com

### STAGE
- Frontend: https://stage.punchout.waters.com
- API: https://stage-api.punchout.waters.com
- Gateway: https://stage-gateway.punchout.waters.com

### PREPROD
- Frontend: https://preprod.punchout.waters.com
- API: https://preprod-api.punchout.waters.com
- Gateway: https://preprod-gateway.punchout.waters.com

### S4-DEV
- Frontend: https://s4-dev.punchout.waters.com
- API: https://s4-dev-api.punchout.waters.com
- Gateway: https://s4-dev-gateway.punchout.waters.com

### PRODUCTION
- Frontend: https://punchout.waters.com
- API: https://api.punchout.waters.com
- Gateway: https://gateway.punchout.waters.com

## Security Notes

### Environment Variables
- ✅ Never commit `.env.local` (in .gitignore)
- ✅ Store secrets in environment or vault
- ✅ Use different secrets per environment
- ✅ Rotate credentials regularly

### CORS Configuration
- Local: Allow all origins
- DEV/STAGE: Allow specific domains
- PRODUCTION: Strict origin checking

## Troubleshooting

### Build Fails
```bash
# Clear cache
rm -rf .next node_modules
npm install
npm run build
```

### Wrong Environment Loaded
```bash
# Check which env file is being used
cat .env.production  # or .env.dev, etc.

# Verify environment variable
echo $NEXT_PUBLIC_ENV
```

### API Not Reachable
```bash
# Test API connectivity
curl $NEXT_PUBLIC_API_URL/v1/sessions

# Check CORS
curl -H "Origin: http://localhost:3000" -I $NEXT_PUBLIC_API_URL/v1/sessions
```

## Summary

✅ **6 environments supported** - local, dev, stage, preprod, s4-dev, prod
✅ **Environment-specific configs** - Separate .env files
✅ **Build scripts** - `npm run build:{env}`
✅ **Start scripts** - `npm run start:{env}`
✅ **Docker support** - Dockerfile with build args
✅ **Docker Compose** - Multi-environment profiles
✅ **Deployment script** - `./deploy.sh {env}`
✅ **CI/CD ready** - GitHub Actions example

The frontend is now ready for deployment to any environment! 🚀

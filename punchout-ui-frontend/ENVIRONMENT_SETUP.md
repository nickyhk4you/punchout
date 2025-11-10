# Frontend Environment Setup - Quick Reference

## 🚀 Quick Start

### Local Development
```bash
npm install
npm run dev
```

### Build for Specific Environment
```bash
npm run build:dev      # Development
npm run build:stage    # Staging
npm run build:preprod  # Pre-Production
npm run build:s4-dev   # S4 Development
npm run build:prod     # Production
```

### Start Specific Environment
```bash
npm run start:dev      # Development
npm run start:stage    # Staging
npm run start:preprod  # Pre-Production
npm run start:s4-dev   # S4 Development
npm run start:prod     # Production
```

## 📁 Environment Files

```
punchout-ui-frontend/
├── .env.local         # Local development (git-ignored)
├── .env.dev           # Development server
├── .env.stage         # Staging server
├── .env.preprod       # Pre-production server
├── .env.s4-dev        # S4 Development server
├── .env.production    # Production server
└── .env.example       # Template (copy to .env.local)
```

## 🔧 Configuration Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_ENV` | Environment name | `dev`, `stage`, `prod` |
| `NEXT_PUBLIC_API_URL` | Backend API URL | `https://dev-api.punchout.waters.com/api` |
| `NEXT_PUBLIC_GATEWAY_URL` | Gateway URL | `https://dev-gateway.punchout.waters.com` |
| `NEXT_PUBLIC_APP_NAME` | App title | `PunchOut Testing Platform - DEV` |

### Access in Code

```typescript
// In any component
const apiUrl = process.env.NEXT_PUBLIC_API_URL;
const gatewayUrl = process.env.NEXT_PUBLIC_GATEWAY_URL;
const environment = process.env.NEXT_PUBLIC_ENV;
```

## 🐳 Docker Deployment

### Build Docker Image
```bash
# For DEV
docker build --build-arg ENVIRONMENT=dev -t punchout-ui:dev .

# For STAGE
docker build --build-arg ENVIRONMENT=stage -t punchout-ui:stage .

# For PRODUCTION
docker build --build-arg ENVIRONMENT=production -t punchout-ui:prod .
```

### Run Docker Container
```bash
docker run -p 3000:3000 punchout-ui:dev
```

### Using Docker Compose
```bash
# Development
docker-compose --profile dev up -d

# Staging
docker-compose --profile stage up -d

# Production
docker-compose --profile prod up -d

# Stop
docker-compose --profile dev down
```

## 🔄 Deployment Script

Simple deployment helper:

```bash
./deploy.sh dev      # Deploy to DEV
./deploy.sh stage    # Deploy to STAGE
./deploy.sh preprod  # Deploy to PRE-PROD
./deploy.sh s4-dev   # Deploy to S4-DEV
./deploy.sh prod     # Deploy to PRODUCTION
```

## 🌐 Environment URLs

### Local
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

## ✅ Pre-Deployment Checklist

Before deploying to any environment:

- [ ] Update .env.{environment} with correct URLs
- [ ] Verify backend API is running
- [ ] Verify Gateway is running
- [ ] Test API connectivity
- [ ] Check CORS configuration
- [ ] Verify MongoDB connection
- [ ] Test build locally: `npm run build:{env}`
- [ ] Review environment-specific settings

## 🔐 Security Checklist

- [ ] No secrets in .env files committed to git
- [ ] .env.local is in .gitignore
- [ ] CORS configured for specific origins (not *)
- [ ] HTTPS enabled for non-local environments
- [ ] API endpoints secured
- [ ] Environment variables validated

## 📊 Monitoring After Deployment

1. **Check application health**
   ```bash
   curl https://dev.punchout.waters.com/
   ```

2. **Verify API calls work**
   - Open browser DevTools
   - Navigate to /sessions
   - Check Network tab for API calls

3. **Test key features**
   - Login (if auth enabled)
   - View sessions
   - Execute PunchOut test
   - View network requests

4. **Check logs**
   ```bash
   docker logs punchout-ui-dev
   ```

## 🆘 Troubleshooting

### Build Fails
```bash
# Clear everything
rm -rf .next node_modules package-lock.json
npm install
npm run build:dev
```

### Environment Variables Not Loading
```bash
# Verify env file exists
ls -la .env.dev

# Check content
cat .env.dev

# Build with explicit env
env-cmd -f .env.dev next build
```

### Port Already in Use
```bash
# Find process
lsof -ti :3000

# Kill process
lsof -ti :3000 | xargs kill

# Or use different port
next start -p 3001
```

### Docker Build Fails
```bash
# Clean Docker
docker system prune -a

# Rebuild without cache
docker build --no-cache --build-arg ENVIRONMENT=dev -t punchout-ui:dev .
```

## 📝 Notes

### Environment Variable Naming
- All frontend variables must start with `NEXT_PUBLIC_`
- This makes them available in the browser
- Backend-only secrets should NOT use this prefix

### Build vs Runtime
- `NEXT_PUBLIC_*` variables are baked into build
- Changing .env requires rebuild
- Cannot change at runtime (by design)

### Standalone Mode
- `output: 'standalone'` creates self-contained deployment
- Includes only necessary files
- Smaller Docker images
- Faster deployments

## 🎯 Summary

✅ **6 environments configured** - local, dev, stage, preprod, s4-dev, prod
✅ **Separate .env files** - Environment-specific configuration
✅ **Build scripts** - Easy building for each environment
✅ **Docker support** - Containerized deployments
✅ **Docker Compose** - Multi-environment orchestration
✅ **Deployment script** - One-command deployment
✅ **CI/CD ready** - GitHub Actions example

The frontend is now fully configured for multi-environment deployment! 🚀

# Production Deployment Guide

## Overview
Best practices for deploying the Punchout platform to production, covering containerization, orchestration, monitoring, and security.

## Architecture

```mermaid
graph TB
    subgraph Internet
        Users[External Users]
        B2B[B2B Platforms<br/>Ariba/Coupa/OCI]
    end
    
    subgraph Kubernetes Cluster - Production
        Ingress[NGINX Ingress<br/>TLS Termination]
        
        subgraph Services
            GW[Gateway Pods<br/>2-10 replicas<br/>Port 9090]
            API[UI Backend Pods<br/>2-5 replicas<br/>Port 8080]
            UI[Frontend Pods<br/>2-5 replicas<br/>Port 3000]
        end
        
        subgraph Data
            Mongo[(MongoDB<br/>3-node Replica Set)]
        end
    end
    
    subgraph External Services
        Auth[Auth Service]
        Mule[Mule ESB]
    end
    
    Users --> Ingress
    B2B --> Ingress
    Ingress --> GW
    Ingress --> API
    Ingress --> UI
    GW --> Mongo
    API --> Mongo
    GW --> Auth
    GW --> Mule
    UI --> API
```

## Deployment Strategy

### 🎯 Recommended: Kubernetes + Managed MongoDB

**For Production:**
- Kubernetes (EKS/AKS/GKE) for container orchestration
- Managed MongoDB (Atlas/DocumentDB) for database
- NGINX Ingress for routing and TLS
- Prometheus + Grafana for monitoring
- ELK/OpenSearch for centralized logging

**For Development/Staging:**
- Same Kubernetes setup (smaller clusters)
- Or Docker Compose for simplicity

## 1. Containerization

### Docker Images for Spring Boot Services

#### Gateway Dockerfile
```dockerfile
# punchout-gateway/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY ../punchout-common/pom.xml ../punchout-common/
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

EXPOSE 9090

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### UI Backend Dockerfile
```dockerfile
# punchout-ui-backend/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY ../punchout-common/pom.xml ../punchout-common/
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Image for Next.js Frontend

#### Frontend Dockerfile
```dockerfile
# punchout-ui-frontend/Dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM node:18-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app

COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/public ./public

ENV NODE_ENV=production
ENV PORT=3000

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/api/health || exit 1

CMD ["npm", "start"]
```

### Build Images

```bash
# Build all images with version tag
VERSION=1.0.0
REGISTRY=your-registry.azurecr.io

# Gateway
docker build -t $REGISTRY/punchout-gateway:$VERSION -t $REGISTRY/punchout-gateway:latest \
  -f punchout-gateway/Dockerfile .

# UI Backend
docker build -t $REGISTRY/punchout-ui-backend:$VERSION -t $REGISTRY/punchout-ui-backend:latest \
  -f punchout-ui-backend/Dockerfile .

# Frontend
docker build -t $REGISTRY/punchout-ui-frontend:$VERSION -t $REGISTRY/punchout-ui-frontend:latest \
  -f punchout-ui-frontend/Dockerfile ./punchout-ui-frontend

# Push to registry
docker push $REGISTRY/punchout-gateway:$VERSION
docker push $REGISTRY/punchout-ui-backend:$VERSION
docker push $REGISTRY/punchout-ui-frontend:$VERSION
```

## 2. Kubernetes Deployment

### Namespace Setup

```yaml
# k8s/namespaces.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: punchout-dev
---
apiVersion: v1
kind: Namespace
metadata:
  name: punchout-stage
---
apiVersion: v1
kind: Namespace
metadata:
  name: punchout-prod
```

### ConfigMaps and Secrets

```yaml
# k8s/prod/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: punchout-config
  namespace: punchout-prod
data:
  APP_ENVIRONMENT: "prod"
  SPRING_DATA_MONGODB_DATABASE: "punchout"
  NEXT_PUBLIC_API_URL: "https://api.punchout.company.com/api"
---
# k8s/prod/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: punchout-secrets
  namespace: punchout-prod
type: Opaque
stringData:
  MONGODB_URI: "mongodb+srv://user:password@prod-cluster.mongodb.net/punchout?retryWrites=true&w=majority"
  MONGODB_USERNAME: "punchout-prod-user"
  MONGODB_PASSWORD: "secure-password-here"
```

### Gateway Deployment

```yaml
# k8s/prod/gateway-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: punchout-gateway
  namespace: punchout-prod
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: punchout-gateway
  template:
    metadata:
      labels:
        app: punchout-gateway
        version: "1.0.0"
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchLabels:
                  app: punchout-gateway
              topologyKey: topology.kubernetes.io/zone
      containers:
      - name: gateway
        image: your-registry.azurecr.io/punchout-gateway:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9090
          name: http
        env:
        - name: APP_ENVIRONMENT
          valueFrom:
            configMapKeyRef:
              name: punchout-config
              key: APP_ENVIRONMENT
        - name: SPRING_DATA_MONGODB_HOST
          value: "prod-mongodb-service"
        - name: SPRING_DATA_MONGODB_PORT
          value: "27017"
        - name: SPRING_DATA_MONGODB_USERNAME
          valueFrom:
            secretKeyRef:
              name: punchout-secrets
              key: MONGODB_USERNAME
        - name: SPRING_DATA_MONGODB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: punchout-secrets
              key: MONGODB_PASSWORD
        - name: SPRING_DATA_MONGODB_DATABASE
          valueFrom:
            configMapKeyRef:
              name: punchout-config
              key: SPRING_DATA_MONGODB_DATABASE
        resources:
          requests:
            cpu: 300m
            memory: 600Mi
          limits:
            cpu: 1000m
            memory: 1200Mi
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 9090
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 9090
          initialDelaySeconds: 20
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
        volumeMounts:
        - name: tmp
          mountPath: /tmp
      volumes:
      - name: tmp
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: punchout-gateway
  namespace: punchout-prod
spec:
  type: ClusterIP
  selector:
    app: punchout-gateway
  ports:
  - port: 9090
    targetPort: 9090
    name: http
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: punchout-gateway
  namespace: punchout-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: punchout-gateway
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 60
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 75
---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: punchout-gateway
  namespace: punchout-prod
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: punchout-gateway
```

### Ingress Configuration

```yaml
# k8s/prod/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: punchout-ingress
  namespace: punchout-prod
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "30"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - punchout.company.com
    - api.punchout.company.com
    - gateway.punchout.company.com
    secretName: punchout-tls
  rules:
  # Frontend
  - host: punchout.company.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: punchout-ui-frontend
            port:
              number: 3000
  # UI Backend API
  - host: api.punchout.company.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: punchout-ui-backend
            port:
              number: 8080
  # Gateway (for B2B platforms)
  - host: gateway.punchout.company.com
    http:
      paths:
      - path: /punchout
        pathType: Prefix
        backend:
          service:
            name: punchout-gateway
            port:
              number: 9090
```

## 3. MongoDB Setup

### Managed MongoDB (Recommended)

**MongoDB Atlas:**
```bash
# 1. Create cluster (M10 or higher for prod)
# 2. Enable 3-node replica set across availability zones
# 3. Configure IP whitelist or VPC peering
# 4. Enable encryption at rest and in transit
# 5. Set up automated backups (daily snapshots, 30-day retention)
# 6. Enable point-in-time recovery (PITR)
```

**AWS DocumentDB:**
```bash
# 1. Create cluster with 3 instances across AZs
# 2. Configure VPC and security groups
# 3. Enable TLS
# 4. Set up automated backups
```

### MongoDB Indexes and TTL

```javascript
// mongo-init/create-indexes.js
db = db.getSiblingDB('punchout');

// Environment configs (unique per environment)
db.environment_configs.createIndex(
  { environment: 1 },
  { unique: true, background: true }
);

// Sessions indexes
db.punchout_sessions.createIndex(
  { sessionKey: 1 },
  { unique: true, background: true }
);
db.punchout_sessions.createIndex(
  { sessionDate: -1 },
  { background: true }
);
db.punchout_sessions.createIndex(
  { environment: 1, sessionDate: -1 },
  { background: true }
);

// Orders indexes
db.orders.createIndex(
  { orderId: 1 },
  { unique: true, background: true }
);
db.orders.createIndex(
  { orderDate: -1 },
  { background: true }
);
db.orders.createIndex(
  { customerId: 1, orderDate: -1 },
  { background: true }
);
db.orders.createIndex(
  { status: 1, orderDate: -1 },
  { background: true }
);

// Network requests indexes + TTL
db.network_requests.createIndex(
  { sessionKey: 1 },
  { background: true }
);
db.network_requests.createIndex(
  { orderId: 1 },
  { background: true }
);
db.network_requests.createIndex(
  { timestamp: -1 },
  { background: true }
);

// TTL index - Auto-delete network requests after 30 days
db.network_requests.createIndex(
  { timestamp: 1 },
  { expireAfterSeconds: 2592000, background: true }
);

print("Indexes created successfully");
```

**Deploy as Kubernetes Job:**
```yaml
# k8s/jobs/mongo-init-job.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: mongo-init
  namespace: punchout-prod
spec:
  template:
    spec:
      containers:
      - name: mongo-init
        image: mongo:7
        command:
        - sh
        - -c
        - |
          mongosh "$MONGODB_URI" < /scripts/create-indexes.js
          mongoimport --uri="$MONGODB_URI" --db=punchout --collection=environment_configs \
            --file=/data/mongodb-environment-configs-sample-data.json --jsonArray --upsert
        env:
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: punchout-secrets
              key: MONGODB_URI
        volumeMounts:
        - name: scripts
          mountPath: /scripts
        - name: data
          mountPath: /data
      volumes:
      - name: scripts
        configMap:
          name: mongo-scripts
      - name: data
        configMap:
          name: mongo-data
      restartPolicy: OnFailure
```

## 4. Application Configuration

### Gateway application.yml (Production)

```yaml
spring:
  application:
    name: punchout-gateway
  data:
    mongodb:
      uri: ${MONGODB_URI}
      # Or individual properties:
      # host: ${SPRING_DATA_MONGODB_HOST}
      # port: ${SPRING_DATA_MONGODB_PORT}
      # database: ${SPRING_DATA_MONGODB_DATABASE}
      # username: ${SPRING_DATA_MONGODB_USERNAME}
      # password: ${SPRING_DATA_MONGODB_PASSWORD}

server:
  port: 9090
  shutdown: graceful
  tomcat:
    threads:
      max: 200

app:
  environment: ${APP_ENVIRONMENT:prod}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.waters.punchout.gateway: INFO
  pattern:
    console: '%d{yyyy-MM-dd HH:MM:ss} [%thread] %-5level %logger{36} - %msg%n'
```

## 5. CI/CD Pipeline

### GitHub Actions Example

```yaml
# .github/workflows/deploy.yml
name: Build and Deploy

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: your-registry.azurecr.io
  
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Build and Test
        run: mvn clean verify
      
      - name: Run Integration Tests
        run: |
          docker-compose up -d mongodb
          ./run-integration-tests.sh
          docker-compose down
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: test-results/
  
  build-images:
    needs: build-and-test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v3
      
      - name: Docker meta
        id: meta
        uses: docker/metadata-action@v4
        with:
          images: |
            ${{ env.REGISTRY }}/punchout-gateway
            ${{ env.REGISTRY }}/punchout-ui-backend
            ${{ env.REGISTRY }}/punchout-ui-frontend
          tags: |
            type=sha
            type=semver,pattern={{version}}
            type=raw,value=latest,enable={{is_default_branch}}
      
      - name: Login to Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ secrets.REGISTRY_USERNAME }}
          password: ${{ secrets.REGISTRY_PASSWORD }}
      
      - name: Build and Push Gateway
        uses: docker/build-push-action@v4
        with:
          context: .
          file: punchout-gateway/Dockerfile
          push: true
          tags: ${{ env.REGISTRY }}/punchout-gateway:${{ steps.meta.outputs.version }}
      
      - name: Build and Push UI Backend
        uses: docker/build-push-action@v4
        with:
          context: .
          file: punchout-ui-backend/Dockerfile
          push: true
          tags: ${{ env.REGISTRY }}/punchout-ui-backend:${{ steps.meta.outputs.version }}
      
      - name: Build and Push Frontend
        uses: docker/build-push-action@v4
        with:
          context: punchout-ui-frontend
          file: punchout-ui-frontend/Dockerfile
          push: true
          tags: ${{ env.REGISTRY }}/punchout-ui-frontend:${{ steps.meta.outputs.version }}
  
  deploy-dev:
    needs: build-images
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: development
    steps:
      - uses: actions/checkout@v3
      
      - name: Configure kubectl
        uses: azure/setup-kubectl@v3
      
      - name: Set K8s context
        uses: azure/k8s-set-context@v3
        with:
          kubeconfig: ${{ secrets.KUBE_CONFIG }}
      
      - name: Deploy to Dev
        run: |
          kubectl set image deployment/punchout-gateway \
            gateway=${{ env.REGISTRY }}/punchout-gateway:${{ github.sha }} \
            -n punchout-dev
          kubectl set image deployment/punchout-ui-backend \
            backend=${{ env.REGISTRY }}/punchout-ui-backend:${{ github.sha }} \
            -n punchout-dev
          kubectl set image deployment/punchout-ui-frontend \
            frontend=${{ env.REGISTRY }}/punchout-ui-frontend:${{ github.sha }} \
            -n punchout-dev
      
      - name: Wait for rollout
        run: |
          kubectl rollout status deployment/punchout-gateway -n punchout-dev
          kubectl rollout status deployment/punchout-ui-backend -n punchout-dev
          kubectl rollout status deployment/punchout-ui-frontend -n punchout-dev
      
      - name: Run Smoke Tests
        run: |
          curl -f https://gateway-dev.punchout.company.com/punchout/health || exit 1
          curl -f https://api-dev.punchout.company.com/api/health || exit 1
          curl -f https://dev.punchout.company.com || exit 1
  
  deploy-prod:
    needs: build-images
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to Production
        # Same as dev but with -n punchout-prod
        run: |
          # Deployment commands for prod namespace
          kubectl set image deployment/punchout-gateway \
            gateway=${{ env.REGISTRY }}/punchout-gateway:${{ github.sha }} \
            -n punchout-prod
          # ... etc
```

## 6. Monitoring

### Prometheus Metrics

```yaml
# k8s/monitoring/servicemonitor.yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: punchout-gateway
  namespace: punchout-prod
spec:
  selector:
    matchLabels:
      app: punchout-gateway
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

### Grafana Dashboard

Key metrics to monitor:
- **Request Rate**: Requests per second
- **Error Rate**: 5xx errors per second
- **Latency**: p50, p95, p99 response times
- **JVM Metrics**: Heap usage, GC pause time
- **MongoDB**: Connection pool, query time
- **Network Requests**: Logged per minute
- **Auth Service**: Success rate, latency
- **Mule ESB**: Success rate, latency

### Alerting Rules

```yaml
# prometheus-rules.yaml
groups:
- name: punchout
  interval: 30s
  rules:
  - alert: HighErrorRate
    expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High 5xx error rate"
  
  - alert: HighLatency
    expr: histogram_quantile(0.95, http_server_requests_seconds_bucket) > 2
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "P95 latency above 2 seconds"
  
  - alert: MongoDBDown
    expr: mongodb_up == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "MongoDB is down"
```

## 7. Logging

### Structured Logging Configuration

```yaml
# Gateway application.yml
logging:
  pattern:
    console: '{"time":"%d{yyyy-MM-dd HH:mm:ss.SSS}","level":"%level","thread":"%thread","logger":"%logger","message":"%message","sessionKey":"${sessionKey:-}","correlationId":"${correlationId:-}"}%n'
```

### Fluent Bit DaemonSet

```yaml
# k8s/logging/fluentbit.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluent-bit-config
  namespace: kube-system
data:
  fluent-bit.conf: |
    [SERVICE]
        Parsers_File parsers.conf
    
    [INPUT]
        Name tail
        Path /var/log/containers/punchout-*.log
        Parser docker
        Tag kube.*
    
    [FILTER]
        Name kubernetes
        Match kube.*
        Merge_Log On
    
    [OUTPUT]
        Name opensearch
        Match kube.*
        Host ${OPENSEARCH_HOST}
        Port 443
        TLS On
        Index punchout-logs
```

## 8. Security

### Network Policies

```yaml
# k8s/prod/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: punchout-gateway-policy
  namespace: punchout-prod
spec:
  podSelector:
    matchLabels:
      app: punchout-gateway
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 9090
  egress:
  # Allow MongoDB
  - to:
    - podSelector:
        matchLabels:
          app: mongodb
    ports:
    - protocol: TCP
      port: 27017
  # Allow Auth Service (adjust IP/CIDR)
  - to:
    - ipBlock:
        cidr: 10.0.0.0/8
    ports:
    - protocol: TCP
      port: 443
  # Allow Mule ESB
  - to:
    - ipBlock:
        cidr: 10.0.0.0/8
    ports:
    - protocol: TCP
      port: 443
  # Allow DNS
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: UDP
      port: 53
```

### Pod Security

```yaml
# k8s/prod/pod-security-policy.yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: punchout-restricted
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
  - ALL
  runAsUser:
    rule: MustRunAsNonRoot
  seLinux:
    rule: RunAsAny
  fsGroup:
    rule: RunAsAny
  readOnlyRootFilesystem: true
```

## 9. Deployment Checklist

### Pre-Deployment

- [ ] Build and test all Docker images
- [ ] Run integration tests successfully
- [ ] Create/update Kubernetes manifests
- [ ] Configure secrets (MongoDB credentials, API keys)
- [ ] Set up MongoDB indexes and TTL
- [ ] Import environment configurations to MongoDB
- [ ] Configure TLS certificates
- [ ] Set up monitoring and alerting
- [ ] Configure log aggregation
- [ ] Review security policies
- [ ] Document rollback procedure

### Deployment Steps

1. **Deploy MongoDB** (if self-hosted)
2. **Run MongoDB init job** (indexes + environment configs)
3. **Deploy backend services:**
   - punchout-gateway
   - punchout-ui-backend
4. **Deploy frontend:**
   - punchout-ui-frontend
5. **Configure Ingress**
6. **Run smoke tests**
7. **Monitor dashboards**

### Post-Deployment

- [ ] Verify health endpoints responding
- [ ] Test punchout flow end-to-end
- [ ] Test order submission
- [ ] Check MongoDB connections
- [ ] Verify logs appearing in centralized system
- [ ] Verify metrics in Grafana
- [ ] Test from external B2B platform
- [ ] Document any issues or changes

## 10. Environment Management

### Environment Configurations

**Development:**
```bash
APP_ENVIRONMENT=dev
MONGODB_URI=mongodb://mongodb-dev:27017/punchout
```

**Staging:**
```bash
APP_ENVIRONMENT=stage
MONGODB_URI=mongodb+srv://stage-cluster.mongodb.net/punchout
```

**Production:**
```bash
APP_ENVIRONMENT=prod
MONGODB_URI=mongodb+srv://prod-cluster.mongodb.net/punchout
```

### Configuration Priority

1. **Environment Variables** (highest)
2. **MongoDB environment_configs** collection
3. **application.yml** (fallback)

## 11. Scaling Strategy

### Auto-scaling Configuration

**Gateway (high load):**
- Min replicas: 2
- Max replicas: 10
- Target CPU: 60%
- Target Memory: 75%

**UI Backend (medium load):**
- Min replicas: 2
- Max replicas: 5
- Target CPU: 60%

**Frontend (medium load):**
- Min replicas: 2
- Max replicas: 5
- Target CPU: 60%

### Resource Allocation

| Service | CPU Request | Memory Request | CPU Limit | Memory Limit |
|---------|-------------|----------------|-----------|--------------|
| Gateway | 300m | 600Mi | 1000m | 1200Mi |
| UI Backend | 200m | 400Mi | 800m | 800Mi |
| Frontend | 100m | 200Mi | 500m | 500Mi |

## 12. Disaster Recovery

### Backup Strategy

**MongoDB:**
- Daily automated snapshots
- 30-day retention
- Point-in-time recovery enabled
- Test restore monthly

**Application:**
- Source code in Git
- Docker images in registry
- Infrastructure as Code (Kubernetes manifests)

### Rollback Procedure

```bash
# Rollback to previous version
kubectl rollout undo deployment/punchout-gateway -n punchout-prod
kubectl rollout undo deployment/punchout-ui-backend -n punchout-prod
kubectl rollout undo deployment/punchout-ui-frontend -n punchout-prod

# Or rollback to specific revision
kubectl rollout undo deployment/punchout-gateway --to-revision=3 -n punchout-prod
```

## 13. Cost Optimization

1. **Right-size resources** - Start small, scale based on metrics
2. **Use spot instances** for non-prod environments
3. **Implement TTL** for network request logs (already configured)
4. **Compress log data** before shipping to centralized logging
5. **Use CDN** for frontend static assets
6. **Archive old data** to cheaper storage (S3 Glacier)

## 14. Quick Start Commands

### Build All Images
```bash
./build-docker-images.sh
```

### Deploy to Kubernetes
```bash
# Deploy to dev
kubectl apply -k k8s/overlays/dev/

# Deploy to prod
kubectl apply -k k8s/overlays/prod/
```

### Check Deployment Status
```bash
kubectl get pods -n punchout-prod
kubectl logs -f deployment/punchout-gateway -n punchout-prod
kubectl describe pod <pod-name> -n punchout-prod
```

### Scale Services
```bash
kubectl scale deployment/punchout-gateway --replicas=5 -n punchout-prod
```

## Summary

**Best Practice Production Deployment:**

✅ **Containers** - Docker multi-stage builds, non-root, minimal images
✅ **Orchestration** - Kubernetes with namespaces per environment
✅ **Database** - Managed MongoDB (Atlas/DocumentDB) with replica set
✅ **Configuration** - Environment variables + MongoDB-backed configs
✅ **Load Balancing** - NGINX Ingress with TLS
✅ **Scaling** - HPA with 2-10 replicas based on CPU/memory
✅ **High Availability** - Multi-AZ, PodDisruptionBudgets, rolling updates
✅ **Monitoring** - Prometheus + Grafana dashboards
✅ **Logging** - Structured JSON logs → OpenSearch/ELK
✅ **Security** - TLS everywhere, NetworkPolicies, non-root, secrets management
✅ **CI/CD** - Automated build/test/deploy with GitHub Actions
✅ **Backups** - Daily MongoDB snapshots, 30-day retention, PITR

This provides enterprise-grade deployment with high availability, security, and observability! 🚀

# PunchOut System Optimization Plan

## Executive Summary

This document outlines functional and non-functional optimizations for the PunchOut integration system, prioritized by impact and effort.

## Priority 1: Security & Compliance (🔴 Critical - Implement Immediately)

### 1.1 Stop Logging Secrets/Tokens
**Impact**: High security risk - credentials/tokens exposed in logs  
**Effort**: Small (0.5-1 day)

**Current Issues:**
- Auth requests/responses logged with passwords
- Tokens logged in network requests
- Encrypted passwords logged in error messages

**Actions:**
```java
// Add to NetworkRequestLogger
private String maskSecrets(String body) {
    if (body == null) return null;
    return body
        .replaceAll("(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1********$3")
        .replaceAll("(\"token\"\\s*:\\s*\")([^\"]+)(\")", "$1********$3")
        .replaceAll("(\"wuser_key\"\\s*:\\s*\")([^\"]+)(\")", "$1********$3")
        .replaceAll("(<password>)(.+?)(</password>)", "$1********$3")
        .replaceAll("(Set-Cookie:\\s*)(.*)", "$1[REDACTED]");
}
```

### 1.2 Validate Environment Values
**Impact**: Prevent injection/misconfiguration  
**Effort**: Small (0.5 day)

```java
// EnvironmentUtil.java
public class EnvironmentUtil {
    private static final Set<String> VALID_ENVS = Set.of("dev", "stage", "prod", "s4-dev");
    
    public static String normalize(String env) {
        if (env == null || !VALID_ENVS.contains(env.toLowerCase())) {
            log.warn("Invalid environment: {}, defaulting to dev", env);
            return "dev";
        }
        return env.toLowerCase();
    }
}
```

### 1.3 Fail Fast on Missing Encryption
**Impact**: Prevent plain text password exposure  
**Effort**: Small (1 hour)

```java
// In getAuthPassword()
if (encryptedPassword.startsWith("ENC(") && stringEncryptor == null) {
    throw new IllegalStateException(
        "Password is encrypted but Jasypt not configured. Set JASYPT_ENCRYPTOR_PASSWORD");
}
```

## Priority 2: Performance & Reliability (🟡 High - Next Sprint)

### 2.1 Add Timeouts, Retries & Circuit Breakers
**Impact**: Prevent cascading failures, improve reliability  
**Effort**: Medium (1-2 days)

**Add Resilience4j:**
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
    <version>2.1.0</version>
</dependency>
```

**Configure WebClient:**
```java
@Bean
public WebClient webClient(EnvironmentConfigService envService) {
    return WebClient.builder()
        .codecs(configurer -> configurer.defaultCodecs()
            .maxInMemorySize(10 * 1024 * 1024)) // 10MB
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create()
                .responseTimeout(Duration.ofMillis(envService.getTimeout("dev")))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        ))
        .build();
}
```

**Add Retry Logic:**
```java
@Retry(name = "authService", fallbackMethod = "authFallback")
@CircuitBreaker(name = "authService")
public String getAuthToken(PunchOutRequest request, String environment) {
    // existing logic
}
```

### 2.2 Enable Environment Config Caching
**Impact**: Reduce DB queries by ~80%, faster decryption  
**Effort**: Small (0.5 day)

```java
// Enable Caffeine cache
@Cacheable(value = "environmentConfig", key = "#environment")
public EnvironmentConfig getConfig(String environment) {
    // existing logic
}

@CacheEvict(value = "environmentConfig", key = "#config.environment")
public EnvironmentConfig saveConfig(EnvironmentConfig config) {
    // existing logic
}
```

```yaml
# application.yml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=100,expireAfterWrite=5m
```

### 2.3 MongoDB Indexes
**Impact**: 10-100x faster queries  
**Effort**: Small (2 hours)

```javascript
// Add indexes
db.punchout_sessions.createIndex({ sessionKey: 1 }, { unique: true })
db.punchout_sessions.createIndex({ environment: 1, sessionDate: -1 })
db.punchout_sessions.createIndex({ sessionDate: 1 }, { expireAfterSeconds: 7776000 }) // 90 days TTL

db.customer_onboarding.createIndex({ environment: 1, deployed: 1 })
db.customer_onboarding.createIndex({ customerName: 1, environment: 1 })

db.network_requests.createIndex({ sessionKey: 1, timestamp: -1 })
db.network_requests.createIndex({ timestamp: 1 }, { expireAfterSeconds: 7776000 }) // 90 days TTL

db.environment_configs.createIndex({ environment: 1 }, { unique: true })
```

## Priority 3: Observability (🟢 Medium - Future Sprint)

### 3.1 Add Metrics
**Impact**: Understand system behavior, identify bottlenecks  
**Effort**: Medium (1-2 days)

```java
@Autowired
private MeterRegistry meterRegistry;

// In AuthServiceClient
Timer.Sample sample = Timer.start(meterRegistry);
try {
    // auth call
    sample.stop(Timer.builder("auth.request")
        .tag("environment", environment)
        .tag("result", "success")
        .register(meterRegistry));
} catch (Exception e) {
    sample.stop(Timer.builder("auth.request")
        .tag("environment", environment)
        .tag("result", "error")
        .register(meterRegistry));
}
```

### 3.2 Add Distributed Tracing
**Impact**: Debug cross-service issues  
**Effort**: Medium (1-2 days)

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

### 3.3 Structured Logging
**Impact**: Better searchability, alerting  
**Effort**: Small (1 day)

```java
// Use structured logging
log.info("Auth token acquired", 
    kv("sessionKey", sessionKey),
    kv("environment", environment),
    kv("duration", duration),
    kv("success", true)
);
```

## Priority 4: Functional Enhancements (🔵 Medium)

### 4.1 Rate Limiting
**Impact**: Protect against abuse  
**Effort**: Small (1 day)

```java
@Bean
public RateLimiter rateLimiter() {
    return RateLimiter.create(100); // 100 req/sec per instance
}

// In controller
if (!rateLimiter.tryAcquire()) {
    throw new TooManyRequestsException();
}
```

### 4.2 Idempotency for Orders
**Impact**: Prevent duplicate orders  
**Effort**: Medium (1-2 days)

```java
// Generate deterministic order ID
String orderId = DigestUtils.sha256Hex(
    buyerCookie + orderTimestamp + items.hashCode()
);

// Upsert with unique index
orderRepository.upsert(orderId, order);
```

### 4.3 Health Check Implementation
**Impact**: Better orchestration, faster detection  
**Effort**: Small (0.5 day)

```java
@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            // Check auth service
            webClient.get().uri(healthCheckUrl).retrieve()
                .toEntity(String.class).block(Duration.ofSeconds(5));
            return Health.up()
                .withDetail("authService", "reachable")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("authService", "unreachable")
                .withException(e)
                .build();
        }
    }
}
```

## Priority 5: Code Quality (🟢 Low - Continuous)

### 5.1 Reduce Code Duplication
- Extract common converter logic to base class
- Centralize template placeholder replacement
- Unified error handling strategy

### 5.2 Improve Test Coverage
- Unit tests for each converter
- Integration tests for auth/mule flows
- Contract tests for external APIs

### 5.3 Documentation
- API documentation (Swagger/OpenAPI)
- Architecture decision records
- Runbooks for common operations

## Non-Functional Requirements Summary

| Category | Current State | Target State | Priority |
|----------|--------------|--------------|----------|
| **Security** | ⚠️ Passwords encrypted, but secrets in logs | ✅ No secrets in logs, validated inputs | 🔴 Critical |
| **Performance** | ⚠️ No caching, repeated DB calls | ✅ Config cached, indexed queries | 🟡 High |
| **Reliability** | ⚠️ No timeouts/retries | ✅ Timeouts, retries, circuit breakers | 🟡 High |
| **Scalability** | ⚠️ No rate limiting | ✅ Per-customer rate limits | 🔵 Medium |
| **Observability** | ⚠️ Basic logging only | ✅ Metrics, tracing, structured logs | 🟢 Medium |
| **Maintainability** | ⚠️ Some duplication | ✅ DRY, well-tested, documented | 🟢 Low |

## Quick Wins (Implement This Week)

1. **Mask secrets in logs** (2 hours)
2. **Add MongoDB indexes** (1 hour)
3. **Enable config caching** (2 hours)
4. **Add timeouts to WebClient** (2 hours)
5. **Validate environment values** (1 hour)

**Total: 1 day of effort for significant security and performance improvements**

## Recommended Implementation Order

**Week 1:**
- ✅ Security: Mask secrets, fail fast on missing encryption
- ✅ Performance: MongoDB indexes, config caching
- ✅ Reliability: Add timeouts to external calls

**Week 2:**
- Reliability: Add retry logic with exponential backoff
- Reliability: Implement circuit breakers
- Observability: Basic metrics (auth duration, success rate)

**Week 3:**
- Functional: Rate limiting
- Functional: Idempotent order processing
- Functional: Health check endpoints

**Week 4+:**
- Code quality: Reduce duplication
- Code quality: Increase test coverage
- Observability: Distributed tracing

## Metrics to Track

After optimizations, monitor:
- P95/P99 auth service response time
- Circuit breaker open/closed state
- Cache hit ratio for environment configs
- Database query duration
- Failed vs successful PunchOut sessions by environment

## Risk Mitigation

- Test all changes in dev environment first
- Enable feature flags for gradual rollout
- Have rollback plan ready
- Monitor error rates closely after deployment

See individual files for detailed implementation:
- `SECURITY_IMPROVEMENTS.md` - Security hardening details
- `PERFORMANCE_TUNING.md` - Performance optimization guide
- `OBSERVABILITY_SETUP.md` - Monitoring and metrics setup

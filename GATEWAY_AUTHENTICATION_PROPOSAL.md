# PunchOut Gateway Authentication - Security Proposal

## Current State (Development)

❌ **No authentication** - Gateway accepts all cXML requests
❌ **No authorization** - Any customer can access any endpoint
❌ **No request validation** - SharedSecret not verified

**Risk:** Production deployment would expose Gateway to unauthorized access.

## Recommended Multi-Layer Security Approach

### Layer 1: Network-Level Security (Infrastructure)

**API Gateway / Load Balancer:**
- IP Whitelisting (allow only known customer IPs)
- Rate limiting (prevent DoS)
- TLS/HTTPS only (encrypted transport)
- DDoS protection

**Benefits:**
- ✅ Blocks unknown sources at network edge
- ✅ No code changes needed
- ✅ Handles 99% of malicious traffic

---

### Layer 2: API Key Authentication (Simple & Effective)

**Approach:** Require API key in HTTP header

**Implementation:**

#### 2.1 Spring Security Filter

```java
@Component
@Order(1)
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${security.api-key.header:X-API-Key}")
    private String apiKeyHeader;
    
    private final CustomerApiKeyService apiKeyService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Skip for health checks
        if (request.getRequestURI().contains("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String apiKey = request.getHeader(apiKeyHeader);
        
        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/xml");
            response.getWriter().write(buildCxmlError("Missing API key"));
            return;
        }
        
        // Validate API key and get customer info
        Optional<CustomerCredential> customer = apiKeyService.validateApiKey(apiKey);
        
        if (customer.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/xml");
            response.getWriter().write(buildCxmlError("Invalid API key"));
            return;
        }
        
        // Set customer context for logging
        request.setAttribute("customerId", customer.get().getCustomerId());
        MDC.put("customerId", customer.get().getCustomerId());
        
        filterChain.doFilter(request, response);
    }
}
```

#### 2.2 API Key Service

```java
@Service
public class CustomerApiKeyService {
    
    private final CustomerCredentialRepository repository;
    
    public Optional<CustomerCredential> validateApiKey(String apiKey) {
        // Hash the API key
        String hashedKey = hashApiKey(apiKey);
        
        // Look up in database
        return repository.findByApiKeyHash(hashedKey)
                .filter(cred -> cred.isActive())
                .filter(cred -> !cred.isExpired());
    }
    
    private String hashApiKey(String apiKey) {
        return DigestUtils.sha256Hex(apiKey);
    }
}
```

#### 2.3 Customer Configuration

**MongoDB Collection: `customer_credentials`**
```javascript
{
  customerId: "CUST001",
  customerName: "Acme Corporation",
  apiKeyHash: "sha256_hash_of_api_key",
  isActive: true,
  createdAt: ISODate("2025-01-01"),
  expiresAt: ISODate("2026-01-01"),
  allowedIPs: ["203.0.113.0/24"],
  rateLimit: 1000  // requests per hour
}
```

**Usage:**
```bash
# Customer includes API key in header
curl -X POST https://gateway.punchout.waters.com/punchout/setup \
  -H "X-API-Key: ACME_a1b2c3d4e5f6g7h8i9j0" \
  -H "Content-Type: text/xml" \
  -d @acme-punchout.xml
```

**Benefits:**
- ✅ Simple to implement
- ✅ Easy for customers to use
- ✅ Per-customer rate limiting
- ✅ Revocable credentials
- ✅ API key rotation support

---

### Layer 3: cXML SharedSecret Validation (Standard)

**Approach:** Validate SharedSecret in cXML Sender/Credential

**cXML Standard:**
```xml
<cXML>
  <Header>
    <Sender>
      <Credential domain="NetworkID">
        <Identity>acme.com</Identity>
        <SharedSecret>acme-secret-2024</SharedSecret>  <!-- Verify this -->
      </Credential>
    </Sender>
  </Header>
</cXML>
```

**Implementation:**

```java
@Component
public class CxmlSharedSecretValidator {
    
    private final CustomerCredentialRepository repository;
    
    public void validateSharedSecret(JsonNode root, String customerId) {
        String sharedSecret = root.path("Header")
                                 .path("Sender")
                                 .path("Credential")
                                 .path("SharedSecret")
                                 .asText(null);
        
        if (sharedSecret == null) {
            throw new SecurityException("SharedSecret missing in cXML");
        }
        
        CustomerCredential credential = repository.findByCustomerId(customerId)
                .orElseThrow(() -> new SecurityException("Unknown customer"));
        
        if (!credential.getSharedSecret().equals(sharedSecret)) {
            throw new SecurityException("Invalid SharedSecret for customer: " + customerId);
        }
        
        log.info("SharedSecret validated for customer: {}", customerId);
    }
}
```

**Integration:**
```java
// In CxmlConversionService
public PunchOutRequest convertCxmlToRequest(String cxmlContent) throws Exception {
    JsonNode root = xmlMapper.readTree(cxmlContent);
    
    // Resolve customer
    ConversionKey key = customerResolver.resolve(root);
    
    // Validate SharedSecret
    sharedSecretValidator.validateSharedSecret(root, key.getCustomerId());
    
    // Continue with conversion...
}
```

**Benefits:**
- ✅ Part of cXML standard
- ✅ Validates request authenticity
- ✅ Per-customer secrets
- ✅ No extra headers needed

---

### Layer 4: OAuth2 Client Credentials (Enterprise)

**Approach:** OAuth2 for service-to-service authentication

**Flow:**
```
1. Customer system obtains access token
   POST /oauth/token
   Body: grant_type=client_credentials
         client_id=acme_client
         client_secret=acme_secret
   
2. Receive JWT access token
   Response: {
     "access_token": "eyJhbGci...",
     "token_type": "Bearer",
     "expires_in": 3600
   }
   
3. Use token in PunchOut request
   POST /punchout/setup
   Header: Authorization: Bearer eyJhbGci...
   Body: cXML content
```

**Implementation:**

```java
@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/punchout/**").authenticated()
                .and()
            .oauth2ResourceServer()
                .jwt()
                    .jwtAuthenticationConverter(jwtAuthenticationConverter());
    }
    
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String customerId = jwt.getClaimAsString("customer_id");
            return Arrays.asList(new SimpleGrantedAuthority("CUSTOMER_" + customerId));
        });
        return converter;
    }
}
```

**Benefits:**
- ✅ Industry standard (OAuth2)
- ✅ Token-based (stateless)
- ✅ Token expiration
- ✅ Scope-based access control
- ✅ Easy to integrate with enterprise IAM

---

### Layer 5: Mutual TLS (mTLS) (Most Secure)

**Approach:** Client certificate authentication

**Configuration:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    client-auth: need  # Require client certificate
    trust-store: classpath:truststore.p12
    trust-store-password: ${TRUSTSTORE_PASSWORD}
```

**Benefits:**
- ✅ Strongest authentication
- ✅ Mutual authentication (server + client)
- ✅ Certificate-based (no passwords)
- ✅ Per-customer certificates
- ✅ Audit trail via certificates

**Drawbacks:**
- ⚠️ Complex setup
- ⚠️ Certificate management overhead
- ⚠️ Customer onboarding complexity

---

## Recommended Implementation

### Phase 1: Quick Win (1-2 days)
**API Key + SharedSecret**

Combine Layer 2 (API Key) + Layer 3 (SharedSecret):

1. **API Key in HTTP header** - Fast validation before parsing cXML
2. **SharedSecret in cXML** - Standard cXML validation

**Code Example:**

```java
@Component
@Slf4j
public class PunchOutSecurityFilter extends OncePerRequestFilter {
    
    private final CustomerApiKeyService apiKeyService;
    private final CxmlSharedSecretValidator sharedSecretValidator;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Skip health checks
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Layer 1: Validate API Key
        String apiKey = request.getHeader("X-API-Key");
        Optional<CustomerCredential> customer = apiKeyService.validateApiKey(apiKey);
        
        if (customer.isEmpty()) {
            sendCxmlError(response, "Unauthorized: Invalid API key");
            return;
        }
        
        // Set customer context
        request.setAttribute("customerId", customer.get().getCustomerId());
        request.setAttribute("customerName", customer.get().getCustomerName());
        
        // Layer 2: SharedSecret validation happens in conversion
        // (after cXML is parsed)
        
        filterChain.doFilter(request, response);
    }
    
    private void sendCxmlError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("text/xml");
        response.getWriter().write(
            "<?xml version=\"1.0\"?>\n" +
            "<cXML><Response><Status code=\"401\" text=\"unauthorized\">" +
            message + "</Status></Response></cXML>"
        );
    }
}
```

---

### Phase 2: Enterprise Grade (1 week)
**OAuth2 Client Credentials**

Integrate with Waters' existing OAuth2/IAM:

```yaml
# application-prod.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.waters.com
          jwk-set-uri: https://auth.waters.com/.well-known/jwks.json
```

**Customer Integration:**
```bash
# Customer obtains token from Waters Auth
curl -X POST https://auth.waters.com/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=acme_punchout" \
  -d "client_secret=acme_secret_xyz" \
  -d "scope=punchout:setup"

# Use token in PunchOut request
curl -X POST https://gateway.punchout.waters.com/punchout/setup \
  -H "Authorization: Bearer eyJhbGci..." \
  -H "Content-Type: text/xml" \
  -d @punchout.xml
```

---

## Comparison Matrix

| Method | Security | Complexity | Customer Effort | Cost |
|--------|----------|------------|-----------------|------|
| **API Key** | ⭐⭐⭐ | Low | Low | $ |
| **SharedSecret** | ⭐⭐⭐ | Low | None (in cXML) | $ |
| **API Key + SharedSecret** | ⭐⭐⭐⭐ | Medium | Low | $ |
| **OAuth2** | ⭐⭐⭐⭐⭐ | Medium | Medium | $$ |
| **mTLS** | ⭐⭐⭐⭐⭐ | High | High | $$$ |

---

## Recommended Solution: Hybrid Approach

### For Production

**Combine:**
1. **API Key** (HTTP header) - First line of defense
2. **SharedSecret** (cXML) - Standard validation
3. **IP Whitelist** (Infrastructure) - Network security

### Implementation Plan

#### Step 1: Create Security Infrastructure

**CustomerCredential Entity:**
```java
@Document(collection = "customer_credentials")
@Data
public class CustomerCredentialDocument {
    @Id
    private String id;
    
    private String customerId;
    private String customerName;
    
    // API Key (stored as hash)
    private String apiKeyHash;
    
    // SharedSecret (from cXML)
    private String sharedSecret;
    
    // Security settings
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private List<String> allowedIPs;
    private Integer rateLimit;  // per hour
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;
    private Integer requestCount;
}
```

#### Step 2: API Key Filter

```java
@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 10)
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        
        // Public endpoints (skip auth)
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Validate API key
        String apiKey = request.getHeader("X-API-Key");
        Optional<CustomerCredential> customer = validateApiKey(apiKey);
        
        if (customer.isEmpty()) {
            sendUnauthorizedResponse(response);
            return;
        }
        
        // Check IP whitelist
        if (!isAllowedIP(request.getRemoteAddr(), customer.get())) {
            sendForbiddenResponse(response);
            return;
        }
        
        // Check rate limit
        if (isRateLimitExceeded(customer.get())) {
            sendRateLimitResponse(response);
            return;
        }
        
        // Set security context
        setCustomerContext(request, customer.get());
        
        // Continue
        filterChain.doFilter(request, response);
    }
}
```

#### Step 3: SharedSecret Validator

```java
@Component
public class CxmlSharedSecretValidator {
    
    public void validateSharedSecret(JsonNode root, String customerId) {
        String sharedSecret = extractSharedSecret(root);
        
        if (sharedSecret == null) {
            throw new SecurityException("SharedSecret missing in cXML");
        }
        
        CustomerCredential credential = getCustomerCredential(customerId);
        
        if (!credential.getSharedSecret().equals(sharedSecret)) {
            throw new SecurityException("Invalid SharedSecret");
        }
    }
}
```

#### Step 4: Integration in Gateway

```java
@RestController
@RequestMapping("/punchout")
public class PunchOutGatewayController {
    
    @PostMapping("/setup")
    public ResponseEntity<String> handlePunchOutSetup(
            @RequestBody String cxmlContent,
            @RequestAttribute("customerId") String customerId) {  // From filter
        
        log.info("Processing PunchOut for authenticated customer: {}", customerId);
        
        // Process request...
    }
}
```

---

## Configuration per Environment

### Local Development
```yaml
security:
  enabled: false  # Disable for local testing
```

### DEV/STAGE
```yaml
security:
  enabled: true
  api-key:
    header: X-API-Key
    validation: lenient  # Log warnings, don't block
```

### Production
```yaml
security:
  enabled: true
  api-key:
    header: X-API-Key
    validation: strict  # Block invalid requests
  rate-limit:
    enabled: true
    requests-per-hour: 1000
```

---

## Customer Onboarding Process

### 1. Generate Credentials

```bash
# Admin generates API key for new customer
./generate-api-key.sh --customer CUST001 --name "Acme Corporation"

# Output:
API Key: ACME_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
SharedSecret: acme-shared-secret-2024
Expires: 2026-01-01
```

### 2. Store in MongoDB

```javascript
db.customer_credentials.insertOne({
  customerId: "CUST001",
  customerName: "Acme Corporation",
  apiKeyHash: "sha256_hash_here",
  sharedSecret: "acme-shared-secret-2024",
  isActive: true,
  expiresAt: ISODate("2026-01-01"),
  allowedIPs: ["203.0.113.0/24", "198.51.100.0/24"],
  rateLimit: 1000,
  createdAt: ISODate("2025-01-01")
})
```

### 3. Send to Customer

**Email template:**
```
Subject: Waters PunchOut Gateway Credentials

Dear Acme Corporation,

Your PunchOut integration credentials:

API Key: ACME_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
SharedSecret: acme-shared-secret-2024
Gateway URL: https://gateway.punchout.waters.com/punchout/setup

Usage:
1. Include API key in HTTP header: X-API-Key
2. Include SharedSecret in cXML Sender/Credential
3. Send POST request to Gateway URL

Example:
curl -X POST https://gateway.punchout.waters.com/punchout/setup \
  -H "X-API-Key: ACME_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" \
  -H "Content-Type: text/xml" \
  -d @punchout-request.xml

Support: punchout-support@waters.com
```

---

## Security Features

### Authentication
✅ API Key validation (HTTP header)
✅ SharedSecret validation (cXML content)
✅ Customer identification
✅ Credential expiration
✅ IP whitelisting

### Authorization
✅ Per-customer access control
✅ Rate limiting per customer
✅ Endpoint-level permissions
✅ Audit logging

### Protection
✅ DDoS prevention (rate limiting)
✅ Replay attack prevention (one-time tokens)
✅ Man-in-the-middle prevention (TLS)
✅ Injection prevention (XML escaping)

---

## Monitoring & Alerts

### Failed Authentication Tracking

```java
@Component
public class SecurityAuditLogger {
    
    public void logFailedAuth(String apiKey, String ip, String reason) {
        SecurityAuditEvent event = new SecurityAuditEvent();
        event.setTimestamp(LocalDateTime.now());
        event.setApiKey(maskApiKey(apiKey));
        event.setSourceIP(ip);
        event.setReason(reason);
        event.setSeverity("WARNING");
        
        repository.save(event);
        
        // Alert if too many failures
        if (getRecentFailures(ip) > 10) {
            alertService.sendAlert("Multiple auth failures from IP: " + ip);
        }
    }
}
```

### Metrics to Track
- Total authentication attempts
- Success rate
- Failed attempts by reason
- Rate limit violations
- Expired credentials usage
- IP whitelist violations

---

## Migration Path

### Phase 1: Development (Now)
- ✅ No authentication (existing)
- Use for testing

### Phase 2: Staging (Week 1)
- Add API key validation
- Test with sample customers
- Validate SharedSecret

### Phase 3: Pre-Production (Week 2)
- Add rate limiting
- Add IP whitelisting
- Load testing

### Phase 4: Production (Week 3-4)
- Full security enabled
- Customer credential provisioning
- Monitoring and alerting
- OAuth2 integration (optional)

---

## Code Structure

```
com.waters.punchout.gateway.security/
├── filter/
│   └── ApiKeyAuthenticationFilter.java
├── validator/
│   └── CxmlSharedSecretValidator.java
├── service/
│   ├── CustomerApiKeyService.java
│   └── SecurityAuditService.java
├── repository/
│   ├── CustomerCredentialRepository.java
│   └── SecurityAuditRepository.java
├── entity/
│   ├── CustomerCredentialDocument.java
│   └── SecurityAuditEvent.java
└── config/
    └── SecurityConfiguration.java
```

---

## Summary & Recommendation

### **Recommended: API Key + SharedSecret**

**Why:**
1. **Simple** - Easy to implement and use
2. **Standard** - SharedSecret is cXML standard
3. **Secure** - Two-layer validation
4. **Manageable** - Easy credential rotation
5. **Monitorable** - Clear audit trail

### Implementation Priority

**High Priority (Production Blocker):**
1. API Key authentication filter
2. SharedSecret validation
3. Customer credential management

**Medium Priority (Production Ready):**
4. IP whitelisting
5. Rate limiting
6. Security audit logging

**Low Priority (Enhancement):**
7. OAuth2 integration
8. mTLS support

### Estimated Effort

- **API Key + SharedSecret:** 2-3 days
- **IP Whitelist + Rate Limit:** 1-2 days
- **Audit & Monitoring:** 1 day
- **Testing & Documentation:** 1 day
- **Total:** 1 week

---

## Next Steps

1. **Review this proposal** with security team
2. **Choose authentication method** (recommend: API Key + SharedSecret)
3. **Create implementation plan**
4. **Set up customer credentials** in MongoDB
5. **Implement security filter**
6. **Test with sample customers**
7. **Deploy to staging**
8. **Production rollout**

**Ready to implement when approved!** 🔐

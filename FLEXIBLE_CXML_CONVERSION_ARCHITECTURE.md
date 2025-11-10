# Flexible cXML Conversion Architecture

## Executive Summary

Transform the cXML conversion from a rigid, one-size-fits-all approach to a **flexible, extensible, customer-specific system** using **Strategy Pattern** + **Template Method** + **Configuration-Driven Resolution**.

## Current Problems

### 1. **Single Converter for All Customers**
```java
// Current: One class handles all customers the same way
public class CxmlToJsonConverter {
    public PunchOutRequest convertCxmlToRequest(String cxmlContent) {
        // Same logic for everyone
    }
}
```

**Issues:**
- ❌ Cannot handle customer-specific requirements
- ❌ Different credential domains (NetworkID vs DUNS vs AribaNetworkUserId)
- ❌ Different extrinsic field mappings
- ❌ No versioning support
- ❌ Adding new customer requires modifying existing code

### 2. **Real-World Customer Differences**

**Acme Corporation:**
- Uses **DUNS** credentials in production
- Requires **CostCenter** extrinsic
- Session key = BuyerCookie + "-" + ToIdentity

**TechCorp Industries:**
- Uses **AribaNetworkUserId** credentials
- Email at `Contact/EmailAddress` (not `Contact/Email`)
- Requires **CompanyCode** and **UniqueName** extrinsics

**Global Solutions:**
- Uses **NetworkID** credentials
- Requires **Region** extrinsic (EMEA/APAC/AMER)
- Different validation rules for European customers

## Proposed Solution

### Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                   CxmlConversionService                       │
│                      (Orchestrator)                           │
└───────┬────────────────────────────┬────────────────┬────────┘
        │                            │                │
        ▼                            ▼                ▼
┌───────────────┐         ┌─────────────────┐  ┌──────────────┐
│ DialectDetector│         │CustomerResolver│  │ConverterRegistry│
│                │         │                 │  │              │
│ ARIBA/SAP/OCI  │         │ Config-driven   │  │ Auto-discover│
└───────────────┘         └─────────────────┘  └──────────────┘
                                   │                    │
                                   ▼                    ▼
                          ┌─────────────────┐  ┌──────────────┐
                          │ConversionKey    │  │  Strategies  │
                          │(customer,version)│  │              │
                          └─────────────────┘  └──────────────┘
                                                        │
                          ┌─────────────────────────────┴──────┐
                          │                                     │
                ┌─────────▼────────┐                ┌──────────▼────────┐
                │  BaseConverter   │                │ CustomerConverters │
                │  (Template)      │                │                   │
                │                  │                │ - AcmeV1Converter │
                │ Common Logic:    │                │ - TechCorpV2Converter
                │ - Extract fields │                │ - GlobalV1Converter
                │ - Map extrinsics │                │                   │
                │ - Hooks for      │                │ Override hooks:   │
                │   customization  │                │ - customize()     │
                │                  │                │ - validate()      │
                └──────────────────┘                └───────────────────┘
```

### Two-Layer Design

**Layer 1: Dialect Normalization**
- Detects dialect (Ariba, SAP, OCI)
- Normalizes structural differences
- Produces consistent JsonNode

**Layer 2: Customer Strategy**
- Base converter with common logic
- Customer-specific converters with overrides
- Config-driven field mappings

## Implementation Plan

### Phase 1: Core Infrastructure (High Priority)

#### 1.1 Create Package Structure
```
com.waters.punchout.gateway.converter/
├── CxmlConversionService.java       # Main orchestrator
├── dialect/
│   ├── Dialect.java                 # Enum: ARIBA, SAP, OCI
│   ├── DialectDetector.java         # Detect from UserAgent
│   └── CxmlDialectAdapter.java      # Interface (start with identity)
├── strategy/
│   ├── PunchOutConverterStrategy.java   # Interface
│   ├── BaseConverter.java               # Template Method
│   ├── ConverterRegistry.java           # Auto-discover strategies
│   └── customers/
│       ├── DefaultV1Converter.java      # Default converter
│       ├── AcmeV1Converter.java         # Acme-specific
│       ├── TechCorpV2Converter.java     # TechCorp-specific
│       └── GlobalV1Converter.java       # Global Solutions
├── resolve/
│   ├── CustomerResolver.java        # Match cXML to customer
│   ├── ConversionKey.java            # (customerId, version)
│   ├── CustomerConfig.java           # Config per customer
│   └── MatchCriteria.java            # Matching rules
└── config/
    └── PunchoutConversionProperties.java  # @ConfigurationProperties
```

#### 1.2 Define Interfaces

**PunchOutConverterStrategy.java:**
```java
public interface PunchOutConverterStrategy {
    String customerId();
    String version();
    PunchOutRequest convert(JsonNode root, ConversionContext ctx) throws Exception;
    default void validate(PunchOutRequest request, ConversionContext ctx) {}
}
```

**ConversionContext.java:**
```java
@Data
public class ConversionContext {
    private final ConversionKey key;
    private final Dialect dialect;
    private final JsonNode normalizedRoot;
    private final String originalCxml;
    private final CustomerConfig customerConfig;
}
```

#### 1.3 Base Converter (Template Method)

```java
@Slf4j
public abstract class BaseConverter implements PunchOutConverterStrategy {
    
    protected final ObjectMapper jsonMapper = new ObjectMapper();
    protected final XmlMapper xmlMapper = new XmlMapper();
    
    @Override
    public final PunchOutRequest convert(JsonNode root, ConversionContext ctx) throws Exception {
        log.debug("Converting cXML for customer: {}, version: {}", customerId(), version());
        
        // Step 1: Extract common fields
        PunchOutRequest request = buildCommon(root, ctx);
        
        // Step 2: Apply configuration-driven mappings
        applyConfigMappings(request, root, ctx);
        
        // Step 3: Customer-specific customization hook
        customize(request, root, ctx);
        
        // Step 4: Validation
        validate(request, ctx);
        
        return request;
    }
    
    protected PunchOutRequest buildCommon(JsonNode root, ConversionContext ctx) {
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        
        PunchOutRequest request = new PunchOutRequest();
        
        // Extract BuyerCookie as session key
        String buyerCookie = requestNode.path("BuyerCookie").asText();
        if (buyerCookie == null || buyerCookie.isEmpty()) {
            buyerCookie = generateSessionKey();
        }
        request.setSessionKey(buyerCookie);
        request.setBuyerCookie(buyerCookie);
        request.setOperation(requestNode.path("operation").asText("create"));
        request.setTimestamp(LocalDateTime.now());
        
        // Extract contact email
        JsonNode email = requestNode.path("Contact").path("Email");
        if (!email.isMissingNode()) {
            request.setContactEmail(email.asText());
        }
        
        // Extract cart return URL
        JsonNode browserFormPost = requestNode.path("BrowserFormPost").path("URL");
        if (!browserFormPost.isMissingNode()) {
            request.setCartReturnUrl(browserFormPost.asText());
        }
        
        // Extract identities
        JsonNode headerNode = root.path("Header");
        request.setFromIdentity(extractIdentity(headerNode.path("From")));
        request.setToIdentity(extractIdentity(headerNode.path("To")));
        request.setSenderIdentity(extractIdentity(headerNode.path("Sender")));
        
        // Extract all extrinsics into a map
        request.setExtrinsics(extractExtrinsics(requestNode));
        
        return request;
    }
    
    protected void applyConfigMappings(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        CustomerConfig config = ctx.getCustomerConfig();
        if (config == null || request.getExtrinsics() == null) return;
        
        // Map extrinsics to request fields
        Map<String, String> mappings = config.getExtrinsicToField();
        if (mappings != null) {
            mappings.forEach((extrinsicName, fieldName) -> {
                String value = request.getExtrinsics().get(extrinsicName);
                if (value != null) {
                    applyField(request, fieldName, value);
                }
            });
        }
    }
    
    // Hook for customer-specific customization
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        // Default: no customization
    }
    
    // Helper methods
    protected Map<String, String> extractExtrinsics(JsonNode requestNode) {
        // Extract all Extrinsic elements
    }
    
    protected String extractIdentity(JsonNode credentialNode) {
        // Extract Identity from Credential
    }
    
    protected String generateSessionKey() {
        return "SESSION_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
```

### Phase 2: Customer Strategies (Medium Priority)

#### Example: Acme V1 Converter

```java
@Component
@Slf4j
public class AcmeV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "acme";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying Acme-specific customizations");
        
        // Acme uses composite session key
        String toIdentity = request.getToIdentity();
        if (toIdentity != null) {
            String customSessionKey = request.getBuyerCookie() + "-" + toIdentity;
            request.setSessionKey(customSessionKey);
            log.debug("Acme session key: {}", customSessionKey);
        }
        
        // Extract CostCenter to dedicated field (if you add it to PunchOutRequest)
        if (request.getExtrinsics() != null) {
            String costCenter = request.getExtrinsics().get("CostCenter");
            if (costCenter != null) {
                // request.setCostCenter(costCenter);  // If field exists
                log.debug("Acme CostCenter: {}", costCenter);
            }
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating Acme requirements");
        
        // Require CostCenter extrinsic
        if (request.getExtrinsics() == null || 
            request.getExtrinsics().get("CostCenter") == null) {
            throw new IllegalArgumentException("Acme requires CostCenter extrinsic");
        }
        
        // Require DUNS or NetworkID domain (production check)
        String fromIdentity = request.getFromIdentity();
        if (fromIdentity == null || fromIdentity.isEmpty()) {
            throw new IllegalArgumentException("Acme requires From identity");
        }
    }
}
```

#### Example: TechCorp V2 Converter

```java
@Component
@Slf4j
public class TechCorpV2Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "techcorp";
    }
    
    @Override
    public String version() {
        return "v2";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying TechCorp v2 customizations");
        
        // TechCorp uses EmailAddress instead of Email
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        JsonNode emailAddress = requestNode.path("Contact").path("EmailAddress");
        if (!emailAddress.isMissingNode()) {
            request.setContactEmail(emailAddress.asText());
            log.debug("TechCorp email from EmailAddress: {}", emailAddress.asText());
        }
        
        // Extract CompanyCode
        if (request.getExtrinsics() != null) {
            String companyCode = request.getExtrinsics().get("CompanyCode");
            if (companyCode != null) {
                log.debug("TechCorp CompanyCode: {}", companyCode);
            }
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        // Require CompanyCode and UniqueName
        if (request.getExtrinsics() == null) {
            throw new IllegalArgumentException("TechCorp requires extrinsics");
        }
        
        if (request.getExtrinsics().get("CompanyCode") == null) {
            throw new IllegalArgumentException("TechCorp requires CompanyCode extrinsic");
        }
        
        if (request.getExtrinsics().get("UniqueName") == null) {
            throw new IllegalArgumentException("TechCorp requires UniqueName extrinsic");
        }
    }
}
```

### Phase 3: Configuration (application.yml)

```yaml
punchout:
  conversion:
    customers:
      # Default converter (fallback)
      - id: default
        version: v1
        dialect: UNKNOWN
        match: {}
        
      # Acme Corporation
      - id: acme
        version: v1
        dialect: ARIBA
        match:
          fromDomain: DUNS
          fromIdentityPattern: "ACME.*"
        extrinsicToField:
          CostCenter: costCenter
          Department: department
        requiredExtrinsics:
          - CostCenter
        allowedCredentialDomains:
          - DUNS
          - NetworkID
          
      # TechCorp Industries
      - id: techcorp
        version: v2
        dialect: OCI
        match:
          senderUserAgentPattern: ".*TechCorp.*"
          toIdentityPattern: "TC-.*"
        extrinsicToField:
          CompanyCode: companyCode
          UniqueName: uniqueName
        requiredExtrinsics:
          - CompanyCode
          - UniqueName
        allowedCredentialDomains:
          - AribaNetworkUserId
          
      # Global Solutions
      - id: global
        version: v1
        dialect: ARIBA
        match:
          fromIdentityPattern: "GLOBAL.*"
        extrinsicToField:
          Region: region
        requiredExtrinsics:
          - Region
        allowedRegions:
          - EMEA
          - APAC
          - AMER
```

## Benefits

### 1. **Easy to Add New Customers**

**Config-Only (Simple Customer):**
```yaml
# Add to application.yml - no code needed!
punchout:
  conversion:
    customers:
      - id: newcustomer
        version: v1
        match:
          fromIdentityPattern: "NEW.*"
        extrinsicToField:
          CostCode: costCode
        requiredExtrinsics:
          - CostCode
```

**Custom Converter (Complex Customer):**
```java
// Only if special logic needed
@Component
public class NewCustomerV1Converter extends BaseConverter {
    public String customerId() { return "newcustomer"; }
    public String version() { return "v1"; }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        // Special logic here
    }
}
```

### 2. **Versioning Support**

```java
// Support multiple versions per customer
- AcmeV1Converter (legacy format)
- AcmeV2Converter (new format)

// Configuration determines which version to use
match:
  senderUserAgentPattern: ".*Acme v2.*"  # Use v2
```

### 3. **Separation of Concerns**

- **DialectDetector** - Identifies cXML dialect
- **CustomerResolver** - Determines which customer
- **ConverterRegistry** - Finds the right converter
- **BaseConverter** - Common extraction logic
- **Customer Converters** - Specific overrides

### 4. **Configuration-Driven**

Most customizations via YAML:
- Extrinsic mappings
- Required fields
- Validation rules
- Matching criteria

Code only when truly custom logic needed.

### 5. **Testability**

```java
// Test each converter independently
@Test
void testAcmeV1Conversion() {
    String cxml = loadFixture("acme-v1-sample.xml");
    PunchOutRequest result = conversionService.convert(cxml);
    
    assertThat(result.getCustomerId()).isEqualTo("acme");
    assertThat(result.getExtrinsics()).containsKey("CostCenter");
    assertThat(result.getSessionKey()).matches(".*-ACME.*");
}

@Test
void testTechCorpV2Validation() {
    String cxml = loadFixture("techcorp-v2-missing-companycode.xml");
    
    assertThatThrownBy(() -> conversionService.convert(cxml))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CompanyCode");
}
```

## Migration Path

### Step 1: Refactor Current Code (2-3 hours)
1. Create new package structure
2. Extract current logic to `BaseConverter`
3. Create `DefaultV1Converter` with current behavior
4. Create `CxmlConversionService` orchestrator
5. Wire everything up
6. Test with existing flows

### Step 2: Add Customer Strategies (1 hour each)
1. Create `AcmeV1Converter`
2. Create `TechCorpV2Converter`  
3. Create `GlobalV1Converter`
4. Add configuration in application.yml
5. Write tests

### Step 3: Deploy & Verify (1 hour)
1. Test with Postman
2. Test Developer PunchOut page
3. Verify all customers work
4. Check logs

## Example Usage

### Scenario: Adding Innovation Labs

**Option A: Config Only (Simple)**
```yaml
punchout:
  conversion:
    customers:
      - id: innovationlabs
        version: v1
        match:
          fromIdentityPattern: "INNO.*"
        extrinsicToField:
          Lab: labCode
        requiredExtrinsics:
          - Lab
```
**Time: 5 minutes**

**Option B: Custom Converter (Complex)**
```java
@Component
public class InnovationLabsV1Converter extends BaseConverter {
    public String customerId() { return "innovationlabs"; }
    public String version() { return "v1"; }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        // Special logic: Session key includes lab code
        String lab = request.getExtrinsics().get("Lab");
        if (lab != null) {
            request.setSessionKey(request.getBuyerCookie() + "-LAB-" + lab);
        }
    }
}
```
**Time: 30 minutes**

## Comparison

### Before (Current)

```java
// One converter for all
class CxmlToJsonConverter {
    public PunchOutRequest convert(String cxml) {
        // Same for everyone
        // No customization
        // Hard to maintain
    }
}
```

**Problems:**
- ❌ Cannot handle customer differences
- ❌ Adding customer requires changing core code
- ❌ No versioning
- ❌ Difficult to test customer-specific logic

### After (Proposed)

```java
// Base converter + customer strategies
abstract class BaseConverter {
    // Common logic
    protected void customize(...) {} // Hook
}

@Component
class AcmeV1Converter extends BaseConverter {
    // Acme-specific logic
}

@Component  
class TechCorpV2Converter extends BaseConverter {
    // TechCorp-specific logic
}
```

**Benefits:**
- ✅ Clean separation per customer
- ✅ Easy to add new customers
- ✅ Versioning support
- ✅ Configuration-driven
- ✅ Testable in isolation

## Recommendation

### Implement in 3 Phases:

**Phase 1: Foundation (Now)**
- Core architecture
- BaseConverter
- DefaultV1Converter
- Registry and resolver

**Phase 2: Customer Strategies (Next)**
- Acme, TechCorp, Global converters
- Configuration
- Tests

**Phase 3: Advanced (Future)**
- Dialect adapters (if needed)
- Rule engine (if needed)
- API versioning

### Start Small

Begin with:
1. Default converter (current behavior)
2. One custom converter (e.g., Acme)
3. Config-driven resolution
4. Prove the pattern works
5. Add more customers incrementally

## Summary

✅ **Strategy Pattern** - Customer-specific converters
✅ **Template Method** - Common logic + hooks
✅ **Configuration-Driven** - YAML-based rules
✅ **Auto-Discovery** - Spring component scanning
✅ **Versioning** - Support customer v1, v2, etc.
✅ **Testable** - Unit tests per customer
✅ **Extensible** - Add customers without changing existing code
✅ **Maintainable** - Clear separation of concerns

This architecture would make the conversion system **production-ready** for handling multiple customers with different requirements! 🚀

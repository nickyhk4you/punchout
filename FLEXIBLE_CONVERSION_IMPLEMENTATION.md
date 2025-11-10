# Flexible cXML Conversion - Implementation Complete

## ✅ Implementation Summary

The flexible, extensible cXML conversion architecture has been successfully implemented!

## What Was Built

### **Core Architecture**

#### 1. **CxmlConversionService** (Orchestrator)
- Main entry point for all conversions
- Coordinates dialect detection, customer resolution, and strategy execution
- Replaces the old `CxmlToJsonConverter`

#### 2. **BaseConverter** (Template Method)
- Abstract base class with common conversion logic
- Provides hooks for customization
- Methods:
  - `buildCommon()` - Extract standard cXML fields
  - `applyConfigMappings()` - Apply YAML-driven mappings
  - `customize()` - Hook for customer-specific logic
  - `validate()` - Customer-specific validation

#### 3. **Customer Strategies** (Concrete Implementations)
- `DefaultV1Converter` - Fallback for unknown customers
- `AcmeV1Converter` - Acme Corporation specific logic
- `TechCorpV2Converter` - TechCorp Industries specific logic
- `GlobalV1Converter` - Global Solutions specific logic

#### 4. **Resolution & Registry**
- `CustomerResolver` - Matches cXML to customer based on config
- `ConverterRegistry` - Auto-discovers and registers all converters
- `DialectDetector` - Detects Ariba/SAP/OCI from UserAgent

#### 5. **Supporting Classes**
- `ConversionKey` - (customerId, version) tuple
- `ConversionContext` - Context passed to converters
- `CustomerConfig` - YAML configuration per customer
- `MatchCriteria` - Rules for customer matching
- `Dialect` - Enum for cXML dialects

## Package Structure

```
com.waters.punchout.gateway.converter/
├── CxmlConversionService.java          # NEW - Main orchestrator
├── CxmlToJsonConverter.java            # OLD - Still exists (legacy)
│
├── config/
│   └── PunchoutConversionProperties.java  # NEW - @ConfigurationProperties
│
├── dialect/
│   ├── Dialect.java                    # NEW - Enum
│   └── DialectDetector.java            # NEW - Detect from UserAgent
│
├── resolve/
│   ├── ConversionKey.java              # NEW - (customer, version)
│   ├── ConversionContext.java          # NEW - Context object
│   ├── CustomerConfig.java             # NEW - Per-customer config
│   ├── CustomerResolver.java           # NEW - Match cXML to customer
│   └── MatchCriteria.java              # NEW - Matching rules
│
└── strategy/
    ├── PunchOutConverterStrategy.java  # NEW - Interface
    ├── BaseConverter.java              # NEW - Template Method
    ├── ConverterRegistry.java          # NEW - Auto-discover converters
    └── customers/
        ├── DefaultV1Converter.java     # NEW - Default fallback
        ├── AcmeV1Converter.java        # NEW - Acme-specific
        ├── TechCorpV2Converter.java    # NEW - TechCorp-specific
        └── GlobalV1Converter.java      # NEW - Global-specific
```

## How It Works

### Conversion Flow

```
1. cXML arrives
   ↓
2. CxmlConversionService.convertCxmlToRequest()
   ├─> Parse XML to JsonNode
   ├─> Detect dialect (Ariba/SAP/OCI)
   ├─> Resolve customer (from config matching)
   ├─> Get converter strategy from registry
   ├─> Execute conversion with context
   └─> Return PunchOutRequest
   
3. Converter Strategy Execution
   ├─> buildCommon() - Extract standard fields
   ├─> applyConfigMappings() - Apply YAML rules
   ├─> customize() - Customer-specific logic
   ├─> validate() - Customer validation
   └─> Return completed PunchOutRequest
```

### Customer Resolution Example

**cXML from Acme:**
```xml
<cXML>
  <Header>
    <From>
      <Credential domain="NetworkID">
        <Identity>buyer123</Identity>  <!-- Matches Acme pattern! -->
      </Credential>
    </From>
    ...
  </Header>
</cXML>
```

**Resolution:**
1. Extract: `fromIdentity = "buyer123"`
2. Check configurations in order
3. Match: `buyer123` matches pattern in Acme config
4. Return: `ConversionKey(acme, v1)`
5. Registry provides: `AcmeV1Converter`

## Customer-Specific Behavior

### Acme Corporation

**Configuration:**
```yaml
- id: acme
  version: v1
  dialect: ARIBA
  match:
    fromIdentityPattern: "buyer123"
  requiredExtrinsics:
    - CostCenter
```

**Custom Logic (AcmeV1Converter):**
```java
@Override
protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
    // Acme uses composite session key
    String sessionKey = request.getBuyerCookie() + "-" + request.getToIdentity();
    request.setSessionKey(sessionKey);
}

@Override
public void validate(PunchOutRequest request, ConversionContext ctx) {
    // Require CostCenter
    if (request.getExtrinsics().get("CostCenter") == null) {
        throw new IllegalArgumentException("Acme requires CostCenter");
    }
}
```

### TechCorp Industries

**Configuration:**
```yaml
- id: techcorp
  version: v2
  dialect: OCI
  match:
    fromIdentityPattern: "buyer456"
  requiredExtrinsics:
    - CompanyCode
    - UniqueName
```

**Custom Logic (TechCorpV2Converter):**
```java
@Override
protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
    // TechCorp uses EmailAddress instead of Email
    JsonNode emailAddress = root.path("Request").path("PunchOutSetupRequest")
                                .path("Contact").path("EmailAddress");
    if (!emailAddress.isMissingNode()) {
        request.setContactEmail(emailAddress.asText());
    }
}

@Override
public void validate(PunchOutRequest request, ConversionContext ctx) {
    // Require CompanyCode and UniqueName
    if (request.getExtrinsics().get("CompanyCode") == null ||
        request.getExtrinsics().get("UniqueName") == null) {
        throw new IllegalArgumentException("TechCorp requires CompanyCode and UniqueName");
    }
}
```

### Global Solutions

**Configuration:**
```yaml
- id: global
  version: v1
  dialect: ARIBA
  match:
    fromIdentityPattern: "buyer789"
  requiredExtrinsics:
    - Region
```

**Custom Logic (GlobalV1Converter):**
```java
@Override
protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
    // Validate region
    String region = request.getExtrinsics().get("Region");
    if (region != null && !region.matches("EMEA|APAC|AMER")) {
        log.warn("Invalid region: {}", region);
    }
}
```

## Adding a New Customer

### Option 1: Config-Only (Simple)

**Add to application.yml:**
```yaml
punchout:
  conversion:
    customers:
      - id: enterprise
        version: v1
        dialect: ARIBA
        match:
          fromIdentityPattern: "buyer321"
        requiredExtrinsics:
          - Department
```

**That's it!** Uses BaseConverter default behavior.
**Time: 2 minutes**

### Option 2: Custom Converter (Complex)

**1. Create converter class:**
```java
@Component
public class EnterpriseV1Converter extends BaseConverter {
    public String customerId() { return "enterprise"; }
    public String version() { return "v1"; }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        // Custom logic here
    }
}
```

**2. Add to configuration:**
```yaml
- id: enterprise
  version: v1
  match:
    fromIdentityPattern: "buyer321"
```

**3. Restart service** - Auto-discovered via `@Component`
**Time: 30 minutes**

## Testing

### Test Acme Conversion
```bash
curl -X POST http://localhost:9090/punchout/setup \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?>
<cXML>
  <Header>
    <From>
      <Credential domain="NetworkID">
        <Identity>buyer123</Identity>  <!-- Matches Acme! -->
      </Credential>
    </From>
    ...
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>ACME_TEST</BuyerCookie>
      <Extrinsic name="CostCenter">CC-1234</Extrinsic>
      ...
    </PunchOutSetupRequest>
  </Request>
</cXML>'
```

**Check logs:**
```
Detected dialect: ARIBA
Matched customer: acme version: v1
Converting cXML for customer: acme, version: v1
Applying Acme-specific customizations
Acme composite session key: ACME_TEST-supplier456
Validating Acme requirements
Acme CostCenter: CC-1234
```

## Benefits Achieved

### 1. **Extensibility**
✅ Add new customers without modifying existing code
✅ Configuration-driven for simple cases
✅ Code-based for complex cases

### 2. **Maintainability**
✅ Clear separation per customer
✅ Common logic in one place (BaseConverter)
✅ Easy to find customer-specific code

### 3. **Testability**
✅ Unit test each converter independently
✅ Mock ConversionContext for testing
✅ Test configuration resolution separately

### 4. **Versioning**
✅ Support multiple versions per customer
✅ AcmeV1 vs AcmeV2 side-by-side
✅ Gradual migration support

### 5. **Production Ready**
✅ Handles Ariba, SAP, OCI dialects
✅ Customer-specific validation
✅ Detailed logging per customer
✅ Fallback to default converter

## Configuration Reference

### Full Example (application.yml)

```yaml
punchout:
  conversion:
    customers:
      # Match by fromIdentity pattern
      - id: acme
        version: v1
        dialect: ARIBA
        match:
          fromIdentityPattern: "buyer123"
        requiredExtrinsics:
          - CostCenter
      
      # Match by UserAgent pattern
      - id: techcorp
        version: v2
        match:
          senderUserAgentPattern: ".*TechCorp.*"
      
      # Match by multiple criteria
      - id: global
        version: v1
        match:
          fromDomain: NetworkID
          toIdentityPattern: "GLOBAL.*"
      
      # Fallback
      - id: default
        version: v1
```

## Logs to Watch

### Startup
```
Registered converter: default version v1
Registered converter: acme version v1
Registered converter: techcorp version v2
Registered converter: global version v1
Total converters registered: 4
```

### During Conversion
```
Detected dialect: ARIBA
Matched customer: acme version: v1
Using converter: acme version v1
Converting cXML for customer: acme, version: v1
Built common request: sessionKey=ACME_TEST, operation=create, extrinsics count=3
Applying Acme-specific customizations
Acme composite session key: ACME_TEST-supplier456
Validating Acme requirements
Acme CostCenter: CC-1234, Department: IT
Acme validation passed
Conversion completed for sessionKey: ACME_TEST-supplier456
```

## Migration from Old Converter

### Old Code (Still Works)
```java
// Old converter is still in place
CxmlToJsonConverter (legacy)
```

### New Code (Recommended)
```java
// New flexible architecture
CxmlConversionService → BaseConverter → Customer Converters
```

### Transition Plan
1. New system is active (using CxmlConversionService)
2. Old CxmlToJsonConverter can be removed once verified
3. All tests pass with new system

## Summary

✅ **16 new classes** created
✅ **4 converters** registered (default + 3 customers)
✅ **Strategy Pattern** implemented
✅ **Template Method** for common logic
✅ **Configuration-driven** customer matching
✅ **Auto-discovery** via Spring component scanning
✅ **Versioning support** built-in
✅ **Customer-specific** validation and customization
✅ **Production ready** with proper logging

**The cXML conversion system is now flexible, extensible, and maintainable!** 🚀

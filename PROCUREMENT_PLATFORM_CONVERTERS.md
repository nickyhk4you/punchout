# Procurement Platform Converters

## Overview

The Gateway now supports **7 converters** including the three major enterprise procurement platforms: **SAP Ariba**, **Coupa**, and **Oracle iProcurement**.

## Registered Converters

✅ **7 converters registered:**
1. **Default V1** - Fallback for unknown systems
2. **Acme V1** - Acme Corporation (custom)
3. **TechCorp V2** - TechCorp Industries (custom)
4. **Global V1** - Global Solutions (custom)
5. **Ariba V1** - SAP Ariba Network
6. **Coupa V1** - Coupa Procurement Platform
7. **Oracle V1** - Oracle iProcurement / Oracle Cloud

## Major Procurement Platforms

### 1. SAP Ariba Network

**Market Leader** - Largest B2B procurement network

**Characteristics:**
- Uses `AribaNetworkUserId` as credential domain
- Requires `UniqueName` extrinsic
- Supports `AribaNetworkId` and `SupplierANID`
- Includes phone numbers in contact
- Requisitioner ID tracking

**Extrinsics:**
- `AribaNetworkId` - Ariba network identifier
- `SupplierANID` - Supplier's Ariba network ID
- `RequisitionerId` - User making the request
- `UniqueName` - Unique user identifier (required)

**Example cXML:**
```xml
<cXML>
  <Header>
    <From>
      <Credential domain="AribaNetworkUserId">
        <Identity>AN01000000001</Identity>
      </Credential>
    </From>
    <Sender>
      <UserAgent>Ariba Procurement 2.0</UserAgent>
    </Sender>
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>ARIBA_SESSION_123</BuyerCookie>
      <Extrinsic name="UniqueName">john.doe@company.com</Extrinsic>
      <Extrinsic name="AribaNetworkId">AN01000000001</Extrinsic>
      <Extrinsic name="RequisitionerId">JDOE123</Extrinsic>
      <Contact role="endUser">
        <Name>John Doe</Name>
        <Email>john.doe@company.com</Email>
        <Phone name="work">
          <TelephoneNumber>
            <Number>+1-555-0123</Number>
          </TelephoneNumber>
        </Phone>
      </Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>
```

**Detection:**
```yaml
match:
  fromDomain: AribaNetworkUserId
```

---

### 2. Coupa Procurement Platform

**Cloud-Based** - Fast-growing procurement platform

**Characteristics:**
- Uses lowercase extrinsic names (`requester-email`, `requester-login`)
- Includes Coupa version information
- Buyer part number support
- Different contact email structure
- Session key includes requester login

**Extrinsics:**
- `CoupaVersion` - Coupa platform version
- `requester-email` - Requester's email (required)
- `requester-login` - Requester's login ID
- `buyer-part-num` - Buyer's part number

**Example cXML:**
```xml
<cXML>
  <Header>
    <From>
      <Credential domain="DUNS">
        <Identity>123456789</Identity>
      </Credential>
    </From>
    <Sender>
      <UserAgent>Coupa Procurement</UserAgent>
    </Sender>
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>COUPA_SESSION_456</BuyerCookie>
      <Extrinsic name="CoupaVersion">2024.1</Extrinsic>
      <Extrinsic name="requester-email">jane.smith@company.com</Extrinsic>
      <Extrinsic name="requester-login">jsmith</Extrinsic>
      <Extrinsic name="buyer-part-num">BPN-12345</Extrinsic>
      <Contact role="buyer">
        <Name>Jane Smith</Name>
        <email>jane.smith@company.com</email>  <!-- lowercase 'email' -->
      </Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>
```

**Detection:**
```yaml
match:
  senderUserAgentPattern: ".*Coupa.*"
```

**Custom Logic:**
- Session key: `{BuyerCookie}-{requester-login}`
- Handles both `Email` and `email` fields
- Validates `requester-email` presence

---

### 3. Oracle iProcurement / Oracle Cloud

**Enterprise ERP** - Part of Oracle E-Business Suite and Oracle Cloud

**Characteristics:**
- Uses `OrgId` and `UserId` for organization/user tracking
- Responsibility ID (`RespId`) for security
- Application ID tracking
- Specific URL format for cart return
- Complex session key format

**Extrinsics:**
- `OrgId` - Oracle Organization ID (required)
- `OrgCode` - Oracle Organization Code
- `UserId` - Oracle User ID (required)
- `RespId` - Oracle Responsibility ID
- `ApplicationId` - Oracle Application ID

**Example cXML:**
```xml
<cXML>
  <Header>
    <From>
      <Credential domain="NetworkID">
        <Identity>ORACLE_BUYER_123</Identity>
      </Credential>
    </From>
    <Sender>
      <UserAgent>Oracle iProcurement 11.5.10</UserAgent>
    </Sender>
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>ORACLE_SESSION_789</BuyerCookie>
      <Extrinsic name="OrgId">204</Extrinsic>
      <Extrinsic name="OrgCode">US1</Extrinsic>
      <Extrinsic name="UserId">1234</Extrinsic>
      <Extrinsic name="RespId">50012</Extrinsic>
      <Extrinsic name="ApplicationId">201</Extrinsic>
      <BrowserFormPost>
        <URL>https://oracle.company.com/OA_HTML/OAFunc...</URL>
      </BrowserFormPost>
      <Contact role="buyer">
        <Name>Oracle User</Name>
        <Email>oracle.user@company.com</Email>
      </Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>
```

**Detection:**
```yaml
match:
  senderUserAgentPattern: ".*Oracle.*|.*iProcurement.*"
```

**Custom Logic:**
- Session key: `{BuyerCookie}-ORG{OrgId}-USER{UserId}`
- Validates OrgId and UserId presence
- Detects Oracle-specific return URLs

---

## Converter Comparison

| Platform | Credential Domain | Key Extrinsics | Session Key Format |
|----------|------------------|----------------|-------------------|
| **Default** | NetworkID | - | BuyerCookie |
| **Ariba** | AribaNetworkUserId | UniqueName, AribaNetworkId | BuyerCookie |
| **Coupa** | DUNS / NetworkID | requester-email, requester-login | BuyerCookie-Login |
| **Oracle** | NetworkID | OrgId, UserId, RespId | BuyerCookie-ORGxxx-USERyyy |
| **Acme** | NetworkID / DUNS | CostCenter | BuyerCookie-ToIdentity |
| **TechCorp** | AribaNetworkUserId | CompanyCode, UniqueName | BuyerCookie |
| **Global** | NetworkID | Region | BuyerCookie |

## Automatic Detection

### How Customers Are Matched

**1. By Credential Domain:**
```yaml
# Matches Ariba Network
fromDomain: AribaNetworkUserId
```

**2. By Identity Pattern:**
```yaml
# Matches specific buyer IDs
fromIdentityPattern: "buyer123"
```

**3. By UserAgent Pattern:**
```yaml
# Matches Coupa
senderUserAgentPattern: ".*Coupa.*"

# Matches Oracle
senderUserAgentPattern: ".*Oracle.*|.*iProcurement.*"
```

### Resolution Priority

Configurations are checked **in order**:
1. First match wins
2. Most specific should be first
3. Default is last (catches all)

**Order in application.yml:**
1. Acme (specific buyer ID)
2. TechCorp (specific buyer ID)
3. Global (specific buyer ID)
4. Ariba (credential domain)
5. Coupa (UserAgent)
6. Oracle (UserAgent)
7. Default (no criteria)

## Usage Examples

### Test Ariba Conversion

```bash
curl -X POST http://localhost:9090/punchout/setup \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?>
<cXML>
  <Header>
    <From>
      <Credential domain="AribaNetworkUserId">
        <Identity>AN01000000001</Identity>
      </Credential>
    </From>
    <Sender>
      <UserAgent>Ariba Procurement</UserAgent>
    </Sender>
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>ARIBA_TEST</BuyerCookie>
      <Extrinsic name="UniqueName">test@ariba.com</Extrinsic>
      ...
    </PunchOutSetupRequest>
  </Request>
</cXML>'
```

**Expected Logs:**
```
Detected dialect: ARIBA
Matched customer: ariba version: v1
Using converter: ariba version v1
Applying SAP Ariba customizations
Ariba Network ID: AN01000000001
```

### Test Coupa Conversion

```bash
# Change UserAgent to trigger Coupa converter
<Sender>
  <UserAgent>Coupa Procurement Platform</UserAgent>
</Sender>
<Extrinsic name="requester-email">user@coupa.com</Extrinsic>
<Extrinsic name="requester-login">cuser123</Extrinsic>
```

**Expected Logs:**
```
Detected dialect: CXML
Matched customer: coupa version: v1
Using converter: coupa version v1
Applying Coupa customizations
Coupa Version: 2024.1, Requester: user@coupa.com
```

### Test Oracle Conversion

```bash
# Change UserAgent to trigger Oracle converter
<Sender>
  <UserAgent>Oracle iProcurement 11.5.10</UserAgent>
</Sender>
<Extrinsic name="OrgId">204</Extrinsic>
<Extrinsic name="UserId">1234</Extrinsic>
```

**Expected Logs:**
```
Detected dialect: OCI
Matched customer: oracle version: v1
Using converter: oracle version v1
Applying Oracle iProcurement customizations
Oracle Org ID: 204, User ID: 1234
```

## Platform-Specific Features

### SAP Ariba
- ✅ AribaNetworkUserId credential support
- ✅ Phone number extraction
- ✅ Requisitioner tracking
- ✅ UniqueName validation

### Coupa
- ✅ Lowercase extrinsic names
- ✅ Requester email/login tracking
- ✅ Session key with login suffix
- ✅ Flexible email field handling

### Oracle
- ✅ Organization and User ID tracking
- ✅ Responsibility ID support
- ✅ Complex session key format
- ✅ Oracle-specific URL detection

## Configuration Reference

### Complete application.yml

```yaml
punchout:
  conversion:
    customers:
      # Internal customers (specific buyer IDs)
      - id: acme
        version: v1
        match:
          fromIdentityPattern: "buyer123"
        requiredExtrinsics: [CostCenter]
      
      - id: techcorp
        version: v2
        match:
          fromIdentityPattern: "buyer456"
        requiredExtrinsics: [CompanyCode, UniqueName]
      
      - id: global
        version: v1
        match:
          fromIdentityPattern: "buyer789"
        requiredExtrinsics: [Region]
      
      # Major procurement platforms (by domain/UserAgent)
      - id: ariba
        version: v1
        dialect: ARIBA
        match:
          fromDomain: AribaNetworkUserId
        requiredExtrinsics: [UniqueName]
      
      - id: coupa
        version: v1
        match:
          senderUserAgentPattern: ".*Coupa.*"
        requiredExtrinsics: [requester-email]
      
      - id: oracle
        version: v1
        dialect: OCI
        match:
          senderUserAgentPattern: ".*Oracle.*|.*iProcurement.*"
        requiredExtrinsics: [OrgId, UserId]
      
      # Catch-all
      - id: default
        version: v1
```

## Real-World Scenarios

### Scenario 1: Hospital Using Ariba

**Hospital:** Uses SAP Ariba for procurement
**Challenge:** Ariba uses AribaNetworkUserId credentials

**Solution:**
- Ariba converter auto-detected by credential domain
- Extracts UniqueName for user tracking
- Handles Ariba-specific phone format
- Logs AribaNetworkId for debugging

### Scenario 2: Pharma Company Using Coupa

**Company:** Uses Coupa procurement platform
**Challenge:** Coupa uses lowercase extrinsic names

**Solution:**
- Coupa converter detected by UserAgent
- Handles `requester-email` and `requester-login`
- Session key includes login for correlation
- Flexible email field handling (Email vs email)

### Scenario 3: Enterprise Using Oracle EBS

**Enterprise:** Uses Oracle E-Business Suite
**Challenge:** Oracle requires OrgId, UserId, RespId

**Solution:**
- Oracle converter detected by UserAgent
- Extracts all Oracle-specific IDs
- Builds composite session key with org and user
- Validates required Oracle fields

## Adding Your Own Platform

### Example: Add Jaggaer Converter

**1. Create converter:**
```java
@Component
public class JaggaerV1Converter extends BaseConverter {
    public String customerId() { return "jaggaer"; }
    public String version() { return "v1"; }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        // Jaggaer-specific logic
    }
}
```

**2. Add configuration:**
```yaml
- id: jaggaer
  version: v1
  match:
    senderUserAgentPattern: ".*Jaggaer.*|.*SciQuest.*"
  requiredExtrinsics:
    - UserAccountCode
```

**3. Restart** - Auto-registered!

## Benefits by Platform

### For Ariba Customers
✅ Proper AribaNetworkUserId handling
✅ UniqueName validation
✅ Phone number support
✅ Ariba-specific logging

### For Coupa Customers
✅ Lowercase extrinsic support
✅ Requester tracking
✅ Enhanced session correlation
✅ Flexible field mapping

### For Oracle Customers
✅ OrgId/UserId extraction
✅ Responsibility tracking
✅ Complex session key format
✅ Oracle URL detection

### For Waters (All Platforms)
✅ **One Gateway** handles all platforms
✅ **No platform-specific deployments** needed
✅ **Automatic detection** - no manual configuration per request
✅ **Future-proof** - easy to add new platforms

## Monitoring

### Check Which Converter Was Used

**Logs show:**
```
Matched customer: ariba version: v1
Using converter: ariba version v1
```

### Converter Statistics

```bash
# Check startup logs
tail -f /tmp/punchout-gateway.log | grep "Registered converter"

# Output:
Registered converter: acme version v1
Registered converter: ariba version v1
Registered converter: coupa version v1
Registered converter: default version v1
Registered converter: global version v1
Registered converter: oracle version v1
Registered converter: techcorp version v2
Total converters registered: 7
```

## Testing Each Platform

### Test Matrix

| Platform | Test Scenario | Validation |
|----------|---------------|------------|
| Ariba | AribaNetworkUserId domain | UniqueName present |
| Coupa | UserAgent contains "Coupa" | requester-email present |
| Oracle | UserAgent contains "Oracle" | OrgId + UserId present |
| Acme | buyer123 identity | CostCenter present |
| TechCorp | buyer456 identity | CompanyCode present |
| Global | buyer789 identity | Region present |
| Default | Unknown buyer | Basic validation |

## Platform Coverage

### Supported Platforms ✅
- SAP Ariba Network
- Coupa Procurement
- Oracle iProcurement / Oracle Cloud
- Generic cXML (default)
- Custom customers (Acme, TechCorp, Global)

### Common Platforms (Can Add Later) 📋
- Jaggaer (formerly SciQuest)
- Ivalua
- Basware
- GEP SMART
- Zycus
- Proactis

### Easy to Add
Just create a new converter class and add 5 lines to application.yml!

## Summary

✅ **7 converters** supporting major platforms
✅ **SAP Ariba** - Market leader support
✅ **Coupa** - Cloud platform support
✅ **Oracle** - ERP integration support
✅ **Custom customers** - Waters-specific customers
✅ **Auto-detection** - Based on credentials and UserAgent
✅ **Extensible** - Add new platforms in minutes
✅ **Production ready** - Proper validation and logging

The Gateway now handles **all major procurement platforms** out of the box! 🌐

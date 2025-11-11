# Punchout Platform Testing Guide

## Overview
Comprehensive testing framework for the Punchout platform using real cXML files and expected responses.

## Quick Start

### 1. Prepare Test Data
```bash
# Test data is already organized in test-data/ folder
cd test-data
ls punchout-sessions/ariba/setup-requests/    # View punchout test requests
ls orders/ariba/order-requests/                # View order test requests
```

### 2. Run All Tests
```bash
./run-integration-tests.sh
```

### 3. Run Specific Test Types
```bash
# Run only punchout session tests
./run-integration-tests.sh punchout

# Run only order tests
./run-integration-tests.sh orders
```

### 4. View Test Results
```bash
# Results are saved in test-results/{timestamp}/
ls test-results/
cat test-results/{timestamp}/summary.txt
```

## Test Data Structure

```
test-data/
├── punchout-sessions/
│   └── ariba/
│       ├── setup-requests/
│       │   └── ariba_create_simple_001.xml
│       └── expected-responses/
│           └── ariba_create_simple_001_response.xml
├── orders/
│   └── ariba/
│       ├── order-requests/
│       │   └── ariba_new_simple_001.xml
│       ├── expected-responses/
│       │   └── ariba_new_simple_001_response.xml
│       └── expected-network-logs/
│           └── ariba_new_simple_001_network.json
└── test-cases.json
```

## Adding New Test Cases

### Step 1: Create Request File

**For Punchout Session:**
```bash
cd test-data/punchout-sessions/ariba/setup-requests
cp ariba_create_simple_001.xml ariba_create_newtest_002.xml
# Edit the file with your test data
```

**For Order:**
```bash
cd test-data/orders/ariba/order-requests
cp ariba_new_simple_001.xml ariba_new_newtest_002.xml
# Edit the file with your test data
```

### Step 2: Create Expected Response

```bash
# For punchout
cd test-data/punchout-sessions/ariba/expected-responses
cp ariba_create_simple_001_response.xml ariba_create_newtest_002_response.xml

# For order
cd test-data/orders/ariba/expected-responses
cp ariba_new_simple_001_response.xml ariba_new_newtest_002_response.xml
```

### Step 3: Add to test-cases.json

```json
{
  "punchoutSessions": [
    {
      "id": "ariba_create_newtest_002",
      "platform": "ariba",
      "scenario": "Description of what this test validates",
      "operation": "CREATE",
      "requestFile": "punchout-sessions/ariba/setup-requests/ariba_create_newtest_002.xml",
      "expectedResponseFile": "punchout-sessions/ariba/expected-responses/ariba_create_newtest_002_response.xml",
      "expectedStatus": 200,
      "validations": [
        "Validation point 1",
        "Validation point 2"
      ],
      "tags": ["ariba", "create"]
    }
  ]
}
```

### Step 4: Run Your New Test

```bash
./run-integration-tests.sh
```

## Test Scenarios

### Punchout Session Tests

1. **Simple CREATE** (`ariba_create_simple_001`)
   - Minimal required fields
   - Basic validation
   - Session key generation

2. **CREATE with Extrinsics** (add your own)
   - Include custom fields
   - Validate extrinsic handling

3. **EDIT Operation** (add your own)
   - Modify existing session
   - Validate update logic

4. **Different Platforms** (add your own)
   - Coupa format
   - OCI format
   - SAP Ariba variations

### Order Tests

1. **Simple Order** (`ariba_new_simple_001`)
   - 3 line items
   - Complete addresses
   - Standard flow

2. **Complex Order** (add your own)
   - Many line items
   - Multiple addresses
   - Tax calculations

3. **Order Update** (add your own)
   - Modify existing order
   - Validate update flow

## Expected Response Placeholders

Use these placeholders in expected response files:

- `{{SESSION_KEY}}` - Will be replaced with actual session key
- `{{ORDER_ID}}` - Will be replaced with actual order ID
- `{{MULE_ORDER_ID}}` - Will be replaced with Mule order ID
- `{{CATALOG_URL}}` - Will be replaced with catalog URL
- `{{TIMESTAMP}}` - Will be replaced with timestamp

Example:
```xml
<BuyerCookie>{{SESSION_KEY}}</BuyerCookie>
<URL>{{CATALOG_URL}}/catalog?sessionKey={{SESSION_KEY}}</URL>
```

## Test Execution Options

### Environment Variables

```bash
# Gateway URL (default: http://localhost:9090)
export GATEWAY_URL="http://localhost:9090"

# MongoDB URI (default: mongodb://localhost:27017/punchout)
export MONGODB_URI="mongodb://localhost:27017/punchout"

# Enable verbose output
export VERBOSE="true"

# Run tests in parallel
export PARALLEL="true"

# Then run tests
./run-integration-tests.sh
```

### Command Line Options

```bash
# Run all tests
./run-integration-tests.sh

# Run only punchout tests
./run-integration-tests.sh punchout

# Run only order tests
./run-integration-tests.sh orders

# Run specific test by ID
./run-integration-tests.sh ariba_create_simple_001
```

## Test Results

### Success Output
```
=========================================
Punchout Integration Test Runner
=========================================
Test Data Dir: /path/to/test-data
Results Dir: test-results/20251111_143000
Gateway URL: http://localhost:9090

Running Punchout Session Tests...

[2025-11-11 14:30:01] Testing Punchout Session: ariba_create_simple_001
[2025-11-11 14:30:02] Sending request to http://localhost:9090/punchout/setup
[2025-11-11 14:30:03] Session key extracted: SESSION_001_ABC123
[2025-11-11 14:30:03] ✓ Response matches expected output

=========================================
Test Summary
=========================================
Total Tests: 2
Passed: 2
Failed: 0
Skipped: 0

All tests PASSED ✓
```

### Failure Output
```
[2025-11-11 14:30:03] ✗ Response does not match expected output
[2025-11-11 14:30:03] ✗ Diff saved to: test-results/20251111_143000/failures/ariba_create_simple_001_diff.txt

Test Summary
=========================================
Total Tests: 2
Passed: 1
Failed: 1

Tests FAILED ✗
```

## Validating Test Results

### Check Response Differences
```bash
# View diff for failed test
cat test-results/{timestamp}/failures/{test_id}_diff.txt

# View actual response
cat test-results/{timestamp}/actual-responses/{test_id}_actual.xml
```

### Verify in Database
```bash
# Check if session was created
mongosh punchout --eval "db.punchout_sessions.find({sessionKey: 'SESSION_001_ABC123'}).pretty()"

# Check if order was created
mongosh punchout --eval "db.orders.find({orderId: 'TEST-ORD-001'}).pretty()"

# Check network requests
mongosh punchout --eval "db.network_requests.find({orderId: 'TEST-ORD-001'}).pretty()"
```

## Testing Best Practices

### 1. Use Realistic Data
```xml
<!-- Good: Realistic data -->
<Name>WatersCorp Laboratory Supply</Name>
<Email>john.doe@waterscorp.com</Email>

<!-- Bad: Generic placeholder -->
<Name>Test Company</Name>
<Email>test@test.com</Email>
```

### 2. Test Edge Cases
- Minimum required fields
- Maximum field lengths
- Special characters (é, ñ, 中文)
- Empty optional fields
- Large orders (100+ line items)

### 3. Test Error Scenarios
Create tests for:
- Invalid cXML structure
- Missing required fields
- Invalid credentials
- Timeout scenarios
- Downstream service failures

### 4. Document Test Purpose
```json
{
  "scenario": "Validate handling of special characters in customer name",
  "validations": [
    "Special characters are preserved",
    "UTF-8 encoding is maintained",
    "Database stores correctly"
  ]
}
```

## Continuous Integration

### GitHub Actions Example
```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Start Services
        run: docker-compose up -d
      
      - name: Wait for Services
        run: ./wait-for-services.sh
      
      - name: Run Tests
        run: ./run-integration-tests.sh
      
      - name: Upload Results
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: test-results/
```

## Troubleshooting

### Tests Won't Run

**Problem:** `run-integration-tests.sh: command not found`
**Solution:**
```bash
chmod +x run-integration-tests.sh
./run-integration-tests.sh
```

**Problem:** `jq: command not found`
**Solution:**
```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq
```

### Connection Errors

**Problem:** `curl: (7) Failed to connect to localhost port 9090`
**Solution:**
```bash
# Check if gateway is running
curl http://localhost:9090/punchout/health

# Start services
./start-all-services.sh
```

### Response Mismatch

**Problem:** Test fails with "Response does not match"
**Solution:**
```bash
# View the diff
cat test-results/{timestamp}/failures/{test_id}_diff.txt

# Check actual response
cat test-results/{timestamp}/actual-responses/{test_id}_actual.xml

# Update expected response if actual is correct
cp test-results/{timestamp}/actual-responses/{test_id}_actual.xml \
   test-data/{path}/expected-responses/{test_id}_response.xml
```

## Next Steps

1. **Add More Test Cases**
   - Cover all platforms (Coupa, OCI, SAP)
   - Cover all operations (CREATE, EDIT, INSPECT)
   - Add error scenarios

2. **Enhance Validation**
   - Validate database state
   - Validate network request logs
   - Validate Mule ESB integration

3. **Performance Testing**
   - Load testing with multiple concurrent requests
   - Stress testing with large cXML files
   - Benchmark response times

4. **Automate**
   - Set up CI/CD pipeline
   - Automated nightly test runs
   - Slack/email notifications

## Summary

This testing framework provides:
✅ Organized test data structure
✅ Real cXML request files
✅ Expected response validation
✅ Automated test execution
✅ Detailed test reports
✅ Easy to add new tests
✅ Platform-specific coverage
✅ End-to-end validation

You can now confidently test the entire punchout flow with real customer cXML files! 🎉

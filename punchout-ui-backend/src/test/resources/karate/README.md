# Karate API Tests

This directory contains Karate framework tests for all REST API controllers in the punchout-ui-backend application.

## Test Files

1. **punchout-session.feature** - Tests for PunchOutSessionController
2. **gateway-request.feature** - Tests for GatewayRequestController
3. **order-object.feature** - Tests for OrderObjectController

## Running Karate Tests

Karate tests require the application to be running first!

### Step 1: Start the application
```bash
# From the project root
cd punchout-ui-backend
mvn spring-boot:run
```

###Step 2: Run Karate tests (in separate terminal)
```bash
# From the project root
mvn test -Dtest=KarateTestRunner -pl punchout-ui-backend
```

## Prerequisites

- Application must be running on `http://localhost:8080`
- Database should have mock data initialized
- All endpoints should be accessible

## Test Configuration

Configuration file: `karate-config.js`
- Base URL: `http://localhost:8080`
- Connect timeout: 5000ms
- Read timeout: 5000ms

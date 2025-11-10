# PunchOut Gateway Service

## Overview
Standalone microservice that acts as middleware between B2B customers and third-party catalog services.

## Architecture

```
B2B Customer → [INBOUND] → PunchOut Gateway → [OUTBOUND] → Auth Service → Token
                              ↓
                         Convert cXML → JSON
                              ↓
                     [OUTBOUND] → Catalog Service → Response
                              ↓
                         Log All Requests
                              ↓
                         Save to MongoDB
```

## Features

- ✅ Receives cXML requests from customers
- ✅ Converts cXML to JSON format
- ✅ Calls authentication service for tokens
- ✅ Forwards requests to catalog services
- ✅ Logs all inbound/outbound traffic to MongoDB
- ✅ Returns cXML responses to customers

## Components

### Controllers
- `PunchOutGatewayController` - Main entry point for cXML requests

### Services
- `PunchOutOrchestrationService` - Orchestrates the entire flow
- `CxmlToJsonConverter` - Converts cXML to JSON
- `NetworkRequestLogger` - Logs all requests to MongoDB

### Clients
- `AuthServiceClient` - Calls 3rd party auth service
- `CatalogServiceClient` - Calls 3rd party catalog service

### Entities
- `NetworkRequestDocument` - MongoDB entity for request logging
- `PunchOutSessionDocument` - MongoDB entity for sessions

## Endpoints

### POST /punchout/setup
Receives cXML PunchOutSetupRequest

**Request:**
```xml
<?xml version="1.0"?>
<cXML>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>...</BuyerCookie>
      <Contact><Email>...</Email></Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>
```

**Response:**
```xml
<?xml version="1.0"?>
<cXML>
  <Response>
    <Status code="200" text="success"/>
    <PunchOutSetupResponse>
      <StartPage><URL>...</URL></StartPage>
    </PunchOutSetupResponse>
  </Response>
</cXML>
```

### POST /punchout/order
Receives cXML PunchOutOrderMessage

### GET /punchout/health
Health check endpoint

## Running

```bash
cd punchout-gateway
mvn spring-boot:run
```

Gateway runs on port **9090**

## Configuration

Edit `application.yml`:

```yaml
server:
  port: 9090

spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: punchout

thirdparty:
  auth:
    url: https://auth-service.example.com/api/v1/token
  catalog:
    url: https://catalog-service.example.com/api/v1
```

## Testing

### Test with cURL

```bash
curl -X POST http://localhost:9090/punchout/setup \
  -H "Content-Type: text/xml" \
  -d @sample-cxml-request.xml
```

### Test with Postman
Import the test request from `sample-cxml-request.xml`

## Request Flow

1. **Customer sends cXML** → Gateway receives at `/punchout/setup`
2. **Log inbound request** → Saved to `network_requests` collection
3. **Convert to JSON** → cXML → PunchOutRequest object
4. **Get auth token** → Call auth service (logged as OUTBOUND)
5. **Call catalog** → Send JSON with token (logged as OUTBOUND)
6. **Save session** → Saved to `punchout` collection  
7. **Return cXML** → Send response back to customer

## Logging

All requests are logged to MongoDB:
- **Direction**: INBOUND (from customer) or OUTBOUND (to 3rd party)
- **Source**: Where request came from
- **Destination**: Where request went to
- **Method**: HTTP method
- **Headers**: All HTTP headers
- **Body**: Full request/response payload
- **Duration**: Time taken
- **Status**: Success/failure

## Database Integration

Uses the same MongoDB database as UI backend:
- Collection: `network_requests` - All HTTP traffic
- Collection: `punchout` - Session data

UI backend can query these collections to display in dashboards.

## Dependencies

- Spring Boot 2.7.18
- Spring Data MongoDB
- Spring WebFlux (WebClient)
- Jackson (XML/JSON processing)
- Lombok

## Build

```bash
mvn clean install
```

## Project Structure

```
punchout-gateway/
├── src/main/java/com/waters/punchout/gateway/
│   ├── PunchOutGatewayApplication.java
│   ├── controller/
│   │   └── PunchOutGatewayController.java
│   ├── service/
│   │   └── PunchOutOrchestrationService.java
│   ├── converter/
│   │   └── CxmlToJsonConverter.java
│   ├── client/
│   │   ├── AuthServiceClient.java
│   │   └── CatalogServiceClient.java
│   ├── logging/
│   │   └── NetworkRequestLogger.java
│   ├── entity/
│   │   ├── NetworkRequestDocument.java
│   │   └── PunchOutSessionDocument.java
│   ├── repository/
│   │   └── NetworkRequestRepository.java
│   └── model/
│       └── PunchOutRequest.java
└── src/main/resources/
    └── application.yml
```

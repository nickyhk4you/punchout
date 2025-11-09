# Network Request Tracking Design

## Overview

This document describes the design and implementation of network request tracking in the Punchout system. Each punchout session now records all network requests between the B2B customer, punchout website (middleware), and 3rd party services.

## Data Model

### MongoDB Collection: `network_requests`

Each network request document contains:

| Field | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | MongoDB unique identifier |
| `sessionKey` | String | Links to the parent punchout session |
| `requestId` | String | Human-readable request identifier |
| `timestamp` | DateTime | When the request was made |
| `direction` | String | Either "INBOUND" or "OUTBOUND" |
| `source` | String | Origin of the request (e.g., "B2B Customer", "Punchout Website") |
| `destination` | String | Target of the request (e.g., "3rd Party API", "Punchout Website") |
| `method` | String | HTTP method (GET, POST, PUT, DELETE) |
| `url` | String | Full request URL |
| `headers` | Object | Key-value pairs of HTTP headers |
| `requestBody` | String | Request payload (XML, JSON, etc.) |
| `statusCode` | Integer | HTTP status code of the response |
| `responseHeaders` | Object | Key-value pairs of response headers |
| `responseBody` | String | Response payload |
| `duration` | Long | Request duration in milliseconds |
| `requestType` | String | Type of request (cXML, OCI, REST, SOAP) |
| `success` | Boolean | Whether the request was successful |
| `errorMessage` | String | Error details if request failed |

### Request Flow

```
INBOUND: B2B Customer → Punchout Website
OUTBOUND: Punchout Website → 3rd Party Service
```

## Backend Implementation

### Java Entities

**NetworkRequestDocument.java** - MongoDB entity with all fields mapped
- Located: `punchout-ui-backend/src/main/java/com/waters/punchout/mongo/entity/`

**NetworkRequestDTO.java** - Data transfer object for API responses
- Located: `punchout-ui-backend/src/main/java/com/waters/punchout/dto/`

### Repository

**NetworkRequestMongoRepository.java** - Spring Data MongoDB repository
- Methods:
  - `findBySessionKey(String sessionKey)` - Get all requests for a session
  - `findBySessionKeyOrderByTimestampAsc(String sessionKey)` - Ordered by time
  - `findBySessionKeyAndDirection(String sessionKey, String direction)` - Filter by direction

### Service Layer

**NetworkRequestMongoService.java** - Business logic layer
- Methods:
  - `getNetworkRequestsBySessionKey(String sessionKey)` - Get all requests
  - `getNetworkRequestsBySessionKeyAndDirection(String sessionKey, String direction)` - Filter by direction
  - `getNetworkRequestById(String id)` - Get single request details

### REST Controllers

**NetworkRequestMongoController.java** - REST API endpoints

#### Endpoints:

1. **GET** `/api/v1/sessions/{sessionKey}/network-requests`
   - Returns all network requests for a session

2. **GET** `/api/v1/sessions/{sessionKey}/network-requests/inbound`
   - Returns only inbound requests

3. **GET** `/api/v1/sessions/{sessionKey}/network-requests/outbound`
   - Returns only outbound requests

4. **GET** `/api/v1/network-requests/{id}`
   - Returns detailed information for a specific request

## Frontend Implementation

### TypeScript Types

**NetworkRequest** interface in `src/types/index.ts`:
```typescript
export interface NetworkRequest {
  id: string;
  sessionKey: string;
  requestId: string;
  timestamp: string;
  direction: 'INBOUND' | 'OUTBOUND';
  source: string;
  destination: string;
  method: string;
  url: string;
  headers?: Record<string, string>;
  requestBody?: string;
  statusCode?: number;
  responseHeaders?: Record<string, string>;
  responseBody?: string;
  duration?: number;
  requestType: string;
  success: boolean;
  errorMessage?: string;
}
```

### API Client

**networkRequestAPI** in `src/lib/api.ts`:
- `getNetworkRequests(sessionKey)` - Get all requests
- `getInboundRequests(sessionKey)` - Get inbound only
- `getOutboundRequests(sessionKey)` - Get outbound only
- `getNetworkRequestById(id)` - Get request details

### UI Components

#### Session Detail Page
**Location**: `src/app/sessions/[sessionKey]/page.tsx`

Features:
- Displays network requests in a table
- Tabs to filter: All / Inbound / Outbound
- Shows: timestamp, direction, method, type, source/destination, status, duration
- "View Details" link for each request

#### Network Request Detail Page
**Location**: `src/app/sessions/[sessionKey]/requests/[requestId]/page.tsx`

Features:
- Full request details overview
- Request and response headers
- Request and response body with JSON formatting
- Error messages if request failed
- Color-coded status indicators

## Sample Data

**12 sample network requests** have been imported into MongoDB:
- 3 requests for SESSION_001_ABC123 (cXML setup, catalog query, order message)
- 2 requests for SESSION_002_DEF456 (OCI setup, payment validation)
- 4 requests for SESSION_003_GHI789 (catalog, inventory, pricing, shipping - includes 1 failure)
- 1 request for SESSION_004_JKL012 (inspect operation)
- 2 requests for SESSION_005_MNO345 (OCI setup, catalog search)

### Request Types Covered:
- **cXML**: PunchOut setup requests, order messages
- **OCI**: SAP SRM punchout setup
- **REST**: Catalog APIs, payment gateways, inventory services, pricing services, shipping calculators

### Scenarios Covered:
- Successful requests (200 status)
- Failed requests (503 service unavailable)
- Various durations (156ms to 5001ms)
- Different content types (XML, JSON, HTML)
- Request/response body examples

## Testing

### Using Postman

Import `Punchout_API.postman_collection.json` which includes:
- Get all network requests for a session
- Get inbound requests only
- Get outbound requests only
- Get network request by ID

### Manual Testing

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `npm run dev`
3. Navigate to: `http://localhost:3000/sessions`
4. Click any session to view details
5. Scroll to "Network Requests" section
6. Use tabs to filter by direction
7. Click "View Details" on any request

## Use Cases

### 1. Debugging Integration Issues
View all requests between customer and 3rd party to identify failures.

### 2. Performance Analysis
Check request durations to identify slow 3rd party services.

### 3. Audit Trail
Complete record of all data exchanged during a punchout session.

### 4. Error Investigation
View request/response bodies and error messages for failed requests.

### 5. Compliance & Security
Track what data was sent/received for compliance purposes.

## Future Enhancements

- Add search/filter capabilities
- Add request replay functionality
- Export requests to HAR format
- Real-time request streaming
- Request analytics and dashboards
- Automatic error detection and alerting

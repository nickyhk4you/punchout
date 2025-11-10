# Order Processing System - Design Document

## Overview

Implement comprehensive Order processing that mirrors the PunchOut architecture:
- Receive cXML OrderRequest from customers
- Convert to JSON
- Send to Mule Service
- Log all requests (INBOUND cXML, OUTBOUND JSON)
- Display orders in UI with complete visibility

## Architecture

### Similar to PunchOut Flow

```
Customer → Gateway (cXML Order) → Convert → Mule (JSON) → Save Order → Response
           ↓                                    ↓
        MongoDB                              MongoDB
     (Log INBOUND)                       (Log OUTBOUND)
```

## Data Model

### Order Document (MongoDB)

```javascript
{
  _id: ObjectId("..."),
  orderId: "PC2341240",                    // From cXML OrderID
  sessionKey: "SESSION_DEV_CUST001_123",   // Link to PunchOut session
  orderDate: ISODate("2025-11-07..."),
  orderType: "regular",
  orderVersion: "1",
  customerId: "CUST001",
  customerName: "Acme Corporation",
  
  // Financial
  total: 698.64,
  currency: "USD",
  taxAmount: 0,
  
  // Parties
  shipTo: {
    name: "Janssen Biotech Inc",
    addressId: "186001B",
    street: "52 Great Valley Parkway",
    city: "Malvern",
    state: "PA",
    postalCode: "19355",
    country: "US",
    email: "SSchalle@its.jnj.com",
    phone: "+1 610 6516000"
  },
  
  billTo: {
    name: "Janssen R&D, L.L.C.",
    addressId: "1270_BillTo",
    street: "PO Box 16571",
    city: "New Brunswick",
    state: "NJ",
    postalCode: "08906-6571",
    country: "US"
  },
  
  // Line Items
  items: [
    {
      lineNumber: 1,
      quantity: 3,
      supplierPartId: "205000343",
      description: "Kit, ACQUITY Col. In-Line Filter",
      unitPrice: 232.88,
      extendedAmount: 698.64,
      currency: "USD",
      unitOfMeasure: "EA",
      unspsc: "41115854",
      
      extrinsics: {
        "Req. Line No.": "1",
        "Requester": "Stephanie Schallenhammer",
        "PR No.": "PR9169355",
        "Line Type.ID": "Material"
      }
    }
  ],
  
  // Extrinsics
  extrinsics: {
    "Supplier ID": "26811819",
    "Requester's Phone": "+1 609 8280346",
    "Total": "$698.64 USD",
    "PurchasingUnit": "1270",
    "PUName": "Janssen Research & Development, LLC. (1270)"
  },
  
  // Comments
  comments: "Terms and Conditions...",
  
  // Tracking
  status: "RECEIVED",              // RECEIVED, SENT_TO_MULE, CONFIRMED, FAILED
  receivedAt: ISODate("2025-11-07..."),
  processedAt: ISODate("2025-11-07..."),
  muleOrderId: "MULE_ORD_12345",   // From Mule response
  
  // Metadata
  environment: "PRODUCTION",
  source: "Ariba Network",
  dialect: "ARIBA"
}
```

### Network Requests (Same Collection)

**INBOUND - cXML Order:**
```javascript
{
  requestId: "REQ_ORD_001",
  sessionKey: "SESSION_DEV_CUST001_123",  // Optional, may not exist
  orderId: "PC2341240",                     // Link to order
  direction: "INBOUND",
  source: "Ariba Network",
  destination: "Punchout Gateway",
  method: "POST",
  url: "/punchout/order",
  headers: {
    "Content-Type": "text/xml",
    "X-API-Key": "ACME_..."
  },
  requestBody: "<?xml version=\"1.0\"...",  // Full cXML
  statusCode: 200,
  duration: 150
}
```

**OUTBOUND - JSON to Mule:**
```javascript
{
  requestId: "REQ_ORD_002",
  orderId: "PC2341240",
  direction: "OUTBOUND",
  source: "Punchout Gateway",
  destination: "Mule Service",
  method: "POST",
  url: "https://mule.waters.com/api/v1/order",
  headers: {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGci..."
  },
  requestBody: '{"orderId":"PC2341240","items":[...]}',  // JSON
  responseBody: '{"muleOrderId":"MULE_ORD_12345","status":"success"}',
  statusCode: 200,
  duration: 450
}
```

## Backend Implementation

### 1. Order Entity

```java
@Document(collection = "orders")
@Data
public class OrderDocument {
    @Id
    private String id;
    
    private String orderId;           // From cXML
    private String sessionKey;         // Link to PunchOut session (optional)
    private LocalDateTime orderDate;
    private String orderType;          // regular, blanket, standing
    private String orderVersion;
    private String customerId;
    private String customerName;
    
    // Financial
    private BigDecimal total;
    private String currency;
    private BigDecimal taxAmount;
    
    // Parties
    private OrderAddress shipTo;
    private OrderAddress billTo;
    private PaymentInfo payment;
    
    // Line Items
    private List<OrderItem> items;
    
    // Additional Data
    private Map<String, String> extrinsics;
    private String comments;
    
    // Tracking
    private String status;             // RECEIVED, PROCESSING, SENT_TO_MULE, CONFIRMED, FAILED
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private String muleOrderId;
    
    // Metadata
    private String environment;
    private String source;
    private String dialect;
}

@Data
public class OrderAddress {
    private String addressId;
    private String name;
    private String deliverTo;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String email;
    private String phone;
}

@Data
public class OrderItem {
    private Integer lineNumber;
    private Integer quantity;
    private String supplierPartId;
    private String supplierPartAuxiliaryId;
    private String description;
    private BigDecimal unitPrice;
    private BigDecimal extendedAmount;
    private String currency;
    private String unitOfMeasure;
    private String unspsc;
    private String url;
    private Map<String, String> extrinsics;
}
```

### 2. Order Converter

```java
@Service
@Slf4j
public class CxmlOrderConverter {
    
    private final XmlMapper xmlMapper = new XmlMapper();
    
    public OrderDocument convertCxmlToOrder(String cxmlContent) throws Exception {
        JsonNode root = xmlMapper.readTree(cxmlContent);
        JsonNode orderNode = root.path("Request").path("OrderRequest");
        JsonNode headerNode = orderNode.path("OrderRequestHeader");
        
        OrderDocument order = new OrderDocument();
        
        // Header info
        order.setOrderId(headerNode.path("orderID").asText());
        order.setOrderDate(parseDateTime(headerNode.path("orderDate").asText()));
        order.setOrderType(headerNode.path("orderType").asText("regular"));
        order.setOrderVersion(headerNode.path("orderVersion").asText("1"));
        
        // Total
        JsonNode totalNode = headerNode.path("Total").path("Money");
        order.setTotal(new BigDecimal(totalNode.asText("0")));
        order.setCurrency(totalNode.path("currency").asText("USD"));
        
        // ShipTo
        order.setShipTo(extractAddress(headerNode.path("ShipTo").path("Address")));
        
        // BillTo
        order.setBillTo(extractAddress(headerNode.path("BillTo").path("Address")));
        
        // Items
        order.setItems(extractItems(orderNode));
        
        // Extrinsics
        order.setExtrinsics(extractExtrinsics(headerNode));
        
        // Comments
        order.setComments(headerNode.path("Comments").asText());
        
        // Status
        order.setStatus("RECEIVED");
        order.setReceivedAt(LocalDateTime.now());
        
        return order;
    }
    
    private List<OrderItem> extractItems(JsonNode orderNode) {
        List<OrderItem> items = new ArrayList<>();
        
        JsonNode itemsNode = orderNode.path("ItemOut");
        if (itemsNode.isArray()) {
            itemsNode.forEach(itemNode -> items.add(extractItem(itemNode)));
        } else if (!itemsNode.isMissingNode()) {
            items.add(extractItem(itemsNode));
        }
        
        return items;
    }
    
    private OrderItem extractItem(JsonNode itemNode) {
        OrderItem item = new OrderItem();
        
        item.setLineNumber(itemNode.path("lineNumber").asInt());
        item.setQuantity(itemNode.path("quantity").asInt());
        
        JsonNode itemId = itemNode.path("ItemID");
        item.setSupplierPartId(itemId.path("SupplierPartID").asText());
        item.setSupplierPartAuxiliaryId(itemId.path("SupplierPartAuxiliaryID").asText());
        
        JsonNode detail = itemNode.path("ItemDetail");
        item.setDescription(detail.path("Description").asText());
        item.setUnitOfMeasure(detail.path("UnitOfMeasure").asText());
        item.setUnspsc(detail.path("Classification").asText());
        
        JsonNode unitPrice = detail.path("UnitPrice").path("Money");
        item.setUnitPrice(new BigDecimal(unitPrice.asText("0")));
        item.setCurrency(unitPrice.path("currency").asText("USD"));
        
        // Calculate extended amount
        item.setExtendedAmount(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
        
        // Item extrinsics
        item.setExtrinsics(extractExtrinsics(detail));
        
        return item;
    }
}
```

### 3. Order Controller (Gateway)

```java
@RestController
@RequestMapping("/punchout")
public class PunchOutGatewayController {
    
    @PostMapping(value = "/order", 
                 consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> handleOrderRequest(@RequestBody String cxmlContent) {
        log.info("Received Order request, content length: {}", cxmlContent.length());
        
        try {
            // Process order
            OrderResponse response = orderOrchestrationService.processOrder(cxmlContent);
            
            // Build cXML response
            String cxmlResponse = buildCxmlOrderResponse(response);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body(cxmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing order request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildCxmlErrorResponse(e.getMessage()));
        }
    }
}
```

### 4. Order Orchestration Service

```java
@Service
@Slf4j
public class OrderOrchestrationService {
    
    private final CxmlOrderConverter orderConverter;
    private final AuthServiceClient authServiceClient;
    private final MuleServiceClient muleServiceClient;
    private final NetworkRequestLogger networkRequestLogger;
    private final OrderRepository orderRepository;
    
    public OrderResponse processOrder(String cxmlContent) {
        log.info("Processing order request");
        
        try {
            // 1. Parse cXML to Order
            OrderDocument order = orderConverter.convertCxmlToOrder(cxmlContent);
            log.info("Parsed order: orderId={}, items={}, total={}", 
                    order.getOrderId(), order.getItems().size(), order.getTotal());
            
            // 2. Log INBOUND cXML request
            logInboundOrderRequest(cxmlContent, order.getOrderId());
            
            // 3. Get JWT token
            String token = getAuthToken(order);
            
            // 4. Convert order to JSON
            Map<String, Object> jsonOrder = convertOrderToJson(order);
            
            // 5. Send to Mule Service
            Map<String, Object> muleResponse = sendOrderToMule(jsonOrder, token, order.getOrderId());
            
            // 6. Update order with Mule response
            order.setMuleOrderId((String) muleResponse.get("muleOrderId"));
            order.setStatus("CONFIRMED");
            order.setProcessedAt(LocalDateTime.now());
            
            // 7. Save order to MongoDB
            orderRepository.save(order);
            
            log.info("Order processed successfully: orderId={}, muleOrderId={}", 
                    order.getOrderId(), order.getMuleOrderId());
            
            return new OrderResponse(order.getOrderId(), order.getMuleOrderId(), "success");
            
        } catch (Exception e) {
            log.error("Failed to process order: {}", e.getMessage(), e);
            throw new RuntimeException("Order processing failed: " + e.getMessage(), e);
        }
    }
    
    private void logInboundOrderRequest(String cxmlContent, String orderId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/xml");
        
        networkRequestLogger.logInboundRequest(
                null,                   // sessionKey may not exist
                "Customer System",
                "Punchout Gateway",
                "POST",
                "/punchout/order",
                headers,
                cxmlContent,
                "cXML-Order"
        );
        // Store orderId reference for later correlation
    }
}
```

## UI Design

### 1. Orders List Page (`/orders`)

**Layout:**
```
┌─────────────────────────────────────────────────────────────┐
│  Waters Punchout Platform                                   │
│  [Dashboard] [Sessions] [Orders] [Developer] [Converter]    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                         Orders                               │
│  View and manage all customer orders                         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Filters                                                     │
│  [Status ▼] [Customer ▼] [Date Range] [Clear Filters]      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Order ID     Customer      Date       Items  Total    Status│
│  ─────────────────────────────────────────────────────────── │
│  PC2341240    Acme Corp     Nov 7      3      $698.64  ✓    │
│  PC2341239    TechCorp      Nov 7      5      $1,234   ✓    │
│  PC2341238    Global Sol    Nov 6      2      $450.00  ✓    │
└─────────────────────────────────────────────────────────────┘
```

**Features:**
- Sortable columns (Order ID, Date, Total)
- Filter by status, customer, date range
- Click order ID to view details
- Status badges (Received, Confirmed, Failed)
- Customer badges (color-coded)

### 2. Order Detail Page (`/orders/{orderId}`)

**Layout:**

```
┌─────────────────────────────────────────────────────────────┐
│  Order PC2341240                                             │
│  Acme Corporation · Nov 7, 2025 · $698.64 · Status: ✓      │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────┐  ┌──────────────────────┐
│  Order Information   │  │  Network Requests    │
│                      │  │                      │
│  Order ID: PC2341240 │  │  📥 INBOUND          │
│  Date: Nov 7, 2025   │  │  cXML Order          │
│  Type: Regular       │  │  150ms               │
│  Version: 1          │  │                      │
│  Total: $698.64 USD  │  │  📤 OUTBOUND         │
│  Status: Confirmed   │  │  JWT Token Request   │
│  Mule ID: MULE_123   │  │  45ms                │
│                      │  │                      │
│                      │  │  📤 OUTBOUND         │
│                      │  │  JSON to Mule        │
│                      │  │  450ms               │
└──────────────────────┘  └──────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Shipping Information                                        │
│                                                              │
│  Janssen Biotech Inc                                         │
│  52 Great Valley Parkway                                     │
│  Malvern, PA 19355                                          │
│  Contact: SSchalle@its.jnj.com                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Line Items (3)                                              │
│  ───────────────────────────────────────────────────────────│
│  Line  Part Number   Description              Qty  Price     │
│  1     205000343     Kit, ACQUITY...          3    $232.88   │
│  2     205000344     Filter Assembly          2    $150.00   │
│  3     205000345     Replacement Filter       5    $45.00    │
│  ───────────────────────────────────────────────────────────│
│                                          Subtotal: $698.64   │
│                                               Tax: $0.00     │
│                                             Total: $698.64   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Extrinsics                                                  │
│                                                              │
│  Supplier ID: 26811819                                       │
│  Purchasing Unit: 1270                                       │
│  Requester: Stephanie Schallenhammer                         │
│  PR No.: PR9169355                                          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Network Requests Details                                    │
│  ───────────────────────────────────────────────────────────│
│  [INBOUND] cXML Order Request                               │
│  Time: 9:49:27 AM · Duration: 150ms · Status: 200          │
│                                                              │
│  Request Headers                 Response Headers            │
│  Content-Type: text/xml         Content-Type: text/xml     │
│  X-API-Key: ACME_***            Content-Length: 425         │
│                                                              │
│  Request Body                    Response Body               │
│  <?xml version="1.0"...         <?xml version="1.0"...     │
│  [View Full cXML]                [View Full cXML]           │
│  ───────────────────────────────────────────────────────────│
│  [OUTBOUND] JSON Order to Mule                              │
│  Time: 9:49:27 AM · Duration: 450ms · Status: 200          │
│                                                              │
│  Request Headers                 Response Headers            │
│  Content-Type: application/json  Content-Type: application  │
│  Authorization: Bearer eyJh...   Content-Length: 156        │
│                                                              │
│  Request Body                    Response Body               │
│  {"orderId":"PC2341240"...      {"muleOrderId":"MULE..."   │
│  [View Full JSON]                [View Full JSON]           │
└─────────────────────────────────────────────────────────────┘
```

### 3. UI Components

**OrdersPage.tsx:**
```typescript
export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [filters, setFilters] = useState({
    status: '',
    customerId: '',
    startDate: '',
    endDate: ''
  });
  
  // Fetch orders
  useEffect(() => {
    loadOrders();
  }, [filters]);
  
  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-green-600 to-teal-600">
        <h1>Orders</h1>
        <p>View and manage all customer orders</p>
      </div>
      
      {/* Filters */}
      <FilterCard filters={filters} onChange={setFilters} />
      
      {/* Orders Table */}
      <OrdersTable orders={orders} />
    </div>
  );
}
```

**OrderDetailPage.tsx:**
```typescript
export default function OrderDetailPage({ params }) {
  const [order, setOrder] = useState<Order>();
  const [networkRequests, setNetworkRequests] = useState([]);
  
  useEffect(() => {
    loadOrder(params.orderId);
    loadNetworkRequests(params.orderId);
  }, [params.orderId]);
  
  return (
    <div>
      {/* Order Header */}
      <OrderHeader order={order} />
      
      {/* Grid Layout */}
      <div className="grid grid-cols-2 gap-6">
        <OrderInfoCard order={order} />
        <NetworkRequestsCard requests={networkRequests} />
      </div>
      
      {/* Shipping & Billing */}
      <div className="grid grid-cols-2 gap-6">
        <ShippingCard address={order.shipTo} />
        <BillingCard address={order.billTo} />
      </div>
      
      {/* Line Items Table */}
      <LineItemsTable items={order.items} />
      
      {/* Extrinsics */}
      <ExtrinsicsCard extrinsics={order.extrinsics} />
      
      {/* Network Request Details */}
      <NetworkRequestDetailsCard requests={networkRequests} />
    </div>
  );
}
```

## API Endpoints

### UI Backend APIs

```typescript
// Get all orders
GET /api/v1/orders
Query params: ?status=CONFIRMED&customerId=CUST001&startDate=2025-11-01

// Get order by ID
GET /api/v1/orders/{orderId}

// Get network requests for order
GET /api/v1/orders/{orderId}/network-requests

// Get orders by customer
GET /api/v1/orders/customer/{customerId}

// Get order statistics
GET /api/v1/orders/stats
Response: {
  totalOrders: 1234,
  totalValue: 1250000.00,
  ordersByStatus: {...},
  ordersByCustomer: {...}
}
```

### Gateway APIs

```typescript
// Process order (cXML)
POST /punchout/order
Content-Type: text/xml
Body: cXML OrderRequest

// Get order status
GET /punchout/order/{orderId}/status
```

## Dashboard Enhancement

### Add Orders Section to Homepage

```typescript
// Homepage stats
{
  totalSessions: 150,
  totalOrders: 1234,
  devSessions: 50,
  prodOrders: 980,
  orderValue: 1250000.00,
  recentOrders: [...]
}

// New "Recent Orders" card
<div className="bg-white rounded-xl shadow-lg">
  <h2>Recent Orders</h2>
  {recentOrders.map(order => (
    <OrderRow 
      orderId={order.orderId}
      customer={order.customerName}
      total={order.total}
      status={order.status}
    />
  ))}
</div>
```

## Implementation Phases

### Phase 1: Backend (2-3 days)
1. Create Order entity and repository
2. Create Order converter (cXML → Order)
3. Create Order orchestration service
4. Implement /punchout/order endpoint
5. Add network request logging for orders
6. Create Mule order forwarding

### Phase 2: UI Backend APIs (1 day)
1. Create Order controller
2. Add GET /api/v1/orders
3. Add GET /api/v1/orders/{orderId}
4. Add GET /api/v1/orders/{orderId}/network-requests

### Phase 3: Frontend (2 days)
1. Create /orders page (list view)
2. Create /orders/{orderId} page (detail view)
3. Add order components (OrderCard, LineItemsTable)
4. Add to navigation menu
5. Update dashboard with order stats

### Phase 4: Testing (1 day)
1. Test with sample cXML orders
2. Verify conversion accuracy
3. Check network request logging
4. Validate UI display
5. E2E testing

## Key Features

### 1. Order List View
✅ Sortable/filterable table
✅ Status indicators
✅ Customer badges
✅ Total value display
✅ Click to details

### 2. Order Detail View
✅ Complete order information
✅ Shipping & billing addresses
✅ Line items with pricing
✅ Extrinsics display
✅ Network request logs (cXML IN, JSON OUT)

### 3. Network Request Tracking
✅ INBOUND: cXML OrderRequest
✅ OUTBOUND: JWT token request
✅ OUTBOUND: JSON order to Mule
✅ Complete headers
✅ Full payloads
✅ Status codes & timing

### 4. Order Analytics
✅ Total orders count
✅ Total order value
✅ Orders by customer
✅ Orders by status
✅ Success rate

## Data Relationships

```
Order (orders collection)
  ├─> orderId: "PC2341240"
  ├─> sessionKey: "SESSION_..." (optional - links to PunchOut)
  └─> Has Many: Network Requests

Network Request (network_requests collection)
  ├─> orderId: "PC2341240"  (links to order)
  ├─> sessionKey: null (or PunchOut session if available)
  └─> direction: INBOUND or OUTBOUND

Session (punchout collection)
  ├─> sessionKey: "SESSION_..."
  └─> May have: Orders (if customer completes purchase)
```

## Summary

### What This Enables

**For Waters:**
- ✅ Complete order visibility
- ✅ Track all customer orders
- ✅ Monitor Mule integration
- ✅ Audit trail for compliance
- ✅ Debug order issues

**For Developers:**
- ✅ See cXML → JSON conversion
- ✅ Verify Mule integration
- ✅ Debug order problems
- ✅ Test with different customers

**For QA:**
- ✅ Validate order processing
- ✅ Verify data accuracy
- ✅ Test error scenarios
- ✅ Regression testing

### Total Effort

- **Backend:** 3 days
- **Frontend:** 2 days
- **Testing:** 1 day
- **Total:** 6 days / 1.5 weeks

**Ready to implement?** Let me know and I'll start building the order processing system! 📦

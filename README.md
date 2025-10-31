# cXML to JSON Conversion Service

A flexible Spring Boot multi-module application for converting cXML documents to JSON format for B2B e-commerce customers.

## Architecture

### Multi-Module Structure
```
punchout/                           # Parent project
├── punchout-common/                # Common shared code
│   ├── model/                      # Data models and DTOs
│   │   ├── ConversionResponse
│   │   ├── CxmlRequest
│   │   └── OrderData
│   ├── exception/                  # Exception handling
│   │   ├── CxmlConversionException
│   │   └── GlobalExceptionHandler
│   ├── converter/                  # Converter interface
│   │   └── CxmlConverter
│   └── service/                    # Service interface
│       └── CxmlConversionService
│
├── punchout-order/                 # Order processing module
│   ├── converter/impl/             # Order converter implementations
│   │   ├── DefaultCxmlConverter
│   │   └── CustomerACxmlConverter
│   ├── service/impl/               # Service implementations
│   │   └── CxmlConversionServiceImpl
│   └── controller/                 # REST API endpoints
│       └── CxmlConversionController
│
├── punchout-invoice/               # Invoice processing module
│   └── invoice/
│       └── InvoiceConverter
│
├── punchout-ui-backend/            # Backend REST API application
│   └── PunchoutApplication         # Spring Boot main class (Port 8080)
│
└── punchout-ui-frontend/           # Web UI frontend application
    ├── controller/                 # UI controllers
    │   └── HomeController
    ├── templates/                  # Thymeleaf templates
    │   ├── index.html
    │   └── converter.html
    └── PunchoutUiApplication       # Spring Boot UI app (Port 8081)
```

### Module Dependencies
- `punchout-common`: No dependencies (base module)
- `punchout-order`: Depends on `punchout-common`
- `punchout-invoice`: Depends on `punchout-common`
- `punchout-ui-backend`: Depends on `punchout-common`, `punchout-order`, `punchout-invoice`
- `punchout-ui-frontend`: Independent web UI application

### Design Pattern
The application uses the **Strategy Pattern** for customer-specific conversion logic:
- Each customer can have their own `CxmlConverter` implementation
- The service automatically discovers and registers all converters
- Falls back to `DefaultCxmlConverter` if no customer-specific converter exists

## API Endpoints

### 1. Convert cXML (JSON Request)
```
POST /api/v1/cxml/convert
Content-Type: application/json

{
  "customerId": "CUSTOMER_A",
  "documentType": "ORDER",
  "cxmlContent": "<cXML>...</cXML>"
}
```

### 2. Convert cXML (Raw XML)
```
POST /api/v1/cxml/convert/{customerId}/{documentType}
Content-Type: text/xml

<cXML>...</cXML>
```

### 3. Health Check
```
GET /api/v1/cxml/health
```

## Adding a New Customer Converter

1. Navigate to the `punchout-order` module
2. Create a new class in `converter/impl/` implementing `CxmlConverter`:
```java
package com.waters.punchout.converter.impl;

import com.waters.punchout.converter.CxmlConverter;
import org.springframework.stereotype.Component;

@Component
public class CustomerBCxmlConverter implements CxmlConverter {
    
    @Override
    public Object convert(String cxmlContent, String documentType) {
        // Custom conversion logic for Customer B
    }
    
    @Override
    public boolean supports(String customerId) {
        return "CUSTOMER_B".equalsIgnoreCase(customerId);
    }
    
    @Override
    public String getCustomerId() {
        return "CUSTOMER_B";
    }
}
```

3. The converter will be automatically registered by Spring

## Running the Application

### Build all modules
```bash
mvn clean install
```

### Run the backend application
```bash
# From the root directory
cd punchout-ui-backend
mvn spring-boot:run

# Or run the packaged JAR
java -jar punchout-ui-backend/target/punchout-ui-backend-1.0.0.jar
```

The backend API will start on **port 8080**.

### Run the UI frontend application
```bash
# From the root directory
cd punchout-ui-frontend
mvn spring-boot:run

# Or run the packaged JAR
java -jar punchout-ui-frontend/target/punchout-ui-frontend-1.0.0.jar
```

The web UI will start on **port 8081**.

Access the UI at: **http://localhost:8081**

### Build individual modules
```bash
# Build only common module
mvn clean install -pl punchout-common

# Build order module (requires common)
mvn clean install -pl punchout-order

# Build invoice module (requires common)
mvn clean install -pl punchout-invoice

# Build UI backend module (requires all business modules)
mvn clean install -pl punchout-ui-backend

# Build UI frontend module (independent)
mvn clean install -pl punchout-ui-frontend
```

## Example cXML Order Document
```xml
<cXML>
  <OrderID>12345</OrderID>
  <OrderDate>2025-10-30</OrderDate>
  <BuyerCookie>ACME Corp</BuyerCookie>
  <ItemOut lineNumber="1">
    <ItemID>PROD-001</ItemID>
    <Description>Product 1</Description>
    <Quantity>10</Quantity>
    <UnitPrice>99.99</UnitPrice>
    <UnitOfMeasure>EA</UnitOfMeasure>
  </ItemOut>
</cXML>
```

## Technologies Used
- Spring Boot 2.7.18
- Java 8
- Jackson (JSON/XML processing)
- Lombok
- Maven (Multi-module project)

## Module Descriptions

### punchout-common
Core shared library containing:
- Data models (DTOs)
- Exception classes and handlers
- Service and converter interfaces
- No dependencies on other modules

### punchout-order
Order processing functionality:
- Order-specific cXML converters
- Business logic implementation
- REST API controllers
- Depends on `punchout-common`

### punchout-invoice
Invoice processing functionality:
- Invoice-specific converters (placeholder)
- Future invoice processing logic
- Depends on `punchout-common`

### punchout-ui-backend
Backend REST API application:
- Spring Boot main application class
- Application configuration files
- Executable JAR generation
- Aggregates all business modules
- Runs on port 8080

### punchout-ui-frontend
Web UI frontend application:
- Thymeleaf-based web interface
- Interactive cXML converter tool
- HTML/CSS/JavaScript frontend
- Independent Spring Boot application
- Runs on port 8081

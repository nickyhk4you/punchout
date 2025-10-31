# PunchOut UI Backend Service

Spring Boot backend service for managing PunchOut Sessions, Order Objects, and Gateway Requests.

## Features

- **PunchOut Session Management**: Create, retrieve, update, and filter PunchOut sessions
- **Order Object Management**: Manage order objects associated with sessions
- **Gateway Request Tracking**: Log and retrieve gateway requests by session
- **Exception Handling**: Global exception handling with consistent error responses
- **Validation**: Bean validation for all DTOs and entities
- **Logging**: SLF4J logging with Logback
- **Testing**: Comprehensive unit and integration tests

## Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL** (production)
- **H2 Database** (development/testing)
- **Lombok**
- **JUnit 5 & Mockito**

## Project Structure

```
src/main/java/com/waters/punchout/
├── controller/          # REST API controllers
├── dto/                # Data Transfer Objects
├── entity/             # JPA entities
├── exception/          # Custom exceptions and global handler
├── mapper/             # Entity-DTO mappers
├── repository/         # JPA repositories
└── service/            # Business logic layer
```

## API Endpoints

### PunchOut Session Endpoints
- `GET /api/punchout-sessions` - Get all sessions (supports filtering)
- `GET /api/punchout-sessions/{sessionKey}` - Get session by key
- `POST /api/punchout-sessions` - Create new session
- `PUT /api/punchout-sessions/{sessionKey}` - Update session

### Order Object Endpoints
- `GET /api/punchout-sessions/{sessionKey}/order-object` - Get order object
- `POST /api/punchout-sessions/{sessionKey}/order-object` - Create/update order object

### Gateway Request Endpoints
- `GET /api/punchout-sessions/{sessionKey}/gateway-requests` - Get all gateway requests
- `POST /api/gateway-requests` - Create gateway request

## Configuration

### Database Configuration

**Production (PostgreSQL)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/punchout_db
    username: postgres
    password: postgres
```

**Development (H2)**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Access H2 Console: http://localhost:8080/h2-console

### Environment Profiles

- `default` - PostgreSQL (production)
- `dev` - H2 in-memory database with console enabled
- `test` - H2 in-memory for testing

## Build and Run

### Prerequisites
- Java 8 or higher
- Maven 3.6+

### Quick Start (No Database Setup Required!)

The application runs with H2 in-memory database by default with pre-loaded mock data:

```bash
cd punchout-ui-backend
mvn spring-boot:run
```

**That's it!** The application will start with sample data automatically loaded.

### Build
```bash
mvn clean install
```

### Run with Different Profiles

```bash
# Default (H2 in-memory with mock data)
mvn spring-boot:run

# PostgreSQL (requires database setup)
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Run Tests
```bash
mvn test
```

### Access Points

Once running:
- **API Base URL**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:punchoutdb`
  - Username: `sa`
  - Password: (leave empty)

### Sample API Calls

```bash
# Get all sessions
curl http://localhost:8080/api/punchout-sessions

# Get specific session
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001

# Get order object
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001/order-object

# Get gateway requests
curl http://localhost:8080/api/punchout-sessions/SESSION-2025-001/gateway-requests
```

## Database Schema

### PunchOutSession
- Primary Key: `sessionKey` (String)
- Indexed fields: `sessionDate`, `contactEmail`, `environment`

### OrderObject
- Primary Key: `sessionKey` (String)
- Foreign Key to PunchOutSession

### GatewayRequest
- Primary Key: `id` (Long, auto-generated)
- Foreign Key: `sessionKey`
- Indexed fields: `sessionKey`, `datetime`

## Performance Considerations

- Database indexes on frequently queried fields
- Optimized repository queries with `@Query` annotations
- Transaction management with `@Transactional`
- Read-only transactions for query operations

## Error Handling

All errors return consistent JSON responses:
```json
{
  "timestamp": "2025-10-31T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Session not found with key: XYZ",
  "path": "/api/punchout-sessions/XYZ"
}
```

## Logging

Logs are configured with SLF4J and Logback:
- Application logs: `com.waters.punchout` at INFO level
- Spring Web: DEBUG level
- Hibernate: INFO level

## Security

Currently, the API endpoints are open. For production:
- Implement OAuth2 or API key authentication
- Add Spring Security configuration
- Secure sensitive endpoints

## Future Enhancements

- Pagination for list endpoints
- Advanced search capabilities
- Caching for frequently accessed sessions
- API documentation with Swagger/OpenAPI
- Metrics and monitoring with Actuator

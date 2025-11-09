# UI Backend Integration Guide

## Overview
This guide explains how the frontend UI connects to the Spring Boot backend API.

## Configuration

### Backend URL Configuration

The backend API URL is configurable via environment variable. The frontend uses the `NEXT_PUBLIC_API_URL` environment variable to determine the backend endpoint.

**Default:** `http://localhost:8080/api`

### Setting Up Environment Variables

1. **For Local Development:**
   
   Create or edit `.env.local` file in the `punchout-ui-frontend` directory:
   
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   ```

2. **For Production:**
   
   ```bash
   NEXT_PUBLIC_API_URL=https://api.yourdomain.com/api
   ```

3. **For Staging:**
   
   ```bash
   NEXT_PUBLIC_API_URL=https://staging-api.yourdomain.com/api
   ```

### Example File

An `.env.example` file is provided with sample configuration. Copy it to create your local environment file:

```bash
cd punchout-ui-frontend
cp .env.example .env.local
# Edit .env.local with your settings
```

## API Endpoints

The frontend connects to these backend endpoints:

### Sessions API (MongoDB)
- `GET /api/v1/sessions` - Get all punchout sessions
- `GET /api/v1/sessions/{sessionKey}` - Get specific session by key

### Order Object API
- `GET /api/punchout-sessions/{sessionKey}/order-object` - Get order object for session
- `POST /api/punchout-sessions/{sessionKey}/order-object` - Create order object

### Gateway Request API
- `GET /api/punchout-sessions/{sessionKey}/gateway-requests` - Get gateway requests for session
- `POST /api/gateway-requests` - Create gateway request

## Running the Application

### 1. Start the Backend (Spring Boot)

Make sure MongoDB is running, then:

```bash
cd punchout-ui-backend
mvn spring-boot:run
```

Backend will start on: `http://localhost:8080`

### 2. Start the Frontend (Next.js)

```bash
cd punchout-ui-frontend
npm run dev
```

Frontend will start on: `http://localhost:3000`

## Troubleshooting

### CORS Issues

If you encounter CORS errors, ensure the backend has CORS enabled. The `SessionMongoController` already has:

```java
@CrossOrigin(origins = "*")
```

### Connection Refused

- Verify backend is running on port 8080
- Check MongoDB is running and accessible
- Verify the `.env.local` file has the correct URL

### API Not Found (404)

- Check the API endpoint paths match between frontend and backend
- Verify the backend controller mappings
- Check browser console for the actual request URL

## Checking Configuration

When the frontend starts, it will log the API base URL in the browser console:

```
API Base URL: http://localhost:8080/api
```

Each API request will also be logged:

```
API Request: GET http://localhost:8080/api/v1/sessions
```

## Sample Data

Sample MongoDB data has been imported. You can test the integration by:

1. Start backend and frontend
2. Navigate to `http://localhost:3000/sessions`
3. You should see 10 sample sessions
4. Click on any session to view details

## API Testing

A Postman collection is provided in `Punchout_API.postman_collection.json` for testing backend APIs directly.

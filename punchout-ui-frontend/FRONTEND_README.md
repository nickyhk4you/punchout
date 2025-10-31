# PunchOut UI Frontend

Modern Next.js frontend application for the PunchOut Session Management system.

## 🚀 Quick Start

### Prerequisites
- Node.js 18.x or higher
- npm 9.x or higher
- Backend API running on http://localhost:8080

### Installation & Run

```bash
cd punchout-ui-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at **http://localhost:3000**

---

## 📋 Features

### Dashboard (Home Page)
- **Real-time Statistics**
  - Total sessions count
  - Production sessions count
  - Total order value
  - Average order value
- **Recent Sessions Table** - Last 5 sessions
- **Quick Action Cards** - Navigate to different sections

### Sessions List Page (`/sessions`)
- **Advanced Filtering**
  - Filter by operation (CREATE, EDIT, INSPECT)
  - Filter by environment (PRODUCTION, STAGING, DEVELOPMENT)
  - Filter by route name
  - Clear all filters button
- **Sessions Table** with sorting and pagination
- **Color-coded Status Badges**
  - Operations: CREATE (green), EDIT (yellow), INSPECT (blue)
  - Environments: PRODUCTION (red), STAGING (orange), DEV (gray)
- **Responsive Design** - Mobile-friendly table

### Session Details Page (`/sessions/[sessionKey]`)
- **Complete Session Information**
  - All session metadata
  - Timestamps (session date, punched in/out)
  - Order details (value, line items, quantity)
  - Network and parser information
- **Order Object Details**
  - User information
  - Company details
  - Identity and lookup data
- **Gateway Requests Timeline**
  - All requests for the session
  - Timestamps and URIs
  - External links

---

## 🔧 Technology Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **UI Components**: Custom components with Tailwind

---

## 📁 Project Structure

```
src/
├── app/
│   ├── page.tsx                    # Dashboard/Home page
│   ├── layout.tsx                  # Root layout
│   ├── sessions/
│   │   ├── page.tsx               # Sessions list page
│   │   └── [sessionKey]/
│   │       └── page.tsx           # Session details page
├── lib/
│   └── api.ts                     # API client and methods
├── types/
│   └── index.ts                   # TypeScript interfaces
└── components/                     # Reusable components (existing)
```

---

## 🔌 API Integration

The frontend integrates with the backend REST API:

### API Service (`src/lib/api.ts`)

**Session Operations:**
- `getAllSessions(filters?)` - Get all sessions with optional filters
- `getSessionByKey(sessionKey)` - Get specific session
- `createSession(session)` - Create new session
- `updateSession(sessionKey, session)` - Update session

**Order Operations:**
- `getOrderObject(sessionKey)` - Get order for session
- `createOrderObject(sessionKey, order)` - Create/update order

**Gateway Operations:**
- `getGatewayRequests(sessionKey)` - Get gateway requests
- `createGatewayRequest(request)` - Create gateway request

### API Configuration

Set the backend URL in `.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## 🎨 UI Components

### Dashboard Cards
- Statistics cards with icons
- Color-coded metrics
- Real-time data updates

### Data Tables
- Sortable columns
- Filterable rows
- Responsive design
- Hover effects

### Status Badges
- Operation types (CREATE, EDIT, INSPECT)
- Environment indicators (PRODUCTION, STAGING, DEV)
- Custom color schemes

### Forms & Filters
- Select dropdowns
- Text inputs
- Clear filters functionality

---

## 📱 Responsive Design

The application is fully responsive:
- **Desktop**: Full-width tables, multi-column grids
- **Tablet**: 2-column grids, scrollable tables
- **Mobile**: Single-column layout, card-based design

---

## 🔄 Data Flow

```
User Action → Component → API Service → Backend REST API → Response → State Update → UI Refresh
```

### Example: Viewing Sessions

1. User navigates to `/sessions`
2. Component calls `sessionAPI.getAllSessions()`
3. Axios sends GET request to `http://localhost:8080/api/punchout-sessions`
4. Backend returns session array
5. React state updates with data
6. Table re-renders with sessions

---

## 🛠️ Development

### Run Development Server
```bash
npm run dev
```

### Build for Production
```bash
npm run build
npm start
```

### Lint Code
```bash
npm run lint
```

---

## 🔒 Environment Variables

Create `.env.local` file:

```env
# Backend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080/api

# Application Info
NEXT_PUBLIC_APP_NAME=PunchOut Session Manager
NEXT_PUBLIC_APP_VERSION=1.0.0
```

---

## 📊 Sample Data

The backend provides mock data:
- 5 PunchOut Sessions
- 4 Order Objects
- 9 Gateway Requests

This data is automatically loaded when the backend starts.

---

## 🚦 Running Full Stack

### Terminal 1: Start Backend
```bash
cd punchout-ui-backend
mvn spring-boot:run
```

### Terminal 2: Start Frontend
```bash
cd punchout-ui-frontend
npm run dev
```

### Access Application
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- H2 Console: http://localhost:8080/h2-console

---

## 🎯 Key Features

### Real-time Updates
- Automatic data refresh
- Loading states
- Error handling

### User Experience
- Smooth transitions
- Hover effects
- Visual feedback
- Responsive navigation

### Data Visualization
- Currency formatting
- Date/time formatting
- Color-coded statuses
- Icon indicators

---

## 🐛 Troubleshooting

### Backend Not Connecting
```bash
# Check if backend is running
curl http://localhost:8080/api/punchout-sessions

# Update API URL in .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

### Port Already in Use
```bash
# Change port in package.json or use:
npm run dev -- -p 3001
```

### Build Errors
```bash
# Clear cache and reinstall
rm -rf .next node_modules
npm install
npm run dev
```

---

## 📚 Next Steps

1. **Add Authentication** - Protect routes and API calls
2. **Add Create/Edit Forms** - Allow creating new sessions via UI
3. **Add Charts** - Visualize session data with Chart.js
4. **Add Real-time Updates** - WebSocket integration
5. **Add Export** - Download sessions as CSV/Excel

---

## 🤝 Integration with Backend

The frontend is designed to work seamlessly with the Spring Boot backend:

- **Type Safety**: TypeScript interfaces match backend DTOs
- **Error Handling**: Catches and displays API errors
- **Loading States**: Shows spinners during API calls
- **Null Safety**: Handles optional fields gracefully

---

**Enjoy building with the PunchOut Session Manager! 🎉**

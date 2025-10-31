# 🚀 Frontend Quick Start Guide

## Run the Frontend in 3 Steps!

### Step 1: Navigate to Frontend Directory
```bash
cd /Users/nickhu/dev/java/punchout/punchout-ui-frontend
```

### Step 2: Install Dependencies (First Time Only)
```bash
npm install
```

### Step 3: Start Development Server
```bash
npm run dev
```

**✅ Done!** Open your browser to **http://localhost:3000**

---

## 📋 Prerequisites

Make sure the backend is running first:

```bash
# In another terminal
cd /Users/nickhu/dev/java/punchout/punchout-ui-backend
mvn spring-boot:run
```

Backend should be accessible at: http://localhost:8080

---

## 🎯 What You'll See

### Home Page (Dashboard) - http://localhost:3000
- 📊 Statistics cards showing:
  - Total sessions
  - Production sessions
  - Total order value
  - Average order value
- 📋 Recent sessions table
- 🎯 Quick action cards

### Sessions Page - http://localhost:3000/sessions
- 🔍 Filter sessions by:
  - Operation (CREATE, EDIT, INSPECT)
  - Environment (PRODUCTION, STAGING, DEVELOPMENT)
  - Route name
- 📊 Full sessions table with all details
- 🎨 Color-coded status badges

### Session Details - http://localhost:3000/sessions/SESSION-2025-001
- 📝 Complete session information
- 👤 Order object details
- 🌐 Gateway requests timeline

---

## 🧪 Test the Integration

### 1. View Sessions
```
URL: http://localhost:3000/sessions
Expected: See 5 mock sessions from backend
```

### 2. Filter by Production
```
Click "Environment" dropdown → Select "PRODUCTION"
Expected: See only production sessions
```

### 3. View Session Details
```
Click "View Details" on any session
Expected: See full session info, order object, and gateway requests
```

### 4. Check Statistics
```
URL: http://localhost:3000
Expected: Dashboard shows totals calculated from all sessions
```

---

## 🛠️ Available Commands

```bash
# Development mode (with hot reload)
npm run dev

# Production build
npm run build

# Start production server
npm start

# Run linter
npm run lint
```

---

## 🔧 Configuration

### Backend API URL

The frontend is configured to connect to:
```
http://localhost:8080/api
```

To change it, edit `.env.local`:
```env
NEXT_PUBLIC_API_URL=http://your-backend-url/api
```

---

## 📱 Responsive Design

The UI works on:
- 🖥️ Desktop (1920px+)
- 💻 Laptop (1024px+)
- 📱 Tablet (768px+)
- 📱 Mobile (320px+)

---

## 🎨 UI Features

### Color Coding
- **Operations**
  - CREATE: Green badge
  - EDIT: Yellow badge
  - INSPECT: Blue badge

- **Environments**
  - PRODUCTION: Red badge
  - STAGING: Orange badge
  - DEVELOPMENT: Gray badge

### Interactive Elements
- ✅ Hover effects on rows
- ✅ Loading spinners
- ✅ Error messages
- ✅ Responsive tables
- ✅ Smooth transitions

---

## 📊 Sample Data

The backend provides these test sessions:
1. **SESSION-2025-001** - ACME Corp (Production)
2. **SESSION-2025-002** - Globex Corp (Staging)
3. **SESSION-2025-003** - Initech (Development)
4. **SESSION-2025-004** - Umbrella Corp (Production)
5. **SESSION-2025-005** - Cyberdyne Systems (Production)

---

## 🐛 Troubleshooting

### Problem: "Failed to load sessions"

**Solution:**
```bash
# Check backend is running
curl http://localhost:8080/api/punchout-sessions

# Should return JSON array of sessions
```

### Problem: Page shows loading forever

**Solution:**
1. Open browser DevTools (F12)
2. Check Console for errors
3. Check Network tab for failed requests
4. Verify backend URL in .env.local

### Problem: Styles not loading

**Solution:**
```bash
# Clear Next.js cache
rm -rf .next
npm run dev
```

### Problem: Port 3000 already in use

**Solution:**
```bash
# Use different port
npm run dev -- -p 3001

# Then access: http://localhost:3001
```

---

## 🔄 Full Stack Running

### Terminal 1: Backend
```bash
cd punchout-ui-backend
mvn spring-boot:run
```
✅ Backend running on http://localhost:8080

### Terminal 2: Frontend
```bash
cd punchout-ui-frontend
npm run dev
```
✅ Frontend running on http://localhost:3000

### Access Points
- 🌐 **Frontend UI**: http://localhost:3000
- 🔌 **Backend API**: http://localhost:8080/api
- 🗄️ **H2 Console**: http://localhost:8080/h2-console

---

## ✨ Next Steps

1. ✅ Browse sessions on the dashboard
2. ✅ Filter sessions by environment
3. ✅ View detailed session information
4. ✅ Check order objects and gateway requests
5. ✅ Explore the responsive design on mobile

---

## 📚 Learn More

- See [FRONTEND_README.md](./FRONTEND_README.md) for detailed documentation
- See [../punchout-ui-backend/QUICKSTART.md](../punchout-ui-backend/QUICKSTART.md) for backend guide

---

**Happy coding! 🎉**

# 🚀 Quick Run Instructions

## Start the Complete Application in 2 Terminals

### Terminal 1: Backend
```bash
cd /Users/nickhu/dev/java/punchout/punchout-ui-backend
mvn spring-boot:run
```
✅ Wait for: "Mock data initialization completed!"

### Terminal 2: Frontend
```bash
cd /Users/nickhu/dev/java/punchout/punchout-ui-frontend
npm run dev
```
✅ Wait for: "Ready on http://localhost:3000"

---

## Access the Application

Open your browser to: **http://localhost:3000**

You should see:
- 📊 Dashboard with statistics
- 📋 5 sample sessions
- 💰 Total order value displayed

---

## Test the Features

1. **Dashboard** → View statistics and recent sessions
2. **Click "View All Sessions"** → See full session list
3. **Filter by Environment** → Select "PRODUCTION"
4. **Click "View Details"** → See complete session information

---

## Verify Backend

```bash
curl http://localhost:8080/api/punchout-sessions
```

Should return 5 sessions in JSON format.

---

## Access Points

| What | URL |
|------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api |
| H2 Database | http://localhost:8080/h2-console |

### H2 Console Login
- **JDBC URL**: `jdbc:h2:mem:punchoutdb`
- **Username**: `sa`
- **Password**: (leave empty)

---

## Stop the Applications

- **Backend**: Press `Ctrl+C` in Terminal 1
- **Frontend**: Press `Ctrl+C` in Terminal 2

---

## Need Help?

See detailed guides:
- [FULL_STACK_GUIDE.md](./FULL_STACK_GUIDE.md) - Complete documentation
- [punchout-ui-backend/QUICKSTART.md](./punchout-ui-backend/QUICKSTART.md) - Backend guide
- [punchout-ui-frontend/QUICKSTART_FRONTEND.md](./punchout-ui-frontend/QUICKSTART_FRONTEND.md) - Frontend guide

---

**That's it! Enjoy your PunchOut Session Manager! 🎉**

# 🚀 QUICK START GUIDE - Get IPL App Running

## ⚡ **TL;DR - Copy-Paste These Commands:**

### Terminal 1 (Backend):
```bash
cd C:\Users\User\Desktop\IPL-APP\IPL-BACKEND
mvn spring-boot:run
```

### Terminal 2 (Frontend):
```bash
cd C:\Users\User\Desktop\IPL-APP\IPL-FRONTEND
npm start
```

**Open browser:** `http://localhost:3001/dashboard`

---

## 📋 Detailed Steps

### Step 1: Verify Java & Maven

```bash
java -version
mvn -version
```

**Required:** Java 11+, Maven 3.6+

---

### Step 2: Start Backend (Spring Boot)

```bash
cd IPL-BACKEND
mvn spring-boot:run
```

**Wait for this message:**
```
Started IPLBackendApplication in 4.3 seconds (JVM running for 5.1)
```

**Backend runs at:** `http://localhost:8081/ipl-backend`

**Test it now:** Open `http://localhost:8081/ipl-backend/api/matches` in browser
→ Should show JSON with 10 matches

**If port 8081 is busy:**
```bash
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

---

### Step 3: Start Frontend (Node.js Express)

```bash
cd IPL-FRONTEND
npm install   # first time only
npm start
```

**Expected output:**
```
🎯 Frontend server running at http://localhost:3001
🔗 Connected to Backend: http://localhost:8081/ipl-backend/api
```

**DO NOT** double-click HTML files. Use `http://localhost:3001/dashboard`

---

### Step 4: Open App

**Main Dashboard:** `http://localhost:3001/dashboard`

**Pages:**
- Home: `http://localhost:3001/`
- Matches: `http://localhost:3001/#matches`
- Leaderboard: `http://localhost:3001/#leaderboard`

---

## 🔍 Troubleshooting: "No Data Showing"

### Problem: Pages load but show empty

**Checklist:**

1. **Is backend running?**
   - Look at Terminal 1 - is it showing logs?
   - Test: `http://localhost:8081/ipl-backend/api/matches` in browser
   - If 404/error → backend not started correctly

2. **Is frontend on port 3001?**
   - Terminal 2 should say `running at http://localhost:3001`
   - If it says 8080, edit `server.js` line 4: `const PORT = 3001;`

3. **Check browser console (F12)**
   - Press F12 → Console tab
   - Look for red errors:
     - `Failed to fetch` → backend down
     - `CORS` → check backend CORS includes port 3001
     - `Unexpected token` → backend returning HTML error page

4. **Check H2 Database has data**
   - Open: `http://localhost:8081/ipl-backend/h2-console`
   - JDBC URL: `jdbc:h2:mem:ipldb`
   - Username: `sa` (no password)
   - Run: `SELECT COUNT(*) FROM matches;`
   - Should return `10`. If 0 → restart backend.

5. **Restart both servers**
   ```bash
   # Terminal 1: Ctrl+C, then:
   cd IPL-BACKEND
   mvn spring-boot:run
   
   # Terminal 2: Ctrl+C, then:
   cd IPL-FRONTEND
   npm start
   ```

---

## 📡 API Endpoints Reference

| Endpoint | URL |
|----------|-----|
| All Matches | `GET /ipl-backend/api/matches` |
| Today's Match | `GET /ipl-backend/api/matches/today` |
| Leaderboard | `GET /ipl-backend/api/leaderboard` |
| Submit Prediction | `POST /ipl-backend/api/predictions` |
| Users | `GET /ipl-backend/api/users/{id}` |
| H2 Console | `http://localhost:8081/ipl-backend/h2-console` |

---

## 🎯 Expected Behavior

**After successful startup:**

1. **Home page** (`/`) → Shows today's match + full season table with 10 matches
2. **Matches page** (`/#matches`) → Shows all matches + prediction form dropdowns populated
3. **Leaderboard page** (`/#leaderboard`) → Shows top 3 podium + full leaderboard table with 4 seeded users (`user1`, `user2`, `user3`, `admin`)

**Seeded users:**
- `user1` / `pass1` (80 pts)
- `user2` / `pass2` (60 pts)
- `user3` / `pass3` (40 pts)
- `admin` / `admin123` (100 pts)

---

## ❌ Common Errors & Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `Connection refused` | Backend not running | Start backend: `mvn spring-boot:run` |
| `404 Not Found` | Wrong port | Backend uses 8081, not 8080 |
| `CORS header ‘Access-Control-Allow-Origin’ missing` | Frontend port not in CORS | Backend already allows 3001, 5500, 8080 |
| Empty tables in H2 console | Seed not executed | Restart backend (tables auto-seed on first call) |
| `npm ERR!` | Dependencies missing | Run `npm install` first |

---

## 📝 Project Structure Recap

```
IPL-APP/
├── IPL-BACKEND/          ← Spring Boot (port 8081)
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── init.sql
│   └── pom.xml
│
├── IPL-FRONTEND/         ← Node.js Express (port 3001)
│   ├── server.js
│   ├── app.js
│   ├── *.html (pages)
│   ├── package.json
│   └── node_modules/
│
└── DEBUG.md              ← Detailed troubleshooting
```

---

## 🆘 Need Help?

1. Read `DEBUG.md` for detailed troubleshooting
2. Check browser console (F12) for errors
3. Verify backend responds: `curl http://localhost:8081/ipl-backend/api/matches`
4. Check H2 console for table data

---

**Ready? Start both servers and go to `http://localhost:3001/dashboard`** 🎉

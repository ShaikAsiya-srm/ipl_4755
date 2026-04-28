# 🔧 STEP-BY-STEP FIX: No Data Loading

## Problem
Home page, Matches page, and Leaderboard page show no data.

## ✅ Solution Steps

### 1️⃣ START THE BACKEND (Spring Boot)

Open a **new terminal** and run:

```bash
cd IPL-BACKEND
mvn spring-boot:run
```

**Wait for this success message:**
```
Started IPLBackendApplication in 5.2 seconds (JVM running for 6.1)
```

**Backend URL:** `http://localhost:8081/ipl-backend`

---

### 2️⃣ VERIFY BACKEND IS WORKING

Open in browser:
- **Matches API:** http://localhost:8081/ipl-backend/api/matches
  - Should see JSON array of 10 matches with team names like "Royal Challengers Bangalore"
- **Today's Match:** http://localhost:8081/ipl-backend/api/matches/today
- **Leaderboard:** http://localhost:8081/ipl-backend/api/leaderboard
  - Should see array of users: `[{"username":"user1","score":80},...]`

**If you see:**
- `404 Not Found` → backend not running or wrong port
- `[]` (empty array) → database empty, restart backend to re-seed
- Error page → check terminal for stack trace

---

### 3️⃣ START THE FRONTEND SERVER (Node.js)

In a **new terminal**:

```bash
cd IPL-FRONTEND
npm install   # first time only
npm start
```

**You should see:**
```
🎯 Frontend server running at http://localhost:3001
🔗 Connected to Backend: http://localhost:8081/ipl-backend/api
```

**If backend port message says 8080**, edit `server.js` line 21 to say 8081.

---

### 4️⃣ OPEN THE APP IN BROWSER

**DO NOT** double-click HTML files (file:// won't work with fetch CORS).

Instead, open:
```
http://localhost:3001/dashboard
```

Or navigate:
- Home: `http://localhost:3001/`
- Matches: `http://localhost:3001/#matches`
- Leaderboard: `http://localhost:3001/#leaderboard`

---

### 5️⃣ CHECK BROWSER CONSOLE FOR ERRORS

Press **F12** → Console tab.

**Look for:**
- `Failed to fetch` → backend not running on port 8081
- `CORS policy` → check backend CORS config allows localhost:3001
- `Unexpected token` → JSON parse error (backend returning HTML error page)

**Fix based on what you see:**

#### ❌ "Failed to fetch" or "Connection refused"
→ Backend not running. Go to Step 1.

#### ❌ "404 Not Found" for `/api/matches`
→ Backend context path mismatch. Check `application.properties` has:
```
server.servlet.context-path=/ipl-backend
```

#### ❌ CORS error: "No 'Access-Control-Allow-Origin' header"
→ Backend CORS needs to allow port 3001. Check all `@CrossOrigin` annotations in controllers include `http://localhost:3001`.

#### ❌ Empty data `[]` but status 200
→ Database not seeded. Restart backend (Ctrl+C then `mvn spring-boot:run`). Check H2 console:
  - Visit: http://localhost:8081/ipl-backend/h2-console
  - JDBC URL: `jdbc:h2:mem:ipldb`
  - Username: `sa`, Password: (empty)
  - Run: `SELECT * FROM matches;`

---

### 6️⃣ QUICK TEST WITH CURL

Open terminal and run:
```bash
curl http://localhost:8081/ipl-backend/api/matches
```

**Expected output:** JSON array with 10 matches.

**If empty or error:** Backend issue, not frontend.

---

### 7️⃣ COMMON FIXES

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Backend won't start | Port 8081 in use | `netstat -ano \| findstr :8081` (Windows) kill the process |
| Frontend shows nothing but no console errors | API returned empty data | Check backend logs: `SELECT COUNT(*) FROM matches;` |
| CORS error | Frontend port not in @CrossOrigin | Add `"http://localhost:3001"` to all controllers |
| "Cannot read property 'map' of undefined" | API returned empty array | Ensure backend seeded data; check `MatchService.insertDefaultMatchesIfEmpty()` called |

---

### 8️⃣ FINAL VERIFICATION

After fixing, you should see:

**Home page:**
- Today's match card populated
- Full season schedule table with 10 matches

**Matches page:**
- Match list populated
- Prediction dropdowns populated with team names

**Leaderboard page:**
- Top 3 podium with real usernames
- Full leaderboard table with scores

---

## 📝 Summary of What Was Fixed

1. **home-module.html** - Fixed JS syntax error, changed `/matches/live` → `/matches/today`
2. **LeaderboardController** - Fixed duplicate method, ensured proper closing brace
3. **All frontend files** - Added `BASE_URL = "http://localhost:8081/ipl-backend"`
4. **Backend CORS** - Added localhost:3001, 5500, 8080 to all controllers
5. **Database schema** - Added `points` column, proper FK constraints, full team names

---

## 🚀 STILL NOT WORKING?

1. Share terminal output from backend startup
2. Share browser console screenshot (F12 → Console)
3. Test API directly: `curl -v http://localhost:8081/ipl-backend/api/matches`

# IPL Prediction - Complete Application

A full-stack IPL prediction application built with **Java Servlets** backend and **HTML/CSS/JavaScript** frontend.

## 📋 Quick Overview

- **Frontend**: IPL-APP folder (HTML5, CSS3, JavaScript)
- **Backend**: IPL-BACKEND folder (Java Servlets)
- **Database**: Oracle SQL Developer (Oracle JDBC connection)

---

## 🛠️ Prerequisites

1. **Oracle Database** - Downloaded and installed
2. **Oracle SQL Developer** - For database management
3. **Apache Tomcat 9** - Download from https://tomcat.apache.org/download-90.cgi
4. **Java 11+** - For compiling backend
5. **Maven** - For building the backend

---

## 📂 Project Structure

```
IPL_model-main/
├── IPL-APP/
│   ├── index.html        # Main HTML file with buttons
│   ├── app.js           # JavaScript with API integration
│   ├── style.css        # Styling
│   └── README.md        # This file
│
└── IPL-BACKEND/
    ├── src/main/java/com/ipl/backend/
    │   ├── DBConnection.java        # Oracle JDBC connection
    │   ├── User.java               # User model
    │   ├── Match.java              # Match model
    │   ├── Prediction.java         # Prediction model
    │   ├── MatchServlet.java       # Match API
    │   ├── UserServlet.java        # User login/register
    │   ├── PredictionServlet.java  # Prediction API
    │   └── LeaderboardServlet.java # Leaderboard API
    ├── src/main/resources/
    │   └── oracle-init.sql         # Oracle database setup
    ├── src/main/webapp/WEB-INF/
    │   └── web.xml                 # Servlet configuration
    ├── pom.xml                    # Maven dependencies
    └── target/ipl-backend-1.0-SNAPSHOT.war  # Deployable WAR
```

---

## 🎯 Step 1: Setup Oracle Database

### 1.1 Create Oracle User

Open **SQL*Plus** or **Oracle SQL Developer** and run:

```sql
-- Connect as SYSTEM user first
CREATE USER ipl_user IDENTIFIED BY password;
GRANT CREATE SESSION TO ipl_user;
GRANT CREATE TABLE TO ipl_user;
GRANT UNLIMITED TABLESPACE TO ipl_user;
```

### 1.2 Create Tables and Insert Data

**Connect as ipl_user** and run the script from:
`IPL-BACKEND/src/main/resources/oracle-init.sql`

Or manually execute:

```sql
-- Create sequences
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE match_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE prediction_seq START WITH 1 INCREMENT BY 1;

-- Create Users Table
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    score NUMBER(5) DEFAULT 0,
    created_at TIMESTAMP DEFAULT SYSDATE
);

-- Create Matches Table
CREATE TABLE matches (
    id NUMBER PRIMARY KEY,
    team1 VARCHAR2(50) NOT NULL,
    team2 VARCHAR2(50) NOT NULL,
    logo1 VARCHAR2(255),
    logo2 VARCHAR2(255),
    stadium VARCHAR2(100),
    time VARCHAR2(20),
    is_today NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT SYSDATE
);

-- Create Predictions Table
CREATE TABLE predictions (
    id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    match_id NUMBER NOT NULL,
    toss_winner VARCHAR2(50),
    bat_first VARCHAR2(50),
    player VARCHAR2(100),
    created_at TIMESTAMP DEFAULT SYSDATE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (match_id) REFERENCES matches(id)
);

-- Insert Sample Users
INSERT INTO users VALUES (user_seq.NEXTVAL, 'user1', 'pass1', 80, SYSDATE);
INSERT INTO users VALUES (user_seq.NEXTVAL, 'user2', 'pass2', 60, SYSDATE);
INSERT INTO users VALUES (user_seq.NEXTVAL, 'user3', 'pass3', 40, SYSDATE);

-- Insert Sample Matches
INSERT INTO matches VALUES (match_seq.NEXTVAL, 'CSK', 'RCB', 'https://...logo...', 'https://...logo...', 'Wankhede', '7:30 PM', 1, SYSDATE);
INSERT INTO matches VALUES (match_seq.NEXTVAL, 'MI', 'KKR', 'https://...logo...', 'https://...logo...', 'Eden Gardens', '8:00 PM', 0, SYSDATE);

COMMIT;
```

---

## 🚀 Step 2: Configure Database Connection

Edit: `IPL-BACKEND/src/main/java/com/ipl/backend/DBConnection.java`

```java
private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
private static final String USER = "ipl_user";
private static final String PASSWORD = "password"; // Your Oracle password
```

Update the connection details based on your Oracle setup:
- **localhost** - Your Oracle server hostname
- **1521** - Oracle default port (change if different)
- **xe** - Your database SID (e.g., orcl, xe, etc.)

---

## 🔧 Step 3: Build & Deploy Backend

### 3.1 Build the Backend

```bash
cd IPL-BACKEND
mvn clean package
```

### 3.2 Deploy to Tomcat

1. Copy `IPL-BACKEND/target/ipl-backend-1.0-SNAPSHOT.war` to `Tomcat/webapps/` folder
2. Start Tomcat:
   ```bash
   cd Tomcat/bin
   startup.bat  (Windows)
   ./startup.sh (Linux/Mac)
   ```

### 3.3 Verify Backend

Open browser and visit:
`http://localhost:8080/ipl-backend-1.0-SNAPSHOT/api/matches`

Should return JSON with matches data.

---

## 🎨 Step 4: Run Frontend

### Option A: VS Code Live Server (Recommended)

1. Install "Live Server" extension in VS Code
2. Right-click `IPL-APP/index.html` → Open with Live Server
3. Frontend runs on: `http://localhost:3000`

### Option B: Direct Browser

Just open `IPL-APP/index.html` directly in your browser.

---

## 🎮 Features & Buttons

### Home Page
- 🔄 **Refresh Matches** - Reload today's match data
- 📊 **My Predictions** - View your submitted predictions from database

### Matches Page
- **All Matches** - Show all matches
- **Today** - Show today's matches only
- **Upcoming** - Show upcoming matches

### Leaderboard Page
- 🔄 **Refresh** - Reload leaderboard scores from database
- 📥 **Download** - Download leaderboard as CSV file

### General Features
- ✅ **Register/Login** - Create account and login (stores in Oracle)
- 🎯 **Make Predictions** - Predict toss winner, bat first, player of match
- 📈 **View Scores** - See your points in navbar after login
- 🔐 **Secure** - All data stored in Oracle database

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/matches` | Get all matches from database |
| POST | `/api/predictions` | Submit prediction to database |
| GET | `/api/predictions?userId=X` | Get user's predictions |
| POST | `/api/users?action=login` | Login user |
| POST | `/api/users?action=register` | Register new user |
| GET | `/api/leaderboard` | Get top 10 users by score |

---

## 🐛 Troubleshooting

### Backend Issues

**Error: "Oracle Driver not found"**
- Ensure `ojdbc11` is in Maven dependencies (check pom.xml)
- Run `mvn clean package` to download dependencies

**Error: "Cannot connect to database"**
- Check Oracle is running
- Verify connection details in DBConnection.java
- Make sure username/password are correct
- Check SID matches your Oracle instance

**404 Error on `/api/matches`**
- Ensure Tomcat is running
- Check WAR is deployed in webapps folder
- Tomcat might need restart after deploying

### Frontend Issues

**Predictions not saving**
- Check browser console (F12) for errors
- Verify backend is running and accessible
- Check network tab to see API responses

**Login always fails**
- Make sure database has users table with sample data
- Check DBConnection credentials

---

## 📝 Database Setup Checklist

- [ ] Oracle Database installed and running
- [ ] ipl_user created with proper grants
- [ ] Users, matches, predictions tables created
- [ ] Sample data inserted
- [ ] DBConnection.java has correct Oracle credentials
- [ ] Oracle JDBC driver in Maven (pom.xml)

---

## 🎓 Technology Stack

- **Frontend**: HTML5, CSS3, ES6+ JavaScript, Fetch API
- **Backend**: Java 11, Servlets, Jackson JSON
- **Database**: Oracle Database with JDBC
- **Build**: Maven 3.x
- **Server**: Apache Tomcat 9
- **Other**: VS Code Live Server

---

## 📌 Notes

- All user data, predictions, and scores are stored in **Oracle Database**
- No static data - everything is dynamic from database
- CORS headers enabled on all servlets for frontend communication
- Predictions are validated before saving

---

## 🤝 Support

If you encounter issues:
1. Check the browser console for JavaScript errors (F12)
2. Check Tomcat logs in `Tomcat/logs/catalina.out`
3. Verify Oracle database connection with `sqlplus`
4. Ensure all required software versions are met

---

**Happy Predicting! 🏏**
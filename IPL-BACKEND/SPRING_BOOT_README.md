# IPL Manager Dashboard - Spring Boot Version

A complete IPL (Indian Premier League) prediction and leaderboard management system built with **Spring Boot** (Backend) and **Vanilla JavaScript** (Frontend).

## 📋 Project Structure

```
IPL-APP/
├── IPL-BACKEND/          # Spring Boot Backend (Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ipl/backend/
│   │   │   │       ├── IplApplication.java          # Main Spring Boot app
│   │   │   │       ├── controller/                  # REST Controllers
│   │   │   │       │   ├── UserController.java
│   │   │   │       │   ├── MatchController.java
│   │   │   │       │   ├── PredictionController.java
│   │   │   │       │   └── LeaderboardController.java
│   │   │   │       ├── service/                     # Business Logic
│   │   │   │       │   ├── UserService.java
│   │   │   │       │   ├── MatchService.java
│   │   │   │       │   ├── PredictionService.java
│   │   │   │       │   └── LeaderboardService.java
│   │   │   │       ├── model/                       # Data Models
│   │   │   │       │   ├── User.java
│   │   │   │       │   ├── Match.java
│   │   │   │       │   └── Prediction.java
│   │   │   │       └── util/                        # Database Connection
│   │   │   │           └── DBConnection.java
│   │   │   └── resources/
│   │   │       ├── application.properties           # Spring Boot config
│   │   │       └── init.sql                         # Database init script
│   │   └── test/                                    # Unit tests
│   └── pom.xml                                      # Maven POM (Spring Boot)
│
└── IPL-FRONTEND/         # Static Frontend (JavaScript)
    ├── manager-dashboard.html   # Main HTML page
    ├── app.js                    # Frontend application logic
    ├── style.css                 # Styling
    ├── server.js                 # Express.js server
    └── package.json              # Node.js dependencies
```

## 🚀 Quick Start

### Prerequisites

- **Java 11+** (for Spring Boot)
- **Maven 3.6+** (for building the backend)
- **Node.js 14+** (for the frontend server)
- **Oracle Database** (XE or Standard Edition)

### Step 1: Setup Oracle Database

```sql
-- Create user and schema
CREATE USER ipl_user IDENTIFIED BY ipl123;
GRANT CONNECT, RESOURCE TO ipl_user;

-- Run the init script
sqlplus ipl_user/ipl123 @init.sql
```

Or use the alternate Oracle script:
```bash
sqlplus ipl_user/ipl123 @oracle-init.sql
```

### Step 2: Start the Backend (Spring Boot)

```bash
cd IPL-BACKEND
mvn clean compile spring-boot:run
```

Or build and run:
```bash
mvn clean package
java -jar target/ipl-backend-1.0-SNAPSHOT.jar
```

**Backend will be available at:** `http://localhost:8080/ipl-backend`

### Step 3: Start the Frontend (Node.js)

```bash
cd IPL-FRONTEND
npm install
npm start
```

**Frontend will be available at:** `http://localhost:3000`

## 📡 API Endpoints

### User Management
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - Login user
- `GET /api/users/auth` - Check session

**Request/Response Format:**
```json
{
  "username": "john_doe",
  "password": "secure_pass",
  "id": 1,
  "score": 150
}
```

### Matches
- `GET /api/matches` - Get all matches
- `GET /api/matches/{id}` - Get specific match
- `GET /api/matches/today` - Get today's match

**Response Format:**
```json
{
  "id": 1,
  "team1": "Mumbai Indians",
  "team2": "Chennai Super Kings",
  "logo1": "https://...",
  "logo2": "https://...",
  "stadium": "Wankhede Stadium",
  "time": "7:30 PM",
  "isToday": true
}
```

### Predictions
- `POST /api/predictions` - Create prediction
- `GET /api/predictions/user/{userId}` - Get user predictions
- `GET /api/predictions/match/{matchId}` - Get match predictions
- `GET /api/predictions/{id}` - Get specific prediction

**Request Format:**
```json
{
  "userId": 1,
  "matchId": 5,
  "tossWinner": "Mumbai Indians",
  "batFirst": "Chennai Super Kings",
  "player": "Virat Kohli"
}
```

### Leaderboard
- `GET /api/leaderboard` - Get top players
- `GET /api/leaderboard/rank/{userId}` - Get user rank

**Response Format:**
```json
[
  {
    "id": 1,
    "username": "player1",
    "score": 450
  },
  {
    "id": 2,
    "username": "player2",
    "score": 420
  }
]
```

## 🛠 Configuration

### Backend Configuration (application.properties)

```properties
# Server
server.port=8080
server.servlet.context-path=/ipl-backend

# Database
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=ipl_user
spring.datasource.password=ipl123

# Logging
logging.level.com.ipl.backend=DEBUG
```

### Frontend Configuration (app.js)

```javascript
const API_BASE = "http://localhost:8080/ipl-backend/api";
```

## 📦 Technologies Used

### Backend
- **Spring Boot 3.1.5** - Modern Java framework
- **Spring Web** - REST API support
- **Spring Data JPA** - Database operations
- **Oracle JDBC** - Database driver
- **Lombok** - Boilerplate reduction
- **Maven** - Build management

### Frontend
- **Express.js** - Simple HTTP server
- **Vanilla JavaScript** - No framework dependencies
- **HTML5** - Semantic markup
- **CSS3** - Modern styling

## 🗄 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(100) NOT NULL,
    score NUMBER DEFAULT 0
);
```

### Matches Table
```sql
CREATE TABLE matches (
    id NUMBER PRIMARY KEY,
    team1 VARCHAR2(50),
    team2 VARCHAR2(50),
    logo1 VARCHAR2(500),
    logo2 VARCHAR2(500),
    stadium VARCHAR2(100),
    time VARCHAR2(20),
    is_today NUMBER(1) DEFAULT 0
);
```

### Predictions Table
```sql
CREATE TABLE predictions (
    id NUMBER PRIMARY KEY,
    user_id NUMBER,
    match_id NUMBER,
    toss_winner VARCHAR2(50),
    bat_first VARCHAR2(50),
    player VARCHAR2(100),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (match_id) REFERENCES matches(id)
);
```

## 🔄 Features

✅ User Registration & Login
✅ View all IPL Matches
✅ View match details
✅ Make predictions for matches
✅ View personal predictions
✅ Global leaderboard
✅ User rankings
✅ Points system
✅ Real-time UI updates
✅ CORS enabled for frontend

## 🐛 Troubleshooting

### Backend won't start
- Check Java version: `java -version` (should be 11+)
- Check Oracle database connection
- Check if port 8080 is available

### Frontend can't connect to backend
- Verify backend is running on port 8080
- Check CORS configuration in `IplApplication.java`
- Open browser console for detailed errors

### Database connection issues
- Verify Oracle service is running
- Check credentials in `application.properties`
- Verify database schema is initialized

## 📝 License

This project is open source and available under the MIT License.

## 👨‍💻 Development Notes

- Spring Boot auto-restarts on file changes (DevTools enabled)
- Frontend requires `npm install` before first run
- Database initialization scripts are in `src/main/resources/`
- Lombok reduces getter/setter boilerplate
- CORS is configured to allow localhost:3000

## 🚀 Deployment

### Production Build
```bash
# Backend
mvn clean package -DskipTests
# Creates: target/ipl-backend-1.0-SNAPSHOT.jar

# Frontend
npm install --production
```

### Docker Support (Optional)
Create a `Dockerfile` in the root directory for containerized deployment.

---

**Happy Predicting!** 🏏

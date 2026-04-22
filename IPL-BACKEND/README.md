# IPL Backend

This is the backend for the IPL Prediction application, built with Java Servlets and MySQL database.

## Prerequisites

- Java 11 or higher
- Maven
- MySQL Server
- Apache Tomcat or any servlet container

## Setup

1. **Database Setup:**
   - Install MySQL and create a database user.
   - Run the SQL script in `src/main/resources/init.sql` to create the database and tables.

2. **Configure Database Connection:**
   - Update `DBConnection.java` with your MySQL credentials (URL, USER, PASSWORD).

3. **Build the Project:**
   ```
   mvn clean compile
   ```

4. **Package the WAR:**
   ```
   mvn package
   ```

5. **Deploy to Tomcat:**
   - Copy the generated `target/ipl-backend-1.0-SNAPSHOT.war` to Tomcat's `webapps` directory.
   - Start Tomcat.

## API Endpoints

- `GET /api/matches` - Get all matches
- `POST /api/predictions` - Submit a prediction (JSON body)
- `GET /api/predictions?userId=<id>` - Get predictions for a user
- `POST /api/users?action=login` - Login (form data)
- `POST /api/users?action=register` - Register (form data)
- `GET /api/leaderboard` - Get leaderboard

## Frontend Integration

The frontend should make AJAX requests to these endpoints. For example, update the frontend's JavaScript to fetch data from the backend instead of using static data.
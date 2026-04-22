package com.ipl.backend.service;

import com.ipl.backend.model.User;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class UserService {

    // Initialize database tables
    private void initializeTables() throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "score INT DEFAULT 0, " +
            "email VARCHAR(100))";

        // Insert sample users if they don't exist
        String[] defaultUsers = {
            "INSERT INTO users (username, password, score) SELECT 'user1', 'pass1', 80 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1')",
            "INSERT INTO users (username, password, score) SELECT 'user2', 'pass2', 60 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2')",
            "INSERT INTO users (username, password, score) SELECT 'user3', 'pass3', 40 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user3')",
            "INSERT INTO users (username, password, score) SELECT 'admin', 'admin123', 100 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')"
        };

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            System.out.println("✅ Users table ensured successfully.");
            
            for (String sql : defaultUsers) {
                stmt.execute(sql);
            }
            System.out.println("User tables initialized successfully");
        }
    }

    // ✅ REGISTER USER
    public User register(User user) throws Exception {
        System.out.println("📝 Registration attempt for: " + user.getUsername());
        // Ensure tables exist
        initializeTables();

        String sql = "INSERT INTO users (username, password, score) VALUES (?, ?, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getLong(1));
                        user.setScore(0);
                        user.setPassword(null);
                        return user;
                    }
                }
            }

            throw new Exception("Failed to register user");

        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    // ✅ LOGIN USER
    public User login(String username, String password) throws Exception {
        // Ensure tables exist
        initializeTables();

        System.out.println("Attempting login for user: " + username);

        String sql = "SELECT id, username, score FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setScore(rs.getInt("score"));
                System.out.println("Login successful for user: " + username);
                return user;
            }

            System.out.println("Login failed - user not found or wrong password: " + username);
            return null;

        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    public User getUserById(Long id) throws Exception {
        String sql = "SELECT id, username, email, score FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setScore(rs.getInt("score"));
                return user;
            }

            return null;

        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }
}
package com.ipl.backend.service;

import com.ipl.backend.model.User;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class UserService {

    private final DatabaseInitializer databaseInitializer;

    public UserService(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    public User register(User user) throws Exception {
        databaseInitializer.ensureSchema();

        if (isBlank(user.getUsername()) || isBlank(user.getPassword())) {
            throw new IllegalArgumentException("Username and password are required");
        }

        String sql = "INSERT INTO users (username, password, email, points) VALUES (?, ?, ?, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername().trim());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new Exception("Failed to register user");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }

            user.setUsername(user.getUsername().trim());
            user.setPassword(null);
            user.setPoints(0);
            return user;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw new IllegalArgumentException("Username already exists");
            }
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    public User login(String username, String password) throws Exception {
        databaseInitializer.ensureSchema();

        if (isBlank(username) || isBlank(password)) {
            return null;
        }

        String sql = "SELECT id, username, email, points FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    public User getUserById(Long id) throws Exception {
        databaseInitializer.ensureSchema();

        if (id == null) {
            return null;
        }

        String sql = "SELECT id, username, email, points FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPoints(rs.getInt("points"));
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isDuplicateKey(SQLException e) {
        return "23505".equals(e.getSQLState()) || e.getMessage().toLowerCase().contains("unique");
    }
}

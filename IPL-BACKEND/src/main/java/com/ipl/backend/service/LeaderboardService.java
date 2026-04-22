package com.ipl.backend.service;

import com.ipl.backend.model.User;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    public List<User> getLeaderboard() throws Exception {
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, username, score FROM users ORDER BY score DESC")) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setScore(rs.getInt("score"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
        return users;
    }

    public int getUserRank(Long userId) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) as rank FROM users WHERE score > (SELECT score FROM users WHERE id = ?)")) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("rank") + 1;
            }
            return 0;
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }
}

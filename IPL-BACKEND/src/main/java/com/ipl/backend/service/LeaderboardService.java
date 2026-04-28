package com.ipl.backend.service;

import com.ipl.backend.model.LeaderboardDTO;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    public List<LeaderboardDTO> getLeaderboard() throws Exception {
        List<LeaderboardDTO> leaderboard = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT u.username, COALESCE(SUM(p.points), 0) AS score " +
                             "FROM users u " +
                             "LEFT JOIN predictions p ON u.id = p.user_id " +
                             "GROUP BY u.id, u.username " +
                             "ORDER BY score DESC")) {

            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");
                leaderboard.add(new LeaderboardDTO(username, score));
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
        return leaderboard;
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

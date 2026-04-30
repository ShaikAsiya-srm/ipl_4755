package com.ipl.backend.service;

import com.ipl.backend.model.LeaderboardDTO;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final DatabaseInitializer databaseInitializer;

    public LeaderboardService(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    public List<LeaderboardDTO> getLeaderboard() throws Exception {
        List<LeaderboardDTO> leaderboard = new ArrayList<>();
        databaseInitializer.ensureSchema();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT username, COALESCE(points, 0) AS points " +
                             "FROM users " +
                             "ORDER BY points DESC, username ASC")) {

            while (rs.next()) {
                String username = rs.getString("username");
                int points = rs.getInt("points");
                leaderboard.add(new LeaderboardDTO(username, points));
            }

        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }

        return leaderboard;
    }

    public int getUserRank(Long userId) throws Exception {
        databaseInitializer.ensureSchema();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement userStmt = conn.prepareStatement("SELECT points FROM users WHERE id = ?");
             PreparedStatement rankStmt = conn.prepareStatement(
                     "SELECT COUNT(*) AS rank FROM users WHERE points > ?")) {

            userStmt.setLong(1, userId);
            try (ResultSet userRs = userStmt.executeQuery()) {
                if (!userRs.next()) {
                    return 0;
                }

                rankStmt.setInt(1, userRs.getInt("points"));
                try (ResultSet rankRs = rankStmt.executeQuery()) {
                    if (rankRs.next()) {
                        return rankRs.getInt("rank") + 1;
                    }
                }
            }
            return 0;

        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }
}

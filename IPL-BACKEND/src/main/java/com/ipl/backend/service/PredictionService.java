package com.ipl.backend.service;

import com.ipl.backend.model.Prediction;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    public void initializeTables() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            try (Statement createStmt = conn.createStatement()) {
                createStmt.execute("CREATE TABLE IF NOT EXISTS predictions (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +
                        "match_id BIGINT NOT NULL, " +
                        "toss_winner VARCHAR(50), " +
                        "bat_first VARCHAR(50), " +
                        "player VARCHAR(100), " +
                        "winner VARCHAR(50), " +
                        "top_scorer VARCHAR(100), " +
                        "top_bowler VARCHAR(100), " +
                        "total_sixes VARCHAR(20), " +
                        "total_runs VARCHAR(20), " +
                        "predicted_team VARCHAR(50), " +
                        "points INT DEFAULT 0, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
            }
        }
    }

    public Prediction createPrediction(Prediction prediction) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {

            // Create predictions table if it doesn't exist
            try (Statement createStmt = conn.createStatement()) {
                createStmt.execute("CREATE TABLE IF NOT EXISTS predictions (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +
                        "match_id BIGINT NOT NULL, " +
                        "toss_winner VARCHAR(50), " +
                        "bat_first VARCHAR(50), " +
                        "player VARCHAR(100), " +
                        "winner VARCHAR(50), " +
                        "top_scorer VARCHAR(100), " +
                        "top_bowler VARCHAR(100), " +
                        "total_sixes VARCHAR(20), " +
                        "total_runs VARCHAR(20), " +
                        "predicted_team VARCHAR(50), " +
                        "points INT DEFAULT 0, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "CONSTRAINT fk_match FOREIGN KEY (match_id) REFERENCES matches(id), " +
                        "CONSTRAINT unique_user_match UNIQUE (user_id, match_id))");
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO predictions (user_id, match_id, toss_winner, bat_first, player, winner, top_scorer, top_bowler, total_sixes, total_runs, predicted_team, points) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1, prediction.getUserId());
                stmt.setLong(2, prediction.getMatchId());
                stmt.setString(3, prediction.getTossWinner());
                stmt.setString(4, prediction.getBatFirst());
                stmt.setString(5, prediction.getPlayer());
                stmt.setString(6, prediction.getWinner());
                stmt.setString(7, prediction.getTopScorer());
                stmt.setString(8, prediction.getTopBowler());
                stmt.setString(9, prediction.getTotalSixes());
                stmt.setString(10, prediction.getTotalRuns());
                stmt.setString(11, prediction.getPredictedTeam());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    prediction.setId(rs.getLong(1));
                    // No participation points awarded; points start at 0
                    return prediction;
                }
                throw new Exception("Failed to create prediction");
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    public void updatePredictionPoints(Long predictionId, int points) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE predictions SET points = ? WHERE id = ?")) {
            stmt.setInt(1, points);
            stmt.setLong(2, predictionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    public List<Prediction> getUserPredictions(Long userId) throws Exception {
        List<Prediction> predictions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM predictions WHERE user_id = ?")) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Prediction prediction = mapResultSetToPrediction(rs);
                predictions.add(prediction);
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
        return predictions;
    }

    public List<Prediction> getMatchPredictions(Long matchId) throws Exception {
        List<Prediction> predictions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM predictions WHERE match_id = ?")) {

            stmt.setLong(1, matchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Prediction prediction = mapResultSetToPrediction(rs);
                predictions.add(prediction);
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
        return predictions;
    }

    public Prediction getPredictionById(Long id) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM predictions WHERE id = ?")) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPrediction(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    private Prediction mapResultSetToPrediction(ResultSet rs) throws SQLException {
        Prediction prediction = new Prediction();
        prediction.setId(rs.getLong("id"));
        prediction.setUserId(rs.getLong("user_id"));
        prediction.setMatchId(rs.getLong("match_id"));
        prediction.setTossWinner(rs.getString("toss_winner"));
        prediction.setBatFirst(rs.getString("bat_first"));
        prediction.setPlayer(rs.getString("player"));
        prediction.setWinner(rs.getString("winner"));
        prediction.setTopScorer(rs.getString("top_scorer"));
        prediction.setTopBowler(rs.getString("top_bowler"));
        prediction.setTotalSixes(rs.getString("total_sixes"));
        prediction.setTotalRuns(rs.getString("total_runs"));
        prediction.setPredictedTeam(rs.getString("predicted_team"));
        prediction.setPoints(rs.getInt("points"));
        return prediction;
    }
}

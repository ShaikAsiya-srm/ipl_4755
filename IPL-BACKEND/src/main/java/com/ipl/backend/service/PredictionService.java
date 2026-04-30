package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import com.ipl.backend.model.Prediction;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    private final DatabaseInitializer databaseInitializer;
    private final MatchService matchService;
    private final ScoringService scoringService;

    public PredictionService(DatabaseInitializer databaseInitializer,
                             MatchService matchService,
                             ScoringService scoringService) {
        this.databaseInitializer = databaseInitializer;
        this.matchService = matchService;
        this.scoringService = scoringService;
    }

    public Prediction createPrediction(Prediction prediction) throws Exception {
        databaseInitializer.ensureSchema();
        validatePrediction(prediction);
        normalizePrediction(prediction);

        ensureUserExists(prediction.getUserId());
        ensureMatchExists(prediction.getMatchId());
        ensureNotDuplicate(prediction.getUserId(), prediction.getMatchId());

        String sql = "INSERT INTO predictions " +
                "(user_id, match_id, toss_winner, bat_first, player, winner, " +
                "top_scorer, top_bowler, total_sixes, total_runs, predicted_team, points) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    prediction.setId(rs.getLong(1));
                }
            }

            prediction.setPoints(0);
            return prediction;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw new IllegalArgumentException("Prediction already submitted for this match");
            }
            throw new Exception("Error creating prediction: " + e.getMessage());
        }
    }

    public int evaluatePredictions(Long matchId) throws Exception {
        return evaluatePrediction(matchId, null);
    }

    public int evaluatePrediction(Long matchId, String actualWinner) throws Exception {
        databaseInitializer.ensureSchema();

        Match match = matchService.getMatchById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found");
        }
        if (!isBlank(actualWinner)) {
            match.setActualWinner(actualWinner.trim());
        }

        List<Prediction> predictions = getMatchPredictions(matchId);
        for (Prediction prediction : predictions) {
            int points = scoringService.calculatePoints(prediction, match);
            updatePredictionPoints(prediction.getId(), points);
            recalculateUserPoints(prediction.getUserId());
        }

        return predictions.size();
    }

    public List<Prediction> getUserPredictions(Long userId) throws Exception {
        databaseInitializer.ensureSchema();

        List<Prediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM predictions WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    predictions.add(mapResultSetToPrediction(rs));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching user predictions: " + e.getMessage());
        }

        return predictions;
    }

    public List<Prediction> getMatchPredictions(Long matchId) throws Exception {
        databaseInitializer.ensureSchema();

        List<Prediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM predictions WHERE match_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    predictions.add(mapResultSetToPrediction(rs));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching match predictions: " + e.getMessage());
        }

        return predictions;
    }

    public Prediction getPredictionById(Long id) throws Exception {
        databaseInitializer.ensureSchema();

        String sql = "SELECT * FROM predictions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPrediction(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new Exception("Error fetching prediction: " + e.getMessage());
        }
    }

    public void updatePredictionPoints(Long predictionId, int points) throws Exception {
        String sql = "UPDATE predictions SET points = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, points);
            stmt.setLong(2, predictionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error updating prediction points: " + e.getMessage());
        }
    }

    private void recalculateUserPoints(Long userId) throws Exception {
        String sql = "UPDATE users SET points = (" +
                "SELECT COALESCE(SUM(points), 0) FROM predictions WHERE user_id = ?" +
                ") WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error updating user points: " + e.getMessage());
        }
    }

    private void ensureUserExists(Long userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("User not found");
    }

    private void ensureMatchExists(Long matchId) throws Exception {
        if (matchService.getMatchById(matchId) == null) {
            throw new IllegalArgumentException("Match not found");
        }
    }

    private void ensureNotDuplicate(Long userId, Long matchId) throws Exception {
        String sql = "SELECT COUNT(*) FROM predictions WHERE user_id = ? AND match_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Prediction already submitted for this match");
                }
            }
        }
    }

    private void validatePrediction(Prediction prediction) {
        if (prediction == null) {
            throw new IllegalArgumentException("Prediction body is required");
        }
        if (prediction.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (prediction.getMatchId() == null) {
            throw new IllegalArgumentException("matchId is required");
        }
        if (isBlank(prediction.getWinner()) && isBlank(prediction.getPredictedTeam())) {
            throw new IllegalArgumentException("winner is required");
        }
    }

    private void normalizePrediction(Prediction prediction) {
        if (isBlank(prediction.getWinner()) && !isBlank(prediction.getPredictedTeam())) {
            prediction.setWinner(prediction.getPredictedTeam().trim());
        }
        if (isBlank(prediction.getPredictedTeam()) && !isBlank(prediction.getWinner())) {
            prediction.setPredictedTeam(prediction.getWinner().trim());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isDuplicateKey(SQLException e) {
        return "23505".equals(e.getSQLState()) || e.getMessage().toLowerCase().contains("unique");
    }
}

package com.ipl.backend.service;

import com.ipl.backend.model.Prediction;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    public Prediction createPrediction(Prediction prediction) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO predictions (user_id, match_id, toss_winner, bat_first, player) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, prediction.getUserId());
            stmt.setLong(2, prediction.getMatchId());
            stmt.setString(3, prediction.getTossWinner());
            stmt.setString(4, prediction.getBatFirst());
            stmt.setString(5, prediction.getPlayer());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                prediction.setId(rs.getLong(1));
                return prediction;
            }
            throw new Exception("Failed to create prediction");
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
        return prediction;
    }
}

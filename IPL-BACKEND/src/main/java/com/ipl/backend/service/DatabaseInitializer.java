package com.ipl.backend.service;

import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class DatabaseInitializer {

    private boolean initialized = false;

    public synchronized void ensureSchema() throws SQLException {
        if (initialized) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "points INT DEFAULT 0" +
                    ")");
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100)");
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS points INT DEFAULT 0");

            stmt.execute("CREATE TABLE IF NOT EXISTS matches (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "team1 VARCHAR(50), " +
                    "team2 VARCHAR(50), " +
                    "logo1 VARCHAR(255), " +
                    "logo2 VARCHAR(255), " +
                    "stadium VARCHAR(100), " +
                    "match_date VARCHAR(30), " +
                    "time VARCHAR(20), " +
                    "score1 VARCHAR(50), " +
                    "score2 VARCHAR(50), " +
                    "result VARCHAR(255), " +
                    "is_today BOOLEAN DEFAULT FALSE, " +
                    "actual_winner VARCHAR(50), " +
                    "top_scorer VARCHAR(100), " +
                    "top_bowler VARCHAR(100), " +
                    "actual_total_sixes VARCHAR(20), " +
                    "actual_total_runs VARCHAR(20), " +
                    "toss_winner VARCHAR(50), " +
                    "bat_first VARCHAR(50)" +
                    ")");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS logo1 VARCHAR(255)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS logo2 VARCHAR(255)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS score1 VARCHAR(50)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS score2 VARCHAR(50)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS result VARCHAR(255)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS is_today BOOLEAN DEFAULT FALSE");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS actual_winner VARCHAR(50)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS top_scorer VARCHAR(100)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS top_bowler VARCHAR(100)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS actual_total_sixes VARCHAR(20)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS actual_total_runs VARCHAR(20)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS toss_winner VARCHAR(50)");
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS bat_first VARCHAR(50)");

            stmt.execute("CREATE TABLE IF NOT EXISTS predictions (" +
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
                    "CONSTRAINT unique_user_match UNIQUE (user_id, match_id)" +
                    ")");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS toss_winner VARCHAR(50)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS bat_first VARCHAR(50)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS player VARCHAR(100)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS winner VARCHAR(50)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS top_scorer VARCHAR(100)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS top_bowler VARCHAR(100)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS total_sixes VARCHAR(20)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS total_runs VARCHAR(20)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS predicted_team VARCHAR(50)");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS points INT DEFAULT 0");
            stmt.execute("ALTER TABLE predictions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

            seedUsers(stmt);
            seedMatches(stmt);
            initialized = true;
        }
    }

    private void seedUsers(Statement stmt) throws SQLException {
        if (tableHasRows(stmt, "users")) {
            return;
        }

        stmt.execute("INSERT INTO users (username, password, points) " +
                "VALUES ('user1', 'pass1', 80)");
        stmt.execute("INSERT INTO users (username, password, points) " +
                "VALUES ('user2', 'pass2', 60)");
        stmt.execute("INSERT INTO users (username, password, points) " +
                "VALUES ('user3', 'pass3', 40)");
        stmt.execute("INSERT INTO users (username, password, points) " +
                "VALUES ('admin', 'admin123', 100)");
        stmt.execute("INSERT INTO users (username, password, points) " +
                "VALUES ('IPLFan', 'password123', 120)");
    }

    private void seedMatches(Statement stmt) throws SQLException {
        if (tableHasRows(stmt, "matches")) {
            return;
        }

        stmt.execute("INSERT INTO matches (team1, team2, stadium, match_date, time, result, is_today, " +
                "actual_winner, toss_winner, bat_first) " +
                "VALUES ('CSK', 'MI', 'Chennai', '2026-04-30', '7:30 PM', 'Scheduled', TRUE, " +
                "NULL, NULL, NULL)");
    }

    private boolean tableHasRows(Statement stmt, String tableName) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}

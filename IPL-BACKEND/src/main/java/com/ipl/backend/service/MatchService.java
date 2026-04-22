package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchService {

    // =========================
    // TABLE INITIALIZATION
    // =========================
    private void initializeTables() throws SQLException {

        String createMatchesTable =
                "CREATE TABLE IF NOT EXISTS matches (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "team1 VARCHAR(50) NOT NULL, " +
                        "team2 VARCHAR(50) NOT NULL, " +
                        "logo1 VARCHAR(255), " +
                        "logo2 VARCHAR(255), " +
                        "stadium VARCHAR(100), " +
                        "match_date VARCHAR(30), " +
                        "time VARCHAR(20), " +
                        "score1 VARCHAR(50), " +
                        "score2 VARCHAR(50), " +
                        "result VARCHAR(255), " +
                        "is_today BOOLEAN DEFAULT FALSE)";

        String createUsersTable =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "score INT DEFAULT 0, " +
                        "email VARCHAR(100))";

        String createPredictionsTable =
                "CREATE TABLE IF NOT EXISTS predictions (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +
                        "match_id BIGINT NOT NULL, " +
                        "toss_winner VARCHAR(50), " +
                        "bat_first VARCHAR(50), " +
                        "player VARCHAR(100), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id), " +
                        "FOREIGN KEY (match_id) REFERENCES matches(id))";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createMatchesTable);
            stmt.execute(createUsersTable);
            stmt.execute(createPredictionsTable);

            // default users
            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'user1', 'pass1', 80 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='user1')");

            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'user2', 'pass2', 60 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='user2')");

            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'user3', 'pass3', 40 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='user3')");

            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'admin', 'admin123', 100 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin')");

        }
    }

    // =========================
    // MAP RESULTSET → MATCH
    // =========================
    private Match mapMatch(ResultSet rs) throws SQLException {
        Match match = new Match();
        match.setId(rs.getLong("id"));
        match.setTeam1(rs.getString("team1"));
        match.setTeam2(rs.getString("team2"));
        match.setLogo1(rs.getString("logo1"));
        match.setLogo2(rs.getString("logo2"));
        match.setStadium(rs.getString("stadium"));
        match.setTime(rs.getString("time"));
        match.setDate(rs.getString("match_date"));
        match.setScore1(rs.getString("score1"));
        match.setScore2(rs.getString("score2"));
        match.setResult(rs.getString("result"));
        match.setIsToday(rs.getBoolean("is_today"));
        return match;
    }

    // =========================
    // GET ALL MATCHES
    // =========================
    public List<Match> getAllMatches() throws Exception {
        initializeTables();
        insertDefaultMatches();

        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM matches ORDER BY match_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                matches.add(mapMatch(rs));
            }
        }
        return matches;
    }

    // =========================
    // GET MATCH BY ID
    // =========================
    public Match getMatchById(Long id) throws Exception {
        initializeTables();

        String sql = "SELECT * FROM matches WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMatch(rs);
                }
            }
        }

        return null;
    }

    // =========================
    // UPSERT MATCH (FIXED)
    // =========================
    public void upsertMatch(Match match) throws Exception {
        initializeTables();

        String selectSql =
                "SELECT id FROM matches WHERE team1=? AND team2=? AND match_date=?";

        String updateSql =
                "UPDATE matches SET logo1=?, logo2=?, stadium=?, match_date=?, time=?, " +
                        "score1=?, score2=?, result=?, is_today=? WHERE id=?";

        String insertSql =
                "INSERT INTO matches (team1, team2, logo1, logo2, stadium, match_date, time, score1, score2, result, is_today) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            Long existingId = null;

            // CHECK EXISTING
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, match.getTeam1());
                ps.setString(2, match.getTeam2());
                ps.setString(3, match.getDate());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getLong("id");
                    }
                }
            }

            // UPDATE
            if (existingId != null) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {

                    ps.setString(1, match.getLogo1());
                    ps.setString(2, match.getLogo2());
                    ps.setString(3, match.getStadium());
                    ps.setString(4, match.getDate());
                    ps.setString(5, match.getTime());
                    ps.setString(6, match.getScore1());
                    ps.setString(7, match.getScore2());
                    ps.setString(8, match.getResult());
                    ps.setBoolean(9, match.getIsToday());
                    ps.setLong(10, existingId);

                    ps.executeUpdate();
                }
                return;
            }

            // INSERT
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                ps.setString(1, match.getTeam1());
                ps.setString(2, match.getTeam2());
                ps.setString(3, match.getLogo1());
                ps.setString(4, match.getLogo2());
                ps.setString(5, match.getStadium());
                ps.setString(6, match.getDate());
                ps.setString(7, match.getTime());
                ps.setString(8, match.getScore1());
                ps.setString(9, match.getScore2());
                ps.setString(10, match.getResult());
                ps.setBoolean(11, match.getIsToday());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new Exception("Upsert error: " + e.getMessage());
        }
    }

    // =========================
    // GET TODAY MATCH
    // =========================
    public Match getTodayMatch() throws Exception {
        initializeTables();

        String sql = "SELECT * FROM matches WHERE is_today = true LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapMatch(rs);
            }
        }

        return null;
    }

    // =========================
    // SEED DATA
    // =========================
    private void insertDefaultMatches() throws Exception {

        List<Match> defaultMatches = new ArrayList<>();

        Match m1 = new Match();
        m1.setTeam1("SRH");
        m1.setTeam2("DC");
        m1.setStadium("Hyderabad Stadium");
        m1.setTime("7:30 PM");
        m1.setDate("21 Apr 2026");
        m1.setScore1("242/2 (20)");
        m1.setScore2("195/9 (20)");
        m1.setResult("SRH Won By 47 Runs");
        m1.setIsToday(false);
        defaultMatches.add(m1);

        Match m2 = new Match();
        m2.setTeam1("MI");
        m2.setTeam2("GT");
        m2.setStadium("Ahmedabad Stadium");
        m2.setTime("7:30 PM");
        m2.setDate("20 Apr 2026");
        m2.setScore1("199/5 (20)");
        m2.setScore2("100/10 (15.5)");
        m2.setResult("MI Won By 99 Runs");
        m2.setIsToday(false);
        defaultMatches.add(m2);

        Match m3 = new Match();
        m3.setTeam1("PBKS");
        m3.setTeam2("LSG");
        m3.setStadium("Mullanpur Stadium");
        m3.setTime("7:30 PM");
        m3.setDate("19 Apr 2026");
        m3.setScore1("254/7 (20)");
        m3.setScore2("200/5 (20)");
        m3.setResult("PBKS Won By 54 Runs");
        m3.setIsToday(false);
        defaultMatches.add(m3);

        Match m4 = new Match();
        m4.setTeam1("RR");
        m4.setTeam2("KKR");
        m4.setStadium("Eden Gardens");
        m4.setTime("3:30 PM");
        m4.setDate("19 Apr 2026");
        m4.setScore1("155/9 (20)");
        m4.setScore2("161/6 (19.4)");
        m4.setResult("KKR Won By 4 Wickets");
        m4.setIsToday(false);
        defaultMatches.add(m4);

        Match m5 = new Match();
        m5.setTeam1("CSK");
        m5.setTeam2("RCB");
        m5.setStadium("M. Chinnaswamy Stadium");
        m5.setTime("7:30 PM");
        m5.setDate("24 Apr 2026");
        m5.setScore1("-");
        m5.setScore2("-");
        m5.setResult("Upcoming");
        m5.setIsToday(false);
        defaultMatches.add(m5);

        Match m6 = new Match();
        m6.setTeam1("GT");
        m6.setTeam2("DC");
        m6.setStadium("Narendra Modi Stadium");
        m6.setTime("3:30 PM");
        m6.setDate("25 Apr 2026");
        m6.setScore1("-");
        m6.setScore2("-");
        m6.setResult("Upcoming");
        m6.setIsToday(false);
        defaultMatches.add(m6);

        Match m7 = new Match();
        m7.setTeam1("MI");
        m7.setTeam2("SRH");
        m7.setStadium("Wankhede Stadium");
        m7.setTime("7:30 PM");
        m7.setDate("26 Apr 2026");
        m7.setScore1("-");
        m7.setScore2("-");
        m7.setResult("Upcoming");
        m7.setIsToday(true);
        defaultMatches.add(m7);

        Match m8 = new Match();
        m8.setTeam1("LSG");
        m8.setTeam2("RR");
        m8.setStadium("Ekana Stadium, Lucknow");
        m8.setTime("7:30 PM");
        m8.setDate("22 Apr 2026");
        m8.setScore1("-");
        m8.setScore2("-");
        m8.setResult("Upcoming");
        m8.setIsToday(false);
        defaultMatches.add(m8);

        Match m9 = new Match();
        m9.setTeam1("KKR");
        m9.setTeam2("DC");
        m9.setStadium("Eden Gardens");
        m9.setTime("7:30 PM");
        m9.setDate("24 Apr 2026");
        m9.setScore1("-");
        m9.setScore2("-");
        m9.setResult("Upcoming");
        m9.setIsToday(false);
        defaultMatches.add(m9);

        Match m10 = new Match();
        m10.setTeam1("PBKS");
        m10.setTeam2("CSK");
        m10.setStadium("Mullanpur Stadium");
        m10.setTime("7:30 PM");
        m10.setDate("27 Apr 2026");
        m10.setScore1("-");
        m10.setScore2("-");
        m10.setResult("Upcoming");
        m10.setIsToday(false);
        defaultMatches.add(m10);

        for (Match m : defaultMatches) {
            upsertMatch(m);
        }
    }
}

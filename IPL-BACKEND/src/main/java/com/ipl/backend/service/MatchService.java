package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import com.ipl.backend.model.Prediction;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MatchService {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter D_MMM_YYYY =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d MMM uuuu")
                    .toFormatter(Locale.ENGLISH);

    private final PredictionService predictionService;
    private final ScoringService scoringService;

    public MatchService(PredictionService predictionService, ScoringService scoringService) {
        this.predictionService = predictionService;
        this.scoringService = scoringService;
    }

    private String normalizeDateToIso(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        try {
            return LocalDate.parse(s, ISO_DATE).format(ISO_DATE);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(s, D_MMM_YYYY).format(ISO_DATE);
        } catch (DateTimeParseException ignored) {
        }

        return s;
    }

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
                        "is_today BOOLEAN DEFAULT FALSE, " +
                        "actual_winner VARCHAR(50), " +
                        "top_scorer VARCHAR(100), " +
                        "top_bowler VARCHAR(100), " +
                        "actual_total_sixes VARCHAR(20), " +
                        "actual_total_runs VARCHAR(20), " +
                        "toss_winner VARCHAR(50), " +
                        "bat_first VARCHAR(50))";

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
                        "CONSTRAINT unique_user_match UNIQUE (user_id, match_id))";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createMatchesTable);
            stmt.execute(createUsersTable);
            stmt.execute(createPredictionsTable);

            // Add new columns to matches table if they don't exist (for existing DBs)
            addMissingMatchColumns(conn);

            // default users
            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'user1', 'pass1', 0 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='user1')");

            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'user2', 'pass2', 0 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='user2')");

            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'user3', 'pass3', 0 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='user3')");

            stmt.execute("INSERT INTO users (username, password, score) " +
                    "SELECT 'admin', 'admin123', 0 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin')");

        }
    }

    private void addMissingMatchColumns(Connection conn) throws SQLException {
        String[] columns = {
            "ACTUAL_WINNER VARCHAR(50)",
            "TOP_SCORER VARCHAR(100)",
            "TOP_BOWLER VARCHAR(100)",
            "ACTUAL_TOTAL_SIXES VARCHAR(20)",
            "ACTUAL_TOTAL_RUNS VARCHAR(20)",
            "TOSS_WINNER VARCHAR(50)",
            "BAT_FIRST VARCHAR(50)"
        };

        for (String columnDef : columns) {
            String columnName = columnDef.split(" ")[0];
            // H2 stores metadata in uppercase by default
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'MATCHES' AND COLUMN_NAME = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, columnName);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    String alterSql = "ALTER TABLE matches ADD COLUMN " + columnDef;
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(alterSql);
                    }
                }
            }
        }
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
        match.setDate(normalizeDateToIso(rs.getString("match_date")));
        match.setScore1(rs.getString("score1"));
        match.setScore2(rs.getString("score2"));
        match.setResult(rs.getString("result"));
        match.setIsToday(rs.getBoolean("is_today"));
        match.setActualWinner(rs.getString("actual_winner"));
        match.setTopScorer(rs.getString("top_scorer"));
        match.setTopBowler(rs.getString("top_bowler"));
        match.setActualTotalSixes(rs.getString("actual_total_sixes"));
        match.setActualTotalRuns(rs.getString("actual_total_runs"));
        match.setTossWinner(rs.getString("toss_winner"));
        match.setBatFirst(rs.getString("bat_first"));
        return match;
    }

    // =========================
    // GET ALL MATCHES
    // =========================
    public List<Match> getAllMatches() throws Exception {
        initializeTables();
        insertDefaultMatchesIfEmpty();

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

        match.setDate(normalizeDateToIso(match.getDate()));

        String selectSql =
                "SELECT id FROM matches WHERE team1=? AND team2=? AND match_date=?";

        String updateSql =
                "UPDATE matches SET logo1=?, logo2=?, stadium=?, match_date=?, time=?, " +
                        "score1=?, score2=?, result=?, is_today=?, " +
                        "actual_winner=?, top_scorer=?, top_bowler=?, " +
                        "actual_total_sixes=?, actual_total_runs=?, toss_winner=?, bat_first=? " +
                        "WHERE id=?";

        String insertSql =
                "INSERT INTO matches (team1, team2, logo1, logo2, stadium, match_date, time, " +
                        "score1, score2, result, is_today, actual_winner, top_scorer, top_bowler, " +
                        "actual_total_sixes, actual_total_runs, toss_winner, bat_first) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                    ps.setString(10, match.getActualWinner());
                    ps.setString(11, match.getTopScorer());
                    ps.setString(12, match.getTopBowler());
                    ps.setString(13, match.getActualTotalSixes());
                    ps.setString(14, match.getActualTotalRuns());
                    ps.setString(15, match.getTossWinner());
                    ps.setString(16, match.getBatFirst());
                    ps.setLong(17, existingId);

                    ps.executeUpdate();
                }
                // Trigger scoring if match has actual results
                if (isMatchCompleted(match)) {
                    recalculateMatchScores(conn, existingId);
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
                ps.setString(12, match.getActualWinner());
                ps.setString(13, match.getTopScorer());
                ps.setString(14, match.getTopBowler());
                ps.setString(15, match.getActualTotalSixes());
                ps.setString(16, match.getActualTotalRuns());
                ps.setString(17, match.getTossWinner());
                ps.setString(18, match.getBatFirst());

                ps.executeUpdate();

                // Get the inserted ID
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                    if (rs.next()) {
                        Long insertedId = rs.getLong(1);
                        // Trigger scoring if match has actual results
                        if (isMatchCompleted(match)) {
                            recalculateMatchScores(conn, insertedId);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new Exception("Upsert error: " + e.getMessage());
        }
    }

    private boolean isMatchCompleted(Match match) {
        // Match is considered completed if actualWinner is set (or result is not "Upcoming")
        String result = match.getResult();
        if (result != null && !result.equalsIgnoreCase("Upcoming") && !result.equals("-")) {
            return true;
        }
        // Also check if key result fields are filled
        return match.getActualWinner() != null && !match.getActualWinner().isEmpty();
    }

    private void recalculateMatchScores(Long matchId) throws Exception {
        Match match = getMatchById(matchId);
        if (match == null) return;

        // Only recalc if match is completed
        if (!isMatchCompleted(match)) return;

        List<Prediction> predictions = predictionService.getMatchPredictions(matchId);
        Set<Long> affectedUserIds = new HashSet<>();

        for (Prediction pred : predictions) {
            int points = scoringService.calculatePoints(pred, match);
            predictionService.updatePredictionPoints(pred.getId(), points);
            affectedUserIds.add(pred.getUserId());
        }

        // Recalculate total score for each affected user
        for (Long userId : affectedUserIds) {
            recalculateUserScore(userId);
        }
    }

    private void recalculateUserScore(Long userId) throws Exception {
        int total = 0;
        String sql = "SELECT COALESCE(SUM(points), 0) AS total FROM predictions WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET score = ? WHERE id = ?")) {
            ps.setInt(1, total);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    // =========================
    // GET TODAY MATCH
    // =========================
    public Match getTodayMatch() throws Exception {
        initializeTables();

        insertDefaultMatchesIfEmpty();

        String todayIso = LocalDate.now(IST).format(ISO_DATE);
        refreshTodayFlags(todayIso);

        String sql = "SELECT * FROM matches WHERE match_date = ? ORDER BY time LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, todayIso);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMatch(rs);
                }
            }
        }

        return null;
    }

    private void refreshTodayFlags(String todayIso) throws SQLException {
        String clearSql = "UPDATE matches SET is_today = false";
        String setSql = "UPDATE matches SET is_today = true WHERE match_date = ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(clearSql)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(setSql)) {
                ps.setString(1, todayIso);
                ps.executeUpdate();
            }
        }
    }

    // =========================
    // SEED DATA
    // =========================
    private void insertDefaultMatchesIfEmpty() throws Exception {
        if (!isMatchesTableEmpty()) return;

        List<Match> defaultMatches = new ArrayList<>();

        Match m1 = new Match();
        m1.setTeam1("Sunrisers Hyderabad");
        m1.setTeam2("Delhi Capitals");
        m1.setStadium("Hyderabad Stadium");
        m1.setTime("7:30 PM");
        m1.setDate("21 Apr 2026");
        m1.setScore1("242/2 (20)");
        m1.setScore2("195/9 (20)");
        m1.setResult("Sunrisers Hyderabad Won By 47 Runs");
        m1.setIsToday(false);
        defaultMatches.add(m1);

        Match m2 = new Match();
        m2.setTeam1("Mumbai Indians");
        m2.setTeam2("Gujarat Titans");
        m2.setStadium("Ahmedabad Stadium");
        m2.setTime("7:30 PM");
        m2.setDate("20 Apr 2026");
        m2.setScore1("199/5 (20)");
        m2.setScore2("100/10 (15.5)");
        m2.setResult("Mumbai Indians Won By 99 Runs");
        m2.setIsToday(false);
        defaultMatches.add(m2);

        Match m3 = new Match();
        m3.setTeam1("Punjab Kings");
        m3.setTeam2("Lucknow Super Giants");
        m3.setStadium("Mullanpur Stadium");
        m3.setTime("7:30 PM");
        m3.setDate("19 Apr 2026");
        m3.setScore1("254/7 (20)");
        m3.setScore2("200/5 (20)");
        m3.setResult("Punjab Kings Won By 54 Runs");
        m3.setIsToday(false);
        defaultMatches.add(m3);

        Match m4 = new Match();
        m4.setTeam1("Rajasthan Royals");
        m4.setTeam2("Kolkata Knight Riders");
        m4.setStadium("Eden Gardens");
        m4.setTime("3:30 PM");
        m4.setDate("19 Apr 2026");
        m4.setScore1("155/9 (20)");
        m4.setScore2("161/6 (19.4)");
        m4.setResult("Kolkata Knight Riders Won By 4 Wickets");
        m4.setIsToday(false);
        defaultMatches.add(m4);

        Match m5 = new Match();
        m5.setTeam1("Chennai Super Kings");
        m5.setTeam2("Royal Challengers Bangalore");
        m5.setStadium("M. Chinnaswamy Stadium");
        m5.setTime("7:30 PM");
        m5.setDate("24 Apr 2026");
        m5.setScore1("-");
        m5.setScore2("-");
        m5.setResult("Upcoming");
        m5.setIsToday(false);
        defaultMatches.add(m5);

        Match m6 = new Match();
        m6.setTeam1("Gujarat Titans");
        m6.setTeam2("Delhi Capitals");
        m6.setStadium("Narendra Modi Stadium");
        m6.setTime("3:30 PM");
        m6.setDate("25 Apr 2026");
        m6.setScore1("-");
        m6.setScore2("-");
        m6.setResult("Upcoming");
        m6.setIsToday(false);
        defaultMatches.add(m6);

        Match m7 = new Match();
        m7.setTeam1("Mumbai Indians");
        m7.setTeam2("Sunrisers Hyderabad");
        m7.setStadium("Wankhede Stadium");
        m7.setTime("7:30 PM");
        m7.setDate("26 Apr 2026");
        m7.setScore1("-");
        m7.setScore2("-");
        m7.setResult("Upcoming");
        m7.setIsToday(false);
        defaultMatches.add(m7);

        Match m8 = new Match();
        m8.setTeam1("Lucknow Super Giants");
        m8.setTeam2("Rajasthan Royals");
        m8.setStadium("Ekana Stadium, Lucknow");
        m8.setTime("7:30 PM");
        m8.setDate("22 Apr 2026");
        m8.setScore1("-");
        m8.setScore2("-");
        m8.setResult("Upcoming");
        m8.setIsToday(false);
        defaultMatches.add(m8);

        Match m9 = new Match();
        m9.setTeam1("Kolkata Knight Riders");
        m9.setTeam2("Delhi Capitals");
        m9.setStadium("Eden Gardens");
        m9.setTime("7:30 PM");
        m9.setDate("24 Apr 2026");
        m9.setScore1("-");
        m9.setScore2("-");
        m9.setResult("Upcoming");
        m9.setIsToday(false);
        defaultMatches.add(m9);

        Match m10 = new Match();
        m10.setTeam1("Punjab Kings");
        m10.setTeam2("Chennai Super Kings");
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

    private boolean isMatchesTableEmpty() throws SQLException {
        String sql = "SELECT COUNT(1) AS c FROM matches";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("c") == 0;
            }
        }
        return true;
    }
}

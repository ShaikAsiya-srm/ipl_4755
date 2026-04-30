package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import com.ipl.backend.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MatchService {

    // ✅ CACHE (correct place inside class)
    private List<Match> cachedMatches;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter D_MMM_YYYY =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d MMM uuuu")
                    .toFormatter(Locale.ENGLISH);

    private final DatabaseInitializer databaseInitializer;

    public MatchService(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    // =========================
    // DATE NORMALIZATION
    // =========================
    private String normalizeDateToIso(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        try {
            return LocalDate.parse(s, ISO_DATE).format(ISO_DATE);
        } catch (DateTimeParseException ignored) {}

        try {
            return LocalDate.parse(s, D_MMM_YYYY).format(ISO_DATE);
        } catch (DateTimeParseException ignored) {}

        return s;
    }

    // =========================
    // TABLE INIT
    // =========================
    private void initializeTables() throws SQLException {
        databaseInitializer.ensureSchema();
    }

    // =========================
    // MAP RESULTSET
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
    // GET ALL MATCHES (CACHED)
    // =========================
    public List<Match> getAllMatches() throws Exception {

        initializeTables();
        insertDefaultMatchesIfEmpty();

        // ✅ RETURN CACHE IF AVAILABLE
        if (cachedMatches != null) {
            return cachedMatches;
        }

        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM matches ORDER BY match_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                matches.add(mapMatch(rs));
            }
        }

        // ✅ STORE IN CACHE
        cachedMatches = matches;

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
    // UPSERT MATCH
    // =========================
    public void upsertMatch(Match match) throws Exception {

        initializeTables();
        match.setDate(normalizeDateToIso(match.getDate()));

        String sql =
                "INSERT INTO matches (team1, team2, logo1, logo2, stadium, match_date, time, " +
                        "score1, score2, result, is_today, actual_winner, top_scorer, top_bowler, " +
                        "actual_total_sixes, actual_total_runs, toss_winner, bat_first) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
        }

        // ✅ CLEAR CACHE AFTER UPDATE (IMPORTANT)
        cachedMatches = null;
    }

    // =========================
    // TODAY MATCH
    // =========================
    public Match getTodayMatch() throws Exception {

        initializeTables();

        String today = LocalDate.now(IST).format(ISO_DATE);

        String sql = "SELECT * FROM matches WHERE match_date = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, today);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMatch(rs);
                }
            }
        }

        return null;
    }

    // =========================
    // DEFAULT MATCHES (SEED)
    // =========================
    private void insertDefaultMatchesIfEmpty() throws Exception {

        String countSql = "SELECT COUNT(*) FROM matches";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        Match m = new Match();
        m.setTeam1("CSK");
        m.setTeam2("MI");
        m.setStadium("Chennai");
        m.setTime("7:30 PM");
        m.setDate("2026-04-29");
        m.setResult("Upcoming");

        upsertMatch(m);
    }
}

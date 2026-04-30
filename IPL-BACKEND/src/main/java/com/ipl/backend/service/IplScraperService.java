package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class IplScraperService {

    private static final String FEED_URL = "https://ipl-stats-sports-mechanic.s3.ap-south-1.amazonaws.com/ipl/feeds/284-matchschedule.js";
    private final MatchService matchService;

    public IplScraperService(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Fetches match data from the official IPL feed and returns a list of Match objects.
     */
     public List<Match> fetchMatches() throws Exception {
         // Fetch the JSONP response with extended timeout for large feed (~750KB)
         String jsonp = Jsoup.connect(FEED_URL)
                 .ignoreContentType(true)
                 .timeout(60000) // 60 seconds
                 .maxBodySize(0) // unlimited
                 .execute()
                 .body();

        // Strip JSONP callback: MatchSchedule({ ... });
        int start = jsonp.indexOf('{');
        int end = jsonp.lastIndexOf('}');
        if (start == -1 || end == -1) {
            throw new IllegalArgumentException("Invalid response from IPL feed");
        }
        String json = jsonp.substring(start, end + 1);

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray matchSummaries = root.getAsJsonArray("Matchsummary");

        List<Match> matches = new ArrayList<>();
        for (JsonElement element : matchSummaries) {
            JsonObject obj = element.getAsJsonObject();

            Match match = new Match();
            match.setTeam1(obj.get("FirstBattingTeamName").getAsString());
            match.setTeam2(obj.get("SecondBattingTeamName").getAsString());
            match.setLogo1(obj.get("HomeTeamLogo").getAsString());
            match.setLogo2(obj.get("AwayTeamLogo").getAsString());
            match.setStadium(obj.get("GroundName").getAsString());
            match.setTime(obj.get("MatchTime").getAsString());
            match.setDate(obj.get("MatchDate").getAsString());

            // Determine if match is today (use IST timezone)
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            String today = istNow.toLocalDate().toString(); // YYYY-MM-DD in IST
            match.setIsToday(match.getDate() != null && match.getDate().equals(today));

            matches.add(match);
        }

        return matches;
    }

    /**
     * Syncs the database with the latest match data from the feed.
     */
    public void syncMatches() throws Exception {
        List<Match> matches = fetchMatches();
        for (Match match : matches) {
            matchService.upsertMatch(match);
        }
    }

     // Returns raw feed data - helpful for bypassing CORS
     public String fetchRawFeed() throws Exception {
         String jsonp = Jsoup.connect(FEED_URL)
                 .ignoreContentType(true)
                 .timeout(60000) // 60 seconds
                 .maxBodySize(0) // unlimited
                 .execute()
                 .body();
         return jsonp;
     }
}

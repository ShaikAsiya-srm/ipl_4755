package com.ipl.backend.controller;

import com.ipl.backend.model.Match;
import com.ipl.backend.service.IplScraperService;
import com.ipl.backend.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class MatchController {

    private final IplScraperService scraper;
    private final MatchService matchService;

    public MatchController(IplScraperService scraper, MatchService matchService) {
        this.scraper = scraper;
        this.matchService = matchService;
    }

    @GetMapping("/matches")
    public List<Match> getMatches() throws Exception {
        return matchService.getAllMatches();
    }

    @GetMapping("/matches/today")
    public Match getTodayMatch() throws Exception {
        return matchService.getTodayMatch();
    }

    @GetMapping("/matches/sync")
    public Map<String, String> syncMatches() {

        new Thread(() -> {
            try {
                scraper.syncMatches();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Sync started in background 🚀");
        return response;
    }

    @GetMapping("/matches/live")
    public String getLiveMatches() throws Exception {
        return scraper.fetchRawFeed();
    }

    @GetMapping("/ping")
    public String ping() {
        return "Backend is working 🚀";
    }
}

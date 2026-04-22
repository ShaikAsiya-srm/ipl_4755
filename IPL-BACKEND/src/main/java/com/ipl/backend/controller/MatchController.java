package com.ipl.backend.controller;

import com.ipl.backend.model.Match;
import com.ipl.backend.service.IplScraperService;
import com.ipl.backend.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3001")
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
    public List<Match> syncMatches() throws Exception {
        scraper.syncMatches();
        return matchService.getAllMatches();
    }

    // New endpoint - fetches directly from IPL feed (bypasses CORS)
    @GetMapping("/matches/live")
    public String getLiveMatches() throws Exception {
        return scraper.fetchRawFeed();
    }
}

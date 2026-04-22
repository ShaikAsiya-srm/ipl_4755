package com.ipl.backend.controller;

import com.ipl.backend.model.User;
import com.ipl.backend.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "http://localhost:3001")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<?> getLeaderboard() {
        try {
            List<User> leaderboard = leaderboardService.getLeaderboard();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<?> getUserRank(@PathVariable Long userId) {
        try {
            int rank = leaderboardService.getUserRank(userId);
            Map<String, Integer> response = new HashMap<>();
            response.put("rank", rank);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}

package com.ipl.backend.controller;

import com.ipl.backend.model.LeaderboardDTO;
import com.ipl.backend.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<?> getLeaderboard() {
        try {
            List<LeaderboardDTO> leaderboard = leaderboardService.getLeaderboard();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            e.printStackTrace();  // 👈 to see real error in console
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
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
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

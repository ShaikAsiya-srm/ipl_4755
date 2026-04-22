package com.ipl.backend.controller;

import com.ipl.backend.model.Prediction;
import com.ipl.backend.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "http://localhost:3001")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping
    public ResponseEntity<?> createPrediction(@RequestBody Prediction prediction) {
        try {
            Prediction created = predictionService.createPrediction(prediction);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPredictions(@PathVariable Long userId) {
        try {
            List<Prediction> predictions = predictionService.getUserPredictions(userId);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMatchPredictions(@PathVariable Long matchId) {
        try {
            List<Prediction> predictions = predictionService.getMatchPredictions(matchId);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPredictionById(@PathVariable Long id) {
        try {
            Prediction prediction = predictionService.getPredictionById(id);
            if (prediction != null) {
                return ResponseEntity.ok(prediction);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>());
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}

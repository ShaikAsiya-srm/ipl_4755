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
@RequestMapping({"/api/predictions", "/api/predict"})
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping
    public ResponseEntity<?> createPrediction(@RequestBody Prediction prediction) {
        try {
            Prediction created = predictionService.createPrediction(prediction);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @PostMapping("/evaluate/{matchId}")
    public ResponseEntity<?> evaluatePredictions(@PathVariable Long matchId,
                                                 @RequestBody(required = false) Map<String, String> result) {
        try {
            String actualWinner = result == null ? null : result.get("actualWinner");
            int evaluated = predictionService.evaluatePrediction(matchId, actualWinner);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Predictions evaluated successfully");
            response.put("evaluated", evaluated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPredictions(@PathVariable Long userId) {
        try {
            List<Prediction> predictions = predictionService.getUserPredictions(userId);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMatchPredictions(@PathVariable Long matchId) {
        try {
            List<Prediction> predictions = predictionService.getMatchPredictions(matchId);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPredictionById(@PathVariable Long id) {
        try {
            Prediction prediction = predictionService.getPredictionById(id);
            if (prediction != null) {
                return ResponseEntity.ok(prediction);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Prediction not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    private Map<String, String> error(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

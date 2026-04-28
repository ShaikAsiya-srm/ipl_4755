package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import com.ipl.backend.model.Prediction;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    public int calculatePoints(Prediction prediction, Match match) {
        int points = 0;

        // Correct match winner: +20
        if (prediction.getWinner() != null && prediction.getWinner().equalsIgnoreCase(match.getActualWinner())) {
            points += 20;
        }

        // Correct toss winner: +5
        if (prediction.getTossWinner() != null && prediction.getTossWinner().equalsIgnoreCase(match.getTossWinner())) {
            points += 5;
        }

        // Correct bat first: +5
        if (prediction.getBatFirst() != null && prediction.getBatFirst().equalsIgnoreCase(match.getBatFirst())) {
            points += 5;
        }

        // Correct top scorer: +15
        if (prediction.getTopScorer() != null && prediction.getTopScorer().equalsIgnoreCase(match.getTopScorer())) {
            points += 15;
        }

        // Correct top bowler: +15
        if (prediction.getTopBowler() != null && prediction.getTopBowler().equalsIgnoreCase(match.getTopBowler())) {
            points += 15;
        }

        // Correct total sixes range: +10
        if (prediction.getTotalSixes() != null && match.getActualTotalSixes() != null) {
            try {
                int predictedSixes = Integer.parseInt(prediction.getTotalSixes().trim());
                int actualSixes = Integer.parseInt(match.getActualTotalSixes().trim());
                if (Math.abs(predictedSixes - actualSixes) <= 2) {
                    points += 10;
                }
            } catch (NumberFormatException e) {
                // If parsing fails, no points
            }
        }

        // Correct total runs range: +10
        if (prediction.getTotalRuns() != null && match.getActualTotalRuns() != null) {
            try {
                int predictedRuns = Integer.parseInt(prediction.getTotalRuns().trim());
                int actualRuns = Integer.parseInt(match.getActualTotalRuns().trim());
                if (Math.abs(predictedRuns - actualRuns) <= 10) {
                    points += 10;
                }
            } catch (NumberFormatException e) {
                // If parsing fails, no points
            }
        }

        return points;
    }
}

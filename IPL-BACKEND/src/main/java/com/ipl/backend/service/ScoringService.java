package com.ipl.backend.service;

import com.ipl.backend.model.Match;
import com.ipl.backend.model.Prediction;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    /**
     * Calculate points earned for a prediction based on actual match results.
     * Maximum possible: 80 points
     */
    public int calculatePoints(Prediction prediction, Match match) {
        if (match == null || prediction == null) {
            return 0;
        }

        int points = 0;

        // 1. Match winner (20 points)
        if (prediction.getWinner() != null && 
            prediction.getWinner().equalsIgnoreCase(match.getActualWinner())) {
            points += 20;
        }

        // 2. Toss winner (5 points)
        if (prediction.getTossWinner() != null && 
            prediction.getTossWinner().equalsIgnoreCase(match.getTossWinner())) {
            points += 5;
        }

        // 3. Bat first (5 points)
        if (prediction.getBatFirst() != null && 
            prediction.getBatFirst().equalsIgnoreCase(match.getBatFirst())) {
            points += 5;
        }

        // 4. Top scorer (15 points)
        if (prediction.getTopScorer() != null && 
            prediction.getTopScorer().equalsIgnoreCase(match.getTopScorer())) {
            points += 15;
        }

        // 5. Top bowler (15 points)
        if (prediction.getTopBowler() != null && 
            prediction.getTopBowler().equalsIgnoreCase(match.getTopBowler())) {
            points += 15;
        }

        // 6. Total sixes range (10 points)
        if (prediction.getTotalSixes() != null && 
            prediction.getTotalSixes().equalsIgnoreCase(match.getActualTotalSixes())) {
            points += 10;
        }

        // 7. Total runs range (10 points)
        if (prediction.getTotalRuns() != null && 
            prediction.getTotalRuns().equalsIgnoreCase(match.getActualTotalRuns())) {
            points += 10;
        }

        return points;
    }
}

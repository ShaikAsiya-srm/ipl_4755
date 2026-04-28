package com.ipl.backend.model;

public class Prediction {
    private Long id;
    private Long userId;
    private Long matchId;
    private String tossWinner;
    private String batFirst;
    private String player;
    private String winner;
    private String topScorer;
    private String topBowler;
    private String totalSixes;
    private String totalRuns;
    private String predictedTeam;
    private int points;

    public Prediction() {}

    public Prediction(Long id, Long userId, Long matchId, String tossWinner, String batFirst, String player) {
        this.id = id;
        this.userId = userId;
        this.matchId = matchId;
        this.tossWinner = tossWinner;
        this.batFirst = batFirst;
        this.player = player;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getTossWinner() { return tossWinner; }
    public void setTossWinner(String tossWinner) { this.tossWinner = tossWinner; }

    public String getBatFirst() { return batFirst; }
    public void setBatFirst(String batFirst) { this.batFirst = batFirst; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getTopScorer() { return topScorer; }
    public void setTopScorer(String topScorer) { this.topScorer = topScorer; }

    public String getTopBowler() { return topBowler; }
    public void setTopBowler(String topBowler) { this.topBowler = topBowler; }

    public String getTotalSixes() { return totalSixes; }
    public void setTotalSixes(String totalSixes) { this.totalSixes = totalSixes; }

    public String getTotalRuns() { return totalRuns; }
    public void setTotalRuns(String totalRuns) { this.totalRuns = totalRuns; }

    public String getPredictedTeam() { return predictedTeam; }
    public void setPredictedTeam(String predictedTeam) { this.predictedTeam = predictedTeam; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}

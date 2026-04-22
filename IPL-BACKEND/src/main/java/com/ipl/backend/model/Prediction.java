package com.ipl.backend.model;

public class Prediction {
    private Long id;
    private Long userId;
    private Long matchId;
    private String tossWinner;
    private String batFirst;
    private String player;

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
}

package com.ipl.backend.model;

public class LeaderboardDTO {
    private String username;
    private int points;

    public LeaderboardDTO() {}

    public LeaderboardDTO(String username, int points) {
        this.username = username;
        this.points = points;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getScore() {
        return points;
    }

    public void setScore(int score) {
        this.points = score;
    }
}

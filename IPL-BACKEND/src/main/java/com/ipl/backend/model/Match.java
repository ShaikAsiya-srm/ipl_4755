package com.ipl.backend.model;

public class Match {
    private Long id;
    private String team1;
    private String team2;
    private String logo1;
    private String logo2;
    private String stadium;
    private String time;
    private String date; // match date in format YYYY-MM-DD
    private String score1;
    private String score2;
    private String result;
    private Boolean isToday;

    public Match() {}

    public String getScore1() { return score1; }
    public void setScore1(String score1) { this.score1 = score1; }
    public String getScore2() { return score2; }
    public void setScore2(String score2) { this.score2 = score2; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Match(Long id, String team1, String team2, String logo1, String logo2, String stadium, String time, Boolean isToday) {
        this.id = id;
        this.team1 = team1;
        this.team2 = team2;
        this.logo1 = logo1;
        this.logo2 = logo2;
        this.stadium = stadium;
        this.time = time;
        this.isToday = isToday;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeam1() { return team1; }
    public void setTeam1(String team1) { this.team1 = team1; }

    public String getTeam2() { return team2; }
    public void setTeam2(String team2) { this.team2 = team2; }

    public String getLogo1() { return logo1; }
    public void setLogo1(String logo1) { this.logo1 = logo1; }

    public String getLogo2() { return logo2; }
    public void setLogo2(String logo2) { this.logo2 = logo2; }

    public String getStadium() { return stadium; }
    public void setStadium(String stadium) { this.stadium = stadium; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Boolean getIsToday() { return isToday; }
    public void setIsToday(Boolean isToday) { this.isToday = isToday; }
}

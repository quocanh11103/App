package com.example.nickhercore;

public class Match {

    private int id;
    private int fixtureId;

    private String league;
    private String homeTeam;
    private String awayTeam;

    private int homeScore;
    private int awayScore;

    private String matchTime;
    private String status;

    private String homeLogo;
    private String awayLogo;

    private String round;
    private String date;
    private String venue;

    public Match(int fixtureId, String homeTeam, String awayTeam, String homeLogo, String awayLogo) {
        this.id = fixtureId;
        this.fixtureId = fixtureId;
        this.league = "";
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = 0;
        this.awayScore = 0;
        this.matchTime = "";
        this.status = "";
        this.homeLogo = homeLogo;
        this.awayLogo = awayLogo;
        this.round = "";
        this.date = "";
        this.venue = "";
    }

    public Match(String league, String homeTeam, String awayTeam, int homeScore, int awayScore, String matchTime, String status) {
        this.id = 0;
        this.fixtureId = 0;
        this.league = league;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.matchTime = matchTime;
        this.status = status;
        this.homeLogo = "";
        this.awayLogo = "";
        this.round = "";
        this.date = "";
        this.venue = "";
    }

    public Match(int id, String league, String homeTeam, String awayTeam, int homeScore, int awayScore, String matchTime, String status) {
        this.id = id;
        this.fixtureId = id;
        this.league = league;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.matchTime = matchTime;
        this.status = status;
        this.homeLogo = "";
        this.awayLogo = "";
        this.round = "";
        this.date = "";
        this.venue = "";
    }

    public Match(int fixtureId, String league, String homeTeam, String awayTeam,
                 int homeScore, int awayScore, String matchTime, String status,
                 String homeLogo, String awayLogo, String round, String date, String venue) {
        this.id = fixtureId;
        this.fixtureId = fixtureId;
        this.league = league;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.matchTime = matchTime;
        this.status = status;
        this.homeLogo = homeLogo;
        this.awayLogo = awayLogo;
        this.round = round;
        this.date = date;
        this.venue = venue;
    }

    public int getId() {
        return id;
    }

    public int getFixtureId() {
        return fixtureId;
    }

    public String getLeague() {
        return league == null ? "" : league;
    }

    public String getHomeTeam() {
        return homeTeam == null ? "" : homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam == null ? "" : awayTeam;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public String getMatchTime() {
        return matchTime == null ? "" : matchTime;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getHomeLogo() {
        return homeLogo == null ? "" : homeLogo;
    }

    public String getAwayLogo() {
        return awayLogo == null ? "" : awayLogo;
    }

    public String getRound() {
        return round == null ? "" : round;
    }

    public String getDate() {
        return date == null ? "" : date;
    }

    public String getVenue() {
        return venue == null ? "" : venue;
    }

    // Hàm này để MatchAdapter gọi sân vận động
    public String getStadium() {
        return venue == null ? "" : venue;
    }

    // Hàm này để MatchAdapter hiện phút thi đấu
    public int getMinute() {
        if (matchTime == null || matchTime.trim().isEmpty()) {
            return 0;
        }

        try {
            String value = matchTime.replace("'", "").trim();

            if (value.contains("+")) {
                value = value.substring(0, value.indexOf("+"));
            }

            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFixtureId(int fixtureId) {
        this.fixtureId = fixtureId;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public void setMatchTime(String matchTime) {
        this.matchTime = matchTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHomeLogo(String homeLogo) {
        this.homeLogo = homeLogo;
    }

    public void setAwayLogo(String awayLogo) {
        this.awayLogo = awayLogo;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
}
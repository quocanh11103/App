package com.example.nickhercore;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StandingResponse {

    public String get;
    public Object parameters;
    public Object errors;
    public int results;
    public List<StandingData> response;

    public static class StandingData {
        public League league;
    }

    public static class League {
        public int id;
        public String name;
        public String country;
        public String logo;
        public String flag;
        public int season;

        // API-Football trả standings là mảng 2 chiều:
        // standings -> group -> team standing
        public List<List<TeamStanding>> standings;
    }

    public static class TeamStanding {
        public int rank;
        public Team team;
        public int points;
        public int goalsDiff;
        public String group;
        public String form;
        public String status;
        public String description;

        public StandingStats all;
        public StandingStats home;
        public StandingStats away;

        public String update;
    }

    public static class Team {
        public int id;
        public String name;
        public String logo;
    }

    public static class StandingStats {
        public int played;
        public int win;
        public int draw;
        public int lose;
        public Goals goals;
    }

    public static class Goals {
        @SerializedName("for")
        public int goalsFor;

        public int against;
    }
}
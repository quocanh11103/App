package com.example.nickhercore;

import com.google.gson.annotations.SerializedName;

public class ApiFixtureItem {

    public FixtureInfo fixture;
    public LeagueInfo league;
    public TeamsInfo teams;
    public GoalsInfo goals;

    public static class FixtureInfo {
        public int id;
        public String date;
        public StatusInfo status;
        public VenueInfo venue;
    }

    public static class StatusInfo {
        @SerializedName("long")
        public String longName;

        @SerializedName("short")
        public String shortName;

        public Integer elapsed;
    }

    public static class VenueInfo {
        public String name;
        public String city;
    }

    public static class LeagueInfo {
        public int id;
        public String name;
        public String country;
        public String logo;
    }

    public static class TeamsInfo {
        public TeamSide home;
        public TeamSide away;
    }

    public static class TeamSide {
        public int id;
        public String name;
        public String logo;
        public Boolean winner;
    }

    public static class GoalsInfo {
        public Integer home;
        public Integer away;
    }
}
package com.example.nickhercore;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FixtureResponse {

    public List<MatchData> response;
    public Object errors;

    public static class MatchData {
        public Fixture fixture;
        public League league;
        public Teams teams;
        public Goals goals;
        public Score score;

        // Có thể không dùng nữa nếu đã gọi endpoint riêng,
        // nhưng giữ lại để tránh lỗi code cũ.
        public List<Event> events;
        public List<Lineup> lineups;
        public List<Statistic> statistics;
    }

    public static class Fixture {
        public int id;
        public String date;
        public String round;
        public Venue venue;
        public Status status;
    }

    public static class Venue {
        public String name;
        public String city;
    }

    public static class Status {

        @SerializedName("long")
        public String longName;

        @SerializedName("short")
        public String shortName;

        public Integer elapsed;
        public Integer extra;

        // Giữ lại để tương thích với code cũ nếu có gọi shortStatus/longStatus
        public String longStatus;
        public String shortStatus;

        public String getShortText() {
            if (shortName != null && !shortName.isEmpty()) return shortName;
            if (shortStatus != null && !shortStatus.isEmpty()) return shortStatus;
            return "";
        }

        public String getLongText() {
            if (longName != null && !longName.isEmpty()) return longName;
            if (longStatus != null && !longStatus.isEmpty()) return longStatus;
            return "";
        }
    }

    public static class League {
        public int id;
        public String name;
        public String country;
        public String logo;
        public String flag;
        public int season;
        public String round;
    }

    public static class Teams {
        public Team home;
        public Team away;
    }

    public static class Team {
        public int id;
        public String name;
        public String logo;
        public Boolean winner;
    }

    public static class Goals {
        public Integer home;
        public Integer away;
    }

    public static class Score {
        public Goals halftime;
        public Goals fulltime;
        public Goals extratime;
        public Goals penalty;
    }

    public static class Event {
        public Time time;
        public Team team;
        public Player player;
        public Player assist;
        public String type;
        public String detail;
        public String comments;
    }

    public static class Time {
        public int elapsed;
        public Integer extra;
    }

    public static class Player {
        public int id;
        public String name;
    }

    public static class Lineup {
        public Team team;
        public String formation;
        public List<PlayerInLineup> startXI;
        public List<PlayerInLineup> substitutes;
    }

    public static class PlayerInLineup {
        public PlayerInfo player;
    }

    public static class PlayerInfo {
        public Integer id;
        public String name;
        public Integer number;
        public String pos;
        public String grid;
    }

    public static class Statistic {
        public Team team;
        public List<StatValue> statistics;
    }

    public static class StatValue {
        public String type;
        public Object value;
    }
}
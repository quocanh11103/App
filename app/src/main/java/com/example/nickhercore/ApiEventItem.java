package com.example.nickhercore;

public class ApiEventItem {

    public TimeInfo time;
    public TeamInfo team;
    public PlayerInfo player;
    public PlayerInfo assist;
    public String type;
    public String detail;
    public String comments;

    public static class TimeInfo {
        public Integer elapsed;
        public Integer extra;
    }

    public static class TeamInfo {
        public int id;
        public String name;
        public String logo;
    }

    public static class PlayerInfo {
        public Integer id;
        public String name;
    }
}
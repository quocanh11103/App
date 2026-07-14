package com.example.nickhercore;

import java.util.List;

public class ApiLineupItem {

    public TeamInfo team;
    public String formation;
    public CoachInfo coach;
    public List<LineupPlayerWrapper> startXI;
    public List<LineupPlayerWrapper> substitutes;

    public static class TeamInfo {
        public int id;
        public String name;
        public String logo;
    }

    public static class CoachInfo {
        public Integer id;
        public String name;
        public String photo;
    }

    public static class LineupPlayerWrapper {
        public LineupPlayer player;
    }

    public static class LineupPlayer {
        public Integer id;
        public String name;
        public Integer number;
        public String pos;
        public String grid;
    }
}
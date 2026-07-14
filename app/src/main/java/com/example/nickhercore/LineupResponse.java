package com.example.nickhercore;
import java.util.List;

public class LineupResponse {
    public List<LineupData> response;

    public static class LineupData {
        public Team team;
        public String formation;
        public List<PlayerEntry> startXI;
    }

    public static class Team { public String name; }
    public static class PlayerEntry { public PlayerDetails player; }
    public static class PlayerDetails { public String name; public String number; }
}
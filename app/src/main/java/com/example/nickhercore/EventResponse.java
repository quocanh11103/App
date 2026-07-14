package com.example.nickhercore;
import java.util.List;

public class EventResponse {
    public List<EventData> response;

    public static class EventData {
        public Time time;
        public Team team;
        public Player player;
        public String type;
        public String detail;
    }

    public static class Time { public int elapsed; public Integer extra; }
    public static class Team { public String name; }
    public static class Player { public String name; }
}
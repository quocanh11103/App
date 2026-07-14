package com.example.nickhercore;
import java.util.List;

public class StatisticResponse {
    public List<StatData> response;

    public static class StatData {
        public Team team;
        public List<StatValue> statistics;
    }

    public static class Team { public String name; }
    public static class StatValue { public String type; public Object value; }
}
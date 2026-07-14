package com.example.nickhercore;

import java.util.List;

public class ApiStatisticItem {

    public TeamInfo team;
    public List<StatisticInfo> statistics;

    public static class TeamInfo {
        public int id;
        public String name;
        public String logo;
    }

    public static class StatisticInfo {
        public String type;
        public Object value;
    }
}
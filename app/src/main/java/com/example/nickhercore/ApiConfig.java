package com.example.nickhercore;

public class ApiConfig {

    public static final String BASE_URL = "https://v3.football.api-sports.io/";

    public static final String API_KEY = "413bf87ecc7971cca4f97225d9d4c0f4";

    public static final int WORLD_CUP_LEAGUE_ID = 1;

    public static final int WORLD_CUP_SEASON = 2022;

    public static boolean hasApiKey() {
        return API_KEY != null && API_KEY.trim().length() >= 30;
    }
}
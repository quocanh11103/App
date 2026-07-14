package com.example.nickhercore;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WorldCup2026ApiService {

    @GET("get/games")
    Call<JsonElement> getGames();

    @GET("get/game/{id}")
    Call<JsonElement> getGameById(@Path("id") int id);

    @GET("get/groups")
    Call<JsonElement> getGroups();

    @GET("get/teams")
    Call<JsonElement> getTeams();

    @GET("get/stadiums")
    Call<JsonElement> getStadiums();
}
package com.example.nickhercore;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ZafronixApiService {

    @GET("matches")
    Call<JsonElement> getMatches(@Query("year") int year);

    @GET("bracket")
    Call<JsonElement> getBracket(@Query("year") int year);

    @GET("teams")
    Call<JsonElement> getTeams(@Query("tournament") int tournament);

    @GET("standings")
    Call<JsonElement> getStandings(@Query("year") int year);

    @GET("stadiums")
    Call<JsonElement> getStadiums(@Query("year") int year);

    @GET("players")
    Call<JsonElement> getPlayers(@Query("year") int year);
}
package com.example.nickhercore;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SalahWcupApiService {

    @GET("data.php")
    Call<JsonElement> getData(@Query("action") String action);

    @GET("data.php")
    Call<JsonElement> getMatch(@Query("action") String action, @Query("id") int id);
}
package com.example.nickhercore;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FootballApiService {

    @GET("fixtures")
    Call<FixtureResponse> getFixtures(
            @Query("league") int leagueId,
            @Query("season") int season
    );

    // Lấy trận đang LIVE
    @GET("fixtures")
    Call<FixtureResponse> getLiveFixtures(
            @Query("live") String live
    );

    @GET("fixtures/lineups")
    Call<LineupResponse> getLineups(
            @Query("fixture") int fixtureId
    );
    @GET("fixtures")
    Call<FixtureResponse> getUpcomingFixtures(
            @Query("next") int next
    );
    @GET("fixtures/statistics")
    Call<StatisticResponse> getStatistics(
            @Query("fixture") int fixtureId
    );

    @GET("fixtures/events")
    Call<EventResponse> getEvents(
            @Query("fixture") int fixtureId
    );

    @GET("standings")
    Call<StandingResponse> getStandings(
            @Query("league") int leagueId,
            @Query("season") int season
    );
}
package com.example.nickhercore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FootballRepository {

    private FootballApiService apiService;

    public FootballRepository() {
        this.apiService = ApiClient.getApiService();
    }

    // =========================================================
    // 1. Hàm cũ: lấy FixtureResponse thô
    // =========================================================
    public void fetchFixtures(final OnDataRetrieved<FixtureResponse> callback) {
        apiService.getFixtures(ApiConfig.WORLD_CUP_LEAGUE_ID, ApiConfig.WORLD_CUP_SEASON)
                .enqueue(new Callback<FixtureResponse>() {
                    @Override
                    public void onResponse(Call<FixtureResponse> call, Response<FixtureResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("Lỗi lấy lịch thi đấu. Mã lỗi: " + response.code());
                            return;
                        }

                        if (response.body().response == null) {
                            callback.onError("API trả về dữ liệu rỗng.");
                            return;
                        }

                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<FixtureResponse> call, Throwable t) {
                        callback.onError("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    // =========================================================
    // 2. Hàm dùng cho MainActivity giao diện cũ
    // =========================================================
    public void getWorldCupMatches(final MatchCallback callback) {
        apiService.getFixtures(ApiConfig.WORLD_CUP_LEAGUE_ID, ApiConfig.WORLD_CUP_SEASON)
                .enqueue(new Callback<FixtureResponse>() {
                    @Override
                    public void onResponse(Call<FixtureResponse> call, Response<FixtureResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("Không lấy được dữ liệu World Cup. Mã lỗi: " + response.code());
                            return;
                        }

                        if (response.body().errors != null && response.body().errors.toString().length() > 2) {
                            callback.onError("Lỗi API: " + response.body().errors.toString());
                            return;
                        }

                        if (response.body().response == null) {
                            callback.onError("API trả response null.");
                            return;
                        }

                        if (response.body().response.isEmpty()) {
                            callback.onError("API trả về 0 trận. Kiểm tra league=1, season=2022 hoặc quota API.");
                            return;
                        }

                        try {
                            List<Match> matches = convertFixtureResponseToMatches(response.body().response);

                            if (matches.isEmpty()) {
                                callback.onError("Có dữ liệu API nhưng convert sang Match bị rỗng.");
                                return;
                            }

                            callback.onSuccess(matches);
                        } catch (Exception e) {
                            callback.onError("Lỗi xử lý dữ liệu trận đấu: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<FixtureResponse> call, Throwable t) {
                        callback.onError("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    // =========================================================
    // 3. Lấy trận LIVE
    // =========================================================
    public void getLiveMatches(final MatchCallback callback) {
        apiService.getLiveFixtures("all")
                .enqueue(new Callback<FixtureResponse>() {
                    @Override
                    public void onResponse(Call<FixtureResponse> call, Response<FixtureResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("Không lấy được trận LIVE. Mã lỗi: " + response.code());
                            return;
                        }

                        if (response.body().response == null) {
                            callback.onError("Không có dữ liệu LIVE.");
                            return;
                        }

                        try {
                            List<Match> matches = convertFixtureResponseToMatches(response.body().response);
                            callback.onSuccess(matches);
                        } catch (Exception e) {
                            callback.onError("Lỗi xử lý LIVE: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<FixtureResponse> call, Throwable t) {
                        callback.onError("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    // =========================================================
    // 4. Lấy trận Knock-out
    // =========================================================
    public void getKnockoutMatches(final MatchCallback callback) {
        getWorldCupMatches(new MatchCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                List<Match> knockoutMatches = new ArrayList<>();

                for (Match match : matches) {
                    String round = match.getRound();

                    if (isKnockoutRound(round)) {
                        knockoutMatches.add(match);
                    }
                }

                callback.onSuccess(knockoutMatches);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private boolean isKnockoutRound(String round) {
        if (round == null) return false;

        return round.contains("Round of 16")
                || round.contains("Quarter-finals")
                || round.contains("Semi-finals")
                || round.contains("3rd Place Final")
                || round.equals("Final");
    }

    // =========================================================
    // 5. Lấy diễn biến trận đấu
    // =========================================================
    public void getMatchEvents(int fixtureId, final DetailCallback callback) {
        apiService.getEvents(fixtureId).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().response == null) {
                    callback.onSuccess("Không có diễn biến trận đấu.");
                    return;
                }

                if (response.body().response.isEmpty()) {
                    callback.onSuccess("Không có diễn biến trận đấu.");
                    return;
                }

                StringBuilder sb = new StringBuilder();

                for (EventResponse.EventData e : response.body().response) {
                    if (e == null) continue;

                    String time = "";
                    String type = "";
                    String playerName = "N/A";
                    String teamName = "N/A";

                    if (e.time != null) {
                        time = String.valueOf(e.time.elapsed);
                        if (e.time.extra != null) {
                            time += "+" + e.time.extra;
                        }
                    }

                    if (e.type != null) {
                        type = e.type;
                    }

                    if (e.player != null && e.player.name != null) {
                        playerName = e.player.name;
                    }

                    if (e.team != null && e.team.name != null) {
                        teamName = e.team.name;
                    }

                    sb.append(time)
                            .append("' - ")
                            .append(type)
                            .append(": ")
                            .append(playerName)
                            .append(" (")
                            .append(teamName)
                            .append(")\n");
                }

                callback.onSuccess(sb.length() > 0 ? sb.toString() : "Không có diễn biến trận đấu.");
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                callback.onError("Lỗi lấy diễn biến: " + t.getMessage());
            }
        });
    }

    // =========================================================
    // 6. Lấy đội hình
    // =========================================================
    public void getMatchLineups(int fixtureId, final DetailCallback callback) {
        apiService.getLineups(fixtureId).enqueue(new Callback<LineupResponse>() {
            @Override
            public void onResponse(Call<LineupResponse> call, Response<LineupResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().response == null) {
                    callback.onSuccess("Không có dữ liệu đội hình.");
                    return;
                }

                if (response.body().response.isEmpty()) {
                    callback.onSuccess("Không có dữ liệu đội hình.");
                    return;
                }

                StringBuilder sb = new StringBuilder();

                for (LineupResponse.LineupData l : response.body().response) {
                    if (l == null) continue;

                    String teamName = "N/A";
                    String formation = "";

                    if (l.team != null && l.team.name != null) {
                        teamName = l.team.name;
                    }

                    if (l.formation != null) {
                        formation = l.formation;
                    }

                    sb.append("--- ")
                            .append(teamName)
                            .append(" (")
                            .append(formation)
                            .append(") ---\n");

                    if (l.startXI != null) {
                        for (LineupResponse.PlayerEntry p : l.startXI) {
                            if (p == null || p.player == null) continue;

                            String number = p.player.number == null ? "" : String.valueOf(p.player.number);
                            String name = p.player.name == null ? "N/A" : p.player.name;

                            sb.append(number)
                                    .append(". ")
                                    .append(name)
                                    .append("\n");
                        }
                    }

                    sb.append("\n");
                }

                callback.onSuccess(sb.length() > 0 ? sb.toString() : "Không có dữ liệu đội hình.");
            }

            @Override
            public void onFailure(Call<LineupResponse> call, Throwable t) {
                callback.onError("Lỗi lấy đội hình: " + t.getMessage());
            }
        });
    }

    // =========================================================
    // 7. Lấy thống kê
    // =========================================================
    public void getMatchStatistics(int fixtureId, final DetailCallback callback) {
        apiService.getStatistics(fixtureId).enqueue(new Callback<StatisticResponse>() {
            @Override
            public void onResponse(Call<StatisticResponse> call, Response<StatisticResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().response == null) {
                    callback.onSuccess("Không có dữ liệu thống kê.");
                    return;
                }

                if (response.body().response.isEmpty()) {
                    callback.onSuccess("Không có dữ liệu thống kê.");
                    return;
                }

                StringBuilder sb = new StringBuilder();

                for (StatisticResponse.StatData s : response.body().response) {
                    if (s == null) continue;

                    String teamName = "N/A";

                    if (s.team != null && s.team.name != null) {
                        teamName = s.team.name;
                    }

                    sb.append("=== ")
                            .append(teamName)
                            .append(" ===\n");

                    if (s.statistics != null) {
                        for (StatisticResponse.StatValue v : s.statistics) {
                            if (v == null) continue;

                            String type = v.type == null ? "N/A" : v.type;
                            String value = v.value == null ? "0" : String.valueOf(v.value);

                            sb.append(type)
                                    .append(": ")
                                    .append(value)
                                    .append("\n");
                        }
                    }

                    sb.append("\n");
                }

                callback.onSuccess(sb.length() > 0 ? sb.toString() : "Không có dữ liệu thống kê.");
            }

            @Override
            public void onFailure(Call<StatisticResponse> call, Throwable t) {
                callback.onError("Lỗi lấy thống kê: " + t.getMessage());
            }
        });
    }

    // =========================================================
    // 8. Chuyển FixtureResponse sang List<Match>
    // =========================================================
    private List<Match> convertFixtureResponseToMatches(List<FixtureResponse.MatchData> dataList) {
        List<Match> matches = new ArrayList<>();

        if (dataList == null) {
            return matches;
        }

        for (FixtureResponse.MatchData item : dataList) {
            if (item == null) continue;

            int fixtureId = 0;
            String leagueName = "FIFA World Cup 2022";
            String round = "";
            String homeTeam = "";
            String awayTeam = "";
            String homeLogo = "";
            String awayLogo = "";
            String status = "";
            String date = "";
            String venue = "";
            String matchTime = "";
            int homeScore = 0;
            int awayScore = 0;

            if (item.fixture != null) {
                fixtureId = item.fixture.id;
                date = item.fixture.date == null ? "" : item.fixture.date;

                if (item.fixture.status != null) {
                    status = item.fixture.status.shortName == null ? "" : item.fixture.status.shortName;

                    if (item.fixture.status.elapsed != null) {
                        matchTime = item.fixture.status.elapsed + "'";
                    }
                }

                if (item.fixture.venue != null) {
                    venue = item.fixture.venue.name == null ? "" : item.fixture.venue.name;
                }
            }

            if (item.league != null) {
                if (item.league.name != null) {
                    leagueName = item.league.name;
                }

                if (item.league.round != null) {
                    round = item.league.round;
                }
            }

            if (item.teams != null) {
                if (item.teams.home != null) {
                    homeTeam = item.teams.home.name == null ? "" : item.teams.home.name;
                    homeLogo = item.teams.home.logo == null ? "" : item.teams.home.logo;
                }

                if (item.teams.away != null) {
                    awayTeam = item.teams.away.name == null ? "" : item.teams.away.name;
                    awayLogo = item.teams.away.logo == null ? "" : item.teams.away.logo;
                }
            }

            if (item.goals != null) {
                homeScore = item.goals.home == null ? 0 : item.goals.home;
                awayScore = item.goals.away == null ? 0 : item.goals.away;
            }

            String leagueText = leagueName;
            if (!round.isEmpty()) {
                leagueText = leagueName + " • " + round;
            }

            Match match = new Match(
                    fixtureId,
                    leagueText,
                    homeTeam,
                    awayTeam,
                    homeScore,
                    awayScore,
                    matchTime,
                    status,
                    homeLogo,
                    awayLogo,
                    round,
                    date,
                    venue
            );

            matches.add(match);
        }

        return matches;
    }
    public void getStandingsText(final DetailCallback callback) {
        apiService.getStandings(ApiConfig.WORLD_CUP_LEAGUE_ID, ApiConfig.WORLD_CUP_SEASON)
                .enqueue(new Callback<StandingResponse>() {
                    @Override
                    public void onResponse(Call<StandingResponse> call, Response<StandingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().response == null) {
                            callback.onError("Không lấy được bảng đấu. Mã lỗi: " + response.code());
                            return;
                        }

                        try {
                            StringBuilder sb = new StringBuilder();

                            for (StandingResponse.StandingData item : response.body().response) {
                                if (item == null || item.league == null || item.league.standings == null) {
                                    continue;
                                }

                                sb.append(item.league.name == null ? "World Cup" : item.league.name)
                                        .append("\n\n");

                                for (List<StandingResponse.TeamStanding> group : item.league.standings) {
                                    if (group == null || group.isEmpty()) continue;

                                    String groupName = group.get(0).group == null ? "Bảng" : group.get(0).group;
                                    sb.append(groupName).append("\n");

                                    for (StandingResponse.TeamStanding team : group) {
                                        if (team == null || team.team == null) continue;

                                        sb.append(team.rank)
                                                .append(". ")
                                                .append(team.team.name)
                                                .append(" - ")
                                                .append(team.points)
                                                .append(" điểm")
                                                .append(" | HS: ")
                                                .append(team.goalsDiff)
                                                .append("\n");
                                    }

                                    sb.append("\n");
                                }
                            }

                            if (sb.length() == 0) {
                                callback.onSuccess("Không có dữ liệu bảng đấu.");
                            } else {
                                callback.onSuccess(sb.toString());
                            }

                        } catch (Exception e) {
                            callback.onError("Lỗi xử lý bảng đấu: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<StandingResponse> call, Throwable t) {
                        callback.onError("Lỗi mạng: " + t.getMessage());
                    }
                });
    }
    public void getUpcomingMatches(final MatchCallback callback) {
        apiService.getUpcomingFixtures(30)
                .enqueue(new Callback<FixtureResponse>() {
                    @Override
                    public void onResponse(Call<FixtureResponse> call, Response<FixtureResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("Không lấy được lịch thi đấu sắp tới. Mã lỗi: " + response.code());
                            return;
                        }

                        if (response.body().errors != null && response.body().errors.toString().length() > 2) {
                            callback.onError("Lỗi API: " + response.body().errors.toString());
                            return;
                        }

                        if (response.body().response == null || response.body().response.isEmpty()) {
                            callback.onError("Không có trận sắp diễn ra.");
                            return;
                        }

                        try {
                            List<Match> matches = convertFixtureResponseToMatches(response.body().response);
                            callback.onSuccess(matches);
                        } catch (Exception e) {
                            callback.onError("Lỗi xử lý lịch sắp diễn ra: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<FixtureResponse> call, Throwable t) {
                        callback.onError("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    // =========================================================
    // 9. Interfaces
    // =========================================================
    public interface OnDataRetrieved<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public interface MatchCallback {
        void onSuccess(List<Match> matches);
        void onError(String message);
    }

    public interface DetailCallback {
        void onSuccess(String text);
        void onError(String message);
    }
}
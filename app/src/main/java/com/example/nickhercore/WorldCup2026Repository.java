package com.example.nickhercore;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorldCup2026Repository {

    private final WorldCup2026ApiService apiService;

    public WorldCup2026Repository() {
        apiService = WorldCup2026Client.getApiService();
    }

    public void getAllMatches(final FootballRepository.MatchCallback callback) {
        apiService.getGames().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                handleMatchesResponse(response, callback, "Không lấy được dữ liệu World Cup 2026");
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng World Cup 2026: " + t.getMessage());
            }
        });
    }

    public void getFutureMatches(final FootballRepository.MatchCallback callback) {
        getAllMatches(new FootballRepository.MatchCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                List<Match> result = new ArrayList<>();

                for (Match match : matches) {
                    String status = match.getStatus().toLowerCase(Locale.ROOT);

                    if (!status.contains("finished")) {
                        result.add(match);
                    }
                }

                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getMatchesByStage(final String stage, final FootballRepository.MatchCallback callback) {
        getAllMatches(new FootballRepository.MatchCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                List<Match> result = new ArrayList<>();

                for (Match match : matches) {
                    if (isStage(match, stage)) {
                        result.add(match);
                    }
                }

                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getGroupsText(final FootballRepository.DetailCallback callback) {
        apiService.getGroups().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không lấy được bảng đấu World Cup 2026. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    String text = convertGroupsToText(response.body(), new HashMap<>());

                    if (text == null || text.trim().isEmpty()) {
                        callback.onError("API có trả dữ liệu nhưng app chưa đọc được bảng đấu.");
                        return;
                    }

                    callback.onSuccess(text);
                } catch (Exception e) {
                    callback.onError("Lỗi xử lý bảng đấu: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng bảng đấu: " + t.getMessage());
            }
        });
    }

    public void getStadiumsText(final FootballRepository.DetailCallback callback) {
        apiService.getStadiums().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không lấy được sân vận động. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    callback.onSuccess(convertStadiumsToText(response.body()));
                } catch (Exception e) {
                    callback.onError("Lỗi xử lý sân vận động: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng sân vận động: " + t.getMessage());
            }
        });
    }

    private void handleMatchesResponse(Response<JsonElement> response,
                                       FootballRepository.MatchCallback callback,
                                       String errorPrefix) {
        if (!response.isSuccessful() || response.body() == null) {
            callback.onError(errorPrefix + ". Mã lỗi: " + response.code());
            return;
        }

        try {
            List<Match> matches = convertToMatches(response.body());

            if (matches.isEmpty()) {
                String preview = new GsonBuilder().setPrettyPrinting().create().toJson(response.body());

                if (preview.length() > 500) {
                    preview = preview.substring(0, 500);
                }

                callback.onError("API có dữ liệu nhưng app chưa đọc được trận. JSON mẫu: " + preview);
                return;
            }

            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match a, Match b) {
                    int dateCompare = a.getDate().compareTo(b.getDate());

                    if (dateCompare != 0) {
                        return dateCompare;
                    }

                    return Integer.compare(a.getFixtureId(), b.getFixtureId());
                }
            });

            callback.onSuccess(matches);
        } catch (Exception e) {
            callback.onError("Lỗi xử lý dữ liệu World Cup 2026: " + e.getMessage());
        }
    }

    private List<Match> convertToMatches(JsonElement root) {
        List<Match> matches = new ArrayList<>();
        List<JsonObject> objects = extractObjects(root);

        for (JsonObject obj : objects) {
            String homeTeam = getStringAny(obj,
                    "home_team_name_en",
                    "home_team_label",
                    "home_team",
                    "homeTeam",
                    "home",
                    "team1",
                    "home_name");

            String awayTeam = getStringAny(obj,
                    "away_team_name_en",
                    "away_team_label",
                    "away_team",
                    "awayTeam",
                    "away",
                    "team2",
                    "away_name");

            if (homeTeam.isEmpty()) {
                homeTeam = getNestedString(obj, "home", "name_en", "name", "team", "country");
            }

            if (awayTeam.isEmpty()) {
                awayTeam = getNestedString(obj, "away", "name_en", "name", "team", "country");
            }

            if (homeTeam.isEmpty()) {
                homeTeam = "TBD";
            }

            if (awayTeam.isEmpty()) {
                awayTeam = "TBD";
            }

            int fixtureId = getIntAny(obj, -1,
                    "id",
                    "match_id",
                    "game_id",
                    "fixture_id");

            if (fixtureId <= 0) {
                fixtureId = Math.abs(obj.toString().hashCode());
            }

            int homeScore = getIntAny(obj, 0,
                    "home_score",
                    "homeScore",
                    "home_goals",
                    "score_home");

            int awayScore = getIntAny(obj, 0,
                    "away_score",
                    "awayScore",
                    "away_goals",
                    "score_away");

            String date = getStringAny(obj,
                    "local_date",
                    "date",
                    "datetime",
                    "kickoff",
                    "kickoff_time",
                    "match_date",
                    "start_time");

            String status = buildStatus(obj);
            String round = buildRound(obj);

            String venue = getStringAny(obj,
                    "venue",
                    "stadium",
                    "stadium_name",
                    "location",
                    "stadium_id");

            if (!venue.isEmpty() && venue.matches("\\d+")) {
                venue = "Stadium ID: " + venue;
            }

            String homeLogo = getFlagUrl(homeTeam);
            String awayLogo = getFlagUrl(awayTeam);

            Match match = new Match(
                    fixtureId,
                    "FIFA World Cup 2026",
                    homeTeam,
                    awayTeam,
                    homeScore,
                    awayScore,
                    date,
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

    private String buildStatus(JsonObject obj) {
        String finished = getStringAny(obj, "finished");
        String elapsed = getStringAny(obj, "time_elapsed");
        String status = getStringAny(obj, "status", "match_status", "state");

        if (!status.isEmpty()) {
            return status;
        }

        if ("TRUE".equalsIgnoreCase(finished) || "true".equalsIgnoreCase(finished)) {
            return "Finished";
        }

        if (!elapsed.isEmpty()
                && !"notstarted".equalsIgnoreCase(elapsed)
                && !"0".equals(elapsed)) {
            return "Live";
        }

        return "Scheduled";
    }

    private String buildRound(JsonObject obj) {
        String type = getStringAny(obj, "type");
        String group = getStringAny(obj, "group");
        String matchday = getStringAny(obj, "matchday");

        String t = type.toLowerCase(Locale.ROOT);

        if (t.equals("group")) {
            if (!group.isEmpty()) {
                if (!matchday.isEmpty()) {
                    return "Group " + group + " - Matchday " + matchday;
                }

                return "Group " + group;
            }

            return "Group Stage";
        }

        if (t.equals("r32")) {
            return "Round of 32";
        }

        if (t.equals("r16")) {
            return "Round of 16";
        }

        if (t.equals("qf")) {
            return "Quarter-finals";
        }

        if (t.equals("sf")) {
            return "Semi-finals";
        }

        if (t.equals("third")) {
            return "3rd Place Final";
        }

        if (t.equals("final")) {
            return "Final";
        }

        return "World Cup 2026";
    }

    private boolean isStage(Match match, String stage) {
        String round = match.getRound().toLowerCase(Locale.ROOT);

        if ("R32".equals(stage)) {
            return round.contains("round of 32");
        }

        if ("R16".equals(stage)) {
            return round.contains("round of 16");
        }

        if ("QF".equals(stage)) {
            return round.contains("quarter");
        }

        if ("SF".equals(stage)) {
            return round.contains("semi");
        }

        if ("FINAL".equals(stage)) {
            return round.contains("final") || round.contains("3rd place");
        }

        return false;
    }

    private String convertGroupsToText(JsonElement root, Map<String, String> teamMap) {
        List<JsonObject> groups = extractObjects(root);

        if (groups.isEmpty()) {
            return new GsonBuilder().setPrettyPrinting().create().toJson(root);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("BẢNG ĐẤU WORLD CUP 2026\n\n");

        for (JsonObject group : groups) {
            String groupName = getStringAny(group,
                    "group",
                    "name",
                    "group_name",
                    "letter",
                    "title");

            if (groupName.isEmpty()) {
                groupName = "Bảng";
            }

            if (!groupName.toLowerCase(Locale.ROOT).contains("group")
                    && !groupName.toLowerCase(Locale.ROOT).contains("bảng")) {
                sb.append("Bảng ").append(groupName).append("\n");
            } else {
                sb.append(groupName).append("\n");
            }

            JsonElement teamsElement = null;

            if (group.has("teams")) {
                teamsElement = group.get("teams");
            } else if (group.has("standings")) {
                teamsElement = group.get("standings");
            } else if (group.has("members")) {
                teamsElement = group.get("members");
            } else if (group.has("countries")) {
                teamsElement = group.get("countries");
            }

            if (teamsElement != null && teamsElement.isJsonArray()) {
                JsonArray arr = teamsElement.getAsJsonArray();

                for (JsonElement e : arr) {
                    if (e != null && e.isJsonObject()) {
                        JsonObject team = e.getAsJsonObject();

                        String teamId = getStringAny(team, "team_id", "id");
                        String teamName = getStringAny(team,
                                "team_name_en",
                                "name_en",
                                "name",
                                "team",
                                "country");

                        if (teamName.isEmpty() && !teamId.isEmpty() && teamMap.containsKey(teamId)) {
                            teamName = teamMap.get(teamId);
                        }

                        if (teamName.isEmpty()) {
                            teamName = "Team ID: " + teamId;
                        }

                        String pts = getStringAny(team, "pts", "points");
                        String played = getStringAny(team, "mp", "played", "p");
                        String win = getStringAny(team, "w", "win");
                        String draw = getStringAny(team, "d", "draw");
                        String lose = getStringAny(team, "l", "lose");
                        String gf = getStringAny(team, "gf");
                        String ga = getStringAny(team, "ga");
                        String gd = getStringAny(team, "gd");

                        sb.append("• ").append(teamName);

                        if (!pts.isEmpty()) {
                            sb.append(" | Điểm: ").append(pts);
                        }

                        if (!played.isEmpty()) {
                            sb.append(" | Trận: ").append(played);
                        }

                        if (!win.isEmpty() || !draw.isEmpty() || !lose.isEmpty()) {
                            sb.append(" | T-H-B: ")
                                    .append(win.isEmpty() ? "0" : win).append("-")
                                    .append(draw.isEmpty() ? "0" : draw).append("-")
                                    .append(lose.isEmpty() ? "0" : lose);
                        }

                        if (!gf.isEmpty() || !ga.isEmpty() || !gd.isEmpty()) {
                            sb.append(" | BT-BB: ")
                                    .append(gf.isEmpty() ? "0" : gf).append("-")
                                    .append(ga.isEmpty() ? "0" : ga);

                            if (!gd.isEmpty()) {
                                sb.append(" | HS: ").append(gd);
                            }
                        }

                        sb.append("\n");
                    } else {
                        String teamName = elementToString(e);

                        if (!teamName.isEmpty()) {
                            sb.append("• ").append(teamName).append("\n");
                        }
                    }
                }
            } else {
                sb.append(group.toString()).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private String convertStadiumsToText(JsonElement root) {
        List<JsonObject> stadiums = extractObjects(root);

        if (stadiums.isEmpty()) {
            return new GsonBuilder().setPrettyPrinting().create().toJson(root);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SÂN VẬN ĐỘNG WORLD CUP 2026\n\n");

        for (JsonObject stadium : stadiums) {
            String name = getStringAny(stadium,
                    "name_en",
                    "name",
                    "fifa_name");

            String city = getStringAny(stadium,
                    "city_en",
                    "city");

            String country = getStringAny(stadium,
                    "country_en",
                    "country");

            String capacity = getStringAny(stadium,
                    "capacity");

            sb.append("• ").append(name);

            if (!city.isEmpty()) {
                sb.append("\n  Thành phố: ").append(city);
            }

            if (!country.isEmpty()) {
                sb.append("\n  Quốc gia: ").append(country);
            }

            if (!capacity.isEmpty()) {
                sb.append("\n  Sức chứa: ").append(capacity);
            }

            sb.append("\n\n");
        }

        return sb.toString();
    }

    private List<JsonObject> extractObjects(JsonElement root) {
        List<JsonObject> list = new ArrayList<>();

        if (root == null || root.isJsonNull()) {
            return list;
        }

        if (root.isJsonArray()) {
            JsonArray arr = root.getAsJsonArray();

            for (JsonElement e : arr) {
                if (e != null && e.isJsonObject()) {
                    list.add(e.getAsJsonObject());
                }
            }

            return list;
        }

        if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();

            String[] keys = {
                    "games",
                    "groups",
                    "teams",
                    "stadiums",
                    "data",
                    "response",
                    "matches",
                    "fixtures",
                    "result",
                    "items",
                    "records"
            };

            for (String key : keys) {
                if (obj.has(key)) {
                    JsonElement e = obj.get(key);

                    if (e != null && e.isJsonArray()) {
                        list.addAll(extractObjects(e));
                        return list;
                    }

                    if (e != null && e.isJsonObject()) {
                        List<JsonObject> sub = extractObjects(e);

                        if (!sub.isEmpty()) {
                            list.addAll(sub);
                            return list;
                        }
                    }
                }
            }

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                JsonElement e = entry.getValue();

                if (e != null && e.isJsonArray()) {
                    List<JsonObject> sub = extractObjects(e);

                    if (!sub.isEmpty()) {
                        list.addAll(sub);
                        return list;
                    }
                }
            }
        }

        return list;
    }

    private String getStringAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key)) {
                String value = elementToString(obj.get(key));

                if (!value.isEmpty()
                        && !"null".equalsIgnoreCase(value)
                        && !"undefined".equalsIgnoreCase(value)) {
                    return value;
                }
            }
        }

        return "";
    }

    private String getNestedString(JsonObject obj, String parent, String... keys) {
        if (!obj.has(parent) || !obj.get(parent).isJsonObject()) {
            return "";
        }

        return getStringAny(obj.get(parent).getAsJsonObject(), keys);
    }

    private int getIntAny(JsonObject obj, int defaultValue, String... keys) {
        for (String key : keys) {
            if (obj.has(key)) {
                try {
                    JsonElement e = obj.get(key);

                    if (e == null || e.isJsonNull()) {
                        continue;
                    }

                    if (e.isJsonPrimitive()) {
                        String value = e.getAsString()
                                .replace("null", "")
                                .replaceAll("[^0-9-]", "");

                        if (!value.isEmpty()) {
                            return Integer.parseInt(value);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return defaultValue;
    }

    private String elementToString(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return "";
        }

        if (e.isJsonPrimitive()) {
            return e.getAsString();
        }

        if (e.isJsonObject()) {
            JsonObject obj = e.getAsJsonObject();

            String value = getStringAny(obj,
                    "name_en",
                    "name",
                    "title",
                    "country",
                    "team",
                    "team_name",
                    "short_name",
                    "abbr",
                    "code",
                    "fifa_code");

            if (!value.isEmpty()) {
                return value;
            }

            return obj.toString();
        }

        return "";
    }

    private String getFlagUrl(String teamName) {
        String code = getCountryCode(teamName);

        if (code.isEmpty()) {
            return "";
        }

        return "https://flagcdn.com/w80/" + code + ".png";
    }

    private String getCountryCode(String teamName) {
        if (teamName == null) {
            return "";
        }

        String name = teamName.trim().toLowerCase(Locale.ROOT);

        switch (name) {
            case "argentina":
                return "ar";
            case "australia":
                return "au";
            case "austria":
                return "at";
            case "belgium":
                return "be";
            case "bosnia and herzegovina":
            case "bosnia":
                return "ba";
            case "brazil":
                return "br";
            case "canada":
                return "ca";
            case "cape verde":
                return "cv";
            case "colombia":
                return "co";
            case "croatia":
                return "hr";
            case "dr congo":
            case "congo dr":
            case "democratic republic of the congo":
                return "cd";
            case "ecuador":
                return "ec";
            case "egypt":
                return "eg";
            case "england":
                return "gb-eng";
            case "france":
                return "fr";
            case "germany":
                return "de";
            case "ghana":
                return "gh";
            case "ivory coast":
            case "côte d'ivoire":
            case "cote d'ivoire":
                return "ci";
            case "japan":
                return "jp";
            case "mexico":
                return "mx";
            case "morocco":
                return "ma";
            case "netherlands":
                return "nl";
            case "norway":
                return "no";
            case "paraguay":
                return "py";
            case "portugal":
                return "pt";
            case "senegal":
                return "sn";
            case "south africa":
                return "za";
            case "spain":
                return "es";
            case "sweden":
                return "se";
            case "switzerland":
                return "ch";
            case "united states":
            case "usa":
            case "united states of america":
                return "us";

            case "algeria":
                return "dz";
            case "cameroon":
                return "cm";
            case "chile":
                return "cl";
            case "china":
                return "cn";
            case "costa rica":
                return "cr";
            case "denmark":
                return "dk";
            case "greece":
                return "gr";
            case "iran":
                return "ir";
            case "iraq":
                return "iq";
            case "italy":
                return "it";
            case "jamaica":
                return "jm";
            case "korea republic":
            case "south korea":
            case "korea":
                return "kr";
            case "new zealand":
                return "nz";
            case "nigeria":
                return "ng";
            case "panama":
                return "pa";
            case "peru":
                return "pe";
            case "poland":
                return "pl";
            case "qatar":
                return "qa";
            case "romania":
                return "ro";
            case "saudi arabia":
                return "sa";
            case "scotland":
                return "gb-sct";
            case "serbia":
                return "rs";
            case "tunisia":
                return "tn";
            case "turkey":
            case "türkiye":
                return "tr";
            case "ukraine":
                return "ua";
            case "uruguay":
                return "uy";
            case "venezuela":
                return "ve";
            case "wales":
                return "gb-wls";

            default:
                return "";
        }
    }
}
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

public class ZafronixRepository {

    private final ZafronixApiService apiService;

    private static final Map<Integer, JsonObject> matchCache = new HashMap<>();

    public ZafronixRepository() {
        apiService = ZafronixClient.getApiService();
    }

    public void getAllMatches(final FootballRepository.MatchCallback callback) {
        apiService.getMatches(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                handleMatchesResponse(response, callback);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng Zafronix matches: " + t.getMessage());
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

    public void getBracketStage(final String stageKey, final FootballRepository.MatchCallback callback) {
        apiService.getBracket(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không tải được bracket. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    List<Match> matches = parseBracketStage(response.body(), stageKey);
                    callback.onSuccess(matches);
                } catch (Exception e) {
                    callback.onError("Lỗi xử lý bracket: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng bracket: " + t.getMessage());
            }
        });
    }

    public void getStandingsText(final FootballRepository.DetailCallback callback) {
        apiService.getStandings(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không tải được bảng đấu. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    callback.onSuccess(convertStandingsToText(response.body()));
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
        apiService.getStadiums(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không tải được sân vận động. Mã lỗi: " + response.code());
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

    public void getLineupsText(final int matchNo, final FootballRepository.DetailCallback callback) {
        getMatchObject(matchNo, new RawMatchCallback() {
            @Override
            public void onSuccess(JsonObject match) {
                callback.onSuccess(convertLineupsToText(match));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getEventsText(final int matchNo, final FootballRepository.DetailCallback callback) {
        getMatchObject(matchNo, new RawMatchCallback() {
            @Override
            public void onSuccess(JsonObject match) {
                callback.onSuccess(convertEventsToText(match));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getStatsText(final int matchNo, final FootballRepository.DetailCallback callback) {
        getMatchObject(matchNo, new RawMatchCallback() {
            @Override
            public void onSuccess(JsonObject match) {
                callback.onSuccess(convertStatsToText(match));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void getMatchObject(final int matchNo, final RawMatchCallback callback) {
        if (matchCache.containsKey(matchNo)) {
            callback.onSuccess(matchCache.get(matchNo));
            return;
        }

        apiService.getMatches(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không tải được chi tiết trận. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    List<JsonObject> objects = extractObjects(response.body());

                    for (JsonObject obj : objects) {
                        int no = getIntAny(obj, -1, "matchNo", "match_no", "number");

                        if (no > 0) {
                            matchCache.put(no, obj);
                        }
                    }

                    if (matchCache.containsKey(matchNo)) {
                        callback.onSuccess(matchCache.get(matchNo));
                    } else {
                        callback.onError("Không tìm thấy dữ liệu chi tiết cho trận số " + matchNo);
                    }
                } catch (Exception e) {
                    callback.onError("Lỗi xử lý chi tiết trận: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng chi tiết trận: " + t.getMessage());
            }
        });
    }

    private void handleMatchesResponse(Response<JsonElement> response, FootballRepository.MatchCallback callback) {
        if (!response.isSuccessful() || response.body() == null) {
            callback.onError("Không tải được matches. Mã lỗi: " + response.code());
            return;
        }

        try {
            List<Match> matches = convertMatches(response.body());

            if (matches.isEmpty()) {
                String preview = new GsonBuilder().setPrettyPrinting().create().toJson(response.body());

                if (preview.length() > 500) {
                    preview = preview.substring(0, 500);
                }

                callback.onError("API có dữ liệu nhưng app chưa đọc được matches. JSON mẫu: " + preview);
                return;
            }

            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match a, Match b) {
                    return Integer.compare(a.getFixtureId(), b.getFixtureId());
                }
            });

            callback.onSuccess(matches);
        } catch (Exception e) {
            callback.onError("Lỗi xử lý matches: " + e.getMessage());
        }
    }

    private List<Match> convertMatches(JsonElement root) {
        List<Match> result = new ArrayList<>();
        List<JsonObject> objects = extractObjects(root);

        for (JsonObject obj : objects) {
            int matchNo = getIntAny(obj, -1, "matchNo", "match_no", "number");

            if (matchNo <= 0) {
                matchNo = Math.abs(getStringAny(obj, "id", "matchId").hashCode());
            }

            matchCache.put(matchNo, obj);

            String home = getStringAny(obj, "homeTeam", "home", "home_team");
            String away = getStringAny(obj, "awayTeam", "away", "away_team");

            if (home.isEmpty()) home = "TBD";
            if (away.isEmpty()) away = "TBD";

            Integer hs = getNullableInt(obj, "homeScore");
            Integer as = getNullableInt(obj, "awayScore");

            int homeScore = hs == null ? 0 : hs;
            int awayScore = as == null ? 0 : as;

            String date = getDateText(obj);
            String status = getStatusText(obj);
            String round = getRoundText(getStringAny(obj, "stage"));
            String stadium = getStringAny(obj, "stadium");
            String city = getStringAny(obj, "city");

            String venue = stadium;
            if (!city.isEmpty()) {
                venue = stadium + " - " + city;
            }

            String homeLogo = getFlagUrl(home);
            String awayLogo = getFlagUrl(away);

            Match match = new Match(
                    matchNo,
                    "FIFA World Cup 2026",
                    home,
                    away,
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

            result.add(match);
        }

        return result;
    }

    private List<Match> parseBracketStage(JsonElement root, String stageKey) {
        List<Match> result = new ArrayList<>();

        if (root == null || !root.isJsonObject()) {
            return result;
        }

        JsonObject obj = root.getAsJsonObject();

        if (!obj.has("stages") || !obj.get("stages").isJsonObject()) {
            return result;
        }

        JsonObject stages = obj.get("stages").getAsJsonObject();

        if ("final".equals(stageKey)) {
            if (stages.has("third_place")) {
                result.addAll(parseBracketArray(stages.get("third_place")));
            }

            if (stages.has("final")) {
                result.addAll(parseBracketArray(stages.get("final")));
            }

            return result;
        }

        if (!stages.has(stageKey)) {
            return result;
        }

        return parseBracketArray(stages.get(stageKey));
    }

    private List<Match> parseBracketArray(JsonElement element) {
        List<Match> result = new ArrayList<>();

        if (element == null || !element.isJsonArray()) {
            return result;
        }

        JsonArray arr = element.getAsJsonArray();

        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;

            JsonObject obj = e.getAsJsonObject();

            int matchNo = getIntAny(obj, -1, "matchNo", "match_no", "number");

            if (matchNo <= 0) {
                matchNo = Math.abs(getStringAny(obj, "matchId", "id").hashCode());
            }

            String home = getStringAny(obj, "home");
            String away = getStringAny(obj, "away");

            if (home.isEmpty()) home = "TBD";
            if (away.isEmpty()) away = "TBD";

            Integer hs = getNullableInt(obj, "homeScore");
            Integer as = getNullableInt(obj, "awayScore");

            int homeScore = hs == null ? 0 : hs;
            int awayScore = as == null ? 0 : as;

            String round = getRoundText(getStringAny(obj, "stage"));

            String stadium = getStringAny(obj, "stadium");
            String city = getStringAny(obj, "city");

            String venue = stadium;
            if (!city.isEmpty()) {
                venue = stadium + " - " + city;
            }

            String date = getStringAny(obj, "kickoffUtc");
            String status = (hs == null || as == null) ? "Scheduled" : "Finished";

            Match match = new Match(
                    matchNo,
                    "FIFA World Cup 2026",
                    home,
                    away,
                    homeScore,
                    awayScore,
                    date,
                    status,
                    getFlagUrl(home),
                    getFlagUrl(away),
                    round,
                    date,
                    venue
            );

            result.add(match);
        }

        return result;
    }

    private String convertLineupsToText(JsonObject match) {
        if (!match.has("lineups") || !match.get("lineups").isJsonObject()) {
            return "Trận này chưa có dữ liệu đội hình.";
        }

        String home = getStringAny(match, "homeTeam");
        String away = getStringAny(match, "awayTeam");

        JsonObject lineups = match.get("lineups").getAsJsonObject();

        StringBuilder sb = new StringBuilder();

        sb.append("ĐỘI HÌNH\n\n");

        if (lineups.has("home")) {
            sb.append(home).append("\n");
            sb.append(formatPlayers(lineups.get("home")));
            sb.append("\n");
        }

        if (lineups.has("away")) {
            sb.append(away).append("\n");
            sb.append(formatPlayers(lineups.get("away")));
        }

        return sb.toString();
    }

    private String formatPlayers(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return "Không có dữ liệu.\n";
        }

        JsonArray arr = element.getAsJsonArray();

        StringBuilder starters = new StringBuilder();
        StringBuilder subs = new StringBuilder();

        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;

            JsonObject p = e.getAsJsonObject();

            String name = getStringAny(p, "player", "name");
            String number = getStringAny(p, "number");
            String position = getStringAny(p, "position");
            String starter = getStringAny(p, "starter");
            String captain = getStringAny(p, "captain");

            String row = "• ";

            if (!number.isEmpty()) {
                row += "#" + number + " ";
            }

            row += name;

            if (!position.isEmpty()) {
                row += " (" + position + ")";
            }

            if ("true".equalsIgnoreCase(captain)) {
                row += " [C]";
            }

            row += "\n";

            if ("true".equalsIgnoreCase(starter)) {
                starters.append(row);
            } else {
                subs.append(row);
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Đá chính:\n");
        sb.append(starters.length() == 0 ? "Chưa có.\n" : starters.toString());

        sb.append("Dự bị:\n");
        sb.append(subs.length() == 0 ? "Chưa có.\n" : subs.toString());

        return sb.toString();
    }

    private String convertEventsToText(JsonObject match) {
        if (!match.has("goals") || !match.get("goals").isJsonArray()) {
            return "Trận này chưa có diễn biến bàn thắng.";
        }

        JsonArray goals = match.get("goals").getAsJsonArray();

        if (goals.size() == 0) {
            return "Trận này chưa có bàn thắng.";
        }

        String home = getStringAny(match, "homeTeam");
        String away = getStringAny(match, "awayTeam");

        StringBuilder sb = new StringBuilder();

        sb.append("DIỄN BIẾN BÀN THẮNG\n\n");

        for (JsonElement e : goals) {
            if (!e.isJsonObject()) continue;

            JsonObject g = e.getAsJsonObject();

            String minute = getStringAny(g, "minute");
            String team = getStringAny(g, "team");
            String scorer = getStringAny(g, "scorer");

            String teamName = team;

            if ("home".equalsIgnoreCase(team)) {
                teamName = home;
            } else if ("away".equalsIgnoreCase(team)) {
                teamName = away;
            }

            sb.append(minute).append("' ");
            sb.append(teamName).append(" - ");
            sb.append(scorer).append("\n");
        }

        return sb.toString();
    }

    private String convertStatsToText(JsonObject match) {
        StringBuilder sb = new StringBuilder();

        sb.append("THỐNG KÊ / THÔNG TIN TRẬN\n\n");

        sb.append("Trận: ")
                .append(getStringAny(match, "homeTeam"))
                .append(" vs ")
                .append(getStringAny(match, "awayTeam"))
                .append("\n");

        sb.append("Tỉ số: ")
                .append(getStringAny(match, "result"))
                .append("\n");

        sb.append("Ngày: ")
                .append(getStringAny(match, "date"))
                .append(" ")
                .append(getStringAny(match, "kickoff"))
                .append("\n");

        sb.append("Sân: ")
                .append(getStringAny(match, "stadium"))
                .append(" - ")
                .append(getStringAny(match, "city"))
                .append("\n");

        String attendance = getStringAny(match, "attendance");
        if (!attendance.isEmpty()) {
            sb.append("Khán giả: ").append(attendance).append("\n");
        }

        if (match.has("referee") && match.get("referee").isJsonObject()) {
            JsonObject ref = match.get("referee").getAsJsonObject();

            sb.append("Trọng tài: ")
                    .append(getStringAny(ref, "name"));

            String country = getStringAny(ref, "country");
            if (!country.isEmpty()) {
                sb.append(" (").append(country).append(")");
            }

            sb.append("\n");
        }

        if (match.has("weather") && match.get("weather").isJsonObject()) {
            JsonObject w = match.get("weather").getAsJsonObject();

            sb.append("\nThời tiết:\n");
            sb.append("Nhiệt độ: ").append(getStringAny(w, "tempC")).append("°C\n");
            sb.append("Độ ẩm: ").append(getStringAny(w, "humidityPct")).append("%\n");
            sb.append("Mưa: ").append(getStringAny(w, "precipitationMm")).append(" mm\n");
            sb.append("Gió: ").append(getStringAny(w, "windKmh")).append(" km/h\n");
        }

        return sb.toString();
    }

    private String convertStandingsToText(JsonElement root) {
        String pretty = new GsonBuilder().setPrettyPrinting().create().toJson(root);

        if (pretty.length() > 8000) {
            pretty = pretty.substring(0, 8000) + "\n...";
        }

        return "BẢNG ĐẤU WORLD CUP 2026\n\n" + pretty;
    }

    private String convertStadiumsToText(JsonElement root) {
        List<JsonObject> stadiums = extractObjects(root);

        if (stadiums.isEmpty()) {
            return new GsonBuilder().setPrettyPrinting().create().toJson(root);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("SÂN VẬN ĐỘNG WORLD CUP 2026\n\n");

        for (JsonObject s : stadiums) {
            String name = getStringAny(s, "name", "name_en", "stadium");
            String city = getStringAny(s, "city");
            String country = getStringAny(s, "country");
            String capacity = getStringAny(s, "capacity");

            sb.append("• ").append(name).append("\n");

            if (!city.isEmpty()) {
                sb.append("  Thành phố: ").append(city).append("\n");
            }

            if (!country.isEmpty()) {
                sb.append("  Quốc gia: ").append(country).append("\n");
            }

            if (!capacity.isEmpty()) {
                sb.append("  Sức chứa: ").append(capacity).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private String getDateText(JsonObject obj) {
        String date = getStringAny(obj, "date");
        String kickoff = getStringAny(obj, "kickoff");
        String kickoffUtc = getStringAny(obj, "kickoffUtc");

        if (!date.isEmpty() && !kickoff.isEmpty()) {
            return date + " " + kickoff;
        }

        if (!kickoffUtc.isEmpty()) {
            return kickoffUtc;
        }

        return date;
    }

    private String getStatusText(JsonObject obj) {
        String result = getStringAny(obj, "result");
        Integer hs = getNullableInt(obj, "homeScore");
        Integer as = getNullableInt(obj, "awayScore");

        if (!result.isEmpty() || (hs != null && as != null)) {
            return "Finished";
        }

        return "Scheduled";
    }

    private String getRoundText(String stage) {
        if (stage == null) {
            return "World Cup 2026";
        }

        String value = stage.toLowerCase(Locale.ROOT);

        if (value.contains("group_a")) return "Group A";
        if (value.contains("group_b")) return "Group B";
        if (value.contains("group_c")) return "Group C";
        if (value.contains("group_d")) return "Group D";
        if (value.contains("group_e")) return "Group E";
        if (value.contains("group_f")) return "Group F";
        if (value.contains("group_g")) return "Group G";
        if (value.contains("group_h")) return "Group H";
        if (value.contains("group_i")) return "Group I";
        if (value.contains("group_j")) return "Group J";
        if (value.contains("group_k")) return "Group K";
        if (value.contains("group_l")) return "Group L";

        if (value.contains("round_of_32") || value.contains("r32")) return "Round of 32";
        if (value.contains("round_of_16") || value.contains("r16")) return "Round of 16";
        if (value.contains("quarter")) return "Quarter-finals";
        if (value.contains("semi")) return "Semi-finals";
        if (value.contains("third")) return "3rd Place Final";
        if (value.contains("final")) return "Final";

        return stage;
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
                    "data", "matches", "games", "stadiums", "teams", "players",
                    "response", "result", "items", "records"
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

    private int getIntAny(JsonObject obj, int defaultValue, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    return obj.get(key).getAsInt();
                }
            } catch (Exception ignored) {
            }
        }

        return defaultValue;
    }

    private Integer getNullableInt(JsonObject obj, String key) {
        try {
            if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsInt();
            }
        } catch (Exception ignored) {
        }

        return null;
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
                    "name",
                    "name_en",
                    "player",
                    "team",
                    "country",
                    "title",
                    "code");

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
        if (teamName == null) return "";

        String name = teamName.trim().toLowerCase(Locale.ROOT);

        switch (name) {
            case "argentina": return "ar";
            case "australia": return "au";
            case "austria": return "at";
            case "belgium": return "be";
            case "bosnia and herzegovina":
            case "bosnia": return "ba";
            case "brazil": return "br";
            case "cabo verde":
            case "cape verde": return "cv";
            case "canada": return "ca";
            case "colombia": return "co";
            case "croatia": return "hr";
            case "dr congo":
            case "congo dr":
            case "democratic republic of the congo": return "cd";
            case "ecuador": return "ec";
            case "egypt": return "eg";
            case "england": return "gb-eng";
            case "france": return "fr";
            case "germany": return "de";
            case "ghana": return "gh";
            case "côte d'ivoire":
            case "cote d'ivoire":
            case "ivory coast": return "ci";
            case "japan": return "jp";
            case "mexico": return "mx";
            case "morocco": return "ma";
            case "netherlands": return "nl";
            case "norway": return "no";
            case "paraguay": return "py";
            case "portugal": return "pt";
            case "senegal": return "sn";
            case "south africa": return "za";
            case "spain": return "es";
            case "sweden": return "se";
            case "switzerland": return "ch";
            case "united states":
            case "usa":
            case "united states of america": return "us";
            case "algeria": return "dz";
            case "cameroon": return "cm";
            case "chile": return "cl";
            case "costa rica": return "cr";
            case "denmark": return "dk";
            case "iran": return "ir";
            case "italy": return "it";
            case "korea republic":
            case "south korea": return "kr";
            case "new zealand": return "nz";
            case "nigeria": return "ng";
            case "panama": return "pa";
            case "peru": return "pe";
            case "poland": return "pl";
            case "qatar": return "qa";
            case "saudi arabia": return "sa";
            case "scotland": return "gb-sct";
            case "serbia": return "rs";
            case "tunisia": return "tn";
            case "turkey":
            case "türkiye": return "tr";
            case "ukraine": return "ua";
            case "uruguay": return "uy";
            case "wales": return "gb-wls";
            default: return "";
        }
    }

    private interface RawMatchCallback {
        void onSuccess(JsonObject match);
        void onError(String message);
    }
}
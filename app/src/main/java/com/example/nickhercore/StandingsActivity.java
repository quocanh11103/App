package com.example.nickhercore;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StandingsActivity extends AppCompatActivity {

    private LinearLayout root;
    private LinearLayout boardLayout;
    private LinearLayout groupGrid;
    private LinearLayout bracketBoard;
    private TextView statusText;

    private final Map<String, Map<String, TeamStanding>> groupStandings = new LinkedHashMap<>();
    private final Map<String, List<KnockoutMatch>> knockoutStages = new LinkedHashMap<>();

    private final String[] groupLetters = {
            "A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initEmptyGroups();
        createLayout();
        loadGroupStageFromMatches();
        loadKnockoutBracket();
    }

    private void initEmptyGroups() {
        groupStandings.clear();

        for (String g : groupLetters) {
            groupStandings.put(g, new LinkedHashMap<>());
        }
    }

    private void createLayout() {
        ScrollView verticalScroll = new ScrollView(this);
        verticalScroll.setBackgroundColor(Color.parseColor("#07111F"));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(26), dp(14), dp(16));

        verticalScroll.addView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("←", 30, Color.parseColor("#00D68F"), false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(42), dp(48)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        TextView app = text("NickherCore", 25, Color.WHITE, true);
        TextView sub = text("World Cup 2026 • Bảng đấu & Vòng loại trực tiếp", 13, Color.parseColor("#B8C7D9"), false);

        titleBox.addView(app);
        titleBox.addView(sub);

        top.addView(titleBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView trophy = text("🏆", 30, Color.parseColor("#00D68F"), false);
        trophy.setGravity(Gravity.CENTER);
        top.addView(trophy, new LinearLayout.LayoutParams(dp(52), dp(52)));

        root.addView(top);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(16), 0, dp(12));

        tabs.addView(chip("Tất cả", true));
        tabs.addView(chip("Vòng bảng", false));
        tabs.addView(chip("Knock-out", false));
        tabs.addView(chip("Lịch đấu", false));

        root.addView(tabs);

        statusText = text("Đang tải dữ liệu từ API lịch đấu...", 14, Color.parseColor("#00D68F"), false);
        statusText.setPadding(0, 0, 0, dp(12));
        root.addView(statusText);

        HorizontalScrollView horizontalScroll = new HorizontalScrollView(this);
        horizontalScroll.setHorizontalScrollBarEnabled(true);

        boardLayout = new LinearLayout(this);
        boardLayout.setOrientation(LinearLayout.HORIZONTAL);
        boardLayout.setPadding(dp(12), dp(12), dp(12), dp(12));
        boardLayout.setBackground(roundBg("#081827", "#26384F", 1, 18));

        LinearLayout groupSection = new LinearLayout(this);
        groupSection.setOrientation(LinearLayout.VERTICAL);
        groupSection.setPadding(dp(8), dp(8), dp(18), dp(8));

        TextView groupTitle = text("🛡  VÒNG BẢNG", 20, Color.parseColor("#00D68F"), true);
        groupTitle.setPadding(0, 0, 0, dp(10));
        groupSection.addView(groupTitle);

        groupGrid = new LinearLayout(this);
        groupGrid.setOrientation(LinearLayout.VERTICAL);
        groupSection.addView(groupGrid);

        boardLayout.addView(groupSection, new LinearLayout.LayoutParams(dp(690), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout divider = new LinearLayout(this);
        divider.setBackgroundColor(Color.parseColor("#26384F"));
        boardLayout.addView(divider, new LinearLayout.LayoutParams(dp(1), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout knockoutSection = new LinearLayout(this);
        knockoutSection.setOrientation(LinearLayout.VERTICAL);
        knockoutSection.setPadding(dp(18), dp(8), dp(8), dp(8));

        TextView knockoutTitle = text("🏆  VÒNG LOẠI TRỰC TIẾP", 20, Color.parseColor("#00D68F"), true);
        knockoutTitle.setPadding(0, 0, 0, dp(10));
        knockoutSection.addView(knockoutTitle);

        bracketBoard = new LinearLayout(this);
        bracketBoard.setOrientation(LinearLayout.HORIZONTAL);
        knockoutSection.addView(bracketBoard);

        boardLayout.addView(knockoutSection, new LinearLayout.LayoutParams(dp(900), LinearLayout.LayoutParams.WRAP_CONTENT));

        horizontalScroll.addView(boardLayout);
        root.addView(horizontalScroll);

        TextView footer = text("📅  12 BẢNG ĐẤU     👥  48 ĐỘI TUYỂN     ⚽  104 TRẬN ĐẤU     🌎  UNITED 2026", 13, Color.parseColor("#B8C7D9"), true);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(12), 0, 0);
        root.addView(footer);

        renderGroups();
        renderBracket();

        setContentView(verticalScroll);
    }

    private TextView chip(String label, boolean active) {
        TextView tv = text(label, 14, active ? Color.parseColor("#07111F") : Color.WHITE, true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(18), 0, dp(18), 0);
        tv.setBackground(active
                ? roundBg("#6FFFD2", "#6FFFD2", 0, 18)
                : roundBg("#10233A", "#26384F", 1, 18));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(44)
        );
        params.setMargins(0, 0, dp(8), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private void loadGroupStageFromMatches() {
        ZafronixClient.getApiService().getMatches(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    statusText.setText("Không tải được lịch đấu. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    initEmptyGroups();
                    parseMatchesForGroupTables(response.body());
                    renderGroups();
                    statusText.setText("Đã tải vòng bảng từ matches?year=2026");
                } catch (Exception e) {
                    statusText.setText("Lỗi xử lý vòng bảng: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                statusText.setText("Lỗi mạng vòng bảng: " + t.getMessage());
            }
        });
    }

    private void loadKnockoutBracket() {
        ZafronixClient.getApiService().getBracket(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                try {
                    knockoutStages.clear();
                    parseBracket(response.body());
                    renderBracket();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                // Không làm văng app nếu bracket lỗi.
            }
        });
    }

    private void parseMatchesForGroupTables(JsonElement rootElement) {
        List<JsonObject> matchObjects = new ArrayList<>();
        collectMatchObjects(rootElement, matchObjects);

        for (JsonObject match : matchObjects) {
            String stage = getStringAny(match, "stage", "round", "group");

            if (!isGroupStage(stage)) {
                continue;
            }

            String groupLetter = getGroupLetter(stage);

            if (groupLetter.isEmpty()) {
                continue;
            }

            String home = getStringAny(match, "homeTeam", "home", "home_name");
            String away = getStringAny(match, "awayTeam", "away", "away_name");

            if (home.isEmpty() || away.isEmpty()) {
                continue;
            }

            TeamStanding homeRow = getOrCreateTeam(groupLetter, home);
            TeamStanding awayRow = getOrCreateTeam(groupLetter, away);

            Integer homeScore = getNullableInt(match, "homeScore", "home_score", "goalsHome");
            Integer awayScore = getNullableInt(match, "awayScore", "away_score", "goalsAway");

            if (homeScore == null || awayScore == null) {
                continue;
            }

            homeRow.played++;
            awayRow.played++;

            homeRow.goalsFor += homeScore;
            homeRow.goalsAgainst += awayScore;

            awayRow.goalsFor += awayScore;
            awayRow.goalsAgainst += homeScore;

            if (homeScore > awayScore) {
                homeRow.won++;
                awayRow.lost++;
                homeRow.points += 3;
            } else if (homeScore < awayScore) {
                awayRow.won++;
                homeRow.lost++;
                awayRow.points += 3;
            } else {
                homeRow.draw++;
                awayRow.draw++;
                homeRow.points += 1;
                awayRow.points += 1;
            }
        }
    }

    private void collectMatchObjects(JsonElement element, List<JsonObject> result) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                collectMatchObjects(e, result);
            }
            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        JsonObject obj = element.getAsJsonObject();

        boolean looksLikeMatch =
                obj.has("homeTeam") || obj.has("awayTeam")
                        || obj.has("home") || obj.has("away")
                        || obj.has("matchNo");

        if (looksLikeMatch) {
            result.add(obj);
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonArray() || child.isJsonObject())) {
                collectMatchObjects(child, result);
            }
        }
    }

    private TeamStanding getOrCreateTeam(String groupLetter, String teamName) {
        Map<String, TeamStanding> group = groupStandings.get(groupLetter);

        if (group == null) {
            group = new LinkedHashMap<>();
            groupStandings.put(groupLetter, group);
        }

        if (!group.containsKey(teamName)) {
            TeamStanding row = new TeamStanding();
            row.team = teamName;
            group.put(teamName, row);
        }

        return group.get(teamName);
    }

    private boolean isGroupStage(String stage) {
        if (stage == null) {
            return false;
        }

        String s = stage.toLowerCase(Locale.ROOT);
        return s.startsWith("group_") || s.startsWith("group ");
    }

    private String getGroupLetter(String stage) {
        if (stage == null) {
            return "";
        }

        String s = stage.toLowerCase(Locale.ROOT).trim();

        if (s.contains("group_a") || s.contains("group a")) return "A";
        if (s.contains("group_b") || s.contains("group b")) return "B";
        if (s.contains("group_c") || s.contains("group c")) return "C";
        if (s.contains("group_d") || s.contains("group d")) return "D";
        if (s.contains("group_e") || s.contains("group e")) return "E";
        if (s.contains("group_f") || s.contains("group f")) return "F";
        if (s.contains("group_g") || s.contains("group g")) return "G";
        if (s.contains("group_h") || s.contains("group h")) return "H";
        if (s.contains("group_i") || s.contains("group i")) return "I";
        if (s.contains("group_j") || s.contains("group j")) return "J";
        if (s.contains("group_k") || s.contains("group k")) return "K";
        if (s.contains("group_l") || s.contains("group l")) return "L";

        return "";
    }

    private void parseBracket(JsonElement root) {
        JsonObject stagesObject = findStagesObject(root);

        if (stagesObject == null) {
            return;
        }

        putStage(stagesObject, "round_of_32", "Vòng 32");
        putStage(stagesObject, "r32", "Vòng 32");

        putStage(stagesObject, "round_of_16", "Vòng 16 đội");
        putStage(stagesObject, "r16", "Vòng 16 đội");

        putStage(stagesObject, "quarter_final", "Tứ kết");
        putStage(stagesObject, "quarter_finals", "Tứ kết");
        putStage(stagesObject, "quarterfinals", "Tứ kết");
        putStage(stagesObject, "qf", "Tứ kết");

        putStage(stagesObject, "semi_final", "Bán kết");
        putStage(stagesObject, "semi_finals", "Bán kết");
        putStage(stagesObject, "semifinals", "Bán kết");
        putStage(stagesObject, "sf", "Bán kết");

        putStage(stagesObject, "final", "Chung kết");
        putStage(stagesObject, "third_place", "Tranh hạng 3");
    }

    private JsonObject findStagesObject(JsonElement root) {
        if (root == null || root.isJsonNull()) {
            return null;
        }

        if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();

            if (obj.has("stages") && obj.get("stages").isJsonObject()) {
                return obj.get("stages").getAsJsonObject();
            }

            return obj;
        }

        return null;
    }

    private void putStage(JsonObject stagesObject, String key, String title) {
        if (!stagesObject.has(key)) {
            return;
        }

        if (!knockoutStages.containsKey(title)) {
            knockoutStages.put(title, new ArrayList<>());
        }

        collectKnockoutMatches(stagesObject.get(key), knockoutStages.get(title));
    }

    private void collectKnockoutMatches(JsonElement element, List<KnockoutMatch> list) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();

            for (JsonElement e : arr) {
                collectKnockoutMatches(e, list);
            }

            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        JsonObject obj = element.getAsJsonObject();

        String home = getStringAny(obj, "home", "homeTeam", "homeRef");
        String away = getStringAny(obj, "away", "awayTeam", "awayRef");

        if (!home.isEmpty() && !away.isEmpty()) {
            KnockoutMatch m = new KnockoutMatch();
            m.home = home;
            m.away = away;
            m.homeScore = getNullableInt(obj, "homeScore", "home_score");
            m.awayScore = getNullableInt(obj, "awayScore", "away_score");
            m.kickoff = getStringAny(obj, "kickoffUtc", "date", "kickoff");
            m.winner = getStringAny(obj, "winner");
            list.add(m);
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonArray() || child.isJsonObject())) {
                collectKnockoutMatches(child, list);
            }
        }
    }

    private void renderGroups() {
        if (groupGrid == null) {
            return;
        }

        groupGrid.removeAllViews();

        for (int row = 0; row < 4; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;

                if (index >= groupLetters.length) {
                    continue;
                }

                String letter = groupLetters[index];

                LinearLayout groupCard = groupCard(letter);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dp(210),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, dp(12), dp(14));

                rowLayout.addView(groupCard, params);
            }

            groupGrid.addView(rowLayout);
        }
    }

    private LinearLayout groupCard(String letter) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(roundBg("#10233A", "#26384F", 1, 12));

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView group = text(letter, 15, Color.WHITE, true);
        group.setGravity(Gravity.CENTER);
        group.setBackground(roundBg("#0B8F4D", "#0B8F4D", 0, 6));
        head.addView(group, new LinearLayout.LayoutParams(dp(28), dp(28)));

        TextView cols = text("      P   W   D   L   GD  Pts", 10, Color.parseColor("#B8C7D9"), true);
        head.addView(cols, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        card.addView(head);

        List<TeamStanding> rows = new ArrayList<>();

        Map<String, TeamStanding> groupMap = groupStandings.get(letter);

        if (groupMap != null) {
            rows.addAll(groupMap.values());
        }

        Collections.sort(rows, (a, b) -> {
            if (b.points != a.points) return b.points - a.points;
            if (b.goalDiff() != a.goalDiff()) return b.goalDiff() - a.goalDiff();
            if (b.goalsFor != a.goalsFor) return b.goalsFor - a.goalsFor;
            return a.team.compareToIgnoreCase(b.team);
        });

        if (rows.isEmpty()) {
            TextView empty = text("Chưa có dữ liệu bảng " + letter, 11, Color.parseColor("#8FA8C5"), false);
            empty.setPadding(0, dp(8), 0, 0);
            card.addView(empty);
            return card;
        }

        for (int i = 0; i < rows.size(); i++) {
            card.addView(teamRow(i + 1, rows.get(i)));
        }

        return card;
    }

    private LinearLayout teamRow(int rank, TeamStanding t) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        int rankColor = rank <= 2 ? Color.parseColor("#00D68F") : Color.WHITE;

        TextView rankView = text(String.valueOf(rank), 11, rankColor, true);
        rankView.setGravity(Gravity.CENTER);
        row.addView(rankView, new LinearLayout.LayoutParams(dp(18), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView flag = text(flagEmoji(t.team), 13, Color.WHITE, false);
        flag.setGravity(Gravity.CENTER);
        row.addView(flag, new LinearLayout.LayoutParams(dp(20), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView name = text(shortTeam(t.team), 11, Color.WHITE, rank <= 2);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        row.addView(num(t.played));
        row.addView(num(t.won));
        row.addView(num(t.draw));
        row.addView(num(t.lost));
        row.addView(numText(t.goalDiff() > 0 ? "+" + t.goalDiff() : String.valueOf(t.goalDiff())));
        row.addView(numText(String.valueOf(t.points)));

        return row;
    }

    private TextView num(int value) {
        return numText(String.valueOf(value));
    }

    private TextView numText(String value) {
        TextView tv = text(value, 10, Color.WHITE, false);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setLayoutParams(new LinearLayout.LayoutParams(dp(22), LinearLayout.LayoutParams.WRAP_CONTENT));
        return tv;
    }

    private void renderBracket() {
        if (bracketBoard == null) {
            return;
        }

        bracketBoard.removeAllViews();

        if (knockoutStages.isEmpty()) {
            String[] titles = {"Vòng 32", "Vòng 16 đội", "Tứ kết", "Bán kết", "Chung kết"};

            for (String title : titles) {
                bracketBoard.addView(stageColumn(title, new ArrayList<>()));
            }

            return;
        }

        String[] ordered = {"Vòng 32", "Vòng 16 đội", "Tứ kết", "Bán kết", "Chung kết", "Tranh hạng 3"};

        for (String title : ordered) {
            if (knockoutStages.containsKey(title)) {
                bracketBoard.addView(stageColumn(title, knockoutStages.get(title)));
            }
        }
    }

    private LinearLayout stageColumn(String title, List<KnockoutMatch> matches) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, 0, dp(14), 0);

        TextView head = text(title, 13, Color.WHITE, true);
        head.setGravity(Gravity.CENTER);
        head.setBackground(roundBg("#1A2736", "#26384F", 1, 8));
        col.addView(head, new LinearLayout.LayoutParams(dp(150), dp(34)));

        if (matches == null || matches.isEmpty()) {
            TextView empty = text("Chưa có dữ liệu", 11, Color.parseColor("#8FA8C5"), false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(18), 0, 0);
            col.addView(empty, new LinearLayout.LayoutParams(dp(150), LinearLayout.LayoutParams.WRAP_CONTENT));
            return col;
        }

        int topSpace = getStageTopSpace(title);

        if (topSpace > 0) {
            TextView spacer = new TextView(this);
            col.addView(spacer, new LinearLayout.LayoutParams(1, dp(topSpace)));
        }

        for (KnockoutMatch m : matches) {
            col.addView(matchCard(m));
        }

        return col;
    }

    private int getStageTopSpace(String title) {
        if ("Vòng 32".equals(title)) return 8;
        if ("Vòng 16 đội".equals(title)) return 48;
        if ("Tứ kết".equals(title)) return 108;
        if ("Bán kết".equals(title)) return 188;
        if ("Chung kết".equals(title)) return 270;
        if ("Tranh hạng 3".equals(title)) return 410;
        return 8;
    }

    private LinearLayout matchCard(KnockoutMatch match) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(7), dp(8), dp(7));
        card.setBackground(roundBg("#10233A", "#3A4759", 1, 10));

        TextView time = text(formatDate(match.kickoff), 10, Color.parseColor("#B8C7D9"), false);
        card.addView(time);

        card.addView(knockoutTeamRow(match.home, match.homeScore));
        card.addView(knockoutTeamRow(match.away, match.awayScore));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(150), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(10), 0, dp(12));
        card.setLayoutParams(params);

        return card;
    }

    private LinearLayout knockoutTeamRow(String team, Integer score) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = text(flagEmoji(team) + " " + shortTeam(team), 12, Color.WHITE, true);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView scoreText = text(score == null ? "-" : String.valueOf(score), 13, Color.WHITE, true);
        scoreText.setGravity(Gravity.RIGHT);
        row.addView(scoreText, new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT));

        return row;
    }

    private String getStringAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    JsonElement e = obj.get(key);

                    if (e.isJsonPrimitive()) {
                        String value = e.getAsString();

                        if (value != null && !value.trim().isEmpty()) {
                            return value.trim();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private Integer getNullableInt(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    return obj.get(key).getAsInt();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String formatDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "TBD";
        }

        try {
            if (raw.length() >= 16 && raw.contains("T")) {
                String date = raw.substring(0, 10);
                String time = raw.substring(11, 16);
                String[] parts = date.split("-");
                return parts[2] + "/" + parts[1] + " • " + time;
            }
        } catch (Exception ignored) {
        }

        return raw;
    }

    private String shortTeam(String name) {
        if (name == null) return "";

        String n = name.trim();

        if (n.length() <= 14) {
            return n;
        }

        if (n.equalsIgnoreCase("Bosnia and Herzegovina")) return "Bosnia";
        if (n.equalsIgnoreCase("Côte d'Ivoire")) return "Bờ Biển Ngà";
        if (n.equalsIgnoreCase("United States")) return "USA";
        if (n.equalsIgnoreCase("South Africa")) return "Nam Phi";
        if (n.equalsIgnoreCase("Cabo Verde")) return "Cabo Verde";

        return n.substring(0, 13) + ".";
    }

    private String flagEmoji(String team) {
        if (team == null) return "🏳";

        String n = team.trim().toLowerCase(Locale.ROOT);

        switch (n) {
            case "argentina": return "🇦🇷";
            case "australia": return "🇦🇺";
            case "austria": return "🇦🇹";
            case "belgium": return "🇧🇪";
            case "brazil": return "🇧🇷";
            case "canada": return "🇨🇦";
            case "colombia": return "🇨🇴";
            case "croatia": return "🇭🇷";
            case "ecuador": return "🇪🇨";
            case "egypt": return "🇪🇬";
            case "england": return "🏴";
            case "france": return "🇫🇷";
            case "germany": return "🇩🇪";
            case "ghana": return "🇬🇭";
            case "japan": return "🇯🇵";
            case "mexico": return "🇲🇽";
            case "morocco": return "🇲🇦";
            case "netherlands": return "🇳🇱";
            case "norway": return "🇳🇴";
            case "paraguay": return "🇵🇾";
            case "portugal": return "🇵🇹";
            case "senegal": return "🇸🇳";
            case "south africa": return "🇿🇦";
            case "spain": return "🇪🇸";
            case "sweden": return "🇸🇪";
            case "switzerland": return "🇨🇭";
            case "usa":
            case "united states": return "🇺🇸";
            case "uruguay": return "🇺🇾";
            case "wales": return "🏴";
            case "iran": return "🇮🇷";
            case "algeria": return "🇩🇿";
            case "côte d'ivoire": return "🇨🇮";
            case "ivory coast": return "🇨🇮";
            case "denmark": return "🇩🇰";
            case "poland": return "🇵🇱";
            case "romania": return "🇷🇴";
            case "turkiye":
            case "türkiye": return "🇹🇷";
            default: return "🏳";
        }
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value == null ? "" : value);
        tv.setTextSize(size);
        tv.setTextColor(color);

        if (bold) {
            tv.setTypeface(null, Typeface.BOLD);
        }

        return tv;
    }

    private GradientDrawable roundBg(String color, String stroke, int strokeWidth, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(color));
        gd.setCornerRadius(dp(radius));

        if (strokeWidth > 0) {
            gd.setStroke(dp(strokeWidth), Color.parseColor(stroke));
        }

        return gd;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class TeamStanding {
        String team;
        int played;
        int won;
        int draw;
        int lost;
        int goalsFor;
        int goalsAgainst;
        int points;

        int goalDiff() {
            return goalsFor - goalsAgainst;
        }
    }

    private static class KnockoutMatch {
        String home;
        String away;
        Integer homeScore;
        Integer awayScore;
        String kickoff;
        String winner;
    }
}
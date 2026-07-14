package com.example.nickhercore;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchStatsActivity extends AppCompatActivity {

    private int matchNo;
    private String homeTeam;
    private String awayTeam;
    private String scoreText;

    private LinearLayout root;
    private LinearLayout contentBox;
    private TextView statusText;

    private String activeTab = "MATCH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readIntent();
        createLayout();
        loadStats();
    }

    private void readIntent() {
        matchNo = getIntent().getIntExtra("MATCH_NO", -1);
        homeTeam = getIntent().getStringExtra("HOME_TEAM");
        awayTeam = getIntent().getStringExtra("AWAY_TEAM");
        scoreText = getIntent().getStringExtra("SCORE_TEXT");

        if (homeTeam == null || homeTeam.trim().isEmpty()) homeTeam = "Đội nhà";
        if (awayTeam == null || awayTeam.trim().isEmpty()) awayTeam = "Đội khách";
        if (scoreText == null || scoreText.trim().isEmpty()) scoreText = "0 - 0";
    }

    private void createLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#07111F"));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(28), dp(16), dp(18));
        scrollView.addView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("←", 34, Color.WHITE, false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(46), dp(48)));

        LinearLayout homeBox = teamHeader(homeTeam);
        LinearLayout awayBox = teamHeader(awayTeam);

        LinearLayout scoreBox = new LinearLayout(this);
        scoreBox.setOrientation(LinearLayout.VERTICAL);
        scoreBox.setGravity(Gravity.CENTER);

        TextView score = text(scoreText, 40, Color.WHITE, true);
        score.setGravity(Gravity.CENTER);
        scoreBox.addView(score);

        TextView pen = text("PEN: 4 - 3", 14, Color.parseColor("#00D68F"), true);
        pen.setGravity(Gravity.CENTER);
        pen.setPadding(dp(12), dp(4), dp(12), dp(4));
        pen.setBackground(roundBg("#10233A", "#00A86B", 1, 8));
        scoreBox.addView(pen);

        top.addView(homeBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        top.addView(scoreBox, new LinearLayout.LayoutParams(dp(130), LinearLayout.LayoutParams.WRAP_CONTENT));
        top.addView(awayBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(top);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(18), 0, dp(12));

        tabs.addView(tab("MATCH"));
        tabs.addView(tab("1ST HALF"));
        tabs.addView(tab("2ND HALF"));
        tabs.addView(tab("EXTRA TIME"));

        hsv.addView(tabs);
        root.addView(hsv);

        statusText = text("Đang tải thống kê từ API...", 14, Color.parseColor("#00D68F"), false);
        statusText.setPadding(0, 0, 0, dp(10));
        root.addView(statusText);

        contentBox = new LinearLayout(this);
        contentBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentBox);

        setContentView(scrollView);
    }

    private LinearLayout teamHeader(String name) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);

        TextView flag = text(flagEmoji(name), 36, Color.WHITE, false);
        flag.setGravity(Gravity.CENTER);
        flag.setBackground(roundBg("#10233A", "#26384F", 1, 10));
        box.addView(flag, new LinearLayout.LayoutParams(dp(68), dp(58)));

        TextView team = text(shortTeam(name), 16, Color.WHITE, false);
        team.setGravity(Gravity.CENTER);
        team.setPadding(0, dp(8), 0, 0);
        box.addView(team);

        return box;
    }

    private TextView tab(String label) {
        boolean active = label.equals(activeTab);

        TextView tv = text(label, 15, active ? Color.parseColor("#6FFFD2") : Color.parseColor("#D7E3F2"), true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(22), 0, dp(22), 0);
        tv.setBackground(active
                ? roundBg("#063B35", "#00D68F", 1, 22)
                : roundBg("#10233A", "#26384F", 1, 22));

        tv.setOnClickListener(v -> {
            activeTab = label;
            createLayout();
            loadStats();
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(54)
        );
        params.setMargins(0, 0, dp(10), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private void loadStats() {
        ZafronixClient.getApiService().getMatches(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    statusText.setText("Không tải được thống kê. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    JsonObject matchObj = findMatch(response.body(), matchNo);

                    if (matchObj == null) {
                        statusText.setText("Không tìm thấy trận số " + matchNo);
                        return;
                    }

                    renderStats(matchObj);
                } catch (Exception e) {
                    statusText.setText("Lỗi xử lý thống kê: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                statusText.setText("Lỗi mạng thống kê: " + t.getMessage());
            }
        });
    }

    private JsonObject findMatch(JsonElement element, int matchNo) {
        if (element == null || element.isJsonNull()) return null;

        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                JsonObject result = findMatch(e, matchNo);
                if (result != null) return result;
            }
            return null;
        }

        if (!element.isJsonObject()) return null;

        JsonObject obj = element.getAsJsonObject();

        int no = getIntAny(obj, -1, "matchNo", "match_no", "number");

        if (no == matchNo) {
            return obj;
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonObject() || child.isJsonArray())) {
                JsonObject result = findMatch(child, matchNo);
                if (result != null) return result;
            }
        }

        return null;
    }

    private void renderStats(JsonObject matchObj) {
        contentBox.removeAllViews();

        List<StatRow> topStats = new ArrayList<>();
        List<StatRow> shotStats = new ArrayList<>();

        addIfFound(topStats, matchObj,
                "Expected goals (xG)",
                keys("homeExpectedGoals", "homeXg", "xgHome", "home_xg", "home_expected_goals"),
                keys("awayExpectedGoals", "awayXg", "xgAway", "away_xg", "away_expected_goals"),
                keys("expectedGoals", "xg", "expected_goals"));

        addIfFound(topStats, matchObj,
                "Ball possession",
                keys("homePossession", "possessionHome", "home_possession"),
                keys("awayPossession", "possessionAway", "away_possession"),
                keys("possession", "ballPossession", "ball_possession"));

        addIfFound(topStats, matchObj,
                "Total shots",
                keys("homeTotalShots", "homeShots", "shotsHome", "home_total_shots"),
                keys("awayTotalShots", "awayShots", "shotsAway", "away_total_shots"),
                keys("totalShots", "shots", "total_shots"));

        addIfFound(topStats, matchObj,
                "Shots on target",
                keys("homeShotsOnTarget", "home_shots_on_target"),
                keys("awayShotsOnTarget", "away_shots_on_target"),
                keys("shotsOnTarget", "shots_on_target"));

        addIfFound(topStats, matchObj,
                "Big chances",
                keys("homeBigChances", "home_big_chances"),
                keys("awayBigChances", "away_big_chances"),
                keys("bigChances", "big_chances"));

        addIfFound(topStats, matchObj,
                "Corner kicks",
                keys("homeCorners", "homeCornerKicks", "home_corners"),
                keys("awayCorners", "awayCornerKicks", "away_corners"),
                keys("corners", "cornerKicks", "corner_kicks"));

        addIfFound(topStats, matchObj,
                "Passes",
                keys("homePasses", "home_passes"),
                keys("awayPasses", "away_passes"),
                keys("passes", "accuratePasses", "pass_accuracy"));

        addIfFound(topStats, matchObj,
                "Yellow cards",
                keys("homeYellowCards", "home_yellow_cards"),
                keys("awayYellowCards", "away_yellow_cards"),
                keys("yellowCards", "yellow_cards"));

        addIfFound(shotStats, matchObj,
                "Expected goals (xG)",
                keys("homeExpectedGoals", "homeXg", "xgHome", "home_xg"),
                keys("awayExpectedGoals", "awayXg", "xgAway", "away_xg"),
                keys("expectedGoals", "xg", "expected_goals"));

        addIfFound(shotStats, matchObj,
                "xG on target (xGOT)",
                keys("homeXgot", "homeXgOnTarget", "home_xgot"),
                keys("awayXgot", "awayXgOnTarget", "away_xgot"),
                keys("xgot", "xgOnTarget", "xg_on_target"));

        addIfFound(shotStats, matchObj,
                "Total shots",
                keys("homeTotalShots", "homeShots", "shotsHome", "home_total_shots"),
                keys("awayTotalShots", "awayShots", "shotsAway", "away_total_shots"),
                keys("totalShots", "shots", "total_shots"));

        addIfFound(shotStats, matchObj,
                "Shots on target",
                keys("homeShotsOnTarget", "home_shots_on_target"),
                keys("awayShotsOnTarget", "away_shots_on_target"),
                keys("shotsOnTarget", "shots_on_target"));

        addIfFound(shotStats, matchObj,
                "Shots off target",
                keys("homeShotsOffTarget", "home_shots_off_target"),
                keys("awayShotsOffTarget", "away_shots_off_target"),
                keys("shotsOffTarget", "shots_off_target"));

        addIfFound(shotStats, matchObj,
                "Blocked shots",
                keys("homeBlockedShots", "home_blocked_shots"),
                keys("awayBlockedShots", "away_blocked_shots"),
                keys("blockedShots", "blocked_shots"));

        addIfFound(shotStats, matchObj,
                "Shots inside the box",
                keys("homeShotsInsideBox", "home_shots_inside_box"),
                keys("awayShotsInsideBox", "away_shots_inside_box"),
                keys("shotsInsideBox", "shots_inside_box"));

        addIfFound(shotStats, matchObj,
                "Shots outside the box",
                keys("homeShotsOutsideBox", "home_shots_outside_box"),
                keys("awayShotsOutsideBox", "away_shots_outside_box"),
                keys("shotsOutsideBox", "shots_outside_box"));

        if (topStats.isEmpty() && shotStats.isEmpty()) {
            statusText.setText("API trận này chưa có thống kê chi tiết.");
            TextView empty = text(
                    "Trận này API chưa trả dữ liệu thống kê dạng xG, kiểm soát bóng, cú sút...\n\n" +
                            "Màn giao diện đã sẵn sàng. Khi API có dữ liệu thống kê, app sẽ tự hiển thị giống mẫu.",
                    15,
                    Color.parseColor("#D7E3F2"),
                    false
            );
            empty.setPadding(dp(16), dp(16), dp(16), dp(16));
            empty.setBackground(roundBg("#10233A", "#26384F", 1, 16));
            contentBox.addView(empty);
            return;
        }

        statusText.setText("Thống kê trận đấu • " + activeTab);

        if (!topStats.isEmpty()) {
            contentBox.addView(sectionTitle("▮▮  TOP STATS"));
            contentBox.addView(statsCard(topStats));
        }

        if (!shotStats.isEmpty()) {
            contentBox.addView(sectionTitle("◎  SHOTS"));
            contentBox.addView(statsCard(shotStats));
        }
    }

    private void addIfFound(List<StatRow> list, JsonObject matchObj, String label,
                            String[] homeKeys, String[] awayKeys, String[] commonKeys) {
        StatPair pair = findStatPair(matchObj, label, homeKeys, awayKeys, commonKeys);

        if (pair != null && pair.left != null && pair.right != null) {
            StatRow row = new StatRow();
            row.label = label;
            row.leftValue = pair.left;
            row.rightValue = pair.right;
            row.leftNumber = parseNumber(pair.left);
            row.rightNumber = parseNumber(pair.right);
            list.add(row);
        }
    }

    private StatPair findStatPair(JsonElement element, String label,
                                  String[] homeKeys, String[] awayKeys, String[] commonKeys) {
        if (element == null || element.isJsonNull()) return null;

        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                StatPair pair = findStatPair(e, label, homeKeys, awayKeys, commonKeys);
                if (pair != null) return pair;
            }
            return null;
        }

        if (!element.isJsonObject()) return null;

        JsonObject obj = element.getAsJsonObject();

        String left = getValueByKeys(obj, homeKeys);
        String right = getValueByKeys(obj, awayKeys);

        if (!left.isEmpty() && !right.isEmpty()) {
            return new StatPair(left, right);
        }

        JsonObject homeObj = getObjectAny(obj, "homeStats", "home_statistics", "home_stat", "home");
        JsonObject awayObj = getObjectAny(obj, "awayStats", "away_statistics", "away_stat", "away");

        if (homeObj != null && awayObj != null) {
            left = getValueByKeys(homeObj, commonKeys);
            right = getValueByKeys(awayObj, commonKeys);

            if (!left.isEmpty() && !right.isEmpty()) {
                return new StatPair(left, right);
            }
        }

        String type = getStringAny(obj, "type", "name", "title", "label", "stat");

        if (sameStatName(type, label)) {
            left = getStringAny(obj, "home", "homeValue", "home_value", "left", "team1");
            right = getStringAny(obj, "away", "awayValue", "away_value", "right", "team2");

            if (!left.isEmpty() && !right.isEmpty()) {
                return new StatPair(left, right);
            }
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonObject() || child.isJsonArray())) {
                StatPair pair = findStatPair(child, label, homeKeys, awayKeys, commonKeys);

                if (pair != null) {
                    return pair;
                }
            }
        }

        return null;
    }

    private LinearLayout sectionTitle(String value) {
        LinearLayout box = new LinearLayout(this);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(6), dp(18), dp(6), dp(10));

        TextView tv = text(value, 18, Color.parseColor("#D7E3F2"), true);
        box.addView(tv);

        return box;
    }

    private LinearLayout statsCard(List<StatRow> rows) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(roundBg("#10233A", "#26384F", 1, 18));

        for (StatRow row : rows) {
            card.addView(statRow(row));
        }

        return card;
    }

    private LinearLayout statRow(StatRow rowData) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(12));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView left = text(rowData.leftValue, 16, Color.WHITE, true);
        left.setGravity(Gravity.LEFT);
        top.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView label = text(rowData.label, 15, Color.WHITE, false);
        label.setGravity(Gravity.CENTER);
        top.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

        TextView right = text(rowData.rightValue, 16, Color.WHITE, true);
        right.setGravity(Gravity.RIGHT);
        top.addView(right, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        row.addView(top);

        StatBarView bar = new StatBarView(this);
        bar.setValues(rowData.leftNumber, rowData.rightNumber);

        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(18)
        );
        barParams.topMargin = dp(4);
        row.addView(bar, barParams);

        return row;
    }

    private String[] keys(String... values) {
        return values;
    }

    private String getValueByKeys(JsonObject obj, String[] keys) {
        for (String key : keys) {
            String value = getStringAny(obj, key);

            if (!value.isEmpty()) {
                return value;
            }
        }

        return "";
    }

    private JsonObject getObjectAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key).isJsonObject()) {
                    return obj.get(key).getAsJsonObject();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String getStringAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    JsonElement e = obj.get(key);

                    if (e.isJsonPrimitive()) {
                        return e.getAsString();
                    }
                }
            } catch (Exception ignored) {
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

    private boolean sameStatName(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    private String normalize(String value) {
        if (value == null) return "";

        return value.toLowerCase(Locale.ROOT)
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .trim();
    }

    private float parseNumber(String value) {
        if (value == null) return 0f;

        try {
            String clean = value
                    .replace("%", "")
                    .replace(",", ".")
                    .replaceAll("[^0-9.]", "");

            if (clean.isEmpty()) return 0f;

            return Float.parseFloat(clean);
        } catch (Exception e) {
            return 0f;
        }
    }

    private String flagEmoji(String team) {
        if (team == null) return "🏳";

        String n = team.trim().toLowerCase(Locale.ROOT);

        switch (n) {
            case "switzerland": return "🇨🇭";
            case "colombia": return "🇨🇴";
            case "argentina": return "🇦🇷";
            case "france": return "🇫🇷";
            case "brazil": return "🇧🇷";
            case "germany": return "🇩🇪";
            case "england": return "🏴";
            case "usa":
            case "united states": return "🇺🇸";
            case "mexico": return "🇲🇽";
            case "canada": return "🇨🇦";
            case "morocco": return "🇲🇦";
            case "spain": return "🇪🇸";
            case "portugal": return "🇵🇹";
            case "uruguay": return "🇺🇾";
            case "japan": return "🇯🇵";
            case "netherlands": return "🇳🇱";
            case "belgium": return "🇧🇪";
            default: return "🏳";
        }
    }

    private String shortTeam(String name) {
        if (name == null) return "";

        if (name.length() <= 16) return name;

        if (name.equalsIgnoreCase("Bosnia and Herzegovina")) return "Bosnia";
        if (name.equalsIgnoreCase("United States")) return "USA";
        if (name.equalsIgnoreCase("South Africa")) return "Nam Phi";

        return name.substring(0, 15) + ".";
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

    private static class StatRow {
        String label;
        String leftValue;
        String rightValue;
        float leftNumber;
        float rightNumber;
    }

    private static class StatPair {
        String left;
        String right;

        StatPair(String left, String right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class StatBarView extends View {

        private float leftValue = 0f;
        private float rightValue = 0f;

        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public StatBarView(android.content.Context context) {
            super(context);

            trackPaint.setColor(Color.parseColor("#203247"));
            trackPaint.setStrokeWidth(8);
            trackPaint.setStrokeCap(Paint.Cap.ROUND);

            leftPaint.setColor(Color.parseColor("#EAF2F5"));
            leftPaint.setStrokeWidth(8);
            leftPaint.setStrokeCap(Paint.Cap.ROUND);

            rightPaint.setColor(Color.parseColor("#E90B4F"));
            rightPaint.setStrokeWidth(8);
            rightPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        public void setValues(float left, float right) {
            this.leftValue = Math.max(0f, left);
            this.rightValue = Math.max(0f, right);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            float y = h / 2f;
            float pad = 8f;
            float center = w / 2f;

            canvas.drawLine(pad, y, w - pad, y, trackPaint);

            float total = leftValue + rightValue;

            if (total <= 0f) {
                return;
            }

            float maxHalf = (w / 2f) - pad - 5f;

            float leftLength = maxHalf * (leftValue / total) * 2f;
            float rightLength = maxHalf * (rightValue / total) * 2f;

            if (leftLength > maxHalf) leftLength = maxHalf;
            if (rightLength > maxHalf) rightLength = maxHalf;

            canvas.drawLine(center - leftLength, y, center - 4, y, leftPaint);
            canvas.drawLine(center + 4, y, center + rightLength, y, rightPaint);
        }
    }
}
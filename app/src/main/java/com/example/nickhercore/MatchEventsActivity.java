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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchEventsActivity extends AppCompatActivity {

    private int matchNo;
    private String homeTeam;
    private String awayTeam;
    private String scoreText;
    private String statusTextValue;

    private LinearLayout root;
    private LinearLayout contentBox;
    private TextView loadingText;

    private boolean importantOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readIntent();
        createLayout();
        loadEvents();
    }

    private void readIntent() {
        matchNo = getIntent().getIntExtra("MATCH_NO", -1);
        homeTeam = getIntent().getStringExtra("HOME_TEAM");
        awayTeam = getIntent().getStringExtra("AWAY_TEAM");
        scoreText = getIntent().getStringExtra("SCORE_TEXT");
        statusTextValue = getIntent().getStringExtra("STATUS_TEXT");

        if (homeTeam == null || homeTeam.trim().isEmpty()) homeTeam = "Đội nhà";
        if (awayTeam == null || awayTeam.trim().isEmpty()) awayTeam = "Đội khách";
        if (scoreText == null || scoreText.trim().isEmpty()) scoreText = "0 - 0";
        if (statusTextValue == null || statusTextValue.trim().isEmpty()) statusTextValue = "MATCH";
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

        TextView status = text(statusTextValue, 12, Color.WHITE, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10), dp(4), dp(10), dp(4));
        status.setBackground(roundBg("#10233A", "#00A86B", 1, 8));
        scoreBox.addView(status);

        top.addView(homeBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        top.addView(scoreBox, new LinearLayout.LayoutParams(dp(132), LinearLayout.LayoutParams.WRAP_CONTENT));
        top.addView(awayBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(top);

        HorizontalScrollView mainTabsScroll = new HorizontalScrollView(this);
        mainTabsScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout mainTabs = new LinearLayout(this);
        mainTabs.setOrientation(LinearLayout.HORIZONTAL);
        mainTabs.setPadding(0, dp(18), 0, dp(10));

        mainTabs.addView(topTab("SUMMARY", false));
        mainTabs.addView(topTab("STATS", false));
        mainTabs.addView(topTab("LINEUPS", false));
        mainTabs.addView(topTab("PLAYER STATS", false));
        mainTabs.addView(topTab("COMMENTARY", true));

        mainTabsScroll.addView(mainTabs);
        root.addView(mainTabsScroll);

        LinearLayout filterBar = new LinearLayout(this);
        filterBar.setOrientation(LinearLayout.HORIZONTAL);
        filterBar.setPadding(0, dp(8), 0, dp(12));
        filterBar.setBackground(roundBg("#092636", "#092636", 0, 4));

        TextView all = filterButton("ALL COMMENTS", !importantOnly);
        TextView important = filterButton("IMPORTANT ONLY", importantOnly);

        all.setOnClickListener(v -> {
            importantOnly = false;
            createLayout();
            loadEvents();
        });

        important.setOnClickListener(v -> {
            importantOnly = true;
            createLayout();
            loadEvents();
        });

        filterBar.addView(all);
        filterBar.addView(important);

        root.addView(filterBar);

        loadingText = text("Đang tải diễn biến trận đấu...", 14, Color.parseColor("#00D68F"), false);
        loadingText.setPadding(0, dp(12), 0, dp(12));
        root.addView(loadingText);

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

        TextView team = text(shortTeam(name), 15, Color.WHITE, true);
        team.setGravity(Gravity.CENTER);
        team.setPadding(0, dp(8), 0, 0);
        box.addView(team);

        return box;
    }

    private TextView topTab(String label, boolean active) {
        TextView tv = text(label, 12, active ? Color.WHITE : Color.parseColor("#D7E3F2"), true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(14), 0, dp(14), 0);
        tv.setBackground(active
                ? roundBg("#E90B4F", "#E90B4F", 0, 8)
                : roundBg("#123448", "#123448", 0, 8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(42)
        );
        params.setMargins(0, 0, dp(8), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private TextView filterButton(String label, boolean active) {
        TextView tv = text(label, 12, active ? Color.WHITE : Color.parseColor("#D7E3F2"), true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(14), 0, dp(14), 0);
        tv.setBackground(active
                ? roundBg("#5C6874", "#5C6874", 0, 8)
                : roundBg("#092636", "#092636", 0, 8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(38)
        );
        params.setMargins(0, 0, dp(8), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private void loadEvents() {
        ZafronixClient.getApiService().getMatches(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    loadingText.setText("Không tải được diễn biến. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    JsonObject matchObj = findMatch(response.body(), matchNo);

                    if (matchObj == null) {
                        loadingText.setText("Không tìm thấy trận số " + matchNo);
                        return;
                    }

                    renderEvents(matchObj);
                } catch (Exception e) {
                    loadingText.setText("Lỗi xử lý diễn biến: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loadingText.setText("Lỗi mạng diễn biến: " + t.getMessage());
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

    private void renderEvents(JsonObject matchObj) {
        contentBox.removeAllViews();

        List<EventItem> events = new ArrayList<>();

        collectCommentaryEvents(matchObj, events);
        collectGoalEvents(matchObj, events);
        collectCardEvents(matchObj, events);
        collectPenaltyEvents(matchObj, events);
        collectSubstitutionEvents(matchObj, events);

        if (events.isEmpty()) {
            generateBasicEvents(matchObj, events);
        }

        Collections.sort(events, (a, b) -> b.minute - a.minute);

        if (importantOnly) {
            List<EventItem> filtered = new ArrayList<>();

            for (EventItem e : events) {
                if (e.important) filtered.add(e);
            }

            events = filtered;
        }

        loadingText.setText("Diễn biến trận đấu • " + events.size() + " sự kiện");

        if (events.isEmpty()) {
            TextView empty = text("Trận này chưa có diễn biến quan trọng.", 15, Color.parseColor("#D7E3F2"), false);
            empty.setPadding(dp(14), dp(14), dp(14), dp(14));
            empty.setBackground(roundBg("#10233A", "#26384F", 1, 14));
            contentBox.addView(empty);
            return;
        }

        for (EventItem item : events) {
            contentBox.addView(eventRow(item));
        }
    }

    private void collectCommentaryEvents(JsonElement element, List<EventItem> events) {
        if (element == null || element.isJsonNull()) return;

        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                collectCommentaryEvents(e, events);
            }
            return;
        }

        if (!element.isJsonObject()) return;

        JsonObject obj = element.getAsJsonObject();

        String text = getStringAny(obj, "comment", "commentary", "text", "description", "detail", "message");
        int minute = getIntAny(obj, -1, "minute", "time", "elapsed");

        if (!text.isEmpty() && minute >= 0) {
            EventItem item = new EventItem();
            item.minute = minute;
            item.icon = getEventIcon(obj);
            item.text = text;
            item.important = isImportantText(text) || isImportantType(obj);
            item.highlight = item.important;
            events.add(item);
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);

            if (key.contains("comment")
                    || key.contains("event")
                    || key.contains("timeline")
                    || key.contains("incident")) {
                collectCommentaryEvents(entry.getValue(), events);
            }
        }
    }

    private void collectGoalEvents(JsonObject matchObj, List<EventItem> events) {
        JsonArray goals = getArrayAny(matchObj, "goals", "goalEvents", "scorers");

        if (goals == null) return;

        for (JsonElement e : goals) {
            if (!e.isJsonObject()) continue;

            JsonObject obj = e.getAsJsonObject();

            int minute = getIntAny(obj, 0, "minute", "time");
            String scorer = getStringAny(obj, "scorer", "player", "name");
            String teamSide = getStringAny(obj, "team", "side");
            String teamName = resolveTeamName(teamSide);

            EventItem item = new EventItem();
            item.minute = minute;
            item.icon = "⚽";
            item.important = true;
            item.highlight = true;

            if (!scorer.isEmpty()) {
                item.text = "Goal! " + scorer + " (" + teamName + ") ghi bàn.";
            } else {
                item.text = "Goal! " + teamName + " ghi bàn.";
            }

            events.add(item);
        }
    }

    private void collectCardEvents(JsonObject matchObj, List<EventItem> events) {
        JsonArray cards = getArrayAny(matchObj, "cards", "cardEvents");

        if (cards == null) return;

        for (JsonElement e : cards) {
            if (!e.isJsonObject()) continue;

            JsonObject obj = e.getAsJsonObject();

            int minute = getIntAny(obj, 0, "minute", "time");
            String player = getStringAny(obj, "player", "name");
            String cardType = getStringAny(obj, "card", "type");
            String teamSide = getStringAny(obj, "team", "side");
            String teamName = resolveTeamName(teamSide);

            EventItem item = new EventItem();
            item.minute = minute;
            item.icon = cardType.toLowerCase(Locale.ROOT).contains("red") ? "🟥" : "🟨";
            item.important = true;
            item.highlight = true;

            item.text = (cardType.isEmpty() ? "Card" : cardType)
                    + ": "
                    + (player.isEmpty() ? "Cầu thủ" : player)
                    + " ("
                    + teamName
                    + ").";

            events.add(item);
        }
    }

    private void collectPenaltyEvents(JsonObject matchObj, List<EventItem> events) {
        JsonElement penalties = getElementAny(matchObj, "penalties", "penaltyShootout", "shootout");

        if (penalties == null || penalties.isJsonNull()) return;

        collectPenaltyRecursive(penalties, events);
    }

    private void collectPenaltyRecursive(JsonElement element, List<EventItem> events) {
        if (element == null || element.isJsonNull()) return;

        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                collectPenaltyRecursive(e, events);
            }
            return;
        }

        if (!element.isJsonObject()) return;

        JsonObject obj = element.getAsJsonObject();

        String player = getStringAny(obj, "player", "name", "scorer");
        String result = getStringAny(obj, "result", "outcome", "status");
        String teamSide = getStringAny(obj, "team", "side");
        int minute = getIntAny(obj, 121, "minute", "time");

        if (!player.isEmpty() || !result.isEmpty()) {
            EventItem item = new EventItem();
            item.minute = minute;
            item.icon = result.toLowerCase(Locale.ROOT).contains("miss")
                    || result.toLowerCase(Locale.ROOT).contains("saved")
                    || result.toLowerCase(Locale.ROOT).contains("fail")
                    ? "🥅"
                    : "⚽";
            item.important = true;
            item.highlight = true;

            String teamName = resolveTeamName(teamSide);

            if (result.toLowerCase(Locale.ROOT).contains("miss")
                    || result.toLowerCase(Locale.ROOT).contains("saved")
                    || result.toLowerCase(Locale.ROOT).contains("fail")) {
                item.text = player + " (" + teamName + ") không thực hiện thành công quả penalty.";
            } else {
                item.text = "Goal! " + player + " (" + teamName + ") sút penalty thành công.";
            }

            events.add(item);
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonArray() || child.isJsonObject())) {
                collectPenaltyRecursive(child, events);
            }
        }
    }

    private void collectSubstitutionEvents(JsonObject matchObj, List<EventItem> events) {
        JsonArray subs = getArrayAny(matchObj, "substitutions", "subs");

        if (subs == null) return;

        for (JsonElement e : subs) {
            if (!e.isJsonObject()) continue;

            JsonObject obj = e.getAsJsonObject();

            int minute = getIntAny(obj, 0, "minute", "time");
            String in = getStringAny(obj, "in", "playerIn", "player_in");
            String out = getStringAny(obj, "out", "playerOut", "player_out");
            String teamSide = getStringAny(obj, "team", "side");

            EventItem item = new EventItem();
            item.minute = minute;
            item.icon = "🔄";
            item.important = false;
            item.highlight = false;
            item.text = "Thay người "
                    + resolveTeamName(teamSide)
                    + ": "
                    + (in.isEmpty() ? "Cầu thủ vào" : in)
                    + " vào sân, "
                    + (out.isEmpty() ? "cầu thủ rời sân" : out)
                    + " rời sân.";

            events.add(item);
        }
    }

    private void generateBasicEvents(JsonObject matchObj, List<EventItem> events) {
        String status = getStringAny(matchObj, "status", "result", "stage");

        EventItem start = new EventItem();
        start.minute = 0;
        start.icon = "▶";
        start.important = false;
        start.highlight = false;
        start.text = "Trận đấu giữa " + homeTeam + " và " + awayTeam + " bắt đầu.";
        events.add(start);

        if (status.toLowerCase(Locale.ROOT).contains("finished")
                || status.toLowerCase(Locale.ROOT).contains("ft")) {
            EventItem end = new EventItem();
            end.minute = 90;
            end.icon = "🏁";
            end.important = true;
            end.highlight = true;
            end.text = "Trận đấu đã kết thúc với tỉ số " + scoreText + ".";
            events.add(end);
        }
    }

    private LinearLayout eventRow(EventItem item) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, dp(14), 0, dp(14));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView minute = text(item.minute + "'", 14, Color.WHITE, true);
        minute.setGravity(Gravity.TOP | Gravity.LEFT);
        row.addView(minute, new LinearLayout.LayoutParams(dp(46), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView icon = text(item.icon, 18, Color.WHITE, false);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundBg("#07111F", "#26384F", 1, 16));
        row.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));

        TextView content = text(item.text, 13, item.highlight ? Color.parseColor("#FF1F5B") : Color.WHITE, true);
        content.setPadding(dp(10), 0, 0, 0);
        content.setLineSpacing(4, 1.1f);
        row.addView(content, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        wrapper.addView(row);

        TextView divider = new TextView(this);
        divider.setBackgroundColor(Color.parseColor("#123448"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        params.setMargins(0, dp(14), 0, 0);
        wrapper.addView(divider, params);

        return wrapper;
    }

    private String getEventIcon(JsonObject obj) {
        String type = getStringAny(obj, "type", "event", "detail");

        String t = type.toLowerCase(Locale.ROOT);

        if (t.contains("goal")) return "⚽";
        if (t.contains("yellow")) return "🟨";
        if (t.contains("red")) return "🟥";
        if (t.contains("sub")) return "🔄";
        if (t.contains("pen")) return "🥅";
        if (t.contains("save")) return "🧤";

        return "💬";
    }

    private boolean isImportantType(JsonObject obj) {
        String type = getStringAny(obj, "type", "event", "detail").toLowerCase(Locale.ROOT);

        return type.contains("goal")
                || type.contains("card")
                || type.contains("pen")
                || type.contains("red")
                || type.contains("yellow")
                || type.contains("var");
    }

    private boolean isImportantText(String text) {
        String t = text.toLowerCase(Locale.ROOT);

        return t.contains("goal")
                || t.contains("penalty")
                || t.contains("red card")
                || t.contains("yellow card")
                || t.contains("var")
                || t.contains("miss")
                || t.contains("save")
                || t.contains("scores")
                || t.contains("wins");
    }

    private JsonArray getArrayAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key).isJsonArray()) {
                    return obj.get(key).getAsJsonArray();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private JsonElement getElementAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key)) {
                    return obj.get(key);
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String resolveTeamName(String side) {
        if (side == null) return "";

        String s = side.toLowerCase(Locale.ROOT);

        if (s.equals("home") || s.equals("h") || s.equals("team1")) {
            return homeTeam;
        }

        if (s.equals("away") || s.equals("a") || s.equals("team2")) {
            return awayTeam;
        }

        if (!side.trim().isEmpty()) {
            return side;
        }

        return "";
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
            case "paraguay": return "🇵🇾";
            case "norway": return "🇳🇴";
            case "south africa": return "🇿🇦";
            case "sweden": return "🇸🇪";
            default: return "🏳";
        }
    }

    private String shortTeam(String name) {
        if (name == null) return "";

        if (name.length() <= 16) return name;

        if (name.equalsIgnoreCase("Bosnia and Herzegovina")) return "Bosnia";
        if (name.equalsIgnoreCase("United States")) return "USA";
        if (name.equalsIgnoreCase("South Africa")) return "Nam Phi";
        if (name.equalsIgnoreCase("Côte d'Ivoire")) return "Bờ Biển Ngà";

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

    private static class EventItem {
        int minute;
        String icon;
        String text;
        boolean important;
        boolean highlight;
    }
}
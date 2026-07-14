package com.example.nickhercore;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LineupPitchActivity extends AppCompatActivity {

    private int matchNo;
    private String homeTeam;
    private String awayTeam;
    private String scoreText;

    private LinearLayout root;
    private TextView titleText;
    private TextView formationText;
    private FrameLayout pitchFrame;
    private LinearLayout substitutesLayout;

    private SalahWcupRepository salahRepository;

    private Map<String, String> photoMap = new HashMap<>();

    private final int pitchWidthDp = 900;
    private final int pitchHeightDp = 560;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        salahRepository = new SalahWcupRepository();

        readIntent();
        createLayout();
        loadPhotoMapThenMatch();
    }

    private void readIntent() {
        matchNo = getIntent().getIntExtra("MATCH_NO", -1);
        homeTeam = getIntent().getStringExtra("HOME_TEAM");
        awayTeam = getIntent().getStringExtra("AWAY_TEAM");
        scoreText = getIntent().getStringExtra("SCORE_TEXT");

        if (homeTeam == null || homeTeam.trim().isEmpty()) {
            homeTeam = "Đội nhà";
        }

        if (awayTeam == null || awayTeam.trim().isEmpty()) {
            awayTeam = "Đội khách";
        }

        if (scoreText == null) {
            scoreText = "";
        }
    }

    private void createLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#07111F"));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(4), dp(14), dp(14));

        scrollView.addView(root);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.RIGHT);
        topBar.setPadding(0, dp(26), 0, dp(8));

        Button back = new Button(this);
        back.setText("← Quay lại");
        back.setTextColor(Color.WHITE);
        back.setTextSize(12);
        back.setBackgroundColor(Color.parseColor("#10233A"));
        back.setOnClickListener(v -> goBackHome());

        topBar.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(110),
                        dp(42)
                )
        );

        root.addView(
                topBar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        titleText = makeText(homeTeam + " vs " + awayTeam, 22, Color.WHITE, true);
        titleText.setGravity(Gravity.CENTER);
        root.addView(titleText);

        TextView score = makeText(scoreText, 28, Color.WHITE, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, dp(6), 0, dp(10));
        root.addView(score);

        formationText = makeText("Đang tải đội hình...", 14, Color.parseColor("#B8C7D9"), true);
        formationText.setGravity(Gravity.CENTER);
        formationText.setPadding(0, 0, 0, dp(12));
        root.addView(formationText);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(true);

        pitchFrame = new FrameLayout(this);
        pitchFrame.setBackgroundColor(Color.parseColor("#063B35"));

        hsv.addView(
                pitchFrame,
                new HorizontalScrollView.LayoutParams(
                        dp(pitchWidthDp),
                        dp(pitchHeightDp)
                )
        );

        root.addView(
                hsv,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(pitchHeightDp + 12)
                )
        );

        substitutesLayout = new LinearLayout(this);
        substitutesLayout.setOrientation(LinearLayout.VERTICAL);
        substitutesLayout.setPadding(0, dp(16), 0, dp(10));
        root.addView(substitutesLayout);

        setContentView(scrollView);
    }

    private void loadPhotoMapThenMatch() {
        salahRepository.getPlayerPhotoMap(new SalahWcupRepository.PhotoMapCallback() {
            @Override
            public void onSuccess(Map<String, String> map) {
                photoMap = map;
                loadMatchLineup();
            }

            @Override
            public void onError(String message) {
                photoMap = new HashMap<>();
                loadMatchLineup();
            }
        });
    }

    private void loadMatchLineup() {
        ZafronixClient.getApiService().getMatches(2026).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    formationText.setText("Không tải được đội hình. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    JsonObject matchObj = findMatch(response.body(), matchNo);

                    if (matchObj == null) {
                        formationText.setText("Không tìm thấy trận số " + matchNo);
                        return;
                    }

                    drawLineup(matchObj);
                } catch (Exception e) {
                    formationText.setText("Lỗi xử lý đội hình: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                formationText.setText("Lỗi mạng đội hình: " + t.getMessage());
            }
        });
    }

    private JsonObject findMatch(JsonElement root, int matchNo) {
        if (root == null || root.isJsonNull()) {
            return null;
        }

        if (root.isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray()) {
                JsonObject result = findMatch(e, matchNo);

                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        if (!root.isJsonObject()) {
            return null;
        }

        JsonObject obj = root.getAsJsonObject();

        int no = getIntAny(obj, -1, "matchNo", "match_no", "number");

        if (no == matchNo) {
            return obj;
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonArray() || child.isJsonObject())) {
                JsonObject result = findMatch(child, matchNo);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private void drawLineup(JsonObject matchObj) {
        pitchFrame.removeAllViews();

        PitchView pitchView = new PitchView(this);
        pitchFrame.addView(
                pitchView,
                new FrameLayout.LayoutParams(
                        dp(pitchWidthDp),
                        dp(pitchHeightDp)
                )
        );

        if (!matchObj.has("lineups") || !matchObj.get("lineups").isJsonObject()) {
            formationText.setText("Trận này chưa có dữ liệu đội hình.");
            return;
        }

        JsonObject lineups = matchObj.get("lineups").getAsJsonObject();

        List<PlayerEntry> homePlayers = parsePlayers(lineups.get("home"), homeTeam);
        List<PlayerEntry> awayPlayers = parsePlayers(lineups.get("away"), awayTeam);

        List<PlayerEntry> homeStarters = filterPlayers(homePlayers, true);
        List<PlayerEntry> awayStarters = filterPlayers(awayPlayers, true);

        List<PlayerEntry> homeSubs = filterPlayers(homePlayers, false);
        List<PlayerEntry> awaySubs = filterPlayers(awayPlayers, false);

        formationText.setText(
                getFormationText(homeStarters) + "     FORMATION     " + getFormationText(awayStarters)
        );

        placeTeam(homeStarters, true);
        placeTeam(awayStarters, false);

        drawSubstitutes(homeSubs, awaySubs);
    }

    private List<PlayerEntry> parsePlayers(JsonElement element, String team) {
        List<PlayerEntry> result = new ArrayList<>();

        if (element == null || !element.isJsonArray()) {
            return result;
        }

        for (JsonElement e : element.getAsJsonArray()) {
            if (!e.isJsonObject()) {
                continue;
            }

            JsonObject obj = e.getAsJsonObject();

            PlayerEntry p = new PlayerEntry();
            p.name = getStringAny(obj, "player", "name");
            p.number = getStringAny(obj, "number", "num");
            p.position = getStringAny(obj, "position", "pos");
            p.team = team;
            p.starter = getBooleanAny(obj, "starter");
            p.captain = getBooleanAny(obj, "captain");
            p.photoUrl = salahRepository.getPhotoFromMap(photoMap, team, p.name);

            result.add(p);
        }

        return result;
    }

    private List<PlayerEntry> filterPlayers(List<PlayerEntry> players, boolean starter) {
        List<PlayerEntry> result = new ArrayList<>();

        for (PlayerEntry p : players) {
            if (p.starter == starter) {
                result.add(p);
            }
        }

        return result;
    }

    private void placeTeam(List<PlayerEntry> players, boolean isHome) {
        Map<Integer, List<PlayerEntry>> rows = new HashMap<>();

        for (PlayerEntry p : players) {
            int row = getPositionRow(p.position);

            if (!rows.containsKey(row)) {
                rows.put(row, new ArrayList<>());
            }

            rows.get(row).add(p);
        }

        for (Map.Entry<Integer, List<PlayerEntry>> entry : rows.entrySet()) {
            int row = entry.getKey();
            List<PlayerEntry> rowPlayers = entry.getValue();

            for (int i = 0; i < rowPlayers.size(); i++) {
                PlayerEntry player = rowPlayers.get(i);

                int xPercent;

                if (isHome) {
                    if (row == 0) {
                        xPercent = 8;
                    } else if (row == 1) {
                        xPercent = 20;
                    } else if (row == 2) {
                        xPercent = 33;
                    } else {
                        xPercent = 45;
                    }
                } else {
                    if (row == 0) {
                        xPercent = 92;
                    } else if (row == 1) {
                        xPercent = 80;
                    } else if (row == 2) {
                        xPercent = 67;
                    } else {
                        xPercent = 55;
                    }
                }

                int yPercent = getYPercent(i, rowPlayers.size());

                addPlayerCard(player, xPercent, yPercent);
            }
        }
    }

    private int getPositionRow(String pos) {
        if (pos == null) {
            return 2;
        }

        String p = pos.toUpperCase();

        if (p.contains("GK")) {
            return 0;
        }

        if (p.contains("CB")
                || p.contains("RB")
                || p.contains("LB")
                || p.contains("RWB")
                || p.contains("LWB")
                || p.contains("DF")) {
            return 1;
        }

        if (p.contains("DM")
                || p.contains("CM")
                || p.contains("AM")
                || p.contains("LM")
                || p.contains("RM")
                || p.contains("MF")) {
            return 2;
        }

        return 3;
    }

    private int getYPercent(int index, int total) {
        if (total <= 1) {
            return 50;
        }

        int top = 15;
        int bottom = 85;

        return top + (bottom - top) * index / (total - 1);
    }

    private String getFormationText(List<PlayerEntry> starters) {
        int def = 0;
        int mid = 0;
        int atk = 0;

        for (PlayerEntry p : starters) {
            int row = getPositionRow(p.position);

            if (row == 1) {
                def++;
            } else if (row == 2) {
                mid++;
            } else if (row == 3) {
                atk++;
            }
        }

        if (def == 0 && mid == 0 && atk == 0) {
            return "-";
        }

        return def + " - " + mid + " - " + atk;
    }

    private void addPlayerCard(PlayerEntry player, int xPercent, int yPercent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(2), dp(2), dp(2), dp(2));

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (player.photoUrl != null && !player.photoUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(player.photoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .into(image);
        } else {
            image.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        card.addView(image, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView name = makeText(getPlayerLabel(player), 10, Color.WHITE, true);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(2);
        name.setBackgroundColor(Color.parseColor("#AA001B22"));
        name.setPadding(dp(4), dp(2), dp(4), dp(2));
        card.addView(name, new LinearLayout.LayoutParams(dp(82), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView pos = makeText(player.position, 9, Color.parseColor("#B8F7D0"), true);
        pos.setGravity(Gravity.CENTER);
        card.addView(pos);

        int cardW = dp(90);
        int cardH = dp(86);

        int x = dp(pitchWidthDp) * xPercent / 100 - cardW / 2;
        int y = dp(pitchHeightDp) * yPercent / 100 - cardH / 2;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardW, cardH);
        params.leftMargin = x;
        params.topMargin = y;

        pitchFrame.addView(card, params);
    }

    private String getPlayerLabel(PlayerEntry p) {
        String label = "";

        if (p.number != null && !p.number.isEmpty()) {
            label += p.number + " ";
        }

        label += shortName(p.name);

        if (p.captain) {
            label += " (C)";
        }

        return label;
    }

    private String shortName(String name) {
        if (name == null) {
            return "";
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length <= 2) {
            return name;
        }

        return parts[parts.length - 1];
    }

    private void drawSubstitutes(List<PlayerEntry> homeSubs, List<PlayerEntry> awaySubs) {
        substitutesLayout.removeAllViews();

        TextView title = makeText("SUBSTITUTED PLAYERS / DỰ BỊ", 14, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, dp(10));
        title.setBackgroundColor(Color.parseColor("#10233A"));
        substitutesLayout.addView(title);

        int max = Math.max(homeSubs.size(), awaySubs.size());

        for (int i = 0; i < max; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView left = makeText(i < homeSubs.size() ? subText(homeSubs.get(i)) : "", 12, Color.WHITE, false);
            TextView right = makeText(i < awaySubs.size() ? subText(awaySubs.get(i)) : "", 12, Color.WHITE, false);
            right.setGravity(Gravity.RIGHT);

            row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            row.addView(right, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            substitutesLayout.addView(row);
        }
    }

    private String subText(PlayerEntry p) {
        String number = p.number == null || p.number.isEmpty() ? "" : "#" + p.number + " ";
        return number + p.name + "  (" + p.position + ")";
    }

    private TextView makeText(String text, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text == null ? "" : text);
        tv.setTextSize(size);
        tv.setTextColor(color);

        if (bold) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return tv;
    }

    private String getStringAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    return obj.get(key).getAsString();
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

    private boolean getBooleanAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    return obj.get(key).getAsBoolean();
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private void goBackHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBackHome();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class PlayerEntry {
        String name;
        String number;
        String position;
        String team;
        String photoUrl;
        boolean starter;
        boolean captain;
    }

    public static class PitchView extends View {

        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public PitchView(android.content.Context context) {
            super(context);

            linePaint.setColor(Color.parseColor("#88D7E3F2"));
            linePaint.setStrokeWidth(3);
            linePaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            canvas.drawColor(Color.parseColor("#063B35"));

            int pad = 24;

            canvas.drawRect(pad, pad, w - pad, h - pad, linePaint);
            canvas.drawLine(w / 2f, pad, w / 2f, h - pad, linePaint);
            canvas.drawCircle(w / 2f, h / 2f, 70, linePaint);

            canvas.drawRect(pad, h / 2f - 120, pad + 120, h / 2f + 120, linePaint);
            canvas.drawRect(w - pad - 120, h / 2f - 120, w - pad, h / 2f + 120, linePaint);

            canvas.drawRect(pad, h / 2f - 60, pad + 45, h / 2f + 60, linePaint);
            canvas.drawRect(w - pad - 45, h / 2f - 60, w - pad, h / 2f + 60, linePaint);

            canvas.drawCircle(pad + 90, h / 2f, 4, linePaint);
            canvas.drawCircle(w - pad - 90, h / 2f, 4, linePaint);
        }
    }
}
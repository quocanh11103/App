package com.example.nickhercore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppDatabaseHelper db;
    private FootballRepository footballRepository;
    private ZafronixRepository zafronixRepository;

    private RecyclerView recyclerView;
    private MatchAdapter adapter;

    private TextView statusText;
    private TextView titleText;

    private final List<Match> allMatchesCache = new ArrayList<>();

    private String mode = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new AppDatabaseHelper(this);

        if (!db.isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        footballRepository = new FootballRepository();
        zafronixRepository = new ZafronixRepository();

        NotificationHelper.createChannel(this);
        requestNotificationPermission();

        createLayout();
        loadAllMatches();
    }

    private void createLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(24), dp(14), 0);
        root.setBackgroundColor(Color.parseColor("#07111F"));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(12));

        TextView logo = text("N", 24, Color.parseColor("#00D68F"), true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(roundBg("#10233A", "#00D68F", 1, 14));
        header.addView(logo, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(dp(12), 0, 0, 0);

        titleText = text("NickherCore", 24, Color.WHITE, true);
        TextView sub = text("World Cup 2026  •  Live Scores", 14, Color.parseColor("#B8C7D9"), false);

        titleBox.addView(titleText);
        titleBox.addView(sub);

        header.addView(titleBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView liveBadge = text("LIVE", 14, Color.WHITE, true);
        liveBadge.setGravity(Gravity.CENTER);
        liveBadge.setBackground(roundBg("#E90B2F", "#E90B2F", 0, 10));
        header.addView(liveBadge, new LinearLayout.LayoutParams(dp(62), dp(40)));

        TextView bell = text("🔔", 25, Color.parseColor("#00D68F"), false);
        bell.setGravity(Gravity.CENTER);
        bell.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        header.addView(bell, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView user = text("👤", 24, Color.parseColor("#00D68F"), false);
        user.setGravity(Gravity.CENTER);
        user.setOnClickListener(v -> {
            db.logout();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
        header.addView(user, new LinearLayout.LayoutParams(dp(44), dp(48)));

        root.addView(header);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(16), dp(18), dp(16));
        hero.setBackground(roundBg("#063B35", "#00A86B", 1, 18));

        TextView trophy = text("🏆", 34, Color.WHITE, false);
        hero.addView(trophy);

        TextView heroTitle = text("FIFA World Cup 2026™", 29, Color.WHITE, true);
        heroTitle.setPadding(0, dp(8), 0, dp(4));
        hero.addView(heroTitle);

        TextView heroSub = text("Live score  •  Lineups  •  Stats  •  Events  •  Fixtures", 14, Color.parseColor("#D7E3F2"), false);
        hero.addView(heroSub);

        statusText = text("●  Đang cập nhật trực tiếp", 14, Color.parseColor("#00D68F"), false);
        statusText.setPadding(0, dp(10), 0, 0);
        hero.addView(statusText);

        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        heroParams.bottomMargin = dp(12);
        root.addView(hero, heroParams);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);

        chips.addView(chip("Tất cả", "ALL"));
        chips.addView(chip("LIVE", "LIVE"));
        chips.addView(chip("Lịch đấu", "FIXTURES"));
        chips.addView(chip("Bảng đấu", "STANDINGS"));
        chips.addView(chip("Yêu thích", "FAVORITES"));

        hsv.addView(chips);
        root.addView(hsv);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchAdapter(this, new ArrayList<>(), db);
        recyclerView.setAdapter(adapter);

        LinearLayout.LayoutParams rvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        rvParams.topMargin = dp(10);
        root.addView(recyclerView, rvParams);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(0, dp(6), 0, dp(6));
        bottom.setBackground(roundBg("#081827", "#26384F", 1, 24));

        bottom.addView(nav("Trang chủ", "🏠"));
        bottom.addView(nav("Lịch đấu", "📅"));
        bottom.addView(nav("Bảng đấu", "🏆"));
        bottom.addView(nav("Yêu thích", "☆"));
        bottom.addView(nav("Thông báo", "🔔"));

        root.addView(bottom, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(74)
        ));

        setContentView(root);
    }

    private TextView chip(String label, String targetMode) {
        TextView tv = text(label, 14, Color.WHITE, true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(18), 0, dp(18), 0);
        tv.setBackground(roundBg("#10233A", "#26384F", 1, 18));
        tv.setOnClickListener(v -> {
            mode = targetMode;

            if ("ALL".equals(mode)) {
                loadAllMatches();
            } else if ("LIVE".equals(mode)) {
                loadLiveMatches();
            } else if ("FIXTURES".equals(mode)) {
                loadFixtures();
            } else if ("STANDINGS".equals(mode)) {
                showStandings();
            } else if ("FAVORITES".equals(mode)) {
                showFavorites();
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(46)
        );
        params.setMargins(0, 0, dp(8), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private TextView nav(String label, String icon) {
        TextView tv = text(icon + "\n" + label, 12, Color.parseColor("#B8C7D9"), false);
        tv.setGravity(Gravity.CENTER);

        tv.setOnClickListener(v -> {
            if (label.equals("Trang chủ")) {
                mode = "ALL";
                loadAllMatches();
            } else if (label.equals("Lịch đấu")) {
                mode = "FIXTURES";
                loadFixtures();
            } else if (label.equals("Bảng đấu")) {
                showStandings();
            } else if (label.equals("Yêu thích")) {
                mode = "FAVORITES";
                showFavorites();
            } else if (label.equals("Thông báo")) {
                startActivity(new Intent(this, NotificationActivity.class));
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        tv.setLayoutParams(params);

        return tv;
    }

    private void loadAllMatches() {
        statusText.setText("●  Đang tải World Cup 2026...");

        zafronixRepository.getAllMatches(new FootballRepository.MatchCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                allMatchesCache.clear();
                allMatchesCache.addAll(matches);
                adapter.setItems(matches);
                statusText.setText("●  World Cup 2026 • " + matches.size() + " trận");
                checkFavoriteNotifications(matches);
            }

            @Override
            public void onError(String message) {
                statusText.setText("Không tải được dữ liệu");
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadFixtures() {
        statusText.setText("●  Đang tải lịch đấu...");

        zafronixRepository.getFutureMatches(new FootballRepository.MatchCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                allMatchesCache.clear();
                allMatchesCache.addAll(matches);
                adapter.setItems(matches);
                statusText.setText("●  Lịch đấu • " + matches.size() + " trận");
                checkFavoriteNotifications(matches);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadLiveMatches() {
        statusText.setText("●  Đang tải LIVE...");

        footballRepository.getLiveMatches(new FootballRepository.MatchCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                adapter.setItems(matches);
                statusText.setText("●  LIVE • " + matches.size() + " trận");
                checkFavoriteNotifications(matches);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                statusText.setText("LIVE cần API-Football còn lượt request");
            }
        });
    }

    private void showFavorites() {
        if (allMatchesCache.isEmpty()) {
            loadAllMatches();
            return;
        }

        List<Integer> ids = db.getFavoriteIds();
        List<Match> favs = new ArrayList<>();

        for (Match m : allMatchesCache) {
            if (ids.contains(m.getFixtureId())) {
                favs.add(m);
            }
        }

        adapter.setItems(favs);
        statusText.setText("★  Trận yêu thích • " + favs.size() + " trận");
    }

    private void showStandings() {
        startActivity(new Intent(this, StandingsActivity.class));
    }

    private void checkFavoriteNotifications(List<Match> matches) {
        for (Match match : matches) {
            if (!db.isFavorite(match.getFixtureId())) {
                continue;
            }

            AppDatabaseHelper.FavoriteState state = db.getFavoriteState(match.getFixtureId());

            if (state == null || !state.notifyEnabled) {
                continue;
            }

            String status = safe(match.getStatus());
            boolean isFinished = status.toLowerCase(Locale.ROOT).contains("finished");
            boolean isStarted = !status.toLowerCase(Locale.ROOT).contains("scheduled");

            boolean finalLineupNotified = state.lineupsNotified;

            if (db.getSetting("notify_lineup") && isStarted && !state.lineupsNotified) {
                String title = "Đội hình ra sân đã có";
                String body = match.getHomeTeam() + " vs " + match.getAwayTeam();
                NotificationHelper.showMatchNotification(this, title, body, match.getFixtureId());
                db.addLog(title, body, "lineup");
                finalLineupNotified = true;
            }

            boolean scoreChanged =
                    state.lastHomeScore != match.getHomeScore()
                            || state.lastAwayScore != match.getAwayScore();

            if (db.getSetting("notify_score") && scoreChanged) {
                String title = "Cập nhật tỉ số";
                String body = match.getHomeTeam() + " " + match.getHomeScore()
                        + " - " + match.getAwayScore() + " " + match.getAwayTeam();
                NotificationHelper.showMatchNotification(this, title, body, match.getFixtureId());
                db.addLog(title, body, "score");
            }

            boolean wasFinished = state.lastStatus != null
                    && state.lastStatus.toLowerCase(Locale.ROOT).contains("finished");

            if (db.getSetting("notify_finished") && isFinished && !wasFinished) {
                String title = "Trận đấu đã kết thúc";
                String body = match.getHomeTeam() + " " + match.getHomeScore()
                        + " - " + match.getAwayScore() + " " + match.getAwayTeam();
                NotificationHelper.showMatchNotification(this, title, body, match.getFixtureId());
                db.addLog(title, body, "finished");
            }

            db.updateFavoriteSnapshot(
                    match.getFixtureId(),
                    match.getHomeScore(),
                    match.getAwayScore(),
                    status,
                    finalLineupNotified
            );
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
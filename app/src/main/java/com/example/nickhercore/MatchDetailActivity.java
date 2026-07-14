package com.example.nickhercore;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MatchDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvLeague;
    private TextView tvScore;
    private TextView tvInfo;
    private TextView tvNotice;
    private TextView tvContentTitle;
    private TextView tvContent;

    private FootballRepository footballRepository;
    private ZafronixRepository zafronixRepository;
    private SalahWcupRepository salahWcupRepository;

    private int fixtureId;
    private String homeTeam;
    private String awayTeam;
    private String league;
    private String round;
    private String date;
    private String status;
    private String venue;
    private int homeScore;
    private int awayScore;

    private boolean isWorldCup2026Match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        footballRepository = new FootballRepository();
        zafronixRepository = new ZafronixRepository();
        salahWcupRepository = new SalahWcupRepository();

        readIntentData();
        createLayout();
        setupHeader();
    }

    private void createLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#07111F"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(26), dp(18), dp(18));

        scrollView.addView(root);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(0, 0, 0, dp(12));

        Button btnBack = new Button(this);
        btnBack.setText("← Quay lại");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(12);
        btnBack.setBackgroundColor(Color.parseColor("#10233A"));
        btnBack.setOnClickListener(v -> goBackHome());

        topBar.addView(btnBack, new LinearLayout.LayoutParams(dp(120), dp(44)));

        TextView topTitle = makeText(18, Color.WHITE, true);
        topTitle.setText("Chi tiết trận đấu");
        topTitle.setGravity(Gravity.CENTER);

        topBar.addView(topTitle, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnShare = new Button(this);
        btnShare.setText("↗");
        btnShare.setTextColor(Color.parseColor("#00D68F"));
        btnShare.setTextSize(18);
        btnShare.setBackgroundColor(Color.TRANSPARENT);
        btnShare.setOnClickListener(v -> shareMatch());

        topBar.addView(btnShare, new LinearLayout.LayoutParams(dp(48), dp(44)));

        root.addView(topBar);

        tvTitle = makeText(24, Color.WHITE, true);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, dp(8), 0, dp(8));
        root.addView(tvTitle);

        tvLeague = makeText(14, Color.parseColor("#B8C7D9"), true);
        tvLeague.setGravity(Gravity.CENTER);
        root.addView(tvLeague);

        tvScore = makeText(40, Color.WHITE, true);
        tvScore.setGravity(Gravity.CENTER);
        tvScore.setPadding(0, dp(18), 0, dp(18));
        root.addView(tvScore);

        tvInfo = makeBoxText();
        root.addView(tvInfo);

        tvNotice = makeText(13, Color.parseColor("#00D68F"), true);
        tvNotice.setPadding(0, dp(14), 0, dp(14));
        root.addView(tvNotice);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, dp(8), 0, dp(8));
        root.addView(row1);

        Button btnLineups = makeSmallButton("Đội hình sân");
        Button btnStats = makeSmallButton("Thống kê");

        row1.addView(btnLineups, new LinearLayout.LayoutParams(0, dp(48), 1));
        row1.addView(btnStats, new LinearLayout.LayoutParams(0, dp(48), 1));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 0, 0, dp(18));
        root.addView(row2);

        Button btnEvents = makeSmallButton("Diễn biến");
        Button btnHighlight = makeSmallButton("Highlight");

        row2.addView(btnEvents, new LinearLayout.LayoutParams(0, dp(48), 1));
        row2.addView(btnHighlight, new LinearLayout.LayoutParams(0, dp(48), 1));

        tvContentTitle = makeText(18, Color.WHITE, true);
        tvContentTitle.setPadding(0, dp(8), 0, dp(10));
        root.addView(tvContentTitle);

        tvContent = makeBoxText();
        root.addView(tvContent);

        btnLineups.setOnClickListener(v -> {
            if (isWorldCup2026Match) {
                openPitchLineup();
            } else {
                loadFootballLineups();
            }
        });

        btnStats.setOnClickListener(v -> {
            if (isWorldCup2026Match) {
                openStatsScreen();
            } else {
                loadFootballStats();
            }
        });

        btnEvents.setOnClickListener(v -> {
            if (isWorldCup2026Match) {
                loadZafronixEvents();
            } else {
                loadFootballEvents();
            }
        });

        btnHighlight.setOnClickListener(v -> loadHighlight());

        setContentView(scrollView);
    }

    private TextView makeText(int size, int color, boolean bold) {
        TextView textView = new TextView(this);
        textView.setTextSize(size);
        textView.setTextColor(color);

        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return textView;
    }

    private TextView makeBoxText() {
        TextView textView = new TextView(this);
        textView.setTextSize(14);
        textView.setTextColor(Color.parseColor("#D7E3F2"));
        textView.setBackgroundColor(Color.parseColor("#10233A"));
        textView.setPadding(dp(14), dp(14), dp(14), dp(14));
        textView.setLineSpacing(4, 1.1f);
        return textView;
    }

    private Button makeSmallButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setBackgroundColor(Color.parseColor("#00A86B"));
        return button;
    }

    private void readIntentData() {
        fixtureId = getIntent().getIntExtra("FIXTURE_ID", -1);

        if (fixtureId <= 0) {
            fixtureId = getIntent().getIntExtra("fixture_id", -1);
        }

        if (fixtureId <= 0) {
            fixtureId = getIntent().getIntExtra("match_id", -1);
        }

        homeTeam = getStringExtraAny("HOME_TEAM", "home_team", "homeTeam", "home");
        awayTeam = getStringExtraAny("AWAY_TEAM", "away_team", "awayTeam", "away");

        league = getStringExtraAny("LEAGUE", "league");
        round = getStringExtraAny("ROUND", "round");
        date = getStringExtraAny("DATE", "date", "MATCH_TIME", "matchTime");
        status = getStringExtraAny("STATUS", "status");
        venue = getStringExtraAny("VENUE", "venue", "STADIUM", "stadium");

        homeScore = getIntent().getIntExtra("HOME_SCORE", 0);
        awayScore = getIntent().getIntExtra("AWAY_SCORE", 0);

        if (homeTeam.isEmpty()) {
            homeTeam = "Đội nhà";
        }

        if (awayTeam.isEmpty()) {
            awayTeam = "Đội khách";
        }

        isWorldCup2026Match = isWorldCup2026();

        if (league.isEmpty()) {
            league = isWorldCup2026Match ? "FIFA World Cup 2026" : "Football Match";
        }

        if (status.isEmpty()) {
            status = "Scheduled";
        }
    }

    private String getStringExtraAny(String... keys) {
        for (String key : keys) {
            String value = getIntent().getStringExtra(key);

            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return "";
    }

    private boolean isWorldCup2026() {
        String data = (league + " " + round + " " + date).toLowerCase(Locale.ROOT);

        if (data.contains("world cup 2026")
                || data.contains("fifa world cup 2026")
                || data.contains("2026")) {
            return true;
        }

        return fixtureId >= 1 && fixtureId <= 104;
    }

    private void setupHeader() {
        tvTitle.setText(homeTeam + " vs " + awayTeam);
        tvLeague.setText(league);
        tvScore.setText(homeScore + " - " + awayScore);

        StringBuilder info = new StringBuilder();

        info.append("Trạng thái: ").append(status).append("\n");

        if (!date.isEmpty()) {
            info.append("Thời gian: ").append(date).append("\n");
        }

        if (!round.isEmpty()) {
            info.append("Vòng đấu: ").append(round).append("\n");
        }

        if (!venue.isEmpty()) {
            info.append("Sân vận động: ").append(venue).append("\n");
        }

        if (fixtureId > 0) {
            info.append("Mã trận: ").append(fixtureId);
        }

        tvInfo.setText(info.toString());

        if (isWorldCup2026Match) {
            tvNotice.setText("Nguồn: Zafronix + wcup2026.org. Có đội hình, thống kê, diễn biến và highlight nếu API hỗ trợ.");
            showBasicWorldCup2026Content();
        } else {
            tvNotice.setText("Nguồn API-Football: dữ liệu phụ thuộc API key và số lượt request.");
            tvContentTitle.setText("Chi tiết trận đấu");
            tvContent.setText("Bấm Đội hình, Thống kê hoặc Diễn biến để tải dữ liệu.");
        }
    }

    private void showBasicWorldCup2026Content() {
        tvContentTitle.setText("Thông tin trận đấu");

        StringBuilder sb = new StringBuilder();

        sb.append("Trận đấu: ").append(homeTeam).append(" vs ").append(awayTeam).append("\n");
        sb.append("Tỉ số: ").append(homeScore).append(" - ").append(awayScore).append("\n");
        sb.append("Trạng thái: ").append(status).append("\n");

        if (!date.isEmpty()) {
            sb.append("Thời gian: ").append(date).append("\n");
        }

        if (!round.isEmpty()) {
            sb.append("Vòng đấu: ").append(round).append("\n");
        }

        if (!venue.isEmpty()) {
            sb.append("Sân vận động: ").append(venue).append("\n");
        }

        sb.append("\nBấm Đội hình sân để xem đội hình dạng sân bóng.");
        sb.append("\nBấm Thống kê để xem thống kê dạng thanh so sánh.");
        sb.append("\nBấm Diễn biến để xem bàn thắng/sự kiện.");
        sb.append("\nBấm Highlight để mở video sau trận nếu API có link.");

        tvContent.setText(sb.toString());
    }

    private void openPitchLineup() {
        Intent intent = new Intent(this, LineupPitchActivity.class);

        intent.putExtra("MATCH_NO", fixtureId);
        intent.putExtra("HOME_TEAM", homeTeam);
        intent.putExtra("AWAY_TEAM", awayTeam);
        intent.putExtra("SCORE_TEXT", homeScore + " - " + awayScore);

        startActivity(intent);
    }

    private void openStatsScreen() {
        Intent intent = new Intent(this, MatchStatsActivity.class);

        intent.putExtra("MATCH_NO", fixtureId);
        intent.putExtra("HOME_TEAM", homeTeam);
        intent.putExtra("AWAY_TEAM", awayTeam);
        intent.putExtra("SCORE_TEXT", homeScore + " - " + awayScore);

        startActivity(intent);
    }

    private void loadHighlight() {
        if (fixtureId <= 0) {
            Toast.makeText(this, "Không có mã trận để tải highlight", Toast.LENGTH_SHORT).show();
            return;
        }

        tvContentTitle.setText("Highlight");
        tvContent.setText("Đang tìm highlight...");

        salahWcupRepository.getHighlightUrl(fixtureId, new FootballRepository.DetailCallback() {
            @Override
            public void onSuccess(String text) {
                tvContent.setText("Đã tìm thấy highlight:\n" + text);
                openHighlightUrl(text);
            }

            @Override
            public void onError(String message) {
                tvContent.setText(message == null ? "Trận này chưa có highlight." : message);
            }
        });
    }

    private void openHighlightUrl(String highlightUrl) {
        if (highlightUrl == null || highlightUrl.trim().isEmpty()) {
            Toast.makeText(this, "Trận này chưa có highlight", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(highlightUrl));
            startActivity(Intent.createChooser(intent, "Mở highlight bằng"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng để mở highlight", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được highlight: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadZafronixEvents() {
        tvContentTitle.setText("Diễn biến");
        tvContent.setText("Đang tải diễn biến từ Zafronix...");

        zafronixRepository.getEventsText(fixtureId, new FootballRepository.DetailCallback() {
            @Override
            public void onSuccess(String text) {
                if (text == null || text.trim().isEmpty()) {
                    tvContent.setText("Trận này chưa có dữ liệu diễn biến.");
                } else {
                    tvContent.setText(text);
                }
            }

            @Override
            public void onError(String message) {
                tvContent.setText(message == null ? "Không tải được diễn biến." : message);
            }
        });
    }

    private void loadFootballLineups() {
        if (fixtureId <= 0) {
            Toast.makeText(this, "Không có mã trận để tải đội hình", Toast.LENGTH_LONG).show();
            return;
        }

        tvContentTitle.setText("Đội hình");
        tvContent.setText("Đang tải đội hình...");

        footballRepository.getMatchLineups(fixtureId, new FootballRepository.DetailCallback() {
            @Override
            public void onSuccess(String text) {
                tvContent.setText(text == null || text.trim().isEmpty() ? "Không có dữ liệu đội hình." : text);
            }

            @Override
            public void onError(String message) {
                tvContent.setText(message == null ? "Không lấy được đội hình." : message);
            }
        });
    }

    private void loadFootballStats() {
        if (fixtureId <= 0) {
            Toast.makeText(this, "Không có mã trận để tải thống kê", Toast.LENGTH_LONG).show();
            return;
        }

        tvContentTitle.setText("Thống kê");
        tvContent.setText("Đang tải thống kê...");

        footballRepository.getMatchStatistics(fixtureId, new FootballRepository.DetailCallback() {
            @Override
            public void onSuccess(String text) {
                tvContent.setText(text == null || text.trim().isEmpty() ? "Không có dữ liệu thống kê." : text);
            }

            @Override
            public void onError(String message) {
                tvContent.setText(message == null ? "Không lấy được thống kê." : message);
            }
        });
    }

    private void loadFootballEvents() {
        if (fixtureId <= 0) {
            Toast.makeText(this, "Không có mã trận để tải diễn biến", Toast.LENGTH_LONG).show();
            return;
        }

        tvContentTitle.setText("Diễn biến");
        tvContent.setText("Đang tải diễn biến trận đấu...");

        footballRepository.getMatchEvents(fixtureId, new FootballRepository.DetailCallback() {
            @Override
            public void onSuccess(String text) {
                tvContent.setText(text == null || text.trim().isEmpty() ? "Không có dữ liệu diễn biến." : text);
            }

            @Override
            public void onError(String message) {
                tvContent.setText(message == null ? "Không lấy được diễn biến trận đấu." : message);
            }
        });
    }

    private void shareMatch() {
        String content = homeTeam + " " + homeScore + " - " + awayScore + " " + awayTeam
                + "\n" + league
                + "\n" + venue;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);

        startActivity(Intent.createChooser(intent, "Chia sẻ trận đấu"));
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
}
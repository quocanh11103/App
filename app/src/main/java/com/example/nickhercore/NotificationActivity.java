package com.example.nickhercore;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private AppDatabaseHelper db;
    private LinearLayout root;
    private LinearLayout favoritesBox;
    private LinearLayout logsBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new AppDatabaseHelper(this);

        createLayout();
        renderFavorites();
        renderLogs();
    }

    private void createLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#07111F"));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(26), dp(16), dp(18));
        scrollView.addView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("←", 30, Color.parseColor("#00D68F"), false);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(42), dp(46)));

        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);

        TextView app = text("NickherCore", 20, Color.WHITE, true);
        TextView sub = text("World Cup 2026", 13, Color.parseColor("#B8C7D9"), false);

        nameBox.addView(app);
        nameBox.addView(sub);

        top.addView(nameBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView bell = text("🔔", 28, Color.parseColor("#00D68F"), false);
        bell.setGravity(Gravity.CENTER);
        top.addView(bell, new LinearLayout.LayoutParams(dp(52), dp(52)));

        root.addView(top);

        TextView title = text("Thông báo", 36, Color.WHITE, true);
        title.setPadding(0, dp(22), 0, dp(8));
        root.addView(title);

        TextView desc = text("Chỉ nhận thông báo cho trận đã thêm vào Yêu thích.", 15, Color.parseColor("#B8C7D9"), false);
        root.addView(desc);

        LinearLayout settings = card();
        settings.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView settingsTitle = text("Cài đặt thông báo", 21, Color.WHITE, true);
        settings.addView(settingsTitle);

        settings.addView(settingRow("👥", "Đội hình ra sân", "notify_lineup"));
        settings.addView(settingRow("📈", "Cập nhật tỉ số", "notify_score"));
        settings.addView(settingRow("🎯", "Kết thúc trận đấu", "notify_finished"));
        settings.addView(settingRow("📰", "Tin tức", "notify_news"));

        root.addView(settings);

        favoritesBox = card();
        favoritesBox.setPadding(dp(14), dp(14), dp(14), dp(14));
        root.addView(favoritesBox);

        logsBox = card();
        logsBox.setPadding(dp(14), dp(14), dp(14), dp(14));
        root.addView(logsBox);

        setContentView(scrollView);
    }

    private LinearLayout settingRow(String icon, String label, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(10));

        TextView left = text(icon + "  " + label, 16, Color.WHITE, false);
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch sw = new Switch(this);
        sw.setChecked(db.getSetting(key));
        sw.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> db.setSetting(key, isChecked));

        row.addView(sw);

        return row;
    }

    private void renderFavorites() {
        favoritesBox.removeAllViews();

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text("Trận yêu thích", 21, Color.WHITE, true);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView manage = text("Quản lý  ›", 14, Color.parseColor("#B8C7D9"), false);
        titleRow.addView(manage);

        favoritesBox.addView(titleRow);

        List<AppDatabaseHelper.FavoriteItem> favorites = db.getFavoriteItems();

        if (favorites.isEmpty()) {
            TextView empty = text("Chưa có trận yêu thích. Bấm ☆ ở trang chủ để thêm trận.", 14, Color.parseColor("#8FA8C5"), false);
            empty.setPadding(0, dp(14), 0, 0);
            favoritesBox.addView(empty);
            return;
        }

        for (AppDatabaseHelper.FavoriteItem item : favorites) {
            favoritesBox.addView(favoriteRow(item));
        }
    }

    private LinearLayout favoriteRow(AppDatabaseHelper.FavoriteItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.setBackground(roundBg("#07111F", "#26384F", 1, 14));

        TextView left = text(item.home + "\n" + safe(item.roundName), 13, Color.WHITE, true);
        left.setGravity(Gravity.CENTER);
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView mid = text("VS\n" + safe(item.venue), 13, Color.parseColor("#B8C7D9"), false);
        mid.setGravity(Gravity.CENTER);
        row.addView(mid, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView right = text(item.away + "\n" + (item.notifyEnabled ? "🔔 Bật" : "🔕 Tắt"), 13, item.notifyEnabled ? Color.parseColor("#00D68F") : Color.parseColor("#8FA8C5"), true);
        right.setGravity(Gravity.CENTER);
        right.setOnClickListener(v -> {
            db.toggleFavoriteNotify(item.matchNo, !item.notifyEnabled);
            renderFavorites();
        });
        row.addView(right, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(10), 0, 0);
        row.setLayoutParams(params);

        return row;
    }

    private void renderLogs() {
        logsBox.removeAllViews();

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text("Hoạt động gần đây", 21, Color.WHITE, true);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView all = text("Xem tất cả  ›", 14, Color.parseColor("#B8C7D9"), false);
        titleRow.addView(all);

        logsBox.addView(titleRow);

        List<AppDatabaseHelper.LogItem> logs = db.getLogs();

        if (logs.isEmpty()) {
            TextView empty = text("Chưa có thông báo nào.", 14, Color.parseColor("#8FA8C5"), false);
            empty.setPadding(0, dp(14), 0, 0);
            logsBox.addView(empty);
            return;
        }

        for (AppDatabaseHelper.LogItem log : logs) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(12), 0, dp(12));

            TextView icon = text(getIcon(log.type), 22, Color.parseColor("#00D68F"), false);
            icon.setGravity(Gravity.CENTER);
            row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

            TextView body = text(log.title + "\n" + log.body + "\n" + log.createdAt, 14, Color.WHITE, false);
            row.addView(body, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            logsBox.addView(row);
        }
    }

    private String getIcon(String type) {
        if ("lineup".equals(type)) return "👥";
        if ("score".equals(type)) return "1:0";
        if ("finished".equals(type)) return "🎯";
        return "🔔";
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundBg("#10233A", "#26384F", 1, 18));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(18), 0, 0);
        card.setLayoutParams(params);

        return card;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value == null ? "" : value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
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
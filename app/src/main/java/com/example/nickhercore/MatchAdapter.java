package com.example.nickhercore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private final Context context;
    private final AppDatabaseHelper db;
    private List<Match> items = new ArrayList<>();

    public MatchAdapter(Context context, List<Match> items, AppDatabaseHelper db) {
        this.context = context;
        this.items = items;
        this.db = db;
    }

    public void setItems(List<Match> newItems) {
        this.items = newItems == null ? new ArrayList<>() : newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(roundBg("#10233A", "#26384F", 1, 18));

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(params);

        return new MatchViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder h, int position) {
        Match match = items.get(position);

        h.card.removeAllViews();

        LinearLayout top = new LinearLayout(context);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView status = text(getStatusChip(match), 12, Color.WHITE, true);
        status.setGravity(Gravity.CENTER);
        status.setBackground(roundBg(getStatusColor(match), getStatusColor(match), 0, 8));
        top.addView(status, new LinearLayout.LayoutParams(dp(76), dp(34)));

        TextView round = text(safe(match.getRound()) + "  ›", 13, Color.parseColor("#B8C7D9"), false);
        round.setGravity(Gravity.CENTER);
        top.addView(round, new LinearLayout.LayoutParams(0, dp(34), 1));

        TextView star = text(db.isFavorite(match.getFixtureId()) ? "★" : "☆", 28, Color.parseColor("#FFD45A"), false);
        star.setGravity(Gravity.CENTER);
        star.setOnClickListener(v -> {
            boolean nowFavorite = !db.isFavorite(match.getFixtureId());
            db.setFavorite(match, nowFavorite);

            Toast.makeText(
                    context,
                    nowFavorite ? "Đã thêm vào Yêu thích. Trận này sẽ có thông báo." : "Đã bỏ khỏi Yêu thích",
                    Toast.LENGTH_SHORT
            ).show();

            notifyItemChanged(position);
        });
        top.addView(star, new LinearLayout.LayoutParams(dp(42), dp(40)));

        h.card.addView(top);

        LinearLayout middle = new LinearLayout(context);
        middle.setGravity(Gravity.CENTER);
        middle.setPadding(0, dp(10), 0, dp(8));

        LinearLayout home = teamBox(match.getHomeTeam(), match.getHomeLogo());
        LinearLayout away = teamBox(match.getAwayTeam(), match.getAwayLogo());

        TextView score = text(scoreText(match), 34, Color.WHITE, true);
        score.setGravity(Gravity.CENTER);

        middle.addView(home, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        middle.addView(score, new LinearLayout.LayoutParams(dp(110), LinearLayout.LayoutParams.WRAP_CONTENT));
        middle.addView(away, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        h.card.addView(middle);

        TextView venue = text("🏟  " + safe(match.getStadium()), 13, Color.parseColor("#B8C7D9"), false);
        venue.setGravity(Gravity.CENTER);
        venue.setPadding(0, 0, 0, dp(10));
        h.card.addView(venue);

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        actions.addView(action("👕  Đội hình", () -> openLineup(match)), new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(action("▮▮  Thống kê", () -> openStats(match)), new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(action("☷  Diễn biến", () -> openEvents(match)), new LinearLayout.LayoutParams(0, dp(42), 1));

        h.card.addView(actions);

        h.card.setOnClickListener(v -> openDetail(match));
    }

    private LinearLayout teamBox(String name, String logoUrl) {
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);

        ImageView img = new ImageView(context);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(img);
        } else {
            img.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        box.addView(img, new LinearLayout.LayoutParams(dp(64), dp(64)));

        TextView tv = text(safe(name), 16, Color.WHITE, true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(8), 0, 0);
        box.addView(tv);

        return box;
    }

    private TextView action(String label, Runnable runnable) {
        TextView tv = text(label, 12, Color.parseColor("#D7E3F2"), true);
        tv.setGravity(Gravity.CENTER);
        tv.setBackground(roundBg("#07111F", "#26384F", 1, 12));
        tv.setOnClickListener(v -> runnable.run());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(42), 1);
        params.setMargins(dp(3), 0, dp(3), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private void openDetail(Match match) {
        Intent intent = new Intent(context, MatchDetailActivity.class);

        intent.putExtra("FIXTURE_ID", match.getFixtureId());
        intent.putExtra("HOME_TEAM", safe(match.getHomeTeam()));
        intent.putExtra("AWAY_TEAM", safe(match.getAwayTeam()));
        intent.putExtra("LEAGUE", safe(match.getLeague()));
        intent.putExtra("ROUND", safe(match.getRound()));
        intent.putExtra("DATE", safe(match.getDate()));
        intent.putExtra("STATUS", safe(match.getStatus()));
        intent.putExtra("VENUE", safe(match.getStadium()));
        intent.putExtra("HOME_SCORE", match.getHomeScore());
        intent.putExtra("AWAY_SCORE", match.getAwayScore());

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    private void openLineup(Match match) {
        Intent intent = new Intent(context, LineupPitchActivity.class);

        intent.putExtra("MATCH_NO", match.getFixtureId());
        intent.putExtra("HOME_TEAM", safe(match.getHomeTeam()));
        intent.putExtra("AWAY_TEAM", safe(match.getAwayTeam()));
        intent.putExtra("SCORE_TEXT", match.getHomeScore() + " - " + match.getAwayScore());

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    private void openStats(Match match) {
        Intent intent = new Intent(context, MatchStatsActivity.class);

        intent.putExtra("MATCH_NO", match.getFixtureId());
        intent.putExtra("HOME_TEAM", safe(match.getHomeTeam()));
        intent.putExtra("AWAY_TEAM", safe(match.getAwayTeam()));
        intent.putExtra("SCORE_TEXT", match.getHomeScore() + " - " + match.getAwayScore());

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    private void openEvents(Match match) {
        Intent intent = new Intent(context, MatchEventsActivity.class);

        intent.putExtra("MATCH_NO", match.getFixtureId());
        intent.putExtra("HOME_TEAM", safe(match.getHomeTeam()));
        intent.putExtra("AWAY_TEAM", safe(match.getAwayTeam()));
        intent.putExtra("SCORE_TEXT", match.getHomeScore() + " - " + match.getAwayScore());
        intent.putExtra("STATUS_TEXT", safe(match.getStatus()));

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    private String scoreText(Match m) {
        String status = safe(m.getStatus()).toLowerCase(Locale.ROOT);

        if (status.contains("scheduled")) {
            return "VS";
        }

        return m.getHomeScore() + " - " + m.getAwayScore();
    }

    private String getStatusChip(Match m) {
        String status = safe(m.getStatus()).toLowerCase(Locale.ROOT);

        if (status.contains("finished")) return "FT";
        if (status.contains("live")) return "LIVE";
        if (status.contains("scheduled")) return "Sắp đá";

        return safe(m.getStatus()).isEmpty() ? "Match" : m.getStatus();
    }

    private String getStatusColor(Match m) {
        String status = safe(m.getStatus()).toLowerCase(Locale.ROOT);

        if (status.contains("finished")) return "#3B4450";
        if (status.contains("live")) return "#E90B2F";

        return "#0E5BA8";
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(context);
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
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;

        public MatchViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            card = itemView;
        }
    }
}
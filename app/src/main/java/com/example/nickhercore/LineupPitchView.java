package com.example.nickhercore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineupPitchView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<PitchPlayer> homePlayers = new ArrayList<>();
    private final List<PitchPlayer> awayPlayers = new ArrayList<>();

    private String homeTeam = "Home";
    private String awayTeam = "Away";
    private String homeFormation = "4-3-3";
    private String awayFormation = "4-2-3-1";

    public LineupPitchView(Context context) {
        super(context);
        initDefaultPlayers();
    }

    public LineupPitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefaultPlayers();
    }

    public LineupPitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaultPlayers();
    }

    public void setMatch(String homeTeam, String awayTeam, int fixtureId) {
        this.homeTeam = safe(homeTeam, "Home");
        this.awayTeam = safe(awayTeam, "Away");

        homePlayers.clear();
        awayPlayers.clear();

        if (fixtureId == 1006) {
            homeFormation = "4-2-3-1";
            awayFormation = "4-3-3";
            loadGermanySpain();
        } else if (fixtureId == 1003) {
            homeFormation = "4-3-3";
            awayFormation = "4-2-3-1";
            loadArgentinaFrance();
        } else {
            homeFormation = "4-3-3";
            awayFormation = "4-2-3-1";
            initDefaultPlayers();
        }

        invalidate();
    }

    private void initDefaultPlayers() {
        homePlayers.clear();
        awayPlayers.clear();

        homePlayers.add(new PitchPlayer("1", "GK", "7.0", 50, 8));
        homePlayers.add(new PitchPlayer("2", "RB", "6.8", 20, 22));
        homePlayers.add(new PitchPlayer("4", "CB", "7.1", 40, 23));
        homePlayers.add(new PitchPlayer("5", "CB", "6.9", 60, 23));
        homePlayers.add(new PitchPlayer("3", "LB", "7.0", 80, 22));
        homePlayers.add(new PitchPlayer("6", "DM", "7.2", 35, 38));
        homePlayers.add(new PitchPlayer("8", "CM", "7.1", 50, 42));
        homePlayers.add(new PitchPlayer("10", "AM", "7.5", 65, 38));
        homePlayers.add(new PitchPlayer("7", "RW", "7.3", 25, 56));
        homePlayers.add(new PitchPlayer("9", "ST", "7.8", 50, 60));
        homePlayers.add(new PitchPlayer("11", "LW", "7.4", 75, 56));

        awayPlayers.add(new PitchPlayer("1", "GK", "6.9", 50, 92));
        awayPlayers.add(new PitchPlayer("2", "RB", "6.7", 20, 78));
        awayPlayers.add(new PitchPlayer("4", "CB", "6.8", 40, 77));
        awayPlayers.add(new PitchPlayer("5", "CB", "7.0", 60, 77));
        awayPlayers.add(new PitchPlayer("3", "LB", "6.9", 80, 78));
        awayPlayers.add(new PitchPlayer("6", "DM", "7.1", 38, 63));
        awayPlayers.add(new PitchPlayer("8", "DM", "7.0", 62, 63));
        awayPlayers.add(new PitchPlayer("7", "RW", "7.2", 25, 47));
        awayPlayers.add(new PitchPlayer("10", "AM", "7.5", 50, 44));
        awayPlayers.add(new PitchPlayer("11", "LW", "7.1", 75, 47));
        awayPlayers.add(new PitchPlayer("9", "ST", "7.4", 50, 33));
    }

    private void loadGermanySpain() {
        homePlayers.add(new PitchPlayer("1", "Neuer", "7.0", 50, 8));
        homePlayers.add(new PitchPlayer("2", "Rüdiger", "7.2", 22, 23));
        homePlayers.add(new PitchPlayer("4", "Tah", "6.9", 42, 24));
        homePlayers.add(new PitchPlayer("3", "Mittel.", "6.8", 62, 24));
        homePlayers.add(new PitchPlayer("18", "Raum", "7.1", 82, 23));
        homePlayers.add(new PitchPlayer("6", "Kimmich", "7.5", 38, 39));
        homePlayers.add(new PitchPlayer("8", "Kroos", "7.8", 62, 39));
        homePlayers.add(new PitchPlayer("10", "Musiala", "8.1", 25, 55));
        homePlayers.add(new PitchPlayer("21", "Gündo.", "7.0", 50, 56));
        homePlayers.add(new PitchPlayer("7", "Wirtz", "7.3", 75, 55));
        homePlayers.add(new PitchPlayer("9", "Havertz", "7.6", 50, 69));

        awayPlayers.add(new PitchPlayer("23", "Simon", "6.7", 50, 92));
        awayPlayers.add(new PitchPlayer("2", "Carvajal", "6.8", 20, 78));
        awayPlayers.add(new PitchPlayer("3", "Le Norm.", "6.9", 40, 77));
        awayPlayers.add(new PitchPlayer("14", "Laporte", "7.1", 60, 77));
        awayPlayers.add(new PitchPlayer("24", "Cucur.", "6.8", 80, 78));
        awayPlayers.add(new PitchPlayer("16", "Rodri", "7.6", 50, 63));
        awayPlayers.add(new PitchPlayer("8", "Pedri", "7.4", 35, 50));
        awayPlayers.add(new PitchPlayer("20", "Olmo", "7.2", 65, 50));
        awayPlayers.add(new PitchPlayer("19", "Yamal", "7.8", 25, 34));
        awayPlayers.add(new PitchPlayer("7", "Morata", "7.0", 50, 30));
        awayPlayers.add(new PitchPlayer("11", "Williams", "7.5", 75, 34));
    }

    private void loadArgentinaFrance() {
        homePlayers.add(new PitchPlayer("23", "Martinez", "7.4", 50, 8));
        homePlayers.add(new PitchPlayer("26", "Molina", "6.8", 20, 23));
        homePlayers.add(new PitchPlayer("13", "Romero", "7.3", 40, 24));
        homePlayers.add(new PitchPlayer("19", "Otamendi", "7.0", 60, 24));
        homePlayers.add(new PitchPlayer("3", "Taglia.", "6.9", 80, 23));
        homePlayers.add(new PitchPlayer("7", "De Paul", "7.2", 35, 39));
        homePlayers.add(new PitchPlayer("24", "Enzo", "7.5", 50, 43));
        homePlayers.add(new PitchPlayer("20", "Mac All.", "7.4", 65, 39));
        homePlayers.add(new PitchPlayer("11", "Di Maria", "8.0", 25, 58));
        homePlayers.add(new PitchPlayer("9", "Alvarez", "7.1", 50, 62));
        homePlayers.add(new PitchPlayer("10", "Messi", "8.9", 75, 58));

        awayPlayers.add(new PitchPlayer("1", "Maignan", "6.7", 50, 92));
        awayPlayers.add(new PitchPlayer("5", "Kounde", "6.5", 20, 78));
        awayPlayers.add(new PitchPlayer("4", "Upame.", "6.8", 40, 77));
        awayPlayers.add(new PitchPlayer("17", "Saliba", "7.0", 60, 77));
        awayPlayers.add(new PitchPlayer("22", "Theo", "7.2", 80, 78));
        awayPlayers.add(new PitchPlayer("8", "Tchou.", "7.0", 38, 63));
        awayPlayers.add(new PitchPlayer("14", "Rabiot", "6.9", 62, 63));
        awayPlayers.add(new PitchPlayer("7", "Griez.", "7.4", 50, 48));
        awayPlayers.add(new PitchPlayer("11", "Dembele", "6.6", 25, 34));
        awayPlayers.add(new PitchPlayer("9", "Giroud", "6.8", 50, 30));
        awayPlayers.add(new PitchPlayer("10", "Mbappe", "8.3", 75, 34));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int padding = dp(10);
        RectF pitch = new RectF(padding, padding, width - padding, height - padding);

        drawPitch(canvas, pitch);
        drawHeader(canvas, pitch);
        drawPlayers(canvas, pitch, homePlayers, true);
        drawPlayers(canvas, pitch, awayPlayers, false);
    }

    private void drawPitch(Canvas canvas, RectF pitch) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#062D2E"));
        canvas.drawRoundRect(pitch, dp(16), dp(16), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.parseColor("#4B6F70"));
        canvas.drawRoundRect(pitch, dp(16), dp(16), paint);

        float left = pitch.left + dp(14);
        float right = pitch.right - dp(14);
        float top = pitch.top + dp(42);
        float bottom = pitch.bottom - dp(14);
        float centerY = (top + bottom) / 2f;
        float centerX = (left + right) / 2f;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.parseColor("#416466"));

        canvas.drawLine(left, centerY, right, centerY, paint);
        canvas.drawCircle(centerX, centerY, dp(38), paint);

        RectF topBox = new RectF(centerX - dp(72), top, centerX + dp(72), top + dp(54));
        RectF bottomBox = new RectF(centerX - dp(72), bottom - dp(54), centerX + dp(72), bottom);
        canvas.drawRect(topBox, paint);
        canvas.drawRect(bottomBox, paint);

        RectF topSmall = new RectF(centerX - dp(42), top, centerX + dp(42), top + dp(24));
        RectF bottomSmall = new RectF(centerX - dp(42), bottom - dp(24), centerX + dp(42), bottom);
        canvas.drawRect(topSmall, paint);
        canvas.drawRect(bottomSmall, paint);
    }

    private void drawHeader(Canvas canvas, RectF pitch) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#08213A"));
        RectF header = new RectF(pitch.left, pitch.top, pitch.right, pitch.top + dp(38));
        canvas.drawRoundRect(header, dp(16), dp(16), paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(sp(11));
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(homeFormation, pitch.left + dp(14), pitch.top + dp(25), paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(awayFormation, pitch.right - dp(14), pitch.top + dp(25), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.parseColor("#A9B4C2"));
        canvas.drawText("FORMATION", pitch.centerX(), pitch.top + dp(25), paint);

        paint.setFakeBoldText(false);
    }

    private void drawPlayers(Canvas canvas, RectF pitch, List<PitchPlayer> players, boolean home) {
        float fieldTop = pitch.top + dp(48);
        float fieldBottom = pitch.bottom - dp(14);
        float fieldLeft = pitch.left + dp(18);
        float fieldRight = pitch.right - dp(18);
        float fieldWidth = fieldRight - fieldLeft;
        float fieldHeight = fieldBottom - fieldTop;

        for (PitchPlayer player : players) {
            float x = fieldLeft + fieldWidth * player.xPercent / 100f;
            float y = fieldTop + fieldHeight * player.yPercent / 100f;

            drawPlayer(canvas, x, y, player, home);
        }
    }

    private void drawPlayer(Canvas canvas, float x, float y, PitchPlayer player, boolean home) {
        int shirtColor = home ? Color.parseColor("#1565C0") : Color.parseColor("#B91C1C");
        int ringColor = home ? Color.parseColor("#16C47F") : Color.parseColor("#F59E0B");

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#07111F"));
        canvas.drawCircle(x, y, dp(17), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(ringColor);
        canvas.drawCircle(x, y, dp(17), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shirtColor);
        canvas.drawCircle(x, y, dp(13), paint);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(sp(10));
        paint.setFakeBoldText(true);
        canvas.drawText(player.number, x, y + dp(4), paint);

        paint.setFakeBoldText(false);

        RectF ratingBox = new RectF(x + dp(8), y - dp(24), x + dp(34), y - dp(8));
        paint.setColor(getRatingColor(player.rating));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(ratingBox, dp(4), dp(4), paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(sp(8));
        paint.setFakeBoldText(true);
        canvas.drawText(player.rating, ratingBox.centerX(), ratingBox.centerY() + dp(3), paint);
        paint.setFakeBoldText(false);

        String name = player.name;
        if (name.length() > 9) {
            name = name.substring(0, 8) + ".";
        }

        paint.setColor(Color.WHITE);
        paint.setTextSize(sp(9));
        paint.setFakeBoldText(true);
        canvas.drawText(name, x, y + dp(31), paint);
        paint.setFakeBoldText(false);
    }

    private int getRatingColor(String rating) {
        try {
            float value = Float.parseFloat(rating);
            if (value >= 8.0f) return Color.parseColor("#16A34A");
            if (value >= 7.0f) return Color.parseColor("#65A30D");
            if (value >= 6.5f) return Color.parseColor("#F59E0B");
            return Color.parseColor("#EA580C");
        } catch (Exception e) {
            return Color.parseColor("#64748B");
        }
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private static class PitchPlayer {
        String number;
        String name;
        String rating;
        float xPercent;
        float yPercent;

        PitchPlayer(String number, String name, String rating, float xPercent, float yPercent) {
            this.number = number;
            this.name = name;
            this.rating = rating;
            this.xPercent = xPercent;
            this.yPercent = yPercent;
        }
    }
}
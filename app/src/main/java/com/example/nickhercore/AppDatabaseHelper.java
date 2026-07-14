package com.example.nickhercore;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "nickhercore.db";
    private static final int DB_VERSION = 5;

    private final Context context;

    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "email TEXT UNIQUE," +
                        "password TEXT)"
        );

        db.execSQL(
                "CREATE TABLE favorites (" +
                        "match_no INTEGER PRIMARY KEY," +
                        "home TEXT," +
                        "away TEXT," +
                        "league TEXT," +
                        "round_name TEXT," +
                        "date_text TEXT," +
                        "status TEXT," +
                        "venue TEXT," +
                        "home_score INTEGER," +
                        "away_score INTEGER," +
                        "notify_enabled INTEGER DEFAULT 1," +
                        "lineups_notified INTEGER DEFAULT 0," +
                        "last_status TEXT," +
                        "last_home_score INTEGER," +
                        "last_away_score INTEGER)"
        );

        db.execSQL(
                "CREATE TABLE settings (" +
                        "key_name TEXT PRIMARY KEY," +
                        "value INTEGER)"
        );

        db.execSQL(
                "CREATE TABLE logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "body TEXT," +
                        "type TEXT," +
                        "created_at TEXT)"
        );

        insertDefaultSettings(db);
    }

    private void insertDefaultSettings(SQLiteDatabase db) {
        insertSetting(db, "notify_lineup", 1);
        insertSetting(db, "notify_score", 1);
        insertSetting(db, "notify_finished", 1);
        insertSetting(db, "notify_news", 0);
    }

    private void insertSetting(SQLiteDatabase db, String key, int value) {
        ContentValues cv = new ContentValues();
        cv.put("key_name", key);
        cv.put("value", value);
        db.insert("settings", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS settings");
        db.execSQL("DROP TABLE IF EXISTS logs");
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);

        try {
            long result = db.insertOrThrow("users", null, cv);
            return result > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, name, email FROM users WHERE email=? AND password=?",
                new String[]{email, password}
        );

        boolean ok = c.moveToFirst();

        if (ok) {
            SharedPreferences sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
            sp.edit()
                    .putBoolean("logged_in", true)
                    .putString("email", c.getString(c.getColumnIndexOrThrow("email")))
                    .putString("name", c.getString(c.getColumnIndexOrThrow("name")))
                    .apply();
        }

        c.close();
        return ok;
    }

    public boolean isLoggedIn() {
        SharedPreferences sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        return sp.getBoolean("logged_in", false);
    }

    public String getCurrentUserName() {
        SharedPreferences sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        return sp.getString("name", "Người dùng");
    }

    public void logout() {
        SharedPreferences sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    public boolean isFavorite(int matchNo) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT match_no FROM favorites WHERE match_no=?",
                new String[]{String.valueOf(matchNo)}
        );

        boolean exists = c.moveToFirst();
        c.close();

        return exists;
    }

    public void addFavorite(Match match) {
        SQLiteDatabase db = getWritableDatabase();

        String status = safe(match.getStatus());
        int alreadyHasLineup = status.toLowerCase(Locale.ROOT).contains("finished") ? 1 : 0;

        ContentValues cv = new ContentValues();
        cv.put("match_no", match.getFixtureId());
        cv.put("home", safe(match.getHomeTeam()));
        cv.put("away", safe(match.getAwayTeam()));
        cv.put("league", safe(match.getLeague()));
        cv.put("round_name", safe(match.getRound()));
        cv.put("date_text", safe(match.getDate()));
        cv.put("status", status);
        cv.put("venue", safe(match.getStadium()));
        cv.put("home_score", match.getHomeScore());
        cv.put("away_score", match.getAwayScore());
        cv.put("notify_enabled", 1);
        cv.put("lineups_notified", alreadyHasLineup);
        cv.put("last_status", status);
        cv.put("last_home_score", match.getHomeScore());
        cv.put("last_away_score", match.getAwayScore());

        db.insertWithOnConflict("favorites", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeFavorite(int matchNo) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("favorites", "match_no=?", new String[]{String.valueOf(matchNo)});
    }

    public void setFavorite(Match match, boolean favorite) {
        if (favorite) {
            addFavorite(match);
        } else {
            removeFavorite(match.getFixtureId());
        }
    }

    public List<Integer> getFavoriteIds() {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT match_no FROM favorites", null);

        while (c.moveToNext()) {
            ids.add(c.getInt(c.getColumnIndexOrThrow("match_no")));
        }

        c.close();
        return ids;
    }

    public FavoriteState getFavoriteState(int matchNo) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM favorites WHERE match_no=?",
                new String[]{String.valueOf(matchNo)}
        );

        FavoriteState state = null;

        if (c.moveToFirst()) {
            state = new FavoriteState();
            state.matchNo = matchNo;
            state.notifyEnabled = c.getInt(c.getColumnIndexOrThrow("notify_enabled")) == 1;
            state.lineupsNotified = c.getInt(c.getColumnIndexOrThrow("lineups_notified")) == 1;
            state.lastStatus = c.getString(c.getColumnIndexOrThrow("last_status"));
            state.lastHomeScore = c.getInt(c.getColumnIndexOrThrow("last_home_score"));
            state.lastAwayScore = c.getInt(c.getColumnIndexOrThrow("last_away_score"));
        }

        c.close();
        return state;
    }

    public void updateFavoriteSnapshot(int matchNo, int homeScore, int awayScore, String status, boolean lineupsNotified) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("last_home_score", homeScore);
        cv.put("last_away_score", awayScore);
        cv.put("last_status", status);
        cv.put("status", status);
        cv.put("home_score", homeScore);
        cv.put("away_score", awayScore);
        cv.put("lineups_notified", lineupsNotified ? 1 : 0);

        db.update("favorites", cv, "match_no=?", new String[]{String.valueOf(matchNo)});
    }

    public void toggleFavoriteNotify(int matchNo, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("notify_enabled", enabled ? 1 : 0);

        db.update("favorites", cv, "match_no=?", new String[]{String.valueOf(matchNo)});
    }

    public List<FavoriteItem> getFavoriteItems() {
        List<FavoriteItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM favorites ORDER BY date_text ASC", null);

        while (c.moveToNext()) {
            FavoriteItem item = new FavoriteItem();
            item.matchNo = c.getInt(c.getColumnIndexOrThrow("match_no"));
            item.home = c.getString(c.getColumnIndexOrThrow("home"));
            item.away = c.getString(c.getColumnIndexOrThrow("away"));
            item.roundName = c.getString(c.getColumnIndexOrThrow("round_name"));
            item.venue = c.getString(c.getColumnIndexOrThrow("venue"));
            item.status = c.getString(c.getColumnIndexOrThrow("status"));
            item.homeScore = c.getInt(c.getColumnIndexOrThrow("home_score"));
            item.awayScore = c.getInt(c.getColumnIndexOrThrow("away_score"));
            item.notifyEnabled = c.getInt(c.getColumnIndexOrThrow("notify_enabled")) == 1;
            list.add(item);
        }

        c.close();
        return list;
    }

    public boolean getSetting(String key) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT value FROM settings WHERE key_name=?",
                new String[]{key}
        );

        boolean result = false;

        if (c.moveToFirst()) {
            result = c.getInt(c.getColumnIndexOrThrow("value")) == 1;
        }

        c.close();
        return result;
    }

    public void setSetting(String key, boolean value) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("key_name", key);
        cv.put("value", value ? 1 : 0);

        db.insertWithOnConflict("settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addLog(String title, String body, String type) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("body", body);
        cv.put("type", type);
        cv.put("created_at", now());

        db.insert("logs", null, cv);
    }

    public List<LogItem> getLogs() {
        List<LogItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM logs ORDER BY id DESC LIMIT 20",
                null
        );

        while (c.moveToNext()) {
            LogItem item = new LogItem();
            item.title = c.getString(c.getColumnIndexOrThrow("title"));
            item.body = c.getString(c.getColumnIndexOrThrow("body"));
            item.type = c.getString(c.getColumnIndexOrThrow("type"));
            item.createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
            list.add(item);
        }

        c.close();
        return list;
    }

    private String now() {
        return new SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(new Date());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class FavoriteState {
        public int matchNo;
        public boolean notifyEnabled;
        public boolean lineupsNotified;
        public String lastStatus;
        public int lastHomeScore;
        public int lastAwayScore;
    }

    public static class FavoriteItem {
        public int matchNo;
        public String home;
        public String away;
        public String roundName;
        public String venue;
        public String status;
        public int homeScore;
        public int awayScore;
        public boolean notifyEnabled;
    }

    public static class LogItem {
        public String title;
        public String body;
        public String type;
        public String createdAt;
    }
}
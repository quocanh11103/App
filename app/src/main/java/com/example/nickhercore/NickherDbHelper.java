package com.example.nickhercore;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NickherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "nickhercore.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_MATCHES = "matches";

    public static final String COL_ID = "id";
    public static final String COL_LEAGUE = "league";
    public static final String COL_HOME_TEAM = "home_team";
    public static final String COL_AWAY_TEAM = "away_team";
    public static final String COL_HOME_SCORE = "home_score";
    public static final String COL_AWAY_SCORE = "away_score";
    public static final String COL_MATCH_TIME = "match_time";
    public static final String COL_STATUS = "status";

    public NickherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MATCHES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LEAGUE + " TEXT NOT NULL, " +
                COL_HOME_TEAM + " TEXT NOT NULL, " +
                COL_AWAY_TEAM + " TEXT NOT NULL, " +
                COL_HOME_SCORE + " INTEGER DEFAULT 0, " +
                COL_AWAY_SCORE + " INTEGER DEFAULT 0, " +
                COL_MATCH_TIME + " TEXT, " +
                COL_STATUS + " TEXT" +
                ")";

        db.execSQL(createTable);
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        insertMatch(db, "Premier League", "Manchester United", "Chelsea", 2, 1, "20:00", "Đang đá");
        insertMatch(db, "La Liga", "Barcelona", "Real Madrid", 1, 1, "22:00", "Hiệp 2");
        insertMatch(db, "Serie A", "AC Milan", "Inter Milan", 0, 0, "01:45", "Sắp diễn ra");
        insertMatch(db, "Bundesliga", "Bayern Munich", "Dortmund", 3, 2, "Kết thúc", "FT");
        insertMatch(db, "V-League", "Hà Nội FC", "Công An Hà Nội", 1, 0, "18:00", "Đang đá");
    }

    private void insertMatch(SQLiteDatabase db, String league, String homeTeam, String awayTeam,
                             int homeScore, int awayScore, String matchTime, String status) {
        ContentValues values = new ContentValues();
        values.put(COL_LEAGUE, league);
        values.put(COL_HOME_TEAM, homeTeam);
        values.put(COL_AWAY_TEAM, awayTeam);
        values.put(COL_HOME_SCORE, homeScore);
        values.put(COL_AWAY_SCORE, awayScore);
        values.put(COL_MATCH_TIME, matchTime);
        values.put(COL_STATUS, status);
        db.insert(TABLE_MATCHES, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATCHES);
        onCreate(db);
    }
}

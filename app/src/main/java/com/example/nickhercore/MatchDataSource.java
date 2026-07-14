package com.example.nickhercore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class MatchDataSource {

    private NickherDbHelper dbHelper;

    public MatchDataSource(Context context) {
        dbHelper = new NickherDbHelper(context);
    }

    public long addMatch(Match match) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NickherDbHelper.COL_LEAGUE, match.getLeague());
        values.put(NickherDbHelper.COL_HOME_TEAM, match.getHomeTeam());
        values.put(NickherDbHelper.COL_AWAY_TEAM, match.getAwayTeam());
        values.put(NickherDbHelper.COL_HOME_SCORE, match.getHomeScore());
        values.put(NickherDbHelper.COL_AWAY_SCORE, match.getAwayScore());
        values.put(NickherDbHelper.COL_MATCH_TIME, match.getMatchTime());
        values.put(NickherDbHelper.COL_STATUS, match.getStatus());

        long result = db.insert(NickherDbHelper.TABLE_MATCHES, null, values);
        db.close();
        return result;
    }

    public ArrayList<Match> getAllMatches() {
        ArrayList<Match> matchList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                NickherDbHelper.TABLE_MATCHES,
                null,
                null,
                null,
                null,
                null,
                NickherDbHelper.COL_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Match match = createMatchFromCursor(cursor);
                matchList.add(match);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return matchList;
    }

    public Match getMatchById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Match match = null;

        Cursor cursor = db.query(
                NickherDbHelper.TABLE_MATCHES,
                null,
                NickherDbHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            match = createMatchFromCursor(cursor);
        }

        cursor.close();
        db.close();
        return match;
    }

    public void resetSampleData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(NickherDbHelper.TABLE_MATCHES, null, null);

        insertMatchDirect(db, "Premier League", "Manchester United", "Chelsea", 2, 1, "20:00", "Đang đá");
        insertMatchDirect(db, "La Liga", "Barcelona", "Real Madrid", 1, 1, "22:00", "Hiệp 2");
        insertMatchDirect(db, "Serie A", "AC Milan", "Inter Milan", 0, 0, "01:45", "Sắp diễn ra");
        insertMatchDirect(db, "Bundesliga", "Bayern Munich", "Dortmund", 3, 2, "Kết thúc", "FT");
        insertMatchDirect(db, "V-League", "Hà Nội FC", "Công An Hà Nội", 1, 0, "18:00", "Đang đá");

        db.close();
    }

    private void insertMatchDirect(SQLiteDatabase db, String league, String homeTeam, String awayTeam,
                                   int homeScore, int awayScore, String matchTime, String status) {
        ContentValues values = new ContentValues();
        values.put(NickherDbHelper.COL_LEAGUE, league);
        values.put(NickherDbHelper.COL_HOME_TEAM, homeTeam);
        values.put(NickherDbHelper.COL_AWAY_TEAM, awayTeam);
        values.put(NickherDbHelper.COL_HOME_SCORE, homeScore);
        values.put(NickherDbHelper.COL_AWAY_SCORE, awayScore);
        values.put(NickherDbHelper.COL_MATCH_TIME, matchTime);
        values.put(NickherDbHelper.COL_STATUS, status);
        db.insert(NickherDbHelper.TABLE_MATCHES, null, values);
    }

    private Match createMatchFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_ID));
        String league = cursor.getString(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_LEAGUE));
        String homeTeam = cursor.getString(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_HOME_TEAM));
        String awayTeam = cursor.getString(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_AWAY_TEAM));
        int homeScore = cursor.getInt(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_HOME_SCORE));
        int awayScore = cursor.getInt(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_AWAY_SCORE));
        String matchTime = cursor.getString(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_MATCH_TIME));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(NickherDbHelper.COL_STATUS));

        return new Match(id, league, homeTeam, awayTeam, homeScore, awayScore, matchTime, status);
    }
}

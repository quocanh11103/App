package com.example.nickhercore;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddMatchActivity extends Activity {

    private EditText edtLeague;
    private EditText edtHomeTeam;
    private EditText edtAwayTeam;
    private EditText edtHomeScore;
    private EditText edtAwayScore;
    private EditText edtMatchTime;
    private EditText edtStatus;
    private Button btnSave;
    private Button btnCancel;

    private MatchDataSource matchDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_match);

        edtLeague = findViewById(R.id.edtLeague);
        edtHomeTeam = findViewById(R.id.edtHomeTeam);
        edtAwayTeam = findViewById(R.id.edtAwayTeam);
        edtHomeScore = findViewById(R.id.edtHomeScore);
        edtAwayScore = findViewById(R.id.edtAwayScore);
        edtMatchTime = findViewById(R.id.edtMatchTime);
        edtStatus = findViewById(R.id.edtStatus);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        matchDataSource = new MatchDataSource(this);

        btnSave.setOnClickListener(v -> saveMatch());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveMatch() {
        String league = edtLeague.getText().toString().trim();
        String homeTeam = edtHomeTeam.getText().toString().trim();
        String awayTeam = edtAwayTeam.getText().toString().trim();
        String homeScoreText = edtHomeScore.getText().toString().trim();
        String awayScoreText = edtAwayScore.getText().toString().trim();
        String matchTime = edtMatchTime.getText().toString().trim();
        String status = edtStatus.getText().toString().trim();

        if (TextUtils.isEmpty(league) || TextUtils.isEmpty(homeTeam) || TextUtils.isEmpty(awayTeam)) {
            Toast.makeText(this, "Vui lòng nhập tên giải đấu và hai đội bóng", Toast.LENGTH_SHORT).show();
            return;
        }

        int homeScore = parseScore(homeScoreText);
        int awayScore = parseScore(awayScoreText);

        if (TextUtils.isEmpty(matchTime)) {
            matchTime = "Chưa cập nhật";
        }

        if (TextUtils.isEmpty(status)) {
            status = "Sắp diễn ra";
        }

        Match match = new Match(league, homeTeam, awayTeam, homeScore, awayScore, matchTime, status);
        long result = matchDataSource.addMatch(match);

        if (result > 0) {
            Toast.makeText(this, "Đã thêm trận đấu", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Thêm trận đấu thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private int parseScore(String scoreText) {
        if (TextUtils.isEmpty(scoreText)) {
            return 0;
        }

        try {
            return Integer.parseInt(scoreText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

package com.example.airhockey.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.airhockey.R;

public class EndGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);
        TextView scorePlayerView = findViewById(R.id.player_score);
        TextView scoreOpponentView = findViewById(R.id.opponent_score);
        Intent intent = getIntent();
        int playerScore = intent.getIntExtra("player_score", 0);
        int opponentScore = intent.getIntExtra("opponent_score", 0);
        boolean drop = intent.getBooleanExtra("drop", false);
        if (drop) {
            Toast.makeText(getApplicationContext(), "opponent quits the game", Toast.LENGTH_LONG).show();
        }
        scoreOpponentView.setText("" + opponentScore);
        scorePlayerView.setText("" + playerScore);
    }
}
package com.example.cps_ca3;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.cps_ca3.Board.GameView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import LocationCalculator.LocationCalculator;
import LocationCalculator.GravityLocationCalculator;
import LocationCalculator.GyroscopeLocationCalculator;
import Model.Pair;
import Model.State;
import loop.GameLoop;

public class GameActivity extends AppCompatActivity {

    FloatingActionButton randomSpeed_btn;
    GameView gameView;
    GameLoop gameLoop;
    String sensorType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        randomSpeed_btn = (FloatingActionButton) findViewById(R.id.gyroscope_random_speed_btn);
        Intent intent = getIntent();
        sensorType = intent.getStringExtra("sensor_type");
        randomSpeed_btn.setOnClickListener(v -> {
            gameLoop.pushBall();
        });
        gameView = (GameView) findViewById(R.id.game_view);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Pair<Integer,Integer> screen = new Pair<>(displayMetrics.widthPixels, displayMetrics.heightPixels);
        gameLoop = new GameLoop(gameView,sensorManager,sensorType, 16,screen);
        gameLoop.start();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gameLoop.endLoop();
        while (gameLoop.isAlive());
    }
}
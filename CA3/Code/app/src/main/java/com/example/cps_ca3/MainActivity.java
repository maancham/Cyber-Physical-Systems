package com.example.cps_ca3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button gravity_btn;
    Button gyroscope_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gravity_btn = (Button) findViewById(R.id.btn_gravity);
        gyroscope_btn = (Button) findViewById(R.id.btn_gyroscope);

        gravity_btn.setOnClickListener(v -> {
            Intent newIntent = new Intent(this, GameActivity.class);
            newIntent.putExtra("sensor_type","gravity");
            startActivity(newIntent);
        });

        gyroscope_btn.setOnClickListener(v -> {
            Intent newIntent = new Intent(this, GameActivity.class);
            newIntent.putExtra("sensor_type","gyroscope");
            startActivity(newIntent);
        });
    }



}
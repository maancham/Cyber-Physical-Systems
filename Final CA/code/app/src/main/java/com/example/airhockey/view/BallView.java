package com.example.airhockey.view;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.airhockey.R;
import com.example.airhockey.models.Pair;

public class BallView extends androidx.appcompat.widget.AppCompatImageView {

    private float radiusFactor = 0.06f;
    private int radius;
    private int width;
    private int height;
    private float posX,posY;

    public BallView(@NonNull Context context, int width, int height) {
        super(context);
        this.setImageResource(R.drawable.ball);
        this.width = width;
        this.height = height;
        radius = (int) (radiusFactor * width);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(2*radius,2*radius);
        this.setLayoutParams(params);
    }

    private float calculatePosX(float x){
        if (x < 0){
            return 0;
        }
        if (x + 2 * radius > width){
            return width - 2 * radius;
        }
        return x;
    }

    private float calculatePosY(float y){
        if (y < 0){
            return 0;
        }
        if (y + 2 * radius > height){
            return height - 2 * radius;
        }
        return y;
    }

    public void setPosition(float x, float y){
        x = x - 1 * radius;
        y = y - 1 * radius;
        this.animate()
                .x(calculatePosX(x))
                .y(calculatePosY(y))
                .setDuration(0)
                .start();
        posX = x;
        posY = y;
    }

    public Pair<Integer, Integer> getPosition() {
        return new Pair<>((int) (posX + radius), (int) (posY + radius));
    }

    public int getRadius() {
        return radius;
    }
}

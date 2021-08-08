package com.example.airhockey.view;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.airhockey.R;
import com.example.airhockey.models.Pair;
import com.example.airhockey.utils.PhysicalEventCalculator;

public class StrikerView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    private float dX = 0,dY = 0;
    private int width,height;
    private float radiusFactor = 0.1f;
    private int radius;
    private float posX,posY;
    private boolean player;
    private boolean isPositionChanged = false;
    private PhysicalEventCalculator calculator;

    public StrikerView(@NonNull Context context, int width, int height, boolean player) {
        super(context);

        if (player){
            this.setImageResource(R.drawable.img_player);
        }
        else {
            this.setImageResource(R.drawable.img_com);
        }
        this.width = width;
        this.height = height;
        radius = (int) (radiusFactor * width);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(2*radius,2*radius);
        this.setLayoutParams(params);
        this.player = player;
        Log.i("sizeX", ""+width);
        Log.i("sizeY", ""+height);

    }

    public void setCalculator(PhysicalEventCalculator calculator) {
        this.calculator = calculator;
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
        if (y < height/2f && player){
            return height/2f;
        }
        if (y + 2 * radius > height/2f && !player){
            return height/2f - 2 * radius;
        }
        return y;
    }

    public void setPosition(float x, float y){
        if (Float.isNaN(x) || Float.isNaN(y)){
            return;
        }
        x = x - 1 * radius;
        y = y - 1 * radius;
        this.animate()
                .x(calculatePosX(x))
                .y(calculatePosY(y))
                .setDuration(0)
                .start();
//        if (player) {
//            calculator.updateByHittingToStriker();
//        }
        posX = x;
        posY = y;
    }

    public boolean isPositionChanged() {
        boolean temp = isPositionChanged;
        isPositionChanged = false;
        return temp;
    }

    public Pair<Integer, Integer> getPosition() {
        return new Pair<>((int) (posX + radius), (int) (posY + radius));
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!player){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();

            } break;
                case MotionEvent.ACTION_MOVE:
            {
                float y = event.getRawY();
                float x = event.getRawX();
                if (!Float.isNaN(x+dX) && !Float.isNaN(y+dY)){
                    calculator.setPlayerStrikerPosition(new Pair<Double, Double>((double)calculatePosX(x + dX )+radius,(double)calculatePosY(y + dY)+radius));
                }
                else {
                    this.setOnTouchListener(null);
                    this.setOnTouchListener(this::onTouch);
                }
                isPositionChanged = true;
            } break;
            default:
                return false;
        }
        return true;
    }

    public Pair<Double, Double> getPositionStart() {
        return new Pair<>((double) (this.getX() + radius), (double) (this.getY() + radius));
    }

    public int getRadius() {
        return radius;
    }
}

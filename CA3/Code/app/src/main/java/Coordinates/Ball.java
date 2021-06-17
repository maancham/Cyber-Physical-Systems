package Coordinates;

import android.util.TypedValue;

import Model.Pair;

public class Ball {
    // up right is (0,0)?

    private int x;
    private int y;

    public int getRadius() {
        return radius;
    }

    private final int radius = 50;


    public Ball(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Pair<Integer,Integer> getPosition(){
        return new Pair<Integer, Integer>(x, y);
    }

    public void updateLocation(Pair<Double, Double> location) {
        x = (int) Math.round(location.getFirst());
        y = (int) Math.round(location.getSecond());
    }

}

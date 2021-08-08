package com.example.airhockey.utils;


import com.example.airhockey.models.Pair;

public class LocationConverter {
    private int height;
    private int width;
//  (Width, Height) -> (1, height / width)
    public LocationConverter(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public Pair<Integer, Integer> convertToRealPoint(Pair<Double, Double> fractionalPoint) {
        return new Pair<>((int) (fractionalPoint.first * width), (int) (fractionalPoint.second * height));
    }

    public Pair<Double, Double> convertToFractionalPoint(Pair<Integer, Integer> realPoint) {
        return new Pair<>(realPoint.first.doubleValue() / width, realPoint.second.doubleValue() / height);
    }

    public Pair<Double, Double> normalize(Pair<Double, Double> realPoint) {
        return new Pair<>(realPoint.first.doubleValue() / width, realPoint.second.doubleValue() / height);
    }

    public Pair<Integer, Integer> reflectPosition(Pair<Integer, Integer> inputPoint) {
        return new Pair<>(width - inputPoint.first, height - inputPoint.second);
    }

    public Pair<Double, Double> reflectPositionBall(Pair<Integer, Integer> inputPoint) {
        Pair<Integer, Integer> pos = reflectPosition(inputPoint);
        return new Pair<>((double) pos.first, (double) pos.second);
    }


    public Pair<Double, Double> reflectSpeed(Pair<Integer, Integer> inputPoint) {
        return new Pair<>((double)-inputPoint.first,(double) -inputPoint.second);
    }

}

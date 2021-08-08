package com.example.airhockey.models;

public class Vector {
    public Pair<Double, Double> data;

    public Vector(Pair<Double, Double> data) {
        this.data = new Pair<>(data.first, data.second);
    }

    public Vector(Double first, Double second) {
        this.data = new Pair<>(first, second);
    }

    public Vector(Vector vec) {
        this.data = vec.data;
    }

    public Double value() {
        return Math.sqrt(Math.pow(data.first, 2) + Math.pow(data.second, 2));
    }

    public void add(Vector vec) {
        data.first += vec.data.first;
        data.second += vec.data.second;
    }

    public Vector getAdd(Vector vec) {
        return new Vector(data.first + vec.data.first, data.second + vec.data.second);
    }

    public void subtract(Vector vec) {
        data.first -= vec.data.first;
        data.second -= vec.data.second;
    }

    public void scalarMultiply(Double value) {
        data.first *= value;
        data.second *= value;
    }

    public Vector getScalarMultiply(Double value) {
        return new Vector(data.first * value, data.second *= value);
    }

    public Vector getUnit() {
        scalarMultiply( 1 / value());
        return new Vector(data);
    }

    public void setUnit() {
        scalarMultiply(1 / value());
    }

    public Double dotProduct(Vector vec) {
        return data.first * vec.data.first + data.second * vec.data.second;
    }

    public Vector getNormal() {
        return new Vector(data.second, -data.first);
    }
}

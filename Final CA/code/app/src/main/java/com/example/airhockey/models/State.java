package com.example.airhockey.models;



public class State {
    private Pair<Double, Double> velocity;
    private Pair<Double, Double> position;

    public State(Pair<Double, Double> velocity, Pair<Double, Double> position) {
        this.velocity = velocity;
        this.position = position;
    }

    public Pair<Double, Double> getVelocity() {
        return velocity;
    }

    public void setVelocity(Pair<Double, Double> velocity) {
        this.velocity = velocity;
    }

    public Pair<Double, Double> getPosition() {
        return position;
    }

    public void setPosition(Pair<Double, Double> position) {
        this.position = position;
    }
}
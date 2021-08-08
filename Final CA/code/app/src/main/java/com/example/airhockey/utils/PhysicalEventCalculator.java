package com.example.airhockey.utils;

import android.util.Log;

import com.example.airhockey.models.Pair;
import com.example.airhockey.models.State;
import com.example.airhockey.models.Vector;


public class PhysicalEventCalculator {
    private final int AXIS_X = 0;
    private final int AXIS_Y = 1;
    private final int CORNER = 2;
    private final double GOAL_LENGTH_FACTOR = 0.5;
    private final double GOAL_WITH_FACTOR = 0.04;
    private final int xLength;
    private final int yLength;
    private final double remainedForce = 0.8;
    private int axis;
    private int ballRadius;
    private int strikerRadius;
    private State prevBallState;
    private State currentBallState;
    private State prevPlayerStrikerState;
    private State currentPlayerStrikerState;
    private State initStateBall;
    private State initStateStriker;
    private final double dt;
    private boolean touched = false;
    private boolean collision = false;

    private State cloneState(State state) {
        return new State(new Pair<>(state.getVelocity().first, state.getVelocity().second), new Pair<>(state.getPosition().first, state.getPosition().second));
    }

    public PhysicalEventCalculator(int xLength, int yLength, State initBall, State initStriker, double dt) {
        this.dt = dt;
        this.xLength = xLength;
        this.yLength = yLength;
        initStateBall = initBall;
        initStateStriker = initStriker;
        goToInitState();
        Log.e("loc", initBall.getPosition().toString());
    }

    public void goToInitState(){
        this.prevBallState = cloneState(initStateBall);
        this.currentBallState = cloneState(initStateBall);
        this.prevPlayerStrikerState = cloneState(initStateStriker);
        this.currentPlayerStrikerState = cloneState(initStateStriker);
    }

    public void setRadius(int ballRadius, int strikerRadius) {
        this.ballRadius = ballRadius;
        this.strikerRadius = strikerRadius;
    }

    public State reflectHittingToSurface() {
        switch (axis) {
            case AXIS_X:
                return new State(new Pair<>(-currentBallState.getVelocity().first * remainedForce, currentBallState.getVelocity().second)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
            case AXIS_Y:
                return new State(new Pair<>(currentBallState.getVelocity().first, -currentBallState.getVelocity().second * remainedForce)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
            case CORNER:
                return new State(new Pair<>(-currentBallState.getVelocity().first * remainedForce, -currentBallState.getVelocity().second * remainedForce)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
        }
        return null;
    }

    private Pair<Double, Double> fixLocation(Pair<Double, Double> pos) {
        if (pos.first <= ballRadius)
            pos.first = (double) ballRadius;
        if (pos.first >= (xLength - ballRadius))
            pos.first = (double) (xLength - ballRadius);
        if (pos.second <= ballRadius)
            pos.second = (double) ballRadius;
        if (pos.second >= (yLength - ballRadius))
            pos.second = (double) (yLength - ballRadius);
        return pos;
    }

    private Pair<Double, Double> moveWithSteadyVelocity(double dt, Pair<Double, Double> velocity, State state) {
        Pair<Double, Double> curBallPos = state.getPosition();
        return fixLocation(new Pair<>(curBallPos.first + velocity.first * dt, curBallPos.second + velocity.second * dt));
    }

    public void setBallNewState(Pair<Double,Double> position, Pair<Double,Double> velocity){
        currentBallState = new State(velocity, position);
    }

    public void setPlayerStrikerPosition(Pair<Double,Double> position){
        if (Double.isNaN(position.first) && Double.isNaN(position.second)){
            return;
        }
        double prevX = prevPlayerStrikerState.getPosition().first;
        double prevY = prevPlayerStrikerState.getPosition().second;
        prevPlayerStrikerState = currentPlayerStrikerState;
        Pair<Double,Double> newPos = position;
        if (isHitToStriker(position, currentBallState.getPosition())){
            newPos = getTangentPosition(position, currentBallState.getPosition(), strikerRadius, ballRadius);
        }
        currentPlayerStrikerState = new State(new Pair<>((position.first - prevX) / dt, (position.second - prevY) / dt), newPos);
    }

    private Double findDistance(Pair<Double, Double> a, Pair<Double, Double> b) {
        return (Math.sqrt(Math.pow((a.first - b.first), 2) + Math.pow((a.second - b.second), 2)));
    }

    private Pair<Double,Double> getTangentPosition(Pair<Double,Double> circle1, Pair<Double,Double> circle2, int radius1, int radius2){
        double distanceFactor = (radius1 + radius2) / findDistance(circle1, circle2);
        Pair<Double,Double> newPos = new Pair<>((1 - distanceFactor) * circle2.first + distanceFactor * circle1.first
                , (1 - distanceFactor) * circle2.second + distanceFactor * circle1.second);
        return fixLocation(newPos);
    }

    public void move() {
        collision = false;
        Pair<Double, Double> curStrikerPos = currentPlayerStrikerState.getPosition();
        setPlayerStrikerPosition(curStrikerPos);
        Pair<Double, Double> curBallPos = currentBallState.getPosition();
        State newState;
        if (isHitToStriker(curStrikerPos, curBallPos)) {
            newState = checkBallCollision();
        } else if (checkHittingToWalls()) {
            this.touched = false;
            newState = reflectHittingToSurface();
        } else {
            this.touched = false;
            Pair<Double, Double> velocity = currentBallState.getVelocity();
            newState = new State(velocity, moveWithSteadyVelocity(dt, velocity, currentBallState));
        }
        prevBallState = currentBallState;
        currentBallState = newState;
        if (isHitToStriker(curStrikerPos, curBallPos)) {
            newState = checkBallCollision();
            prevBallState = currentBallState;
            currentBallState = newState;
            collision = true;
        }
    }

    public boolean collisionOccur(){
        return collision;
    }

    public void updateByHittingToStriker() {
        State newState;
        Pair<Double, Double> curBallPos = currentBallState.getPosition();
        Pair<Double, Double> curStrikerPos = currentPlayerStrikerState.getPosition();
        if (isHitToStriker(curStrikerPos, curBallPos)) {
            prevBallState = currentBallState;
            currentBallState = checkBallCollision();
        }
    }

    public boolean isHitToStriker(Pair<Double, Double> strikerPos, Pair<Double, Double> ballPos) {
        return (strikerRadius + ballRadius) >= findDistance(strikerPos, ballPos);
    }
    
    public State checkBallCollision() {
        State newState;
        Vector distanceVector = new Vector(currentBallState.getPosition());
        distanceVector.subtract(new Vector(currentPlayerStrikerState.getPosition()));
        Double distance = distanceVector.value() - ((ballRadius + strikerRadius));
        if (this.touched) {
            Pair<Double, Double> velocity = distanceVector.getUnit().getScalarMultiply(distance).data;
            setPlayerStrikerPosition(moveWithSteadyVelocity(dt, velocity, currentPlayerStrikerState));
            distanceVector.scalarMultiply((double)2.0);
            velocity = distanceVector.getUnit().data;
            newState = new State(distanceVector.getUnit().getScalarMultiply((-distance) * 0.5f).getAdd(new Vector(currentBallState.getVelocity())).data, moveWithSteadyVelocity(dt, velocity, currentBallState));
        } else {
            this.touched = true;
            Vector vn1 = new Vector(distanceVector);
            vn1.setUnit();
            Vector vt1 = vn1.getNormal();
            Vector strikerVelocity = new Vector(currentPlayerStrikerState.getVelocity());
            Vector ballVelocity = new Vector(currentBallState.getVelocity());
            Double v1n = strikerVelocity.dotProduct(vn1);
            Double v1t = strikerVelocity.dotProduct(vn1);
            Double v2n = ballVelocity.dotProduct(vn1);
            Double v2t = ballVelocity.dotProduct(vt1);
            Double v1n_a = (((strikerRadius - ballRadius) * v1n) + (ballRadius) * v2n) / (strikerRadius + ballRadius);
            Vector vn2 = vn1.getScalarMultiply((((ballRadius - strikerRadius) * v2n) + (strikerRadius) * v1n) / (strikerRadius + ballRadius));
            Vector vt2 = vt1.getScalarMultiply(v2t);
            vn1.scalarMultiply(v1n_a);
            vt1.scalarMultiply(v1t);
            Vector ballNewVelocity = vn2.getAdd(vt2).getScalarMultiply(1.2d);
            Pair<Double,Double> curBallPos = currentBallState.getPosition();
            Pair<Double,Double> curStrikerPos = currentPlayerStrikerState.getPosition();
            Pair<Double,Double> ballNewPos = getTangentPosition(curBallPos, curStrikerPos, ballRadius, strikerRadius);
            newState = new State(ballNewVelocity.data, ballNewPos);
        }
        return newState;
    }

    public Pair<Double, Double> calculateVelocityAfterHit() {
        Pair<Double, Double> vb = currentBallState.getVelocity();
        Pair<Double, Double> vs = currentPlayerStrikerState.getVelocity();
        return new Pair<>(2 * vs.first - vb.first, 2 * vs.second - vb.second);
    }

    public boolean checkHittingToWalls() {
        Pair<Double, Double> currentPosition = currentBallState.getPosition();
        Pair<Double, Double> previousPosition = prevBallState.getPosition();
        boolean isHit = false;
        if ((currentPosition.first <= ballRadius && previousPosition.first > ballRadius) || (currentPosition.first >= (xLength - ballRadius) && previousPosition.first < (xLength - ballRadius))) {
            axis = AXIS_X;
            isHit = true;
        }
        if ((currentPosition.second <= ballRadius && previousPosition.second > ballRadius) || (currentPosition.second >= (yLength - ballRadius) && previousPosition.second < (yLength - ballRadius))) {
            axis = isHit ? CORNER : AXIS_Y;
            isHit = true;
        }
        return isHit;
    }

    public Pair<Double,Double> getSpeedOfBallAfterCollision() {
        return calculateVelocityAfterHit();
    }

    public boolean isGoalScored() {
        double x = currentBallState.getPosition().first;
        double y = currentBallState.getPosition().second;
        if (x > xLength * (1 - GOAL_LENGTH_FACTOR)/2 && x < xLength * ((1 - GOAL_LENGTH_FACTOR)/2 + GOAL_LENGTH_FACTOR)){
            return y > (1 - GOAL_WITH_FACTOR) * yLength;
        }
        return false;
    }

    public Pair<Double,Double> getPlayerStrikerPosition() {
        return currentPlayerStrikerState.getPosition();
    }

    public State getBallState() {
        return currentBallState;
    }
}
package LocationCalculator;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.Random;

import Model.Pair;
import Model.State;

public abstract class LocationCalculator {

    protected SensorManager sensorManager;
    protected Sensor sensor;
    protected State previous;
    protected State current;
    protected MovementRecognizer recognizer;
    protected double deltaT;
    private final double m = 0.01;

    public LocationCalculator(SensorManager sensorManager, Sensor sensor, State start, MovementRecognizer recognizer) {
        this.sensorManager = sensorManager;
        this.sensor = sensor;
        this.previous = start;
        this.current = start;
        this.recognizer = recognizer;
    }

    public void setVelocity() {
        Pair<Double, Double> a = getAccelerations();
        double ax = a.getFirst();
        double ay = a.getSecond();
        Random random = new Random();
        double aTotal = Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2));
        double speedLimit = 2, speed = (random.nextDouble() * speedLimit) + speedLimit;
        double speedX, speedY;
        speedX = speed * (-ax / aTotal);
        speedY = speed * (-ay / aTotal);
        previous.setV(new Pair<>(speedX, speedY));
        current.setV(new Pair<>(speedX, speedY));
    }

    public abstract State getNextStateInConstantAcceleration(double ax, double ay);

    public abstract Pair<Double, Double> getAccelerations();

    public Pair<Double, Double> nextCoordinate(int deltaT) throws Exception {
        this.deltaT = (float) (deltaT) / 1000;
        State next;
        if (recognizer.isFreeFall(current.getCoordinate()))
            next = nextStateFreeFall();
        else if (recognizer.isHit(current.getCoordinate(), previous.getCoordinate()))
            next = nextStateHit(recognizer.getAxis());
        else if (recognizer.isRolling(current.getCoordinate(), previous.getCoordinate()))
            next = nextStateRolling(recognizer.getAxis());
        else throw new Exception();
        recognizer.fixCoordination(next);
        previous = current;
        current = next;
        return next.getCoordinate();
    }

    private State nextStateFreeFall() {
        Pair<Double, Double> a = getAccelerations();
        return getNextStateInConstantAcceleration(a.getFirst(), a.getSecond());
    }

    private State nextStateRolling(int axis) {
        Pair<Double, Double> a = getAccelerations();
        double wx = m * a.getFirst();
        double wy = m * a.getSecond();
        double ax, ay;
        if (recognizer.isFallingWhileTouching(current.getCoordinate(), a.getFirst(), a.getSecond()))
            return nextStateFreeFall();
        if (axis == 0) { //on y sides (parallel with y axis)
            if (current.getCoordinate().equals(previous.getCoordinate()) && Math.abs(wy) < 0.15 * Math.abs(wx)) return this.current;
            ay = (wy - 0.07 * wx) / m;
            ax = 0;
        } else { //on x sides (parallel with x axis)
            if (current.getCoordinate().equals(previous.getCoordinate()) && Math.abs(wx) < 0.15 * Math.abs(wy)) return this.current;
            ax = (wx - 0.07 * wy) / m;
            ay = 0;
        }
        return getNextStateInConstantAcceleration(ax, ay);
    }

    private State nextStateHit(int axis) {
        Pair<Double, Double> a = getAccelerations();
        double vx = current.getVx(), vy = current.getVy();
        double newV = 3 * Math.sqrt((Math.pow(vx, 2) + Math.pow(vy, 2)) / 10);
        if (axis == 0) { //on x axis
            double theta = Math.atan(Math.abs(vx) / Math.abs(vy));
            current.setV(new Pair<>(-Math.signum(vx) * newV * Math.sin(theta), Math.signum(vy) * newV * Math.cos(theta)));
        } else { //on y axis if axis = 1 otherwise on corner
            double theta = Math.atan(Math.abs(vy) / Math.abs(vx));
            current.setV(new Pair<>( ((axis == 2) ? -1 : 1) * Math.signum(vx) * newV * Math.cos(theta), newV * Math.sin(theta) * -Math.signum(vy)));
        }
        return getNextStateInConstantAcceleration(a.getFirst(), a.getSecond());
    }
}

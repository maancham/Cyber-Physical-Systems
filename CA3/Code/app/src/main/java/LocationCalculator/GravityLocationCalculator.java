package LocationCalculator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import Model.Pair;
import Model.State;

public class GravityLocationCalculator extends LocationCalculator {

    private float[] forces;

    public GravityLocationCalculator(SensorManager sensorManager, Sensor sensor, State start, MovementRecognizer recognizer) {
        super(sensorManager, sensor, start, recognizer);
        this.sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                forces = event.values;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        }, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public Pair<Double, Double> getAccelerations() {
        double ax = -forces[0];
        double ay = -forces[1];
        return new Pair<>(ax, ay);
    }

    @Override
    public State getNextStateInConstantAcceleration(double ax, double ay) {
        double nextVx = ax * deltaT + this.current.getVx();
        double nextVy = ay * deltaT + this.current.getVy();
        double nextX = (this.current.getCoordinate().getFirst() + 1000 * (0.5 * ax * deltaT * deltaT + nextVx * deltaT));
        double deltaY = - (0.5 * ay * deltaT * deltaT + nextVy * deltaT) * 1000;
        double nextY = (this.current.getCoordinate().getSecond() + deltaY);
        return new State(new Pair<>(nextX, nextY), new Pair<>(nextVx, nextVy), new Pair<>(ax, ay));
    }
}

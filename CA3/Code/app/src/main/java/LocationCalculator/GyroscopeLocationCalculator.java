package LocationCalculator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Arrays;

import Model.Pair;
import Model.State;

public class GyroscopeLocationCalculator extends LocationCalculator {

    private final double g = 9.8;
    private float[] deltaAngles = {0,0,0};

    public GyroscopeLocationCalculator(SensorManager sensorManager, Sensor sensor, State start, MovementRecognizer recognizer) {
        super(sensorManager, sensor, start, recognizer);
        this.sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                synchronized (deltaAngles){
                    deltaAngles = event.values;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        }, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public Pair<Double, Double> getAccelerations() {
        double ax = - g * Math.sin(current.getAngles().getFirst());
        double ay = - g * Math.sin(current.getAngles().getSecond());
        return new Pair<>(ax, ay);
    }

    @Override
    public State getNextStateInConstantAcceleration(double ax, double ay) {
        double angleX = current.getAngles().getFirst();
        double angleY = current.getAngles().getSecond();
        double nextVx = ax * deltaT + this.current.getVx();
        double nextVy = ay * deltaT + this.current.getVy();
        double nextX = (this.current.getCoordinate().getFirst() + 1000 * (0.5 * ax * deltaT * deltaT + nextVx * deltaT));
        double nextY = (this.current.getCoordinate().getSecond() - 1000 * (0.5 * ay * deltaT * deltaT + nextVy * deltaT));
        double newAngleX = angleX + deltaT * (-deltaAngles[1]);
        double newAngleY = angleY + deltaT * deltaAngles[0];
        return new State(new Pair<>(nextX, nextY), new Pair<>(nextVx, nextVy), new Pair<>(ax, ay),new Pair<>(newAngleX,newAngleY));
    }
}

package loop;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.cps_ca3.Board.GameView;


import java.util.Random;

import Coordinates.Ball;
import LocationCalculator.LocationCalculator;
import LocationCalculator.GyroscopeLocationCalculator;
import LocationCalculator.GravityLocationCalculator;
import LocationCalculator.MovementRecognizer;

import Model.Pair;
import Model.State;

public class GameLoop extends Thread{

    private boolean running;
    private GameView view;
    private Ball ball;
    private LocationCalculator calculator;
    private int deltaT;

    public GameLoop(GameView view, SensorManager sensorManager, String sensorType, int dt,Pair<Integer,Integer> screen){
        this.view = view;
        this.running = true;
        this.deltaT = dt;
        Sensor sensor;
        Model.State state = randomInitialState(screen);
        ball = new Ball((int) Math.round(state.getCoordinate().getFirst()), (int) Math.round(state.getCoordinate().getSecond()));
        MovementRecognizer movementRecognizer = new MovementRecognizer(screen.getFirst(),screen.getSecond(),ball.getRadius());
        if (sensorType.equals("gravity")){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            calculator = new GravityLocationCalculator(sensorManager,sensor,state,movementRecognizer);
        }
        else {
            state.setAngles(new Pair<>(0.0, 0.0));
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            calculator = new GyroscopeLocationCalculator(sensorManager,sensor,state,movementRecognizer);
        }
    }

    private Model.State randomInitialState(Pair<Integer,Integer> screen){
        int x,y;
        Random random = new Random();
        x = ((random.nextInt() % screen.getFirst()) + screen.getFirst()) % screen.getFirst();
        y = ((random.nextInt() % screen.getSecond()) + screen.getSecond()) % screen.getSecond();
        Pair<Double, Double> startLocation = new Pair<>((double) x, (double) y);
        double speedX,speedY;
        speedX = random.nextDouble() % 0.002;
        speedY = random.nextDouble() % 0.002;
        Pair<Double,Double> startSpeed = new Pair<>(speedX, speedY);
        return new Model.State(startLocation,startSpeed, new Pair<>(0.0, 0.0));
    }

    @Override
    public void run() {
        super.run();
        while (running) {
            try {
                Pair<Integer,Integer> ballPos = ball.getPosition();
                view.updateBallPosition(ballPos.getFirst(),ballPos.getSecond());
                Pair<Double, Double> pos = calculator.nextCoordinate(deltaT);
                ball.updateLocation(pos);
                Thread.sleep(deltaT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pushBall(){
        calculator.setVelocity();
    }

    public void endLoop(){
        running = false;
    }
}

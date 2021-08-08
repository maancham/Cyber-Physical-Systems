package com.example.airhockey.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.airhockey.R;
import com.example.airhockey.models.MessageConstants;
import com.example.airhockey.models.State;
import com.example.airhockey.services.BluetoothService;
import com.example.airhockey.utils.LocationConverter;
import com.example.airhockey.utils.Logger;
import com.example.airhockey.utils.PhysicalEventCalculator;
import com.example.airhockey.utils.ProtocolUtils;
import com.example.airhockey.view.BallView;
import com.example.airhockey.models.Pair;
import com.example.airhockey.view.StrikerView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class GameActivity extends AppCompatActivity {


    StrikerView playerStrikerView;
    StrikerView opponentStrikerView;
    BallView ballView;
    ConstraintLayout gameLayout;
    PhysicalEventCalculator physicalEventCalculator;
    TextView scorePlayerTextView;
    TextView scoreOpponentTextView;
    Logger logger;
    int frameCount;
    private BluetoothService bluetoothService = BluetoothService.getInstance();
    private boolean isPositionChanged = false;
    private LocationConverter converter;

    private Timer goalAckTimer;
    private Timer collisionTimer;
    private long TIMEOUT = 100;
    Thread gameThread;
    boolean resume = true;

    private AtomicBoolean waitForSync;

    private int scorePlayer = 0;
    private int scoreOpponent = 0;
    private final int MAX_SCORE_TO_WIN = 7;
    int width;
    int height;

    private class BluetoothConnectionDropHandler extends Handler {
        public BluetoothConnectionDropHandler() {
            super();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0){
                if (isGameEnded()){
                    return;
                }
                scorePlayer = MAX_SCORE_TO_WIN;
                goToEndGame(true);
            }
        }
    }

    private class BluetoothHandler extends Handler {
        public BluetoothHandler() {
            super();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] msgBytes = (byte[]) msg.obj;
                    InputStream inputStream = new ByteArrayInputStream(msgBytes);
                    ProtocolUtils.MessageTypes type = ProtocolUtils.getTypeOfMessage(inputStream);
                    if (type == ProtocolUtils.MessageTypes.POSITION_REPORT){
                        Pair<Double,Double> rPosition = null;
                        try {
                            rPosition = ProtocolUtils.receivePositionMessage(inputStream);
                            Pair<Integer, Integer> position = converter.reflectPosition(converter.convertToRealPoint(rPosition));
                            opponentStrikerView.setPosition(position.first.floatValue(), position.second.floatValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type == ProtocolUtils.MessageTypes.BALL_COLLISION_REPORT){
                        Pair<Pair<Double,Double>,Pair<Double,Double>> ballInfo = null;
                        Pair<Double,Double> collisionPosition = null;
                        Pair<Double,Double> collisionSpeed = null;
                        try {
                            ballInfo = ProtocolUtils.receiveBallCollisionMessage(inputStream);
                            collisionPosition = ballInfo.first;
                            collisionSpeed = ballInfo.second;
                            Pair<Double, Double> position = converter.reflectPositionBall(converter.convertToRealPoint(collisionPosition));
                            Pair<Double, Double> speed = converter.reflectSpeed(converter.convertToRealPoint(collisionSpeed));
                            physicalEventCalculator.setBallNewState(position, speed);
                            bluetoothService.write(ProtocolUtils.sendBallCollisionAck());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type == ProtocolUtils.MessageTypes.BALL_COLLISION_ACK){
                        try {
                            ProtocolUtils.receiveCollisionAckMessage(inputStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        stopCollisionTimer();
                    }
                    if (type == ProtocolUtils.MessageTypes.GOAL_SCORED_REPORT){
                        try {
                            scorePlayer = ProtocolUtils.receiveGoalScoredMessage(inputStream);
                            Log.e("goal", ""+scorePlayer);
                            Toast.makeText(getApplicationContext(), "player " + scorePlayer + " - " + scoreOpponent + " opponent",Toast.LENGTH_LONG).show();
                            setNewPositionForPlayerStriker(width, height);
                            setNewPositionForOpponentStriker(width, height);
                            bluetoothService.write(ProtocolUtils.sendGoalScoredAck(scorePlayer));
                            if (isGameEnded()){
                                bluetoothService.stopConnection();
                                goToEndGame(false);
                            }
                            else {
                                physicalEventCalculator.goToInitState();
                                setStartPositionForBall();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type == ProtocolUtils.MessageTypes.GOAL_SCORED_ACK){
                        try {
                            Log.e("GOAL", "ack recieved");
                            stopGoalAckTimer();
                            scoreOpponent = ProtocolUtils.receiveGoalScoredAckMessage(inputStream);
                            Toast.makeText(getApplicationContext(), "player " + scorePlayer + " - " + scoreOpponent + " opponent",Toast.LENGTH_LONG).show();
                            setNewPositionForPlayerStriker(width, height);
                            setNewPositionForOpponentStriker(width, height);
                            if (isGameEnded()){
                                bluetoothService.stopConnection();
                                waitForSync.set(false);
                                goToEndGame(false);
                            }
                            else {
                                physicalEventCalculator.goToInitState();
                                setStartPositionForBall();
                                waitForSync.set(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    void goToEndGame(boolean drop) {
        Intent intent = new Intent(getApplicationContext(), EndGameActivity.class);
        intent.putExtra("player_score", scorePlayer);
        intent.putExtra("opponent_score", scoreOpponent);
        intent.putExtra("drop", drop);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private class GoalAckTask extends TimerTask {

        @Override
        public void run() {
            try {
                bluetoothService.write(ProtocolUtils.sendGoalScored(scoreOpponent + 1));
                goalAckTimer.schedule(new GoalAckTask(), TIMEOUT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void startGoalAckTimer(){
        try {
            goalAckTimer.schedule(new GoalAckTask(), TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopGoalAckTimer(){
        try {
            goalAckTimer.cancel();
            goalAckTimer = new Timer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CollisionAckTask extends TimerTask {

        @Override
        public void run() {
            try {
                sendBallCollision();
                collisionTimer.schedule(new CollisionAckTask(), TIMEOUT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void startCollisionTimer(){
        try {
            collisionTimer.schedule(new CollisionAckTask(), TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopCollisionTimer(){
        try {
            collisionTimer.cancel();
            collisionTimer = new Timer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler bluetoothHandler = new BluetoothHandler();
    private final BluetoothConnectionDropHandler dropHandler = new BluetoothConnectionDropHandler();

    void setNewPositionForPlayerStriker(int width, int height) {
        if (playerStrikerView != null) {
            gameLayout.removeView(playerStrikerView);
        }
        playerStrikerView = new StrikerView(this, width, height, true);
        playerStrikerView.setOnTouchListener(playerStrikerView);
        setPositionForStriker(playerStrikerView, width, height, true);
        playerStrikerView.setCalculator(physicalEventCalculator);
    }

    void setNewPositionForOpponentStriker(int width, int height) {
        if (opponentStrikerView != null) {
            gameLayout.removeView(opponentStrikerView);
        }
        opponentStrikerView = new StrikerView(this, width, height, false);
        opponentStrikerView.setOnTouchListener(opponentStrikerView);
        setPositionForStriker(opponentStrikerView, width, height, false);
    }

    void setPositionForStriker(StrikerView view, int width, int height, boolean player) {
        float startLocationFactor = 0.8f;
        ConstraintSet set = new ConstraintSet();
        view.setId(View.generateViewId());
        gameLayout.addView(view, -1);
        set.clone(gameLayout);
        if (player) {
            set.connect(view.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP, (int) (startLocationFactor * height));
            set.connect(view.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.BOTTOM);
        } else {
            set.connect(view.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP);
            set.connect(view.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.BOTTOM, (int) (startLocationFactor * height));

        }
        set.connect(view.getId(), ConstraintSet.LEFT, gameLayout.getId(), ConstraintSet.LEFT);
        set.connect(view.getId(), ConstraintSet.RIGHT, gameLayout.getId(), ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }

    void setStartPositionForBall() {
        if (ballView != null){
            gameLayout.removeView(ballView);
        }
        ballView = new BallView(getApplicationContext(), width, height);
        ConstraintSet set = new ConstraintSet();
        ballView.setId(View.generateViewId());
        gameLayout.addView(ballView, -1);
        set.clone(gameLayout);
        set.connect(ballView.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP);
        set.connect(ballView.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.BOTTOM);
        set.connect(ballView.getId(), ConstraintSet.LEFT, gameLayout.getId(), ConstraintSet.LEFT);
        set.connect(ballView.getId(), ConstraintSet.RIGHT, gameLayout.getId(), ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger = Logger.getInstance();
        gameLayout = findViewById(R.id.board_layout);
        scoreOpponentTextView = findViewById(R.id.in_game_opponent_score);
        scorePlayerTextView = findViewById(R.id.in_game_player_score);
        waitForSync = new AtomicBoolean();
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        bluetoothService.setHandler(bluetoothHandler);
        bluetoothService.setConnectionDropNotifier(dropHandler);
        converter = new LocationConverter(height, width);
        setStartPositionForBall();
        setNewPositionForPlayerStriker(width, height);
        setNewPositionForOpponentStriker(width, height);
        State ballState = new State(new Pair<>(0.0, 0.0), new Pair<>((double) (width/2), (double)(height/2)));
        State strikerState = new State(new Pair<>(0.0, 0.0), new Pair<>((double) (width/2),(double)(height - 2 * playerStrikerView.getRadius())));
        physicalEventCalculator = new PhysicalEventCalculator(width, height, ballState, strikerState, 0.017);
        playerStrikerView.setCalculator(physicalEventCalculator);
        opponentStrikerView.setCalculator(physicalEventCalculator);
        collisionTimer = new Timer();
        goalAckTimer = new Timer();
        gameThread = new Thread(() -> {
                gameLoop();
        });
        gameThread.start();
    }

    void sendBallCollision(){
        State ballState = physicalEventCalculator.getBallState();
        Pair<Double,Double> position = converter.convertToFractionalPoint(new Pair<Integer, Integer>(ballState.getPosition().first.intValue(), ballState.getPosition().second.intValue()));
        Pair<Double,Double> velocity = converter.convertToFractionalPoint(new Pair<Integer, Integer>(ballState.getVelocity().first.intValue(), ballState.getVelocity().second.intValue()));;
        bluetoothService.write(ProtocolUtils.sendBallCollision(position,velocity));
    }

    private boolean isGameEnded(){
        return scorePlayer == MAX_SCORE_TO_WIN || scoreOpponent == MAX_SCORE_TO_WIN;
    }

    public void gameLoop() {
        waitForSync.set(false);
        physicalEventCalculator.setRadius(ballView.getRadius(), playerStrikerView.getRadius());
        logger.log("GameStart", "game started");
        frameCount = 0;
        while (resume) {
            frameCount += 1;
            ProtocolUtils.setFrame(frameCount);
            if (!waitForSync.get()){
                if (isGameEnded()){
                    break;
                }
                boolean strikerPositionChanged = playerStrikerView.isPositionChanged();
                if (strikerPositionChanged) {
                    Pair<Double,Double> currentPoint = converter.convertToFractionalPoint(playerStrikerView.getPosition());
                    byte[] array = ProtocolUtils.sendStrikerPosition(currentPoint);
                    bluetoothService.write(array);
                }
                if (physicalEventCalculator.isGoalScored()){
                    Log.e("GOAL", "here");
                    bluetoothService.write(ProtocolUtils.sendGoalScored(scoreOpponent+1));
                    waitForSync.set(true);
                    startGoalAckTimer();
                    continue;
                }
                physicalEventCalculator.move();
                if (physicalEventCalculator.collisionOccur()){
                    sendBallCollision();
                    startCollisionTimer();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scorePlayerTextView.setText(""+scorePlayer);
                        scoreOpponentTextView.setText(""+scoreOpponent);
                        Pair<Double,Double> strikerPosition = physicalEventCalculator.getPlayerStrikerPosition();
                        playerStrikerView.setPosition(strikerPosition.first.floatValue(),strikerPosition.second.floatValue());
                        Pair<Double, Double> ballPos = physicalEventCalculator.getBallState().getPosition();
                        ballView.setPosition(ballPos.first.floatValue(), ballPos.second.floatValue());
                    }
                });
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        resume = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothService.stopConnection();
        resume = false;
    }
}
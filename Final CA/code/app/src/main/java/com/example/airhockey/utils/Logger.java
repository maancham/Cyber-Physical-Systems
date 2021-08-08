package com.example.airhockey.utils;

import android.os.Environment;
import android.util.Log;

import com.example.airhockey.models.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger {

    private static Logger instance;
    private final static String FILE_NAME = "logs.log";
    private final static String ADDRESS = "AirHockey";
    private File logFile;
    private FileWriter writer;
    private boolean log = false;
    private ConcurrentLinkedQueue<String> logs;

    private Logger() {
        logs = new ConcurrentLinkedQueue<>();
    }

    private void setup() throws IOException {
        File root = new File(Environment.getExternalStorageDirectory(), ADDRESS);
        Log.e("logger", Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!root.exists()) {
            Log.e("Logger",""+root.mkdirs());
        }
        logFile = new File(root, FILE_NAME);
        if (!logFile.exists()) {
            Log.e("Logger",""+logFile.createNewFile());
//            logFile.createNewFile();
        }
        else {
            logFile.delete();
            logFile.createNewFile();
        }
        writer = new FileWriter(logFile);
        Thread writeThread = new Thread(() -> {
            while (log){
                if (!logs.isEmpty()){
                    String logString = logs.poll();
                    try {
                        writer.write(logString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        writeThread.start();
    }

    public void startLogging(boolean log) throws IOException{
        if (log && this.log == false){
            setup();
        }
        this.log = log;
    }

    public static Logger getInstance() {
        if (instance == null){
            instance = new Logger();
        }
        return instance;
    }

    public boolean isLoggingEnabled(){
        return log;
    }

    public void log(String type, String message) {
        if (!isLoggingEnabled()){
            return;
        }
        logs.add(type + " : " + message + "\n");
    }

    public void logBallPosition(int frame, Pair<Double,Double> location) throws IOException {
        if (!isLoggingEnabled()){
            return;
        }
        String message = "frame = " + frame + " posX = " + location.first + " posY = " + location.second;
        log("BallPosReport", message);
    }

    public void logMoveStriker(int frame, Pair<Double,Double> location, boolean player) throws IOException{
        if (!isLoggingEnabled()){
            return;
        }
        String message = "frame = " + frame + " player = " + player + " posX = " + location.first + " posY = " + location.second;
        log("StrikerPosition",message);
    }
}

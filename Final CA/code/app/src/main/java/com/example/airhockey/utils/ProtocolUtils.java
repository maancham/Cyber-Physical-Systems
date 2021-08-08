package com.example.airhockey.utils;

import android.util.Log;

import com.example.airhockey.models.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.airhockey.models.ProtocolConstants.COLLISION_ACK;
import static com.example.airhockey.models.ProtocolConstants.COLLISION_MSG;
import static com.example.airhockey.models.ProtocolConstants.DOUBLE_PATTERN;
import static com.example.airhockey.models.ProtocolConstants.END_MSG;
import static com.example.airhockey.models.ProtocolConstants.GOAL_ACK;
import static com.example.airhockey.models.ProtocolConstants.GOAL_SCORED;
import static com.example.airhockey.models.ProtocolConstants.POSITION_MSG;
import static com.example.airhockey.models.ProtocolConstants.SEP;

public class ProtocolUtils {

    public enum MessageTypes {
        POSITION_REPORT,
        BALL_COLLISION_REPORT,
        BALL_COLLISION_ACK,
        GOAL_SCORED_REPORT,
        GOAL_SCORED_ACK,
        UNKNOWN
    }

    private static AtomicLong msgId = new AtomicLong(0);
    private static Logger logger = Logger.getInstance();
    private static long frame;

    public static void setFrame(long iframe){
        frame = iframe;
    }

    public static byte[] sendStrikerPosition(Pair<Double,Double> position){
        msgId.incrementAndGet();
        String msg = String.format(Locale.US, POSITION_MSG + "%f" + SEP + "%f" + SEP + "%f" + END_MSG, position.first, position.second, msgId.floatValue());
        logger.log("sendStrikerPosition",frame + " " +msg);
        return msg.getBytes();
    }

    public static byte[] sendBallCollision(Pair<Double,Double> position, Pair<Double,Double> velocity){
        msgId.incrementAndGet();
        String msg = String.format(Locale.US, COLLISION_MSG + "%f" + SEP + "%f" + SEP + "%f" + SEP + "%f" + SEP + "%f" + END_MSG, position.first, position.second, velocity.first, velocity.second, msgId.floatValue());
        logger.log("sendBallCollision",frame + " " +msg);
        return msg.getBytes();
    }

    public static byte[] sendBallCollisionAck(){
        msgId.incrementAndGet();
        float ack = msgId.floatValue();
        String msg = (COLLISION_ACK + "" + ack + END_MSG);
        logger.log("sendBallCollisionAck",frame + " " +msg);
        return msg.getBytes();
    }

    public static byte[] sendGoalScoredAck(float goal){
        msgId.incrementAndGet();
        float ack = msgId.floatValue();
        String msg = (GOAL_ACK + "" + goal + SEP + ack + END_MSG);
        logger.log("sendGoalScoredAck",frame + " " +msg);
        return msg.getBytes();
    }

    public static byte[] sendGoalScored(float goal){
        msgId.incrementAndGet();
        float ack = msgId.floatValue();
        String msg = (GOAL_SCORED + "" + goal + SEP + ack + END_MSG);
        logger.log("sendGoalScored",frame + " " +msg);
        return msg.getBytes();
    }

    public static MessageTypes getTypeOfMessage(InputStream stream){
        try {
            switch ((char) stream.read()) {
                case POSITION_MSG:
                    return MessageTypes.POSITION_REPORT;
                case COLLISION_MSG:
                    return MessageTypes.BALL_COLLISION_REPORT;
                case COLLISION_ACK:
                    return MessageTypes.BALL_COLLISION_ACK;
                case GOAL_SCORED:
                    return MessageTypes.GOAL_SCORED_REPORT;
                case GOAL_ACK:
                    return MessageTypes.GOAL_SCORED_ACK;
                default:
                    return MessageTypes.UNKNOWN;
            }
        }
        catch (Exception e){
            return MessageTypes.UNKNOWN;
        }
    }

    static private String getString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine()) != null)
            sb.append(str);
        return sb.toString().trim();
    }

    static public Pair<Double,Double> receivePositionMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[3];
        for (int i = 0; i < 3; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        logger.log("receivePositionMessage",frame+" "+inputs[0]+" "+inputs[1]+" " + inputs[2]);
        return new Pair<>(inputs[0], inputs[1]);
    }

    static public Pair<Pair<Double,Double>,Pair<Double,Double>> receiveBallCollisionMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[5];
        for (int i = 0; i < 5; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        logger.log("receiveBallCollisionMessage",frame+" "+inputs[0]+" "+inputs[1]+" " + inputs[2]+" "+inputs[3]+ " "+inputs[4]);
        return new Pair<>(new Pair<>(inputs[0], inputs[1]),new Pair<>(inputs[2], inputs[3]));
    }

    static public int receiveGoalScoredMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[2];
        for (int i = 0; i < 2; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        logger.log("receiveGoalScoredMessage",frame+" "+inputs[0]+" "+inputs[1]);
        return inputs[0].intValue();
    }

    static public int receiveGoalScoredAckMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[2];
        for (int i = 0; i < 2; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        logger.log("receiveGoalScoredAckMessage",frame+" "+inputs[0]+" " + inputs[1]);
        return inputs[0].intValue();
    }

    static public void receiveCollisionAckMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[1];
        for (int i = 0; i < 1; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        logger.log("receiveCollisionAckMessage",frame+" "+inputs[0]);
    }

}

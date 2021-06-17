package LocationCalculator;

import Model.Pair;
import Model.State;

public class MovementRecognizer {

    private int xLength;
    private int yLength;
    private int radius;
    private int axis;

    public MovementRecognizer(int xLength, int yLength, int radius) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.radius = radius;
        this.axis = 0;
    }

    public boolean isFreeFall(Pair<Double, Double> current) {
        return ((current.getFirst() + radius < xLength) && (current.getFirst() > radius)
                && (current.getSecond() + radius < yLength) && (current.getSecond() > radius));
    }

    public boolean isRolling(Pair<Double, Double> current, Pair<Double, Double> prev) {
        if ((current.getFirst() <= radius && prev.getFirst() <= radius) || (current.getFirst() >= (xLength - radius) && prev.getFirst() >= (xLength - radius))) {
            axis = 0;
            return true;
        }
        if ((current.getSecond() <= radius && prev.getSecond() <= radius) || (current.getSecond() >= (yLength - radius) && prev.getSecond() >= (yLength - radius))) {
            axis = 1;
            return true;
        }
        return false;
    }

    public boolean isFallingWhileTouching(Pair<Double, Double> current, double ax, double ay) {
        if (axis == 0) {
            if (current.getFirst() <= radius)
                return ax > 0;
            if (current.getFirst() >= (xLength - radius))
                return ax < 0;
        } else if (axis == 1) {
            if (current.getSecond() <= radius)
                return ay < 0;
            if (current.getSecond() >= (yLength - radius))
                return ay > 0;
        }
        return false;
    }

    public boolean isHit(Pair<Double, Double> current, Pair<Double, Double> prev) {
        boolean isHit = false;
        if ((current.getFirst() <= radius && prev.getFirst() > radius) || (current.getFirst() >= (xLength - radius) && prev.getFirst() < (xLength - radius))) {
            axis = 0;
            isHit = true;
        }
        if ((current.getSecond() <= radius && prev.getSecond() > radius) || (current.getSecond() >= (yLength - radius) && prev.getSecond() < (yLength - radius))) {
            axis = isHit ? 2 : 1;
            isHit = true;
        }
        return isHit;
    }

    public int getAxis() {
        return axis;
    }

    public State fixCoordination(State current) {
        if (current.getCoordinate().getFirst() < radius)
            current.getCoordinate().setFirst((double) radius);
        else if (current.getCoordinate().getFirst() > xLength - radius)
            current.getCoordinate().setFirst((double) (xLength - radius));
        if (current.getCoordinate().getSecond() < radius)
            current.getCoordinate().setSecond((double) radius);
        else if (current.getCoordinate().getSecond() > yLength - radius)
            current.getCoordinate().setSecond((double) (yLength - radius));
        return current;
    }
}

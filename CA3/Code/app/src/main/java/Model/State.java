package Model;

public class State {
    private Pair<Double, Double> coordinate;
    private Pair<Double, Double> velocity;
    private Pair<Double, Double> acceleration;
    private Pair<Double, Double> angles;

    public State(Pair<Double, Double> coordinate, Pair<Double, Double> velocity, Pair<Double, Double> acceleration) {
        this.coordinate = coordinate;
        this.velocity = velocity;
        this.acceleration = acceleration;
    }

    public State(Pair<Double, Double> coordinate, Pair<Double, Double> velocity, Pair<Double, Double> acceleration,Pair<Double, Double> angles) {
        this.coordinate = coordinate;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.angles = angles;
    }

    public Pair<Double, Double> getAngles() {
        return angles;
    }

    public void setAngles(Pair<Double, Double> angles) {
        this.angles = angles;
    }

    public Pair<Double, Double> getAcceleration() {
        return acceleration;
    }

    public Pair<Double, Double> getCoordinate() {
        return coordinate;
    }

    public double getVx() {
        return velocity.getFirst();
    }

    public double getVy() {
        return velocity.getSecond();
    }

    public void setV(Pair<Double, Double> velocity) {
        this.velocity = velocity;
    }

    @Override
    public String toString() {
        return "State{" +
                "coordinate=" + coordinate +
                ", velocity=" + velocity +
                ", acceleration=" + acceleration +
                '}';
    }
}

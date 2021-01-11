package Client.AI.Brain;

import util.Point;

public class MoveCandidate {

    public final Point attackedPoint;
    public final int score;

    public MoveCandidate(Point origin, int score) {
        this.attackedPoint = origin;
        this.score = score;
    }

    @Override
    public String toString() {
        return "MoveCandidate{" +
                "attackedPoint=" + attackedPoint +
                ", score=" + score +
                '}';
    }
}

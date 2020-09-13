package Client.AI;

import Common.Direction;
import util.Point;

public class AiMove {
    public Direction direction;
    public Point point;

    public AiMove(Direction direction, Point point) {
        this.direction = direction;
        this.point = point;
    }

    public AiMove withAdvancedDirection() {
        return new AiMove(direction, point.moved(direction.vector));
    }

    @Override
    public String toString() {
        return "AiMove{" +
                "direction=" + direction +
                ", point=" + point +
                '}';
    }
}

package Common;

import util.Point;

public enum Direction {

    Left,
    Right,
    Up,
    Down;

    static {
        Left.vector = new Point(0, -1);
        Left.rotated = Up;
        Left.opposite = Right;

        Right.vector = new Point(0, 1);
        Right.rotated = Down;
        Right.opposite = Left;

        Down.vector = new Point(1, 0);
        Down.rotated = Left;
        Down.opposite = Up;

        Up.vector = new Point(-1, 0);
        Up.rotated = Right;
        Up.opposite = Down;
    }

    public Direction rotated;
    public Point vector;
    public Direction opposite;

    public Direction getOpposite() {
        return opposite;
    }

}

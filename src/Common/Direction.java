package Common;

import util.Point;

public enum Direction {

    Left,
    Right,
    Up,
    Down;
//    HORIZONTAL,
//    VERTICAL;

    public Direction rotated;
    public Point vector;
    public Direction opposite;

    static {
//        HORIZONTAL.rotated = VERTICAL;
//        HORIZONTAL.vector = new Point(0,1);
//
//        VERTICAL.rotated = HORIZONTAL;
//        VERTICAL.vector = new Point(1,0);

        Left.vector = new Point(0,-1);
        Left.rotated = Up;
        Left.opposite = Right;

        Right.vector = new Point(0,1);
        Right.rotated = Down;
        Right.opposite = Left;

        Down.vector = new Point(1,0);
        Down.rotated = Left;
        Down.opposite = Up;

        Up.vector = new Point(-1,0);
        Up.rotated = Left;
        Up.opposite = Down;
    }

    public Direction getOpposite(){
        return opposite;
    }

    public Direction getRotated(){
        return rotated;
    }

}

package Common;

import util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {

    private Direction direction;
    public final int size;

    public Ship() {
        size = 0;
    }

    public Ship(Direction direction, int size) {
        this.direction = direction;
        this.size = size;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public List<Point> partsWithOrigin(Point origin) {
        List<Point> parts = new ArrayList<>(size);

        Point current = origin;

        for (int i = 0; i < size; ++i) {
            parts.add(i, current);
            current = current.moved(direction.vector);
        }

        return parts;
    }

}
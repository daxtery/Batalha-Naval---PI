package Common;

import util.Point;

import java.io.Serializable;

public class ShipAndPosition implements Serializable {
    public Ship ship;
    public Point origin;

    public ShipAndPosition() {
    }

    public ShipAndPosition(Ship ship, Point origin) {
        this.ship = ship;
        this.origin = origin;
    }
}

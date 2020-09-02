package Common;

import util.Point;

public class ShipSchematics {

    public final Point origin;
    public final Direction direction;
    public final Ship.ShipType type;

    public ShipSchematics(Point origin, Direction direction, Ship.ShipType type) {
        this.origin = origin;
        this.direction = direction;
        this.type = type;
    }

}

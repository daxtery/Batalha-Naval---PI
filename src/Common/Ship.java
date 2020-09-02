package Common;

import util.Point;

import java.io.Serializable;

public class Ship implements Serializable {

    public ShipType shipType;
    public Direction direction;
    public ShipPiece[] pieces;

    public Ship(ShipPiece[] pieces, Direction direction, ShipType shipType) {
        this.pieces = pieces;
        this.shipType = shipType;
        this.direction = direction;
    }

    public int size() {
        return shipType.value;
    }

    public Ship moveTo(Point newOrigin) {
        Point offset = newOrigin.moved(origin().negated());
        for (ShipPiece piece : this.pieces) {
            piece.point.moved(offset);
        }
        return this;
    }

    public Point origin() {
        return pieces[0].point;
    }

    public Point tail() {
        return pieces[this.pieces.length - 1].point;
    }

    boolean isDestroyed() {
        for (ShipPiece piece : pieces) {
            if (piece.canAttack()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("Ship at ")
                .append(origin())
                .append(" dir: ")
                .append(direction)
                .append(", shipType: ")
                .append(shipType)
                .append("\n");

        for (ShipPiece sp : pieces) {
            s.append(" | ")
                    .append(sp.details())
                    .append("\n");
        }

        return s.toString();
    }

    public enum ShipType {

        One,
        Two,
        Three,
        Four;

        static {
            One.value = 1;
            Two.value = 2;
            Three.value = 3;
            Four.value = 4;
        }

        public int value;

        public static ShipType getShipType(int value) {
            return ShipType.values()[value - 1];
        }

    }

}
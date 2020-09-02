package Common;

import util.Point;

enum ShipPieceStatus {NotAttacked, Attacked, AttackedShipDestroyed}

public class ShipPiece extends BoardTile {

    //WHAT PART OF THE SHIP
    private final int sId;
    Ship ship;

    ShipPiece(int i, Point point, boolean visible) {
        super(point, TileType.ShipPiece, visible);
        sId = i;
    }

    ShipPieceStatus status() {
        return visible ?
                ship.isDestroyed() ?
                        ShipPieceStatus.AttackedShipDestroyed
                        : ShipPieceStatus.Attacked
                : ShipPieceStatus.NotAttacked;
    }

    public int getIdInsideShip() {
        return sId;
    }

    public Ship getShip() {
        return ship;
    }

    @Override
    public String toString() {
        return details();
    }

    String details() {
        return "ShipPiece at " + this.point + ", status: " + status() + " dir: " + (ship != null ? getShip().direction : "-");
    }

}

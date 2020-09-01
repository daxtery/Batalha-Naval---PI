package Common;

import util.Point;

enum ShipPieceStatus {NotAttacked, Attacked, AttackedShipDestroyed}

public class ShipPiece extends BoardTile {

    //WHAT PART OF THE SHIP
    private final int sId;
    Ship ship;

    ShipPiece(Ship _ship, int i, int _x, int _y) {
        super(new Point(_x, _y), TileType.ShipPiece);
        ship = _ship;
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
        return "ShipPiece at " + this.point + ", status: " + status() + " dir: " + getShip().dir;
    }

    String toSendString() {
        if (ship.isDestroyed())
            return PlayerBoardTransformer.PIECE_ATTACKED_SHIP_DESTROYED_STRING;
        if (!canAttack())
            return PlayerBoardTransformer.PIECE_ATTACKED_STRING;
        return PlayerBoardTransformer.PIECE_NOT_ATTACKED_STRING;
    }
}

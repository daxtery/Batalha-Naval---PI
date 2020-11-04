package Common;

import util.Point;

public class BoardTile {

    public final Point point;
    private boolean attackable;

    private Ship ship;
    private int shipPieceIndex;

    public BoardTile(Point point) {
        this.point = point;
        this.attackable = true;
    }

    public void removeShipPiece() {
        this.ship = null;
    }

    public void setShipPiece(Ship ship, int index) {
        this.ship = ship;
        this.shipPieceIndex = index;
    }

    public boolean containsShipPiece() {
        return ship != null;
    }

    public Ship getShip() {
        return ship;
    }

    public int getShipPieceIndex() {
        return shipPieceIndex;
    }

    public boolean isAttackable() {
        return attackable;
    }

    public void setAttackable(boolean attackable) {
        this.attackable = attackable;
    }
}

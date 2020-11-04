package Common;

public class ShipPiece {

    public Ship ship;
    public int shipPieceIndex;

    public ShipPiece(Ship ship, int shipPieceIndex) {
        this.ship = ship;
        this.shipPieceIndex = shipPieceIndex;
    }

    public Ship getShip() {
        return ship;
    }

    public int getShipPieceIndex() {
        return shipPieceIndex;
    }
}

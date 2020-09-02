package Common;

public class PlayerBoardFactory {

    public static final Ship.ShipType[] DEFAULT_SIZES = {
            Ship.ShipType.Four, Ship.ShipType.Three, Ship.ShipType.Three,
            Ship.ShipType.Two, Ship.ShipType.Two, Ship.ShipType.Two,
            Ship.ShipType.One, Ship.ShipType.One, Ship.ShipType.One, Ship.ShipType.One
    };

    public static PlayerBoard getRandomPlayerBoard() {
        PlayerBoard pb = new PlayerBoard();
        pb.placeShips(ShipFactory.getRandomShips(DEFAULT_SIZES));
        //pb.allPieces();
        return pb;
    }

}

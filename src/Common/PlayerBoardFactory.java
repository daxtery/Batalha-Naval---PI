package Common;

import org.jetbrains.annotations.NotNull;

public class PlayerBoardFactory {

    public static final Ship.ShipType[] DEFAULT_SIZES = {
            Ship.ShipType.Four, Ship.ShipType.Three, Ship.ShipType.Three,
            Ship.ShipType.Two, Ship.ShipType.Two, Ship.ShipType.Two,
            Ship.ShipType.One, Ship.ShipType.One, Ship.ShipType.One, Ship.ShipType.One
    };

    public static @NotNull PlayerBoard getRandomPlayerBoard(int lines, int columns, Ship.ShipType[] shipTypes) {
        PlayerBoard pb = new PlayerBoard(lines, columns);
        pb.placeShips(ShipFactory.getRandomShips(lines, columns, shipTypes));
        return pb;
    }

    public static PlayerBoard getRandomPlayerBoard() {
        return getRandomPlayerBoard(PlayerBoardConstants.DEFAULT_LINES, PlayerBoardConstants.DEFAULT_COLUMNS, DEFAULT_SIZES);
    }

}

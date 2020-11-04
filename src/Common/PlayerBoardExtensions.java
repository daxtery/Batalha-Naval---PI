package Common;

import org.jetbrains.annotations.NotNull;
import util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerBoardExtensions {

    private static final Random random = new Random();

    public static void addShipAtRandomPosition(PlayerBoard playerBoard, Ship ship) {

        while (true) {

            int x = random.nextInt(playerBoard.lines());
            int y = random.nextInt(playerBoard.columns());

            final Point at = new Point(x, y);

            if (playerBoard.canShipBeHere(at, ship)) {
                playerBoard.placeShip(at, ship);
                return;
            }
        }

    }

    public static PlayerBoard constructRandomPlayerBoard(GameConfiguration gameConfiguration) {
        PlayerBoard board = new PlayerBoard(gameConfiguration.lines, gameConfiguration.columns);
        List<Integer> remainingShipSizes = new ArrayList<>(gameConfiguration.shipSizes);

        final int shipsCount = remainingShipSizes.size();

        for (int i = 0; i < shipsCount; ++i) {
            final int randomIndex = random.nextInt(gameConfiguration.shipDirections.length);
            final Direction direction = gameConfiguration.shipDirections[randomIndex];
            final int size = remainingShipSizes.remove(random.nextInt(remainingShipSizes.size()));

            addShipAtRandomPosition(board, new Ship(direction, size));
        }

        return board;
    }
}

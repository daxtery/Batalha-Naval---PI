package Common;

import util.Point;

import java.util.Random;

public class ShipFactory {

    private static final Random random = new Random();

    public static Ship build(ShipSchematics schematics) {
        int numberOfPieces = schematics.type.value;
        ShipPiece[] pieces = new ShipPiece[numberOfPieces];
        Point next = schematics.origin;
        Point vector = schematics.direction.vector;
        for (int i = 0; i < numberOfPieces; ++i, next = next.moved(vector)) {
            pieces[i] = new ShipPiece(i, next, false);
        }

        Ship ship = new Ship(pieces, schematics.direction, schematics.type);

        for (int i = 0; i < numberOfPieces; ++i) {
            pieces[i].ship = ship;
        }

        return ship;
    }

    private static Ship constructWithSizeAndPossibleDirections(PlayerBoard pb, Ship.ShipType size, Direction[] directions) {
        while (true) {
            int x = random.nextInt(PlayerBoard.LINES);
            int y = random.nextInt(PlayerBoard.COLUMNS);

            int randomIndex = random.nextInt(directions.length);

            ShipSchematics schematics = new ShipSchematics(new Point(x, y), directions[randomIndex], size);
            Ship tempShip = build(schematics);

            if (pb.canShipBeHere(tempShip)) {
                pb.placeShip(tempShip);
                return tempShip;
            }
        }
    }

    static Ship[] getRandomShips(Ship.ShipType[] types) {
        Ship[] temp = new Ship[PlayerBoard.NUMBER_OF_BOATS];
        // 4
        // 3, 3
        // 2, 2, 2
        // 1, 1, 1, 1
        PlayerBoard tempBoard = new PlayerBoard();
        Direction[] directions = Direction.values();
        int i = 0;
        //System.out.println("I is: " +  i);
        //tempBoard.lightItUp();
        //System.out.println(tempBoard);
        //System.out.println(Arrays.toString(temp));
        do {
            temp[i] = constructWithSizeAndPossibleDirections(tempBoard, types[i], directions);
            i++;
        } while (i != types.length);

        return temp;
    }

}

package Common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Point;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;

class PlayerBoardTest {

    PlayerBoard pb;

    @BeforeEach
    void reset() {
        pb = new PlayerBoard(DEFAULT_LINES, DEFAULT_COLUMNS);
    }

    @Test
    void testAssert() {
        assert (true);
    }

    @Test
    void place4sizedNotEnoughSpace() {
        Point origin = new Point(1, 1);
        Direction direction = Direction.Left;
        int size = 4;

        Ship ship = new Ship(direction, size);

        boolean allowed = pb.canShipBeHere(origin, ship);
        assert (!allowed);

        for (Point ignored : ship.partsWithOrigin(origin)) {
            assert (!pb.isShipPieceAt(origin.moved(direction.vector)));
        }
    }

    @Test
    void place4sized() {
        Point origin = new Point(6, 6);
        Direction direction = Direction.Right;
        int size = 4;

        Ship ship = new Ship(direction, size);

        boolean allowed = pb.canShipBeHere(origin, ship);
        assert (allowed);

        pb.placeShip(origin, ship);

        for (Point ignored : ship.partsWithOrigin(origin)) {
            assert (pb.isShipPieceAt(origin.moved(direction.vector)));
        }
    }

    @Test
    void ships() {

//        origin = new Point(6, 0);
//        direction = Direction.Left;
//        shipType = Ship.ShipType.One;
//        schematics = new ShipSchematics(origin, direction, shipType);
//
//        toAdd = ShipExtensions.build(schematics);
//
//        assert (pb.canShipBeHere(toAdd));
//        pb.placeShip(at, toAdd);
//        assert (!pb.isWaterAt(origin));
//
//        origin = new Point(6, 6);
//        direction = Direction.Up;
//        shipType = Ship.ShipType.One;
//        schematics = new ShipSchematics(origin, direction, shipType);
//        toAdd = ShipExtensions.build(schematics);
//
//        assert (!pb.canShipBeHere(toAdd));
//        assert (!pb.isWaterAt(origin));
//
//        origin = new Point(9, 9);
//        direction = Direction.Up;
//        shipType = Ship.ShipType.Two;
//        schematics = new ShipSchematics(origin, direction, shipType);
//        toAdd = ShipExtensions.build(schematics);
//
//        assert (pb.canShipBeHere(toAdd));
//        pb.placeShip(at, toAdd);
//
//        origin = new Point(7, 9);
//        direction = Direction.Up;
//        shipType = Ship.ShipType.Two;
//        schematics = new ShipSchematics(origin, direction, shipType);
//        toAdd = ShipExtensions.build(schematics);
//
//        assert (!pb.canShipBeHere(toAdd));
//
//        //9,9 -> 2, cima
//        //6,0 -> 1, esquerda
//        //6,6 -> 3, direita
//
//        // SEE IF THERE ARE 20 PIECES
//
//        for (int w = 0; w < 1000; w++) {
//
//            pb = PlayerBoardExtensions.getRandomPlayerBoard();
//            final int lines = pb.lines();
//            final int columns = pb.columns();
//
//            int i = 0;
//            for (int l = 0; l < lines; l++) {
//                for (int c = 0; c < columns; c++) {
//                    if (pb.getTileAt(l, c).tileDescription == TileContent.ShipPiece) {
//                        i++;
//                    }
//                }
//            }
//            assert (i == 20);
//        }
//
//        pb = PlayerBoardExtensions.getRandomPlayerBoard();
//
//
//        pb.getAttacked(0, 8);
//        pb.getAttacked(0, 9);
//        pb.getAttacked(0, 7);
//        pb.getAttacked(0, 6);
//        pb.getAttacked(0, 5);
//        pb.getAttacked(0, 4);
//        pb.getAttacked(0, 3);
//        pb.getAttacked(0, 2);
//        pb.getAttacked(0, 1);
//        pb.getAttacked(0, 0);
//
//        pb.getAttacked(1, 8);
//        pb.getAttacked(2, 9);
//        pb.getAttacked(3, 7);
//        pb.getAttacked(4, 6);
//        pb.getAttacked(5, 5);
//        pb.getAttacked(6, 4);
//        pb.getAttacked(7, 3);
//        pb.getAttacked(8, 2);
//        pb.getAttacked(9, 1);
//
//        pb.getAttacked(1, 1);
//        pb.getAttacked(2, 2);
//        pb.getAttacked(3, 3);
//        pb.getAttacked(4, 4);
//        pb.getAttacked(5, 5);
//        pb.getAttacked(6, 6);
//        pb.getAttacked(7, 7);
//        pb.getAttacked(8, 8);
//        pb.getAttacked(9, 9);
//
//
//        pb.getAttacked(5, 8);
//        pb.getAttacked(5, 9);
//        pb.getAttacked(5, 7);
//        pb.getAttacked(5, 6);
//        pb.getAttacked(4, 5);
//        pb.getAttacked(5, 4);
//        pb.getAttacked(5, 3);
//        pb.getAttacked(5, 2);
//
//        //System.out.println(pb);
//
//        //System.out.println("---------------------------------");
//        //System.out.println("---------------------------------");
//        //System.out.println("---------------------------------");
//
//        //System.out.println(pb);


    }

}
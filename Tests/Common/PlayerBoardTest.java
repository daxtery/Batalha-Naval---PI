package Common;

import org.junit.jupiter.api.Test;
import util.Point;

import static Common.PlayerBoard.COLUMNS;
import static Common.PlayerBoard.LINES;

class PlayerBoardTest {

    PlayerBoard pb = new PlayerBoard();

    @Test
    void testAssert() {
        assert (true);
    }

    @Test
    void ships() {
        Point origin = new Point(1, 1);
        Direction direction = Direction.Left;
        Ship.ShipType shipType = Ship.ShipType.Four;
        ShipSchematics schematics = new ShipSchematics(origin, direction, shipType);

        Ship toAdd = ShipFactory.build(schematics);

        boolean returned = pb.canShipBeHere(toAdd);
        assert (!returned);

        for (ShipPiece shipPiece : toAdd.pieces) {
            assert (pb.freeAt(shipPiece.point.x, shipPiece.point.y));
        }

        origin = new Point(6, 6);
        direction = Direction.Right;
        shipType = Ship.ShipType.Four;
        schematics = new ShipSchematics(origin, direction, shipType);

        toAdd = ShipFactory.build(schematics);
        assert (pb.canShipBeHere(toAdd));
        pb.placeShip(toAdd);
        assert (!pb.freeAt(origin.moved(0, 1)));

        origin = new Point(6, 0);
        direction = Direction.Left;
        shipType = Ship.ShipType.One;
        schematics = new ShipSchematics(origin, direction, shipType);

        toAdd = ShipFactory.build(schematics);

        assert (pb.canShipBeHere(toAdd));
        pb.placeShip(toAdd);
        assert (!pb.freeAt(origin));

        origin = new Point(6, 6);
        direction = Direction.Up;
        shipType = Ship.ShipType.One;
        schematics = new ShipSchematics(origin, direction, shipType);
        toAdd = ShipFactory.build(schematics);

        assert (!pb.canShipBeHere(toAdd));
        assert (!pb.freeAt(origin));

        origin = new Point(9, 9);
        direction = Direction.Up;
        shipType = Ship.ShipType.Two;
        schematics = new ShipSchematics(origin, direction, shipType);
        toAdd = ShipFactory.build(schematics);

        assert (pb.canShipBeHere(toAdd));
        pb.placeShip(toAdd);

        origin = new Point(7, 9);
        direction = Direction.Up;
        shipType = Ship.ShipType.Two;
        schematics = new ShipSchematics(origin, direction, shipType);
        toAdd = ShipFactory.build(schematics);

        assert (!pb.canShipBeHere(toAdd));

        //9,9 -> 2, cima
        //6,0 -> 1, esquerda
        //6,6 -> 3, direita

        // SEE IF THERE ARE 20 PIECES

        for (int w = 0; w < 1000; w++) {

            pb = PlayerBoardFactory.getRandomPlayerBoard();

            int i = 0;
            for (int l = 0; l < LINES; l++) {
                for (int c = 0; c < COLUMNS; c++) {
                    if (pb.getTileAt(l, c).tileType == TileType.ShipPiece) {
                        i++;
                    }
                }
            }
            assert (i == 20);
        }

        pb = PlayerBoardFactory.getRandomPlayerBoard();


        pb.getAttacked(0, 8);
        pb.getAttacked(0, 9);
        pb.getAttacked(0, 7);
        pb.getAttacked(0, 6);
        pb.getAttacked(0, 5);
        pb.getAttacked(0, 4);
        pb.getAttacked(0, 3);
        pb.getAttacked(0, 2);
        pb.getAttacked(0, 1);
        pb.getAttacked(0, 0);

        pb.getAttacked(1, 8);
        pb.getAttacked(2, 9);
        pb.getAttacked(3, 7);
        pb.getAttacked(4, 6);
        pb.getAttacked(5, 5);
        pb.getAttacked(6, 4);
        pb.getAttacked(7, 3);
        pb.getAttacked(8, 2);
        pb.getAttacked(9, 1);

        pb.getAttacked(1, 1);
        pb.getAttacked(2, 2);
        pb.getAttacked(3, 3);
        pb.getAttacked(4, 4);
        pb.getAttacked(5, 5);
        pb.getAttacked(6, 6);
        pb.getAttacked(7, 7);
        pb.getAttacked(8, 8);
        pb.getAttacked(9, 9);


        pb.getAttacked(5, 8);
        pb.getAttacked(5, 9);
        pb.getAttacked(5, 7);
        pb.getAttacked(5, 6);
        pb.getAttacked(4, 5);
        pb.getAttacked(5, 4);
        pb.getAttacked(5, 3);
        pb.getAttacked(5, 2);

        //System.out.println(pb);

        //System.out.println("---------------------------------");
        //System.out.println("---------------------------------");
        //System.out.println("---------------------------------");

        //System.out.println(pb);


    }

}
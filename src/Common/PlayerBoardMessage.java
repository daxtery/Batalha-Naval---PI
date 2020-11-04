package Common;

import util.Point;

import java.io.Serializable;

public class PlayerBoardMessage implements Serializable {

    public Ship[] ships;
    public Point[] positions;
    public Boolean[][] attackableMatrix;

    public PlayerBoardMessage() {
    }

    public PlayerBoardMessage(PlayerBoard playerBoard) {
        this.ships = playerBoard.ships.keySet().toArray(Ship[]::new);
        this.positions = playerBoard.ships.values().toArray(Point[]::new);

        this.attackableMatrix = new Boolean[playerBoard.size.x][playerBoard.size.y];

        final int lines = playerBoard.lines();
        final int columns = playerBoard.columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                BoardTile tile = playerBoard.getTileAt(new Point(l, c));
                this.attackableMatrix[l][c] = tile.isAttackable();
            }
        }
    }

    public PlayerBoard toPlayerBoard() {
        PlayerBoard playerBoard = new PlayerBoard(attackableMatrix.length, attackableMatrix[0].length);

        for (int i = 0; i < ships.length; ++i) {
            Ship ship = ships[i];
            Point origin = positions[i];
            playerBoard.placeShip(origin, ship);
        }

        final int lines = playerBoard.lines();
        final int columns = playerBoard.columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                Point point = new Point(l, c);
                BoardTile tile = playerBoard.getTileAt(point);
                tile.setAttackable(this.attackableMatrix[l][c]);
            }
        }

        return playerBoard;
    }


}

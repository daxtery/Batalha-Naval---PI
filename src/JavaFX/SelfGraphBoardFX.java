package JavaFX;

import Common.BoardTile;
import Common.Direction;
import Common.PlayerBoard;
import Common.ShipPiece;

import static Common.PlayerBoard.COLUMNS;
import static Common.PlayerBoard.LINES;


public class SelfGraphBoardFX extends GraphBoardFX {

    SelfGraphBoardFX(int _w, int _h) {
        super(_w, _h);
    }

    @Override
    void startTiles(String[][] sent) {
        pb = new PlayerBoard(sent);
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                BoardTile boardTile = pb.getTileAt(l, c);

                switch (boardTile.tileType) {
                    case ShipPiece -> {
                        ShipPiece sp = (ShipPiece) pb.getTileAt(l, c);
                        tiles[l][c] = new ShipTileFX(sp.getShip().getSize(), sp.getIdInsideShip(), l, c, sp.getShip().getDirection());
                    }

                    case Water -> tiles[l][c] = new WaterTileFX(l, c, Direction.VERTICAL);
                }

                tiles[l][c].forNormalBoard(false);
            }
        }
    }
}

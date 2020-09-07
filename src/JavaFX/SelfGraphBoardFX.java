package JavaFX;

import Common.*;


public class SelfGraphBoardFX extends GraphBoardFX {

    SelfGraphBoardFX(int lines, int columns, int _w, int _h) {
        super(lines, columns, _w, _h);
    }

    @Override
    void startTiles(String[][] sent) {
        pb = PlayerBoardTransformer.parse(sent);
        final int lines = pb.lines();
        final int columns = pb.columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                BoardTile boardTile = pb.getTileAt(l, c);

                switch (boardTile.tileType) {
                    case ShipPiece -> {
                        ShipPiece sp = (ShipPiece) pb.getTileAt(l, c);
                        tiles[l][c] = new ShipTileFX(sp.getShip().size(), sp.getIdInsideShip(), l, c, sp.getShip().direction);
                    }

                    case Water -> tiles[l][c] = new WaterTileFX(l, c, Direction.Left);
                }

                tiles[l][c].forNormalBoard(false);
            }
        }
    }
}

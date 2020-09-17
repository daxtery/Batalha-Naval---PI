package Client.FX;

import Common.*;


public class SelfGraphBoardFX extends GraphBoardFX {

    public SelfGraphBoardFX(int lines, int columns, int _w, int _h) {
        super(lines, columns, _w, _h);
    }

    @Override
    public void startTiles(String[][] sent) {
        playerBoard = PlayerBoardTransformer.parse(sent);
        final int lines = playerBoard.lines();
        final int columns = playerBoard.columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                BoardTile boardTile = playerBoard.getTileAt(l, c);

                switch (boardTile.tileType) {
                    case ShipPiece -> {
                        ShipPiece sp = (ShipPiece) playerBoard.getTileAt(l, c);
                        tiles[l][c] = new ShipTileFX(sp.getShip().size(), sp.getIdInsideShip(), l, c, sp.getShip().direction);
                    }

                    case Water -> tiles[l][c] = new WaterTileFX(l, c, Direction.Left);
                }

                tiles[l][c].forNormalBoard(false);
            }
        }
    }
}

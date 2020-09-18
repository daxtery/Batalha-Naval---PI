package Client.FX;

import Common.PlayerBoard;
import javafx.scene.layout.GridPane;
import util.Point;

public class PlayerBoardFX extends GridPane {

    public final Tile[][] tiles;
    public final boolean isEnemy;

    public PlayerBoardFX(int lines, int columns, boolean isEnemy) {
        this.isEnemy = isEnemy;
        tiles = new Tile[lines][columns];

        for (int j = 0; j < lines; j++) {
            for (int k = 0; k < columns; k++) {
                Tile tile = new Tile(new Point(j, k));
                add(tile, j, k);
            }
        }
    }

    public void setBoard(PlayerBoard board) {
        for (int j = 0; j < tiles.length; j++) {
            for (int k = 0; k < tiles[0].length; k++) {
                tiles[j][k].update(isEnemy, board.boardTiles[j][k]);
            }
        }
    }
}

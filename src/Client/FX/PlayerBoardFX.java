package Client.FX;

import Common.PlayerBoard;
import javafx.scene.layout.GridPane;
import util.Point;

public class PlayerBoardFX extends GridPane {

    public final Tile[][] tiles;
    public final boolean isEnemy;
    private LocationAttackerHandler handler;

    public PlayerBoardFX(int lines, int columns, boolean isEnemy) {
        this.isEnemy = isEnemy;
        tiles = new Tile[lines][columns];

        for (int j = 0; j < lines; j++) {
            for (int k = 0; k < columns; k++) {
                Point point = new Point(j, k);
                Tile tile = new Tile(point);
                add(tile, k, j);
                tile.setOnMouseClicked(clicked -> {
                    if (handler != null) {
                        handler.handle(point);
                    }
                });

                tiles[j][k] = tile;
            }
        }
    }

    public void setBoard(PlayerBoard board) {
        for (int j = 0; j < tiles.length; j++) {
            for (int k = 0; k < tiles[0].length; k++) {
                tiles[j][k].update(!isEnemy, board.boardTiles[j][k]);
            }
        }
    }

    public void setOnLocationAttacked(LocationAttackerHandler handler) {
        this.handler = handler;
    }
}

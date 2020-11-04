package Client.FX;

import Common.PlayerBoard;
import javafx.scene.layout.GridPane;
import util.Point;

public class PlayerBoardFX extends GridPane {

    public final Tile[][] tiles;
    public final boolean isEnemy;
    private LocationEnteredHandler enteredHandler;
    private LocationExitedHandler exitedHandler;
    private LocationAttackerHandler clickedHandler;

    public PlayerBoardFX(int lines, int columns, boolean isEnemy) {
        this.isEnemy = isEnemy;
        tiles = new Tile[lines][columns];

        for (int j = 0; j < lines; j++) {
            for (int k = 0; k < columns; k++) {
                Point point = new Point(j, k);
                Tile tile = new Tile(point);
                add(tile, k, j);
                tile.setOnMouseClicked(clicked -> {
                    if (clickedHandler != null) {
                        clickedHandler.handle(point);
                    }
                });

                tile.setOnMouseEntered(clicked -> {
                    if (enteredHandler != null) {
                        enteredHandler.handle(point);
                    }
                });

                tile.setOnMouseExited(clicked -> {
                    if (exitedHandler != null) {
                        exitedHandler.handle(point);
                    }
                });

                tiles[j][k] = tile;
            }
        }
    }

    public void setBoard(PlayerBoard board) {
        for (int j = 0; j < tiles.length; j++) {
            for (int k = 0; k < tiles[0].length; k++) {
                tiles[j][k].update(!isEnemy, new Point(j, k), board);
            }
        }
    }

    public void setEnteredHandler(LocationEnteredHandler enteredHandler) {
        this.enteredHandler = enteredHandler;
    }

    public void setExitedHandler(LocationExitedHandler exitedHandler) {
        this.exitedHandler = exitedHandler;
    }

    public void setClickedHandler(LocationAttackerHandler clickedHandler) {
        this.clickedHandler = clickedHandler;
    }
}

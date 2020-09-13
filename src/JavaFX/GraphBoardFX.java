package JavaFX;

import Common.*;
import javafx.animation.AnimationTimer;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import util.Point;


public class GraphBoardFX extends EmptyGraphBoardFX {

    TileFX[][] tiles;
    PlayerBoard pb;
    Point last;

    public GraphBoardFX(int lines, int columns, int _w, int _h) {
        super(lines, columns, _w, _h);
        tiles = new TileFX[lines][columns];
        gc.setLineWidth(1);
        last = null;

        anim = new AnimationTimer() {
            final double perSec = 1;
            final int x_max = columns * TileFX.TILE_SIZE;
            final int y_max = lines * TileFX.TILE_SIZE;
            long lastNano = System.nanoTime();

            public void handle(long currentNanoTime) {
                if ((currentNanoTime - lastNano) / 1000000000.0 > perSec) {
                    for (int l = 0; l < lines; l++)
                        for (int c = 0; c < columns; c++)
                            tiles[l][c].draw(gc);
                    for (int l = 0; l < lines; l++)
                        gc.strokeLine(0, l * TileFX.TILE_SIZE, x_max, l * TileFX.TILE_SIZE);
                    for (int c = 0; c < columns; c++)
                        gc.strokeLine(c * TileFX.TILE_SIZE, 0, c * TileFX.TILE_SIZE, y_max);
                    if (last != null) {
                        Paint p = gc.getStroke();
                        double gcW = gc.getLineWidth();
                        gc.setLineWidth(4);
                        gc.setStroke(Color.rgb(255, 50, 50, 0.5));
                        gc.strokeRect(last.y * TileFX.TILE_SIZE, last.x * TileFX.TILE_SIZE, TileFX.TILE_SIZE, TileFX.TILE_SIZE);
                        gc.setStroke(p);
                        gc.setLineWidth(gcW);
                    }
                    lastNano = currentNanoTime;
                }
            }
        };
    }

    /**
     * @param event
     * @return a point with coordenates (L, C) -> L - Line; C -> Column
     */
    public Point pointCoordinates(MouseEvent event) {
        //BECAUSE ON SCREEN IS THE OTHER WAY AROUND
        int l = (int) event.getY() / TileFX.TILE_SIZE;
        int c = (int) event.getX() / TileFX.TILE_SIZE;

        if (l > 9 || l < 0 || c > 9 || c < 0)
            return null;

        return new Point(l, c);
    }

    public void startTiles(String[][] sent) {

        pb = PlayerBoardTransformer.parse(sent);
        final int lines = pb.lines();
        final int columns = pb.columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                BoardTile tile = pb.getTileAt(l, c);
                switch (tile.tileType) {
                    case Water -> addWaterTileFX(l, c, (WaterTile) pb.getTileAt(l, c));
                    case ShipPiece -> {
                        ShipPiece sp = (ShipPiece) pb.getTileAt(l, c);
                        //System.out.println(sp.getShip().getDirection());
                        addShipTileFX(l, c, sp);
                    }
                }
            }
        }
    }

    void addShipTileFX(int l, int c, ShipPiece sp) {
        tiles[l][c] = new ShipTileFX(sp.getShip().size(), sp.getIdInsideShip(), l, c, sp.getShip().direction);
    }

    void addWaterTileFX(int l, int c, WaterTile wt) {
        tiles[l][c] = new WaterTileFX(l, c, Direction.Right);
    }

    public void updateTiles(String[][] sent) {
        final int lines = sent.length;
        final int columns = sent[0].length;

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                TileFX t = tiles[l][c];
                switch (sent[l][c]) {
                    case PlayerBoardTransformer.PIECE_ATTACKED_SHIP_DESTROYED_STRING:
                        ShipTileFX st = (ShipTileFX) t;
                        st.shipDestroyed();
                        break;
                    case PlayerBoardTransformer.PIECE_ATTACKED_STRING:
                        st = (ShipTileFX) t;
                        st.attack();
                        break;
                    case PlayerBoardTransformer.PIECE_NOT_ATTACKED_STRING:
                        st = (ShipTileFX) t;
                        st.attacked = false;
                        break;
                    case PlayerBoardTransformer.WATER_NOT_VISIBLE_STRING:
                        WaterTileFX wt = (WaterTileFX) t;
                        wt.attacked = false;
                        break;
                    case PlayerBoardTransformer.WATER_VISIBLE_STRING:
                        wt = (WaterTileFX) t;
                        wt.attack();
                        break;
                }
            }
        }
    }

    public void setPlayerBoard(PlayerBoard playerBoard) {
        pb = playerBoard;
    }

    public void setLast(Point p) {
        last = p;
    }
}

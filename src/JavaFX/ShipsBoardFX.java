package JavaFX;

import Common.*;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import util.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Common.PlayerBoard.COLUMNS;
import static Common.PlayerBoard.LINES;
import static Common.PlayerBoardFactory.DEFAULT_SIZES;

public class ShipsBoardFX extends GraphBoardFX {

    ShipFX[] shipsFX;
    ShipFX selected;
    List<Point> tilesToDraw;
    boolean canPlace;
    boolean toRotate;
    boolean finished;

    ShipsBoardFX(int _w, int _h) {
        super(_w, _h);
        shipsFX = new ShipFX[10];
        tilesToDraw = new ArrayList<>();
        canPlace = false;
        toRotate = false;
        initShips();

        anim = new AnimationTimer() {
            final double perSec = 0;
            final int x_max = COLUMNS * TileFX.TILE_SIZE;
            final int y_max = LINES * TileFX.TILE_SIZE;
            long lastNano = System.nanoTime();

            public void handle(long currentNanoTime) {

                if (((currentNanoTime - lastNano) / 1000000000.0) > perSec) {
                    Paint fill = gc.getFill();
                    Paint stroke = gc.getStroke();
                    gc.clearRect(0, 0, getWidth(), getHeight());
                    gc.drawImage(new Image("images/agua_bg.png"), 0, 0);
                    for (int l = 0; l < LINES; l++)
                        gc.strokeLine(0, l * TileFX.TILE_SIZE, x_max, l * TileFX.TILE_SIZE);
                    for (int c = 0; c < COLUMNS; c++)
                        gc.strokeLine(c * TileFX.TILE_SIZE, 0, c * TileFX.TILE_SIZE, y_max);

                    for (ShipFX s : shipsFX)
                        s.draw(gc);
                    gc.setStroke(Color.BLUE);
                    if (selected != null)
                        gc.strokeRect(selected.x, selected.y, selected.width, selected.height);
                    gc.setFill(Color.rgb(3, 200, 100, 0.5));
                    if (!canPlace)
                        gc.setFill(Color.rgb(233, 0, 3, 0.5));
                    if (tilesToDraw.size() > 0) {
                        for (Point p : tilesToDraw) {
                            gc.fillRect(p.x * TileFX.TILE_SIZE, p.y * TileFX.TILE_SIZE, TileFX.TILE_SIZE, TileFX.TILE_SIZE);
                        }
                    }

                    gc.setFill(Color.WHITE);

                    for (int l = 0; l < LINES; l++) {
                        for (int c = 0; c < COLUMNS; c++) {
                            gc.fillText(l + ":" + c, (c + 0.5) * TileFX.TILE_SIZE, (l + 0.5) * TileFX.TILE_SIZE);
                        }
                    }

                    gc.setFill(fill);
                    gc.setStroke(stroke);
                    lastNano = currentNanoTime;
                }
            }
        };
    }

    void initShips() {
        for (int i = 0; i < DEFAULT_SIZES.length; i++) {
            shipsFX[i] = new ShipFX(DEFAULT_SIZES[i].value);
            int startX = TileFX.TILE_SIZE * COLUMNS + 5;
            shipsFX[i].setPosition(startX, i * TileFX.TILE_SIZE);
        }
    }

    void initShips(PlayerBoard _pb) {
        pb = _pb;
        List<Ship> ships = pb.getShips();
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            //System.out.println(s);
            shipsFX[i] = new ShipFX(s.size(), s.origin(), s.direction, true);
            shipsFX[i].setShip(s);
        }

        for (int i = 0; i < ships.size(); i++) {
            System.out.println(shipsFX[i].ship);
        }

    }

    private Ship buildSelectedStartingAt(int l, int c) {
        ShipSchematics schematics = new ShipSchematics(new Point(l, c), selected.dir, Ship.ShipType.getShipType(selected.shipSize));
        return ShipFactory.build(schematics);
    }

    void seeIfShipFXCanBePlaced(double x, double y) {
        tilesToDraw.clear();

        if (!(x < 0 || x > LINES * TileFX.TILE_SIZE || y < 0 || y > COLUMNS * TileFX.TILE_SIZE))
            if (selected != null) {

                int l = (int) y / TileFX.TILE_SIZE;
                int c = (int) x / TileFX.TILE_SIZE;

                Ship temp = buildSelectedStartingAt(l, c);

                canPlace = pb.canShipBeHere(temp);
                for (ShipPiece sp : temp.pieces)
                    tilesToDraw.add(sp.point.flipped());
            }
    }

    boolean canPlace(double x, double y) {
        seeIfShipFXCanBePlaced(x, y);
        return canPlace;
    }

    void placeShipFX(double x, double y) {

        int l = (int) y / TileFX.TILE_SIZE;
        int c = (int) x / TileFX.TILE_SIZE;

        if (l > 9 || l < 0 || c > 9 || c < 0)
            return;

        if (canPlace && selected != null) {
            Ship temp = buildSelectedStartingAt(l, c);
            pb.placeShip(temp);
            selected.setShip(temp);
            selected = null;
        }
    }


    public ShipFX checkAShip(double x, double y) {
        for (ShipFX s : shipsFX)
            if (s.contains(x, y))
                return s;
        return null;
    }

    void setSelected(ShipFX _selected) {
        selected = _selected;
    }

    public void removeShipFX(ShipFX s) {
        pb.removeShip(s.ship);
    }

    @Override
    public void setPlayerBoard(PlayerBoard playerBoard) {
        pb = playerBoard;
        initShips(pb);
    }
}

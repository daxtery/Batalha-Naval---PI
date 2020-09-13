package Client.FX;

import Common.*;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import util.Point;

import java.util.List;

import static Common.PlayerBoardFactory.DEFAULT_SIZES;

public class ShipsBoardFX extends GraphBoardFX {

    ShipFX[] shipsFX;

    ShipFX selected;
    ShipFX preview;

    boolean canPlace;

    public ShipsBoardFX(int lines, int columns, int w, int h) {
        super(lines, columns, w, h);
        shipsFX = new ShipFX[DEFAULT_SIZES.length];
        canPlace = false;
        initShips(columns);

        anim = new AnimationTimer() {
            final double perSec = 0;
            final int x_max = columns * TileFX.TILE_SIZE;
            final int y_max = lines * TileFX.TILE_SIZE;
            long lastNano = System.nanoTime();

            public void handle(long currentNanoTime) {

                if (((currentNanoTime - lastNano) / 1000000000.0) > perSec) {

                    Paint fill = gc.getFill();
                    Paint stroke = gc.getStroke();

                    gc.clearRect(0, 0, getWidth(), getHeight());
                    gc.drawImage(new Image("images/agua_bg.png"), 0, 0);

                    for (int l = 0; l < lines; l++) {
                        gc.strokeLine(0, l * TileFX.TILE_SIZE, x_max, l * TileFX.TILE_SIZE);
                    }

                    for (int c = 0; c < columns; c++) {
                        gc.strokeLine(c * TileFX.TILE_SIZE, 0, c * TileFX.TILE_SIZE, y_max);
                    }

                    for (ShipFX s : shipsFX) {

                        if (s == selected && selected.ship != null) {
                            gc.setGlobalAlpha(0.5);
                            s.draw(gc);
                            gc.setGlobalAlpha(1);
                            continue;
                        }

                        s.draw(gc);

                    }

                    if (selected != null) {
                        gc.setStroke(Color.BLUE);
                        gc.strokeRect(selected.x, selected.y, selected.width, selected.height);
                    }

                    gc.setStroke(stroke);

                    if (preview != null && preview.ship != null) {

                        final Color ok = Color.rgb(3, 200, 100, 0.5);
                        final Color forbidden = Color.rgb(233, 0, 3, 0.5);

                        gc.setFill(canPlace ? ok : forbidden);

                        preview.draw(gc);

                        for (ShipPiece piece : preview.ship.pieces) {
                            gc.fillRect(piece.point.y * TileFX.TILE_SIZE, piece.point.x * TileFX.TILE_SIZE, TileFX.TILE_SIZE, TileFX.TILE_SIZE);
                        }

                    }

                    gc.setFill(Color.WHITE);

                    for (int l = 0; l < lines; l++) {
                        for (int c = 0; c < columns; c++) {
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

    void initShips(int columns) {
        for (int i = 0; i < DEFAULT_SIZES.length; i++) {
            shipsFX[i] = new ShipFX(DEFAULT_SIZES[i].value);
            int startX = TileFX.TILE_SIZE * columns + 5;
            shipsFX[i].setPosition(startX, i * TileFX.TILE_SIZE);
        }
    }

    public void initShips(PlayerBoard _pb) {
        pb = _pb;
        List<Ship> ships = pb.getShips();
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            //System.out.println(s);
            shipsFX[i] = new ShipFX(s.size(), s.origin(), s.direction, true);
            shipsFX[i].setShip(s);
        }
        deselectSelected();
    }

    @Override
    public void setPlayerBoard(PlayerBoard playerBoard) {
        pb = playerBoard;
        initShips(pb);
    }

    public ShipFX raycastForShip(double x, double y) {
        for (ShipFX s : shipsFX)
            if (s.contains(x, y))
                return s;
        return null;
    }

    boolean insideBoard(double x, double y) {
        final int lines = pb.lines();
        final int columns = pb.columns();

        return !(x < 0 || x > lines * TileFX.TILE_SIZE || y < 0 || y > columns * TileFX.TILE_SIZE);
    }

    private Point boardPointFromScreenCoords(double x, double y) {
        int line = (int) (y / TileFX.TILE_SIZE);
        int column = (int) (x / TileFX.TILE_SIZE);
        return new Point(line, column);
    }

    private void recalculatePreview(Point point) {
        ShipSchematics schematics = new ShipSchematics(point, preview.dir, Ship.ShipType.getShipType(preview.shipSize));
        Ship ship = ShipFactory.build(schematics);
        preview.setShip(ship);
    }

    public void OnMouseMoved(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        if (!insideBoard(x, y)) {
            return;
        }

        if (preview == null) {
            return;
        }

        Point point = boardPointFromScreenCoords(x, y);
        recalculatePreview(point);
        computeCanPlacePreview();
    }

    public void computeCanPlacePreview() {
        if (selected != null && selected.ship != null) {
            Ship old = selected.ship;
            pb.removeShip(old);
            canPlace = pb.canShipBeHere(preview.ship);
            pb.placeShip(old);
            return;
        }

        canPlace = pb.canShipBeHere(preview.ship);
    }

    public void select(ShipFX shipFX) {
        selected = shipFX;
        preview = new ShipFX(selected.shipSize);
        preview.dir = selected.dir;

        if (selected.ship != null) {
            preview.setShip(selected.ship);
        }
    }

    public void deselectSelected() {
        preview = null;
        selected = null;
    }

    public void OnMouseClicked(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        ShipFX result = raycastForShip(x, y);

        switch (event.getButton()) {
            case PRIMARY -> {
                if (preview == null || selected == null) {
                    return;
                }

                computeCanPlacePreview();

                if (!canPlace) {
                    return;
                }

                ShipSchematics schematics = new ShipSchematics(preview.ship.origin(), preview.dir, Ship.ShipType.getShipType(preview.shipSize));
                Ship ship = ShipFactory.build(schematics);

                if (selected.ship != null) {
                    Ship old = selected.ship;
                    pb.removeShip(old);
                }

                selected.setShip(ship);
                pb.placeShip(ship);
                deselectSelected();
            }
            case SECONDARY -> {
                if (result == null) {
                    return;
                }

                if (selected == result) {
                    deselectSelected();
                    return;
                }

                select(result);
            }
            case NONE, MIDDLE -> {
            }
        }
    }

    public void OnRotateKeyPressed() {
        if (preview == null) return;

        preview.dir = preview.dir.rotated;
        recalculatePreview(preview.ship.origin());
        computeCanPlacePreview();
    }
}

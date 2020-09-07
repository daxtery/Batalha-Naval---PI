package Common;

import util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerBoard implements Serializable {

    public final static int NUMBER_OF_BOATS = 10;
    public final Point size;
    public BoardTile[][] boardTiles;

    public List<Ship> ships;
    public List<ShipPiece> pieces;

    public PlayerBoard(int lines, int columns) {
        size = new Point(lines, columns);
        boardTiles = new BoardTile[lines][lines];
        pieces = new ArrayList<>();
        fillWithWater();
    }

    public int lines() {
        return size.x;
    }

    public int columns() {
        return size.y;
    }

    public boolean inBounds(int x, int y) {
        return inBounds(new Point(x, y));
    }

    public boolean inBounds(Point point) {
        final Point lower = new Point(-1, -1);

        return point.isConstrainedBy(
                lower,
                size
        );
    }

    public List<Ship> getShips() {
        if (ships == null) {
            ships = Arrays.stream(boardTiles)
                    .flatMap(row -> Arrays.stream(row)
                            .filter(tile -> tile.tileType == TileType.ShipPiece)
                    )
                    .map(tile -> ((ShipPiece) tile).ship)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return ships;
    }

    public List<ShipPiece> getPieces() {
        if (pieces == null) {
            pieces = Arrays.stream(boardTiles)
                    .flatMap(row -> Arrays.stream(row)
                            .filter(tile -> tile.tileType == TileType.ShipPiece)
                    )
                    .map(tile -> (ShipPiece) tile)
                    .collect(Collectors.toList());
        }

        return pieces;
    }

    protected void fillWithWater() {

        final int lines = lines();
        final int columns = columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                boardTiles[l][c] = new WaterTile(l, c);
            }
        }
    }

    public AttackResult getAttacked(Point point) {
        if (!inBounds(point)) return AttackResult.OutsideBounds();

        BoardTile boardTile = getTileAt(point);

        if (boardTile.visible) return AttackResult.AlreadyVisible();

        boardTile.visible = true;

        return switch (boardTile.tileType) {
            case Water -> AttackResult.HitWater();

            case ShipPiece -> {
                ShipPiece shipPiece = (ShipPiece) boardTile;
                pieces.remove(shipPiece);
                Ship ship = shipPiece.ship;
                if (ship.isDestroyed()) {
                    shipDestroyed(ship);
                    yield AttackResult.HitShipPiece(true);
                }
                yield AttackResult.HitShipPiece(false);
            }
        };
    }

    public AttackResult getAttacked(int x, int y) {
        return getAttacked(new Point(x, y));
    }

    public List<Point> getAvailable() {
        List<Point> points = new ArrayList<>();
        final int lines = lines();
        final int columns = columns();

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                if (boardTiles[l][c].canAttack()) {
                    points.add(new Point(l, c));
                }
            }
        }

        return points;
    }

    public boolean isGameOver() {
        return pieces.isEmpty();
    }

    private void shipDestroyed(Ship s) {
        for (ShipPiece piece : s.pieces) {
            Point[] points = getSurroundingPoints(piece.point);
            for (Point point : points) {
                if (inBounds(point.x, point.y)) {
                    getTileAt(point.x, point.y).visible = true;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        final int lines = lines();
        final int columns = lines();

        for (int i = 0; i < lines; i++) {
            for (int c = 0; c < columns; c++) {
                s.append(boardTiles[i][c])
                        .append("\n");
            }
            s.append("\n");
        }
        return s.toString();
    }

    protected void placeShips(Ship[] toAdd) {
        for (Ship ship : toAdd) {
            placeShip(ship);
        }
    }

    public void placeShip(Ship toAdd) {
        for (ShipPiece piece : toAdd.pieces) {
            boardTiles[piece.point.x][piece.point.y] = piece;
            pieces.add(piece);
        }
    }

    protected Point[] getSurroundingPoints(Point point) {
        return new Point[]{
                point.moved(1, 0),
                point.moved(1, 1),
                point.moved(1, -1),

                point.moved(-1, 0),
                point.moved(-1, 1),
                point.moved(-1, -1),

                point.moved(0, 1),
                point.moved(0, -1),
        };
    }

    public boolean canShipBeHere(Ship toAdd) {
        for (ShipPiece piece : toAdd.pieces) {
            boolean isInBounds = inBounds(piece.point);
            if (!isInBounds) {
                return false;
            }
            boolean isNotAdjacent = checkSurroundings(piece);
            if (!isNotAdjacent || !freeAt(piece.point)) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkSurroundings(BoardTile tile) {
        return checkSurroundings(tile.point);
    }

    protected boolean checkSurroundings(Point _point) {
        Point[] points = getSurroundingPoints(_point);
        for (Point point : points) {
            if (inBounds(point.x, point.y)) {
                if (!freeAt(point.x, point.y)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean freeAt(Point point) {
        if (inBounds(point)) {
            return getTileAt(point).tileType == TileType.Water;
        }
        return true;
    }

    protected boolean freeAt(int x, int y) {
        return freeAt(new Point(x, y));
    }

    public BoardTile getTileAt(Point point) {
        if (inBounds(point)) {
            return boardTiles[point.x][point.y];
        }
        return null;
    }

    public BoardTile getTileAt(int x, int y) {
        return getTileAt(new Point(x, y));
    }

    public void removeShip(Ship ship) {
        for (ShipPiece piece : ship.pieces) {
            //System.out.println("REMOVING " + piece.getClass().getSimpleName() + " AT: " + piece.x + " " + piece.y);
            boardTiles[piece.point.x][piece.point.y] = new WaterTile(piece.point.x, piece.point.y);
            pieces.remove(piece);
        }
    }

}

package Common;

import util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerBoard implements Serializable {

    public static final int LINES = 10;
    public static final int COLUMNS = 10;
    public final static int NUMBER_OF_BOATS = 10;
    public BoardTile[][] boardTiles;
    private final List<Ship> ships;
    private final List<ShipPiece> pieces;

    public PlayerBoard() {
        boardTiles = new BoardTile[LINES][COLUMNS];
        pieces = new ArrayList<>();
        ships = new ArrayList<>();
        fillWithWater();
    }

    //region TransformBack!

    public static boolean inBounds(int x, int y) {
        return inBounds(new Point(x, y));
    }

    public static boolean inBounds(Point point) {
        return point.isConstrainedBy(
                new Point(-1, -1),
                new Point(LINES, COLUMNS)
        );
    }

    public List<Ship> getShips() {
        return ships;
    }

    private void fillWithWater() {
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
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
        ArrayList<Point> points = new ArrayList<>();
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                if (boardTiles[l][c].canAttack())
                    points.add(new Point(l, c));
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
        for (int i = 0; i < LINES; i++) {
            for (int c = 0; c < COLUMNS; c++) {
                s.append(boardTiles[i][c]).append("\n");
            }
            s.append("\n");
        }
        return s.toString();
    }

    void placeShips(Ship[] toAdd) {
        //int i = 0;
        for (Ship ship : toAdd) {
            //ships[i] = ship;
            //i++;
            placeShip(ship);
        }
    }

    //region CanPlaceShip

    public void placeShip(Ship toAdd) {
        ships.add(toAdd);
        for (ShipPiece piece : toAdd.pieces) {
            //System.out.println("PLACING " + piece.getClass().getSimpleName() + " AT: " + piece.x + " " + piece.y);
            boardTiles[piece.point.x][piece.point.y] = piece;
            pieces.add(piece);
        }
    }

    private Point[] getSurroundingPoints(Point point) {
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
            //System.out.println(piece.getPointCoordinates());
            boolean isInBounds = inBounds(piece.point);
            if (!isInBounds) {
                //System.out.println("NO BOUNDS");
                return false;
            }
            boolean isNotAdjacent = checkSurroundings(piece);
            if (!isNotAdjacent || !freeAt(piece.point)) {
                //System.out.println("ADJACENT");
                return false;
            }
        }
        return true;
    }

    private boolean checkSurroundings(BoardTile tile) {
        return checkSurroundings(tile.point);
    }

    private boolean checkSurroundings(Point _point) {
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

    //endregion

    boolean freeAt(Point point) {
        if (inBounds(point)) {
            return getTileAt(point).tileType == TileType.Water;
        }
        return true;
    }

    boolean freeAt(int x, int y) {
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
        ships.remove(ship);
        for (ShipPiece piece : ship.pieces) {
            //System.out.println("REMOVING " + piece.getClass().getSimpleName() + " AT: " + piece.x + " " + piece.y);
            boardTiles[piece.point.x][piece.point.y] = new WaterTile(piece.point.x, piece.point.y);
            pieces.remove(piece);
        }
    }

    public boolean fullOfShips() {
        //System.out.println(pieces.size());
        return pieces.size() == 20;
    }

}

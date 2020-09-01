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
    private String[][] toPaint;
    private List<Ship> ships;
    private ArrayList<ShipPiece> pieces;
    private boolean lastShipDestroyed;
    private boolean actualHit;
    private boolean shipHit;

    public PlayerBoard() {
        boardTiles = new BoardTile[LINES][COLUMNS];
        pieces = new ArrayList<>();
        toPaint = new String[LINES][COLUMNS];
        ships = new ArrayList<>();
        fillWithWater();
    }

    public PlayerBoard(String[][] sent) {
        this();
        transformBack(sent);
    }

    public static boolean aPieceInTheArray(String[][] sent, int l, int c) {
        return sent[l][c].equalsIgnoreCase(PlayerBoardTransformer.PIECE_ATTACKED_STRING) ||
                sent[l][c].equalsIgnoreCase(PlayerBoardTransformer.PIECE_NOT_ATTACKED_STRING) ||
                sent[l][c].equalsIgnoreCase(PlayerBoardTransformer.PIECE_ATTACKED_SHIP_DESTROYED_STRING);
    }

    public static PlayerBoard getRandomPlayerBoard() {
        PlayerBoard pb = new PlayerBoard();
        pb.placeShips(Ship.getRandomShips());
        //pb.allPieces();
        return pb;
    }

    public static void printDoubleArray(String[][] array) {
        StringBuilder s = new StringBuilder();
        for (String[] arry : array) {
            s.append("\n");
            for (String e : arry)
                s.append(",").append(e);
        }
        System.out.println(s);
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

    void seeAllShips() {
        Ship old = null;
        for (ShipPiece piece : pieces) {
            if (!piece.ship.equals(old)) {
                System.out.println(piece.ship);
                old = piece.ship;
            }
        }
    }


    //FROM String[][] go get the ship
    private Ship constructShip(ArrayList<Point> toSkip, String[][] sent, int l, int c) {
        return initializeShipConstruction(toSkip, sent, l, c).getShip();
    }

    private ConstructorShip initializeShipConstruction(ArrayList<Point> toSkip, String[][] sent, int l, int c) {

        ConstructorShip ship = new ConstructorShip(l, c);

        int[] vec = Direction.HORIZONTAL.getDirectionVector();
        //HORIZONTAL
        if (inBounds(l + vec[0], c + vec[1]) && aPieceInTheArray(sent, l + vec[0], c + vec[1])) {
            ship.setDirection(Direction.HORIZONTAL);
            return buildIt(toSkip, sent, l, c, ship, Direction.HORIZONTAL, 0);
        }
        //VERTICAL OR JUST 1 PIECE-SHIP
        ship.setDirection(Direction.VERTICAL);
        return buildIt(toSkip, sent, l, c, ship, Direction.VERTICAL, 0);
    }

    //endregion

    private ConstructorShip buildIt(ArrayList<Point> toSkip, String[][] sent, int l, int c, ConstructorShip ship, Direction _dir, int i) {

        ShipPiece sp = new ShipPiece(null, i, l, c);
        if (sent[l][c].equalsIgnoreCase(PlayerBoardTransformer.PIECE_NOT_ATTACKED_STRING)) {
            sp = new ShipPiece(null, i, l, c);
        } else {
            sp.visible = true;
        }
        toSkip.add(new Point(l, c));
        ship.addPiece(sp);

        int[] vec = _dir.getDirectionVector();
        Point newP = new Point(l + vec[0], c + vec[1]);

        if (inBounds(newP) && aPieceInTheArray(sent, newP.x, newP.y)) {
            i++;
            ship = buildIt(toSkip, sent, newP.x, newP.y, ship, _dir, i);
        }
        return ship;
    }

    //region attacked

    private void transformBack(String[][] sent) {

        ArrayList<Point> toSkip = new ArrayList<>();

        for (int l = 0; l < LINES; l++)
            for (int c = 0; c < COLUMNS; c++) {
                if (toSkip.contains(new Point(l, c))) {
                    //already found it
                    continue;
                }
                if (aPieceInTheArray(sent, l, c)) {
                    Ship s = constructShip(toSkip, sent, l, c);
                    placeShip(s);
                } else {
                    //WATER
                    boardTiles[l][c] = new WaterTile(l, c);
                    if (sent[l][c].equalsIgnoreCase(PlayerBoardTransformer.WATER_VISIBLE_STRING)) {
                        boardTiles[l][c].visible = true;
                    }
                }
            }
    }

    //endregion

    private void fillWithWater() {
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                boardTiles[l][c] = new WaterTile(l, c);
                toPaint[l][c] = PlayerBoardTransformer.WATER_NOT_VISIBLE_STRING;
            }
        }
    }

    public AttackResult getAttacked(int x, int y) {
        if (!inBounds(new Point(x, y))) return AttackResult.OutsideBounds;

        BoardTile boardTile = getTileAt(x, y);
        actualHit = false;

        if (boardTile.visible) return AttackResult.AlreadyVisible;

        actualHit = true;
        boardTile.visible = true;

        return switch (boardTile.tileType) {
            case Water -> {
                shipHit = false;
                yield AttackResult.HitWater;
            }

            case ShipPiece -> {
                shipHit = true;
                toPaint[x][y] = PlayerBoardTransformer.PIECE_ATTACKED_STRING;
                pieces.remove((ShipPiece) boardTile);
                Ship ship = ((ShipPiece) boardTile).ship;
                lastShipDestroyed = false;
                if (ship.isDestroyed()) {
                    lastShipDestroyed = true;
                    shipDestroyed(ship);
                }
                yield AttackResult.HitShipPiece;
            }
        };
    }

    public ArrayList<Point> getAvailable() {
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
        for (ShipPiece piece : s.getPieces()) {
            Point[] points = getSurroundingPoints(piece.point);
            for (Point point : points) {
                if (inBounds(point.x, point.y)) {
                    getTileAt(point.x, point.y).visible = true;
                    toPaint[point.x][point.y] = PlayerBoardTransformer.WATER_VISIBLE_STRING;
                }
            }
        }

        for (ShipPiece piece : s.getPieces()) {
            toPaint[piece.point.x][piece.point.y] = PlayerBoardTransformer.PIECE_ATTACKED_SHIP_DESTROYED_STRING;
        }
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < LINES; i++) {
            for (int c = 0; c < COLUMNS; c++) {
                s += boardTiles[i][c] + "\n";
            }
            s += "\n";
        }
        return s;
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

    public boolean placeShip(Ship toAdd) {
        if (canShipBeHere(toAdd)) {
            ships.add(toAdd);
            for (ShipPiece piece : toAdd.getPieces()) {
                //System.out.println("PLACING " + piece.getClass().getSimpleName() + " AT: " + piece.x + " " + piece.y);
                boardTiles[piece.point.x][piece.point.y] = piece;
                pieces.add(piece);
                toPaint[piece.point.x][piece.point.y] = piece.toSendString();
            }
            return true;
        }
        return false;
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
        for (ShipPiece piece : toAdd.getPieces()) {
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

    private boolean checkSurroundings(int x, int y) {
        return checkSurroundings(new Point(x, y));
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
        for (ShipPiece piece : ship.getPieces()) {
            //System.out.println("REMOVING " + piece.getClass().getSimpleName() + " AT: " + piece.x + " " + piece.y);
            boardTiles[piece.point.x][piece.point.y] = new WaterTile(piece.point.x, piece.point.y);
            pieces.remove(piece);
        }
    }

    public boolean lastShipDestroyed() {
        return lastShipDestroyed;
    }

    public boolean actualNewHit() {
        return actualHit;
    }

    public boolean fullOfShips() {
        System.out.println(pieces.size());
        return pieces.size() == 20;
    }

    public boolean isShipHit() {
        return shipHit;
    }
}

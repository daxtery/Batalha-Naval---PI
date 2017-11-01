package Common;

import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class PlayerBoard implements Serializable {

    final static int NUMBER_OF_BOATS = 10;
    public static final int LINES = 10;
    public static final int COLUMNS = 10;
    private boolean gameOver;
    private Ship[] ships = new Ship[10];
    private ArrayList<ShipPiece> pieces;
    private BoardTile[][] boardTiles;
    private int[][] toSend;
    private int shipN = 0;

    public boolean gotAPieceAttacked;

    public int[][] getToSend(){
        lightItUp();
        /*
        System.out.println(this);
        for (int l = 0; l < LINES; l++) {
            System.out.println(Arrays.toString(toSend[l]));
        }
        */
        return toSend;
    }

    private boolean isPiece(int[][] check, int x, int y){
        return check[x][y] > 0;
    }

    public void transformBack(int[][] sent){
        ArrayList<Point> toSkip = new ArrayList<>(LINES * COLUMNS);

        for (int l = 0; l < LINES; l++) {
            System.out.println(Arrays.toString(sent[l]));
        }

        int count = 0;

        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                if (toSkip.contains(new Point(l, c))){
                    //already found it
                    continue;
                }
                //IT'S A PIECE
                if(isPiece(sent, l, c)){
                    //look in directions
                    int x = l;
                    int y = c + 1;
                    int size = 1;
                    boolean foundHorizontal = false;

                    //HORIZONTAL

                    while(inBounds(x, y)){
                        if(isPiece(sent, x, y)){
                            size++;
                            foundHorizontal = true;
                            toSkip.add(new Point(x, y));
                            y++;
                        }
                        else{
                            break;
                        }
                    }

                    //VERTICAL

                    x = l + 1;
                    y = c;

                    if(!foundHorizontal){
                        while(inBounds(x, y)){
                            if(isPiece(sent, x, y)){
                                size++;
                                toSkip.add(new Point(x, y));
                                x++;
                            }
                            else{
                                break;
                            }
                        }
                    }
                    Ship.ShipType st = Ship.ShipType.getShipType(size);
                    Direction d = Direction.DOWN;
                    if(foundHorizontal){
                        d = Direction.RIGHT;
                    }
                    Ship tempShip = new Ship(l, c, d, st);
                    tempShip.getPieces();
                    System.out.println(tempShip);
                    placeShip(tempShip);
                    count += size;
                }

            }
        }
        forTests();
    }

    public PlayerBoard() {
        toSend = new int[LINES][COLUMNS];
        gameOver = false;
        boardTiles = new BoardTile[LINES][COLUMNS];
        fillWithWater();
        ships = new Ship[NUMBER_OF_BOATS];
        pieces = new ArrayList<>();
    }

    public PlayerBoard(int[][] sent){
        this();
        transformBack(sent);
    }

    void forTests(){
        int i = 0;
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                if(getTileAt(l,c).isPiece()){
                    i++;
                }
            }
        }
        if(i!=20){
           System.out.println("HAS " + i + "PIECES, NOT 20");
        }
    }

    private void fillWithWater() {
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                boardTiles[l][c] = new WaterTile(l, c);
                toSend[l][c] = 0;
            }
        }
    }

    //region attacked

    public void getAttacked(int x, int y) {
        //NOT ATTACKED YET
        gotAPieceAttacked = true;
        BoardTile boardTile = getTileAt(x, y);
        if (!boardTile.isVisible) {
            boardTile.setAttacked();
            // NOT A SHIP PIECE
            if (!boardTile.isPiece()) {
                gotAPieceAttacked = false;
                return;
            }
            pieces.remove((ShipPiece) boardTile);
            Ship ship = ((ShipPiece) boardTile)._ship;
            if (ship.isDestroyed()) {
                System.out.println("SHIP DESTROYED");
                shipDestroyed(ship);
            }
        }
        checkGameOver();
    }

    public void getAttacked(Point point){
        getAttacked(point.x, point.y);
    }

    void getAttacked(BoardTile boardTile){
        getAttacked(boardTile._x, boardTile._y);
    }

    public void lightsOut(){
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                if(!getTileAt(l,c).attacked){
                    getTileAt(l,c).isVisible = false;
                }
            }
        }
    }

    //endregion

    public boolean isGameOver(){
        return gameOver;
    }

    private void checkGameOver() {
        gameOver = pieces.isEmpty();
        if(gameOver){
            lightItUp();
        }
    }

    private void shipDestroyed(Ship s) {
        for (ShipPiece piece : s.getPieces()) {
            Point[] points = getSurroundingPoints(piece._x, piece._y);
            for (Point point : points) {
                if (inBounds(point.x, point.y)) {
                    getTileAt(point.x, point.y).setAttacked();
                }
            }
        }
    }

    void nukeIt(){
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                boardTiles[l][c].setAttacked();
            }
        }
    }

    public void lightItUp() {
        for (int l = 0; l < LINES; l++) {
            for (int c = 0; c < COLUMNS; c++) {
                boardTiles[l][c].isVisible = true;
            }
        }
    }

    @Override
    public String toString() {
        String s = "    ";
        for (int i = 0; i < COLUMNS; i++) {
            s += i + 1 + "    ";
        }
        s += "\n";
        for (int i = 0; i < LINES; i++) {
            s += (char) (i + 65);
            for (int c = 0; c < COLUMNS; c++) {
                s += "    " + boardTiles[i][c].toString();
            }
            s += "\n";
        }
        return s;
    }

    public void placeShips(Ship[] toAdd){
        int i = 0;
        for (Ship ship : toAdd) {
            ships[i] = ship;
            i++;
            placeShip(ship);
        }
    }

    public void placeShip(Ship toAdd) {
        shipN++;
        for (ShipPiece piece : toAdd.getPieces()) {
            System.out.println("PLACING " + piece.getClass().getSimpleName() + " AT: " + piece._x + " " + piece._y);
            boardTiles[piece._x][piece._y] = piece;
            pieces.add(piece);
            toSend[piece._x][piece._y] = shipN;
        }
    }

    //region CanPlaceShip

    private Point[] getSurroundingPoints(int x, int y) {
        Point[] points = new Point[8];
        points[0] = new Point(x + 1, y);
        points[1] = new Point(x + 1, y + 1);
        points[2] = new Point(x + 1, y - 1);
        points[3] = new Point(x - 1, y);
        points[4] = new Point(x - 1, y + 1);
        points[5] = new Point(x - 1, y - 1);
        points[6] = new Point(x, y + 1);
        points[7] = new Point(x, y - 1);
        return points;
    }

    public boolean canShipBeHere(Ship toAdd) {
        for (ShipPiece piece : toAdd.getPieces()) {
            //System.out.println(piece.toString());
            boolean isInBounds = inBounds(piece._x, piece._y);
            if (!isInBounds) {
                //System.out.println("NO BOUNDS");
                return false;
            }
            boolean isNotAdjacent = checkSurroundings(piece._x, piece._y);
            if (!isNotAdjacent || !freeAt(piece._x, piece._y)) {
                //System.out.println("ADJACENT");
                return false;
            }
        }
        return true;
    }

    private boolean checkSurroundings(int x, int y) {
        Point[] points = getSurroundingPoints(x, y);
        for (Point point : points) {
            if (inBounds(point.x, point.y)) {
                if (!freeAt(point.x, point.y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean freeAt(int x, int y) {
        return !getTileAt(x, y).isPiece();
    }

    //endregion

    public BoardTile getTileAt(int x, int y) {
        return boardTiles[x][y];
    }

    @Contract(pure = true)
    private boolean inBounds(int x, int y) {
        return (x < LINES && x >= 0) && (y < COLUMNS && y >= 0);
    }


    public void removeShip(Ship ship) {

        for (ShipPiece piece : ship.getPieces()) {
            System.out.println("REMOVING " + piece.getClass().getSimpleName() + " AT: " + piece._x + " " + piece._y);
            boardTiles[piece._x][piece._y] = new WaterTile(piece._x, piece._y);
            pieces.remove(piece);
            toSend[piece._x][piece._y] = 0;
        }

    }

    public boolean fullOfShips(){
        System.out.println(pieces.size());
        return pieces.size() == 20;
    }
}

package Common;

import util.Point;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class PlayerBoardTransformer {

    public static final String PIECE_NOT_ATTACKED_STRING = "P";
    public static final String PIECE_ATTACKED_STRING = "PA";
    public static final String PIECE_ATTACKED_SHIP_DESTROYED_STRING = "PD";

    public static final String WATER_NOT_VISIBLE_STRING = "W";
    public static final String WATER_VISIBLE_STRING = "WV";

    public static String[][] transform(PlayerBoard playerBoard) {

        String[][] message = new String[playerBoard.lines()][playerBoard.columns()];
        BoardTile[][] boardTiles = playerBoard.boardTiles;

        for (int i = 0, nLines = boardTiles.length; i < nLines; i++) {
            BoardTile[] line = boardTiles[i];
            for (int j = 0, nColumns = line.length; j < nColumns; j++) {
                BoardTile boardTile = line[j];
                message[i][j] = getStringToSend(boardTile);
            }
        }

        return message;
    }

    private static String getStringToSend(BoardTile boardTile) {
        return switch (boardTile.tileType) {

            case Water -> {
                WaterTile waterTile = (WaterTile) boardTile;
                yield switch (waterTile.status()) {
                    case NotVisible -> WATER_NOT_VISIBLE_STRING;
                    case Visible -> WATER_VISIBLE_STRING;
                };
            }

            case ShipPiece -> {
                ShipPiece shipPiece = (ShipPiece) boardTile;
                yield switch (shipPiece.status()) {
                    case Attacked -> PIECE_ATTACKED_STRING;
                    case AttackedShipDestroyed -> PIECE_ATTACKED_SHIP_DESTROYED_STRING;
                    case NotAttacked -> PIECE_NOT_ATTACKED_STRING;
                };
            }
        };
    }

    private static boolean theresAPieceAt(String[][] message, Point point) {
        return isAPiece(message[point.x][point.y]);
    }

    private static boolean isAPiece(String encoded) {
        return encoded.equalsIgnoreCase(PIECE_ATTACKED_STRING) ||
                encoded.equalsIgnoreCase(PIECE_NOT_ATTACKED_STRING) ||
                encoded.equalsIgnoreCase(PIECE_ATTACKED_SHIP_DESTROYED_STRING);
    }

    public static PlayerBoard parse(String[][] message) {
        HashSet<Point> toSkip = new HashSet<>();
        final int lines = message.length;
        final int columns = message[0].length;

        PlayerBoard board = new PlayerBoard(message.length, message[0].length);

        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {

                Point point = new Point(l, c);

                if (toSkip.contains(point)) {
                    //already found it
                    continue;
                }

                String encoded = message[l][c];

                if (isAPiece(encoded)) {
                    Ship ship = findAllPiecesAndBuildShip(board, toSkip, message, l, c);
                    board.placeShip(ship);
                } else if (encoded.equals(WATER_VISIBLE_STRING)) {
                    board.getAttacked(point);
                }
            }
        }

        return board;
    }

    private static boolean isWithinBounds(PlayerBoard board, Point point) {
        return board.inBounds(point);
    }

    private static Ship findAllPiecesAndBuildShip(PlayerBoard board, HashSet<Point> toSkip, String[][] message, int l, int c) {
        Point origin = new Point(l, c);
        Direction finalDirection = Direction.Up;

        for (Direction direction : Direction.values()) {
            Point directionVector = direction.vector;
            Point next = origin.moved(directionVector);

            if (isWithinBounds(board, next) && theresAPieceAt(message, next)) {
                finalDirection = direction;
                break;
            }
        }

        List<ShipPiece> pieces = new LinkedList<>();
        Point next = origin;
        Point directionVector = finalDirection.vector;

        for (int i = 0; isWithinBounds(board, next) && theresAPieceAt(message, next); next = next.moved(directionVector), i++) {
            boolean visible = !message[next.x][next.y].equalsIgnoreCase(PIECE_NOT_ATTACKED_STRING);
            pieces.add(new ShipPiece(i, next, visible));

            if (i > 0) toSkip.add(next);
        }

        ShipPiece[] piecesArray = pieces.toArray(new ShipPiece[0]);

        Ship ship = new Ship(piecesArray, finalDirection, Ship.ShipType.getShipType(pieces.size()));

        pieces.forEach(p -> p.ship = ship);
        return ship;
    }

}

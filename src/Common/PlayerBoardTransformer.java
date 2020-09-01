package Common;

public class PlayerBoardTransformer {

    public static final String PIECE_NOT_ATTACKED_STRING = "P";
    public static final String PIECE_ATTACKED_STRING = "PA";
    public static final String PIECE_ATTACKED_SHIP_DESTROYED_STRING = "PD";

    public static final String WATER_NOT_VISIBLE_STRING = "W";
    public static final String WATER_VISIBLE_STRING = "WV";

    public static String[][] transformForOthers(PlayerBoard playerBoard) {

        String[][] message = new String[PlayerBoard.LINES][PlayerBoard.COLUMNS];
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

    public static void transformForSelf(PlayerBoard playerBoard) {
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

//    private PlayerBoard fromEncoded(String[][] message) {
//    }

}

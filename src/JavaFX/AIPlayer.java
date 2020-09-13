package JavaFX;

import AI.MyAI;
import Common.AttackResult;
import Common.PlayerBoard;
import Common.PlayerBoardFactory;
import Common.PlayerBoardTransformer;
import JavaFX.FX.GraphBoardFX;
import JavaFX.FX.TileFX;
import util.Point;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;

public class AIPlayer {

    GraphBoardFX b;
    PlayerBoard board;
    MyAI brain;

    AIPlayer() {
        brain = new MyAI();
        board = PlayerBoardFactory.getRandomPlayerBoard();
        b = new GraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_LINES);
        b.startTiles(PlayerBoardTransformer.transform(board));
    }

    public AttackResult getAttacked(Point p) {
        AttackResult result = board.getAttacked(p);
        b.updateTiles(PlayerBoardTransformer.transform(board));
        return result;
    }

}

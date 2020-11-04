package Server;

import Common.PlayerBoardMessage;

public interface IBoardSetup {

    void transitionToShips();
    void setGameBoardOfPlayer(int gameID, PlayerBoardMessage playerBoardMessage);
    boolean allBoardsSet();

}

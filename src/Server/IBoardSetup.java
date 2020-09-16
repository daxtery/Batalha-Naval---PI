package Server;

public interface IBoardSetup {

    void transitionToShips();
    void setGameBoardOfPlayer(int gameID, String[][] board);
    boolean allBoardsSet();

}

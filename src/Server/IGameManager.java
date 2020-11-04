package Server;

public interface IGameManager {

    void transitionToGame();
    boolean attack(int id, int x, int y);
    boolean gameIsOver();
    boolean isGameOverFor(int id);
    int playersAlive();

}

package Server;

import Common.AttackResult;

public interface IGameManager {

    void transitionToGame();
    AttackResult attack(int id, int x, int y);
    boolean gameIsOver();
    boolean isGameOverFor(int id);
    int playersAlive();

}

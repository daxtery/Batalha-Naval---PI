package Server;

import Common.AttackResult;
import Common.PlayerBoard;
import Common.PlayerBoardTransformer;

import java.util.*;

public class Game {

    public final PlayerBoard[] playerBoards;

    Game(int playerCount) {
        playerBoards = new PlayerBoard[playerCount];
    }

    public void setBoard(int slot, String[][] board) {
        playerBoards[slot] = PlayerBoardTransformer.parse(board);
    }

    public boolean allBoardsSet() {
        return Arrays.stream(playerBoards).noneMatch(Objects::isNull);
    }

    AttackResult attack(int id, int x, int y) {
        return playerBoards[id].getAttacked(x, y);
    }

    void removePlayer(int id) {

    }

    boolean gameIsOver() {
        int i = 0;
        for (PlayerBoard pb : playerBoards) {
            if (pb.isGameOver()) {
                i++;
            }
            if (i == 2) {
                return true;
            }
        }
        return false;
    }

    boolean isGameOverFor(int id) {
        return playerBoards[id].isGameOver();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < playerBoards.length; i++) {
            s.append("Player ")
                    .append(i + 1)
                    .append(": \n");

            s.append(playerBoards[i].toString());
        }
        return s.toString();
    }

}

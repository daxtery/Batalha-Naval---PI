package Server;

import Common.AttackResult;
import Common.PlayerBoard;

public class Game {

    private PlayerBoard[] playerBoards;
    private int idLastAttacked = 0;

    Game(){
        playerBoards = new PlayerBoard[3];
    }

    boolean canStart(){
        for (PlayerBoard pb: playerBoards ) {
            if(pb == null){
                return false;
            }
        }
        return true;
    }

    boolean attack(int id, int x, int y){
        idLastAttacked = id;
        return playerBoards[id].getAttacked(x, y) == AttackResult.HitShipPiece;
    }

    void removePlayer(int id){

    }

    boolean gameIsOver() {
        int i = 0;
        for (PlayerBoard pb: playerBoards ){
            if(pb.isGameOver()){
                i++;
            }
            if(i == 2){
                return true;
            }
        }
        return false;
    }

    boolean isGameOverFor(int id) {
        return (playerBoards[id].isGameOver());
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < playerBoards.length; i++){
            s.append("Player ")
                    .append(i + 1)
                    .append(": \n");

            s.append(playerBoards[i].toString());
        }
        return s.toString();
    }

    void setPlayerBoard(PlayerBoard playerBoard, int i) {
        playerBoards[i] = playerBoard;
    }

    PlayerBoard getPlayerBoard(int i){
        return playerBoards[i];
    }

    public boolean lastAttackShip() {
        return playerBoards[idLastAttacked].isShipHit();
    }

    public boolean lastActualHit(){
        return playerBoards[idLastAttacked].actualNewHit();
    }
}

package Server;

import Common.AttackResult;
import Common.PlayerBoard;
import com.esotericsoftware.kryonet.Connection;

import java.util.*;

public class Lobby {

    private final Map<Integer, PlayerBoard> playerBoardMap;
    private final Map<Integer, Connection> players;

    private final int playerCount;

    Lobby(int playerCount) {
        playerBoardMap = new HashMap<>(playerCount);
        players = new HashMap<>(playerCount);
        this.playerCount = playerCount;
    }

    public void addNewPlayer(Connection connection, int gameID) {
        players.put(gameID, connection);
        playerBoardMap.put(gameID, null);
    }

    public Connection getConnectionById(int id) {
        return players.get(id);
    }

    public boolean allBoardsSet() {
        return full() && playerBoardMap.values().stream().allMatch(Objects::nonNull);
    }

    public int count() {
        return playerBoardMap.size();
    }

    public boolean full() {
        return count() >= this.playerCount;
    }

    public Connection firstConnection() {
        return players.get(0);
    }

    AttackResult attack(int id, int x, int y) {
        return playerBoardMap.get(id).getAttacked(x, y);
    }

    void removePlayer(int id) {

    }

    boolean gameIsOver() {
        int i = 0;
        for (PlayerBoard pb : playerBoardMap.values()) {
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
        return playerBoardMap.get(id).isGameOver();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < playerBoardMap.size(); i++) {
            s.append("Player ")
                    .append(i + 1)
                    .append(": \n");

            s.append(playerBoardMap.get(i).toString());
        }
        return s.toString();
    }

    String[] getNamesOfConnected() {
        return players.values().stream().map(Connection::toString).toArray(String[]::new);
    }

    void setPlayerBoard(PlayerBoard playerBoard, int i) {
        playerBoardMap.put(i, playerBoard);
    }

    PlayerBoard getPlayerBoard(int i) {
        return playerBoardMap.get(i);
    }

    public Collection<Connection> getConnections() {
        return players.values();
    }

    public Set<Map.Entry<Integer, Connection>> getConnectAndIds() {
        return players.entrySet();
    }

    public int getGameIDByConnection(Connection connection) {
        return players.entrySet().stream()
                .filter(entry -> entry.getValue() == connection)
                .findFirst()
                .get()
                .getKey();
    }
}

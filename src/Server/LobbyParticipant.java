package Server;

import Common.PlayerBoard;
import com.esotericsoftware.kryonet.Connection;

public abstract class LobbyParticipant {

    public final String name;
    public final Connection connection;
    private boolean defeated;
    private PlayerBoard playerBoard;

    public LobbyParticipant(String name, Connection connection) {
        this.name = name;
        this.connection = connection;
        this.defeated = false;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public PlayerBoard getPlayerBoard() {
        return playerBoard;
    }

    public void setPlayerBoard(PlayerBoard playerBoard) {
        this.playerBoard = playerBoard;
    }

    protected abstract boolean isBot();

}

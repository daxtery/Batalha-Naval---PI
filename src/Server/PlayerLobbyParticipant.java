package Server;

import com.esotericsoftware.kryonet.Connection;

public class PlayerLobbyParticipant extends LobbyParticipant {

    public PlayerLobbyParticipant(Connection connection, String name) {
        super(name, connection);
    }

    @Override
    protected boolean isBot() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}

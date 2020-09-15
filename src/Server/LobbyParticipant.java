package Server;

import com.esotericsoftware.kryonet.Connection;

public abstract class LobbyParticipant {

    public final String name;
    public final Connection connection;

    public LobbyParticipant(String name, Connection connection) {
        this.name = name;
        this.connection = connection;
    }

    protected abstract boolean isBot();

}

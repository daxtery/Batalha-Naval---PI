package Server;

import Common.*;
import com.esotericsoftware.kryonet.Connection;

public class GameLobby extends Lobby {

    public GameLobby(int count) {
        super(5, count);
    }

    public void addPlayer(int slot, Connection connection) {
        this.participants[slot] = new PlayerLobbyParticipant(connection, connection.toString());
    }

    public void addBot(int slot, String name, BotDifficulty difficulty, Connection connection) {
        this.participants[slot] = new BotLobbyParticipant(difficulty, name, connection);
    }

    public void removeParticipant(int slot) {
        this.participants[slot] = null;
    }

    public int getSlotOf(Connection connection) {

        for (int i = 0, participantsLength = participants.length; i < participantsLength; i++) {
            LobbyParticipant participant = participants[i];

            if (participant.connection == connection) {
                return i;
            }
        }

        return 0;
    }

}

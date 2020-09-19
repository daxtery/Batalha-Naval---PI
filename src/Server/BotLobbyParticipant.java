package Server;

import Common.BotPersonality;
import com.esotericsoftware.kryonet.Connection;

public class BotLobbyParticipant extends LobbyParticipant {

    public final BotPersonality difficulty;

    public BotLobbyParticipant(BotPersonality difficulty, String name, Connection connection) {
        super(name, connection);
        this.difficulty = difficulty;
    }

    @Override
    protected boolean isBot() {
        return true;
    }

    @Override
    public String toString() {
        return name + " (Bot: " + difficulty + ")";
    }
}

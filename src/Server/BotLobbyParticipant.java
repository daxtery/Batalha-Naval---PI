package Server;

import Common.BotDifficulty;
import com.esotericsoftware.kryonet.Connection;

public class BotLobbyParticipant extends LobbyParticipant {

    public final BotDifficulty difficulty;

    public BotLobbyParticipant(BotDifficulty difficulty, String name, Connection connection) {
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

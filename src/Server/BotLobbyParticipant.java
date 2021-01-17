package Server;

import Common.BotPersonality;
import com.esotericsoftware.kryonet.Connection;

public class BotLobbyParticipant extends LobbyParticipant {

    private final BotPersonality difficulty;

    public BotLobbyParticipant(BotPersonality difficulty, Player player) {
        super(player);
        this.difficulty = difficulty;
    }

    public BotPersonality getDifficulty() {
        return difficulty;
    }
}

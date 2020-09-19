package Server;

import Common.BotPersonality;
import com.esotericsoftware.kryonet.Connection;

import java.util.Optional;

public interface ILobby {

    int playersInLobby();
    int slots();
    boolean full();
    void addPlayer(int slot, Connection connection);
    void addBot(int slot, String name, BotPersonality difficulty, Connection connection);
    void removeParticipant(int slot);
    Optional<Integer> getSlotOf(Connection connection);
    void transitionToLobby();
}

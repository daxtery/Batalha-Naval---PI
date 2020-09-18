package Server;

import Common.BotDifficulty;
import com.esotericsoftware.kryonet.Connection;

import java.util.Optional;

public interface ILobby {

    int playersInLobby();
    int slots();
    boolean full();
    void addPlayer(int slot, Connection connection);
    void addBot(int slot, String name, BotDifficulty difficulty, Connection connection);
    void removeParticipant(int slot);
    Optional<Integer> getSlotOf(Connection connection);
    void transitionToLobby();
}

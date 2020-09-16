package Common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

// This class is a convenient place to keep things common to both the client and server.
public class Network {

    public static final int port = 5656;

    // This registers objects that are going to be sent over the network.
    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(JoinLobby.class);
        kryo.register(CreateLobby.class);
        kryo.register(Participant.class);
        kryo.register(Participant[].class);
        kryo.register(BotDifficulty.class);
        kryo.register(ChatMessage.class);
        kryo.register(ChatMessageFromClient.class);
        kryo.register(AddBotToLobby.class);
        kryo.register(RemovePlayerFromLobby.class);
        kryo.register(StartLobby.class);
        kryo.register(JoinLobbyResponse.class);
        kryo.register(AttackResult.class);
        kryo.register(AttackResultStatus.class);
        //
        kryo.register(int[][].class);
        kryo.register(int[].class);
        kryo.register(int.class);
        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(String[][].class);
        kryo.register(String[][][].class);
        //
        kryo.register(IsFull.class);
        kryo.register(ReadyForShips.class);
        kryo.register(Abort.class);
        kryo.register(CanStart.class);
        kryo.register(WhoseTurn.class);
        kryo.register(ConnectedPlayers.class);
        kryo.register(YourBoardToPaint.class);
        kryo.register(AnAttackAttempt.class);
        kryo.register(AnAttackResponse.class);
        kryo.register(EnemyBoardToPaint.class);
        kryo.register(YourTurn.class);
        kryo.register(YouDead.class);
        kryo.register(PlayerDied.class);
        kryo.register(YouWon.class);
        kryo.register(PlayerBoard.class);
        kryo.register(APlayerboard.class);
    }

    public static class YouWon {
    }

    public static class YouDead {
    }

    public static class PlayerDied {
        public int who;
    }

    public static class APlayerboard {
        public String[][] board;
    }

    public static class AnAttackAttempt {
        public int toAttackID;
        public int l;
        public int c;
    }

    public static class EnemyBoardToPaint {
        public String[][] newAttackedBoard;
        public int id;
    }

    public static class AnAttackResponse {
        public String[][] newAttackedBoard;
        public AttackResult attackResult;
    }

    public static class YourBoardToPaint {
        public String[][] board;
    }

    public static class WhoseTurn {
        public String name;
    }

    public static class YourTurn {
    }

    public static class Abort {
    }

    public static class ReadyForShips {
    }

    public static class ChatMessage {
        public String message;
        public int saidIt;
    }

    public static class ChatMessageFromClient {
        public String text;
        public int to;
    }

    public static class Participant {
        public BotDifficulty botDifficulty;
        public String name;
        public int slot;

        public Participant(BotDifficulty botDifficulty, String name, int slot) {
            this.botDifficulty = botDifficulty;
            this.name = name;
            this.slot = slot;
        }

        public Participant(String name, int slot) {
            this.name = name;
            this.slot = slot;
        }

        public Participant() {
        }

        @Override
        public String toString() {

            if (botDifficulty == null) {
                return name + " #" + slot;
            }

            return name + "(Bot: " + botDifficulty + ")" + " #" + slot;
        }
    }

    public static class ConnectedPlayers {
        public Participant[] participants;
        public int slot;
    }

    public static class CanStart {
        public String[][][] boards;
        public int[] indices;
    }

    public static class JoinLobby {
        public String name;
    }

    public static class JoinLobbyResponse {
        public int slots;
    }

    public static class CreateLobby {
        public String name;
        public int count;

        public CreateLobby() {
        }

        public CreateLobby(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    public static class StartLobby {
    }

    public static class AddBotToLobby {
        public int slot;
        public BotDifficulty botDifficulty;
        public String name;

        public AddBotToLobby(int slot, BotDifficulty botDifficulty, String name) {
            this.slot = slot;
            this.botDifficulty = botDifficulty;
            this.name = name;
        }

        public AddBotToLobby() {
        }
    }

    public static class RemovePlayerFromLobby {
        public int slot;

        public RemovePlayerFromLobby(int slot) {
            this.slot = slot;
        }

        public RemovePlayerFromLobby() {
        }
    }

    public static class IsFull {
        @Override
        public String toString() {
            return "Server is full";
        }
    }
}

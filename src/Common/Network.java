package Common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import util.Point;

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
        kryo.register(BotPersonality.class);
        kryo.register(ChatMessageResponse.class);
        kryo.register(ChatMessage.class);
        kryo.register(AddBotToLobby.class);
        kryo.register(RemovePlayerFromLobby.class);
        kryo.register(StartGame.class);
        kryo.register(PlayerCommitBoard.class);
        //
        kryo.register(int.class);
        kryo.register(int[].class);
        kryo.register(int[][].class);

        kryo.register(Point.class);
        kryo.register(Point[].class);
        kryo.register(Ship.class);
        kryo.register(Ship[].class);

        kryo.register(HitResult.class);

        kryo.register(Direction.class);
        kryo.register(boolean.class);
        kryo.register(boolean[].class);
        kryo.register(boolean[][].class);
        kryo.register(Boolean.class);
        kryo.register(Boolean[].class);
        kryo.register(Boolean[][].class);

        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(PlayerBoardMessage.class);
        kryo.register(PlayerBoardMessage[].class);
        //
        kryo.register(ServerIsFullResponse.class);
        kryo.register(PlayersSetBoardResponse.class);
        kryo.register(ReadyForShipsResponse.class);
        kryo.register(AbortResponse.class);
        kryo.register(StartGameResponse.class);
        kryo.register(WhoseTurnResponse.class);
        kryo.register(ConnectedPlayersResponse.class);
        kryo.register(YourBoardResponse.class);
        kryo.register(AnAttack.class);
        kryo.register(AnAttackResponse.class);
        kryo.register(EnemyBoardResponse.class);
        kryo.register(YourTurnResponse.class);
        kryo.register(YouDeadResponse.class);
        kryo.register(PlayerDiedResponse.class);
        kryo.register(YouWonResponse.class);
        kryo.register(PlayerBoard.class);
    }

    public static class YouWonResponse {
    }

    public static class YouDeadResponse {
    }

    public static class PlayerDiedResponse {
        public int slot;

        public PlayerDiedResponse(int slot) {
            this.slot = slot;
        }

        public PlayerDiedResponse() {
        }
    }

    public static class AnAttack {
        public int toAttackID;
        public Point at;
    }

    public static class EnemyBoardResponse {
        public PlayerBoardMessage newAttackedBoard;
        public int id;
    }

    public static class AnAttackResponse {
        public PlayerBoardMessage newAttackedBoard;
        public HitResult hitResult;
        public int attacked;
        public Point point;

        public AnAttackResponse(PlayerBoardMessage newAttackedBoard, HitResult hitResult, int attacked, Point point) {
            this.newAttackedBoard = newAttackedBoard;
            this.hitResult = hitResult;
            this.attacked = attacked;
            this.point = point;
        }

        public AnAttackResponse() {
        }
    }

    public static class YourBoardResponse {
        public PlayerBoardMessage board;
    }

    public static class WhoseTurnResponse {
        public int slot;

        public WhoseTurnResponse(int slot) {
            this.slot = slot;
        }

        public WhoseTurnResponse() {
        }
    }

    public static class YourTurnResponse {
    }

    public static class AbortResponse {
    }

    public static class ReadyForShipsResponse {
    }

    public static class ChatMessageResponse {
        public String message;
        public int saidIt;
    }

    public static class ChatMessage {
        public String text;
        public int to;
    }

    public static class Participant {
        public BotPersonality BotPersonality;
        public String name;
        public int slot;

        public Participant(BotPersonality BotPersonality, String name, int slot) {
            this.BotPersonality = BotPersonality;
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

            if (BotPersonality == null) {
                return name + " #" + slot;
            }

            return name + "(Bot: " + BotPersonality + ")" + " #" + slot;
        }
    }

    public static class ConnectedPlayersResponse {
        public Participant[] participants;
        public int slot;
    }

    public static class PlayersSetBoardResponse {
        public Participant[] participants;
        public boolean[] boardSet;
    }

    public static class StartGameResponse {
        public PlayerBoardMessage[] boards;
        public int[] indices;
    }

    public static class JoinLobby {
        public String name;
    }

    public static class JoinLobbyResponse {
        public int slots;

        public JoinLobbyResponse(int slots) {
            this.slots = slots;
        }

        public JoinLobbyResponse() {
        }
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

    public static class StartGame {
    }

    public static class PlayerCommitBoard {
        public PlayerBoardMessage playerBoardMessage;

        public PlayerCommitBoard(PlayerBoardMessage playerBoardMessage) {
            this.playerBoardMessage = playerBoardMessage;
        }

        public PlayerCommitBoard() {
        }
    }

    public static class BoardRequest {
    }

    public static class AddBotToLobby {
        public int slot;
        public BotPersonality BotPersonality;
        public String name;

        public AddBotToLobby(int slot, BotPersonality BotPersonality, String name) {
            this.slot = slot;
            this.BotPersonality = BotPersonality;
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

    public static class ServerIsFullResponse {
        @Override
        public String toString() {
            return "Server is isFull";
        }
    }
}

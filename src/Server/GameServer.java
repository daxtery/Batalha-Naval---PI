package Server;

import Common.Conversations;
import Common.Network;
import Common.Network.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Optional;

public class GameServer {

    private final static long TIME_TO_WAIT = 1000 * 60;
    public final Server server;
    public GameLobby lobby;
    //WILL SAVE WHAT CONNECTIONS THE GAME STARTED WITH
    //SO IT'S POSSIBLE TO KNOW IF SOMEBODY WHO DROPPED IS RECONNECTING
    private long currentWaitedTime;

    public GameServer(int port) throws IOException {
        server = new Server();
        Network.register(server);

        server.addListener(new Listener() {

            public void received(Connection connection, Object object) {

                System.out.println("Received message from " + connection + " ~ " + object + " ~ ");

                if (object instanceof CreateLobby) {
                    onCreateLobby(connection, (CreateLobby) object);
                } else if (object instanceof AddBotToLobby) {
                    onAddBotToLobby(connection, (AddBotToLobby) object);
                } else if (object instanceof RemovePlayerFromLobby) {
                    onRemovePlayerFromLobby((RemovePlayerFromLobby) object);
                } else if (object instanceof JoinLobby) {
                    onJoinLobby(connection, (JoinLobby) object);
                } else if (object instanceof StartGame) {
                    onStartLobby();
                } else if (object instanceof PlayerCommitBoard) {
                    onPlayerCommitBoard(connection, (PlayerCommitBoard) object);
                } else if (object instanceof BoardRequest) {
                    onBoardRequest(connection, (BoardRequest) object);
                } else if (object instanceof AnAttack) {
                    onAnAttack(connection, (AnAttack) object);
                } else if (object instanceof ChatMessage) {
                    onChatMessageFromClient(connection, (ChatMessage) object);
                }
            }

            public void disconnected(Connection c) {
                onDisconnected(c);
            }

        });

        server.bind(port);
    }

    public static void main(String[] args) throws IOException {
        new GameServer(Network.port).start();
    }

    private void onPlayerCommitBoard(Connection connection, PlayerCommitBoard playerCommitBoard) {
        final int slot = lobby.getSlotOf(connection).orElseThrow();
        lobby.onPlayerCommitBoard(slot, playerCommitBoard);
    }

    private void onStartLobby() {
        lobby.onStartLobby();
    }

    private void onRemovePlayerFromLobby(RemovePlayerFromLobby removePlayerFromLobby) {
        lobby.removeParticipant(removePlayerFromLobby.slot);
        System.out.println("Removed player from lobby. Lobby: " + lobby);
    }

    private void onAddBotToLobby(Connection connection, AddBotToLobby addBotToLobby) {
        connection.setName(addBotToLobby.name);

        lobby.addBot(addBotToLobby.slot, addBotToLobby.name, addBotToLobby.BotPersonality, connection);
        System.out.println("Added bot. Lobby is: " + lobby);
    }

    private void onCreateLobby(Connection connection, CreateLobby createLobby) {
        connection.setName(createLobby.name);

        lobby = new GameLobby(this, createLobby.count, connection);
        System.out.println(connection + " created lobby: " + lobby);
    }

    private void onJoinLobby(Connection connection, JoinLobby joinLobby) {
        connection.setName(joinLobby.name);

        lobby.onJoinLobby(connection, joinLobby);
        System.out.println("Player tried to join. Lobby: " + lobby);
    }

    private void onDisconnected(Connection connection) {
        System.out.println("Disconnected " + connection);

        switch (lobby.getState()) {
            case InGame -> {
                // TODO
                System.err.println("Boy left during game :(");
                // TODO
                lobby.resetLobby();
            }
            case SettingShips -> {
                handleLeavingWhileShips(new Pair<>(connection, lobby.getSlotOf(connection).orElseThrow()));
            }
            case InLobby -> {
                Optional<Integer> result = lobby.getSlotOf(connection);
                result.ifPresent(slot -> {
                    lobby.removeParticipant(slot);
                    sendConnections();
                    System.out.println("Count : " + lobby.playersInLobby());
                    System.out.println("Lobby : " + lobby);
                });
            }
        }
    }

    private void onChatMessageFromClient(Connection connection, ChatMessage message) {
        lobby.onChatMessageFromClient(connection, message);
    }

    private void onGameOver() {
        lobby = null;
    }

    private void onBoardRequest(Connection connection, BoardRequest object) {
        System.err.println("IMPLEMENT ME");
    }

    private void onAnAttack(Connection connection, AnAttack anAttack) {
        lobby.onAnAttack(lobby.getSlotOf(connection).orElseThrow(), anAttack);
    }

    public void start() {
        server.start();
        System.out.println("Server started");
    }

    private void sendConnections() {

        ConnectedPlayersResponse connectedPlayersResponse = new ConnectedPlayersResponse();
        connectedPlayersResponse.participants = new Participant[lobby.participants.length];

        for (int i = 0; i < lobby.participants.length; ++i) {
            final LobbyParticipant participant = lobby.participants[i];

            if (participant == null) {
                connectedPlayersResponse.participants[i] = null;
                continue;
            }

            if (participant.isBot()) {
                BotLobbyParticipant asBot = (BotLobbyParticipant) participant;
                connectedPlayersResponse.participants[i] = new Participant(asBot.difficulty, asBot.name, i);
            } else {
                connectedPlayersResponse.participants[i] = new Participant(participant.name, i);
            }

        }

        for (int i = 0; i < lobby.participants.length; ++i) {
            if (lobby.participants[i] != null) {
                connectedPlayersResponse.slot = i;
                lobby.participants[i].connection.sendTCP(connectedPlayersResponse);
            }
        }

    }

    private void handleLeavingWhileShips(Pair<Connection, Integer> connectionAndId) {
    }

}

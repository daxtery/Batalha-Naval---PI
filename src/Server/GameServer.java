package Server;

import Common.Network;
import Common.Network.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameServer {

    public final Server server;
    public GameLobby lobby;
    public Map<String, Player> playerCodeToPlayer;
    public Map<Connection, Player> connectionToPlayer;

    public GameServer(int port) throws IOException {
        server = new Server();
        Network.register(server);

        playerCodeToPlayer = new HashMap<>();
        connectionToPlayer = new HashMap<>();

        server.addListener(new Listener() {

            public void received(Connection connection, Object object) {

                System.out.println("Received message from " + connection + " ~ " + object + " ~ ");
                if (object instanceof Register) {
                    onRegister(connection, (Register) object);
                } else {
                    Optional<Player> maybePlayer = Optional.ofNullable(
                            connectionToPlayer.getOrDefault(connection, null)
                    );

                    if (maybePlayer.isEmpty()){
                        System.err.println("Connection not registered... or something " + connection);
                        return;
                    }

                    Player player = maybePlayer.get();

                    if (object instanceof CreateLobby) {
                        onCreateLobby(player, (CreateLobby) object);
                    } else if (object instanceof AddBotToLobby) {
                        onAddBotToLobby(player, (AddBotToLobby) object);
                    } else if (object instanceof RemovePlayerFromLobby) {
                        onRemovePlayerFromLobby((RemovePlayerFromLobby) object);
                    } else if (object instanceof JoinLobby) {
                        onJoinLobby(player, (JoinLobby) object);
                    } else if (object instanceof StartGame) {
                        onStartLobby();
                    } else if (object instanceof PlayerCommitBoard) {
                        onPlayerCommitBoard(player, (PlayerCommitBoard) object);
                    } else if (object instanceof BoardRequest) {
                        onBoardRequest(player, (BoardRequest) object);
                    } else if (object instanceof AnAttack) {
                        onAnAttack(player, (AnAttack) object);
                    } else if (object instanceof ChatMessage) {
                        onChatMessageFromClient(player, (ChatMessage) object);
                    }
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

    private void onRegister(Connection connection, Register register) {
        final String code = register.code;
        connection.setName(register.name);

        Player player;

        if (playerCodeToPlayer.containsKey(code)) {
            player = playerCodeToPlayer.get(code);
            player.setConnection(connection);
        } else {
            player = new Player(code, register.isBot, connection);
            playerCodeToPlayer.put(code, player);
        }

        connectionToPlayer.put(connection, player);

        System.out.println(playerCodeToPlayer);
        System.out.println(connectionToPlayer);
    }

    private void onPlayerCommitBoard(Player player, PlayerCommitBoard playerCommitBoard) {
        final int slot = lobby.getSlotOf(player.getConnection()).orElseThrow();
        lobby.onPlayerCommitBoard(slot, playerCommitBoard);
    }

    private void onStartLobby() {
        lobby.onStartLobby();
    }

    private void onRemovePlayerFromLobby(RemovePlayerFromLobby removePlayerFromLobby) {
        lobby.removeParticipant(removePlayerFromLobby.slot);
        System.out.println("Removed player from lobby. Lobby: " + lobby);
    }

    private void onAddBotToLobby(Player player, AddBotToLobby addBotToLobby) {
        lobby.addBot(addBotToLobby.slot, player, addBotToLobby.BotPersonality);
        System.out.println("Added bot. Lobby is: " + lobby);
    }

    private void onCreateLobby(Player player, CreateLobby createLobby) {
        lobby = new GameLobby(this, createLobby.count, player);
        System.out.println(player + " created lobby: " + lobby);
    }

    private void onJoinLobby(Player player, JoinLobby joinLobby) {
        lobby.onJoinLobby(player, joinLobby);
        System.out.println("Player tried to join. Lobby: " + lobby);
    }

    private void onDisconnected(Connection connection) {
        System.out.println("Disconnected " + connection);

//        switch (lobby.getState()) {
//            case InGame -> {
//                // TODO
//                System.err.println("Boy left during game :(");
//                // TODO
//                lobby.resetLobby();
//            }
//            case SettingShips -> {
//                handleLeavingWhileShips(connection);
//            }
//            case InLobby -> {
//                Optional<Integer> result = lobby.getSlotOf(connection);
//                result.ifPresent(slot -> {
//                    lobby.removeParticipant(slot);
//                    sendConnections();
//                    System.out.println("Count : " + lobby.playersInLobby());
//                    System.out.println("Lobby : " + lobby);
//                });
//            }
//        }
    }

    private void onChatMessageFromClient(Player player, ChatMessage message) {
        lobby.onChatMessageFromClient(player.getConnection(), message);
    }

    private void onGameOver() {
        lobby = null;
    }

    private void onBoardRequest(Player player, BoardRequest object) {
        System.err.println("IMPLEMENT ME");
    }

    private void onAnAttack(Player player, AnAttack anAttack) {
        lobby.onAnAttack(lobby.getSlotOf(player.getConnection()).orElseThrow(), anAttack);
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

            if (participant.getPlayer().isBot()) {
                BotLobbyParticipant asBot = (BotLobbyParticipant) participant;
                connectedPlayersResponse.participants[i] = new Participant(
                        asBot.getDifficulty(),
                        asBot.getPlayer().getName(),
                        i
                );
            } else {
                connectedPlayersResponse.participants[i] = new Participant(participant.getPlayer().getName(), i);
            }

        }

        for (int i = 0; i < lobby.participants.length; ++i) {
            if (lobby.participants[i] != null) {
                connectedPlayersResponse.slot = i;
                lobby.participants[i].getPlayer().getConnection().sendTCP(connectedPlayersResponse);
            }
        }

    }

    private void handleLeavingWhileShips(Connection connection) {
        var maybeSlot = lobby.getSlotOf(connection);

        if (maybeSlot.isPresent()) {
            var slot = maybeSlot.get();
        } else {

        }
    }

}

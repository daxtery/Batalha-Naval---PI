package Server;

import Common.*;
import Common.Network.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.util.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GameServer {

    private final static long TIME_TO_WAIT = 1000 * 60;
    private final Server server;
    public GameLobby lobby;
    //WILL SAVE WHAT CONNECTIONS THE GAME STARTED WITH
    //SO IT'S POSSIBLE TO KNOW IF SOMEBODY WHO DROPPED IS RECONNECTING
    private long currentWaitedTime;
    private Conversations conversations;
    private int currentPlayer;

    public GameServer(int port) throws IOException {
        server = new Server();
        Network.register(server);

        server.addListener(new Listener() {

            public void received(Connection c, Object object) {

                if (object instanceof CreateLobby) {
                    onCreateLobby(c, (CreateLobby) object);
                } else if (object instanceof AddBotToLobby) {
                    onAddBotToLobby(c, (AddBotToLobby) object);
                } else if (object instanceof RemovePlayerFromLobby) {
                    onRemovePlayerFromLobby((RemovePlayerFromLobby) object);
                } else if (object instanceof JoinLobby) {
                    onJoinLobby(c, (JoinLobby) object);
                } else if (object instanceof StartLobby) {
                    onStartLobby(c, (StartLobby) object);
                } else if (object instanceof APlayerboard) {
                    onAPlayerBoard(c, (APlayerboard) object);
                } else if (object instanceof AnAttackAttempt) {
                    int gameID = lobby.getSlotOf(c).orElseThrow();
                    onAttackAttempt(new Pair<>(c, gameID), (AnAttackAttempt) object);
                } else if (object instanceof ChatMessageFromClient) {
                    int gameID = lobby.getSlotOf(c).orElseThrow();
                    onChatMessageFromClient(new Pair<>(c, gameID), (ChatMessageFromClient) object);
                }
            }

            public void disconnected(Connection c) {
                onDisconnected(c);
            }

        });

        server.bind(port);
    }

    private void onStartLobby(Connection connection, StartLobby startLobby) {
        if (!lobby.full()) {
            System.err.println("Not full and you're starting?=?????");
            return;
        }

        conversations = new Conversations();
        server.sendToAllTCP(new ReadyForShips());
        lobby.transitionToShips();
    }

    private void onRemovePlayerFromLobby(RemovePlayerFromLobby removePlayerFromLobby) {
        System.out.println("Removing player from lobby at slot " + removePlayerFromLobby.slot);
        lobby.removeParticipant(removePlayerFromLobby.slot);
        sendConnections();
    }

    private void onAddBotToLobby(Connection connection, AddBotToLobby addBotToLobby) {
        System.out.println("Added bot at slot " + addBotToLobby.slot + ": " +
                addBotToLobby.name + "(" + addBotToLobby.botDifficulty + ")");

        connection.setName(addBotToLobby.name);
        lobby.addBot(addBotToLobby.slot, addBotToLobby.name, addBotToLobby.botDifficulty, connection);
        sendConnections();
        printConnections();
    }

    private void onCreateLobby(Connection connection, CreateLobby createLobby) {
        connection.setName(createLobby.name);

        System.out.println(connection + " created lobby!");

        lobby = new GameLobby(createLobby.count);
        lobby.addPlayer(0, connection);

        sendConnections();
        printConnections();
    }

    private void onJoinLobby(Connection connection, JoinLobby joinLobby) {
        connection.setName(joinLobby.name);
        System.out.println(connection + " joined lobby!");

        if (lobby.full()) {
            connection.sendTCP(new IsFull());
            return;
        }

        int slot = lobby.playersInLobby();
        lobby.addPlayer(slot, connection);

        JoinLobbyResponse response = new JoinLobbyResponse();
        response.slots = lobby.slots();
        connection.sendTCP(response);

        sendConnections();

        printConnections();
    }

    private void onDisconnected(Connection connection) {
        System.out.println("Disconnected " + connection);

        switch (lobby.getState()) {
            case InGame -> {
                // TODO
                System.err.println("Boy left during game :(");
                // TODO
                lobby.transitionToLobby();
            }
            case SettingShips -> {
                handleLeavingWhileShips(new Pair<>(connection, lobby.getSlotOf(connection).orElseThrow()));
            }
            case InLobby -> {
                Optional<Integer> result = lobby.getSlotOf(connection);
                result.ifPresent(slot -> {
                    lobby.removeParticipant(slot);
                    sendConnections();
                    printConnections();
                    System.out.println("Count : " + lobby.playersInLobby());
                });
            }
        }
    }

    private void onChatMessageFromClient(Pair<Connection, Integer> connectionAndId, ChatMessageFromClient message) {

        final int from = connectionAndId.getValue();
        final int to = message.to;

        final int c = conversations.getConversationIDWithIDs(from, to);

        conversations.appendToConversation(from, c, message.text);
        Conversations.Line line = conversations.getLastLineFromConversation(c);
        ChatMessage chats = new ChatMessage();
        chats.saidIt = from;
        chats.message = line.decode(lobby.participants[from].name);

        System.out.println(chats.saidIt + " -> " + to + ": «\n" + chats.message + "\n»");
        lobby.participants[to].connection.sendTCP(chats);
    }

    private void onAPlayerBoard(Connection connection, APlayerboard aPlayerBoard) {
        final int gameID = lobby.getSlotOf(connection).orElseThrow();

        lobby.setGameBoardOfPlayer(gameID, aPlayerBoard.board);

        System.out.println("A playerBoard from" + connection + "(" + gameID + ")");

        IBoardSetup asBoardManager = lobby;

        //IF WE'VE RECEIVED ALL, WE CAN START
        if (asBoardManager.allBoardsSet()) {
            System.out.println("All boards are set, the player can now start");

            sendConnections();

            for (int i = 0; i < lobby.playersInLobby(); i++) {
                final int slot = i;

                IntStream base = IntStream.range(0, lobby.playersInLobby());
                IntStream otherIndexes = base.filter(n -> n != slot);

                Stream<String[][]> otherBoards = otherIndexes
                        .mapToObj(n -> lobby.playerBoards[n])
                        .map(PlayerBoardTransformer::transform);

                CanStart canStart = new CanStart();

                canStart.boards = otherBoards.toArray(String[][][]::new);

                // REALLY JAVA?
                base = IntStream.range(0, lobby.playersInLobby());
                otherIndexes = base.filter(n -> n != slot);

                canStart.indices = otherIndexes.toArray();

                lobby.participants[i].connection.sendTCP(canStart);
            }

            WhoseTurn whoseTurn = new WhoseTurn();
            whoseTurn.index = 0;
            server.sendToAllTCP(whoseTurn);

            lobby.participants[0].connection.sendTCP(new YourTurn());
        }
    }

    private int nextTurnId() {

        for (int i = 1; i < lobby.playersInLobby(); i++) {
            int playerIndex = (currentPlayer + i) % lobby.playersInLobby();
            if (!lobby.playerBoards[playerIndex].isGameOver()) {
                return playerIndex;

            }
        }

        System.err.println("No next turn id??" + currentPlayer + " " + lobby.playersInLobby());
        return 0;
    }

    private void onAttackAttempt(Pair<Connection, Integer> connectionAndId, AnAttackAttempt a) {
        final Connection connection = connectionAndId.getKey();
        final int slot = lobby.getSlotOf(connection).orElseThrow();
        final LobbyParticipant attackedParticipant = lobby.participants[a.toAttackID];

        System.out.println(connection + " is attacking " + attackedParticipant.name);

        final PlayerBoard attackedBoard = lobby.playerBoards[a.toAttackID];

        AttackResult result = attackedBoard.getAttacked(a.l, a.c);

        String[][] attackedBoardString = PlayerBoardTransformer.transform(attackedBoard);

        AnAttackResponse response = new AnAttackResponse();
        response.attackResult = result;
        response.newAttackedBoard = attackedBoardString;
        response.attacked = a.toAttackID;

        connection.sendTCP(response);

        // TO THE ATTACKED GUY
        YourBoardToPaint attacked = new YourBoardToPaint();
        attacked.board = attackedBoardString;
        attackedParticipant.connection.sendTCP(attacked);

        final int nPlayers = lobby.playersInLobby();

        for (int i = 0; i < nPlayers; i++) {

            if (i == a.toAttackID || i == slot) {
                continue;
            }

            EnemyBoardToPaint eb = new EnemyBoardToPaint();
            eb.newAttackedBoard = attackedBoardString;
            eb.id = a.toAttackID;

            lobby.participants[i].connection.sendTCP(eb);
        }

        if (!result.shouldPlayAgain()) {
            currentPlayer = nextTurnId();
        } else {
            if (lobby.gameIsOver()) {
                System.out.println("Game over " + connection + " won!");
                attackedParticipant.connection.sendTCP(new YouDead());
                connection.sendTCP(new YouWon());
            } else if (lobby.isGameOverFor(a.toAttackID)) {
                System.out.println("Man down: " + attackedParticipant.connection);
                attackedParticipant.connection.sendTCP(new YouDead());
                PlayerDied playerDied = new PlayerDied();
                playerDied.who = a.toAttackID;
                sendToAllExcept(a.toAttackID, playerDied);
            }
        }

        final LobbyParticipant nextParticipant = lobby.participants[currentPlayer];

        WhoseTurn whoseTurn = new WhoseTurn();
        whoseTurn.index = currentPlayer;

        sendToAllExcept(currentPlayer, whoseTurn);
        nextParticipant.connection.sendTCP(new YourTurn());

        System.out.println("Player is [" + currentPlayer + "]: " + lobby.participants[currentPlayer]);

    }

    private void sendToAllExcept(int i, Object object) {
        for (int k = 0; k < lobby.participants.length; ++k) {
            if (k != i) {
                lobby.participants[k].connection.sendTCP(object);
            }
        }
    }

    public void start() {
        server.start();
        System.out.println("Server started");
    }

    private void printConnections() {
        LobbyParticipant[] participants = lobby.participants;
        for (int i = 0, participantsLength = participants.length; i < participantsLength; i++) {
            LobbyParticipant participant = participants[i];
            if (participant == null) {
                continue;
            }
            final Connection connection = participant.connection;
            System.out.println(participant.name + " has ID:" + i +
                    " and address:" + connection.getRemoteAddressTCP());
        }
    }

    private void sendConnections() {

        ConnectedPlayers connectedPlayers = new ConnectedPlayers();
        connectedPlayers.participants = new Participant[lobby.participants.length];

        for (int i = 0; i < lobby.participants.length; ++i) {
            final LobbyParticipant participant = lobby.participants[i];

            if (participant == null) {
                connectedPlayers.participants[i] = null;
                continue;
            }

            if (participant.isBot()) {
                BotLobbyParticipant asBot = (BotLobbyParticipant) participant;
                connectedPlayers.participants[i] = new Participant(asBot.difficulty, asBot.name, i);
            } else {
                connectedPlayers.participants[i] = new Participant(participant.name, i);
            }

        }

        for (int i = 0; i < lobby.participants.length; ++i) {
            if (lobby.participants[i] != null) {
                connectedPlayers.slot = i;
                lobby.participants[i].connection.sendTCP(connectedPlayers);
            }
        }

    }

    private void handleLeavingWhileShips(Pair<Connection, Integer> connectionAndId) {
    }

}

package Server;

import Common.*;
import Common.Network.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.util.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class GameServer {

    private final static long TIME_TO_WAIT = 1000 * 60;
    private final Server server;
    private Game game;

    public GameLobby lobby;

    //WILL SAVE WHAT CONNECTIONS THE GAME STARTED WITH
    //SO IT'S POSSIBLE TO KNOW IF SOMEBODY WHO DROPPED IS RECONNECTING
    private long currentWaitedTime;
    private GameState state;
    private Conversations conversations;
    private int currentPlayer;

    public GameServer(int port) throws IOException {

        state = GameState.waitingForPlayers;
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
                    int gameID = lobby.getSlotOf(c);
                    onAttackAttempt(new Pair<>(c, gameID), (AnAttackAttempt) object);
                } else if (object instanceof ChatMessageFromClient) {
                    int gameID = lobby.getSlotOf(c);
                    onChatMessageFromClient(new Pair<>(c, gameID), (ChatMessageFromClient) object);
                }
            }

            public void disconnected(Connection c) {
                if (true) return;
                int gameID = lobby.getSlotOf(c);
                onDisconnected(new Pair<>(c, gameID));
            }

        });

        server.bind(port);
    }

    private void onStartLobby(Connection connection, StartLobby startLobby) {
        if (!lobby.full()) {
            System.err.println("Not full and you're starting?=?????");
            return;
        }

        sendReadyForShips();
    }

    private void onRemovePlayerFromLobby(RemovePlayerFromLobby removePlayerFromLobby) {
        lobby.removeParticipant(removePlayerFromLobby.slot);
    }

    private void onAddBotToLobby(Connection connection, AddBotToLobby addBotToLobby) {
        System.out.println("Added bot to lobby!");
        connection.setName(addBotToLobby.name);
        lobby.addBot(addBotToLobby.slot, addBotToLobby.name, addBotToLobby.botDifficulty, connection);
        sendConnections();
        printConnections();
    }

    private void onCreateLobby(Connection connection, CreateLobby createLobby) {
        System.out.println("Created lobby!");
        connection.setName(createLobby.name);
        lobby = new GameLobby(createLobby.count);
        lobby.addPlayer(0, connection);
        game = new Game(createLobby.count);
        sendConnections();
        printConnections();
    }

    private void onJoinLobby(Connection connection, JoinLobby joinLobby) {
        System.out.println("Joined lobby!");
        connection.setName(joinLobby.name);

        switch (state) {
            case waitingForPlayers:
                int slot = lobby.count();
                lobby.addPlayer(slot, connection);
                JoinLobbyResponse response = new JoinLobbyResponse();
                response.slots = game.playerBoards.length;
                connection.sendTCP(response);
                break;
            case waitingForShips:
                connection.sendTCP(new IsFull());
                break;
            case playing:
            case playing2left:
                break;
        }

        sendConnections();
        printConnections();
    }

    private void onDisconnected(Pair<Connection, Integer> connectionAndId) {
        final Connection connection = connectionAndId.getKey();
        System.out.println("Disconnected " + connection.toString());
        switch (state) {
            case waitingForPlayers -> sendConnections();
            case waitingForShips -> handleLeavingWhileShips(connectionAndId);
        }
        System.out.println("Count : " + lobby.count());
        printConnections();
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
        System.out.println("SENDING FROM: " + chats.saidIt + " TO: " + to + " CONTENT: " + chats.message);
        lobby.participants[to].connection.sendTCP(chats);
    }

    private void onAPlayerBoard(Connection connection, APlayerboard aPlayerBoard) {
        final int gameID = lobby.getSlotOf(connection);
        game.setBoard(gameID, aPlayerBoard.board);

        System.out.println("A playerBoard " + connection + " -> " + gameID);

        //IF WE'VE RECEIVED ALL, WE CAN START
        if (game.allBoardsSet()) {

            System.out.println("All set");

            sendOthersDetails();
            sendOthersBoards();

            state = GameState.playing;

            WhoseTurn whoseTurn = new WhoseTurn();
            whoseTurn.name = lobby.participants[0].name;
            server.sendToAllTCP(whoseTurn);

            lobby.participants[0].connection.sendTCP(new YourTurn());
            server.sendToAllTCP(new CanStart());
        }
    }

    private void onAttackAttempt(Pair<Connection, Integer> connectionAndId, AnAttackAttempt a) {

        final Connection connection = connectionAndId.getKey();

        System.out.println(connection.toString() + " IS ATTACKING " + lobby.participants[a.toAttackID].name);

        AttackResult result = game.attack(
                a.toAttackID,
                a.l,
                a.c
        );

        boolean hitShip = result.status == AttackResultStatus.HitShipPiece;
        boolean shouldGoAgain = result.shouldPlayAgain();
        boolean hitSomething = result.valid();

        PlayerBoard _board = game.playerBoards[a.toAttackID];

        String[][] attackedOne = PlayerBoardTransformer.transform(_board);

        System.out.println(Arrays.deepToString(attackedOne));

        AnAttackResponse response = new AnAttackResponse();
        response.again = shouldGoAgain;
        response.newAttackedBoard = attackedOne;
        response.actualHit = hitSomething;
        response.shipHit = hitShip;

        // TO THE ATTACKED GUY

        YourBoardToPaint attacked = new YourBoardToPaint();
        attacked.board = attackedOne;


        //TO THE GUY THAT ATTACKED
        //TO THE GUY NOT ATTACKED
        //TO THE ATTACKED
        switch (state) {
            case playing -> {
                connection.sendTCP(response);
                EnemyBoardToPaint eb = new EnemyBoardToPaint();
                eb.newAttackedBoard = attackedOne;
                eb.id = a.toAttackID;

                lobby.participants[a.otherID].connection.sendTCP(eb);
                lobby.participants[a.toAttackID].connection.sendTCP(attacked);
                if (!shouldGoAgain) {
                    currentPlayer = (currentPlayer + 1) % 3;
                    WhoseTurn whoseTurn = new WhoseTurn();
                    whoseTurn.name = lobby.participants[currentPlayer].name;
                    sendToAllExcept(currentPlayer, whoseTurn);
                    lobby.participants[currentPlayer].connection.sendTCP(new YourTurn());
                } else {
                    System.out.println("HIT");
                    if (game.isGameOverFor(a.toAttackID)) {
                        System.out.println("MAN DOWN!");
                        state = GameState.playing2left;
                        lobby.participants[a.toAttackID].connection.sendTCP(new YouDead());
                        PlayerDied playerDied = new PlayerDied();
                        playerDied.who = a.toAttackID;
                        sendToAllExcept(a.toAttackID, playerDied);
                    }
                }
            }
            case playing2left -> {
                connection.sendTCP(response);
                lobby.participants[a.toAttackID].connection.sendTCP(attacked);
                if (!shouldGoAgain) {
                    currentPlayer = a.toAttackID;
                    WhoseTurn whoseTurn = new WhoseTurn();
                    whoseTurn.name = lobby.participants[currentPlayer].name;
                    sendToAllExcept(currentPlayer, whoseTurn);

                    lobby.participants[currentPlayer].connection.sendTCP(new YourTurn());
                } else {
                    System.out.println("HIT");
                    if (game.isGameOverFor(a.toAttackID)) {
                        //GAME IS OVER
                        lobby.participants[a.toAttackID].connection.sendTCP(new YouDead());
                        lobby.participants[currentPlayer].connection.sendTCP(new YouWon());
                        state = GameState.waitingForPlayers;
                    }
                }
            }
        }

    }

    private void sendOthersBoards() {
        for (int i = 0; i < game.playerBoards.length; i++) {
            EnemiesBoardsToPaint enemiesBoardsToPaint = new EnemiesBoardsToPaint();
            //int own = game.getPlayerById(i).id();

            enemiesBoardsToPaint.board1 = PlayerBoardTransformer.transform(game.playerBoards[((i + 1) % 3)]);
            enemiesBoardsToPaint.board2 = PlayerBoardTransformer.transform(game.playerBoards[((i + 2) % 3)]);

            lobby.participants[i].connection.sendTCP(enemiesBoardsToPaint);
        }
    }

    private void sendOthersDetails() {
        for (int i = 0; i < lobby.count(); i++) {
            OthersSpecs send = new OthersSpecs();
            send.ene1 = (i + 1) % 3;
            send.ene2 = (i + 2) % 3;

            send.ene1n = lobby.participants[((i + 1) % 3)].name;
            send.ene2n = lobby.participants[((i + 2) % 3)].name;

            lobby.participants[i].connection.sendTCP(send);
        }
    }

    private void sendToAllExcept(int i, Object object) {
        lobby.participants[((i + 4) % 3)].connection.sendTCP(object);
        lobby.participants[((i + 5) % 3)].connection.sendTCP(object);
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

        connectedPlayers.participants =
                Arrays.stream(lobby.participants).filter(Objects::nonNull).map(p -> {
                    if (p.isBot()) {
                        BotLobbyParticipant asBot = (BotLobbyParticipant) p;
                        return new Participant(asBot.difficulty, asBot.name);
                    } else {
                        return new Participant(p.name);
                    }
                }).toArray(Participant[]::new);

        server.sendToAllTCP(connectedPlayers);
    }

    private void handleLeavingWhileShips(Pair<Connection, Integer> connectionAndId) {
    }

    private void sendReadyForShips() {
        conversations = new Conversations();
        server.sendToAllTCP(new ReadyForShips());
        state = GameState.waitingForShips;
    }

}

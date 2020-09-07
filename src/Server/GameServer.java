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

public class GameServer {

    private final static long TIME_TO_WAIT = 1000 * 60;
    private final Lobby lobby;
    private final Server server;
    //WILL SAVE WHAT CONNECTIONS THE GAME STARTED WITH
    //SO IT'S POSSIBLE TO KNOW IF SOMEBODY WHO DROPPED IS RECONNECTING
    private long currentWaitedTime;
    private GameState state;
    private Conversations conversations;
    private int currentPlayer;

    public GameServer(int port) throws IOException {

        state = GameState.waitingForPlayers;

        lobby = new Lobby(3);

        server = new Server();

        Network.register(server);

        server.addListener(new Listener() {

            public void received(Connection c, Object object) {

                if (object instanceof Register) {
                    onRegister(c, (Register) object);
                } else if (object instanceof APlayerboard) {
                    int gameID = lobby.getGameIDByConnection(c);
                    onAPlayerBoard(new Pair<>(c, gameID), (APlayerboard) object);
                } else if (object instanceof AnAttackAttempt) {
                    int gameID = lobby.getGameIDByConnection(c);
                    onAttackAttempt(new Pair<>(c, gameID), (AnAttackAttempt) object);
                } else if (object instanceof ChatMessageFromClient) {
                    int gameID = lobby.getGameIDByConnection(c);
                    onChatMessageFromClient(new Pair<>(c, gameID), (ChatMessageFromClient) object);
                }
            }

            public void disconnected(Connection c) {
                int gameID = lobby.getGameIDByConnection(c);
                onDisconnected(new Pair<>(c, gameID));
            }

        });

        server.bind(port);
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
        chats.message = line.decode(lobby.getConnectionById(from).toString());
        System.out.println("SENDING FROM: " + chats.saidIt + " TO: " + to + " CONTENT: " + chats.message);
        lobby.getConnectionById(to).sendTCP(chats);
    }

    private void onAPlayerBoard(Pair<Connection, Integer> connectionAndId, APlayerboard object) {
        final int gameID = connectionAndId.getValue();
        PlayerBoard pb = PlayerBoardTransformer.parse(object.board);
        lobby.setPlayerBoard(pb, gameID);
        //IF WE'VE RECEIVED ALL, WE CAN START
        if (lobby.allBoardsSet()) {

            sendOthersDetails();
            sendOthersBoards();

            state = GameState.playing;

            WhoseTurn whoseTurn = new WhoseTurn();
            whoseTurn.name = lobby.firstConnection().toString();
            server.sendToAllTCP(whoseTurn);

            lobby.firstConnection().sendTCP(new YourTurn());
            server.sendToAllTCP(new CanStart());
        }
    }

    private void onAttackAttempt(Pair<Connection, Integer> connectionAndId, AnAttackAttempt a) {

        final Connection connection = connectionAndId.getKey();

        System.out.println(connection.toString() + " IS ATTACKING " +
                lobby.getConnectionById(a.toAttackID).toString());

        AttackResult result = lobby.attack(
                a.toAttackID,
                a.l,
                a.c
        );

        boolean hitShip = result.status == AttackResultStatus.HitShipPiece;
        boolean shouldGoAgain = result.shouldPlayAgain();
        boolean hitSomething = result.valid();

        PlayerBoard _board = lobby.getPlayerBoard(a.toAttackID);

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
                lobby.getConnectionById(a.otherID).sendTCP(eb);
                lobby.getConnectionById(a.toAttackID).sendTCP(attacked);
                if (!shouldGoAgain) {
                    currentPlayer = (currentPlayer + 1) % 3;
                    WhoseTurn whoseTurn = new WhoseTurn();
                    whoseTurn.name = lobby.getConnectionById(currentPlayer).toString();
                    sendToAllExcept(currentPlayer, whoseTurn);
                    lobby.getConnectionById(currentPlayer).sendTCP(new YourTurn());
                } else {
                    System.out.println("HIT");
                    if (lobby.isGameOverFor(a.toAttackID)) {
                        System.out.println("MAN DOWN!");
                        state = GameState.playing2left;
                        lobby.getConnectionById(a.toAttackID).sendTCP(new YouDead());
                        PlayerDied playerDied = new PlayerDied();
                        playerDied.who = a.toAttackID;
                        sendToAllExcept(a.toAttackID, playerDied);
                    }
                }
            }
            case playing2left -> {
                connection.sendTCP(response);
                lobby.getConnectionById(a.toAttackID).sendTCP(attacked);
                if (!shouldGoAgain) {
                    currentPlayer = a.toAttackID;
                    WhoseTurn whoseTurn = new WhoseTurn();
                    whoseTurn.name = lobby.getConnectionById(currentPlayer).toString();
                    sendToAllExcept(currentPlayer, whoseTurn);
                    lobby.getConnectionById(currentPlayer).sendTCP(new YourTurn());
                } else {
                    System.out.println("HIT");
                    if (lobby.isGameOverFor(a.toAttackID)) {
                        //GAME IS OVER
                        lobby.getConnectionById(a.toAttackID).sendTCP(new YouDead());
                        lobby.getConnectionById(currentPlayer).sendTCP(new YouWon());
                        state = GameState.waitingForPlayers;
                    }
                }
            }
        }

    }

    private void sendOthersBoards() {
        for (int i = 0; i < lobby.count(); i++) {
            EnemiesBoardsToPaint enemiesBoardsToPaint = new EnemiesBoardsToPaint();
            //int own = lobby.getPlayerById(i).id();

            enemiesBoardsToPaint.board1 = PlayerBoardTransformer.transform(lobby.getPlayerBoard((i + 1) % 3));
            enemiesBoardsToPaint.board2 = PlayerBoardTransformer.transform(lobby.getPlayerBoard((i + 2) % 3));

            lobby.getConnectionById(i).sendTCP(enemiesBoardsToPaint);
        }
    }

    private void sendOthersDetails() {
        for (int i = 0; i < lobby.count(); i++) {
            OthersSpecs send = new OthersSpecs();
            send.ene1 = (i + 1) % 3;
            send.ene2 = (i + 2) % 3;

            send.ene1n = lobby.getConnectionById((i + 1) % 3).toString();
            send.ene2n = lobby.getConnectionById((i + 2) % 3).toString();

            lobby.getConnectionById(i).sendTCP(send);
        }
    }

    private void sendToAllExcept(int i, Object object) {
        lobby.getConnectionById((i + 4) % 3).sendTCP(object);
        lobby.getConnectionById((i + 5) % 3).sendTCP(object);
    }

    public void start() {
        server.start();
        System.out.println("Server started");
    }

    private void onRegister(Connection connection, Register r) {

        connection.setName(r.name);

        switch (state) {
            case waitingForPlayers:
                lobby.addNewPlayer(connection, lobby.count());
                if (lobby.full()) {
                    sendReadyForShips();
                } else {
                    sendConnections();
                }
                break;
            case waitingForShips:
                connection.sendTCP(new IsFull());
                break;
            case playing:
            case playing2left:
                break;
        }

        printConnections();
    }


    private void printConnections() {
        for (Map.Entry<Integer, Connection> connectionAndId : lobby.getConnectAndIds()) {
            final Connection connection = connectionAndId.getValue();
            final Integer gameID = connectionAndId.getKey();

            System.out.println(connection.toString() + " has ID:" + gameID +
                    " and address:" + connection.getRemoteAddressTCP());
        }
    }

    private void sendConnections() {
        if (lobby.count() == 0) return;

        ConnectedPlayers connectedPlayers = new ConnectedPlayers();
        connectedPlayers.names = lobby.getNamesOfConnected();

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

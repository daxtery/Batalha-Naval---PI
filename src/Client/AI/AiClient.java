package Client.AI;

import Client.AI.Personality.AIPersonality;
import Client.GameClient;
import Client.IClient;
import Client.PlayerCode;
import Client.PlayerSettings;
import Common.*;
import javafx.util.Pair;
import util.Point;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AiClient implements IClient, Runnable {

    public final int slot;
    public final AIPersonality personality;
    public final String name;
    public final GameClient gameClient;
    private final String address;

    protected boolean debug;
    protected PlayerBoard[] playerBoards;
    protected Network.Participant[] otherParticipants;
    protected Network.Participant[] allParticipants;

    public AiClient(int slot, AIPersonality personality, String name, String address) {
        this.slot = slot;
        this.name = name;
        this.address = address;
        this.gameClient = new GameClient(this);
        this.personality = personality;
        setDebug(true);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public PlayerBoard[] getPlayerBoards() {
        return playerBoards;
    }

    public Network.Participant[] getOtherParticipants() {
        return otherParticipants;
    }


    @Override
    public void onIsFull() {
        System.err.println("!! IS FULL WITH AI CLIENT?");
    }

    @Override
    public void onAbort() {
        gameClient.stop();
        Thread.currentThread().interrupt();
    }

    @Override
    public void onCanStart(Network.StartGameResponse startGameResponse) {
        this.otherParticipants = Arrays.stream(this.allParticipants)
                .filter(p -> p.slot != slot)
                .toArray(Network.Participant[]::new);

        playerBoards = new PlayerBoard[startGameResponse.boards.length + 1];

        for (int i = 0; i < startGameResponse.boards.length; i++) {
            playerBoards[startGameResponse.indices[i]] = startGameResponse.boards[i].toPlayerBoard();
        }

        for (Network.Participant participant : otherParticipants) {
            if (participant.isBot()) {
                continue;
            }

            sendMessageTo(this.personality.introductionMessage(), participant.slot);
        }

        personality.onCanStart(startGameResponse, this);
    }

    @Override
    public void onWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse) {
        personality.onWhoseTurn(whoseTurnResponse, this);
    }

    @Override
    public void onConnectedPlayers(Network.ConnectedPlayersResponse connectedPlayersResponse) {
        this.allParticipants = connectedPlayersResponse.participants;
    }

    @Override
    public void onReadyForShips() {
        GameConfiguration gameConfiguration = new GameConfiguration(
                PlayerBoardConstants.DEFAULT_LINES,
                PlayerBoardConstants.DEFAULT_COLUMNS,
                IntStream.of(PlayerBoardConstants.DEFAULT_SIZES).boxed().collect(Collectors.toList()),
                Direction.values()
        );

        PlayerBoard playerBoard = PlayerBoardExtensions.constructRandomPlayerBoard(gameConfiguration);
        PlayerBoardMessage message = new PlayerBoardMessage(playerBoard);
        Network.PlayerCommitBoard playerCommitBoard = new Network.PlayerCommitBoard(message);

        gameClient.sendTCP(playerCommitBoard);
    }

    @Override
    public void onYourBoardToPaint(Network.YourBoardResponse object) {
        personality.onYourBoardToPaint(object, this);
    }

    @Override
    public void onEnemyBoardToPaint(Network.EnemyBoardResponse object) {
        playerBoards[object.id] = object.newAttackedBoard.toPlayerBoard();
        personality.onEnemyBoardToPaint(object, this);
    }

    @Override
    public void onAnAttackResponse(Network.AnAttackResponse object) {
        playerBoards[object.attacked] = object.newAttackedBoard.toPlayerBoard();
        personality.onAnAttackResponse(object, this);
    }

    @Override
    public void onYourTurn() {
        Pair<Integer, Point> attack = personality.onYourTurn(this);
        int slot = attack.getKey();
        Point point = attack.getValue();

        try {
            Thread.sleep(1000);

            Network.AnAttack attackMessage = new Network.AnAttack(slot, point);
            gameClient.sendTCP(attackMessage);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onYouDead() {
        personality.onYouDead(this);
    }

    @Override
    public void onPlayerDied(Network.PlayerDiedResponse object) {
        personality.onPlayerDied(object, this);
    }

    @Override
    public void onYouWon() {
        personality.onYouWon(this);
    }

    @Override
    public void onChatMessage(Network.ChatMessageResponse object) {
        personality.onChatMessage(object, this);
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {
    }

    @Override
    public void onNetworkDisconnected() {
        onAbort();
    }

    public final void sendMessageTo(String text, int to) {
        if (!text.endsWith("\n")) text = text + "\n";
        Network.ChatMessage message = new Network.ChatMessage(text, to);
        gameClient.sendTCP(message);
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            gameClient.start();

            //TODO: FIX ME
            PlayerSettings settings = PlayerSettings.botConfiguration(name);

            gameClient.tryConnect(this.address, Network.port, settings);

            gameClient.sendTCP(
                    new Network.AddBotToLobby(
                            slot,
                            personality.getPersonality(),
                            name
                    )
            );

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down");
        onAbort();
    }

}

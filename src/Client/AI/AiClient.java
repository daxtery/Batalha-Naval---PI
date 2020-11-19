package Client.AI;

import Client.GameClient;
import Client.IClient;
import Common.*;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AiClient implements IClient, Runnable {

    public final int slot;
    public final AIPersonality personality;
    public final String name;
    public final GameClient gameClient;
    private final String address;

    public AiClient(int slot, AIPersonality personality, String name, String address) {
        this.slot = slot;
        this.name = name;
        this.address = address;
        this.gameClient = new GameClient(this);

        this.personality = personality;
        personality.setAi(this);
    }

    @Override
    public void OnIsFull() {
        System.err.println("!! IS FULL WITH AI CLIENT?");
        personality.OnIsFull();
    }

    @Override
    public void OnAbort() {
        gameClient.stop();
        personality.OnIsFull();
        Thread.currentThread().interrupt();
    }

    @Override
    public void OnCanStart(Network.StartGameResponse startGameResponse) {
        personality.OnCanStart(startGameResponse);
    }

    @Override
    public void OnWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse) {
        personality.OnWhoseTurn(whoseTurnResponse);
    }

    @Override
    public void onConnectedPlayers(Network.ConnectedPlayersResponse connectedPlayersResponse) {
        personality.onConnectedPlayers(connectedPlayersResponse);
    }

    @Override
    public void OnReadyForShips() {
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
        personality.OnReadyForShips();
    }

    @Override
    public void OnYourBoardToPaint(Network.YourBoardResponse object) {
        personality.OnYourBoardToPaint(object);
    }

    @Override
    public void OnEnemyBoardToPaint(Network.EnemyBoardResponse object) {
        personality.OnEnemyBoardToPaint(object);
    }

    @Override
    public void OnAnAttackResponse(Network.AnAttackResponse object) {
        personality.OnAnAttackResponse(object);
    }

    @Override
    public void OnYourTurn() {
        personality.OnYourTurn();
    }

    @Override
    public void OnYouDead() {
        personality.OnYouDead();
    }

    @Override
    public void OnPlayerDied(Network.PlayerDiedResponse object) {
        personality.OnPlayerDied(object);
    }

    @Override
    public void OnYouWon() {
        personality.OnYouWon();
    }

    @Override
    public void OnChatMessage(Network.ChatMessageResponse object) {
        personality.OnChatMessage(object);
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {
        personality.onJoinLobbyResponse(joinLobbyResponse);
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
            gameClient.tryConnect(this.address, Network.port);

            gameClient.sendTCP(
                    new Network.AddBotToLobby(
                            slot,
                            personality.personality,
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
        OnAbort();
    }
}

package Client.AI;

import Client.App;
import Client.GameClient;
import Client.IClient;
import Common.*;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;

public class AiClient implements IClient, Runnable {

    public final int slot;
    public final BotDifficulty botDifficulty;
    public final String name;
    public final GameClient gameClient;
    private final String address;
    private Network.ConnectedPlayers connectedPlayers;
    private int focusing;
    private String lastMessage;

    public AiClient(int slot, BotDifficulty botDifficulty, String name, String address) {
        this.slot = slot;
        this.botDifficulty = botDifficulty;
        this.name = name;
        this.address = address;
        this.gameClient = new GameClient(this);
    }

    @Override
    public void OnIsFull() {
        System.err.println("!! IS FULL WITH AI CLIENT?");
    }

    @Override
    public void OnAbort() {
        gameClient.stop();
        Thread.currentThread().interrupt();
    }

    @Override
    public void OnCanStart(Network.CanStart canStart) {
        IntStream slots = new Random().ints(0, connectedPlayers.participants.length);
        focusing = slots.filter(slot -> slot != connectedPlayers.slot).findFirst().orElseThrow();
    }

    @Override
    public void OnWhoseTurn(Network.WhoseTurn whoseTurn) {
    }

    @Override
    public void onConnectedPlayers(Network.ConnectedPlayers connectedPlayers) {
        this.connectedPlayers = connectedPlayers;
    }

    @Override
    public void OnReadyForShips() {
        PlayerBoard playerBoard = PlayerBoardFactory.getRandomPlayerBoard();

        Network.APlayerboard p = new Network.APlayerboard();
        p.board = PlayerBoardTransformer.transform(playerBoard);

        System.out.println("AI: I sent");

        gameClient.sendTCP(p);
    }

    @Override
    public void OnYourBoardToPaint(Network.YourBoardToPaint object) {
    }

    @Override
    public void OnEnemyBoardToPaint(Network.EnemyBoardToPaint object) {
    }

    @Override
    public void OnAnAttackResponse(Network.AnAttackResponse object) {
    }

    @Override
    public void OnYourTurn() {
        Network.ChatMessageFromClient message = new Network.ChatMessageFromClient();
        message.to = focusing;
        message.text = "I'm coming for you :)";
        gameClient.sendTCP(message);
    }

    @Override
    public void OnYouDead() {
    }

    @Override
    public void OnPlayerDied(Network.PlayerDied object) {
    }

    @Override
    public void OnYouWon() {
    }

    @Override
    public void OnChatMessage(Network.ChatMessage object) {

        if (lastMessage != null && lastMessage.equals(object.message)) {
            return;
        }

        lastMessage = object.message;

        Network.ChatMessageFromClient message = new Network.ChatMessageFromClient();
        message.to = focusing;
        message.text = "I'm coming for you :)";
        gameClient.sendTCP(message);
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {
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
                            botDifficulty,
                            name
                    )
            );

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        OnAbort();
    }
}

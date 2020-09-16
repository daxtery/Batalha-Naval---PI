package Client.AI;

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
    private MyAI brain;

    private PlayerBoard[] playerBoards;
    private AiMove move;

    public AiClient(int slot, BotDifficulty botDifficulty, String name, String address) {
        this.slot = slot;
        this.botDifficulty = botDifficulty;
        this.name = name;
        this.address = address;
        this.gameClient = new GameClient(this);
        this.brain = new MyAI();
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

        playerBoards = new PlayerBoard[canStart.boards.length + 1];

        for (int i = 0; i < canStart.boards.length; i++) {
            playerBoards[canStart.indices[i]] = PlayerBoardTransformer.parse(canStart.boards[i]);
        }

        Network.ChatMessageFromClient message = new Network.ChatMessageFromClient();
        message.to = focusing;

        final String[] taunts = {
                "I'm coming for you :)\n",
                "You won't live much longer haha\n",
                "This is the end of your path, say your prayers\n",
                "終わりました\n",
                "I feel sorry for you\n",
                "I'm sorry, it's nothing personal, I must do this\n",
                "Feel the wrath of a thousand battle ships\n"
        };

        final int index = new Random().nextInt(taunts.length);

        message.text = taunts[index];
        gameClient.sendTCP(message);
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
        playerBoards[object.id] = PlayerBoardTransformer.parse(object.newAttackedBoard);
    }

    @Override
    public void OnAnAttackResponse(Network.AnAttackResponse object) {
        brain.react(playerBoards[focusing], move, object.attackResult);
    }

    @Override
    public void OnYourTurn() {
        move = brain.nextMove(playerBoards[focusing]);

        Network.AnAttackAttempt attack = new Network.AnAttackAttempt();
        attack.toAttackID = focusing;
        attack.l = move.point.x;
        attack.c = move.point.y;

        try {
            Thread.sleep(1000);
            gameClient.sendTCP(attack);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        if (object.saidIt == focusing) {
            lastMessage = object.message;

            final String[] comebacks = {
                    "Despair\n",
                    "There's no talking out of this\n",
                    "Don't even try\n",
                    "お前はもう死んでいる\n",
                    "Good, good, plea more\n",
                    "This makes me as sad as it makes you\n",
                    "Accept that you've lost\n"
            };

            final int index = new Random().nextInt(comebacks.length);

            Network.ChatMessageFromClient message = new Network.ChatMessageFromClient();
            message.to = focusing;
            message.text = comebacks[index];
            gameClient.sendTCP(message);
        }
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

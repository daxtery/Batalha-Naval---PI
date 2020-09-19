package Client.AI;

import Common.BotPersonality;
import Common.Network;
import Common.PlayerBoard;
import Common.PlayerBoardTransformer;

import java.util.Random;
import java.util.stream.IntStream;

public class FocusedBot extends Client.AI.AIPersonality {

    private final MyAI brain;
    private String lastMessage;
    private int focusing;
    private AiMove move;
    private PlayerBoard[] playerBoards;
    private Network.ConnectedPlayers connectedPlayers;

    public FocusedBot() {
        super(BotPersonality.Focused);
        this.brain = new MyAI();
    }

    @Override
    public void OnIsFull() {

    }

    @Override
    public void OnAbort() {

    }

    @Override
    public void OnCanStart(Network.CanStart canStart) {
        IntStream slots = new Random().ints(0, connectedPlayers.participants.length);
        focusing = slots.filter(slot -> slot != connectedPlayers.slot).findFirst().orElseThrow();
        focusing = 0;

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
        ai.gameClient.sendTCP(message);
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
        playerBoards[object.attacked] = PlayerBoardTransformer.parse(object.newAttackedBoard);
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
            Network.ChatMessageFromClient chatMessageFromClient = new Network.ChatMessageFromClient();

            chatMessageFromClient.text =
                    "Gonna attack you with " +
                            move +
                            " out of: " +
                            brain.getPossibleMoves().toString() +
                            "\n";

            chatMessageFromClient.to = focusing;

            ai.gameClient.sendTCP(chatMessageFromClient);

            Thread.sleep(500);
            ai.gameClient.sendTCP(attack);
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
            ai.gameClient.sendTCP(message);
        }
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {

    }
}

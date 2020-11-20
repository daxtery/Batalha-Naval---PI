package Client.AI;

import Common.*;

import java.util.*;
import java.util.stream.IntStream;


public class FocusedBot extends Client.AI.AIPersonality {

    private final BotBrain brain;
    private String lastMessage;
    private int focusing;
    private PlayerBoard[] playerBoards;
    private Network.ConnectedPlayersResponse connectedPlayersResponse;

    public FocusedBot() {
        super(BotPersonality.Focused);
        this.brain = new BotBrain();
    }

    @Override
    public void OnIsFull() {

    }

    @Override
    public void OnAbort() {

    }

    @Override
    public void OnCanStart(Network.StartGameResponse startGameResponse) {
        IntStream slots = new Random().ints(0, connectedPlayersResponse.participants.length);
        focusing = slots.filter(slot -> slot != connectedPlayersResponse.slot).findFirst().orElseThrow();

        playerBoards = new PlayerBoard[startGameResponse.boards.length + 1];

        for (int i = 0; i < startGameResponse.boards.length; i++) {
            playerBoards[startGameResponse.indices[i]] = startGameResponse.boards[i].toPlayerBoard();
        }

        Network.ChatMessage message = new Network.ChatMessage();
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
        client.gameClient.sendTCP(message);
    }

    @Override
    public void OnWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse) {

    }

    @Override
    public void onConnectedPlayers(Network.ConnectedPlayersResponse connectedPlayersResponse) {
        this.connectedPlayersResponse = connectedPlayersResponse;
    }

    @Override
    public void OnReadyForShips() {

    }

    @Override
    public void OnYourBoardToPaint(Network.YourBoardResponse object) {

    }

    @Override
    public void OnEnemyBoardToPaint(Network.EnemyBoardResponse object) {
        playerBoards[object.id] = object.newAttackedBoard.toPlayerBoard();
    }

    @Override
    public void OnAnAttackResponse(Network.AnAttackResponse object) {

        if (object.attacked != connectedPlayersResponse.slot) {
            playerBoards[object.attacked] = object.newAttackedBoard.toPlayerBoard();
        }

    }

    @Override
    public void OnYourTurn() {
        List<MoveCandidate> moves = brain.calculateMoves(playerBoards[focusing]);
        moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
        MoveCandidate move = moves.get(0);

        Network.AnAttack attack = new Network.AnAttack();
        attack.toAttackID = focusing;
        attack.at = move.attackedPoint;

        final boolean debug = true;

        try {
            Network.ChatMessage chatMessage = new Network.ChatMessage();

            StringBuilder builder = new StringBuilder()
                    .append("How do you like that attack @ ")
                    .append(move.attackedPoint)
                    .append("\n");

            if (debug) {
                builder.append("DEBUG: ")
                        .append("\n")
                        .append("Selected move was")
                        .append(move)
                        .append("\n")
                        .append("Considered moves were: ")
                        .append(moves)
                        .append("\n");
            }

            chatMessage.text = builder.toString();
            chatMessage.to = focusing;

            Thread.sleep(1000);

            client.gameClient.sendTCP(chatMessage);
            client.gameClient.sendTCP(attack);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnYouDead() {

    }

    @Override
    public void OnPlayerDied(Network.PlayerDiedResponse object) {

    }

    @Override
    public void OnYouWon() {

    }

    @Override
    public void OnChatMessage(Network.ChatMessageResponse object) {
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

            Network.ChatMessage message = new Network.ChatMessage();
            message.to = focusing;
            message.text = comebacks[index];
            client.gameClient.sendTCP(message);
        }
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {

    }
}

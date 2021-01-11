package Client.AI.Personality;

import Client.AI.AiClient;
import Client.AI.Brain.BaseBotBrain;
import Client.AI.Brain.MoveCandidate;
import Common.*;
import javafx.util.Pair;
import util.Point;

import java.util.*;

public class FocusedBot implements AIPersonality {

    private final BaseBotBrain brain;
    private String lastMessage;
    private int focusing;

    public FocusedBot() {
        this.brain = new BaseBotBrain();
    }

    @Override
    public BotPersonality getPersonality() {
        return BotPersonality.Focused;
    }

    @Override
    public String introductionMessage() {
        return "FocusedBot\n" + "I will choose a target and attack them non-stop";
    }

    @Override
    public void onCanStart(Network.StartGameResponse startGameResponse, AiClient aiClient) {

        Network.Participant[] others = aiClient.getOtherParticipants();

        Random random = new Random();
        int index = random.nextInt(others.length);

        focusing = others[index].slot;

        final String[] taunts = {
                "I'm coming for you :)\n",
                "You won't live much longer haha\n",
                "This is the end of your path, say your prayers\n",
                "終わりました\n",
                "I feel sorry for you\n",
                "I'm sorry, it's nothing personal, I must do this\n",
                "Feel the wrath of a thousand battle ships\n"
        };

        index = random.nextInt(taunts.length);

        aiClient.sendMessageTo(taunts[index], focusing);
    }

    @Override
    public void onWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse, AiClient aiClient) {
    }

    @Override
    public void onYourBoardToPaint(Network.YourBoardResponse object, AiClient aiClient) {
    }

    @Override
    public void onEnemyBoardToPaint(Network.EnemyBoardResponse object, AiClient aiClient) {
    }

    @Override
    public void onAnAttackResponse(Network.AnAttackResponse object, AiClient aiClient) {
    }

    @Override
    public Pair<Integer, Point> onYourTurn(AiClient aiClient) {

        PlayerBoard[] playerBoards = aiClient.getPlayerBoards();

        List<MoveCandidate> moves = brain.calculateMoves(playerBoards[focusing]);
        moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
        MoveCandidate move = moves.get(0);

        StringBuilder builder = new StringBuilder()
                .append("How do you like that attack @ ")
                .append(move.attackedPoint)
                .append("\n");

        if (aiClient.isDebug()) {
            builder.append("DEBUG: ")
                    .append("\n")
                    .append("Selected move was")
                    .append(move)
                    .append("\n")
                    .append("Considered moves were: ")
                    .append(moves)
                    .append("\n");
        }

        aiClient.sendMessageTo(builder.toString(), focusing);

        return new Pair<>(focusing, move.attackedPoint);
    }

    @Override
    public void onYouDead(AiClient aiClient) {

    }

    @Override
    public void onPlayerDied(Network.PlayerDiedResponse object, AiClient aiClient) {

    }

    @Override
    public void onYouWon(AiClient aiClient) {

    }

    @Override
    public void onChatMessage(Network.ChatMessageResponse object, AiClient aiClient) {
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
            aiClient.gameClient.sendTCP(message);
        }
    }
}

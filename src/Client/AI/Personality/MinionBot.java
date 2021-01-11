package Client.AI.Personality;

import Client.AI.AiClient;
import Client.AI.Brain.BaseBotBrain;
import Client.AI.Brain.MoveCandidate;
import Common.*;
import javafx.util.Pair;
import util.Point;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

class SireWishes {
    private int sire;
    private int focusing;
    private Point nextAttack;

    public int getFocusing() {
        return focusing;
    }

    public void setFocusing(int focusing) {
        this.focusing = focusing;
    }

    public Point getNextAttack() {
        return nextAttack;
    }

    public void setNextAttack(Point nextAttack) {
        this.nextAttack = nextAttack;
    }

    public int getSire() {
        return sire;
    }

    public void setSire(int sire) {
        this.sire = sire;
    }
}

public class MinionBot implements AIPersonality {

    private final BaseBotBrain brain;
    private boolean justOneLeft = false;
    private String lastMessage;
    private SireWishes sireWishes;

    public MinionBot() {
        brain = new BaseBotBrain();
    }

    @Override
    public BotPersonality getPersonality() {
        return BotPersonality.Minion;
    }

    @Override
    public String introductionMessage() {
        return "MinionBot\n" +
                "Please tell me what to do and I shall do so!" +
                "(Unless we're the last ones alive, in that case, I'm sorry but I answer to no man)\n" +
                "Commands:" + "\n" +
                "\t {help, focus} with 1 -> I will help you with enemy number 1!\n" +
                "\t {help, focus} with 2 (1:2) -> I will attack 2 at position 1:2, if possible. " +
                "Afterwards, I will focus fire 2!";
    }

    @Override
    public void onCanStart(Network.StartGameResponse startGameResponse, AiClient aiClient) {
        Network.Participant[] otherParticipants = aiClient.getOtherParticipants();
        justOneLeft = otherParticipants.length == 1;
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

        Network.Participant[] otherParticipants = aiClient.getOtherParticipants();
        PlayerBoard[] boards = aiClient.getPlayerBoards();

        Random random = new Random();

        int index;
        Point nextAttack;

        StringBuilder builder = new StringBuilder();

        if (justOneLeft) {
            index = random.nextInt(otherParticipants.length);
            List<MoveCandidate> moves = brain.calculateMoves(boards[index]);
            moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
            MoveCandidate move = moves.get(0);
            nextAttack = move.attackedPoint;

            builder.append("Sorry. I have to attack you now @ ")
                    .append(nextAttack);

            if (sireWishes != null && sireWishes.getSire() == index) {
                builder.append("Sorry sire...\n");
            }

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
        } else if (sireWishes == null) {
            index = random.nextInt(otherParticipants.length);
            List<MoveCandidate> moves = brain.calculateMoves(boards[index]);
            moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
            MoveCandidate move = moves.get(0);
            nextAttack = move.attackedPoint;

            builder.append("Sorry. Had to attack you @ ")
                    .append(nextAttack)
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
        } else {
            nextAttack = sireWishes.getNextAttack();
            index = sireWishes.getFocusing();

            String after = "";

            if (nextAttack == null || boards[index].getAttacked(nextAttack) == HitResult.Invalid) {

                List<MoveCandidate> moves = brain.calculateMoves(boards[index]);
                moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
                MoveCandidate move = moves.get(0);

                if (aiClient.isDebug()) {
                    after = "DEBUG: \n";
                    after += "Selected move was";
                    after += move;
                    after += "\n";
                    after += "Considered moves were: ";
                    after += moves;
                    after += "\n";

                    if (nextAttack != null) {
                        after += "I couldn't do what sire said :( BUUUT I still attacked who he said";
                    } else {
                        after += "I attacked the best target I thought of!";
                    }
                }

                nextAttack = move.attackedPoint;

            } else {
                if (aiClient.isDebug()) {
                    after = "DEBUG: \n" +
                            "I did what my sire told me!";
                }
            }

            sireWishes.setNextAttack(null);

            builder.append("How do you like that attack @ ")
                    .append(nextAttack)
                    .append("\n")
                    .append("My sire rules")
                    .append("\n")
                    .append(after);
        }

        aiClient.sendMessageTo(builder.toString(), index);

        if (sireWishes != null) {
            aiClient.sendMessageTo("Sire, I have a report from my attack of " + index + ":\n" +
                    builder.toString(), sireWishes.getSire());
        }

        return new Pair<>(index, nextAttack);
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

    private Optional<SireWishes> lookForWishes(String message, AiClient aiClient) {

        final String[] keywords = {
                "focus",
                "help with"
        };

        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                Pattern focusPersonPattern = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
                Matcher matcher = focusPersonPattern.matcher(message);

                if (!matcher.find()) {
                    return Optional.empty();
                }

                SireWishes sireWishes = new SireWishes();

                int slot = Integer.parseInt(matcher.group());

                if (slot == aiClient.slot) {
                    return Optional.empty();
                }

                sireWishes.setFocusing(slot);

                Pattern attackPointPattern = Pattern.compile("\\d+:\\d+", Pattern.CASE_INSENSITIVE);
                matcher = attackPointPattern.matcher(message);

                if (matcher.find()) {
                    String asString = matcher.group();
                    String[] numbers = asString.split(":");

                    if (numbers.length == 2) {
                        sireWishes.setNextAttack(new Point(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1])));
                    }

                }

                return Optional.of(sireWishes);
            }
        }

        return Optional.empty();
    }

    @Override
    public void onChatMessage(Network.ChatMessageResponse object, AiClient aiClient) {
        if (lastMessage != null && lastMessage.equals(object.message)) {
            return;
        }

        lastMessage = object.message;
        Optional<SireWishes> maybeWishes = lookForWishes(lastMessage, aiClient);

        if (maybeWishes.isPresent() && !justOneLeft) {
            if (sireWishes == null) {
                sireWishes = maybeWishes.get();
                sireWishes.setSire(object.saidIt);

                aiClient.sendMessageTo("You are my new sire! As you command\n", object.saidIt);

            } else if (sireWishes.getSire() == object.saidIt) {
                sireWishes = maybeWishes.get();
                sireWishes.setSire(object.saidIt);

                aiClient.sendMessageTo("As you wish, sire\n", object.saidIt);

            } else {
                aiClient.sendMessageTo("I will not betray my sire!!!\n", object.saidIt);
            }
        }
    }
}

package Client.AI;

import Common.*;
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

public class MinionBot extends AIPersonality {

    private boolean justOneLeft = false;
    private String lastMessage;
    private SireWishes sireWishes;
    private PlayerBoard[] playerBoards;
    private Network.ConnectedPlayersResponse connectedPlayersResponse;

    public MinionBot() {
        super(BotPersonality.Minion);
    }

    @Override
    public void OnIsFull() {

    }

    @Override
    public void OnAbort() {

    }

    @Override
    public void OnCanStart(Network.StartGameResponse startGameResponse) {
        playerBoards = new PlayerBoard[startGameResponse.boards.length + 1];

        for (int i = 0; i < startGameResponse.boards.length; i++) {
            playerBoards[startGameResponse.indices[i]] = startGameResponse.boards[i].toPlayerBoard();
        }

        justOneLeft = startGameResponse.boards.length == 1;

        if (justOneLeft) {
            return;
        }

        for (int i = 0; i < startGameResponse.boards.length; i++) {
            Network.ChatMessage message = new Network.ChatMessage();
            message.to = i;

            final String[] instructions = {
                    "I am a good boy. Will do what you say :)\n",
                    "Tell me who we are focusing, I can't act alone D:\n",
                    "Tell me what point to attack, and I will do so!\n",
            };

            final int index = new Random().nextInt(instructions.length);

            message.text = instructions[index];
            client.gameClient.sendTCP(message);
        }

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

        Network.ChatMessage message = new Network.ChatMessage();
        Network.AnAttack attack;

        if (sireWishes == null || justOneLeft) {
            IntStream slots = new Random().ints(0, connectedPlayersResponse.participants.length);
            final int index = slots.filter(slot -> slot != connectedPlayersResponse.slot).findFirst().orElseThrow();

            List<MoveCandidate> moves = brain.calculateMoves(playerBoards[index]);
            moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
            MoveCandidate move = moves.get(0);

            attack = new Network.AnAttack();
            attack.toAttackID = index;
            attack.at = move.attackedPoint;

            final boolean debug = true;

            StringBuilder builder = new StringBuilder()
                    .append("How do you like that attack @ ")
                    .append(move.attackedPoint)
                    .append("\n");

            if (justOneLeft) {
                builder.append("Sorry");
            } else {
                builder.append("I'm lost D: don't know what to do!! Panic attacked you!");
            }

            builder.append("\n");

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

            message.text = builder.toString();
            message.to = index;

        } else {
            Point nextAttack = sireWishes.getNextAttack();
            final int index = sireWishes.getFocusing();

            if (nextAttack == null || playerBoards[index].getAttacked(nextAttack) == HitResult.Invalid) {

                List<MoveCandidate> moves = brain.calculateMoves(playerBoards[index]);
                moves.sort(Comparator.comparingInt((MoveCandidate m) -> m.score).reversed());
                MoveCandidate move = moves.get(0);

                attack = new Network.AnAttack();
                attack.toAttackID = index;
                attack.at = move.attackedPoint;

                final boolean debug = true;

                StringBuilder builder = new StringBuilder()
                        .append("How do you like that attack @ ")
                        .append(move.attackedPoint)
                        .append("\n");

                if (justOneLeft) {
                    builder.append("Sorry");
                } else {
                    builder.append("I'm lost D: don't know what to do!! Panic attacked you!");
                }

                builder.append("\n");

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

                message.text = builder.toString();
            } else {
                sireWishes.setNextAttack(null);

                attack = new Network.AnAttack();
                attack.toAttackID = index;
                attack.at = nextAttack;

                StringBuilder builder = new StringBuilder()
                        .append("How do you like that attack @ ")
                        .append(nextAttack)
                        .append("\n");

                builder.append("My master rules");
                builder.append("\n");

                message.text = builder.toString();
            }

            message.to = index;
        }

        try {
            Thread.sleep(1000);

            client.gameClient.sendTCP(message);
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

    private Optional<SireWishes> lookForWishes(String message) {

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
                sireWishes.setFocusing(Integer.parseInt(matcher.group()));

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
    public void OnChatMessage(Network.ChatMessageResponse object) {
        if (lastMessage != null && lastMessage.equals(object.message)) {
            return;
        }

        lastMessage = object.message;
        Optional<SireWishes> maybeWishes = lookForWishes(lastMessage);

        if (maybeWishes.isPresent() && !justOneLeft) {
            if (sireWishes == null || sireWishes.getSire() == object.saidIt) {
                sireWishes = maybeWishes.get();
                sireWishes.setSire(object.saidIt);
                Network.ChatMessage message = new Network.ChatMessage();
                message.to = object.saidIt;
                message.text = "As you wish, sire\n";
                client.gameClient.sendTCP(message);
            } else {
                Network.ChatMessage message = new Network.ChatMessage();
                message.to = object.saidIt;
                message.text = "I will not betray my sire!!!\n";
                client.gameClient.sendTCP(message);
            }
        }
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {

    }
}

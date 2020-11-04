package Client.AI;

import Common.*;
import util.Point;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
//
//class MoveCandidate {
//
//    public final Direction direction;
//    public final int offset;
//    public final Point attackedPoint;
//
//    public MoveCandidate(Direction direction, int offset, Point origin) {
//        this.direction = direction;
//        this.offset = offset;
//        this.attackedPoint = origin.moved(direction.vector.scaled(offset));
//    }
//}
//
//class FocusedBotBrain {
//
//    Point firstContact;
//    Map<Direction, List<MoveCandidate>> candidatesByDirection;
//
//    public FocusedBotBrain() {
//    }
//
//    private void removeAfterSameDirection(MoveCandidate candidate) {
//        candidates.removeIf(c -> c.direction == candidate.direction && c.offset > candidate.offset);
//    }
//
//    private void filterCandidatesByShipSize(int shipSize) {
//        candidates.removeIf(c -> c.offset + 1 > shipSize);
//    }
//
//    public void react(PlayerBoard playerBoard, Point point, AttackResult result) {
//
//        switch (result.status) {
//            case HitWater -> {
//
//                Optional<MoveCandidate> maybeCandidate = candidates.stream()
//                        .filter(mv -> mv.attackedPoint.equals(point))
//                        .findFirst();
//
//                if (maybeCandidate.isPresent()) {
//                    MoveCandidate candidate = maybeCandidate.get();
//                    candidate.confirmWater();
//                    removeAfterSameDirection(candidate);
//                }
//
//            }
//
//            case HitShipPiece -> {
//
//                final int maxPossibleShipSize = playerBoard.ships.stream()
//                        .filter(Predicate.not(Ship::isDestroyed))
//                        .mapToInt(ship -> ship.shipType.value)
//                        .max()
//                        .orElseThrow();
//
//                if (firstContact == null) {
//                    if (result.destroyedShip) {
//                        return;
//                    }
//
//                    firstContact = point;
//
//                    for (Direction direction : Direction.values()) {
//                        Point current = firstContact.moved(direction.vector);
//                        for (int i = 1; i < maxPossibleShipSize && playerBoard.inBounds(current); ++i) {
//                            candidates.add(new MoveCandidate(direction, i, firstContact));
//                        }
//                    }
//
//                    return;
//                }
//
//                if (result.destroyedShip) {
//                    ShipPiece piece = (ShipPiece) playerBoard.getTileAt(firstContact);
//                    if (piece.status() == ShipPieceStatus.AttackedShipDestroyed) {
//                        firstContact = null;
//                        candidates.clear();
//                        return;
//                    }
//
//                    filterCandidatesByShipSize(maxPossibleShipSize);
//                    candidates.removeIf(c -> !playerBoard.canAttackAt(c.attackedPoint));
//                    return;
//                }
//
//                Optional<MoveCandidate> maybeCandidate = candidates.stream()
//                        .filter(mv -> mv.attackedPoint.equals(point))
//                        .findFirst();
//
//                if (maybeCandidate.isPresent()) {
//                    MoveCandidate candidate = maybeCandidate.get();
//                    candidates.remove(candidate);
//
//                }
//            }
//        }
//    }
//
//    public AiMove nextMove(PlayerBoard playerBoard) {
//
//        if (possibleMoves.size() == 0) {
//            List<Point> possible = playerBoard.getAvailable();
//            int index = new Random().nextInt(possible.size());
//            int directionIndex = new Random().nextInt(Direction.values().length);
//            return new AiMove(Direction.values()[directionIndex], possible.get(index));
//        }
//
//        int index = new Random().nextInt(possibleMoves.size());
//        return possibleMoves.get(index);
//    }
//
//    public List<AiMove> getPossibleMoves() {
//        return possibleMoves;
//    }
//}

public class FocusedBot extends Client.AI.AIPersonality {

//    private final FocusedBotBrain brain;
    private String lastMessage;
    private int focusing;
//    private AiMove move;
    private PlayerBoard[] playerBoards;
    private Network.ConnectedPlayers connectedPlayers;

    public FocusedBot() {
        super(BotPersonality.Focused);
//        this.brain = new FocusedBotBrain();
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
            playerBoards[canStart.indices[i]] = canStart.boards[i].toPlayerBoard();
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
        playerBoards[object.id] = object.newAttackedBoard.toPlayerBoard();
    }

    @Override
    public void OnAnAttackResponse(Network.AnAttackResponse object) {

        if (object.attacked != connectedPlayers.slot) {
            playerBoards[object.attacked] = object.newAttackedBoard.toPlayerBoard();

            if (object.attacked == focusing) {
                //brain.react(playerBoards[focusing], move, object.attackResult);
            }

        }

    }

    @Override
    public void OnYourTurn() {
//        move = brain.nextMove(playerBoards[focusing]);
//
//        Network.AnAttackAttempt attack = new Network.AnAttackAttempt();
//        attack.toAttackID = focusing;
//        attack.l = move.point.x;
//        attack.c = move.point.y;
//
//        try {
//            Network.ChatMessageFromClient chatMessageFromClient = new Network.ChatMessageFromClient();
//
//            chatMessageFromClient.text =
//                    "Gonna attacked you with " +
//                            move +
//                            " out of: " +
//                            brain.getPossibleMoves().toString() +
//                            "\n";
//
//            chatMessageFromClient.to = focusing;
//
//            ai.gameClient.sendTCP(chatMessageFromClient);
//
//            Thread.sleep(500);
//            ai.gameClient.sendTCP(attack);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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

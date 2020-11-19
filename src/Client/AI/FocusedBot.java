package Client.AI;

import Common.*;
import util.Point;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;


class MoveCandidate {

    public final Point attackedPoint;
    public final int score;

    public MoveCandidate(Point origin, int score) {
        this.attackedPoint = origin;
        this.score = score;
    }

    @Override
    public String toString() {
        return "MoveCandidate{" +
                "attackedPoint=" + attackedPoint +
                ", score=" + score +
                '}';
    }
}

class FocusedBotBrain {

    Point firstContact;

    public FocusedBotBrain() {
    }

    public List<MoveCandidate> calculateMoves(PlayerBoard playerBoard) {

        if (firstContact != null) {
            BoardTile tile = playerBoard.getTileAt(firstContact);

            firstContact = playerBoard.isShipDestroyed(tile.getShip()) ? null : firstContact;
        }

        if (firstContact == null) {
            List<BoardTile> shipPieces = playerBoard.getTilesWithShipPieces();

            Optional<BoardTile> possiblyNewFirstContact = shipPieces.stream()
                    .filter(Predicate.not(BoardTile::isAttackable))
                    .filter(tile -> !playerBoard.isShipDestroyed(tile.getShip()))
                    .findFirst();

            possiblyNewFirstContact.ifPresent(boardTile -> firstContact = boardTile.point);
        }

        if (firstContact == null) {
            List<BoardTile> possible = playerBoard.getAttackableTiles();
            int index = new Random().nextInt(possible.size());
            BoardTile chosen = possible.get(index);
            return List.of(new MoveCandidate(chosen.point, 1));
        }

        final int maxShipSize = playerBoard.getShips()
                .stream()
                .map(s -> s.size)
                .max(Integer::compareTo)
                .orElseThrow();

        List<MoveCandidate> moveCandidates = new ArrayList<>();

        Direction foundDirection = null;

        for (Direction direction : Direction.values()) {

            if (foundDirection != null && direction != foundDirection.opposite) {
                continue;
            }

            int inThisDirection = 1;
            int inARow = 1;
            Point current = firstContact.moved(direction.vector);
            for (int i = 1; i < maxShipSize && playerBoard.inBounds(current); i++) {
                BoardTile tile = playerBoard.getTileAt(current);

                if (tile.isAttackable()) {
                    MoveCandidate moveCandidate = new MoveCandidate(current, inARow + inThisDirection);
                    moveCandidates.add(moveCandidate);
                    inARow = 0;
                } else {
                    if (tile.containsShipPiece()) {
                        inARow++;
                        inThisDirection++;
                        if (inARow > 1) {
                            foundDirection = direction;
                        }
                    } else {
                        break;
                    }
                }

                current = current.moved(direction.vector);
            }
        }

        return moveCandidates;
    }

}

public class FocusedBot extends Client.AI.AIPersonality {

    private final FocusedBotBrain brain;
    private String lastMessage;
    private int focusing;
    private PlayerBoard[] playerBoards;
    private Network.ConnectedPlayersResponse connectedPlayersResponse;

    public FocusedBot() {
        super(BotPersonality.Focused);
        this.brain = new FocusedBotBrain();
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
        focusing = 0;

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
        ai.gameClient.sendTCP(message);
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
        MoveCandidate move = moves.stream().max(Comparator.comparingInt(m -> m.score)).orElseThrow();

        Network.AnAttack attack = new Network.AnAttack();
        attack.toAttackID = focusing;
        attack.at = move.attackedPoint;

        try {
            Network.ChatMessage chatMessage = new Network.ChatMessage();

            chatMessage.text =
                    "Attacked you with " +
                            move +
                            "(considered were: " + moves + ")" +
                            "\n";

            chatMessage.to = focusing;

            ai.gameClient.sendTCP(chatMessage);

            Thread.sleep(1000);
            ai.gameClient.sendTCP(attack);
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
            ai.gameClient.sendTCP(message);
        }
    }

    @Override
    public void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse) {

    }
}

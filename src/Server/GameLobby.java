package Server;

import Common.*;
import com.esotericsoftware.kryonet.Connection;
import util.Point;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GameLobby {

    public final int code;
    public final LobbyParticipant[] participants;
    private final Conversations conversations;
    private int currentPlayerSlot;
    private GameLobbyState state;

    public GameLobby(GameServer gameServer, int count, Connection connection) {
        this.participants = new LobbyParticipant[count];
        this.state = GameLobbyState.InLobby;
        this.conversations = new Conversations(count);
        this.currentPlayerSlot = 0;

        // FIXME: Do we ever need codes?
        this.code = 5;
        addPlayer(0, connection);
    }

    public GameLobbyState getState() {
        return state;
    }

    public int playersInLobby() {
        return (int) Arrays.stream(this.participants).filter(Objects::nonNull).count();
    }

    public int slots() {
        return this.participants.length;
    }

    public boolean isFull() {
        return playersInLobby() == slots();
    }

    public void addPlayer(int slot, Connection connection) {
        this.participants[slot] = new PlayerLobbyParticipant(connection, connection.toString());
        notifyPlayersInLobby();
    }

    public void addBot(int slot, String name, BotPersonality difficulty, Connection connection) {
        this.participants[slot] = new BotLobbyParticipant(difficulty, name, connection);
        notifyPlayersInLobby();
    }

    public void removeParticipant(int slot) {
        this.participants[slot] = null;
        notifyPlayersInLobby();
    }

    public Optional<Integer> getSlotOf(Connection connection) {

        for (int i = 0, participantsLength = participants.length; i < participantsLength; i++) {
            LobbyParticipant participant = participants[i];

            if (participant.connection == connection) {
                return Optional.of(i);
            }
        }

        System.err.println("No slot found for " + connection);

        return Optional.empty();
    }

    public void resetLobby() {
        state = GameLobbyState.InLobby;
    }

    public void startGame() {
        state = GameLobbyState.InGame;

        List<Integer> allIndexes = IntStream.range(0, participants.length)
                .boxed()
                .collect(Collectors.toList());

        for (int i = 0; i < participants.length; i++) {

            List<Integer> otherIndexes = new ArrayList<>(allIndexes);

            otherIndexes.remove(Integer.valueOf(i));

            Stream<PlayerBoardMessage> otherBoards = otherIndexes
                    .stream()
                    .map(n -> participants[n].getPlayerBoard())
                    .map(PlayerBoardMessage::new);

            Network.StartGameResponse startGameResponse = new Network.StartGameResponse();

            startGameResponse.boards = otherBoards.toArray(PlayerBoardMessage[]::new);
            startGameResponse.indices = otherIndexes.stream().mapToInt(n -> n).toArray();

            sendTo(startGameResponse, i);
        }

        currentPlayerSlot = 0;
        notifyTurn();
    }

    public void onAnAttack(int attackerSlot, Network.AnAttack anAttack) {

        final int attackedSlot = anAttack.toAttackID;
        final Point attackedPoint = anAttack.at;

        final LobbyParticipant attackedParticipant = participants[attackedSlot];
        final PlayerBoard attackedPlayerBoard = attackedParticipant.getPlayerBoard();

        HitResult hitResult = attackedPlayerBoard.getAttacked(attackedPoint);

        final Network.AnAttackResponse response = new Network.AnAttackResponse(
                new PlayerBoardMessage(attackedPlayerBoard),
                hitResult,
                attackedSlot,
                attackedPoint
        );

        sendToAll(response, false);

        switch (hitResult) {
            case HitWater -> {
                currentPlayerSlot = nextTurnSlot();
                notifyTurn();
            }
            case Invalid -> notifyTurn();
            case HitPiece -> {
                if (isGameOverFor(attackedSlot)) {
                    sendTo(new Network.YouDeadResponse(), attackedSlot);
                    attackedParticipant.setDefeated(true);

                    sendToAllExcept(new Network.PlayerDiedResponse(attackedSlot), attackedSlot, true);

                    if (isGameOver()) {
                        sendTo(new Network.YouWonResponse(), attackerSlot);
                    }
                }
                notifyTurn();
            }
        }

    }

    public boolean isGameOver() {
        return Arrays.stream(participants)
                .filter(Predicate.not(LobbyParticipant::isBot))
                .allMatch(LobbyParticipant::isDefeated);
    }

    public boolean isGameOverFor(int id) {
        return participants[id].getPlayerBoard().isGameOver();
    }

    public void readyForBoardCommits() {
        state = GameLobbyState.SettingShips;
        sendToAll(new Network.ReadyForShipsResponse(), true);
    }

    public void onPlayerCommitBoard(int slot, Network.PlayerCommitBoard playerCommitBoard) {
        participants[slot].setPlayerBoard(playerCommitBoard.playerBoardMessage.toPlayerBoard());
        notifyPlayersBoardSet();

        if (areAllBoardsSet()) {
            startGame();
        }

    }

    private void notifyTurn() {
        Network.WhoseTurnResponse whoseTurnResponse = new Network.WhoseTurnResponse(currentPlayerSlot);

        sendToAllExcept(whoseTurnResponse, currentPlayerSlot, false);
        sendTo(new Network.YourTurnResponse(), currentPlayerSlot);
    }

    public boolean areAllBoardsSet() {
        return Arrays.stream(participants).map(LobbyParticipant::getPlayerBoard).noneMatch(Objects::isNull);
    }

    private <T> void sendToAllExcept(T message, int slot, boolean includeDefeated) {
        for (int i = 0; i < participants.length; i++) {
            if (i == slot) {
                continue;
            }

            final LobbyParticipant participant = participants[i];

            if (!includeDefeated && participant.isDefeated()) {
                continue;
            }

            participants[i].connection.sendTCP(message);
        }
    }

    private <T> void sendToAll(T message, boolean includeDefeated) {
        for (final LobbyParticipant participant : participants) {

            if (!includeDefeated && participant.isDefeated()) {
                continue;
            }

            participant.connection.sendTCP(message);
        }
    }

    private <T> void sendTo(T message, int slot) {
        final LobbyParticipant participant = participants[slot];
        participant.connection.sendTCP(message);
    }

    private int nextTurnSlot() {

        for (int nextTurnSlot = (currentPlayerSlot + 1) % participants.length;
             nextTurnSlot != currentPlayerSlot;
             nextTurnSlot = (nextTurnSlot + 1) % participants.length) {

            final LobbyParticipant participant = participants[nextTurnSlot];

            if (participant.isDefeated()) {
                continue;
            }

            return nextTurnSlot;
        }

        return -1;
    }

    private void notifyPlayersInLobby() {
        Network.ConnectedPlayersResponse connectedPlayersResponse = new Network.ConnectedPlayersResponse();
        connectedPlayersResponse.participants = new Network.Participant[participants.length];

        for (int i = 0; i < participants.length; ++i) {
            final LobbyParticipant participant = participants[i];

            if (participant == null) {
                connectedPlayersResponse.participants[i] = null;
                continue;
            }

            if (participant.isBot()) {
                final BotLobbyParticipant asBot = (BotLobbyParticipant) participant;
                connectedPlayersResponse.participants[i] = new Network.Participant(asBot.difficulty, asBot.name, i);
            } else {
                connectedPlayersResponse.participants[i] = new Network.Participant(participant.name, i);
            }

        }

        for (int i = 0; i < participants.length; ++i) {
            if (participants[i] != null) {
                connectedPlayersResponse.slot = i;
                sendTo(connectedPlayersResponse, i);
            }
        }
    }

    private void notifyPlayersBoardSet() {
        Network.PlayersSetBoardResponse connectedPlayersResponse = new Network.PlayersSetBoardResponse();
        connectedPlayersResponse.participants = new Network.Participant[participants.length];
        connectedPlayersResponse.boardSet = new boolean[participants.length];

        for (int i = 0; i < participants.length; ++i) {
            final LobbyParticipant participant = participants[i];

            if (participant.isBot()) {
                final BotLobbyParticipant asBot = (BotLobbyParticipant) participant;
                connectedPlayersResponse.participants[i] = new Network.Participant(asBot.difficulty, asBot.name, i);
            } else {
                connectedPlayersResponse.participants[i] = new Network.Participant(participant.name, i);
            }

            connectedPlayersResponse.boardSet[i] = participant.getPlayerBoard() != null;
        }

        sendToAll(connectedPlayersResponse, false);
    }

    public void onStartLobby() {
        if (!isFull()) {
            System.err.println("Not isFull and you're starting???????");
            return;
        }

        readyForBoardCommits();
    }

    public void onJoinLobby(Connection connection, Network.JoinLobby joinLobby) {
        if (isFull()) {
            connection.sendTCP(new Network.LobbyIsFullResponse());
            return;
        }

        final int slot = playersInLobby();

        Network.JoinLobbyResponse response = new Network.JoinLobbyResponse(slots());
        connection.sendTCP(response);

        addPlayer(slot, connection);
    }

    public void onChatMessageFromClient(Connection connection, Network.ChatMessage message) {
        final int from = getSlotOf(connection).orElseThrow();
        final int to = message.to;

        final Conversations.Conversation conversation = conversations.getConversationBetweenSlots(from, to);
        Conversations.Line line = conversation.add(from, message.text);

        Network.ChatMessageResponse chats = new Network.ChatMessageResponse();
        chats.saidIt = from;
        chats.message = line.decode(participants[from].name);

        System.out.println(chats.saidIt + " -> " + to + ":\n" + chats.message + "\n");
        participants[to].connection.sendTCP(chats);
    }

    public String toString() {
        return "GameLobby{" +
                "code=" + code +
                ", participants=" + Arrays.toString(participants) +
                ", state=" + state +
                '}';
    }
}

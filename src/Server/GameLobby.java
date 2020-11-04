package Server;

import Common.*;
import com.esotericsoftware.kryonet.Connection;
import util.Point;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class GameLobby implements ILobby, IGameManager, IBoardSetup {

    public final int code;
    public final LobbyParticipant[] participants;
    public final PlayerBoard[] playerBoards;
    private GameLobbyState state;

    public GameLobby(int count) {
        this(5, count);
    }

    public GameLobby(int code, int count) {
        this.code = code;
        this.participants = new LobbyParticipant[count];
        this.playerBoards = new PlayerBoard[count];
        this.state = GameLobbyState.InLobby;
    }

    public GameLobbyState getState() {
        return state;
    }

    @Override
    public int playersAlive() {
        return (int) Arrays.stream(this.playerBoards).filter(p -> !p.isGameOver()).count();
    }

    @Override
    public int playersInLobby() {
        return (int) Arrays.stream(this.participants).filter(Objects::nonNull).count();
    }

    @Override
    public int slots() {
        return this.participants.length;
    }

    @Override
    public boolean full() {
        return playersInLobby() == slots();
    }

    @Override
    public void addPlayer(int slot, Connection connection) {
        this.participants[slot] = new PlayerLobbyParticipant(connection, connection.toString());
    }

    @Override
    public void addBot(int slot, String name, BotPersonality difficulty, Connection connection) {
        this.participants[slot] = new BotLobbyParticipant(difficulty, name, connection);
    }

    @Override
    public void removeParticipant(int slot) {
        this.participants[slot] = null;
    }

    @Override
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

    @Override
    public void transitionToLobby() {
        state = GameLobbyState.InLobby;
    }

    @Override
    public void transitionToGame() {
        state = GameLobbyState.InGame;
    }

    @Override
    public boolean attack(int id, int x, int y) {
        Optional<Boolean> canGoAgain = playerBoards[id].getAttacked(new Point(x, y));
        return canGoAgain.isEmpty() || canGoAgain.get();
    }

    @Override
    public boolean gameIsOver() {
        return Arrays.stream(playerBoards).allMatch(PlayerBoard::isGameOver);
    }

    @Override
    public boolean isGameOverFor(int id) {
        return playerBoards[id].isGameOver();
    }

    @Override
    public void transitionToShips() {
        state = GameLobbyState.SettingShips;
    }

    public void setGameBoardOfPlayer(int gameID, PlayerBoardMessage playerBoardMessage) {
        playerBoards[gameID] = playerBoardMessage.toPlayerBoard();
    }

    @Override
    public boolean allBoardsSet() {
        return Arrays.stream(playerBoards).noneMatch(Objects::isNull);
    }
}

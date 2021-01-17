package Server;

import Common.PlayerBoard;

public class LobbyParticipant {

    private final Player player;
    private boolean defeated;
    private PlayerBoard playerBoard;

    public LobbyParticipant(Player player) {
        this.player = player;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public PlayerBoard getPlayerBoard() {
        return playerBoard;
    }

    public void setPlayerBoard(PlayerBoard playerBoard) {
        this.playerBoard = playerBoard;
    }

    public Player getPlayer() {
        return player;
    }
}

package Client;

import Common.Network;

public interface IClient {
    void onIsFull();

    void onAbort();

    void onCanStart(Network.StartGameResponse startGameResponse);

    void onWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse);

    void onConnectedPlayers(Network.ConnectedPlayersResponse connectedPlayersResponse);

    void onReadyForShips();

    void onYourBoardToPaint(Network.YourBoardResponse object);

    void onEnemyBoardToPaint(Network.EnemyBoardResponse object);

    void onAnAttackResponse(Network.AnAttackResponse object);

    void onYourTurn();

    void onYouDead();

    void onPlayerDied(Network.PlayerDiedResponse object);

    void onYouWon();

    void onChatMessage(Network.ChatMessageResponse object);

    void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse);
}

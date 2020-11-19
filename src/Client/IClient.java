package Client;

import Common.Network;

public interface IClient {
    void OnIsFull();

    void OnAbort();

    void OnCanStart(Network.StartGameResponse startGameResponse);

    void OnWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse);

    void onConnectedPlayers(Network.ConnectedPlayersResponse connectedPlayersResponse);

    void OnReadyForShips();

    void OnYourBoardToPaint(Network.YourBoardResponse object);

    void OnEnemyBoardToPaint(Network.EnemyBoardResponse object);

    void OnAnAttackResponse(Network.AnAttackResponse object);

    void OnYourTurn();

    void OnYouDead();

    void OnPlayerDied(Network.PlayerDiedResponse object);

    void OnYouWon();

    void OnChatMessage(Network.ChatMessageResponse object);

    void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse);
}

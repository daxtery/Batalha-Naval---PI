package Client;

import Common.Network;

public interface IClient {
    void OnIsFull();

    void OnAbort();

    void OnCanStart();

    void OnWhoseTurn(Network.WhoseTurn whoseTurn);

    void onConnectedPlayers(Network.ConnectedPlayers connectedPlayers);

    void OnReadyForShips();

    void OnOtherSpecs(Network.OthersSpecs object);

    void OnYourBoardToPaint(Network.YourBoardToPaint object);

    void OnEnemiesBoardsToPaint(Network.EnemiesBoardsToPaint object);

    void OnEnemyBoardToPaint(Network.EnemyBoardToPaint object);

    void OnAnAttackResponse(Network.AnAttackResponse object);

    void OnYourTurn();

    void OnYouDead();

    void OnPlayerDied(Network.PlayerDied object);

    void OnYouWon();

    void OnChatMessage(Network.ChatMessage object);

    void onJoinLobbyResponse(Network.JoinLobbyResponse joinLobbyResponse);
}

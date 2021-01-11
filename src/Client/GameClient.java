package Client;

import Common.Network;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public class GameClient {

    private final Client client;
    private final IClient clientApplication;

    public GameClient(IClient clientApplication) {
        this.clientApplication = clientApplication;
        client = new Client(1000000, 1000000);
        Network.register(client);

        client.addListener(new Listener() {

            public void received(Connection connection, Object object) {

                if (object instanceof Network.LobbyIsFullResponse) {
                    GameClient.this.clientApplication.onIsFull();
                } else if (object instanceof Network.AbortResponse) {
                    GameClient.this.clientApplication.onAbort();
                } else if (object instanceof Network.JoinLobbyResponse) {
                    GameClient.this.clientApplication.onJoinLobbyResponse((Network.JoinLobbyResponse) object);
                } else if (object instanceof Network.StartGameResponse) {
                    GameClient.this.clientApplication.onCanStart((Network.StartGameResponse) object);
                } else if (object instanceof Network.WhoseTurnResponse) {
                    GameClient.this.clientApplication.onWhoseTurn((Network.WhoseTurnResponse) object);
                } else if (object instanceof Network.ConnectedPlayersResponse) {
                    GameClient.this.clientApplication.onConnectedPlayers((Network.ConnectedPlayersResponse) object);
                } else if (object instanceof Network.ReadyForShipsResponse) {
                    GameClient.this.clientApplication.onReadyForShips();
                } else if (object instanceof Network.YourBoardResponse) {
                    GameClient.this.clientApplication.onYourBoardToPaint((Network.YourBoardResponse) object);
                } else if (object instanceof Network.EnemyBoardResponse) {
                    GameClient.this.clientApplication.onEnemyBoardToPaint((Network.EnemyBoardResponse) object);
                } else if (object instanceof Network.AnAttackResponse) {
                    GameClient.this.clientApplication.onAnAttackResponse((Network.AnAttackResponse) object);
                } else if (object instanceof Network.YourTurnResponse) {
                    GameClient.this.clientApplication.onYourTurn();
                } else if (object instanceof Network.YouDeadResponse) {
                    GameClient.this.clientApplication.onYouDead();
                } else if (object instanceof Network.PlayerDiedResponse) {
                    GameClient.this.clientApplication.onPlayerDied((Network.PlayerDiedResponse) object);
                } else if (object instanceof Network.YouWonResponse) {
                    GameClient.this.clientApplication.onYouWon();
                } else if (object instanceof Network.ChatMessageResponse) {
                    GameClient.this.clientApplication.onChatMessage((Network.ChatMessageResponse) object);
                }
            }
        });
    }

    public void tryConnect(String address, int port) throws IOException {
        client.connect(5000, address, port);
    }

    public void sendTCP(Object object) {
        client.sendTCP(object);
    }

    public void start() {
        client.start();
    }

    public void stop() {
        client.stop();
    }
}

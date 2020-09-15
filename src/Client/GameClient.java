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
        client = new Client();
        Network.register(client);

        client.addListener(new Listener() {

            public void received(Connection connection, Object object) {

                if (object instanceof Network.IsFull) {
                    GameClient.this.clientApplication.OnIsFull();
                } else if (object instanceof Network.Abort) {
                    GameClient.this.clientApplication.OnAbort();
                } else if (object instanceof Network.JoinLobbyResponse) {
                    GameClient.this.clientApplication.onJoinLobbyResponse((Network.JoinLobbyResponse) object);
                } else if (object instanceof Network.CanStart) {
                    GameClient.this.clientApplication.OnCanStart();
                } else if (object instanceof Network.WhoseTurn) {
                    GameClient.this.clientApplication.OnWhoseTurn((Network.WhoseTurn) object);
                } else if (object instanceof Network.ConnectedPlayers) {
                    GameClient.this.clientApplication.onConnectedPlayers((Network.ConnectedPlayers) object);
                } else if (object instanceof Network.ReadyForShips) {
                    GameClient.this.clientApplication.OnReadyForShips();
                } else if (object instanceof Network.OthersSpecs) {
                    GameClient.this.clientApplication.OnOtherSpecs((Network.OthersSpecs) object);
                } else if (object instanceof Network.YourBoardToPaint) {
                    GameClient.this.clientApplication.OnYourBoardToPaint((Network.YourBoardToPaint) object);
                } else if (object instanceof Network.EnemiesBoardsToPaint) {
                    GameClient.this.clientApplication.OnEnemiesBoardsToPaint((Network.EnemiesBoardsToPaint) object);
                } else if (object instanceof Network.EnemyBoardToPaint) {
                    GameClient.this.clientApplication.OnEnemyBoardToPaint((Network.EnemyBoardToPaint) object);
                } else if (object instanceof Network.AnAttackResponse) {
                    GameClient.this.clientApplication.OnAnAttackResponse((Network.AnAttackResponse) object);
                } else if (object instanceof Network.YourTurn) {
                    GameClient.this.clientApplication.OnYourTurn();
                } else if (object instanceof Network.YouDead) {
                    GameClient.this.clientApplication.OnYouDead();
                } else if (object instanceof Network.PlayerDied) {
                    GameClient.this.clientApplication.OnPlayerDied((Network.PlayerDied) object);
                } else if (object instanceof Network.YouWon) {
                    GameClient.this.clientApplication.OnYouWon();
                } else if (object instanceof Network.ChatMessage) {
                    GameClient.this.clientApplication.OnChatMessage((Network.ChatMessage) object);
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

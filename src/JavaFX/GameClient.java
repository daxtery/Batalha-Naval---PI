package JavaFX;

import Common.Network;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class GameClient {

    private final Client client;
    private final App app;

    public GameClient(App app) {
        this.app = app;
        client = new Client();
        client.start();

        Network.register(client);

        client.addListener(new Listener() {

            public void received(Connection connection, Object object) {

                if (object instanceof Network.IsFull) {
                    GameClient.this.app.OnIsFull();
                } else if (object instanceof Network.Abort) {
                    GameClient.this.app.OnAbort();
                } else if (object instanceof Network.CanStart) {
                    GameClient.this.app.OnCanStart();
                } else if (object instanceof Network.WhoseTurn) {
                    GameClient.this.app.OnWhoseTurn((Network.WhoseTurn) object);
                } else if (object instanceof Network.ConnectedPlayers) {
                    GameClient.this.app.OnConnectedPlayers((Network.ConnectedPlayers) object);
                } else if (object instanceof Network.ReadyForShips) {
                    GameClient.this.app.OnReadyForShips();
                } else if (object instanceof Network.OthersSpecs) {
                    GameClient.this.app.OnOtherSpecs((Network.OthersSpecs) object);
                } else if (object instanceof Network.YourBoardToPaint) {
                    GameClient.this.app.OnYourBoardToPaint((Network.YourBoardToPaint) object);
                } else if (object instanceof Network.EnemiesBoardsToPaint) {
                    GameClient.this.app.OnEnemiesBoardsToPaint((Network.EnemiesBoardsToPaint) object);
                } else if (object instanceof Network.EnemyBoardToPaint) {
                    GameClient.this.app.OnEnemyBoardToPaint((Network.EnemyBoardToPaint) object);
                } else if (object instanceof Network.AnAttackResponse) {
                    GameClient.this.app.OnAnAttackResponse((Network.AnAttackResponse) object);
                } else if (object instanceof Network.YourTurn) {
                    GameClient.this.app.OnYourTurn();
                } else if (object instanceof Network.YouDead) {
                    GameClient.this.app.OnYouDead();
                } else if (object instanceof Network.PlayerDied) {
                    GameClient.this.app.OnPlayerDied((Network.PlayerDied) object);
                } else if (object instanceof Network.YouWon) {
                    GameClient.this.app.OnYouWon();
                } else if (object instanceof Network.ChatMessage) {
                    GameClient.this.app.OnChatMessage((Network.ChatMessage) object);
                }
            }
        });
    }

    private static InetAddress getMeIPV4() throws UnknownHostException {
        try {
            InetAddress closestOneFound = null;
            // GET AND ITERATE ALL NETWORK CARDS
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // GO THROUGH ALL IP ADDRESSES OF THIS CARD...
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.isSiteLocalAddress())
                            //YEAH BOY, FOUND IT
                            return inetAddress;
                        else if (closestOneFound == null)
                            //FOUND ONE, MIGHT NOT BE WHAT WE WANT
                            //BUT LET'S STORE IT IN CASE WE DON'T FIND EXACTLY WHAT WE WANT
                            closestOneFound = inetAddress;
                    }
                }
            }
            if (closestOneFound != null)
                // NOT IPV4, but will have to do
                return closestOneFound;
            //FOUND NOTHING
            //WILL TRY THE JDK ONE
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null)
                throw new UnknownHostException("The JDK INetAddress.getLocalHost() method unexpectedly returned null.");
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN ADDRESS: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    public Client getNative() {
        return client;
    }

    public Network.Register tryConnect(String name, String address, int port) throws IOException {
        client.connect(5000, address, port);
        Network.Register register = new Network.Register();
        register.name = name;
        client.sendTCP(register);
        return register;
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

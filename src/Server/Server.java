package Server;

import Common.Network;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {
        new GameServer(Network.port).start();
    }

}

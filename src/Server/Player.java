package Server;

import com.esotericsoftware.kryonet.Connection;


public class Player {

    private final String code;
    private final boolean isBot;

    private Connection connection;

    public Player(String code, boolean isBot, Connection connection) {
        this.code = code;
        this.connection = connection;
        this.isBot = isBot;
    }

    public String getCode() {
        return code;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getName() {
        return connection.toString();
    }

    public boolean isBot() {
        return isBot;
    }

    @Override
    public String toString() {
        return getCode() + " ** " + getName();
    }
}

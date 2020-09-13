package Client.Scenes;

import Common.Network;
import Client.App;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class WaitingForPlayersScene extends BaseGameScene {

    private final TextArea textArea;

    public WaitingForPlayersScene(App app) {
        super(app, new BorderPane());

        BorderPane root = (BorderPane) getRoot();
        textArea = new TextArea();
        textArea.setEditable(false);
        root.setCenter(textArea);
    }

    public void OnConnectedPlayers(Network.ConnectedPlayers players) {
        textArea.clear();
        for (String name : players.names) {
            textArea.appendText(name + "\n");
        }
    }

    @Override
    public void OnSceneSet() {

    }

    @Override
    public void OnSceneUnset() {

    }
}
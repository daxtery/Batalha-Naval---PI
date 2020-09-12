package JavaFX.Scenes;

import Common.Network;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;

public class WaitingForPlayersScene extends Scene {

    private final TextArea textArea;

    public WaitingForPlayersScene() {
        super(new BorderPane(),
                Screen.getPrimary().getVisualBounds().getWidth(),
                Screen.getPrimary().getVisualBounds().getHeight()
        );

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
}
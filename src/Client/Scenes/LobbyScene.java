package Client.Scenes;

import Client.App;
import Common.BotDifficulty;
import Common.Network;
import Server.LobbyParticipant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class LobbyScene extends BaseGameScene {

    private int count;
    private boolean admin;
    private List<Label> labels;
    private List<Button> addButtons;
    private List<Button> removeButtons;
    private Button startButton;

    public LobbyScene(App app) {
        super(app, new GridPane());

        GridPane grid = (GridPane) getRoot();
        labels = new ArrayList<>();
        addButtons = new ArrayList<>();
        removeButtons = new ArrayList<>();

        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Lobby");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        startButton = new Button("Start");
        startButton.setOnMouseClicked(event -> {
            app.onLobbyStartButtonClicked();
        });
    }

    public void setNumber(int count, boolean admin) {
        this.count = count;
        this.admin = admin;
        GridPane grid = (GridPane) getRoot();
        grid.getChildren().clear();
        labels.clear();
        addButtons.clear();
        removeButtons.clear();

        if (admin) {
            for (int i = 0; i < count; i++) {
                Button addBotButton = new Button("+");

                final int slot = i;
                addBotButton.setOnMouseClicked(event -> app.onAddBotButton(slot, BotDifficulty.Normal));

                addButtons.add(addBotButton);
                grid.add(addBotButton, 0, i + 1);

                Label userName = new Label("<EMPTY> Slot #" + i);
                labels.add(userName);
                grid.add(userName, 1, i + 1);

                Button removeButton = new Button("x");
                removeButton.setOnMouseClicked(event -> app.onRemovePlayerButton(slot));

                removeButtons.add(removeButton);
                grid.add(removeButton, 2, i + 1);
            }
        } else {
            for (int i = 0; i < count; i++) {
                Label userName = new Label("<EMPTY> Slot #" + i);
                labels.add(userName);
                grid.add(userName, 0, i + 1);
            }
        }


    }

    @Override
    public void OnSceneSet() {
    }

    @Override
    public void OnSceneUnset() {

    }

    public void onConnectedPlayers(Network.ConnectedPlayers players) {
        Network.Participant[] playerLobbyParticipants = players.participants;

        for (int i = 0, playerLobbyParticipantsLength = playerLobbyParticipants.length; i < playerLobbyParticipantsLength; i++) {
            Network.Participant participant = playerLobbyParticipants[i];
            if (admin) {
                addButtons.get(i).setVisible(false);
                removeButtons.get(i).setVisible(i != 0);
            }
            labels.get(i).setText(participant.toString());
        }

        if (admin) {
            for (int i = playerLobbyParticipants.length; i < count; i++) {
                addButtons.get(i).setVisible(true);
                removeButtons.get(i).setVisible(false);
            }
        }

        if (!admin) {
            return;
        }

        GridPane grid = ((GridPane) getRoot());

        if (playerLobbyParticipants.length == count) {
            grid.add(startButton, 0, this.count + 1);
        } else {
            grid.getChildren().remove(startButton);
        }
    }
}
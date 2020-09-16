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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

        grid.setAlignment(Pos.TOP_LEFT);
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

        for (int i = 0; i < count; i++) {
            Label userName = new Label();
            labels.add(userName);
            grid.add(userName, 1, i + 1);
        }

        if (admin) {
            for (int i = 0; i < count; i++) {
                Button addBotButton = new Button("+");

                final int slot = i;
                addBotButton.setOnMouseClicked(event -> app.onAddBotButton(slot, BotDifficulty.Normal));

                addButtons.add(addBotButton);
                grid.add(addBotButton, 0, i + 1);

                Button removeButton = new Button("x");
                removeButton.setOnMouseClicked(event -> app.onRemovePlayerButton(slot));

                removeButtons.add(removeButton);
                grid.add(removeButton, 2, i + 1);
            }
            grid.add(startButton, 1, this.count + 1);
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

            if (participant == null) {
                labels.get(i).setText("Slot #" + i);
                if (admin) {
                    addButtons.get(i).setVisible(true);
                    removeButtons.get(i).setVisible(false);
                }
                continue;
            }

            labels.get(i).setText(participant.toString());
            if (admin) {
                addButtons.get(i).setVisible(false);
                removeButtons.get(i).setVisible(i != 0);
            }
        }

        if (!admin) {
            return;
        }

        startButton.setDisable(Arrays.stream(playerLobbyParticipants).filter(Objects::nonNull).count() != count);
    }
}
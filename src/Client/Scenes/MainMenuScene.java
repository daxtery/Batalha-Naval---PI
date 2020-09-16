package Client.Scenes;

import Client.App;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class MainMenuScene extends BaseGameScene {

    public MainMenuScene(App app) {
        super(app, new BorderPane());

        TextField nameInput = new TextField("Name!");

        nameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            final int maxLength = 10;

            if (newValue.length() > maxLength) {
                String s = newValue.substring(0, maxLength);
                nameInput.setText(s);
            }
        });

        getRoot().setStyle("-fx-background-image: url(images/BattleShip.png);-fx-background-size: cover;");

        Button multiplayerButton = new Button();
        Image mMPlayButtonImage = new Image("images/Botao_Start.png");
        multiplayerButton.setGraphic(new ImageView(mMPlayButtonImage));

        multiplayerButton.setOnAction(event -> {

            String name = nameInput.getText();

            if (name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ups!");
                alert.setHeaderText("Name was null!");
                alert.setContentText("Name can't be null, we need to know who you are :(");
                alert.showAndWait();
                return;
            }

            app.onJoinButtonPressed(name);
        });

        multiplayerButton.setStyle("-fx-background-color: transparent;");

        Button createLobbyButton = new Button();
        Image createLobbyImage = new Image("images/Botao_Solo_Play.png");
        createLobbyButton.setGraphic(new ImageView(createLobbyImage));

        createLobbyButton.setOnAction(event -> {

            String name = nameInput.getText();

            if (name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ups!");
                alert.setHeaderText("Name was null!");
                alert.setContentText("Name can't be null, we need to know who you are :(");
                alert.showAndWait();
                return;
            }

            final int maxPlayers = 3;
            final int minPlayers = 2;

            Spinner<Integer> spinner = new Spinner<>(minPlayers, maxPlayers, maxPlayers);

            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Create Lobby");

            // Set the button types.
            ButtonType okButtonType = new ButtonType("Set", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Number of players:"), 0, 0);
            grid.add(spinner, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return spinner.getValue();
                }
                return null;
            });

            Optional<Integer> result = dialog.showAndWait();
            result.ifPresent(slots -> app.onCreateLobbyButton(name, slots));
        });

        createLobbyButton.setStyle("-fx-background-color: transparent;");

        Button mMExit = new Button();
        Image mMExitButtonImage = new Image("images/Botao_Exit.png");
        mMExit.setGraphic(new ImageView(mMExitButtonImage));
        mMExit.setOnAction(event ->
                Platform.exit()
        );

        mMExit.setStyle("-fx-background-color: transparent;");

        GridPane mMMiddle = new GridPane();
        mMMiddle.add(nameInput, 0, 1);
        mMMiddle.add(multiplayerButton, 0, 2);
        mMMiddle.add(createLobbyButton, 1, 2);
        mMMiddle.add(mMExit, 0, 3);
        mMMiddle.setStyle("-fx-fill: true; -fx-alignment:bottom-center; -fx-padding: 50");

        ((BorderPane) getRoot()).setCenter(mMMiddle);
    }

    @Override
    public void OnSceneSet() {

    }

    @Override
    public void OnSceneUnset() {

    }
}
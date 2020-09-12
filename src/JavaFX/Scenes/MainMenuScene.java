package JavaFX.Scenes;

import JavaFX.App;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;

public class MainMenuScene extends Scene {

    public MainMenuScene(App app) {
        super(new BorderPane(),
                Screen.getPrimary().getVisualBounds().getWidth(),
                Screen.getPrimary().getVisualBounds().getHeight()
        );

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

            app.OnMultiplayerButtonPressed(name);
        });

        multiplayerButton.setStyle("-fx-background-color: transparent;");

        Button mMAloneButton = new Button();
        Image mMAloneButtonImage = new Image("images/Botao_Solo_Play.png");
        mMAloneButton.setGraphic(new ImageView(mMAloneButtonImage));
        mMAloneButton.setOnAction(event -> app.OnSoloButtonPressed());

        mMAloneButton.setStyle("-fx-background-color: transparent;");

        Button mMExit = new Button();
        Image mMExitButtonImage = new Image("images/Botao_Exit.png");
        mMExit.setGraphic(new ImageView(mMExitButtonImage));
        mMExit.setOnAction(event ->
                Platform.exit()
        );

        mMExit.setStyle("-fx-background-color: transparent;");

        GridPane mMMiddle = new GridPane();
        mMMiddle.add(multiplayerButton, 0, 2);
        mMMiddle.add(mMAloneButton, 1, 2);
        mMMiddle.add(mMExit, 1, 3);
        mMMiddle.add(nameInput, 0, 1);
        mMMiddle.setStyle("-fx-fill: true; -fx-alignment:bottom-center; -fx-padding: 50");

        ((BorderPane) getRoot()).setCenter(mMMiddle);
    }
}
package JavaFX.Scenes;

import Common.PlayerBoard;
import Common.PlayerBoardFactory;
import JavaFX.App;
import JavaFX.ShipsBoardFX;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;
import static Common.PlayerBoardFactory.DEFAULT_SIZES;

public class SetShipsScene extends BaseGameScene {

    private final ShipsBoardFX sSboard;
    private PlayerBoard pb;

    public SetShipsScene(App app) {
        super(app, new HBox());

        getRoot().setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        pb = new PlayerBoard(DEFAULT_LINES, DEFAULT_COLUMNS);
        sSboard = new ShipsBoardFX(pb.lines(), pb.columns(), 700, 500);

        sSboard.setPlayerBoard(pb);
        sSboard.startAnimating();

        VBox sSRightStuff = new VBox();

        HBox sSPlaceIntructions = new HBox();
        sSPlaceIntructions.setStyle("-fx-background-color: grey;");

        VBox sSTips = new VBox();

        //TIPS
        sSTips.setSpacing(5);
        sSTips.getChildren().add(new Text("Hey!"));
        sSTips.getChildren().add(new Text(" + Right-Mouse to select/deselect a ship"));
        sSTips.getChildren().add(new Text(" + R while a ship is selected to rotate it"));
        sSTips.getChildren().add(new Text(" + Left-Mouse to confirm the selected ship's position"));

        {
            final HBox pane = new HBox();
            final Text red = new Text("Green");
            red.setFill(Color.GREEN);

            pane.getChildren().addAll(
                    new Text(" + "),
                    red,
                    new Text(" means it can be placed there.")
            );

            sSTips.getChildren().add(pane);
        }

        {
            final HBox pane = new HBox();
            final Text red = new Text("Red");
            red.setFill(Color.RED);

            pane.getChildren().addAll(
                    new Text(" + "),
                    red,
                    new Text(" means it can't be placed there"));

            sSTips.getChildren().add(pane);
        }

        sSPlaceIntructions.getChildren().addAll(sSTips);

        HBox sSReadyBox = new HBox();
        sSReadyBox.setStyle("-fx-background-color: yellow;");

        Button sSRandomButton = new Button("Random");
        sSRandomButton.setFont(new Font(50));
        sSRandomButton.setOnMouseClicked(event -> {
            pb = PlayerBoardFactory.getRandomPlayerBoard();
            sSboard.initShips(pb);
        });

        Button sSReadyButton = new Button("Ready");
        sSReadyButton.setFont(new Font(50));

        sSReadyButton.setOnMouseClicked(event -> {
            if (pb.ships.size() == DEFAULT_SIZES.length) {
                sSReadyButton.setDisable(true);
                sSRandomButton.setDisable(true);

                app.SignalReady(pb);
            }
        });

        sSReadyBox.getChildren().addAll(sSRandomButton, sSReadyButton);

        VBox sSInstructionsGame = new VBox();
        sSInstructionsGame.setStyle("-fx-background-color: grey;");
        sSInstructionsGame.setSpacing(5);
        sSInstructionsGame.getChildren().add(new Text("Instructions: "));
        sSInstructionsGame.getChildren().add(new Text(
                " +When you hit a ship piece, you get to go again"));
        sSInstructionsGame.getChildren().add(new Text(" +Missing means it is now somebody else's turn"));
        sSInstructionsGame.getChildren().add(new Text(" +If you destroy a ship, surrounding area will be shown"));

        sSRightStuff.getChildren().addAll(sSPlaceIntructions, sSReadyBox, sSInstructionsGame);

        sSboard.setOnMouseMoved(sSboard::OnMouseMoved);
        sSboard.setOnMouseClicked(sSboard::OnMouseClicked);

        HBox sSRoot = (HBox) getRoot();

        sSRoot.getChildren().addAll(sSboard, sSRightStuff);
        sSRoot.setPadding(new Insets(25));
        sSRoot.setSpacing(10);

        sSRoot.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                sSboard.OnRotateKeyPressed();
            }
        });
    }

    @Override
    public void OnSceneSet() {
        sSboard.startAnimating();
    }

    @Override
    public void OnSceneUnset() {
        sSboard.stopAnimating();
    }
}
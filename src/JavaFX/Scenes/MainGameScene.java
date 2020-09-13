package JavaFX.Scenes;

import Common.Network;
import Common.PlayerBoard;
import Common.PlayerBoardFactory;
import Common.PlayerBoardTransformer;
import JavaFX.App;
import JavaFX.FX.SelfGraphBoardFX;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;

public class MainGameScene extends BaseGameScene {

    private final SelfGraphBoardFX mGSelfBoard;
    private final Label mGcurrentPlayerText;

    public MainGameScene(App app) {
        super(app, new BorderPane());

        BorderPane MGRoot = (BorderPane) getRoot();
        MGRoot.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        //GridPane gridPane = new GridPane();
        //gridPane.setStyle("-fx-background-color:cyan");

        Group MGCanvasHolder = new Group();

        mGSelfBoard = new SelfGraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, 500, 500);

        mGSelfBoard.startTiles(
                PlayerBoardTransformer.transform(PlayerBoardFactory.getRandomPlayerBoard())
        );

        MGCanvasHolder.getChildren().add(mGSelfBoard);

        StackPane MGTop = new StackPane();

        mGcurrentPlayerText = new Label("IS PLAYING");
        mGcurrentPlayerText.setFont(new Font(30));

        MGTop.getChildren().add(mGcurrentPlayerText);

        VBox MGRight = new VBox(50);

        Button MGAttackButton = new Button("TO ARMS");
        MGAttackButton.setOnMouseClicked(event -> app.OnAttackButtonPressed());

        Button MGChatButton = new Button("CHAT");
        MGChatButton.setOnMouseClicked(event -> app.OnChatButtonPressed());

        VBox.setVgrow(MGAttackButton, Priority.ALWAYS);
        VBox.setVgrow(MGChatButton, Priority.ALWAYS);

        MGRight.getChildren().addAll(MGAttackButton, MGChatButton);

        MGRoot.setRight(MGRight);
        MGRoot.setTop(MGTop);
        MGRoot.setCenter(MGCanvasHolder);
    }

    public void setPlayerBoard(PlayerBoard playerBoard) {
        mGSelfBoard.setPlayerBoard(playerBoard);
        String[][] message = PlayerBoardTransformer.transform(playerBoard);
        mGSelfBoard.startTiles(message);
        mGSelfBoard.updateTiles(message);
    }


    @Override
    public void OnSceneSet() {
        mGSelfBoard.startAnimating();
    }

    @Override
    public void OnSceneUnset() {
        mGSelfBoard.stopAnimating();
    }

    public void OnWhoseTurn(Network.WhoseTurn whoseTurn) {
        mGcurrentPlayerText.setText(whoseTurn.name);
    }

    public void OnYourBoardToPaint(Network.YourBoardToPaint toPaint) {
        mGSelfBoard.updateTiles((toPaint).board);
    }

    public void OnYourTurn() {
        mGcurrentPlayerText.setText("Your turn");
    }
}
package Client.Scenes;

import Client.FX.PlayerBoardFX;
import Client.FX.Tile;
import Common.*;
import Client.App;
import Client.FX.SelfGraphBoardFX;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import util.Point;

import java.util.HashMap;
import java.util.Map;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;

public class MainGameScene extends BaseGameScene {

    static final Border highlighted = new Border(
            new BorderStroke(
                    Color.RED,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderStroke.DEFAULT_WIDTHS
            )
    );

    private final SelfGraphBoardFX mGSelfBoard;
    private final VBox turnQueue;
    private final Map<Integer, StackPane> turnPanes;
    private final TabPane tabs;
    private StackPane previousTurnPane;

    public MainGameScene(App app) {
        super(app, new BorderPane());

        BorderPane root = (BorderPane) getRoot();
        root.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        HBox hBox = new HBox();

        Group canvasWrapper = new Group();

        mGSelfBoard = new SelfGraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, 500, 500);

        mGSelfBoard.startTiles(
                PlayerBoardTransformer.transform(PlayerBoardFactory.getRandomPlayerBoard())
        );

        canvasWrapper.getChildren().add(mGSelfBoard);

        turnQueue = new VBox();
        turnQueue.setSpacing(25);

        turnPanes = new HashMap<>();

        getStylesheets().add("css/mainGame.css");

        tabs = new TabPane();

        Tab allTab = new Tab("All", new Label("All"));
        tabs.getTabs().add(allTab);

        HBox.setMargin(canvasWrapper, new Insets(10, 10, 50, 50));
        HBox.setMargin(turnQueue, new Insets(10, 10, 50, 50));
        HBox.setMargin(tabs, new Insets(10, 10, 50, 50));

        hBox.getChildren().addAll(canvasWrapper, turnQueue, tabs);

        Pane chat = new Pane();
        chat.setStyle("-fx-background:rgb(120,26,155); -fx-border-color: green");

        root.setCenter(hBox);
        root.setBottom(chat);
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

        if (previousTurnPane != null) {
            previousTurnPane.setBorder(Border.EMPTY);
        }

        StackPane turnPane = turnPanes.get(whoseTurn.index);
        turnPane.setBorder(highlighted);

        previousTurnPane = turnPane;
    }

    public void OnYourBoardToPaint(Network.YourBoardToPaint toPaint) {
        mGSelfBoard.updateTiles((toPaint).board);
    }

    public void OnYourTurn() {

        if (previousTurnPane != null) {
            previousTurnPane.setBorder(Border.EMPTY);
        }

        StackPane turnPane = turnPanes.get(app.getPlayers().slot);
        turnPane.setBorder(highlighted);

        previousTurnPane = turnPane;
    }

    public void setupWithPlayers(Network.ConnectedPlayers players) {

        Network.Participant[] participants = players.participants;
        for (int i = 0, participantsLength = participants.length; i < participantsLength; i++) {
            Network.Participant participant = participants[i];

            StackPane stackPane = new StackPane();
            Text text = new Text(participant.name);
            text.setTextAlignment(TextAlignment.JUSTIFY);
            text.setFill(Color.WHITE);

            stackPane.getChildren().addAll(text);

            turnPanes.put(i, stackPane);
            turnQueue.getChildren().add(stackPane);
        }
    }

    public void onCanStart(Network.CanStart canStart) {
        Network.ConnectedPlayers connected = app.getPlayers();

        TilePane allPane = new TilePane();
        allPane.setId("k");
        allPane.setVgap(10);
        allPane.setHgap(10);

        ScrollPane allPaneWrapper = new ScrollPane();
        allPaneWrapper.setContent(allPane);

        String[][][] boards = canStart.boards;

        for (int i = 0, boardsLength = boards.length; i < boardsLength; i++) {
            String[][] boardString = boards[i];
            int index = canStart.indices[i];

            Label name = new Label(connected.participants[index].name);
            PlayerBoardFX board = new PlayerBoardFX(boardString.length, boardString[0].length, true);

            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(board);
            borderPane.setTop(name);

            borderPane.setStyle("-fx-border-color: black");
            allPane.getChildren().add(borderPane);
        }

        Tab allTab = tabs.getTabs().get(0);
        allTab.setContent(allPaneWrapper);

        for (int i = 0, boardsLength = boards.length; i < boardsLength; i++) {
            String[][] boardString = boards[i];
            int index = canStart.indices[i];

            FlowPane flowPane = new FlowPane();

            PlayerBoardFX board = new PlayerBoardFX(boardString.length, boardString[0].length, true);

            Tab tab = new Tab(connected.participants[index].name, new Label(connected.participants[index].name));
            tabs.getTabs().add(tab);

            VBox chat = new VBox();

            TextArea conversation = new TextArea();
            conversation.setEditable(false);

            TextArea message = new TextArea();
            message.setEditable(true);
            message.setOnKeyPressed(k -> {
                if (k.getCode() == KeyCode.ENTER) {
                    app.SendMessage(index, message.getText());
                    conversation.appendText("You: " + message.getText());
                    message.clear();
                }
            });

            Label label = new Label("Send new message: ");
            chat.getChildren().addAll(conversation, label, message);

            flowPane.setHgap(25);

            flowPane.getChildren().addAll(board, chat);
            tab.setContent(flowPane);
        }
    }

}
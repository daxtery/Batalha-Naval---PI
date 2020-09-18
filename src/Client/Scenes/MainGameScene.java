package Client.Scenes;

import Client.FX.PlayerBoardFX;
import Common.*;
import Client.App;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

public class MainGameScene extends BaseGameScene {

    private final Group canvasWrapper;
    private final Map<Integer, PlayerBoardFX> otherBoardsInteractive;
    private final Map<Integer, PlayerBoardFX> otherBoards;
    private final TurnQueue turnQueue;
    private final Map<Integer, TextArea> conversations;
    private final TabPane tabs;
    private PlayerBoardFX myBoard;
    private boolean allowAttacks;

    public MainGameScene(App app) {
        super(app, new BorderPane());

        BorderPane root = (BorderPane) getRoot();
        root.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        HBox hBox = new HBox();

        canvasWrapper = new Group();

        turnQueue = new TurnQueue();
        turnQueue.setSpacing(25);

        otherBoardsInteractive = new HashMap<>();
        otherBoards = new HashMap<>();
        conversations = new HashMap<>();

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
        myBoard = new PlayerBoardFX(playerBoard.lines(), playerBoard.columns(), false);
        myBoard.setBoard(playerBoard);

        canvasWrapper.getChildren().add(myBoard);
    }

    @Override
    public void OnSceneSet() {
//        mGSelfBoard.startAnimating();
    }

    @Override
    public void OnSceneUnset() {
//        mGSelfBoard.stopAnimating();
    }

    public void OnWhoseTurn(Network.WhoseTurn whoseTurn) {


        turnQueue.highlightPlayer(whoseTurn.index);

    }

    public void OnYourBoardToPaint(Network.YourBoardToPaint toPaint) {
        myBoard.setBoard(PlayerBoardTransformer.parse(toPaint.board));
    }

    public void OnYourTurn() {
        turnQueue.highlightPlayer(app.getPlayers().slot);
        allowAttacks = true;
    }

    public void setupWithPlayers(Network.ConnectedPlayers players) {
        Network.Participant[] participants = players.participants;
        for (int i = 0, participantsLength = participants.length; i < participantsLength; i++) {
            turnQueue.addPlayer(i, participants[i]);
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
            board.setBoard(PlayerBoardTransformer.parse(boardString));

            otherBoards.put(index, board);

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
            board.setBoard(PlayerBoardTransformer.parse(boardString));
            board.setOnLocationAttacked(p -> {
                if (!allowAttacks) {
                    return;
                }

                allowAttacks = false;

                Network.AnAttackAttempt anAttackAttempt = new Network.AnAttackAttempt();

                anAttackAttempt.l = p.x;
                anAttackAttempt.c = p.y;

                anAttackAttempt.toAttackID = index;

                app.CommunicateAttackAttempt(anAttackAttempt);
            });

            otherBoardsInteractive.put(index, board);

            Tab tab = new Tab(connected.participants[index].name, new Label(connected.participants[index].name));
            tabs.getTabs().add(tab);

            VBox chat = new VBox();

            TextArea conversation = new TextArea();
            conversation.setEditable(false);
            conversations.put(index, conversation);

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

    public void onEnemyBoardToPaint(Network.EnemyBoardToPaint board) {
        PlayerBoard playerBoard = PlayerBoardTransformer.parse(board.newAttackedBoard);
        otherBoardsInteractive.get(board.id).setBoard(playerBoard);
        otherBoards.get(board.id).setBoard(playerBoard);
    }

    public void OnAttackResponse(Network.AnAttackResponse attackResponse) {
        PlayerBoard playerBoard = PlayerBoardTransformer.parse(attackResponse.newAttackedBoard);

        otherBoardsInteractive.get(attackResponse.attacked).setBoard(playerBoard);
        otherBoards.get(attackResponse.attacked).setBoard(playerBoard);
    }

    public void onPlayerDied(Network.PlayerDied playerDied) {
        turnQueue.removePlayer(playerDied.who);
    }

    public void onChatMessage(Network.ChatMessage chatMessage) {
        TextArea conversation = conversations.get(chatMessage.saidIt);
        conversation.appendText(chatMessage.message);
    }

    private static class TurnQueue extends VBox {
        static final Border highlighted = new Border(
                new BorderStroke(
                        Color.RED,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.DEFAULT_WIDTHS
                )
        );
        private final Map<Integer, StackPane> turnPanes;
        private StackPane previousTurnPane;

        private TurnQueue() {
            turnPanes = new HashMap<>();
        }

        void addPlayer(int slot, Network.Participant participant) {
            StackPane stackPane = new StackPane();
            Text text = new Text(participant.name);
            text.setTextAlignment(TextAlignment.JUSTIFY);
            text.setFill(Color.WHITE);

            stackPane.getChildren().addAll(text);

            turnPanes.put(slot, stackPane);
            getChildren().add(stackPane);
        }

        void removePlayer(int slot) {
            StackPane pane = turnPanes.remove(slot);
            getChildren().remove(pane);
        }

        void highlightPlayer(int slot) {
            if (previousTurnPane != null) {
                previousTurnPane.setBorder(Border.EMPTY);
            }

            StackPane turnPane = turnPanes.get(slot);
            turnPane.setBorder(highlighted);

            previousTurnPane = turnPane;
        }
    }
}
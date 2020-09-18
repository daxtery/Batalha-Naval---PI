package Client.Scenes;

import Client.FX.PlayerBoardFX;
import Common.*;
import Client.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

    private final Map<Integer, PlayerBoardFX> tabBoards;

    private final TurnQueue turnQueue;
    private final Map<Integer, Chat> chats;

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
        turnQueue.setAlignment(Pos.CENTER);
        turnQueue.setSpacing(25);

        tabBoards = new HashMap<>();

        chats = new HashMap<>();

        getStylesheets().add("css/mainGame.css");

        tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        HBox.setMargin(canvasWrapper, new Insets(10, 10, 50, 50));
        HBox.setMargin(turnQueue, new Insets(10, 10, 50, 50));
        HBox.setMargin(tabs, new Insets(10, 10, 50, 50));

        hBox.getChildren().addAll(canvasWrapper, turnQueue, tabs);

        root.setTop(turnQueue);
        root.setCenter(hBox);
    }

    public void setPlayerBoard(PlayerBoard playerBoard) {
        myBoard = new PlayerBoardFX(playerBoard.lines(), playerBoard.columns(), false);
        myBoard.setBoard(playerBoard);

        canvasWrapper.getChildren().add(myBoard);
    }

    @Override
    public void OnSceneSet() {
    }

    @Override
    public void OnSceneUnset() {
    }

    public void OnWhoseTurn(Network.WhoseTurn whoseTurn) {
        turnQueue.highlightPlayer(whoseTurn.index);
        allowAttacks = false;
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
        String[][][] boards = canStart.boards;

        for (int i = 0, boardsLength = boards.length; i < boardsLength; i++) {
            String[][] boardString = boards[i];
            int index = canStart.indices[i];

            Tab tab = new Tab(connected.participants[index].name);
            tabs.getTabs().add(tab);

            HBox tabHBoxContent = new HBox();

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

            tabBoards.put(index, board);

            Chat chat = new Chat(this.app, index);
            chats.put(index, chat);

            tabHBoxContent.setSpacing(25);
            tabHBoxContent.getChildren().addAll(board, chat);

            tab.setContent(tabHBoxContent);
        }
    }

    public void onEnemyBoardToPaint(Network.EnemyBoardToPaint board) {
        PlayerBoard playerBoard = PlayerBoardTransformer.parse(board.newAttackedBoard);
        tabBoards.get(board.id).setBoard(playerBoard);
    }

    public void OnAttackResponse(Network.AnAttackResponse attackResponse) {
        PlayerBoard playerBoard = PlayerBoardTransformer.parse(attackResponse.newAttackedBoard);
        tabBoards.get(attackResponse.attacked).setBoard(playerBoard);
    }

    public void onPlayerDied(Network.PlayerDied playerDied) {
        turnQueue.playerDied(playerDied.who);

        PlayerBoardFX boardFX = tabBoards.get(playerDied.who);
        boardFX.setOnLocationAttacked(l -> {
        });

        boardFX.setStyle("-fx-background-color: red");
    }

    public void onChatMessage(Network.ChatMessage chatMessage) {
        chats.get(chatMessage.saidIt).appendToConversation(chatMessage.message);
    }

    private static class TurnQueue extends HBox {
        static final Border highlighted = new Border(
                new BorderStroke(
                        Color.BLUE,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.DEFAULT_WIDTHS
                )
        );

        static final Border dead = new Border(
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

        void playerDied(int slot) {
            turnPanes.get(slot).setBorder(dead);
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

    private static class Chat extends VBox {

        private final TextArea conversation;

        public Chat(App app, int slot) {
            conversation = new TextArea();
            conversation.setEditable(false);

            TextArea message = new TextArea();
            message.setEditable(true);

            message.setOnKeyPressed(k -> {
                if (k.getCode() == KeyCode.ENTER) {
                    app.SendMessage(slot, message.getText());
                    conversation.appendText("You: " + message.getText());
                    message.clear();
                }
            });

            Label label = new Label("Send new message: ");
            label.setTextFill(Color.WHITE);

            getChildren().addAll(conversation, label, message);
        }

        public void appendToConversation(String message) {
            conversation.appendText(message);
        }
    }

}
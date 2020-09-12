package JavaFX;

import AI.AiMove;
import AI.MyAI;
import Common.*;
import JavaFX.Scenes.BaseGameScene;
import JavaFX.Scenes.MainMenuScene;
import JavaFX.Scenes.SetShipsScene;
import JavaFX.Scenes.WaitingForPlayersScene;
import util.Point;

import com.esotericsoftware.kryonet.Connection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import Common.Network.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;
import static Common.PlayerBoardFactory.DEFAULT_SIZES;

public class App extends Application {

    private static final String ADDRESS = "localhost";
    private final static String MM_IMAGE_BACKGROUND_PATH = "images/BattleShipBigger.png";
    private final static Image MM_IMAGE_BACKGROUND = new Image(MM_IMAGE_BACKGROUND_PATH);
    private final static BackgroundImage MM_BACKGROUND = new BackgroundImage(MM_IMAGE_BACKGROUND,
            BackgroundRepeat.REPEAT,
            BackgroundRepeat.REPEAT,
            null,
            new BackgroundSize(MM_IMAGE_BACKGROUND.getWidth(), MM_IMAGE_BACKGROUND.getHeight(),
                    false, false, true, true)
    );
    private final static Rectangle2D SCREEN_RECTANGLE = Screen.getPrimary().getVisualBounds();
    private final AudioClip soundPlayer = new AudioClip(new File("assets/sound/play.mp3").toURI().toString());
    //FOR OFFLINE
    private AIPlayer ai;
    private boolean vsAI;
    private SelfGraphBoardFX selfvsAI;
    //FOR ONLINE
    private GameClient client;
    private PlayerBoard pb;

    //region SET SHIPS STUFF
    private GridPane mMMiddle;
    private HBox sSRoot;
    private ShipsBoardFX sSboard;
    private VBox sSRightStuff;
    private HBox sSPlaceIntructions;
    private VBox sSTips;
    private HBox sSReadyBox;
    private Button sSRandomButton;
    private Button sSReadyButton;
    private VBox sSInstructionsGame;
    private Text sSPlayer1Ready;

    //endregion

    //region MAIN GAME STUFF
    private Text sSPlayer2Ready;
    private BorderPane MGRoot;
    private Group MGCanvasHolder;
    private SelfGraphBoardFX mGSelfBoard;
    private StackPane MGTop;
    private Label mGcurrentPlayerText;
    private VBox MGRight;
    private Circle MGShips;
    private Button MGAttackButton;

    //endregion

    //region ATTACK WINDOW STUFF
    private Button MGChatButton;
    private boolean iCanAttack;
    private EnemyLocal lastAttacked;
    private EnemyLocal ene1;
    private EnemyLocal ene2;
    private HBox aWRoot;
    private VBox aWvBox = new VBox(50);
    private VBox aWvBox2 = new VBox(50);
    private String aWshipSoundFile = "assets/sound/ship.mp3";
    private String aWwaterSoundFile = "assets/sound/water.mp3";
    private MediaPlayer aWShipSound = new MediaPlayer(new Media(new File(aWshipSoundFile).toURI().toString()));

    //endregion
    private MediaPlayer aWWaterSound = new MediaPlayer(new Media(new File(aWwaterSoundFile).toURI().toString()));
    private TextArea textArea;
    private Label cWl1;
    private Label cWl2;
    private ArrayList<EmptyGraphBoardFX> toAnimate = new ArrayList<>();

    //SCENES
    private Scene mainMenu;
    private Scene mainGame;
    private Scene setShips;
    private Scene attackScene;
    private Scene wonScene;
    private Scene chatScreen;
    private Scene waitingScreen;
    private Scene AIScene;

    private Stage theStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        client = new GameClient(this);
        client.start();
        Network.register(client.getNative());

        vsAI = false;

        theStage = primaryStage;
        setAllScenes();

        theStage.initStyle(StageStyle.UNDECORATED);
        theStage.setTitle("BS");
        theStage.setResizable(false);
        theStage.setMaximized(true);
        theStage.setScene(mainMenu);
        theStage.show();

        soundPlayer.setVolume(.2);
        soundPlayer.setCycleCount(AudioClip.INDEFINITE);
        soundPlayer.play();

        aWShipSound.setVolume(1);
        aWWaterSound.setVolume(.1);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        client.stop();
        System.exit(0);
    }

    private void reset() {
        setShips = new SetShipsScene(this);
    }

    private void lost(String s) {
        Alert lost = new Alert(Alert.AlertType.INFORMATION);
        lost.setContentText(s);
        lost.showAndWait();
        reset();
    }

    private void won() {
        reset();
        transitionTo(wonScene);
    }

    private void removeEnemy(int who) {
        if (who == ene2.serverID)
            aWRoot.getChildren().remove(aWvBox2);
        else
            aWRoot.getChildren().remove(aWvBox);
    }

    private void updateEnemyBoard(int id, String[][] newAttackedBoard) {
        EnemyLocal toUpdate = ene1;

        if (id == ene2.serverID) {
            toUpdate = ene2;
        }

        toUpdate.b.updateTiles(newAttackedBoard);
    }

    private void setAllScenes() {
        mainMenu = new MainMenuScene(this);
        setMainGame();
        setShips = new SetShipsScene(this);
        setAttackScreen();
        setChatScreen();
        setWonScene();
        setAIScene();
    }

    private void transitionTo(Scene scene) {
        for (EmptyGraphBoardFX g : toAnimate)
            g.stopAnimating();
        toAnimate.clear();

        Scene old = theStage.getScene();

        if (old instanceof BaseGameScene) {
            ((BaseGameScene) old).OnSceneUnset();
        }

        theStage.setScene(scene);

        if (scene instanceof BaseGameScene) {
            ((BaseGameScene) scene).OnSceneSet();
        }

        if (scene == mainGame)
            toAnimate.add(mGSelfBoard);
        if (scene == attackScene) {
            toAnimate.add(ene1.b);
            toAnimate.add(ene2.b);
        }
        if (false && scene == setShips)
            toAnimate.add(sSboard);
        if (scene == AIScene) {
            toAnimate.add(selfvsAI);
            toAnimate.add(ai.b);
        }
        for (EmptyGraphBoardFX g : toAnimate)
            g.startAnimating();
    }

    private void setMainGame() {

        MGRoot = new BorderPane();
        MGRoot.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        //GridPane gridPane = new GridPane();
        //gridPane.setStyle("-fx-background-color:cyan");

        MGCanvasHolder = new Group();

        mGSelfBoard = new SelfGraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, 500, 500);

        mGSelfBoard.startTiles(
                PlayerBoardTransformer.transform(PlayerBoardFactory.getRandomPlayerBoard())
        );

        MGCanvasHolder.getChildren().add(mGSelfBoard);

        MGTop = new StackPane();

        mGcurrentPlayerText = new Label("IS PLAYING");
        mGcurrentPlayerText.setFont(new Font(30));
        MGTop.getChildren().add(mGcurrentPlayerText);

        MGRight = new VBox(50);

        MGAttackButton = new Button("TO ARMS");
        MGAttackButton.setOnMouseClicked(event -> transitionTo(attackScene));
        MGChatButton = new Button("CHAT");
        MGChatButton.setOnMouseClicked(event -> transitionTo(chatScreen));

        VBox.setVgrow(MGAttackButton, Priority.ALWAYS);
        VBox.setVgrow(MGChatButton, Priority.ALWAYS);

        MGRight.getChildren().addAll(MGAttackButton, MGChatButton);

        MGRoot.setRight(MGRight);
        MGRoot.setTop(MGTop);
        MGRoot.setCenter(MGCanvasHolder);

        mainGame = new Scene(MGRoot, SCREEN_RECTANGLE.getWidth(), SCREEN_RECTANGLE.getHeight());
    }

    private void setTurnLabel(String name) {
        mGcurrentPlayerText.setText(name);
    }

    private void setAttackScreen() {

        ene1 = new EnemyLocal();
        ene2 = new EnemyLocal();
        lastAttacked = new EnemyLocal();
        iCanAttack = false;

        ene1.b = new GraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_LINES);
        ene2.b = new GraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_LINES);

        PlayerBoard board = PlayerBoardFactory.getRandomPlayerBoard();
        ene1.b.startTiles(PlayerBoardTransformer.transform(board));

        board = PlayerBoardFactory.getRandomPlayerBoard();
        ene2.b.startTiles(PlayerBoardTransformer.transform(board));

        ene1.b.startAnimating();
        ene2.b.startAnimating();

        ene1.labeln = new Label("ENEMY 1");
        ene1.labeln.setFont(new Font("Verdana", 30));
        ene1.labeln.setTextFill(Color.rgb(0, 0, 0));

        ene2.labeln = new Label("ENEMY 2");
        ene2.labeln.setFont(new Font("Verdana", 30));
        ene2.labeln.setTextFill(Color.rgb(0, 0, 0));

        aWvBox = new VBox(10);
        aWvBox.getChildren().addAll(ene1.b, ene1.labeln);

        aWvBox2 = new VBox(10);
        aWvBox2.getChildren().addAll(ene2.b, ene2.labeln);

        Button back = new Button("BACK");
        back.setOnMouseClicked(event -> transitionTo(mainGame));

        aWRoot = new HBox(50);
        aWRoot.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        aWRoot.getChildren().addAll(aWvBox, aWvBox2, back);

        ene1.b.setOnMouseClicked(event -> {
            lastAttacked = ene1;
            if (iCanAttack) {
                iCanAttack = false;

                Point p = ene1.b.pointCoordinates(event);

                AnAttackAttempt anAttackAttempt = new AnAttackAttempt();

                anAttackAttempt.l = p.x;
                anAttackAttempt.c = p.y;

                anAttackAttempt.toAttackID = ene1.serverID;
                anAttackAttempt.otherID = ene2.serverID;

                client.sendTCP(anAttackAttempt);
            }
        });


        ene2.b.setOnMouseClicked(event -> {
            lastAttacked = ene2;
            if (iCanAttack) {
                iCanAttack = false;

                Point p = ene2.b.pointCoordinates(event);

                AnAttackAttempt anAttackAttempt = new AnAttackAttempt();
                anAttackAttempt.l = p.x;
                anAttackAttempt.c = p.y;
                anAttackAttempt.toAttackID = ene2.serverID;
                anAttackAttempt.otherID = ene1.serverID;

                client.sendTCP(anAttackAttempt);
            }
        });

        attackScene = new Scene(aWRoot, SCREEN_RECTANGLE.getWidth(), SCREEN_RECTANGLE.getHeight());

    }

    private void setWonScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-image: url(images/BattleShipBigger2.png)");
        Button b = new Button("Go To Main Menu");
        b.setOnMouseClicked(event -> transitionTo(mainMenu));
        b.setStyle("-fx-alignment: center");
        root.getChildren().add(b);

        wonScene = new Scene(root, SCREEN_RECTANGLE.getWidth(), SCREEN_RECTANGLE.getHeight());
    }

    private void setChatScreen() {

        Button n = new Button("BACK");
        n.setOnMouseClicked(event -> transitionTo(mainGame));

        cWl1 = new Label();
        cWl1.setFont(new Font(30));
        cWl2 = new Label();
        cWl2.setFont(new Font(30));

        VBox vBox1 = new VBox(20);
        VBox vBox2 = new VBox(20);

        ene1.conversation = new TextArea();
        ene1.conversation.setEditable(false);
        ene1.conversation.setWrapText(true);

        TextArea tf1 = new TextArea();
        tf1.setWrapText(true);
        tf1.setMinSize(tf1.getPrefWidth() * 2, tf1.getPrefHeight() * 2);
        tf1.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = tf1.getText();
                tf1.clear();
                ChatMessageFromClient c = new ChatMessageFromClient();
                ene1.conversation.appendText("ME: " + message);
                c.text = message;
                c.to = ene1.serverID;
                client.sendTCP(c);
            }
        });

        ene2.conversation = new TextArea();
        ene2.conversation.setEditable(false);
        ene2.conversation.setWrapText(true);

        TextArea tf2 = new TextArea();
        tf2.setWrapText(true);
        tf2.setMinSize(tf2.getPrefWidth(), tf2.getPrefHeight());
        tf2.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = tf2.getText();
                tf2.clear();
                ChatMessageFromClient c = new ChatMessageFromClient();
                ene2.conversation.appendText("ME: " + message);
                c.text = message;
                c.to = ene2.serverID;
                client.sendTCP(c);
            }
        });

        vBox1.getChildren().addAll(cWl1, ene1.conversation, tf1);
        vBox2.getChildren().addAll(cWl2, ene2.conversation, tf2);

        VBox.setVgrow(ene1.conversation, Priority.ALWAYS);
        VBox.setVgrow(ene2.conversation, Priority.ALWAYS);

        HBox hBox = new HBox(50);
        HBox.setHgrow(vBox1, Priority.ALWAYS);
        HBox.setHgrow(vBox2, Priority.ALWAYS);
        hBox.getChildren().addAll(vBox1, vBox2, n);

        chatScreen = new Scene(hBox, SCREEN_RECTANGLE.getWidth(), SCREEN_RECTANGLE.getHeight());

    }

    private void setAIScene() {

        selfvsAI = new SelfGraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, 500, 500);
        ai = new AIPlayer();
        iCanAttack = true;

        Label label = new Label("YOU!");
        label.setFont(new Font("Verdana", 30));
        label.setTextFill(Color.ALICEBLUE);

        VBox forYou = new VBox(10);
        forYou.getChildren().addAll(selfvsAI, label);

        Label ene = new Label("ENEMY(AI)!");
        ene.setFont(new Font("Verdana", 30));
        ene.setTextFill(Color.ROSYBROWN);

        VBox forAI = new VBox(10);
        forAI.getChildren().addAll(ai.b, ene);

        Button back = new Button("BACK/FORFEIT");
        back.setOnMouseClicked(event -> {
            reset();
            transitionTo(mainMenu);
        });

        HBox root = new HBox(50);
        root.setStyle("-fx-fill: true; -fx-alignment:center");
        root.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");

        root.getChildren().addAll(forYou, forAI, back);

        ai.b.setOnMouseClicked(event -> {
            if (iCanAttack) {
                Point p = ai.b.pointCoordinates(event);
                AttackResult result = ai.getAttacked(p);
                iCanAttack = result.shouldPlayAgain();
                doSounds(result);
                if (ai.board.isGameOver()) {
                    won();
                    return;
                }
                if (!iCanAttack)
                    aiTurn();
            }
        });

        AIScene = new Scene(root, SCREEN_RECTANGLE.getWidth(), SCREEN_RECTANGLE.getHeight());

    }

    private void doSounds(AttackResult result) {
        doSounds(result.valid(), result.status == AttackResultStatus.HitShipPiece);
    }


    private void doSounds(boolean actualHit, boolean shipHit) {
        aWShipSound.stop();
        aWWaterSound.stop();
        if (actualHit) {
            if (shipHit)
                aWShipSound.play();
            else
                aWWaterSound.play();
        }
    }

    private void aiTurn() {
        AiMove move = ai.brain.nextMove(pb);
        System.out.println("CHOSE " + move);

        AttackResult result = pb.getAttacked(move.point);
        boolean hit = result.status == AttackResultStatus.HitShipPiece;

        selfvsAI.updateTiles(PlayerBoardTransformer.transform(pb));
        selfvsAI.setLast(move.point);

        ai.brain.react(pb, move, result);

        if (hit && !pb.isGameOver()) {
            Task<Void> wait = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }
            };
            wait.setOnSucceeded(event -> aiTurn());
            new Thread(wait).start();
        } else if (hit && pb.isGameOver())
            lost("YOU LOST TO AI LOL!");
        else {
            iCanAttack = true;
        }
    }

    public void OnIsFull() {
    }

    public void OnAbort() {
    }

    public void OnCanStart() {
        Platform.runLater(() -> transitionTo(mainGame));
    }

    public void OnWhoseTurn(WhoseTurn whoseTurn) {
        Platform.runLater(() -> setTurnLabel(whoseTurn.name));
    }

    public void OnConnectedPlayers(ConnectedPlayers players) {
        Platform.runLater(() -> {
                    WaitingForPlayersScene scene = (WaitingForPlayersScene) App.this.theStage.getScene();
                    scene.OnConnectedPlayers(players);
                }
        );
    }

    public void OnReadyForShips() {
        Platform.runLater(() -> transitionTo(setShips));
    }

    public void OnOtherSpecs(OthersSpecs othersSpecs) {
        Platform.runLater(() -> {

            ene1.serverID = othersSpecs.ene1;
            ene1.name = othersSpecs.ene1n;
            ene1.labeln.setText(ene1.name);
            cWl1.setText(ene1.name);

            ene2.serverID = othersSpecs.ene2;
            ene2.name = othersSpecs.ene2n;
            ene2.labeln.setText(ene2.name);
            cWl2.setText(ene2.name);

        });
    }

    public void OnYourBoardToPaint(YourBoardToPaint toPaint) {
        Platform.runLater(() -> mGSelfBoard.updateTiles((toPaint).board));
    }

    public void OnEnemiesBoardsToPaint(EnemiesBoardsToPaint boards) {
        Platform.runLater(() -> {
            ene1.b.startTiles(boards.board1);
            ene2.b.startTiles(boards.board2);
        });
    }

    public void OnEnemyBoardToPaint(EnemyBoardToPaint board) {
        Platform.runLater(() -> {
            updateEnemyBoard(board.id, board.newAttackedBoard);
            System.out.println("ENEMY BOARD TO PAINT WITH INDEX " + board.id);
        });
    }

    public void OnAnAttackResponse(AnAttackResponse attackResponse) {
        Platform.runLater(() -> {
            lastAttacked.b.updateTiles(attackResponse.newAttackedBoard);
            iCanAttack = attackResponse.again;
            doSounds(attackResponse.actualHit, attackResponse.shipHit);
        });
    }

    public void OnYourTurn() {
        Platform.runLater(() -> {
            iCanAttack = true;
            setTurnLabel("My TURN!!");
        });
    }

    public void OnYouDead() {
        Platform.runLater(() -> {
            lost("You died a horrible death. RIP you");
            transitionTo(mainMenu);
        });
    }

    public void OnPlayerDied(PlayerDied playerDied) {
        Platform.runLater(() -> removeEnemy(playerDied.who));
    }

    public void OnYouWon() {
        Platform.runLater(() -> {
            Alert lost = new Alert(Alert.AlertType.CONFIRMATION);
            lost.setContentText("YOU BEAT THEM ALL");
            lost.showAndWait();
            won();
        });
    }

    public void OnChatMessage(ChatMessage chatMessage) {
        Platform.runLater(() -> {
            App.EnemyLocal toUpdate = ene1;
            if (chatMessage.saidIt == ene2.serverID) {
                toUpdate = ene2;
            }
            toUpdate.conversation.setText(toUpdate.conversation.getText() + chatMessage.message);
        });
    }

    public void OnConnected(Connection connection) {

    }

    public void OnMultiplayerButtonPressed(String name) {
        Task<Network.Register> tryConnectTask = new Task<>() {
            @Override
            protected Network.Register call() throws IOException {
                return client.tryConnect(name, ADDRESS, Network.port);
            }
        };

        tryConnectTask.setOnSucceeded(t -> {
            waitingScreen = new WaitingForPlayersScene(this);
            App.this.transitionTo(waitingScreen);
        });

        tryConnectTask.setOnFailed(t -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Noo!");
            alert.setHeaderText("Can't play when you can't connect to server :(");
            alert.setContentText("Maybe...Go play alone? \nOr you could try again (:");
            alert.showAndWait();
        });

        new Thread(tryConnectTask).start();
    }

    public void OnSoloButtonPressed() {
        vsAI = true;
        theStage.setScene(setShips);
    }

    public void SignalReady(PlayerBoard pb) {
        if (!vsAI) {
            mGSelfBoard.setPlayerBoard(pb);

            String[][] message = PlayerBoardTransformer.transform(pb);

            mGSelfBoard.startTiles(message);
            mGSelfBoard.updateTiles(message);

            Network.APlayerboard p = new Network.APlayerboard();
            p.board = PlayerBoardTransformer.transform(pb);
            client.sendTCP(p);
        } else {
            String[][] message = PlayerBoardTransformer.transform(pb);
            selfvsAI.startTiles(message);
            transitionTo(AIScene);
        }
    }

    private static class EnemyLocal {
        private int serverID;
        private GraphBoardFX b;
        private String name;
        private Label labeln;
        private TextArea conversation;

        private EnemyLocal() {
            serverID = 0;
        }
    }

    private static class AIPlayer {

        GraphBoardFX b;
        PlayerBoard board;
        MyAI brain;

        AIPlayer() {
            brain = new MyAI();
            board = PlayerBoardFactory.getRandomPlayerBoard();
            b = new GraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_LINES);
            b.startTiles(PlayerBoardTransformer.transform(board));
        }

        public AttackResult getAttacked(Point p) {
            AttackResult result = board.getAttacked(p);
            b.updateTiles(PlayerBoardTransformer.transform(board));
            return result;
        }
    }

}



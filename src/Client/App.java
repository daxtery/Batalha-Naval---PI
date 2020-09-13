package Client;

import Common.*;
import Common.Network.*;
import Client.FX.EmptyGraphBoardFX;
import Client.FX.SelfGraphBoardFX;
import Client.Scenes.*;
import com.esotericsoftware.kryonet.Connection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;

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
    private final List<EmptyGraphBoardFX> toAnimate = new ArrayList<>();
    private final int NUM_PLAYERS = 3;
    private final int NUM_ENEMIES = NUM_PLAYERS - 1;
    public final EnemyLocal[] enemies = new EnemyLocal[NUM_ENEMIES];
    //FOR OFFLINE
    private AIPlayer ai;
    //endregion
    private boolean vsAI;
    private SelfGraphBoardFX selfvsAI;
    //FOR ONLINE
    private GameClient client;
    private boolean iCanAttack;
    private String aWshipSoundFile = "assets/sound/ship.mp3";
    private String aWwaterSoundFile = "assets/sound/water.mp3";
    private MediaPlayer aWShipSound = new MediaPlayer(new Media(new File(aWshipSoundFile).toURI().toString()));
    private MediaPlayer aWWaterSound = new MediaPlayer(new Media(new File(aWwaterSoundFile).toURI().toString()));
    //SCENES
    private Scene mainMenu;
    private MainGameScene mainGame;
    private Scene setShips;
    private AttackScene attackScene;
    private Scene wonScene;
    private ChatScene chatScreen;
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

    private void setAllScenes() {
        mainMenu = new MainMenuScene(this);
        mainGame = new MainGameScene(this);
        setShips = new SetShipsScene(this);
        attackScene = new AttackScene(this);
        chatScreen = new ChatScene(this);

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

        if (scene == AIScene) {
            toAnimate.add(selfvsAI);
            toAnimate.add(ai.b);
        }
        for (EmptyGraphBoardFX g : toAnimate)
            g.startAnimating();
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

    private void setAIScene() {

        selfvsAI = new SelfGraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, 500, 500);
        ai = new AIPlayer();
        iCanAttack = true;

        Label label = new Label("YOU!");
        label.setFont(new Font("Verdana", 30));
        label.setTextFill(Color.ALICEBLUE);

        VBox forYou = new VBox(10);
        forYou.getChildren().addAll(selfvsAI, label);

        Label ene = new Label("ENEMY(Client.AI)!");
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
//        AiMove move = ai.brain.nextMove(pb);
//        System.out.println("CHOSE " + move);
//
//        AttackResult result = pb.getAttacked(move.point);
//        boolean hit = result.status == AttackResultStatus.HitShipPiece;
//
//        selfvsAI.updateTiles(PlayerBoardTransformer.transform(pb));
//        selfvsAI.setLast(move.point);
//
//        ai.brain.react(pb, move, result);
//
//        if (hit && !pb.isGameOver()) {
//            Task<Void> wait = new Task<>() {
//                @Override
//                protected Void call() throws Exception {
//                    Thread.sleep(1000);
//                    return null;
//                }
//            };
//            wait.setOnSucceeded(event -> aiTurn());
//            new Thread(wait).start();
//        } else if (hit && pb.isGameOver())
//            lost("YOU LOST TO Client.AI LOL!");
//        else {
//            iCanAttack = true;
//        }
    }

    public void OnIsFull() {
    }

    public void OnAbort() {
    }

    public void OnCanStart() {
        Platform.runLater(() -> transitionTo(mainGame));
    }

    public void OnWhoseTurn(WhoseTurn whoseTurn) {
        Platform.runLater(() -> mainGame.OnWhoseTurn(whoseTurn));
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
            enemies[0] = new EnemyLocal(othersSpecs.ene1, othersSpecs.ene1n);
            enemies[1] = new EnemyLocal(othersSpecs.ene2, othersSpecs.ene2n);
            chatScreen.onOtherSpecs(othersSpecs);
            attackScene.onOtherSpecs(othersSpecs);
        });
    }

    public void OnYourBoardToPaint(YourBoardToPaint toPaint) {
        Platform.runLater(() -> mainGame.OnYourBoardToPaint(toPaint));
    }

    public void OnEnemiesBoardsToPaint(EnemiesBoardsToPaint boards) {
        Platform.runLater(() -> attackScene.onEnemiesBoardsToPaint(boards));
    }

    public void OnEnemyBoardToPaint(EnemyBoardToPaint board) {
        Platform.runLater(() -> {
            System.out.println("ENEMY BOARD TO PAINT WITH INDEX " + board.id);
            attackScene.onEnemyBoardToPaint(board);
        });
    }

    public void OnAnAttackResponse(AnAttackResponse attackResponse) {
        Platform.runLater(() -> {
            attackScene.OnAttackResponse(attackResponse);
            doSounds(attackResponse.actualHit, attackResponse.shipHit);
        });
    }

    public void OnYourTurn() {
        Platform.runLater(() -> {
            attackScene.OnYourTurn();
            mainGame.OnYourTurn();
        });
    }

    public void OnYouDead() {
        Platform.runLater(() -> {
            lost("You died a horrible death. RIP you");
            transitionTo(mainMenu);
        });
    }

    public void OnPlayerDied(PlayerDied playerDied) {
        Platform.runLater(() -> {
            chatScreen.onPlayerDied(playerDied);
            attackScene.onPlayerDied(playerDied);
        });
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
        Platform.runLater(() -> chatScreen.onChatMessage(chatMessage));
    }

    public void OnConnected(Connection connection) {

    }

    public void OnMultiplayerButtonPressed(String name) {

        System.out.println("I am " + name);

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
            mainGame.setPlayerBoard(pb);
            Network.APlayerboard p = new Network.APlayerboard();
            p.board = PlayerBoardTransformer.transform(pb);
            client.sendTCP(p);
        } else {
            String[][] message = PlayerBoardTransformer.transform(pb);
            selfvsAI.startTiles(message);
            transitionTo(AIScene);
        }
    }

    public Optional<EnemyLocal> maybeEnemyLocalById(int id) {
        for (var enemy : enemies) {
            if (enemy.serverID == id) {
                return Optional.of(enemy);
            }
        }
        return Optional.empty();
    }

    public void OnAttackButtonPressed() {
        transitionTo(attackScene);
    }

    public void OnChatButtonPressed() {
        transitionTo(chatScreen);
    }

    public void OnAttackSceneBackButton() {
        transitionTo(mainGame);
    }

    public void CommunicateAttackAttempt(AnAttackAttempt anAttackAttempt) {
        client.sendTCP(anAttackAttempt);
    }

    public void OnChatSceneBackButton() {
        transitionTo(mainGame);
    }

    public void SendMessage(EnemyLocal enemy, String message) {
        Network.ChatMessageFromClient c = new Network.ChatMessageFromClient();
        c.text = message;
        c.to = enemy.serverID;
        client.sendTCP(c);
    }
}



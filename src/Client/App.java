package Client;

import Client.Scenes.*;
import Common.*;
import Common.Network.*;
import com.esotericsoftware.kryonet.Connection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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

    private final int NUM_PLAYERS = 3;
    private final int NUM_ENEMIES = NUM_PLAYERS - 1;
    public final EnemyLocal[] enemies = new EnemyLocal[NUM_ENEMIES];
    private final String aWshipSoundFile = "assets/sound/ship.mp3";
    private final String aWwaterSoundFile = "assets/sound/water.mp3";
    private final MediaPlayer aWShipSound = new MediaPlayer(new Media(new File(aWshipSoundFile).toURI().toString()));
    private final MediaPlayer aWWaterSound = new MediaPlayer(new Media(new File(aWwaterSoundFile).toURI().toString()));
    //FOR ONLINE
    private GameClient client;
    //SCENES
    private Scene mainMenu;
    private MainGameScene mainGame;
    private Scene setShips;
    private AttackScene attackScene;
    private Scene wonScene;
    private ChatScene chatScreen;
    private Scene waitingScreen;

    private Stage theStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        client = new GameClient(this);
        client.start();
        Network.register(client.getNative());

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

    private void lost() {
        Alert lost = new Alert(Alert.AlertType.INFORMATION);
        lost.setContentText("You died a horrible death. RIP you");
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
    }

    private void transitionTo(Scene scene) {
        Scene old = theStage.getScene();

        if (old instanceof BaseGameScene) {
            ((BaseGameScene) old).OnSceneUnset();
        }

        theStage.setScene(scene);

        if (scene instanceof BaseGameScene) {
            ((BaseGameScene) scene).OnSceneSet();
        }
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
            lost();
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

    public void SignalReady(PlayerBoard pb) {
        mainGame.setPlayerBoard(pb);
        Network.APlayerboard p = new Network.APlayerboard();
        p.board = PlayerBoardTransformer.transform(pb);
        client.sendTCP(p);
    }

    public Optional<EnemyLocal> maybeEnemyLocalById(int id) {
        for (EnemyLocal enemy : enemies) {
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



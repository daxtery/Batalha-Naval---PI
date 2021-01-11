package Client;

import Client.AI.Personality.AIPersonality;
import Client.AI.AiClient;
import Client.AI.Personality.FocusedBot;
import Client.AI.Personality.MinionBot;
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
import java.util.*;

public class App extends Application implements IClient {

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

    private final String aWshipSoundFile = "assets/sound/ship.mp3";
    private final String aWwaterSoundFile = "assets/sound/water.mp3";
    private final MediaPlayer aWShipSound = new MediaPlayer(new Media(new File(aWshipSoundFile).toURI().toString()));
    private final MediaPlayer aWWaterSound = new MediaPlayer(new Media(new File(aWwaterSoundFile).toURI().toString()));

    private final Map<Integer, Thread> aiThreads = new HashMap<>();

    private GameClient client;

    //SCENES
    private MainMenuScene mainMenu;
    private MainGameScene mainGame;
    private SetShipsScene setShips;
    private Scene wonScene;
    private LobbyScene lobbyScene;

    private Stage theStage;

    private ConnectedPlayersResponse players;
    private int ourId;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        client = new GameClient(this);
        client.start();

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
        lobbyScene = new LobbyScene(this);
        setWonScene();
    }

    private void transitionTo(Scene scene) {
        theStage.setScene(scene);
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

    private void doSounds(HitResult hitResult) {
        aWShipSound.stop();
        aWWaterSound.stop();
        switch (hitResult) {
            case Invalid -> {
            }
            case HitPiece -> aWShipSound.play();
            case HitWater -> aWWaterSound.play();
        }
    }

    public void onIsFull() {
    }

    public void onAbort() {
    }

    @Override
    public void onCanStart(StartGameResponse startGameResponse) {
        System.out.println("onCanStart");

        Platform.runLater(() -> {
            ourId = players.slot;

            mainGame.setupWithPlayers(players);
            mainGame.onCanStart(startGameResponse);

            transitionTo(mainGame);
        });
    }

    public void onWhoseTurn(WhoseTurnResponse whoseTurnResponse) {
        Platform.runLater(() -> mainGame.OnWhoseTurn(whoseTurnResponse));
    }

    public void onConnectedPlayers(ConnectedPlayersResponse players) {
        this.players = players;

        Platform.runLater(() -> {
            lobbyScene.onConnectedPlayers(players);
        });
    }

    public void onReadyForShips() {
        Platform.runLater(() -> transitionTo(setShips));
    }

    public void onYourBoardToPaint(YourBoardResponse toPaint) {
        Platform.runLater(() -> mainGame.OnYourBoardToPaint(toPaint));
    }

    public void onEnemyBoardToPaint(EnemyBoardResponse board) {
        Platform.runLater(() -> {
            mainGame.onEnemyBoardToPaint(board);
        });
    }

    public void onAnAttackResponse(AnAttackResponse attackResponse) {
        Platform.runLater(() -> {
            mainGame.OnAttackResponse(attackResponse);
            doSounds(attackResponse.hitResult);
        });
    }

    public void onYourTurn() {
        Platform.runLater(() -> {
            mainGame.OnYourTurn();
        });
    }

    public void onAddBotButton(int slot, BotPersonality botPersonality) {
        AIPersonality personality = switch (botPersonality) {
            case Focused -> new FocusedBot();
            case Minion -> new MinionBot();
        };

        final String[] names = {
                "Damien",
                "Lux",
                "David"
        };

        final Random random = new Random();

        Thread thread = new Thread(new AiClient(slot, personality, names[random.nextInt(names.length)], ADDRESS));
        aiThreads.put(slot, thread);
        thread.start();
    }

    public void onRemovePlayerButton(int slot) {

        System.out.println("Removing from slot " + slot);

        if (aiThreads.containsKey(slot)) {
            aiThreads.get(slot).interrupt();
        }

        client.sendTCP(new RemovePlayerFromLobby(slot));
    }

    public void onYouDead() {
        Platform.runLater(() -> {
            lost();
            transitionTo(mainMenu);
        });
    }

    public void onPlayerDied(PlayerDiedResponse playerDiedResponse) {
        Platform.runLater(() -> {
            mainGame.onPlayerDied(playerDiedResponse);
        });
    }

    public void onYouWon() {
        Platform.runLater(() -> {
            Alert lost = new Alert(Alert.AlertType.CONFIRMATION);
            lost.setContentText("YOU BEAT THEM ALL");
            lost.showAndWait();
            won();
        });
    }

    public void onChatMessage(ChatMessageResponse chatMessageResponse) {
        Platform.runLater(() -> mainGame.onChatMessage(chatMessageResponse));
    }

    @Override
    public void onJoinLobbyResponse(JoinLobbyResponse joinLobbyResponse) {
        Platform.runLater(() -> {
            lobbyScene.setNumber(joinLobbyResponse.slots, false);
            transitionTo(lobbyScene);
        });
    }

    public void OnConnected(Connection connection) {

    }

    public void onJoinButtonPressed(String name) {

        System.out.println("I am " + name);

        Task<Void> tryConnectTask = new Task<>() {
            @Override
            protected Void call() throws IOException {
                client.tryConnect(ADDRESS, Network.port);
                return null;
            }
        };

        tryConnectTask.setOnSucceeded(t -> {
            JoinLobby join = new JoinLobby();
            join.name = name;
            client.sendTCP(join);
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

    public void commitPlayerBoard(PlayerBoard playerBoard) {
        mainGame.setPlayerBoard(playerBoard);
        System.out.println("I sent myself");
        PlayerBoardMessage boardMessage = new PlayerBoardMessage(playerBoard);
        client.sendTCP(new PlayerCommitBoard(boardMessage));
    }

    public void OnAttackSceneBackButton() {
        transitionTo(mainGame);
    }

    public void CommunicateAttackAttempt(AnAttack anAttack) {
        client.sendTCP(anAttack);
    }

    public void OnChatSceneBackButton() {
        transitionTo(mainGame);
    }

    public void onCreateLobbyButton(String name, int count) {
        System.out.println("I am creating a lobby! (" + name + ")");

        lobbyScene.setNumber(count, true);

        Task<Void> tryConnectTask = new Task<>() {
            @Override
            protected Void call() throws IOException {
                client.tryConnect(ADDRESS, Network.port);
                return null;
            }
        };

        tryConnectTask.setOnSucceeded(t -> {
            client.sendTCP(new CreateLobby(name, count));
            transitionTo(lobbyScene);
        });

        tryConnectTask.setOnFailed(t -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Noo!");
            alert.setHeaderText("Can't connect to server :(");
            alert.setContentText("Please try again (:");
            alert.showAndWait();
        });

        new Thread(tryConnectTask).start();
    }

    public void SendMessage(int slot, String message) {
        ChatMessage c = new ChatMessage();
        c.text = message;
        c.to = slot;
        client.sendTCP(c);
    }

    public void onLobbyStartButtonClicked() {
        client.sendTCP(new StartGame());
    }

    public int getOurId() {
        return ourId;
    }

    public ConnectedPlayersResponse getPlayers() {
        return players;
    }

}



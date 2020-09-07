//package JavaFX.Stages;
//
//import Common.Network;
//import Common.PlayerBoard;
//import javafx.application.Platform;
//import javafx.concurrent.Task;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.media.AudioClip;
//import javafx.stage.Stage;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.UnknownHostException;
//
//public class MainMenuStage extends Stage {
//
//    private static final String ADDRESS = "localhost";
//
//    private String myName;
//    private PlayerBoard pb;
//    private String soundFile = "assets/sound/play.mp3";
//    private AudioClip soundPlayer = new AudioClip(new File(soundFile).toURI().toString());
//    private BorderPane mMRoot;
//    private Image mMPlayButtonImage = new Image("images/Botao_Start.png");
//    private Button mMPlayButton;
//    private Image mMAloneButtonImage = new Image("images/Botao_Solo_Play.png");
//    private Button mMAloneButton;
//    private Image mMExitButtonImage = new Image("images/Botao_Exit.png");
//    private Button mMExit;
//    private TextField mMnameInput;
//    private Label mMServerText;
//    private GridPane mMMiddle;
//
//    private boolean vsAI;
//
//
//    MainMenuStage() {
//        mMRoot = new BorderPane();
//        mMRoot.setStyle("-fx-background-image: url(images/BattleShip.png);-fx-background-size: cover;");
//
//        mMPlayButton = new Button();
//        mMPlayButton.setGraphic(new ImageView(mMPlayButtonImage));
//
//        mMPlayButton.setOnAction(event -> {
//
//            myName = mMnameInput.getText();
//            if (myName.equals("")) {
//                Alert alert = new Alert(Alert.AlertType.WARNING);
//                alert.setTitle("Ups!");
//                alert.setHeaderText("Name was null!");
//                alert.setContentText("Name can't be null, we need to know who you are :(");
//                alert.showAndWait();
//                return;
//            }
//
//            setTitle(myName);
//
//            Task<Boolean> connect = new Task<>() {
//                @Override
//                protected Boolean call() {
//                    boolean connected = true;
//                    try {
//                        client.connect(5000, ADDRESS, Network.port);
//                    } catch (IOException e) {
//                        connected = false;
//                    }
//                    return connected;
//                }
//            };
//
//            connect.setOnSucceeded(event1 -> {
//
//                if (connect.getValue()) {
//                    Network.register r = new Network.register();
//                    r.name = myName;
//                    try {
//                        r.address = getMeIPV4().toString();
//                    } catch (UnknownHostException e1) {
//                        e1.printStackTrace();
//                        r.address = "100:00";
//                    }
//
//                    BorderPane root = new BorderPane();
//                    textArea = new TextArea();
//                    textArea.setEditable(false);
//
//                    root.setCenter(textArea);
//
//                    waitingScreen = new Scene(root, SCREEN_RECTANGLE.getWidth(), SCREEN_RECTANGLE.getHeight());
//                    transitionTo(waitingScreen);
//                    client.sendTCP(r);
//                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                    alert.setTitle("YEE");
//                    alert.setHeaderText("WE GOT IN");
//                    alert.setContentText("TEMP MESSAGE TO SAY WE GOT IN; WAIT NOW! DON'T PRESS ANY MORE SHIT");
//                    alert.showAndWait();
//                } else {
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Noo!");
//                    alert.setHeaderText("Can't play when you can't connect to server :(");
//                    alert.setContentText("Maybe...Go play alone? \nOr you could try again (:");
//                    alert.showAndWait();
//                }
//            });
//
//            new Thread(connect).start();
//        });
//        mMPlayButton.setStyle("-fx-background-color: transparent;");
//
//        mMAloneButton = new Button();
//        mMAloneButton.setGraphic(new ImageView(mMAloneButtonImage));
//        mMAloneButton.setOnAction(event -> {
//                    vsAI = true;
//                    theStage.setScene(setShips);
//                }
//        );
//        mMAloneButton.setStyle("-fx-background-color: transparent;");
//
//        mMExit = new Button();
//        mMExit.setGraphic(new ImageView(mMExitButtonImage));
//        mMExit.setOnAction(event ->
//                Platform.exit()
//        );
//        mMExit.setStyle("-fx-background-color: transparent;");
//
//        mMnameInput = new TextField("Name!");
//        mMnameInput.textProperty().addListener((observable, oldValue, newValue) -> {
//            final int maxLength = 10;
//
//            if (mMnameInput.getText().length() > maxLength) {
//                String s = mMnameInput.getText().substring(0, maxLength);
//                mMnameInput.setText(s);
//            }
//        });
//
//        mMMiddle = new GridPane();
//        mMMiddle.add(mMPlayButton, 0, 2);
//        mMMiddle.add(mMAloneButton, 1, 2);
//        mMMiddle.add(mMExit, 1, 3);
//        mMMiddle.add(mMnameInput, 0, 1);
//        mMMiddle.setStyle("-fx-fill: true; -fx-alignment:bottom-center; -fx-padding: 50");
//
//        //mMMiddle.getChildren().addAll(mMPlayButton,mMAloneButton, mMExit, mMServerText, mMnameInput);
//        mMRoot.setCenter(mMMiddle);
//        //mMMiddle.setStyle("-fx-background-color:cyan;");
//
//        mainMenu = new Scene(mMRoot, SCREEN_RECTANGLE.getWidth(),
//                SCREEN_RECTANGLE.getHeight()
//        );
//    }
//
//}

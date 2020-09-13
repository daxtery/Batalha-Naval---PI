package Client.Scenes;

import Common.Network;
import Client.App;
import Client.EnemyLocal;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

public class ChatScene extends BaseGameScene {

    private final Map<EnemyLocal, Label> labels;
    private final Map<EnemyLocal, TextArea> textAreas;
    private final Map<EnemyLocal, TextArea> conversations;
    private final Map<EnemyLocal, VBox> vBoxes;

    public ChatScene(App app) {
        super(app, new HBox(50));

        labels = new HashMap<>();
        textAreas = new HashMap<>();
        conversations = new HashMap<>();
        vBoxes = new HashMap<>();

        Button n = new Button("Back");
        n.setOnMouseClicked(event -> app.OnChatSceneBackButton());
        HBox hBox = (HBox) getRoot();
        hBox.getChildren().add(n);
    }

    @Override
    public void OnSceneSet() {
    }

    @Override
    public void OnSceneUnset() {
    }

    public void onPlayerDied(Network.PlayerDied playerDied) {
        var enemy = app.maybeEnemyLocalById(playerDied.who).orElseThrow();
        var textArea = textAreas.get(enemy);
        textArea.setEditable(false);
        textArea.setOnKeyPressed((event) -> {
        });
    }

    public void onOtherSpecs(Network.OthersSpecs othersSpecs) {
        var enemy1 = app.maybeEnemyLocalById(othersSpecs.ene1).orElseThrow();
        var enemy2 = app.maybeEnemyLocalById(othersSpecs.ene2).orElseThrow();

        var cWl1 = new Label(othersSpecs.ene1n);
        cWl1.setFont(new Font(30));
        labels.put(enemy1, cWl1);

        var cWl2 = new Label(othersSpecs.ene2n);
        cWl2.setFont(new Font(30));
        labels.put(enemy2, cWl2);

        var ene1Conversation = new TextArea();
        ene1Conversation.setEditable(false);
        ene1Conversation.setWrapText(true);

        conversations.put(enemy1, ene1Conversation);

        var ene2Conversation = new TextArea();
        ene2Conversation.setEditable(false);
        ene2Conversation.setWrapText(true);

        conversations.put(enemy2, ene2Conversation);

        TextArea tf1 = new TextArea();
        tf1.setWrapText(true);
        tf1.setMinSize(tf1.getPrefWidth() * 2, tf1.getPrefHeight() * 2);
        tf1.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = tf1.getText();
                tf1.clear();
                app.SendMessage(enemy1, message);
                ene1Conversation.appendText("ME: " + message);
            }
        });

        textAreas.put(enemy1, tf1);

        TextArea tf2 = new TextArea();
        tf2.setWrapText(true);
        tf2.setMinSize(tf1.getPrefWidth() * 2, tf1.getPrefHeight() * 2);
        tf2.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = tf2.getText();
                tf2.clear();
                app.SendMessage(enemy2, message);
                ene2Conversation.appendText("ME: " + message);
            }
        });

        textAreas.put(enemy2, tf2);

        VBox vBox1 = new VBox(20);
        VBox vBox2 = new VBox(20);

        vBoxes.put(enemy1, vBox1);
        vBoxes.put(enemy2, vBox2);

        vBox1.getChildren().addAll(cWl1, ene1Conversation, tf1);
        vBox2.getChildren().addAll(cWl2, ene2Conversation, tf2);

        VBox.setVgrow(ene1Conversation, Priority.ALWAYS);
        VBox.setVgrow(ene2Conversation, Priority.ALWAYS);

        HBox hBox = (HBox) getRoot();
        HBox.setHgrow(vBox1, Priority.ALWAYS);
        HBox.setHgrow(vBox2, Priority.ALWAYS);
        hBox.getChildren().addAll(vBox1, vBox2);
    }

    public void onChatMessage(Network.ChatMessage chatMessage) {
        var enemy = app.maybeEnemyLocalById(chatMessage.saidIt).orElseThrow();
        var conversation = textAreas.get(enemy);
        conversation.appendText(chatMessage.message);
    }
}
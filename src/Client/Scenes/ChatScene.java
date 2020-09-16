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
import java.util.Set;

public class ChatScene extends BaseGameScene {

    private final Map<Integer, Label> labels;
    private final Map<Integer, TextArea> textAreas;
    private final Map<Integer, TextArea> conversations;
    private final Map<Integer, VBox> vBoxes;

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
        TextArea textArea = textAreas.get(playerDied.who);
        textArea.setEditable(false);
        textArea.setOnKeyPressed((event) -> {
        });
    }

    public void onChatMessage(Network.ChatMessage chatMessage) {
        TextArea conversation = conversations.get(chatMessage.saidIt);
        conversation.appendText(chatMessage.message);
    }

    public void setupWithPlayers(Network.ConnectedPlayers players) {
        HBox hBox = (HBox) getRoot();

        for (Network.Participant participant : players.participants) {

            if (participant.slot == players.slot) {
                continue;
            }

            Label label = new Label(participant.name);
            label.setFont(new Font(30));
            labels.put(participant.slot, label);

            TextArea conversation = new TextArea();
            conversation.setEditable(false);
            conversation.setWrapText(true);

            conversations.put(participant.slot, conversation);

            TextArea chatTextArea = new TextArea();
            chatTextArea.setWrapText(true);
            chatTextArea.setMinSize(chatTextArea.getPrefWidth() * 2, chatTextArea.getPrefHeight() * 2);
            chatTextArea.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    String message = chatTextArea.getText();
                    chatTextArea.clear();
                    app.SendMessage(participant.slot, message);
                    conversation.appendText("You: " + message);
                }
            });

            VBox vBox = new VBox(20);
            vBoxes.put(participant.slot, vBox);

            vBox.getChildren().addAll(label, conversation, chatTextArea);
            HBox.setHgrow(vBox, Priority.ALWAYS);
            hBox.getChildren().add(vBox);
        }
    }
}
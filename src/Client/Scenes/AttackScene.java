package Client.Scenes;

import Client.FX.EmptyGraphBoardFX;
import Common.Network;
import Client.App;
import Client.FX.GraphBoardFX;
import Client.FX.TileFX;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import util.Point;

import java.util.HashMap;
import java.util.Map;

import static Common.PlayerBoardConstants.DEFAULT_COLUMNS;
import static Common.PlayerBoardConstants.DEFAULT_LINES;

public class AttackScene extends BaseGameScene {

    private final Map<Integer, Label> labels;
    private final Map<Integer, VBox> vBoxes;
    private final Map<Integer, GraphBoardFX> graphBoards;

    private boolean iCanAttack;
    private int lastAttacked;

    public AttackScene(App app) {
        super(app, new HBox(50));

        labels = new HashMap<>();
        vBoxes = new HashMap<>();
        graphBoards = new HashMap<>();

        iCanAttack = false;

        Button back = new Button("Back");
        back.setOnMouseClicked(event -> app.OnAttackSceneBackButton());

        HBox aWRoot = (HBox) getRoot();
        aWRoot.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");
        aWRoot.getChildren().add(back);
    }

    @Override
    public void OnSceneSet() {
        graphBoards.values().forEach(EmptyGraphBoardFX::startAnimating);
    }

    @Override
    public void OnSceneUnset() {
        graphBoards.values().forEach(EmptyGraphBoardFX::stopAnimating);
    }

    public void OnYourTurn() {
        iCanAttack = true;
    }

    public void OnAttackResponse(Network.AnAttackResponse attackResponse) {
        GraphBoardFX board = graphBoards.get(lastAttacked);
        board.updateTiles(attackResponse.newAttackedBoard);
        iCanAttack = attackResponse.again;
    }

    public void onPlayerDied(Network.PlayerDied playerDied) {
        final int who = playerDied.who;
        graphBoards.get(who).setOnMouseClicked((mouseEvent -> {
        }));
    }

    public void onEnemyBoardToPaint(Network.EnemyBoardToPaint board) {
        final int who = board.id;
        graphBoards.get(who).updateTiles(board.newAttackedBoard);
    }


    public void setupWithPlayers(Network.ConnectedPlayers players) {
        HBox root = (HBox) getRoot();

        for (Network.Participant participant : players.participants) {

            if (players.slot == participant.slot) {
                continue;
            }

            Label label = new Label(participant.toString());
            label.setFont(new Font("Verdana", 30));
            label.setTextFill(Color.rgb(0, 0, 0));

            labels.put(participant.slot, label);

            GraphBoardFX boardFX = new GraphBoardFX(
                    DEFAULT_LINES,
                    DEFAULT_COLUMNS,
                    TileFX.TILE_SIZE * DEFAULT_COLUMNS,
                    TileFX.TILE_SIZE * DEFAULT_LINES
            );

            boardFX.setOnMouseClicked(event -> {
                lastAttacked = participant.slot;
                if (iCanAttack) {
                    iCanAttack = false;

                    Point p = boardFX.pointCoordinates(event);

                    Network.AnAttackAttempt anAttackAttempt = new Network.AnAttackAttempt();

                    anAttackAttempt.l = p.x;
                    anAttackAttempt.c = p.y;

                    anAttackAttempt.toAttackID = participant.slot;

                    app.CommunicateAttackAttempt(anAttackAttempt);
                }
            });

            graphBoards.put(participant.slot, boardFX);

            VBox vBox = new VBox(10);
            vBox.getChildren().addAll(boardFX, label);
            vBoxes.put(participant.slot, vBox);
            root.getChildren().add(vBox);
        }
    }

    public void onCanStart(Network.CanStart canStart) {
        for (int i = 0; i < canStart.boards.length; i++) {
            graphBoards.get(canStart.indices[i]).startTiles(canStart.boards[i]);
        }
    }
}
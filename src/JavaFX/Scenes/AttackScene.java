package JavaFX.Scenes;

import Common.Network;
import JavaFX.App;
import JavaFX.EnemyLocal;
import JavaFX.GraphBoardFX;
import JavaFX.TileFX;
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

    private final Map<EnemyLocal, Label> labels;
    private final Map<EnemyLocal, VBox> vBoxes;
    private final Map<EnemyLocal, GraphBoardFX> graphBoards;

    private boolean iCanAttack;
    private EnemyLocal lastAttacked;

    public AttackScene(App app) {
        super(app, new HBox(50));

        labels = new HashMap<>();
        vBoxes = new HashMap<>();
        graphBoards = new HashMap<>();

        iCanAttack = false;

        Button back = new Button("Back");
        back.setOnMouseClicked(event -> app.OnAttackSceneBackButton());

        var aWRoot = (HBox) getRoot();
        aWRoot.setStyle("-fx-background-image: url(images/BattleShipBigger2.png);-fx-background-size: cover;");
        aWRoot.getChildren().add(back);
    }

    @Override
    public void OnSceneSet() {
        graphBoards.values().forEach(b -> b.startAnimating());
    }

    @Override
    public void OnSceneUnset() {
        graphBoards.values().forEach(b -> b.stopAnimating());
    }

    public void OnYourTurn() {
        iCanAttack = true;
    }

    public void OnAttackResponse(Network.AnAttackResponse attackResponse) {
        var board = graphBoards.get(lastAttacked);
        board.updateTiles(attackResponse.newAttackedBoard);
        iCanAttack = attackResponse.again;
    }

    public void onPlayerDied(Network.PlayerDied playerDied) {
        final int who = playerDied.who;
        var enemy = app.maybeEnemyLocalById(who).orElseThrow();
        graphBoards.get(enemy).setOnMouseClicked((mouseEvent -> {
        }));
    }

    public void onEnemiesBoardsToPaint(Network.EnemiesBoardsToPaint boards) {
        graphBoards.get(app.enemies[0]).startTiles(boards.board1);
        graphBoards.get(app.enemies[1]).startTiles(boards.board2);
    }

    public void onOtherSpecs(Network.OthersSpecs othersSpecs) {

        EnemyLocal enemy1 = app.maybeEnemyLocalById(othersSpecs.ene1).orElseThrow();

        Label label1 = new Label(othersSpecs.ene1n);
        label1.setFont(new Font("Verdana", 30));
        label1.setTextFill(Color.rgb(0, 0, 0));

        labels.put(enemy1, label1);

        Label label2 = new Label(othersSpecs.ene2n);
        label2.setFont(new Font("Verdana", 30));
        label2.setTextFill(Color.rgb(0, 0, 0));

        EnemyLocal enemy2 = app.maybeEnemyLocalById(othersSpecs.ene2).orElseThrow();

        labels.put(enemy2, label2);

        var board1 = new GraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_LINES);

        board1.setOnMouseClicked(event -> {
            lastAttacked = enemy1;
            if (iCanAttack) {
                iCanAttack = false;

                Point p = board1.pointCoordinates(event);

                Network.AnAttackAttempt anAttackAttempt = new Network.AnAttackAttempt();

                anAttackAttempt.l = p.x;
                anAttackAttempt.c = p.y;

                anAttackAttempt.toAttackID = enemy1.serverID;
                anAttackAttempt.otherID = enemy2.serverID;

                app.CommunicateAttackAttempt(anAttackAttempt);
            }
        });

        var board2 = new GraphBoardFX(DEFAULT_LINES, DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_COLUMNS, TileFX.TILE_SIZE * DEFAULT_LINES);

        board2.setOnMouseClicked(event -> {
            lastAttacked = enemy2;
            if (iCanAttack) {
                iCanAttack = false;

                Point p = board1.pointCoordinates(event);

                Network.AnAttackAttempt anAttackAttempt = new Network.AnAttackAttempt();

                anAttackAttempt.l = p.x;
                anAttackAttempt.c = p.y;

                anAttackAttempt.toAttackID = enemy2.serverID;
                anAttackAttempt.otherID = enemy1.serverID;

                app.CommunicateAttackAttempt(anAttackAttempt);
            }
        });

        graphBoards.put(enemy1, board1);
        graphBoards.put(enemy2, board2);

        var aWvBox1 = new VBox(10);
        aWvBox1.getChildren().addAll(board1, label1);

        var aWvBox2 = new VBox(10);
        aWvBox2.getChildren().addAll(board2, label2);

        vBoxes.put(enemy1, aWvBox1);
        vBoxes.put(enemy2, aWvBox2);

        var aWRoot = (HBox) getRoot();
        aWRoot.getChildren().addAll(aWvBox1, aWvBox2);
    }

    public void onEnemyBoardToPaint(Network.EnemyBoardToPaint board) {
        final int who = board.id;
        var enemy = app.maybeEnemyLocalById(who).orElseThrow();
        graphBoards.get(enemy).updateTiles(board.newAttackedBoard);
    }
}
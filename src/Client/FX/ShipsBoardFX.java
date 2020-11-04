package Client.FX;

import Common.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ShipsBoardFX extends HBox {

    private final ShipFX[] shipsFX;
    private final PlayerBoardFX playerBoardFX;
    private ShipFX selected;
    private boolean canPlace;
    private PlayerBoard playerBoard;

    public ShipsBoardFX(GameConfiguration gameConfiguration, int w, int h) {
        super();
        setPrefSize(w, h);

        List<Integer> shipSizes = gameConfiguration.shipSizes;

        shipsFX = new ShipFX[shipSizes.size()];
        canPlace = false;

        for (int i = 0; i < shipSizes.size(); i++) {
            ShipFX shipFX = shipsFX[i] = new ShipFX(shipSizes.get(i));
            shipsFX[i].setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!shipFX.isOnBoard()) {
                        setSelected(shipFX);
                    }
                }
                event.consume();
            });
        }

        VBox shipsRepository = new VBox();

        shipsRepository.getChildren().addAll(shipsFX);

        playerBoardFX = new PlayerBoardFX(gameConfiguration.lines, gameConfiguration.columns, false);

        AtomicReference<Point> lastTileClicked = new AtomicReference<>();

        playerBoardFX.setClickedHandler(point -> {
            BoardTile tile = playerBoard.getTileAt(point);

            if (point.equals(lastTileClicked.get()) && selected == null) {
                if (tile.containsShipPiece()) {
                    Ship ship = tile.getShip();
                    ShipFX shipFX = Arrays.stream(shipsFX)
                            .filter(sfx -> sfx.isOnBoard() && sfx.getShip() == ship)
                            .findFirst()
                            .orElseThrow();
                    playerBoard.removeShip(ship);
                    shipFX.notifyIsOnBoard(false);
                    setSelected(shipFX);
                    lastTileClicked.set(null);

                    canPlace = playerBoard.canShipBeHere(point, ship);

                    if (canPlace) {
                        playerBoard.placeShip(point, ship);
                    }

                    playerBoardFX.setBoard(playerBoard);
                }

            } else if (selected != null && canPlace) {
                selected.notifyIsOnBoard(true);
                deselect();
                playerBoardFX.setBoard(playerBoard);
                lastTileClicked.set(null);
            }

            lastTileClicked.set(point);

        });

        AtomicReference<Point> lastPointEntered = new AtomicReference<>();

        playerBoardFX.setEnteredHandler(point -> {
            if (selected == null) {
                return;
            }

            if (selected.isOnBoard()) {
                return;
            }

            Ship ship = selected.getShip();

            canPlace = playerBoard.canShipBeHere(point, ship);

            if (canPlace) {
                playerBoard.placeShip(point, ship);
            }

            lastPointEntered.set(point);
            playerBoardFX.setBoard(playerBoard);
        });

        playerBoardFX.setExitedHandler(point -> {
            if (selected == null) {
                return;
            }

            if (selected.isOnBoard()) {
                return;
            }

            Ship ship = selected.getShip();

            if (playerBoard.containsShip(ship)) {
                playerBoard.removeShip(ship);
            }

            playerBoardFX.setBoard(playerBoard);
        });

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                deselect();
            } else if (event.getCode() == KeyCode.Q) {
                if (selected != null) {

                    Ship ship = selected.getShip();

                    if (playerBoard.containsShip(ship)) {
                        playerBoard.removeShip(ship);
                    }

                    Direction direction = ship.getDirection();

                    ship.setDirection(direction.rotated);

                    Point point = lastPointEntered.get();

                    if (point == null) {
                        return;
                    }

                    canPlace = playerBoard.canShipBeHere(point, ship);

                    if (canPlace) {
                        playerBoard.placeShip(point, ship);
                    }

                    playerBoardFX.setBoard(playerBoard);
                }
            }
            event.consume();
        });

        getChildren().addAll(shipsRepository, playerBoardFX);

        requestFocus();
        setFocusTraversable(true);
    }

    public void setPlayerBoard(PlayerBoard playerBoard) {
        this.playerBoard = playerBoard;
        int i = 0;
        for (Map.Entry<Ship, Point> entry : playerBoard.ships.entrySet()) {
            shipsFX[i].setShip(entry.getKey());
            shipsFX[i].notifyIsOnBoard(true);
            ++i;
        }
        playerBoardFX.setBoard(playerBoard);
        deselect();
    }

    private void setSelected(ShipFX shipFX) {
        if (selected != null) {
            selected.notifyIsSelected(false);
        }
        selected = shipFX;
        selected.notifyIsSelected(true);
        playerBoardFX.setBoard(playerBoard);
    }

    private void deselect() {
        if (selected != null) {
            selected.notifyIsSelected(false);
        }
        selected = null;
    }

    public void removeAll() {
        for (ShipFX shipFX : shipsFX) {
            shipFX.notifyIsOnBoard(false);
            Ship ship = shipFX.getShip();
            if (playerBoard.containsShip(ship)) {
                playerBoard.removeShip(ship);
            }
        }
        playerBoardFX.setBoard(playerBoard);
    }
}

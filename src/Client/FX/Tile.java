package Client.FX;

import Common.BoardTile;
import Common.PlayerBoard;
import Common.Ship;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.Point;

public class Tile extends Pane {

    static final Border border = new Border(
            new BorderStroke(
                    Color.BLACK,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderStroke.DEFAULT_WIDTHS
            )
    );

    private final ImageView fogView;
    private final ImageView waterBaseView;
    private final ImageView pieceView;
    private final ImageView attackedView;

    public Tile(Point point) {
        super();

        Pane layout = new Pane();

        waterBaseView = new ImageView();
        BorderPane waterWrapper = new BorderPane(waterBaseView);

        pieceView = new ImageView();
        BorderPane pieceWrapper = new BorderPane(pieceView);

        attackedView = new ImageView();
        BorderPane attackedWrapper = new BorderPane(attackedView);

        fogView = new ImageView();
        BorderPane fogWrapper = new BorderPane(fogView);

        Label label = new Label(point.toString());
        label.layoutXProperty().bind(layout.widthProperty().subtract(label.widthProperty()).divide(2));
        label.layoutXProperty().bind(layout.widthProperty().subtract(label.widthProperty()).divide(2));

        label.layoutYProperty().bind(layout.heightProperty().subtract(label.heightProperty()).divide(2));
        label.layoutYProperty().bind(layout.heightProperty().subtract(label.heightProperty()).divide(2));

        label.setTextFill(Color.WHITE);

        layout.getChildren().addAll(waterWrapper, pieceWrapper, attackedWrapper, fogWrapper, label);
        getChildren().add(layout);

        setBorder(border);
    }

    public void update(boolean forSelf, Point point, PlayerBoard playerBoard) {
        BoardTile boardTile = playerBoard.getTileAt(point);
        if (!boardTile.containsShipPiece()) {
            pieceView.setImage(null);
            attackedView.setImage(null);
            if (!boardTile.isAttackable()) {
                waterBaseView.setImage(WaterTileResources.IMAGE_ATTACKED);
                fogView.setImage(null);
            } else {
                if (forSelf) {
                    waterBaseView.setImage(WaterTileResources.IMAGE_TO_SELF);
                    fogView.setImage(null);
                } else {
                    fogView.setImage(TileResources.imageOthersHidden);
                    waterBaseView.setImage(null);
                }
            }
        } else {

            Ship ship = boardTile.getShip();
            int index = boardTile.getShipPieceIndex();

            if (playerBoard.isShipDestroyed(ship)) {
                fogView.setImage(null);
                waterBaseView.setImage(WaterTileResources.IMAGE_ATTACKED);
                pieceView.setImage(ShipTileResources.giveRightImageToShow(ship, index));
                attackedView.setImage(ShipTileResources.DESTROYED);
            } else if (!boardTile.isAttackable()) {
                fogView.setImage(null);
                waterBaseView.setImage(WaterTileResources.IMAGE_ATTACKED);
                pieceView.setImage(forSelf ?
                        ShipTileResources.giveRightImageToShow(ship, index) :
                        ShipTileResources.ATTACKED);
                attackedView.setImage(ShipTileResources.ATTACKED);
            } else {
                if (forSelf) {
                    fogView.setImage(null);
                    waterBaseView.setImage(WaterTileResources.IMAGE_TO_SELF);
                    pieceView.setImage(ShipTileResources.giveRightImageToShow(ship, index));
                } else {
                    waterBaseView.setImage(null);
                    fogView.setImage(TileResources.imageOthersHidden);
                    pieceView.setImage(null);
                }
                attackedView.setImage(null);
            }
        }
    }
}

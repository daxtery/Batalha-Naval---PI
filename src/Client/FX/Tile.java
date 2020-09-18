package Client.FX;

import Common.BoardTile;
import Common.ShipPiece;
import Common.WaterTile;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import util.Point;

public class Tile extends Pane {

    private final ImageView fogView;
    private final ImageView waterBaseView;
    private final ImageView pieceView;
    private final ImageView attackedView;

    public Tile(Point point) {
        super();

        Pane layout = new Pane();

        Label label = new Label(point.toString());
        label.setAlignment(Pos.CENTER);

        waterBaseView = new ImageView();
        BorderPane waterWrapper = new BorderPane(waterBaseView);

        pieceView = new ImageView();
        BorderPane pieceWrapper = new BorderPane(pieceView);

        attackedView = new ImageView();
        BorderPane attackedWrapper = new BorderPane(attackedView);

        fogView = new ImageView();
        BorderPane fogWrapper = new BorderPane(fogView);

        layout.getChildren().addAll(waterWrapper, pieceWrapper, attackedWrapper, fogWrapper, label);
        getChildren().add(layout);

        setStyle("-fx-border-color: black; -fx-border-style: solid; -fx-border-width: 1;");
    }

    public void update(boolean forSelf, BoardTile boardTile) {
        switch (boardTile.tileType) {
            case Water -> {
                WaterTile waterTile = (WaterTile) boardTile;
                switch (waterTile.status()) {
                    case Visible -> {
                        waterBaseView.setImage(WaterTileResources.IMAGE_ATTACKED);
                        fogView.setImage(null);
                    }
                    case NotVisible -> {
                        if (forSelf) {
                            waterBaseView.setImage(WaterTileResources.IMAGE_TO_SELF);
                            fogView.setImage(null);
                        } else {
                            fogView.setImage(TileResources.imageOthersHidden);
                            waterBaseView.setImage(null);
                        }
                    }
                }

            }

            case ShipPiece -> {
                ShipPiece shipPiece = (ShipPiece) boardTile;

                switch (shipPiece.status()) {
                    case AttackedShipDestroyed -> {
                        fogView.setImage(null);
                        waterBaseView.setImage(WaterTileResources.IMAGE_ATTACKED);
                        pieceView.setImage(ShipTileResources.giveRightImageToShow(shipPiece));
                        attackedView.setImage(ShipTileResources.DESTROYED);
                    }
                    case Attacked -> {
                        fogView.setImage(null);
                        waterBaseView.setImage(WaterTileResources.IMAGE_ATTACKED);
                        pieceView.setImage(forSelf ?
                                ShipTileResources.giveRightImageToShow(shipPiece) :
                                ShipTileResources.ATTACKED);
                        attackedView.setImage(ShipTileResources.ATTACKED);
                    }
                    case NotAttacked -> {
                        if (forSelf) {
                            fogView.setImage(null);
                            waterBaseView.setImage(WaterTileResources.IMAGE_TO_SELF);
                            pieceView.setImage(ShipTileResources.giveRightImageToShow(shipPiece));
                        } else {
                            waterBaseView.setImage(null);
                            fogView.setImage(TileResources.imageOthersHidden);
                            pieceView.setImage(null);
                        }
                    }
                }
            }
        }
    }
}

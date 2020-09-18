package Client.FX;

import Common.BoardTile;
import Common.ShipPiece;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import util.Point;

public class Tile extends Pane {

    Pane layout;
    ImageView base;

    public Tile(Point point) {
        super();
        layout = new Pane();

        Label label = new Label(point.toString());
        label.setAlignment(Pos.CENTER);

        base = new ImageView("images/water.png");

        BorderPane imageViewWrapper = new BorderPane(base);

        layout.getChildren().addAll(imageViewWrapper, label);

        imageViewWrapper.setStyle("-fx-border-color: black; -fx-border-style: solid; -fx-border-width: 1;");

        getChildren().add(layout);
    }

    public void update(boolean isEnemy, BoardTile boardTile) {
        if (!boardTile.visible) {
            base.setImage(new Image("images/fog.png"));
            return;
        }

        var newImage = switch (boardTile.tileType){
            case Water -> new Image("images/water_d.png");
            case ShipPiece -> {

            }
        };

        base.setImage(newImage);
    }

    Image imageForShipPiece(ShipPiece shipPiece){

        switch (shipPiece.getShip().shipType){
            case One:
                imageToSelf = giveImageBasedOnDirection(ONE_ONE);
                break;
            case Two:
                switch (id) {
                    case 0 -> imageToSelf = giveImageBasedOnDirection(ONE_TWO);
                    case 1 -> imageToSelf = giveImageBasedOnDirection(TWO_TWO);
                }
                break;
            case Three:
                switch (id) {
                    case 0 -> imageToSelf = giveImageBasedOnDirection(ONE_THREE);
                    case 1 -> imageToSelf = giveImageBasedOnDirection(TWO_THREE);
                    case 2 -> imageToSelf = giveImageBasedOnDirection(THREE_THREE);
                }
                break;
            case Four:
                switch (id) {
                    case 0 -> imageToSelf = giveImageBasedOnDirection(ONE_FOUR);
                    case 1 -> imageToSelf = giveImageBasedOnDirection(TWO_FOUR);
                    case 2 -> imageToSelf = giveImageBasedOnDirection(THREE_FOUR);
                    case 3 -> imageToSelf = giveImageBasedOnDirection(FOUR_FOUR);
                }
        }
        setImageToDraw(imageToSelf);
    }
}

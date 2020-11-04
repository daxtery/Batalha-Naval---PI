package Client.FX;

import Common.Direction;
import Common.Ship;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

class ShipFX extends BorderPane {

    private final static Image ONE = new Image("images/1.png");
    private final static Image TWO = new Image("images/2.png");
    private final static Image THREE = new Image("images/3.png");
    private final static Image FOUR = new Image("images/4.png");
    private static final Border border = new Border(
            new BorderStroke(
                    Color.BLACK,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderStroke.DEFAULT_WIDTHS
            )
    );
    private final ImageView imageView;
    private Ship ship;

    ShipFX(int size) {
        super(new ImageView());
        imageView = (ImageView) getChildren().get(0);
        setShip(new Ship(Direction.Right, size));
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
        imageView.setImage(switch (this.ship.size) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            case 4 -> FOUR;
            default -> throw new IllegalStateException("Unexpected value: " + this.ship.size);
        });
    }

    void notifyIsOnBoard(boolean value) {
        if (!value) {
            imageView.setOpacity(1.0);
        } else {
            imageView.setOpacity(0.1);
        }
    }

    void notifyIsSelected(boolean value) {
        setBorder(value ? border : null);
    }

    boolean isOnBoard() {
        return imageView.getOpacity() == 0.1;
    }

}

package JavaFX;


import Common.Direction;
import Common.Ship;
import javafx.scene.image.Image;
import util.Point;

class ShipFX extends SpriteTileFX {

    private final static Image ONE = new Image("images/1.png");
    private final static Image TWO = new Image("images/2.png");
    private final static Image THREE = new Image("images/3.png");
    private final static Image FOUR = new Image("images/4.png");

    int shipSize;
    boolean placed;
    Ship ship;

    ShipFX(int _ShipSize, Point point, Direction _dir, boolean boardCoord) {
        super(point.x, point.y, boardCoord, _dir);
        shipSize = _ShipSize;
        selectImage();
    }

    ShipFX(int _ShipSize) {
        this(_ShipSize, new Point(), Direction.Right, false);
    }

    void setShip(Ship ship) {
        this.ship = ship;
        this.placed = true;

        Point position = ship.origin();

        if (ship.direction == Direction.Up || ship.direction == Direction.Left) {
            position = ship.tail();
        }

        dir = ship.direction;

        setPositionBoard(position);
        selectImage();
    }

    void selectImage() {
        switch (shipSize) {
            case 1 -> setImageToDraw(ONE);
            case 2 -> setImageToDraw(TWO);
            case 3 -> setImageToDraw(THREE);
            case 4 -> setImageToDraw(FOUR);
        }
        setImageToDraw(giveImageBasedOnDirection(getImageToDraw()));
    }

}

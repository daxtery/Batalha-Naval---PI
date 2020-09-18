package Client.FX;

import Common.Direction;
import Common.ShipPiece;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class ShipTileResources {

    public final static Image ATTACKED = new Image("images/Fogo.png");
    public final static Image DESTROYED = new Image("images/fumeira.png");
    public final static Image ONE_ONE = new Image("images/1_1.png");
    public final static Image ONE_TWO = new Image("images/1_2.png");
    public final static Image TWO_TWO = new Image("images/2_2.png");
    public final static Image ONE_THREE = new Image("images/1_3.png");
    public final static Image TWO_THREE = new Image("images/2_3.png");
    public final static Image THREE_THREE = new Image("images/3_3.png");
    public final static Image ONE_FOUR = new Image("images/1_4.png");
    public final static Image TWO_FOUR = new Image("images/2_4.png");
    public final static Image THREE_FOUR = new Image("images/3_4.png");
    public final static Image FOUR_FOUR = new Image("images/4_4.png");
    private ShipTileResources() {
    }

    public static Image giveRightImageToShow(ShipPiece shipPiece) {
        Image image = switch (shipPiece.getShip().shipType) {
            case One -> ONE_ONE;

            case Two -> switch (shipPiece.getIdInsideShip()) {
                case 0 -> ONE_TWO;
                case 1 -> TWO_TWO;
                default -> {
                    System.err.println(
                            "!! Wrong shipPiece! has shipType: "
                                    + shipPiece.getShip().shipType +
                                    " but id is " +
                                    shipPiece.getIdInsideShip()
                    );
                    yield null;
                }
            };

            case Three -> switch (shipPiece.getIdInsideShip()) {
                case 0 -> ONE_THREE;
                case 1 -> TWO_THREE;
                case 2 -> THREE_THREE;
                default -> {
                    System.err.println(
                            "!! Wrong shipPiece! has shipType: "
                                    + shipPiece.getShip().shipType +
                                    " but id is " +
                                    shipPiece.getIdInsideShip()
                    );
                    yield null;
                }
            };

            case Four -> switch (shipPiece.getIdInsideShip()) {
                case 0 -> ONE_FOUR;
                case 1 -> TWO_FOUR;
                case 2 -> THREE_FOUR;
                case 3 -> FOUR_FOUR;
                default -> {
                    System.err.println(
                            "!! Wrong shipPiece! has shipType: "
                                    + shipPiece.getShip().shipType +
                                    " but id is " +
                                    shipPiece.getIdInsideShip()
                    );
                    yield null;
                }
            };

        };

        return rotateImageByDirection(image, shipPiece.getShip().direction);
    }

    private static Image rotateImageByDirection(Image image, Direction direction) {
        return switch (direction) {
            case Right -> rotateImageByAngles(0, image);
            case Down -> rotateImageByAngles(90, image);
            case Left -> rotateImageByAngles(180, image);
            case Up -> rotateImageByAngles(270, image);
        };
    }

    private static Image rotateImageByAngles(int angles, Image image) {
        ImageView view = new ImageView(image);
        view.setRotate(angles);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.rgb(0, 0, 0, 0));

        return view.snapshot(params, null);
    }
}

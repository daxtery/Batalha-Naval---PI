package Client.FX;

import Common.Direction;
import javafx.scene.image.Image;

public abstract class TileFX extends SpriteTileFX {

    public final static int TILE_SIZE = 50;
    boolean attacked;
    boolean normalBoard;

    TileFX(int _l, int _c, Direction _dir){
        super(_l, _c, true, _dir);
        attacked = false;
        normalBoard = false;
    }

    @Override
    public String toString() {
        return "T at" + l + ":" + c + "; getAttacked: " + attacked;
    }
}

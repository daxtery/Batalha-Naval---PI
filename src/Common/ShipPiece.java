package Common;

import java.awt.*;

public class ShipPiece extends BoardTile {

    public final static Color COLOR_ATTACKED = new Color(150,10,11);
    public final static Color COLOR_ATTACKED_SHIP_DESTROYED = new Color(20,20,20);

    public final static Color COLOR_TO_SHOW = new Color(139,69,19);

    public static final String NOT_ATTACKED_STRING = "P";
    public static final String ATTACKED_STRING = "PA";
    public static final String ATTACKED_SHIP_DESTROYED_STRING = "PD";

    Ship ship;

    ShipPiece(Ship _ship, int _x, int _y) {
        this.x = _x;
        this.y = _y;
        ship = _ship;
    }

    @Override
    public String toString() {
        return details();
    }

    @Override
    boolean isPiece() {
        return true;
    }

    String details(){
        return "ShipPiece at " + this.x + "+" + this.y + " and is attacked: " + attacked;
    }

    String toSendString(){
        if(ship.isDestroyed())
            return ATTACKED_SHIP_DESTROYED_STRING;
        if(!canAttack())
            return ATTACKED_STRING;
        return NOT_ATTACKED_STRING;
    }
}

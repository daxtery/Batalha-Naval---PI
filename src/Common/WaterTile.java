package Common;

import util.Point;

enum WaterTileStatus { Visible, NotVisible }

public class WaterTile extends BoardTile {

    WaterTile(int _x, int _y){
        super(new Point(_x, _y), TileType.Water);
    }

    @Override
    public String toString() {
        return "Water at " + this.point + ", is visible " + visible;
    }

    public WaterTileStatus status() {
        return visible ? WaterTileStatus.Visible : WaterTileStatus.NotVisible;
    }

    @Override
    String toSendString() {
        if(canAttack())
            return PlayerBoardTransformer.WATER_NOT_VISIBLE_STRING;
        return PlayerBoardTransformer.WATER_VISIBLE_STRING;
    }
}

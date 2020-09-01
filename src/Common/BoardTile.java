package Common;

import util.Point;

import java.io.Serializable;

public abstract class BoardTile implements Serializable {

    public TileType tileType;
    public boolean visible;
    public Point point;

    protected BoardTile(Point point, TileType tileType) {
        this(point, tileType, false);
    }

    protected BoardTile(Point point, TileType tileType, boolean visible) {
        this.point = point;
        this.tileType = tileType;
        this.visible = visible;
    }

    public boolean canAttack(){
        return !visible;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof BoardTile))
            return false;
        BoardTile other = (BoardTile) obj;
        return other.point.equals(this.point);
    }

}

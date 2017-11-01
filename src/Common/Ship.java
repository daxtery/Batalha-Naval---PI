package Common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class Ship implements Serializable{

    private int startX;
    private int startY;
    private final ShipType _shipType;
    private Direction _dir;
    private ShipPiece[] pieces;
    private static final Random r = new Random();

    enum ShipType {

        One,
        Two,
        Three,
        Four;

        private int value;

        static {
            One.value = 1;
            Two.value = 2;
            Three.value = 3;
            Four.value = 4;
        }

        @Nullable
        static ShipType getShipType(int value){
            for(ShipType shipType : ShipType.values()){
                if(shipType.value == value){
                    return shipType;
                }
            }
            return null;
        }

    }

    public void setNewCoord(Point point){
        startX = point.x;
        startY = point.y;
    }

    public void changeDirection(){
        _dir = _dir.getRotated();
    }

    Ship(int x, int y, Direction dir, ShipType st){
        _shipType = st;
        pieces = new ShipPiece[st.value];
        setNewCoord(new Point(x, y));
        _dir = dir;
    }


    ShipPiece[] getPieces(){
        calculatePieces();
        return pieces;
    }

    private void calculatePieces(){
        int[] vector = new int[]{0,0};
        if(_dir != null){
            vector = _dir.getDirectionVector();
        }
        for (int i = 0; i < _shipType.ordinal() + 1; i++){
            pieces[i] = new ShipPiece(
                    this,
                    startX + vector[0] * i,
                    startY + vector[1] * i)
            ;
        }
    }

    public int getSize(){
        return _shipType.ordinal() + 1;
    }

    private static Ship getOneRandomShip(PlayerBoard pb, ShipType size, Direction[] directions){
        Ship tempShip = new Ship(0,0,Direction.DOWN, ShipType.One);
        boolean didIt = false;
        while(!didIt) {
            int x = r.nextInt(PlayerBoard.LINES);
            int y = r.nextInt(PlayerBoard.COLUMNS);
            tempShip = new Ship(x, y, directions[r.nextInt(directions.length)], size);
            if (pb.canShipBeHere(tempShip)) {
                didIt = true;
                pb.placeShip(tempShip);
            }
            String did = (didIt) ? "Placed" : "Could not place";
            /*System.out.printf("%s a ship at x-%d and y-%d. The " +
                    "ship has %d size and it's dir is %s \n", did, x, y, tempShip.getSize(), tempShip._dir);*/
        }
        return tempShip;
    }

    @NotNull
    public static Ship[] getFreshShips(){
        return new Ship[]{
                new Ship(0,0,Direction.DOWN, ShipType.Four),
                new Ship(0,0,Direction.DOWN,ShipType.Three),
                new Ship(0,0,Direction.DOWN,ShipType.Three),
                new Ship(0,0,Direction.DOWN,ShipType.Two),
                new Ship(0,0,Direction.DOWN,ShipType.Two),
                new Ship(0,0,Direction.DOWN,ShipType.Two),
                new Ship(0,0,Direction.DOWN,ShipType.One),
                new Ship(0,0,Direction.DOWN,ShipType.One),
                new Ship(0,0,Direction.DOWN,ShipType.One),
                new Ship(0,0,Direction.DOWN,ShipType.One)
        };
    }

    public static Ship[] getRandomShips(){
        ShipType[] types = new ShipType[]{
                ShipType.Four, ShipType.Three, ShipType.Three,
                ShipType.Two, ShipType.Two, ShipType.Two,
                ShipType.One,ShipType.One,ShipType.One,ShipType.One
        };
        Ship[] temp = new Ship[PlayerBoard.NUMBER_OF_BOATS];
        // 4
        // 3, 3
        // 2, 2, 2
        // 1, 1, 1, 1
        PlayerBoard tempBoard = new PlayerBoard();
        Direction[] directions = Direction.values();
        int i = 0;
        while(true){
            temp[i] = getOneRandomShip(tempBoard, types[i], directions);
            i++;
            if(i == types.length){
                //System.out.println("I is: " +  i);
                //tempBoard.lightItUp();
                //System.out.println(tempBoard);
                //System.out.println(Arrays.toString(temp));
                break;
            }
        }

        return temp;
    }

    boolean isDestroyed() {
        for (ShipPiece piece : pieces) {
            if (!piece.attacked) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String s = "[";
        for(int i = 0; i < pieces.length; i++){
            s += pieces[i].details() + ", ";
        }
        s+= "]";
        return s;
    }
}

package Common;

import java.util.List;

public class GameConfiguration {

    public int lines, columns;
    public List<Integer> shipSizes;
    public Direction[] shipDirections;

    public GameConfiguration(int lines, int columns, List<Integer> shipSizes, Direction[] shipDirections) {
        this.lines = lines;
        this.columns = columns;
        this.shipSizes = shipSizes;
        this.shipDirections = shipDirections;
    }
}

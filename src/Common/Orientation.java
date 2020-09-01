package Common;

public enum Orientation {

    HORIZONTAL,
    VERTICAL;

    private Orientation rotated;
    private int[] directionVector;

    static {
        HORIZONTAL.rotated = VERTICAL;
        HORIZONTAL.directionVector = new int[]{0,1};

        VERTICAL.rotated = HORIZONTAL;
        VERTICAL.directionVector = new int[]{1,0};
    }

}

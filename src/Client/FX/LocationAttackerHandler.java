package Client.FX;


import util.Point;

@FunctionalInterface
public interface LocationAttackerHandler {
    void handle(Point location);
}

package Client.FX;


import util.Point;

@FunctionalInterface
public interface LocationEnteredHandler {
    void handle(Point location);
}

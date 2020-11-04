package Client.FX;


import util.Point;

@FunctionalInterface
public interface LocationExitedHandler {
    void handle(Point location);
}

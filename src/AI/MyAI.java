package AI;

import Common.*;
import util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAI {

    private final List<AiMove> possibleMoves;

    public MyAI() {
        possibleMoves = new ArrayList<>();
    }

    private boolean inBounds(Point p) {
        return PlayerBoard.inBounds(p);
    }

    public void react(PlayerBoard playerBoard, AiMove lastMove, AttackResult result) {

        boolean hitShip = result.status == AttackResultStatus.HitShipPiece;
        boolean destroyedShip = result.destroyedShip;

        if (destroyedShip) {
            possibleMoves.clear();
            return;
        }

        List<Point> positions = playerBoard.getAvailable();

        if (possibleMoves.size() > 0) {
            if (!hitShip) {
                possibleMoves.removeIf(move -> move.direction == lastMove.direction);
                return;
            }

            possibleMoves.removeIf(move ->
                    move.direction != lastMove.direction.opposite
            );

            AiMove candidate = lastMove.withAdvancedDirection();

            if (inBounds(candidate.point) && positions.contains(candidate.point)) {
                possibleMoves.add(candidate);
            }
            return;
        }

        if (!hitShip) {
            return;
        }

        for (Direction direction : Direction.values()) {
            Point point = lastMove.point.moved(direction.vector);
            if (inBounds(point) && positions.contains(point)) {
                possibleMoves.add(new AiMove(direction, point));
            }
        }
    }

    public AiMove nextMove(PlayerBoard playerBoard) {

        if (possibleMoves.size() == 0) {
            List<Point> possible = playerBoard.getAvailable();
            int index = new Random().nextInt(possible.size());
            int directionIndex = new Random().nextInt(Direction.values().length);
            return new AiMove(Direction.values()[directionIndex], possible.get(index));
        }

        int index = new Random().nextInt(possibleMoves.size());
        return possibleMoves.remove(index);
    }
}

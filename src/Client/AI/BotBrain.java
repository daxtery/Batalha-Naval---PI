package Client.AI;

import Common.BoardTile;
import Common.Direction;
import Common.PlayerBoard;
import util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

class BotBrain {

    Point firstContact;

    public BotBrain() {
    }

    public List<MoveCandidate> calculateMoves(PlayerBoard playerBoard) {

        if (firstContact != null) {
            BoardTile tile = playerBoard.getTileAt(firstContact);
            firstContact = playerBoard.isShipDestroyed(tile.getShip()) ? null : firstContact;
        }

        if (firstContact == null) {
            List<BoardTile> shipPieces = playerBoard.getTilesWithShipPieces();

            Optional<BoardTile> possiblyNewFirstContact = shipPieces.stream()
                    .filter(Predicate.not(BoardTile::isAttackable))
                    .filter(tile -> !playerBoard.isShipDestroyed(tile.getShip()))
                    .findFirst();

            possiblyNewFirstContact.ifPresent(boardTile -> firstContact = boardTile.point);
        }

        if (firstContact == null) {
            List<BoardTile> possible = playerBoard.getAttackableTiles();
            int index = new Random().nextInt(possible.size());
            BoardTile chosen = possible.get(index);
            return new ArrayList<>() {{
                add(new MoveCandidate(chosen.point, 1));
            }};
        }

        final int maxShipSize = playerBoard.getShips()
                .stream()
                .map(s -> s.size)
                .max(Integer::compareTo)
                .orElseThrow();

        List<MoveCandidate> moveCandidates = new ArrayList<>();

        Direction foundDirection = null;

        for (Direction direction : Direction.values()) {

            if (foundDirection != null && direction != foundDirection.opposite) {
                continue;
            }

            int inThisDirection = 1;
            int inARow = 1;
            Point current = firstContact.moved(direction.vector);
            for (int i = 1; i < maxShipSize && playerBoard.inBounds(current); i++) {
                BoardTile tile = playerBoard.getTileAt(current);

                if (tile.isAttackable()) {
                    MoveCandidate moveCandidate = new MoveCandidate(current, inARow + inThisDirection);
                    moveCandidates.add(moveCandidate);
                    inARow = 0;
                } else {
                    if (tile.containsShipPiece()) {
                        inARow++;
                        inThisDirection++;
                        if (inARow > 1) {
                            foundDirection = direction;
                        }
                    } else {
                        break;
                    }
                }

                current = current.moved(direction.vector);
            }
        }

        return moveCandidates;
    }

}

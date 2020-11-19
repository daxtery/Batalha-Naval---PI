package Common;

import util.Point;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerBoard implements Serializable {

    public final Point size;
    public final BoardTile[][] boardTiles;

    public final Map<Ship, Point> ships;

    public PlayerBoard(int lines, int columns) {
        size = new Point(lines, columns);
        boardTiles = new BoardTile[lines][columns];

        for (int j = 0; j < lines; j++) {
            for (int k = 0; k < columns; k++) {
                boardTiles[j][k] = new BoardTile(new Point(j, k));
            }
        }

        ships = new HashMap<>();
    }

    public int lines() {
        return size.x;
    }

    public int columns() {
        return size.y;
    }

    public boolean inBounds(Point point) {
        final Point lower = new Point(-1, -1);

        return point.isConstrainedBy(
                lower,
                size
        );
    }

    public Set<Ship> getShips() {
        return ships.keySet();
    }

    public HitResult getAttacked(Point point) {
        if (!inBounds(point)) return HitResult.Invalid;

        BoardTile tile = getTileAt(point);

        if (!tile.isAttackable()) return HitResult.Invalid;

        tile.setAttackable(false);

        if (tile.containsShipPiece()) {
            Ship ship = tile.getShip();
            if (isShipDestroyed(ship)) {
                onShipDestroyed(ship);
            }
            return HitResult.HitPiece;
        }

        return HitResult.HitWater;
    }

    public boolean isGameOver() {
        return getShips().stream().allMatch(this::isShipDestroyed);
    }

    private void onShipDestroyed(Ship ship) {
        Point origin = ships.get(ship);
        List<Point> parts = ship.partsWithOrigin(origin);

        for (Point part : parts) {
            List<Point> points = getSurroundingPoints(part);
            for (Point point : points) {
                getTileAt(point).setAttackable(false);
            }
        }
    }

    public List<BoardTile> getAttackableTiles() {
        return Arrays.stream(boardTiles)
                .flatMap(Arrays::stream)
                .filter(BoardTile::isAttackable)
                .collect(Collectors.toList());
    }

    public List<BoardTile> getAttackedTiles() {
        return Arrays.stream(boardTiles)
                .flatMap(Arrays::stream)
                .filter(Predicate.not(BoardTile::isAttackable))
                .collect(Collectors.toList());
    }

    public List<BoardTile> getTilesWithShipPieces() {
        return Arrays.stream(boardTiles)
                .flatMap(Arrays::stream)
                .filter(BoardTile::containsShipPiece)
                .collect(Collectors.toList());
    }

    public void placeShip(Point at, Ship ship) {
        ships.put(ship, at);
        List<Point> parts = ship.partsWithOrigin(at);

        for (int i = 0, partsSize = parts.size(); i < partsSize; i++) {
            Point part = parts.get(i);
            getTileAt(part).setShipPiece(ship, i);
        }
    }

    protected List<Point> getSurroundingPoints(Point point) {
        final Point[] neighbours = new Point[]{
                point.moved(1, 0),
                point.moved(1, 1),
                point.moved(1, -1),

                point.moved(-1, 0),
                point.moved(-1, 1),
                point.moved(-1, -1),

                point.moved(0, 1),
                point.moved(0, -1),
        };

        return Arrays.stream(neighbours).filter(this::inBounds).collect(Collectors.toList());
    }

    public boolean canShipBeHere(Point at, Ship ship) {
        for (Point part : ship.partsWithOrigin(at)) {
            boolean isInBounds = inBounds(part);

            if (!isInBounds) {
                return false;
            }

            if (isShipPieceAt(part) || isAdjacentToShipPieceAt(part)) {
                return false;
            }

        }
        return true;
    }

    protected boolean isAdjacentToShipPieceAt(Point point) {
        List<Point> points = getSurroundingPoints(point);
        return points.stream().anyMatch(this::isShipPieceAt);
    }

    public BoardTile getTileAt(Point point) {
        return boardTiles[point.x][point.y];
    }

    public void removeShip(Ship ship) {
        Point origin = ships.remove(ship);
        List<Point> parts = ship.partsWithOrigin(origin);

        for (Point part : parts) {
            getTileAt(part).removeShipPiece();
        }
    }

    public boolean containsShip(Ship ship) {
        return ships.containsKey(ship);
    }

    public boolean isShipPieceAt(Point point) {
        return getTileAt(point).containsShipPiece();
    }

    public boolean isShipDestroyed(Ship ship) {
        Point origin = ships.get(ship);
        List<Point> parts = ship.partsWithOrigin(origin);

        return parts.stream().noneMatch(p -> getTileAt(p).isAttackable());
    }
}

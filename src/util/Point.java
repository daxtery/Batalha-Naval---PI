package util;

import java.util.Objects;

public class Point {
    public int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point other) {
        this(other.x, other.y);
    }

    public Point() {
        this(0, 0);
    }

    public Point set(Point other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Point moved(Point by) {
        return moved(by.x, by.y);
    }

    public Point moved(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point move(Point by) {
        this.x += by.x;
        this.y += by.y;
        return this;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Point flip() {
        int temp = this.y;
        this.y = this.x;
        this.x = temp;
        return this;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Point flipped() {
        return new Point(this.y, this.x);
    }

    public boolean isConstrainedBy(Point lower, Point higher) {
        return (this.x > lower.x && this.x < higher.x)
                &&
                (this.y > lower.y && this.y < higher.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x &&
                y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

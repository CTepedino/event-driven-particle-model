package ar.edu.itba.ss;

import java.util.Objects;

public class Vector2D {
    private final double x;
    private final double y;

    public Vector2D(double x, double y){
        this.x = x;
        this.y = y;
    }

    public static Vector2D fromPolar(double magnitude, double angle){
        return new Vector2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D point2D = (Vector2D) o;
        return Double.compare(x, point2D.x) == 0 && Double.compare(y, point2D.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public double distance(Vector2D other){
        return Math.sqrt(Math.pow(x-other.x, 2) + Math.pow(y-other.y, 2));
    }

    public Vector2D normalize() {
        double mag = Math.sqrt(x * x + y * y);
        return new Vector2D(x / mag, y / mag);
    }

    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }
}

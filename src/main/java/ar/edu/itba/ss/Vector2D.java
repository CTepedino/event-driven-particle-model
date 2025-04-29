package ar.edu.itba.ss;

import java.util.Objects;

public class Vector2D {
    private final double x;
    private final double y;

    private double magnitude = -1;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D fromPolar(double magnitude, double angle){
        return new Vector2D(
                magnitude * Math.cos(angle),
                magnitude * Math.sin(angle)
        );
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
        Vector2D vector2D = (Vector2D) o;
        return Double.compare(x, vector2D.x) == 0 && Double.compare(y, vector2D.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public double distance(Vector2D other){
        double x_diff = x-other.x;
        double y_diff = y-other.y;
        return Math.sqrt(x_diff * x_diff + y_diff * y_diff);
    }

    public double magnitude(){
        if (magnitude == -1){
            magnitude = Math.sqrt(x*x + y*y);
        }
        return magnitude;
    }

    public Vector2D add(Vector2D other){
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other){
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D scale(double scalar){
        return new Vector2D(scalar * x, scalar * y);
    }

    public double dotProduct(Vector2D other){
        return x * other.x + y * other.y;
    }

    public Vector2D normalized(){
        double mag = magnitude();
        if (mag == 0){
            throw new RuntimeException("Cannot normalize a 0 vector");
        }
        return scale(1/mag);
    }

    public double angle(){
        return Math.atan2(y, x);
    }
}

package ar.edu.itba.ss;

import java.util.Objects;

public class Particle {
    private final long id;

    private Vector2D position;
    private Vector2D velocity;
    private final double mass;
    private final double radius;

    public Particle(long id, Vector2D position, Vector2D velocity, double mass, double radius) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.radius = radius;
    }

    public long getId() {
        return id;
    }

    public void setPosition(Vector2D newPosition){
        this.position = newPosition;
    }

    public void setVelocity(Vector2D newVelocity){
        this.velocity = newVelocity;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Particle particle = (Particle) o;
        return id == particle.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean isOverlapped(Particle other){
        return position.distance(other.position) <= (radius + other.radius);
    }

    @Override
    public String toString() {
        return String.format("%d %f %f %f %f %f %f",
            id,
            position.getX(),
            position.getY(),
            velocity.getX(),
            velocity.getY(),
            mass,
            radius
        );
    }
}

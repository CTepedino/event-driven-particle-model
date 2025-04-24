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

    public void update(double deltaTime){
        double newX = position.getX() + velocity.getX() * deltaTime;
        double newY = position.getY() + velocity.getY() * deltaTime;
        this.position = new Vector2D(newX, newY);
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


    public void bounceOffWall() {
        Vector2D normal = position.normalize();
        double dot = velocity.dot(normal);
        this.velocity = velocity.subtract(normal.scale(2 * dot));
    }

    public void bounceOffObstacle() {
        Vector2D normal = position.normalize();
        double dot = velocity.dot(normal);
        this.velocity = velocity.subtract(normal.scale(2 * dot));
    }

    public void bounceOffParticle(Particle other){
        Vector2D deltaX = this.position.subtract(other.position);
        Vector2D deltaV = this.velocity.subtract(other.velocity);
        double distanceSquared = deltaX.dot(deltaX);

        if (distanceSquared == 0) return;

        double dotProduct = deltaV.dot(deltaX);
        if (dotProduct >= 0) return;

        double m1 = this.mass;
        double m2 = other.mass;

        double factor1 = (2 * m2 / (m1 + m2)) * (dotProduct / distanceSquared);
        Vector2D newVel1 = this.velocity.subtract(deltaX.scale(factor1));

        double factor2 = (2 * m1 / (m1 + m2)) * (-dotProduct / distanceSquared);
        Vector2D newVel2 = other.velocity.add(deltaX.scale(factor2));

        this.velocity = newVel1;
        other.velocity = newVel2;
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

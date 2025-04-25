package ar.edu.itba.ss;

import java.util.Locale;
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
        double dot = velocity.getX() * normal.getX() + velocity.getY() * normal.getY();
        this.velocity = new Vector2D(
                velocity.getX() - 2 * dot * normal.getX(),
                velocity.getY() - 2 * dot * normal.getY()
        );
    }

    public void bounceOffObstacle() {
        Vector2D normal = position.normalize();
        double dot = velocity.getX() * normal.getX() + velocity.getY() * normal.getY();
        this.velocity = new Vector2D(
                velocity.getX() - 2 * dot * normal.getX(),
                velocity.getY() - 2 * dot * normal.getY()
        );
    }

    public void bounceOffParticle(Particle other){

        double sigma = radius + other.getRadius();
        Vector2D deltaR = position.subtract(other.position);
        Vector2D deltaV = velocity.subtract(other.velocity);
        double deltaV_deltaR = deltaV.dot(deltaR);

        double J = 2 * mass * other.mass * deltaV_deltaR /(((mass) + other.mass) * sigma);
        double Jx = J * deltaR.getX() / sigma;
        double Jy = J * deltaR.getY() / sigma;

        velocity = new Vector2D(velocity.getX()  - Jx/mass, velocity.getY() - Jy/mass);
        other.velocity = new Vector2D(other.velocity.getX() + Jx/mass, other.velocity.getY() + Jy/mass);

    }

    @Override
    public String toString() {
        return String.format(Locale.US,"%d %f %f %f %f %f %f",
            id,
            position.getX(),
            position.getY(),
            velocity.getX(),
            velocity.getY(),
            mass,
            radius
        );
    }

    public String positionalInfo() {
        return String.format(Locale.US,"%d %f %f %f %f",
                id,
                position.getX(),
                position.getY(),
                velocity.getX(),
                velocity.getY()
        );
    }
}

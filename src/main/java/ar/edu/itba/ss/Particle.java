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

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public void move(double deltaTime){
        this.position = new Vector2D(
                position.getX() + velocity.getX() * deltaTime,
                position.getY() + velocity.getY() * deltaTime
        );
    }

    public void bounce(){
        Vector2D normal = position.normalized();
        double dot = velocity.dotProduct(normal);
        velocity = velocity.subtract(normal.scale(2*dot));
    }

    public void collideWithParticle(Particle other){
        double sigma = radius + other.radius;
        Vector2D deltaR = position.subtract(other.position);
        Vector2D deltaV = velocity.subtract(other.velocity);
        double dotDrDv = deltaR.dotProduct(deltaV);

        double Jfactor = (2*mass*other.mass*(dotDrDv))/(sigma*(mass+other.mass));
        Vector2D Jvector = deltaR.scale(Jfactor/sigma);

        velocity = velocity.add(Jvector.scale(1/mass));
        other.velocity = other.velocity.subtract(Jvector.scale(1/other.mass));
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

package ar.edu.itba.ss;

import java.io.PrintWriter;
import java.util.*;

public class Board {
    private final double radius;
    private final double obstacleRadius;

    private final Set<Particle> particles;

    public Board(double L, double obstacleRadius, Set<Particle> particles) {
        this.radius = L/2;
        this.obstacleRadius = obstacleRadius;
        this.particles = particles;
    }

    public double getRadius() {
        return radius;
    }

    public double getObstacleRadius() {
        return obstacleRadius;
    }

    public Set<Particle> getParticles() {
        return particles;
    }

    private static double discriminant(double A, double B, double C){
        return B*B-4*A*C;
    }

    private double boardCollisionTime(Particle particle){
        double A = Math.pow(particle.getVelocity().getX(), 2) + Math.pow(particle.getVelocity().getY(), 2);
        double B = 2 * (particle.getPosition().getX() * particle.getVelocity().getX() + particle.getPosition().getY() * particle.getVelocity().getY());
        double C = Math.pow(particle.getPosition().getX(), 2) + Math.pow(particle.getPosition().getY(), 2) - Math.pow(radius - particle.getRadius(), 2);

        double EPS = 1e-12;

        if (A < EPS) return -1;                // no motion
        double disc = B*B - 4*A*C;
        if (disc < 0 && disc > -EPS) disc = 0;
        if (disc < 0) return -1;

        double sqrtD = Math.sqrt(disc);
        double t1 = (-B + sqrtD) / (2*A);
        double t2 = (-B - sqrtD) / (2*A);

        double result = -1;
        if (t1 > EPS && t2 > EPS) {
            result = Math.min(t1, t2);
        } else if (t1 > EPS) {
            result = t1;
        } else if (t2 > EPS) {
            result = t2;
        }
        return result;
    }

    private double obstacleCollisionTime(Particle particle){
        if (obstacleRadius == 0){
            return -1;
        }
        double A = Math.pow(particle.getVelocity().getX(), 2) + Math.pow(particle.getVelocity().getY(), 2);
        double B = 2 * (particle.getPosition().getX() * particle.getVelocity().getX() + particle.getPosition().getY() * particle.getVelocity().getY());
        double C = Math.pow(particle.getPosition().getX(), 2) + Math.pow(particle.getPosition().getY(), 2) - Math.pow(obstacleRadius + particle.getRadius(), 2);

        double EPS = 1e-12;

        if (A < EPS) return -1;                // no motion
        double disc = B*B - 4*A*C;
        if (disc < 0 && disc > -EPS) disc = 0;
        if (disc < 0) return -1;

        double sqrtD = Math.sqrt(disc);
        double t1 = (-B + sqrtD) / (2*A);
        double t2 = (-B - sqrtD) / (2*A);

        double result = -1;
        if (t1 > EPS && t2 > EPS) {
            result = Math.min(t1, t2);
        } else if (t1 > EPS) {
            result = t1;
        } else if (t2 > EPS) {
            result = t2;
        }
        return result;
    }

    private double particlePairCollisionTime(Particle a, Particle b){
        double dx = a.getPosition().getX() - b.getPosition().getX();
        double dy = a.getPosition().getY() - b.getPosition().getY();

        double sgm = a.getRadius() + b.getRadius();

        double dvx = a.getVelocity().getX() - b.getVelocity().getX();
        double dvy = a.getVelocity().getY() - b.getVelocity().getY();

        double A = Math.pow(dvx, 2) + Math.pow(dvy, 2);
        double B = 2 * (dx * dvx + dy * dvy);
        double C = Math.pow(dx, 2) + Math.pow(dy, 2) - Math.pow(sgm, 2);

        double EPS = 1e-12;

        if (A < EPS) return -1;                // no motion
        double disc = B*B - 4*A*C;
        if (disc < 0 && disc > -EPS) disc = 0;
        if (disc < 0) return -1;

        double sqrtD = Math.sqrt(disc);
        double t1 = (-B + sqrtD) / (2*A);
        double t2 = (-B - sqrtD) / (2*A);

        double result = -1;
        if (t1 > EPS && t2 > EPS) {
            result = Math.min(t1, t2);
        } else if (t1 > EPS) {
            result = t1;
        } else if (t2 > EPS) {
            result = t2;
        }
        return result;
    }

    public double toNextCollisionTime(double currentTime, PrintWriter writer, boolean printCollisions){
        List<Collision> soonestPerParticle = new ArrayList<>();

        for (Particle particle: particles){
            List<Collision> collisions = new ArrayList<>();
            collisions.add(
                    Collision.withWall(particle, boardCollisionTime(particle))
            );
            collisions.add(
                    Collision.withObstacle(particle, obstacleCollisionTime(particle))
            );
            for (Particle other: particles){
                if (other != particle && particle.getId() < other.getId()){
                    collisions.add(
                            new Collision(particle, other, particlePairCollisionTime(particle, other))
                    );
                }
            }

            soonestPerParticle.add(
                    collisions.stream()
                            .filter(c -> Double.compare(c.getTime(), 0) > 0)
                            .min(Comparator.comparingDouble(Collision::getTime))
                            .orElse(null)
            );
        }

        double soonestCollisionTime = soonestPerParticle.stream().filter(Objects::nonNull).mapToDouble(Collision::getTime).min().orElse(0);

        if (soonestCollisionTime == 0) {
            throw new RuntimeException("No more collisions. This should not happen");
        }

        for (Particle particle: particles){
            particle.update(soonestCollisionTime);
        }

        List<Collision> happeningNow = new ArrayList<>();

        for (Collision collision: soonestPerParticle){
            if (collision != null && Double.compare(collision.getTime(), soonestCollisionTime) == 0){
                happeningNow.add(collision);
            }
        }

        if (printCollisions) {
            writer.println(happeningNow.size());
        }
        for (Collision collision : happeningNow) {
            if (collision.withWall) {
                collision.getA().bounceOffWall();
                if (printCollisions){
                    writer.println(String.format(Locale.US, "%d W", collision.getA().getId()));
                }
            } else if (collision.withObstacle) {
                collision.getA().bounceOffObstacle();
                if (printCollisions){
                    writer.println(String.format(Locale.US, "%d O", collision.getA().getId()));
                }
            } else {
                collision.getA().bounceOffParticle(collision.getB());
                if (printCollisions){
                    writer.println(String.format(Locale.US, "%d %d", collision.getA().getId(), collision.getB().getId()));
                }
            }
        }

        return currentTime + soonestCollisionTime;
    }

    @Override
    public String toString(){
        StringBuilder strb = new StringBuilder();
        for (Particle particle: particles){
            strb.append(particle.positionalInfo()).append('\n');
        }

        return strb.toString();
    }
}

package ar.edu.itba.ss;

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

        if (A == 0){
            return -1;
        }

        double discriminant = discriminant(A, B, C);

        if (discriminant < 0){
            return -1;
        }

        double disc_sqrt = Math.sqrt(discriminant);
        return Math.min(
                (-B + disc_sqrt)/(2*A),
                (-B - disc_sqrt)/(2*A)
        );
    }

    private double obstacleCollisionTime(Particle particle){
        if (obstacleRadius == 0){
            return -1;
        }
        double A = Math.pow(particle.getVelocity().getX(), 2) + Math.pow(particle.getVelocity().getY(), 2);
        double B = 2 * (particle.getPosition().getX() * particle.getVelocity().getX() + particle.getPosition().getY() * particle.getVelocity().getY());
        double C = Math.pow(particle.getPosition().getX(), 2) + Math.pow(particle.getPosition().getY(), 2) - Math.pow(obstacleRadius + particle.getRadius(), 2);

        if (A == 0){
            return -1;
        }

        double discriminant = discriminant(A, B, C);

        if (discriminant < 0){
            return -1;
        }

        double disc_sqrt = Math.sqrt(discriminant);
        return Math.min(
                (-B + disc_sqrt)/(2*A),
                (-B - disc_sqrt)/(2*A)
        );
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

        if (A == 0){
            return -1;
        }

        double discriminant = discriminant(A, B, C);

        if (discriminant < 0){
            return -1;
        }

        double disc_sqrt = Math.sqrt(discriminant);
        return Math.min(
                (-B + disc_sqrt)/(2*A),
                (-B - disc_sqrt)/(2*A)
        );
    }

    public double toNextCollisionTime(double currentTime){
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

        double epsilon = 1e-8;
        List<Collision> happeningNow = new ArrayList<>();

        for (Particle particle : particles) {
            double wallTime = boardCollisionTime(particle);
            if (Math.abs(wallTime - soonestCollisionTime) < epsilon) {
                happeningNow.add(Collision.withWall(particle, wallTime));
            }

            double obsTime = obstacleCollisionTime(particle);
            if (Math.abs(obsTime - soonestCollisionTime) < epsilon) {
                happeningNow.add(Collision.withObstacle(particle, obsTime));
            }

            for (Particle other : particles) {
                if (particle.getId() < other.getId()) {
                    double pairTime = particlePairCollisionTime(particle, other);
                    if (Math.abs(pairTime - soonestCollisionTime) < epsilon) {
                        happeningNow.add(new Collision(particle, other, pairTime));
                    }
                }
            }
        }

        for (Collision collision : happeningNow) {
            if (collision.withWall) {
                collision.getA().bounceOffWall();
            } else if (collision.withObstacle) {
                collision.getA().bounceOffObstacle();
            } else {
                collision.getA().bounceOffParticle(collision.getB());
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

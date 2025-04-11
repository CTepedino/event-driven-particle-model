package ar.edu.itba.ss;

import java.util.HashSet;
import java.util.Set;

public class Board {
    private final double radius;
    private final double obstacleRadius;

    private final Set<Particle> particles;

    public Board(long L, double obstacleRadius, Set<Particle> particles) {
        this.radius = (double)L/2;
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
}

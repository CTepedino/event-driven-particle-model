package ar.edu.itba.ss;

import java.util.HashSet;
import java.util.Set;

public class Board {
    private final double radius;
    private final Set<Particle> particles = new HashSet<>();

    public Board(long L) {
        this.radius = (double)L/2;
    }

    public double getRadius() {
        return radius;
    }

    public Set<Particle> getParticles() {
        return particles;
    }
}

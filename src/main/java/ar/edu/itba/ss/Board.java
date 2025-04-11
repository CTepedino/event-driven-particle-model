package ar.edu.itba.ss;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final long width;
    private final long height;

    private final List<Particle> particles = new ArrayList<>();

    public Board(long width, long height) {
        this.width = width;
        this.height = height;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }

    public List<Particle> getParticles() {
        return particles;
    }
}

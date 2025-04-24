package ar.edu.itba.ss;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ParticleGenerator {
    private static final String BOARD_DIAMETER = "boardDiameter";
    private static final String OBSTACLE_RADIUS = "obstacleRadius";
    private static final String PARTICLE_COUNT = "particleCount";
    private static final String MASS = "mass";
    private static final String RADIUS = "radius";
    private static final String SPEED = "speed";
    private static final String OUT_PATH = "outPath";

    private final Random random = new Random();

    public static void main(String[] args){
        if (System.getProperty(BOARD_DIAMETER) == null){
            throw new RuntimeException("Missing required param: " + BOARD_DIAMETER);
        }
        double boardDiameter = Double.parseDouble(System.getProperty(BOARD_DIAMETER));

        double obstacleRadius= 0;
        if (System.getProperty(OBSTACLE_RADIUS) != null){
            obstacleRadius = Double.parseDouble(System.getProperty(OBSTACLE_RADIUS));
        }

        int particleCount = 201;
        if (System.getProperty(PARTICLE_COUNT) != null){
            particleCount = Integer.parseInt(System.getProperty(PARTICLE_COUNT));
        }

        if (System.getProperty(MASS) == null){
            throw new RuntimeException("Missing required param: " + MASS);
        }
        double mass = Double.parseDouble(System.getProperty(MASS));

        if (System.getProperty(RADIUS) == null){
            throw new RuntimeException("Missing required param: " + RADIUS);
        }
        double radius = Double.parseDouble(System.getProperty(RADIUS));

        if (System.getProperty(SPEED) == null){
            throw new RuntimeException("Missing required param: " + SPEED);
        }
        double speed = Double.parseDouble(System.getProperty(SPEED));

        String outPath = "particles.txt";
        if (System.getProperty(OUT_PATH) != null){
            outPath = System.getProperty(OUT_PATH);
        }

        ParticleGenerator generator = new ParticleGenerator();

        Set<Particle> particles = generator.generate(particleCount, speed, mass, radius, obstacleRadius, boardDiameter/2);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outPath))){
            writer.println(String.format("%f", boardDiameter));
            writer.println(String.format("%f", obstacleRadius));
            for (Particle particle: particles){
                writer.println(particle);
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    private Vector2D randomPosition(double rInt, double rExt){
        boolean isOutsideLimits = true;
        double randomX = 0;
        double randomY = 0;
        while (isOutsideLimits) {
            randomX = (Math.pow(-1, random.nextBoolean() ? 0 : 1)) * (rInt + (Math.random() * (rExt - rInt)));
            randomY = (Math.pow(-1, random.nextBoolean() ? 0 : 1)) * (rInt + (Math.random() * (rExt - rInt)));
            isOutsideLimits = !isPointInRing(randomX, randomY, rInt, rExt);
        }
        return new Vector2D(randomX, randomY);
    }

    private boolean isPointInRing(double x, double y, double innerRadius, double outerRadius) {
        double distanceSquared = x*x + y*y;
        return distanceSquared >= innerRadius * innerRadius &&
                distanceSquared <= outerRadius * outerRadius;
    }

    private Vector2D randomVelocity(double speed){
        return Vector2D.fromPolar(speed, random.nextDouble() * 2 * Math.PI);
    }

    public Set<Particle> generate(long count, double speed, double mass, double radius, double circleMin, double circleMax){
        Set<Particle> particles = new HashSet<>();

        for (int id = 0; id <= count; id++){
            while(true){
                Particle candidate = new Particle(id, randomPosition(circleMin, circleMax), null, mass, radius);

                if (particles.stream().noneMatch(p -> p.isOverlapped(candidate))){
                    candidate.setVelocity(randomVelocity(speed));
                    particles.add(candidate);
                    break;
                }
            }
        }

        return particles;
    }
}


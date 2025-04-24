package ar.edu.itba.ss;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Simulation {
    private static final String MAX_TIME = "maxTime";
    private static final String MAX_COLLISIONS = "maxCollisions";

    private final Board board;

    public static void main(String[] args){

        Simulation simulation = new Simulation(args[0]);

        Optional<Long> maxTime = Optional.empty();
        if (System.getProperty(MAX_TIME) != null){
            maxTime = Optional.of(Long.parseLong(System.getProperty(MAX_TIME)));
        }

        Optional<Long> maxCollisions = Optional.empty();
        if (System.getProperty(MAX_COLLISIONS) != null){
            maxCollisions = Optional.of(Long.parseLong(System.getProperty(MAX_COLLISIONS)));
        }

        String outPath = "output.txt";
        if (args.length > 2){
            outPath = args[1];
        }

        simulation.execute(maxTime, maxCollisions, outPath);
    }

    public Simulation(String particleFileName){
        board = parseParticlesFile(particleFileName);
    }

    /**
     * boardDiameter
     * obstacleRadius
     * id1 x1 y1 vx1 vy1 m1 r1
     * id2 x2 y2 vx2 vy2 m2 r2
     * ...
     */
    private Board parseParticlesFile(String particleFileName){
        Set<Particle> particles = new HashSet<>();
        double boardDiameter = 0;
        double obstacleRadius = 0;

        try (BufferedReader particlesReader = Files.newBufferedReader(Path.of(particleFileName))){
            boardDiameter = Double.parseDouble(particlesReader.readLine());
            obstacleRadius = Double.parseDouble(particlesReader.readLine());
            Stream<String> lines = particlesReader.lines();
            lines.forEach(line -> {
                    String[] info = line.split("[\t ]+");
                    particles.add( new Particle(
                        Long.parseLong(info[0]),
                        new Vector2D(Double.parseDouble(info[1]), Double.parseDouble(info[2])),
                        new Vector2D(Double.parseDouble(info[3]), Double.parseDouble(info[4])),
                        Double.parseDouble(info[5]),
                        Double.parseDouble(info[6])
                    ));
            });


            lines.close();
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        return new Board(boardDiameter, obstacleRadius, particles);
    }

    public void execute(Optional<Long> maxTime, Optional<Long> maxCollisions, String outFileName){
        double time = 0;
        long collisions = 0;

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(outFileName));

            writer.println(board.getParticles().size());

            while ((maxTime.isEmpty() || maxTime.get() > time) && (maxCollisions.isEmpty() || maxCollisions.get() > collisions)) {
                writer.println(time);
                writer.print(board);

                time = board.toNextCollisionTime(time);
                collisions++;
            }

            writer.close();

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}

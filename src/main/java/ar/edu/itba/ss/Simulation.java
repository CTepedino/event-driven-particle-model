package ar.edu.itba.ss;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class Simulation {
    private static final String MAX_TIME = "maxTime";
    private static final String MAX_COLLISIONS = "maxCollisions";

    private final Board board;

    public Simulation(String particleFileName){
        board = parseParticlesFile(particleFileName);
    }

    public static void main(String[] args){
        Simulation simulation = new Simulation(args[0]);

        Optional<Long> maxTime = System.getProperty(MAX_TIME) == null? Optional.empty() :
                Optional.of(Long.parseLong(System.getProperty(MAX_TIME)));

        Optional<Long> maxCollisions = System.getProperty(MAX_COLLISIONS) == null? Optional.empty() :
                Optional.of(Long.parseLong(System.getProperty(MAX_COLLISIONS)));

        String outPath = "output.txt";
        if (args.length > 2){
            outPath = args[1];
        }

        BiPredicate<Double, Long> cutCondition = (time, collisions) ->
            (maxTime.isEmpty() || maxTime.get() > time)
            && (maxCollisions.isEmpty() || maxCollisions.get() > collisions);

        simulation.execute(cutCondition, outPath);
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
                particles.add(new Particle(
                        Long.parseLong(info[0]),
                        new Vector2D(Double.parseDouble(info[1]), Double.parseDouble(info[2])),
                        new Vector2D(Double.parseDouble(info[3]), Double.parseDouble(info[4])),
                        Double.parseDouble(info[5]),
                        Double.parseDouble(info[6])
                ));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Board(boardDiameter, obstacleRadius, particles);
    }

    public void execute(BiPredicate<Double, Long> cutCondition, String outFileName){
        double time = 0;
        long collisions = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(outFileName))){

            writer.println(board.getParticles().size());

            while (cutCondition.test(time, collisions)) {
                writer.println(time);
                writer.print(board);

                Collision collision = board.toNextState();
                writer.println(collision);

                time = board.getTime();
                collisions = board.getCollisions();
            }

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}

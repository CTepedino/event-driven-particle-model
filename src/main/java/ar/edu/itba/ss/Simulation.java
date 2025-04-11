package ar.edu.itba.ss;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Simulation {
    public static void main(String[] args){

    }

    /**
     * boardDiameter
     * obstacleRadius
     * id1 x1 y1 vx1 vy1 m1 r1
     * id2 x2 y2 vx2 vy2 m2 r2
     * ...
     */
    public Board parseParticlesFile(String particleFileName){
        Set<Particle> particles = new HashSet<>();
        long boardDiameter = 0;
        double obstacleRadius = 0;

        try (BufferedReader particlesReader = Files.newBufferedReader(Path.of(particleFileName))){
            boardDiameter = Long.parseLong(particlesReader.readLine());
            obstacleRadius = Double.parseDouble(particlesReader.readLine());
            Stream<String> lines = particlesReader.lines();
            lines.skip(2).forEach(line -> {
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
            throw new RuntimeException("Error reading input file: " + particleFileName);
        }

        return new Board(boardDiameter, obstacleRadius, particles);
    }

    public void execute(){

    }
}

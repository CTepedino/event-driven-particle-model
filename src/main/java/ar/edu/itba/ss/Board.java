package ar.edu.itba.ss;

import java.util.Set;

public class Board {
    private final double radius;
    private final double obstacleRadius;
    private final Set<Particle> particles;

    private double time;
    private long collisions;

    private Collision lastCollision = new Collision(null, null, Double.MAX_VALUE);

    private static final double EPS = 1e-12;

    public Board(double diameter, double obstacleRadius, Set<Particle> particles){
        this.radius = diameter/2;
        this.obstacleRadius = obstacleRadius;
        this.particles = particles;
        this.time = 0;
        this.collisions = 0;
    }

    public double getTime() {
        return time;
    }

    public long getCollisions() {
        return collisions;
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

    public Collision toNextState(){
        Collision soonest = null;
        Collision candidate = null;
        double soonestTime = Double.MAX_VALUE;
        //Solo hace una actualización por llamado, aunque ocurran muchas en el mismo t. Puede optimizarse...
        for (Particle particle: particles){
            double wallTime = boardCollisionTime(particle);
            if (wallTime > 0 && wallTime < soonestTime){
                candidate = Collision.withWall(particle, wallTime);
                if (!candidate.equals(lastCollision)){
                    soonest = candidate;
                    soonestTime = wallTime;
                }
            }

            double obstacleTime = obstacleCollisionTime(particle);
            if (obstacleTime > 0 && obstacleTime < soonestTime){
                candidate = Collision.withObstacle(particle, obstacleTime);
                if (!candidate.equals(lastCollision)){
                    soonest = candidate;
                    soonestTime = obstacleTime;
                }
            }

            for (Particle other: particles){
                if (particle.getId() < other.getId()){
                    double particleTime = particleCollisionTime(particle, other);
                    if (particleTime > 0 && particleTime < soonestTime){
                        candidate = new Collision(particle, other, time);
                        if (!candidate.equals(lastCollision)){
                            soonest = candidate;
                            soonestTime = particleTime;
                        }
                    }
                }

            }
        }

        for (Particle particle: particles){
            particle.move(soonestTime);
        }

        lastCollision = soonest;

        soonest.execute();

        time += soonestTime;
        collisions++;

        return soonest;
    }

    private double cuadratic(double A, double B, double C){
        if (A < EPS) return -1;
        double disc = B*B - 4*A*C;
        if (disc < 0 && disc > -EPS) disc = 0;

        double sqrtD = Math.sqrt(disc);
        double t1 = (-B + sqrtD) / (2*A);
        double t2 = (-B - sqrtD) / (2*A);

        double result = -1;
        if (t1 >= 0 && t2 >= 0){
            return Math.min(t1, t2);
        } else if (t1 >= 0){
            result = t1;
        } else if (t2 >= 0){
            result = t2;
        }
        return result;
    }


    private double boardCollisionTime(Particle particle){
        double A = Math.pow(particle.getVelocity().magnitude(), 2);
        double B = 2 * particle.getPosition().dotProduct(particle.getVelocity());
        double C = Math.pow(particle.getPosition().magnitude(), 2) - Math.pow(radius - particle.getRadius(), 2);

        return cuadratic(A, B, C);
    }

    private double obstacleCollisionTime(Particle particle){
        if (obstacleRadius == 0){
            return -1;
        }

        double A = Math.pow(particle.getVelocity().magnitude(), 2);
        double B = 2 * particle.getPosition().dotProduct(particle.getVelocity());
        double C = Math.pow(particle.getPosition().magnitude(), 2) - Math.pow(obstacleRadius - particle.getRadius(), 2);

        return cuadratic(A, B, C);
    }

    private double particleCollisionTime(Particle a, Particle b){
        Vector2D deltaR = a.getPosition().subtract(b.getPosition());
        Vector2D deltaV = a.getVelocity().subtract(b.getVelocity());
        double sigma = a.getRadius() + b.getRadius();
        double dotDrDr = deltaR.dotProduct(deltaR);
        double dotDvDv = deltaV.dotProduct(deltaV);
        double dotDvDr = deltaR.dotProduct(deltaV);

        if (dotDvDr >= 0){
            return -1;
        }
        double d = dotDvDr * dotDvDr - dotDvDv * (dotDrDr - sigma*sigma);
        if (d < 0){
            return -1;
        }
        return (-dotDvDr + Math.sqrt(d))/(dotDvDv);
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

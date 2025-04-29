package ar.edu.itba.ss;

import java.util.Locale;
import java.util.Objects;

public class Collision {
    private final Particle a;
    private final Particle b;

    private final double time;

    private  CollisionType type;

    public Collision(Particle a, Particle b, double time){
        this.a = a;
        this.b = b;
        this.time = time;
        this.type = CollisionType.PARTICLE;
    }

    public static Collision withWall(Particle a, double time){
        Collision col = new Collision(a, null, time);
        col.type = CollisionType.WALL;
        return col;
    }

    public static Collision withObstacle(Particle a, double time){
        Collision col = new Collision(a, null, time);
        col.type = CollisionType.OBSTACLE;
        return col;
    }

    public Particle getA() {
        return a;
    }

    public Particle getB() {
        return b;
    }

    public double getTime() {
        return time;
    }

    public CollisionType getType() {
        return type;
    }


    public void execute(){
        switch (type){
            case WALL, OBSTACLE:
                a.bounce();
                break;
            case PARTICLE:
                a.collideWithParticle(b);
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Collision collision = (Collision) o;
        return Objects.equals(a, collision.a) && Objects.equals(b, collision.b) && type == collision.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, type);
    }

    @Override
    public String toString() {
        return switch (type) {
            case WALL -> String.format(Locale.US, "%d W", a.getId());
            case OBSTACLE -> String.format(Locale.US, "%d O", a.getId());
            case PARTICLE -> String.format(Locale.US, "%d %d", a.getId(), b.getId());
        };
    }
}

package ar.edu.itba.ss;

import java.util.Objects;

public class Collision {
    private final Particle a;
    private final Particle b;

    private final double time;

    public boolean withWall = false;
    public boolean withObstacle = false;

    public Collision(Particle a, Particle b, double time){
        this.a = a;
        this.b = b;
        this.time = time;
    }

    public static Collision withWall(Particle a, double time){
        Collision col = new Collision(a, null, time);
        col.withWall = true;
        return col;
    }

    public static Collision withObstacle(Particle a, double time){
        Collision col = new Collision(a, null, time);
        col.withObstacle = true;
        return col;
    }

    public double getTime(){
        return time;
    }

    public Particle getA(){
        return a;
    }

    public Particle getB(){
        return b;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof Collision)){
            return false;
        }
        Collision other = (Collision) o;
        if ((withWall && other.withWall) || (withObstacle && other.withObstacle)){
            return a.equals(other.a);
        }
        return a.equals(other.a) && b.equals(other.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, withWall, withObstacle);
    }
}

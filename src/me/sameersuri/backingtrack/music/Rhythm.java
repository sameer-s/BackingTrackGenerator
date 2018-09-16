package me.sameersuri.backingtrack.music;

public class Rhythm {
    private double beat;
    private double duration;
    private int velocity;

    public Rhythm(double beat, double duration, int velocity) {
        this.beat = beat;
        this.duration = duration;
        this.velocity = velocity;
    }

    public Rhythm(double beat, double duration) {
        this(beat, duration, 64);
    }

    public double getBeat() {
        return beat;
    }

    public double getDuration() {
        return duration;
    }

    public int getVelocity() {
        return velocity;
    }
}

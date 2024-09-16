package dev.enjarai.minitardis.component;

import net.minecraft.util.math.random.Random;

public class FlightWave {
    private double magnitude;
    private double period;
    private double offset;

    public FlightWave(double magnitude, double period, double offset) {
        this.magnitude = magnitude;
        this.period = period;
        this.offset = offset;
    }

    public FlightWave(Random random) {
        this(0, 0, 0);
        shuffle(random);
    }

    public void shuffle(Random random) {
        magnitude = 0.2 + random.nextDouble() * 0.8;
        period = 0.2 + random.nextDouble() * 0.8;
        offset = random.nextDouble();
    }

    public double getMagnitude() {
        return magnitude;
    }

    public double getPeriod() {
        return period;
    }

    public double getOffset() {
        return offset;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}

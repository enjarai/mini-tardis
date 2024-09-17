package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.random.Random;

public class FlightWave {
    public static final Codec<FlightWave> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("magnitude").forGetter(FlightWave::getMagnitude),
            Codec.DOUBLE.fieldOf("period").forGetter(FlightWave::getPeriod),
            Codec.DOUBLE.fieldOf("offset").forGetter(FlightWave::getOffset)
    ).apply(instance, FlightWave::new));

    private double magnitude;
    private double period;
    private double offset;

    public FlightWave(double magnitude, double period, double offset) {
        this.magnitude = snap(magnitude);
        this.period = snap(period);
        this.offset = snap(offset);
    }

    public FlightWave(Random random) {
        this(0, 0, 0);
        shuffle(random);
    }

    public void shuffle(Random random) {
        magnitude = 0.1 + snap(random.nextDouble()) * 0.9;
        period = 0.1 + snap(random.nextDouble()) * 0.9;
        offset = snap(random.nextDouble());
    }

    private double snap(double x) {
        return Math.round(x * 10) / 10.0;
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
        this.magnitude = snap(magnitude);
    }

    public void setPeriod(double period) {
        this.period = snap(period);
    }

    public void setOffset(double offset) {
        this.offset = snap(offset);
    }

    public int getValue(int i) {
        return (int) Math.round(switch (i) {
            case 0 -> (magnitude - 0.1) / 0.9;
            case 1 -> (period - 0.1) / 0.9;
            case 2 -> offset;
            default -> 0;
        } * 10);
    }

    public void setValue(int i, int value) {
        double scaled = value / 10.0;
        switch (i) {
            case 0 -> magnitude = 0.1 + snap(scaled) * 0.9;
            case 1 -> period = 0.1 + snap(scaled) * 0.9;
            case 2 -> offset = snap(scaled);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightWave that)) return false;

        return Double.compare(magnitude, that.magnitude) == 0 && Double.compare(period, that.period) == 0 && Double.compare(offset, that.offset) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(magnitude);
        result = 31 * result + Double.hashCode(period);
        result = 31 * result + Double.hashCode(offset);
        return result;
    }
}

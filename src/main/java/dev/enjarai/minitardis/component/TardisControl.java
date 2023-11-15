package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.flight.LandingState;
import dev.enjarai.minitardis.component.flight.TakingOffState;
import net.minecraft.util.math.Direction;

import java.util.Optional;

/**
 * Takes in button presses and other inputs from the Tardis console and translates them into actions performed on it.
 */
public class TardisControl {
    public static final Codec<TardisControl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("coordinate_scale").forGetter(c -> c.coordinateScale)
    ).apply(instance, TardisControl::new));

    private int coordinateScale;

    Tardis tardis;

    private TardisControl(int coordinateScale) {
        this.coordinateScale = coordinateScale;
    }

    public TardisControl() {
        this(1);
    }


    public boolean resetDestination() {
        tardis.setDestination(tardis.getCurrentLocation());
        return true;
    }

    public boolean updateCoordinateScale(int scale) {
        coordinateScale = scale;
        return true;
    }

    public boolean nudgeDestination(Direction direction) {
        tardis.setDestination(tardis.getDestination()
                .map(d -> d.with(d.pos().add(direction.getVector().multiply(coordinateScale)))));
        return tardis.getDestination().isPresent();
    }

    public boolean handbrake(boolean state) {
        return tardis.suggestStateTransition(state ? new TakingOffState() : new LandingState());
    }
}

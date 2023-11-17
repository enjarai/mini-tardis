package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.flight.LandingState;
import dev.enjarai.minitardis.component.flight.SearchingForLandingState;
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

    @SuppressWarnings("CopyConstructorMissesField")
    public TardisControl(TardisControl copyFrom) {
        this(copyFrom.coordinateScale);
    }

    public TardisControl() {
        this(1);
    }


    public boolean resetDestination() {
        if (tardis.getCurrentLocation().isEmpty()) return false;

        return tardis.setDestination(tardis.getCurrentLocation(), false);
    }

    public boolean updateCoordinateScale(int scale) {
        coordinateScale = scale;
        return true;
    }

    public boolean nudgeDestination(Direction direction) {
        return tardis.setDestination(tardis.getDestination()
                .map(d -> {
                    if (direction.getAxis().isVertical()) {
                        return snapLocationVertically(d, direction);
                    } else {
                        return d.with(d.pos().add(direction.getVector().multiply(coordinateScale)));
                    }
                }), false)
                && tardis.getDestination().isPresent();
    }

    private TardisLocation snapLocationVertically(TardisLocation location, Direction direction) {
        var world = tardis.getDestinationWorld();
        if (world.isPresent()) {
            for (var pos = location.pos().offset(direction); world.get().isInBuildLimit(pos); pos = pos.offset(direction)) {
                var checkLocation = location.with(pos);
                if (tardis.canSnapDestinationTo(checkLocation)) {
                    return checkLocation;
                }
            }
        }
        return location;
    }

    public boolean rotateDestination(Direction direction) {
        return tardis.setDestination(tardis.getDestination()
                .map(d -> d.with(direction)), false)
                && tardis.getDestination().isPresent();
    }

    public boolean handbrake(boolean state) {
        return tardis.suggestStateTransition(state ? new TakingOffState() : new SearchingForLandingState());
    }
}

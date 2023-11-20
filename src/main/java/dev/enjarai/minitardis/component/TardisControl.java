package dev.enjarai.minitardis.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.flight.SearchingForLandingState;
import dev.enjarai.minitardis.component.flight.TakingOffState;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.*;

/**
 * Takes in button presses and other inputs from the Tardis console and translates them into actions performed on it.
 */
public class TardisControl {
    public static final Codec<TardisControl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("coordinate_scale").forGetter(c -> c.coordinateScale),
            ScreenApp.CODEC.listOf().fieldOf("screen_apps").forGetter(c -> ImmutableList.copyOf(c.screenApps.values())),
            Codec.BOOL.optionalFieldOf("destination_locked", false).forGetter(c -> c.destinationLocked)
    ).apply(instance, TardisControl::new));

    private int coordinateScale;
    private final Map<Identifier, ScreenApp> screenApps;
    private boolean destinationLocked;

    Tardis tardis;

    private TardisControl(int coordinateScale, Collection<ScreenApp> screenApps, boolean destinationLocked) {
        this.coordinateScale = coordinateScale;
        var builder = ImmutableMap.<Identifier, ScreenApp>builder();
        ScreenApp.CONSTRUCTORS.forEach((key, value) -> builder.put(key, value.get()));
        screenApps.forEach(app -> builder.put(app.id(), app));
        this.screenApps = builder.buildKeepingLast();
        this.destinationLocked = destinationLocked;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public TardisControl(TardisControl copyFrom) {
        this(copyFrom.coordinateScale, copyFrom.screenApps.values(), copyFrom.destinationLocked);
    }

    public TardisControl() {
        this(1, List.of(), false);
    }


    public boolean resetDestination() {
        if (tardis.getCurrentLocation().isEmpty()) {
            minorMalfunction();
            return false;
        }

        var success = tardis.setDestination(tardis.getCurrentLocation(), false);

        if (success) {
            destinationLocked = false;
        }
        return success;
    }

    public boolean updateCoordinateScale(int scale) {
        coordinateScale = scale;
        return true;
    }

    public boolean nudgeDestination(Direction direction) {
        var success = tardis.setDestination(tardis.getDestination()
                .map(d -> {
                    if (direction.getAxis().isVertical()) {
                        return snapLocationVertically(d, direction);
                    } else {
                        return d.with(d.pos().add(direction.getVector().multiply(coordinateScale)));
                    }
                }), false)
                && tardis.getDestination().isPresent();
        if (success) {
            destinationLocked = false;
        }
        return success;
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
        var success = tardis.setDestination(tardis.getDestination()
                .map(d -> d.with(direction)), false)
                && tardis.getDestination().isPresent();
        if (success) {
            destinationLocked = false;
        }
        return success;
    }

    public boolean handbrake(boolean state) {
        return tardis.suggestStateTransition(state ? new TakingOffState() : new SearchingForLandingState(false));
    }

    public boolean isDestinationLocked() {
        return destinationLocked;
    }

    public boolean setDestinationLocked(boolean destinationLocked, boolean force) {
        if (force || tardis.getState().tryChangeCourse(tardis)) {
            this.destinationLocked = destinationLocked;
            return true;
        }
        return false;
    }


    public void minorMalfunction() {
        tardis.destabilize(1);
    }

    public void moderateMalfunction() {
        tardis.destabilize(20);
    }

    public void majorMalfunction() {
        tardis.destabilize(100);
    }


    public Tardis getTardis() {
        return tardis;
    }

    public Optional<ScreenApp> getScreenApp(Identifier id) {
        return Optional.ofNullable(screenApps.get(id));
    }

    public List<ScreenApp> getAllApps() {
        return screenApps.values().stream().sorted(Comparator.comparing(ScreenApp::id)).toList();
    }
}

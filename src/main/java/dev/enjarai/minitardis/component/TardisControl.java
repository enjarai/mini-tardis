package dev.enjarai.minitardis.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.flight.*;
import dev.enjarai.minitardis.component.screen.app.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Takes in button presses and other inputs from the Tardis console and translates them into actions performed on it.
 */
public class TardisControl {
    public static final Codec<TardisControl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("coordinate_scale").forGetter(c -> c.coordinateScale),
            ScreenApp.CODEC.listOf().fieldOf("screen_apps").forGetter(c -> ImmutableList.copyOf(c.screenApps.values())),
            Codec.BOOL.optionalFieldOf("destination_locked", false).forGetter(c -> c.destinationLocked),
            Codec.BOOL.optionalFieldOf("energy_conduits_unlocked", false).forGetter(c -> c.energyConduitsUnlocked)
    ).apply(instance, TardisControl::new));

    private int coordinateScale;
    private final Map<ScreenAppType<?>, ScreenApp> screenApps;
    private boolean destinationLocked;
    private boolean energyConduitsUnlocked;

    Tardis tardis;

    private TardisControl(int coordinateScale, Collection<ScreenApp> screenApps, boolean destinationLocked, boolean energyConduitsUnlocked) {
        this.coordinateScale = coordinateScale;
        this.screenApps = new HashMap<>();
//        ScreenApp.CONSTRUCTORS.forEach((key, value) -> builder.put(key, value.get()));
        screenApps.forEach(app -> this.screenApps.put(app.getType(), app));
        this.destinationLocked = destinationLocked;
        this.energyConduitsUnlocked = energyConduitsUnlocked;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public TardisControl(TardisControl copyFrom) {
        this(copyFrom.coordinateScale, copyFrom.screenApps.values(), copyFrom.destinationLocked, copyFrom.energyConduitsUnlocked);
    }

    public TardisControl() {
        this(1, List.of(new PackageManagerApp(), new StatusApp(), new GpsApp(), new HistoryApp()), false, false);
    }


    public boolean resetDestination() {
        if (tardis.getCurrentLandedLocation().isEmpty()) {
            tardis.shuffleFlightWave();
            return false;
        }

        var success = tardis.setDestination(tardis.getCurrentLandedLocation(), false);

        if (success) {
            destinationLocked = false;
        }
        return success;
    }

    public boolean updateCoordinateScale(int scale) {
        coordinateScale = scale;
        return true;
    }

    public int getScaleState() {
        return (int) Math.log10(coordinateScale);
    }

    public boolean nudgeDestination(Direction direction) {
        if (!direction.getAxis().isVertical()) {
            switch (tardis.getState()) {
                case FlyingState state -> {
                    if (isDestinationLocked()) {
                        var axis = direction.getAxis().ordinal() == 0 ? 1 : 0;
                        var i = state.scaleState * 2 + axis;
                        var original = state.offsets[i];
                        state.offsets[i] = MathHelper.clamp(original - direction.getDirection().offset(), -1, 1);
                        return true;
                    }
                }
                case RespondsToNudging state -> {
                    return state.nudgeDestination(getTardis(), direction);
                }
                default -> {}
            }
        }

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
            if (!world.get().isInBuildLimit(location.pos())) {
                location = location.with(location.pos().withY(MathHelper.clamp(
                        location.pos().getY(),
                        world.get().getBottomY(),
                        world.get().getTopY()
                )));
            }
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
        if (!state && tardis.getState() instanceof FlyingState && !isDestinationLocked()) {
            return tardis.getDestinationTardis()
                    .map(otherTardis -> tardis.suggestStateTransition(new InterdictingState(otherTardis.uuid())))
                    .orElseGet(() -> tardis.suggestStateTransition(new DriftingState()));
        } else if (tardis.getState() instanceof RespondsToFlyLever respondingState) {
            return respondingState.toggleFlyLever(tardis, state) || tardis.suggestStateTransition(new FlyingState(tardis.getRandom().nextInt()));
        }

        if (tardis.isDoorOpen()) return false;

        return tardis.suggestStateTransition(state ? new TakingOffState() : new SearchingForLandingState(false, 0));
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

    public boolean areEnergyConduitsUnlocked() {
        return energyConduitsUnlocked;
    }

    public boolean setEnergyConduits(boolean unlocked) {
        if (!tardis.getState().isSolid(tardis)) {
            if (!unlocked && tardis.getState(FlyingState.class).isPresent()) {
                tardis.suggestStateTransition(new SuspendedFlightState());
            } else if (unlocked && tardis.getState(SuspendedFlightState.class).isPresent()) {
                tardis.suggestStateTransition(new FlyingState(tardis.getState(SuspendedFlightState.class).get().distance));
            } else if (!unlocked) {
                majorMalfunction();
                return false;
            }
        } else if (unlocked && tardis.getState() instanceof RefuelingState) {
            return false;
        }

        energyConduitsUnlocked = unlocked;
        return true;
    }

    public boolean refuelToggle(boolean state) {
        return tardis.suggestStateTransition(state ? new RefuelingState() : new LandedState());
    }

    public boolean moveDestinationToDimension(RegistryKey<World> worldKey) {
        var success = tardis.setDestination(tardis.getDestination().map(l -> l.with(worldKey)), false) &&
                tardis.getDestination().isPresent();
        if (success) {
            destinationLocked = false;
        }
        return success;
    }

    public boolean toggleDisabledState() {
        return tardis.suggestStateTransition(tardis.getState() instanceof DisabledState ? new BootingUpState() : new DisabledState());
    }


    public void minorMalfunction() {
        tardis.destabilize(10);
    }

    public void moderateMalfunction() {
        tardis.destabilize(200);
    }

    public void majorMalfunction() {
        tardis.destabilize(1000);
    }


    public Tardis getTardis() {
        return tardis;
    }

    public Optional<ScreenApp> getScreenApp(@Nullable ScreenAppType<?> type) {
        return Optional.ofNullable(screenApps.get(type));
    }



    public List<ScreenApp> getAllApps() {
        return screenApps.values().stream().sorted(Comparator.comparing(ScreenApp::getId)).toList();
    }

    public boolean canUninstallApp(ScreenAppType<?> type) {
        return screenApps.containsKey(type) && screenApps.get(type).canBeUninstalled();
    }

    public Optional<ScreenApp> uninstallApp(ScreenAppType<?> type) {
        return Optional.ofNullable(screenApps.remove(type));
    }

    public boolean canInstallApp(ScreenApp app) {
        return !screenApps.containsKey(app.getType());
    }

    public boolean installApp(ScreenApp app) {
        if (!screenApps.containsKey(app.getType()) && screenApps.size() < 12) {
            screenApps.put(app.getType(), app);
            return true;
        }
        return false;
    }
}

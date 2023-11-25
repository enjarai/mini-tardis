package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import static dev.enjarai.minitardis.component.flight.FlyingState.SOUND_LOOP_LENGTH;

public class DriftingState implements FlightState {
    public static final Codec<DriftingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks)
    ).apply(instance, DriftingState::new));
    public static final Identifier ID = MiniTardis.id("drifting");
    private static final int TRANSITION_POINT = 20 * 4;

    private int flyingTicks;

    DriftingState(int flyingTicks) {
        this.flyingTicks = flyingTicks;
    }

    public DriftingState() {
        this(0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, 1);
        }

        tickScreenShake(tardis, 2);

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true);
        }

        if (flyingTicks % 5 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().moderateMalfunction();
            return this;
        }

        tardis.destabilize(1);

        if (flyingTicks == TRANSITION_POINT) {
            tardis.getControls().setDestinationLocked(true, true);
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof FlyingState) {
            if (tardis.getControls().isDestinationLocked()) {
                tardis.getDestination().ifPresent(destination -> {
                    tardis.setCurrentLocation(new PartialTardisLocation(destination.worldKey()));
                });
            }
            return true;
        }
        tardis.getControls().moderateMalfunction();
        return false;
    }

    @Override
    public boolean tryChangeCourse(Tardis tardis) {
        return false;
    }

    @Override
    public boolean isSolid(Tardis tardis) {
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

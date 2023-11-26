package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import static dev.enjarai.minitardis.component.flight.FlyingState.SOUND_LOOP_LENGTH;

public class DriftingState implements FlightState {
    public static final Codec<DriftingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks)
    ).apply(instance, DriftingState::new));
    public static final Identifier ID = MiniTardis.id("drifting");
    private static final int TRANSITION_POINT = 20 * 4;

    int flyingTicks;
    private int driftingTicks;

    DriftingState(int flyingTicks) {
        this.flyingTicks = flyingTicks;
    }

    public DriftingState() {
        this(0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        driftingTicks++;
        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            float errorPitch = tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;

            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                    SoundCategory.BLOCKS, 0.6f, errorPitch);
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, 1);
        }

        tickScreenShake(tardis, 2);

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true);
        }

        // Drifting actually refuels mid-flight
        tardis.addOrDrainFuel(1);
        tardis.destabilize(4);

        if (driftingTicks == TRANSITION_POINT) {
            tardis.getControls().setDestinationLocked(true, true);
            playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 1);
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof FlyingState flyingState) {
            if (tardis.getControls().isDestinationLocked()) {
                tardis.getDestination().ifPresent(destination -> {
                    tardis.setCurrentLocation(new PartialTardisLocation(destination.worldKey()));
                });
            }
            flyingState.flyingTicks = flyingTicks;
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
    public boolean isInteriorLightEnabled(int order) {
        order--;
        return flyingTicks / 5 % 2 == order / 6;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

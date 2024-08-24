package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.ccacomponent.PartialTardisLocation;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import static dev.enjarai.minitardis.ccacomponent.flight.FlyingState.SOUND_LOOP_LENGTH;

public class DriftingState implements FlightState {
    public static final Codec<DriftingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("phase_count").forGetter(s -> s.phaseCount),
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("phase_length").forGetter(s -> s.phaseLength),
            Codec.INT.fieldOf("phase_ticks").forGetter(s -> s.phaseTicks),
            Codec.INT.fieldOf("phases_complete").forGetter(s -> s.phasesComplete)
    ).apply(instance, DriftingState::new));
    public static final Identifier ID = MiniTardis.id("drifting");
    private static final int TRANSITION_POINT = 20 * 2;

    public int phaseCount;
    int flyingTicks;
    public int phaseLength;
    public int phaseTicks;
    public int phasesComplete;

    DriftingState(int phaseCount, int flyingTicks, int phaseLength, int phaseTicks, int phasesComplete) {
        this.phaseCount = phaseCount;
        this.flyingTicks = flyingTicks;
        this.phaseLength = phaseLength;
        this.phaseTicks = phaseTicks;
        this.phasesComplete = phasesComplete;
    }

    public DriftingState() {
        this(0, 0, 0, 0, 0);
    }

    @Override
    public void init(Tardis tardis) {
        phaseCount = tardis.getRandom().nextBetween(2, 3) * 2 - 1;
        phaseLength = tardis.getRandom().nextBetween(20, TRANSITION_POINT);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        phaseTicks++;
        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            float errorPitch = tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;

            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                    SoundCategory.BLOCKS, 0.6f, errorPitch);
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, 1);
        }

//        tickScreenShake(tardis, 2);

        if (phasesComplete >= phaseCount) {
            if (phaseTicks == 11) {
                playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 0.9f);
            } else if (phaseTicks == 16) {
                playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 1.1f);
            } else if (phaseTicks >= 21) {
                tardis.getControls().setDestinationLocked(true, true);
                tardis.getDestination().ifPresent(destination -> {
                    tardis.setCurrentLocation(new PartialTardisLocation(destination.worldKey()));
                });
                var flyingState = new FlyingState(tardis.getRandom().nextInt());
                flyingState.flyingTicks = flyingTicks;
                return flyingState;
            }
        }

        if (tardis.getStability() <= 0) {
            if (tardis.getControls().isDestinationLocked()) {
                tardis.getDestination().ifPresent(destination -> {
                    tardis.setCurrentLocation(new PartialTardisLocation(destination.worldKey()));
                });
            }
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        // Drifting actually refuels mid-flight
        tardis.addOrDrainFuel(1);
        tardis.destabilize(4);

        if (phaseTicks == phaseLength) {
            playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 1);
        }

        return this;
    }

    public boolean toggleFlyLever(Tardis tardis, boolean active) {
        if (phaseTicks >= phaseLength) {
            if (phasesComplete < phaseCount) {
                phasesComplete++;
                phaseTicks = 0;
                phaseLength = tardis.getRandom().nextBetween(5, TRANSITION_POINT);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof FlyingState flyingState) {
            flyingState.flyingTicks = flyingTicks;
            flyingState.errorLoops = 2;
            return true;
        }
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

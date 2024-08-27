package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class SuspendedFlightState implements FlightState {
    public static final MapCodec<SuspendedFlightState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("errorLoops").forGetter(s -> s.errorLoops),
            Codec.INT.optionalFieldOf("distance", 0).forGetter(s -> s.distance)
    ).apply(instance, SuspendedFlightState::new));
    public static final Identifier ID = MiniTardis.id("suspended_flight");
    static final int SOUND_LOOP_LENGTH = 32;
    private static final int AFTERSHAKE_LENGTH = 80;

    int flyingTicks;
    int aftershakeTicks;
    public int errorLoops;
    public int distance;

    private SuspendedFlightState(int flyingTicks, int errorLoops, int distance) {
        this.flyingTicks = flyingTicks;
        this.errorLoops = errorLoops;
        this.distance = distance;
    }

    public SuspendedFlightState() {
        this(0, 0, 0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        aftershakeTicks++;
        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            var isError = errorLoops > 0;

            if (isError) {
                errorLoops--;

                float errorPitch = tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;

                playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                        SoundCategory.BLOCKS, 0.2f, errorPitch);
            }

            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.05f, 1);
        }

        var shakeIntensity = (AFTERSHAKE_LENGTH - aftershakeTicks) / (float) AFTERSHAKE_LENGTH;
        if (shakeIntensity > 0) {
            tickScreenShake(tardis, shakeIntensity);
        }

        var stability = tardis.getStability();
        if (stability <= 0) {
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        if (flyingTicks % 2 == 0 && stability < 1000) {
            tardis.setStability(stability + 1);
        }

        if (flyingTicks % 100 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().moderateMalfunction();
            return this;
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof FlyingState flyingState) {
            flyingState.flyingTicks = flyingTicks;
            flyingState.setOffsets(distance);
            return true;
        } else if (newState instanceof DriftingState driftingState) {
            driftingState.flyingTicks = flyingTicks;
            return true;
        } else if (newState instanceof TakingOffState) {
            return false;
        }
        tardis.getControls().minorMalfunction();
        return false;
    }

    @Override
    public boolean isSolid(Tardis tardis) {
        return false;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return FlyingState.spinnyLighting(order, flyingTicks);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

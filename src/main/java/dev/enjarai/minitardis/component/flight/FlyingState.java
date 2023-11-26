package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class FlyingState implements FlightState {
    public static final Codec<FlyingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("errorLoops").forGetter(s -> s.errorLoops)
    ).apply(instance, FlyingState::new));
    public static final Identifier ID = MiniTardis.id("flying");
    static final int SOUND_LOOP_LENGTH = 32;
    private static final int AFTERSHAKE_LENGTH = 80;

    int flyingTicks;
    int aftershakeTicks;
    public int errorLoops;

    private FlyingState(int flyingTicks, int errorLoops) {
        this.flyingTicks = flyingTicks;
        this.errorLoops = errorLoops;
    }

    public FlyingState() {
        this(0, 0);
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
                        SoundCategory.BLOCKS, 0.6f, errorPitch);
            }

            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, 1);
        }

        var shakeIntensity = (AFTERSHAKE_LENGTH - aftershakeTicks) / (float) AFTERSHAKE_LENGTH;
        if (shakeIntensity > 0) {
            tickScreenShake(tardis, shakeIntensity);
        }

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true);
        }

        if (flyingTicks % 10 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().moderateMalfunction();
            return this;
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof SearchingForLandingState landingState) {
            if (!tardis.getDestination().map(destination -> tardis.getExteriorWorldKey().equals(destination.worldKey())).orElse(false)) {
                tardis.getControls().minorMalfunction();
                return false;
            }

            if (!landingState.crashing && !tardis.getControls().isDestinationLocked()) {
                tardis.getControls().moderateMalfunction();
                return false;
            }

            landingState.flyingTicks = flyingTicks;
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
        return spinnyLighting(order, flyingTicks);
    }

    public static boolean spinnyLighting(int order, int flyingTicks) {
        if (order == 0) {
            return true;
        }
        order--;
        var pointInCycle = flyingTicks / 5 % 12;
        var pointInCycleOffset = (flyingTicks / 5 + 4) % 12;
        return pointInCycle < pointInCycleOffset
                ? order % 12 >= pointInCycle && order % 12 < pointInCycleOffset
                : order % 12 >= pointInCycle || order % 12 < pointInCycleOffset;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

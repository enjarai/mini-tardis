package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.stream.IntStream;

public class FlyingState implements FlightState {
    public static final MapCodec<FlyingState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("errorLoops").forGetter(s -> s.errorLoops),
            Codec.INT_STREAM.fieldOf("offsets").forGetter(s -> Arrays.stream(s.offsets)),
            Codec.INT.fieldOf("scale_state").forGetter(s -> s.scaleState)
    ).apply(instance, FlyingState::new));
    public static final Identifier ID = MiniTardis.id("flying");
    static final int SOUND_LOOP_LENGTH = 32;
    private static final int AFTERSHAKE_LENGTH = 80;

    int flyingTicks;
    int aftershakeTicks;
    public int errorLoops;
    public int[] offsets;
    public int scaleState;

    private FlyingState(int flyingTicks, int errorLoops, IntStream offsets, int scaleState) {
        this.flyingTicks = flyingTicks;
        this.errorLoops = errorLoops;
        this.offsets = offsets.toArray();
        this.scaleState = scaleState;
    }

    public FlyingState(int distance) {
        this.flyingTicks = 0;
        this.errorLoops = 0;
        this.offsets = new int[8];
        this.scaleState = 0;
        setOffsets(distance);
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
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        if (tardis.getControls().isDestinationLocked()) {
            scaleState = tardis.getControls().getScaleState();
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

            landingState.errorDistance = getDistance();
            landingState.flyingTicks = flyingTicks;
            return true;
        } else if (newState instanceof DriftingState driftingState) {
            driftingState.flyingTicks = flyingTicks;
            return true;
        } else if (newState instanceof SuspendedFlightState suspendedFlightState) {
            suspendedFlightState.flyingTicks = flyingTicks;
            suspendedFlightState.distance = getDistance();
            return true;
        } else if (newState instanceof TakingOffState) {
            return false;
        }
        tardis.getControls().minorMalfunction();
        return false;
    }

    public void setOffsets(int distance) {
        for (int i = 0; i < offsets.length; i++) {
            int value = distance >> i * 2 & 0b11;
            offsets[i] = switch (value) {
                case 1 -> -1;
                case 2 -> 1;
                default -> 0;
            };
        }
    }

    public int getDistance() {
        int result = 0;
        for (int i = 0; i < offsets.length; i++) {
            int value = switch (offsets[i]) {
                case -1 -> 1;
                case 1 -> 2;
                default -> 0;
            };
            result |= value << i * 2;
        }
        return result;
    }

    public static int trimDistance(int distance) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            int value = distance >> i * 2 & 0b11;
            if (value == 3) value = 0;
            result |= value << i * 2;
        }
        return result;
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

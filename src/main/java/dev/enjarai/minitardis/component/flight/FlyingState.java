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

    private int flyingTicks;
    public int errorLoops;

    FlyingState(int flyingTicks, int errorLoops) {
        this.flyingTicks = flyingTicks;
        this.errorLoops = errorLoops;
    }

    public FlyingState() {
        this(0, 0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            var isError = errorLoops > 0;
            float pitch = 1;

            if (isError) {
                errorLoops--;

                pitch += tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;
            }

            playForInterior(tardis,
                    isError ? ModSounds.TARDIS_FLY_LOOP_ERROR : ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, pitch);
        }

        var shakeIntensity = (AFTERSHAKE_LENGTH - flyingTicks) / (float) AFTERSHAKE_LENGTH;
        if (shakeIntensity > 0) {
            tickScreenShake(tardis, shakeIntensity);
        }

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true);
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof SearchingForLandingState landingState) {
            landingState.flyingTicks = flyingTicks;
            return true;
        }
        tardis.getControls().minorMalfunction();
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

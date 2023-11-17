package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class FlyingState implements FlightState {
    public static final Codec<FlyingState> CODEC = Codec.INT
            .xmap(FlyingState::new, s -> s.flyingTicks).fieldOf("flying_ticks").codec();
    public static final Identifier ID = MiniTardis.id("flying");
    static final int SOUND_LOOP_LENGTH = 32;
    private static final int AFTERSHAKE_LENGTH = 80;

    private int flyingTicks;

    FlyingState(int flyingTicks) {
        this.flyingTicks = flyingTicks;
    }

    public FlyingState() {
        this(0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP, SoundCategory.BLOCKS, 0.6f, 1);
        }

        var shakeIntensity = (AFTERSHAKE_LENGTH - flyingTicks) / (float) AFTERSHAKE_LENGTH;
        if (shakeIntensity > 0) {
            tickScreenShake(tardis, shakeIntensity);
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof SearchingForLandingState landingState) {
            landingState.flyingTicks = flyingTicks;
            return true;
        }
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

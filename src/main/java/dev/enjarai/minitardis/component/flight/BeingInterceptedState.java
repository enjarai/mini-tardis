package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class BeingInterceptedState implements FlightState {
    public static final MapCodec<BeingInterceptedState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Uuids.CODEC.fieldOf("origin").forGetter(s -> s.origin),
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks)
    ).apply(instance, BeingInterceptedState::new));
    public static final Identifier ID = MiniTardis.id("being_intercepted");

    private final UUID origin;
    int flyingTicks;

    private BeingInterceptedState(UUID origin, int flyingTicks) {
        this.origin = origin;
        this.flyingTicks = flyingTicks;
    }

    public BeingInterceptedState(UUID origin) {
        this(origin, 0);
    }

    @Override
    public void init(Tardis tardis) {
        tardis.setSparksQueued(5);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;

        if (flyingTicks % FlyingState.SOUND_LOOP_LENGTH == 0) {
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                    SoundCategory.BLOCKS, 1, tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f);
        }

        if (flyingTicks % 100 == 0) {
            playForInteriorAndExterior(tardis, ModSounds.CLOISTER_BELL, SoundCategory.BLOCKS, 1, 1);
        }

        var originTardis = tardis.getHolder().getTardis(origin);

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        if (flyingTicks % 2 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().moderateMalfunction();
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public float getScreenShakeIntensity(Tardis tardis) {
        return 2;
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

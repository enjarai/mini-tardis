package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static dev.enjarai.minitardis.component.flight.FlyingState.SOUND_LOOP_LENGTH;

public class InterceptingState implements FlightState {
    public static final MapCodec<InterceptingState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Uuids.CODEC.fieldOf("target").forGetter(s -> s.target),
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks)
    ).apply(instance, InterceptingState::new));
    public static final Identifier ID = MiniTardis.id("intercepting");

    private final UUID target;
    int flyingTicks;

    private InterceptingState(UUID target, int flyingTicks) {
        this.target = target;
        this.flyingTicks = flyingTicks;
    }

    public InterceptingState(UUID target) {
        this(target, 0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;

        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            float errorPitch = tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;

            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                    SoundCategory.BLOCKS, 0.6f, errorPitch);
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, 1);
        }

        var targetTardis = tardis.getHolder().getTardis(target);
        if (targetTardis.isEmpty()) {
            tardis.getControls().majorMalfunction();
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        if (targetTardis.get().getState(BeingInterceptedState.class).isEmpty()) {
            if (!targetTardis.get().suggestStateTransition(new BeingInterceptedState(tardis.uuid()))) {
                tardis.getControls().moderateMalfunction();
                return new FlyingState(tardis.getRandom().nextInt());
            }
        }

//        // If the target somehow changes state, we succeed the intercept.
//        tardis.setCurrentLocation(new PartialTardisLocation(targetTardis.get().getInteriorWorld().getRegistryKey()));
//        return new SearchingForLandingState(false, 0);

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        tardis.destabilize(2);

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

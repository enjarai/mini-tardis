package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisLocation;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class CrashingState extends TransitionalFlightState {
    public static final MapCodec<CrashingState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("ticks_passed").forGetter(s -> s.ticksPassed),
            TardisLocation.CODEC.fieldOf("landing_destination").forGetter(s -> s.landingDestination)
    ).apply(instance, CrashingState::new));
    public static final Identifier ID = MiniTardis.id("crashing");

    private final TardisLocation landingDestination;

    private CrashingState(int ticksPassed, TardisLocation landingDestination) {
        super(ticksPassed);
        this.landingDestination = landingDestination;
    }

    public CrashingState(TardisLocation landingDestination) {
        this(0, landingDestination);
    }

    @Override
    public void init(Tardis tardis) {
        tardis.setCurrentLocation(landingDestination);
        tardis.buildExterior();
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (tardis.getCurrentLandedLocation().map(tardis::canLandAt).orElse(false)) {
            tardis.getControls().moderateMalfunction();
            tardis.setCurrentLocation(new PartialTardisLocation(tardis.getExteriorWorldKey()));
            return new FlyingState(tardis.getRandom().nextInt());
        }

        if (ticksPassed < 20 * 8) {
            if (ticksPassed % FlyingState.SOUND_LOOP_LENGTH == 0) {
                playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                        SoundCategory.BLOCKS, 1, tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f);
            }
        } else if (ticksPassed == 20 * 8) {
            playForInteriorAndExterior(tardis, ModSounds.TARDIS_CRASH_LAND, SoundCategory.BLOCKS, 1, 1);
        }

        if (ticksPassed % 100 == 0) {
            playForInteriorAndExterior(tardis, ModSounds.CLOISTER_BELL, SoundCategory.BLOCKS, 1, 1);
        }

        return super.tick(tardis);
    }

    @Override
    public float getScreenShakeIntensity(Tardis tardis) {
        return 3;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        tardis.getControls().majorMalfunction();
        return false;
    }

    @Override
    public FlightState getNextState(Tardis tardis) {
        return new CrashedState();
    }

    @Override
    public int getTransitionDuration(Tardis tardis) {
        return 20 * (8 + 7);
    }

    @Override
    public boolean isSolid(Tardis tardis) {
        return false;
    }

    @Override
    public boolean tryChangeCourse(Tardis tardis) {
        tardis.getControls().majorMalfunction();
        return false;
    }

    @Override
    public byte getExteriorAlpha(Tardis tardis) {
        var vwoompWave = Math.sin(ticksPassed / 5.0) * 0.2 + 1;
        return (byte) (ticksPassed / (getTransitionDuration(tardis) / 15) * vwoompWave);
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return ticksPassed / 5 % 2 == 0;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

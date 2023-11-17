package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisLocation;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class LandingState extends TransitionalFlightState {
    public static final Codec<LandingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ticks_passed").forGetter(s -> s.ticksPassed),
            TardisLocation.CODEC.fieldOf("landing_destination").forGetter(s -> s.landingDestination)
    ).apply(instance, LandingState::new));
    public static final Identifier ID = MiniTardis.id("landing");

    private final TardisLocation landingDestination;

    private LandingState(int ticksPassed, TardisLocation landingDestination) {
        super(ticksPassed);
        this.landingDestination = landingDestination;
    }

    public LandingState(TardisLocation landingDestination) {
        this(0, landingDestination);
    }

    @Override
    public void init(Tardis tardis) {
        playForInteriorAndExterior(tardis, ModSounds.TARDIS_LANDING, SoundCategory.BLOCKS, 1, 1);

        tardis.setCurrentLocation(landingDestination);
        tardis.buildExterior();
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (tardis.getCurrentLocation().map(tardis::canLandAt).orElse(false)) {
            playForInterior(tardis, ModSounds.TARDIS_FAILURE_SINGLE, SoundCategory.BLOCKS, 1, 1);
            tardis.setCurrentLocation(Optional.empty());
            return new FlyingState();
        }

        tickScreenShake(tardis, 1);
        return super.tick(tardis);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public FlightState getNextState(Tardis tardis) {
        return new LandedState();
    }

    @Override
    public int getTransitionDuration(Tardis tardis) {
        return 20 * 9;
    }

    @Override
    public boolean isSolid(Tardis tardis) {
        return false;
    }

    @Override
    public boolean canChangeCourse(Tardis tardis) {
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

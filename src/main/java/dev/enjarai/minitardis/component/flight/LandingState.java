package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisLocation;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

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
        tardis.setCurrentLocation(landingDestination);
        playForInteriorAndExterior(tardis, ModSounds.TARDIS_LANDING, SoundCategory.BLOCKS, 1, 1);
        tardis.buildExterior();
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (tardis.getCurrentLandedLocation().map(tardis::canLandAt).orElse(false)) {
            tardis.getControls().moderateMalfunction();
            tardis.setCurrentLocation(new PartialTardisLocation(tardis.getExteriorWorldKey()));
            return new FlyingState();
        }

        if (ticksPassed % 2 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().moderateMalfunction();
            return this;
        }

        tickScreenShake(tardis, 1);
        return super.tick(tardis);
    }

    @Override
    public void complete(Tardis tardis) {
        tardis.getControls().setDestinationLocked(false, true);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        tardis.getControls().moderateMalfunction();
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
    public boolean tryChangeCourse(Tardis tardis) {
        tardis.getControls().moderateMalfunction();
        return false;
    }

    @Override
    public byte getExteriorAlpha(Tardis tardis) {
        var vwoompWave = Math.sin(ticksPassed / 5.0) * 0.2 + 1;
        return (byte) (ticksPassed / (getTransitionDuration(tardis) / 15) * vwoompWave);
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return FlyingState.spinnyLighting(order, ticksPassed);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

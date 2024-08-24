package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.ccacomponent.PartialTardisLocation;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import dev.enjarai.minitardis.ccacomponent.TardisLocation;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class LandingState extends TransitionalFlightState {
    public static final Codec<LandingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ticks_passed").forGetter(s -> s.ticksPassed),
            TardisLocation.CODEC.fieldOf("landing_destination").forGetter(s -> s.landingDestination),
            Codec.BOOL.optionalFieldOf("first_landing", false).forGetter(s -> s.firstLanding)
    ).apply(instance, LandingState::new));
    public static final Identifier ID = MiniTardis.id("landing");

    private final TardisLocation landingDestination;
    private final boolean firstLanding;

    private LandingState(int ticksPassed, TardisLocation landingDestination, boolean firstLanding) {
        super(ticksPassed);
        this.landingDestination = landingDestination;
        this.firstLanding = firstLanding;
    }

    public LandingState(TardisLocation landingDestination) {
        this(0, landingDestination, false);
    }

    public LandingState(TardisLocation landingDestination, boolean firstLanding) {
        this(0, landingDestination, firstLanding);
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
            return new FlyingState(tardis.getRandom().nextInt());
        }

        if (ticksPassed % 2 == 0) {
            tardis.addOrDrainFuel(-1);
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
        return firstLanding ? new DisabledState() : new LandedState();
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
        return !firstLanding && FlyingState.spinnyLighting(order, ticksPassed);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

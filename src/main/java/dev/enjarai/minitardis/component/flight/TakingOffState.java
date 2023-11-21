package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class TakingOffState extends TransitionalFlightState {
    public static final Codec<TakingOffState> CODEC = Codec.INT
            .xmap(TakingOffState::new, s -> s.ticksPassed).fieldOf("ticks_passed").codec();
    public static final Identifier ID = MiniTardis.id("taking_off");

    private TakingOffState(int ticksPassed) {
        super(ticksPassed);
    }

    public TakingOffState() {
        this(0);
    }

    @Override
    public void init(Tardis tardis) {
        playForInteriorAndExterior(tardis, ModSounds.TARDIS_TAKEOFF, SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public void complete(Tardis tardis) {
        tardis.setCurrentLocation(Optional.empty());
    }

    @Override
    public FlightState tick(Tardis tardis) {
        tickScreenShake(tardis, 1);

        if (tardis.getStability() <= 0 && tardis.getCurrentLocation().isPresent()) {
            return new CrashingState(tardis.getCurrentLocation().get());
        }

        if (ticksPassed % 2 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().majorMalfunction();
            return this;
        }

        return super.tick(tardis);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof SearchingForLandingState) {
            tardis.getControls().majorMalfunction();
        } else {
            tardis.getControls().moderateMalfunction();
        }
        return false;
    }

    @Override
    public FlightState getNextState(Tardis tardis) {
        return new FlyingState();
    }

    @Override
    public int getTransitionDuration(Tardis tardis) {
        return 20 * 10;
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

package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.Identifier;

public class LandingState extends TransitionalFlightState {
    public static final Codec<LandingState> CODEC = Codec.INT
            .xmap(LandingState::new, s -> s.ticksPassed).fieldOf("ticksPassed").codec();
    public static final Identifier ID = MiniTardis.id("landing");

    private LandingState(int ticksPassed) {
        super(ticksPassed);
    }

    public LandingState() {
        this(0);
    }

    @Override
    public void init(Tardis tardis) {
        tardis.setCurrentLocation(tardis.getDestination());
        tardis.buildExterior();
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
        return 20 * 8;
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
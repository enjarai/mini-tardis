package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class TakingOffState extends TransitionalFlightState {
    public static final Codec<TakingOffState> CODEC = Codec.INT
            .xmap(TakingOffState::new, s -> s.ticksPassed).fieldOf("ticksPassed").codec();
    public static final Identifier ID = MiniTardis.id("taking_off");

    private TakingOffState(int ticksPassed) {
        super(ticksPassed);
    }

    public TakingOffState() {
        this(0);
    }

    @Override
    public void complete(Tardis tardis) {
        tardis.setCurrentLocation(Optional.empty());
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public FlightState getNextState(Tardis tardis) {
        return new FlyingState();
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

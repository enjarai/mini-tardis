package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.Identifier;

public class FlyingState implements FlightState {
    public static final Codec<FlyingState> CODEC = Codec.unit(FlyingState::new);
    public static final Identifier ID = MiniTardis.id("flying");

    @Override
    public FlightState tick(Tardis tardis) {
        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof LandingState;
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

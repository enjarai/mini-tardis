package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.Identifier;

public class LandedState implements FlightState {
    public static final Codec<LandedState> CODEC = Codec.unit(LandedState::new);
    public static final Identifier ID = MiniTardis.id("landed");

    @Override
    public FlightState tick(Tardis tardis) {
        var stability = tardis.getStability();
        if (stability < 100) {
            tardis.setStability(stability + 1);
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof TakingOffState;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

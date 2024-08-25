package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import net.minecraft.util.Identifier;

public class RefuelingState implements FlightState {
    public static final MapCodec<RefuelingState> CODEC = Codec.INT
            .xmap(RefuelingState::new, s -> s.refuelingCounter).fieldOf("refueling_counter");
    public static final Identifier ID = MiniTardis.id("refueling");

    private int refuelingCounter;

    private RefuelingState(int refuelingCounter) {
        this.refuelingCounter = refuelingCounter;
    }

    public RefuelingState() {
        this(0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        var stability = tardis.getStability();
        if (stability < 1000) {
            tardis.setStability(stability + 1);
        }

        if (refuelingCounter > 20) {
            refuelingCounter = 0;

            if (!tardis.addOrDrainFuel(1)) {
                return new LandedState(); // TODO sound
            }
        }
        refuelingCounter++;

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof LandedState;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

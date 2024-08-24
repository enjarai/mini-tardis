package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import net.minecraft.util.Identifier;

public class LandedState implements FlightState {
    public static final Codec<LandedState> CODEC = Codec.unit(LandedState::new);
    public static final Identifier ID = MiniTardis.id("landed");

    @Override
    public FlightState tick(Tardis tardis) {
        var stability = tardis.getStability();
        if (stability < 1000) {
            tardis.setStability(stability + 1);
        }

        if (tardis.getControls().areEnergyConduitsUnlocked() && tardis.getInteriorWorld().getTime() % 40 == 0) {
            tardis.addOrDrainFuel(-1);
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof TakingOffState && tardis.getControls().isDestinationLocked() && tardis.getControls().areEnergyConduitsUnlocked()) {
            return true;
        }

        if (newState instanceof DisabledState) {
            return true;
        }

        return newState instanceof RefuelingState && !tardis.getControls().areEnergyConduitsUnlocked();
    }

    @Override
    public Identifier id() {
        return ID;
    }
}

package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface FlightState {
    Map<Identifier, Codec<? extends FlightState>> ALL = Map.of(
            LandedState.ID, LandedState.CODEC,
            TakingOffState.ID, TakingOffState.CODEC,
            FlyingState.ID, FlyingState.CODEC,
            LandingState.ID, LandingState.CODEC
    );
    Codec<FlightState> CODEC = Identifier.CODEC.dispatch(FlightState::id, ALL::get);

    /**
     * Called once when this state is transitioned into.
     */
    default void init(Tardis tardis) {
    }

    /**
     * Called every tick that this state is active, may return another state
     * instance to transition to another state, or itself.
     */
    FlightState tick(Tardis tardis);

    /**
     * Called once when this state is transitioned out of.
     */
    default void complete(Tardis tardis) {
    }

    /**
     * External factors may use this method to suggest a state transition,
     * the implementor can return a boolean to accept or reject this.
     */
    default boolean suggestTransition(Tardis tardis, FlightState newState) {
        return true;
    }

    /**
     * Whether entities should be able to enter and exit the Tardis.
     */
    default boolean isSolid(Tardis tardis) {
        return true;
    }

    /**
     * A unique id for serialization
     */
    Identifier id();
}

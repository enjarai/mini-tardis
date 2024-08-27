package dev.enjarai.minitardis.component.flight;

import dev.enjarai.minitardis.component.Tardis;

public abstract class TransitionalFlightState implements FlightState {
    protected int ticksPassed;

    protected TransitionalFlightState(int ticksPassed) {
        this.ticksPassed = ticksPassed;
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (ticksPassed >= getTransitionDuration(tardis)) {
            return getNextState(tardis);
        }
        ticksPassed++;
        return this;
    }

    public abstract FlightState getNextState(Tardis tardis);

    public abstract int getTransitionDuration(Tardis tardis);
}

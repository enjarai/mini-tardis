package dev.enjarai.minitardis.ccacomponent.flight;

import dev.enjarai.minitardis.ccacomponent.Tardis;

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

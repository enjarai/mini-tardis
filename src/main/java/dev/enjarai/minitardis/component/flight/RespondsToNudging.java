package dev.enjarai.minitardis.component.flight;

import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.math.Direction;

public interface RespondsToNudging {
    boolean nudgeDestination(Tardis tardis, Direction direction);
}

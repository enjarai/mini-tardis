package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.ccacomponent.ModCCAComponents;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import net.minecraft.world.World;

import java.util.Optional;

public interface TardisAware {
    default Optional<Tardis> getTardis(World world) {
        return world.getComponent(ModCCAComponents.TARDIS_REFERENCE).getTardis();
    }
}

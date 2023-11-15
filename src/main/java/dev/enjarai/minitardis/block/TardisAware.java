package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.world.World;

import java.util.Optional;

public interface TardisAware {
    default Optional<Tardis> getTardis(World world) {
        return world.getComponent(ModComponents.TARDIS_REFERENCE).getTardis();
    }
}

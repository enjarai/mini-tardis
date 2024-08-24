package dev.enjarai.minitardis.ccacomponent;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface LocationWithWorld {
    RegistryKey<World> worldKey();

    ServerWorld getWorld(MinecraftServer server);
}

package dev.enjarai.minitardis.ccacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public record PartialTardisLocation(RegistryKey<World> worldKey) implements LocationWithWorld {
    public static final Codec<PartialTardisLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            World.CODEC.fieldOf("world_key").forGetter(PartialTardisLocation::worldKey)
    ).apply(instance, PartialTardisLocation::new));

    public ServerWorld getWorld(MinecraftServer server) {
        return server.getWorld(worldKey);
    }

    public PartialTardisLocation with(RegistryKey<World> worldKey) {
        return new PartialTardisLocation(worldKey);
    }
}

package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public record TardisLocation(RegistryKey<World> worldKey, BlockPos pos, Direction facing) {
    public static final Codec<TardisLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            World.CODEC.fieldOf("world_key").forGetter(TardisLocation::worldKey),
            BlockPos.CODEC.fieldOf("pos").forGetter(TardisLocation::pos),
            Direction.CODEC.fieldOf("facing").forGetter(TardisLocation::facing)
    ).apply(instance, TardisLocation::new));

    public ServerWorld getWorld(MinecraftServer server) {
        return server.getWorld(worldKey);
    }

    public TardisLocation with(RegistryKey<World> worldKey) {
        return new TardisLocation(worldKey, pos(), facing());
    }

    public TardisLocation with(BlockPos pos) {
        return new TardisLocation(worldKey(), pos, facing());
    }

    public TardisLocation with(Direction facing) {
        return new TardisLocation(worldKey(), pos(), facing);
    }
}

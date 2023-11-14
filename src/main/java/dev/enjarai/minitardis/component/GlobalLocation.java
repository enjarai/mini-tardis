package dev.enjarai.minitardis.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record GlobalLocation(RegistryKey<World> worldKey, BlockPos pos) {
    public static final Codec<GlobalLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            World.CODEC.fieldOf("world_key").forGetter(GlobalLocation::worldKey),
            BlockPos.CODEC.fieldOf("pos").forGetter(GlobalLocation::pos)
    ).apply(instance, GlobalLocation::new));

    public ServerWorld getWorld(MinecraftServer server) {
        return server.getWorld(worldKey);
    }

    public static Optional<GlobalLocation> decode(@Nullable NbtElement element) {
        return element != null ?
                CODEC.decode(NbtOps.INSTANCE, element).result().map(Pair::getFirst) :
                Optional.empty();
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
    public static Optional<NbtElement> encode(Optional<GlobalLocation> location) {
        return location.flatMap(l -> CODEC.encodeStart(NbtOps.INSTANCE, l).result());
    }
}

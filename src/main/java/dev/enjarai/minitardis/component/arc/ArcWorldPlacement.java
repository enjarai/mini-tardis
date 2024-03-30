package dev.enjarai.minitardis.component.arc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.ArcNodeBlock;
import dev.enjarai.minitardis.block.ModBlocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.stream.Stream;

public class ArcWorldPlacement {
    public static final Codec<ArcWorldPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockBox.CODEC.fieldOf("box").forGetter(p -> p.box)
    ).apply(instance, ArcWorldPlacement::new));

    private BlockBox box;

    public ArcWorldPlacement(BlockBox box) {
        this.box = box;
    }

    public Stream<ArcNode> getNodes(ServerWorld world) {
        return box.streamChunkPos().flatMap(chunkPos -> world.getPointOfInterestStorage().getInChunk(
                        poiType -> poiType.value().equals(ModBlocks.ARC_NODE_POI),
                        chunkPos, PointOfInterestStorage.OccupationStatus.ANY
                ))
                .filter(poi -> box.contains(poi.getPos()))
                .map(poi -> new ArcNode(
                        poi.getPos(),
                        world.getBlockState(poi.getPos()).get(ArcNodeBlock.FACING)
                ));
    }
}

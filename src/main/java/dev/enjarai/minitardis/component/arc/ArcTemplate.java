package dev.enjarai.minitardis.component.arc;

import dev.enjarai.minitardis.block.ArcNodeBlock;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.mixin.StructureTemplateAccessor;
import net.minecraft.structure.StructureTemplate;

import java.util.stream.Stream;

public class ArcTemplate {
    private StructureTemplate structure;

    public Stream<ArcNode> getNodes() {
        var paletteLists = ((StructureTemplateAccessor) structure).getBlockInfoLists();
        if (paletteLists.isEmpty()) {
            return Stream.empty();
        }

        var infos = paletteLists.get(0).getAllOf(ModBlocks.ARC_NODE);

        return infos.stream()
                .map(info -> new ArcNode(info.pos(), info.state().get(ArcNodeBlock.FACING)));
    }
}

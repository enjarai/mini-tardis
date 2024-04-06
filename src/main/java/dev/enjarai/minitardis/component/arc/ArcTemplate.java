package dev.enjarai.minitardis.component.arc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.ArcNodeBlock;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.mixin.StructureTemplateAccessor;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;

import java.util.stream.Stream;

public record ArcTemplate(Identifier template) {
    public static final Codec<ArcTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(ArcTemplate::template)
    ).apply(instance, ArcTemplate::new));

    public Stream<ArcNode> getNodes(StructureTemplateManager manager) {
        var paletteLists = ((StructureTemplateAccessor) getStructure(manager)).getBlockInfoLists();
        if (paletteLists.isEmpty()) {
            return Stream.empty();
        }

        var infos = paletteLists.get(0).getAllOf(ModBlocks.ARC_NODE);

        return infos.stream()
                .map(info -> new ArcNode(info.pos(), info.state().get(ArcNodeBlock.FACING)));
    }

    public StructureTemplate getStructure(StructureTemplateManager manager) {
        return manager.getTemplateOrBlank(template);
    }
}

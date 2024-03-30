package dev.enjarai.minitardis.component.arc;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TardisArc {
    // TODO put into tardis itself, not app
//    public static final Codec<TardisArc> CODEC =; // TODO

    private final List<ArcWorldPlacement> placements = new ArrayList<>();

    private Tardis tardis;

    public Stream<ArcNode> getNodes() {
        return placements.stream().flatMap(placement -> placement.getNodes(tardis.getInteriorWorld()));
    }

    /**
     *
     *
     * @param template The template to find a position for
     * @param placedNodeIndex The index of an existing node to use, numbers higher than the existing amount of nodes will wrap
     * @param templateNodeIndex The index of the node on the template to use, also wraps
     * @return A possible position for this template, chosen from all available based on the indices
     */
    public ArcTemplatePlacement getPossiblePlacement(ArcTemplate template, int placedNodeIndex, int templateNodeIndex) {
        // Filter out all vertical nodes for now, we may get back to those later once i figure out how to handle em.
        var placedNodes = getNodes().filter(n -> n.facing().getAxis() != Direction.Axis.Y).toList();
        var placedNode = placedNodes.get(placedNodeIndex % placedNodes.size());

        var templateNodes = template.getNodes().filter(n -> n.facing().getAxis() != Direction.Axis.Y).toList();
        var templateNode = templateNodes.get(templateNodeIndex % templateNodes.size());

        // I can't be bothered to be clever here and its just 4 iterations, so eh.
        BlockRotation rotation = BlockRotation.NONE; // We initialize here to make intellij not complain, but this value can never be used
        for (var i : BlockRotation.values()) {
            if (i.rotate(templateNode.facing()) == placedNode.facing().getOpposite()) {
                rotation = i;
            }
        }

        var desiredPos = placedNode.pos()
                .offset(placedNode.facing())
                .subtract(StructureTemplate.transformAround(templateNode.pos(), BlockMirror.NONE, rotation, BlockPos.ORIGIN));

        return new ArcTemplatePlacement(template, desiredPos, rotation);
    }
}

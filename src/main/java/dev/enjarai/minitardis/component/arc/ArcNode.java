package dev.enjarai.minitardis.component.arc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record ArcNode(BlockPos pos, Direction facing, ArcWorldPlacement parent) {
}

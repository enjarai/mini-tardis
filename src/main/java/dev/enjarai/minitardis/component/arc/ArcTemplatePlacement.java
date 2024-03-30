package dev.enjarai.minitardis.component.arc;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public record ArcTemplatePlacement(ArcTemplate source, BlockPos position, BlockRotation rotation) {
}

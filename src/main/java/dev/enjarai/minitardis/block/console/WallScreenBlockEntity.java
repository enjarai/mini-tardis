package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class WallScreenBlockEntity extends ScreenBlockEntity {
    public WallScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WALL_SCREEN_ENTITY, pos, state);
        backgroundColor = CanvasColor.DARK_CRIMSON_LOWEST;
    }

    @Override
    protected BlockPos getPos(BlockPos pos, BlockState state) {
        return pos;
    }

    @Override
    protected Direction getFacing(BlockPos pos, BlockState state) {
        var faceState = state.get(WallScreenBlock.FACE);
        return faceState == BlockFace.WALL ? state.get(WallScreenBlock.FACING) :
                faceState == BlockFace.FLOOR ? Direction.UP : Direction.DOWN;
    }

    @Override
    protected BlockRotation getRotation(BlockPos pos, BlockState state) {
        var face = state.get(WallScreenBlock.FACE);
        if (face == BlockFace.WALL) {
            return BlockRotation.NONE;
        }

        var facing = state.get(WallScreenBlock.FACING);
        if (face == BlockFace.FLOOR) {
            return BlockRotation.values()[(2 + facing.getHorizontal()) % 4];
        } else {
            return BlockRotation.values()[(6 - facing.getHorizontal()) % 4];
        }
    }

    @Override
    protected void handleClick(ServerPlayerEntity player, ClickType type, int x, int y) {
        if (!MiniTardis.playerIsRealGamer(player.networkHandler) && type == ClickType.LEFT && getWorld() != null) {
            getWorld().breakBlock(getPos(), true, player);
            return;
        }

        super.handleClick(player, type, x, y);
    }
}

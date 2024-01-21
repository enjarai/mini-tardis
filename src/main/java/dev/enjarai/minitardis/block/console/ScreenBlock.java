package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.item.FloppyItem;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class ScreenBlock extends BlockWithEntity implements PolymerBlock, TardisAware, BlockWithElementHolder, ConsoleInput {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty HAS_FLOPPY = BooleanProperty.of("has_floppy");

    public ScreenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HAS_FLOPPY, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_FLOPPY);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = this.getDefaultState();
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                blockState = blockState.with(FACING, direction2);
                if (blockState.canPlaceAt(worldView, blockPos)) {
                    return blockState;
                }
            }
        }

        return null;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Override
    public Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasPack(player) ? PolymerBlock.super.getPolymerBlock(state, player) : Blocks.COAL_BLOCK;
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new ConsoleScreenElementHolder(initialBlockState);
    }

    protected static class ConsoleScreenElementHolder extends ElementHolder {
        final ItemDisplayElement floppyElement;

        ConsoleScreenElementHolder(BlockState initialBlockState) {
            var facing = initialBlockState.get(FACING);

            var exteriorElement = new ItemDisplayElement();
            exteriorElement.setItem(PolymerModels.getStack(PolymerModels.ROTATING_MONITOR));
            exteriorElement.setTranslation(facing.getOpposite().getUnitVector().mul(0.5f));
            exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

            floppyElement = new ItemDisplayElement();
            floppyElement.setItem(PolymerModels.getStack(FloppyItem.MODEL));
            floppyElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));
            floppyElement.setScale(new Vector3f(0.6f));
            floppyElement.setTranslation(facing.getUnitVector().mul(0.4f).add(0, -0.3f, 0));

            addElement(exteriorElement);
            if (initialBlockState.get(HAS_FLOPPY)) {
                addElement(floppyElement);
            }
        }

        void setFloppyVisible(boolean visible) {
            if (visible) {
                addElement(floppyElement);
            } else {
                removeElement(floppyElement);
            }
        }
    }
}

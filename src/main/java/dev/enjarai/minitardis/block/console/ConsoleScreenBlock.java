package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@SuppressWarnings("deprecation")
public class ConsoleScreenBlock extends BlockWithEntity implements PolymerBlock, TardisAware, BlockWithElementHolder, ConsoleInput {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public ConsoleScreenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = this.getDefaultState();
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

        for(Direction direction : directions) {
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var facing = state.get(FACING);
        var hitSide = hit.getSide();

        if (hitSide == facing.rotateYClockwise() || hitSide == facing.rotateYCounterclockwise()) {
            var newPos = pos.offset(hitSide).offset(facing.getOpposite());

            if (world.getBlockState(newPos).isReplaceable()) {
                world.setBlockState(newPos, state.with(FACING, hitSide));

                if (world.getBlockEntity(pos) instanceof ConsoleScreenBlockEntity oldEntity && world.getBlockEntity(newPos) instanceof ConsoleScreenBlockEntity newEntity) {
                    newEntity.selectedApp = oldEntity.selectedApp;
                }

                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                inputSuccess(world, newPos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0);
            }

            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConsoleScreenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlocks.CONSOLE_SCREEN_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof ConsoleScreenBlockEntity entity) {
            entity.cleanUpForRemoval();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

//    @Override
//    public BlockState getPolymerBlockState(BlockState state) {
//        return getPolymerBlock(state).getDefaultState().with(LightBlock.LEVEL_15, 3);
//    }

//    @Override
//    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
//        return true;
//    }


    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var facing = initialBlockState.get(FACING);

        var exteriorElement = new ItemDisplayElement();
        exteriorElement.setItem(PolymerModels.getStack(PolymerModels.ROTATING_MONITOR));
//        var matrix = new Matrix4f();
//        matrix.translate();
        exteriorElement.setTranslation(facing.getOpposite().getUnitVector().mul(0.5f));
        exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

        return new ElementHolder() {{
                addElement(exteriorElement);
        }};
    }
}

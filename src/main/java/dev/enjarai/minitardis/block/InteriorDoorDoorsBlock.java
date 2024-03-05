package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.item.PolymerModels;
import dev.enjarai.minitardis.util.PerhapsElementHolder;
import dev.enjarai.minitardis.util.PerhapsPolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class InteriorDoorDoorsBlock extends HorizontalFacingBlock implements PerhapsPolymerBlock, TardisAware, BlockWithElementHolder {
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public static final VoxelShape[][] OUTLINE_SHAPES = new VoxelShape[][]{
            new VoxelShape[]{
                    VoxelShapes.union(
                            Block.createCuboidShape(0, 1, 2, 1, 16, 10),
                            Block.createCuboidShape(15, 1, 2, 16, 16, 10)
                    ),
                    VoxelShapes.union(
                            Block.createCuboidShape(6, 1, 0, 14, 16, 1),
                            Block.createCuboidShape(6, 1, 15, 14, 16, 16)
                    ),
                    VoxelShapes.union(
                            Block.createCuboidShape(0, 1, 6, 1, 16, 14),
                            Block.createCuboidShape(15, 1, 6, 16, 16, 14)
                    ),
                    VoxelShapes.union(
                            Block.createCuboidShape(2, 1, 0, 10, 16, 1),
                            Block.createCuboidShape(2, 1, 15, 10, 16, 16)
                    )
            },
            new VoxelShape[]{
                    VoxelShapes.union(
                            Block.createCuboidShape(0, 0, 2, 1, 16, 10),
                            Block.createCuboidShape(15, 0, 2, 16, 16, 10)
                    ),
                    VoxelShapes.union(
                            Block.createCuboidShape(6, 0, 0, 14, 16, 1),
                            Block.createCuboidShape(6, 0, 15, 14, 16, 16)
                    ),
                    VoxelShapes.union(
                            Block.createCuboidShape(0, 0, 6, 1, 16, 14),
                            Block.createCuboidShape(15, 0, 6, 16, 16, 14)
                    ),
                    VoxelShapes.union(
                            Block.createCuboidShape(2, 0, 0, 10, 16, 1),
                            Block.createCuboidShape(2, 0, 15, 10, 16, 16)
                    )
            },
    };

    protected InteriorDoorDoorsBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState()
                    .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                    .with(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

//    @Override
//    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
//        var ownHalf = state.get(HALF);
//        var checkDirection = ownHalf == DoubleBlockHalf.UPPER ? Direction.DOWN : Direction.UP;
//        if (direction == checkDirection) {
//            return neighborState.isOf(this) && neighborState.get(HALF) != ownHalf ? state : Blocks.AIR.getDefaultState();
//        }
//        return state;
//    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        closeTardisDoor(world, pos);
        world.removeBlock(state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down(), false);
        world.removeBlock(pos, false);
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        closeTardisDoor(world, pos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPES[state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1][state.get(FACING).getHorizontal()];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), NOTIFY_ALL);
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            if (!getTardis(world).map(Tardis::isDoorOpen).orElse(false)) {
                world.removeBlock(pos.up(), false);
                world.removeBlock(pos, false);
            }
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        return state.get(HALF) == DoubleBlockHalf.LOWER || blockState.isOf(this);
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public Block getPerhapsPolymerBlock(BlockState state) {
        return Blocks.LIGHT;
    }

    @Override
    public BlockState getPerhapsPolymerBlockState(BlockState state) {
        return getPolymerBlock(state).getDefaultState().with(LightBlock.LEVEL_15, 0);
    }

    protected void closeTardisDoor(World world, BlockPos pos) {
        getTardis(world).ifPresent(tardis -> {
            world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_CLOSE, SoundCategory.BLOCKS);
            tardis.getExteriorWorld().ifPresent(exteriorWorld ->
                    exteriorWorld.playSound(null, tardis.getCurrentLandedLocation().get().pos(),
                            SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_CLOSE, SoundCategory.BLOCKS));
            tardis.setDoorOpen(false, false);
        });
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        if (initialBlockState.get(HALF) == DoubleBlockHalf.LOWER) {
            var facing = initialBlockState.get(FACING);

            var doorElement = new ItemDisplayElement();
            doorElement.setItem(PolymerModels.getStack(PolymerModels.INTERIOR_DOOR_OPEN));
            doorElement.setOffset(new Vec3d(0, 1, 0));
            doorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

            var doorCloseInteractionHandler = new VirtualElement.InteractionHandler() {
                @Override
                public void interact(ServerPlayerEntity player, Hand hand) {
                    closeTardisDoor(world, pos);
                }
            };

            var leftDoorInteraction = new InteractionElement();
            leftDoorInteraction.setHandler(doorCloseInteractionHandler);
            leftDoorInteraction.setOffset(new Vec3d(0, 1.0 / 16.0 * -7, 0)
                    .offset(facing, 1.0 / 16.0 * -2.0)
                    .offset(facing.rotateYCounterclockwise(), 1.0 / 16.0 * 11.0));
            leftDoorInteraction.setSize(0.5f, 2);

            var rightDoorInteraction = new InteractionElement();
            rightDoorInteraction.setHandler(doorCloseInteractionHandler);
            rightDoorInteraction.setOffset(new Vec3d(0, 1.0 / 16.0 * -7, 0)
                    .offset(facing, 1.0 / 16.0 * -2.0)
                    .offset(facing.rotateYClockwise(), 1.0 / 16.0 * 11.0));
            rightDoorInteraction.setSize(0.5f, 2);

            return new PerhapsElementHolder() {
                {
                    addElement(doorElement);
                    addElement(leftDoorInteraction);
                    addElement(rightDoorInteraction);
                }
            };
        }
        return null;
    }
}

package dev.enjarai.minitardis.block.console;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class WallScreenBlock extends ScreenBlock {
    public static final MapCodec<WallScreenBlock> CODEC = createCodec(WallScreenBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<BlockFace> FACE = WallMountedBlock.FACE;

    public static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.createCuboidShape(-0.95, 1, 15, 0.050000000000000044, 15, 17),
            Block.createCuboidShape(-0.05, 2, 15.899999999999999, 16.05, 14, 18),
            Block.createCuboidShape(0, 1, 15, 16, 2, 17),
            Block.createCuboidShape(0, 14, 15, 16, 15, 17),
            Block.createCuboidShape(15.95, 1, 15, 16.949999999999996, 15, 17)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape EAST_SHAPE = Stream.of(
            Block.createCuboidShape(-1, 1, -0.9499999999999993, 1, 15, 0.05000000000000071),
            Block.createCuboidShape(-2, 2, -0.05000000000000071, 0.10000000000000142, 14, 16.05),
            Block.createCuboidShape(-1, 1, 0, 1, 2, 16),
            Block.createCuboidShape(-1, 14, 0, 1, 15, 16),
            Block.createCuboidShape(-1, 1, 15.95, 1, 15, 16.949999999999996)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.createCuboidShape(15.95, 1, -1, 16.95, 15, 1),
            Block.createCuboidShape(-0.05000000000000071, 2, -2, 16.05, 14, 0.10000000000000142),
            Block.createCuboidShape(0, 1, -1, 16, 2, 1),
            Block.createCuboidShape(0, 14, -1, 16, 15, 1),
            Block.createCuboidShape(-0.9499999999999957, 1, -1, 0.05000000000000071, 15, 1)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape WEST_SHAPE = Stream.of(
            Block.createCuboidShape(15, 1, 15.95, 17, 15, 16.95),
            Block.createCuboidShape(15.899999999999999, 2, -0.05000000000000071, 18, 14, 16.05),
            Block.createCuboidShape(15, 1, 0, 17, 2, 16),
            Block.createCuboidShape(15, 14, 0, 17, 15, 16),
            Block.createCuboidShape(15, 1, -0.9499999999999957, 17, 15, 0.05000000000000071)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape FLOOR_X_SHAPE = Stream.of(
            Block.createCuboidShape(1, -1, -0.9499999999999993, 15, 1, 0.05000000000000071),
            Block.createCuboidShape(2, -2, -0.05000000000000071, 14, 0.10000000000000142, 16.05),
            Block.createCuboidShape(14, -1, 0, 15, 1, 16),
            Block.createCuboidShape(1, -1, 0, 2, 1, 16),
            Block.createCuboidShape(1, -1, 15.95, 15, 1, 16.949999999999996)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape FLOOR_Z_SHAPE = Stream.of(
            Block.createCuboidShape(-0.9499999999999993, -1, 1, 0.05000000000000071, 1, 15),
            Block.createCuboidShape(-0.05000000000000071, -2, 2, 16.05, 0.10000000000000142, 14),
            Block.createCuboidShape(0, -1, 1, 16, 1, 2),
            Block.createCuboidShape(0, -1, 14, 16, 1, 15),
            Block.createCuboidShape(15.95, -1, 1, 16.949999999999996, 1, 15)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape CEILING_X_SHAPE = Stream.of(
            Block.createCuboidShape(1, 15, -0.9499999999999993, 15, 17, 0.05000000000000071),
            Block.createCuboidShape(2, 15.899999999999999, -0.05000000000000071, 14, 18, 16.05),
            Block.createCuboidShape(1, 15, 0, 2, 17, 16),
            Block.createCuboidShape(14, 15, 0, 15, 17, 16),
            Block.createCuboidShape(1, 15, 15.95, 15, 17, 16.949999999999996)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    public static final VoxelShape CEILING_Z_SHAPE = Stream.of(
            Block.createCuboidShape(-0.9499999999999993, 15, 1, 0.05000000000000071, 17, 15),
            Block.createCuboidShape(-0.05000000000000071, 15.899999999999999, 2, 16.05, 18, 14),
            Block.createCuboidShape(0, 15, 14, 16, 17, 15),
            Block.createCuboidShape(0, 15, 1, 16, 17, 2),
            Block.createCuboidShape(15.95, 15, 1, 16.949999999999996, 17, 15)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public WallScreenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(FACE, BlockFace.FLOOR).with(HAS_FLOPPY, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

//    @Override
//    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
//        var facing = state.get(FACING);
//        var hitSide = hit.getSide();
//
//        // TODO put this in screen click
//        if (hand == Hand.MAIN_HAND && hitSide == Direction.DOWN && world.getBlockEntity(pos) instanceof ScreenBlockEntity blockEntity) {
//            if (trySwitchFloppy(state, world, pos, player, hand)) {
//                return ActionResult.SUCCESS;
//            }
//        }
//
//        return super.onUse(state, world, pos, player, hand, hit);
//    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        switch (state.get(FACE)) {
            case FLOOR:
                if (direction.getAxis() == Direction.Axis.X) {
                    return FLOOR_X_SHAPE;
                } else {
                    return FLOOR_Z_SHAPE;
                }
            case WALL:
                return switch(direction) {
                    case EAST -> EAST_SHAPE;
                    case WEST -> WEST_SHAPE;
                    case SOUTH -> SOUTH_SHAPE;
                    case NORTH, UP, DOWN -> NORTH_SHAPE;
                };
            case CEILING:
            default:
                if (direction.getAxis() == Direction.Axis.X) {
                    return CEILING_X_SHAPE;
                } else {
                    return CEILING_Z_SHAPE;
                }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, HAS_FLOPPY);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WallScreenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlocks.WALL_SCREEN_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        for(Direction direction : ctx.getPlacementDirections()) {
            BlockState blockState;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockState = this.getDefaultState()
                        .with(FACE, direction == Direction.UP ? BlockFace.CEILING : BlockFace.FLOOR)
                        .with(FACING, ctx.getHorizontalPlayerFacing());
            } else {
                blockState = this.getDefaultState().with(FACE, BlockFace.WALL).with(FACING, direction.getOpposite());
            }

            if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                return blockState;
            }
        }

        return null;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof ScreenBlockEntity entity) {
            entity.cleanUpForRemoval();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var direction = getDirection(state);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        return world.getBlockState(blockPos).isSolid();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return getDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    protected static Direction getDirection(BlockState state) {
        return switch (state.get(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> state.get(FACING);
        };
    }
}

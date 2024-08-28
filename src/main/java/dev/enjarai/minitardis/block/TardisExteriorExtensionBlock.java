package dev.enjarai.minitardis.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings("deprecation")
public class TardisExteriorExtensionBlock extends Block {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty VISIBLENESS = IntProperty.of("visibleness", 0, 16);

    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(-1, 0, -1, 17, 15, 17),
            Block.createCuboidShape(-2, 0, -2, 0, 15, 0),
            Block.createCuboidShape(16, 0, -2, 18, 15, 0),
            Block.createCuboidShape(16, 0, 16, 18, 15, 18),
            Block.createCuboidShape(-2, 0, 16, 0, 15, 18),
            Block.createCuboidShape(-2, 15, -2, 18, 21, 18),
            Block.createCuboidShape(-3, 16, -1, 19, 20, 17),
            Block.createCuboidShape(-1, 16, -3, 17, 20, 19),
            Block.createCuboidShape(-1, 21, -1, 17, 23, 17),
            Block.createCuboidShape(6, 23, 6, 10, 27, 10)
    );

    public TardisExteriorExtensionBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(VISIBLENESS, 16));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLENESS);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !neighborState.isOf(ModBlocks.TARDIS_EXTERIOR) ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (hit.getSide() == state.get(FACING)) {
            if (!world.isClient()
                    && world.getBlockEntity(pos.down()) instanceof TardisExteriorBlockEntity blockEntity) {
                blockEntity.teleportEntityIn(player);
            }
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hit);
    }

    // Disabled because of silly transparency issues on block outlines
//    @Override
//    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return OUTLINE_SHAPE;
//    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
}

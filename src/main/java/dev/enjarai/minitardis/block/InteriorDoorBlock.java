package dev.enjarai.minitardis.block;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.item.PolymerModels;
import dev.enjarai.minitardis.util.PerhapsElementHolder;
import dev.enjarai.minitardis.util.PerhapsPolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
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
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class InteriorDoorBlock extends HorizontalFacingBlock implements TardisAware, BlockWithElementHolder {
    public static final MapCodec<InteriorDoorBlock> CODEC = createCodec(InteriorDoorBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public static final VoxelShape[] OUTLINE_SHAPES = new VoxelShape[]{
            VoxelShapes.union(
                    Block.createCuboidShape(-1, 1, -1, 17, 16, 17),
                    Block.createCuboidShape(-2, 0, -2, 18, 1, 18),
                    Block.createCuboidShape(-2, 1, -2, 0, 16, 0),
                    Block.createCuboidShape(16, 1, -2, 18, 16, 0),
                    Block.createCuboidShape(16, 1, 16, 18, 16, 18),
                    Block.createCuboidShape(-2, 1, 16, 0, 16, 18)
            ),
            VoxelShapes.union(
                    Block.createCuboidShape(-1, 0, -1, 17, 16, 17),
                    Block.createCuboidShape(-2, 16, -2, 18, 17, 18),
                    Block.createCuboidShape(-2, 0, -2, 0, 16, 0),
                    Block.createCuboidShape(16, 0, -2, 18, 16, 0),
                    Block.createCuboidShape(16, 0, 16, 18, 16, 18),
                    Block.createCuboidShape(-2, 0, 16, 0, 16, 18)
            )
    };

    protected InteriorDoorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
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

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        var ownHalf = state.get(HALF);
        var checkDirection = ownHalf == DoubleBlockHalf.UPPER ? Direction.DOWN : Direction.UP;
        if (direction == checkDirection) {
            return neighborState.isOf(this) && neighborState.get(HALF) != ownHalf ? state : Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // TODO uncomment when polymer fixes the™
//        if (hit.getSide() == state.get(FACING)) {
            getTardis(world).ifPresent(tardis -> tardis.teleportEntityOut(player, state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos));
            return ActionResult.SUCCESS;
//        }
//        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPES[state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            var facing = state.get(FACING);
            if (getTardis(world).map(Tardis::isDoorOpen).orElse(false)
                    && world.getBlockState(pos.offset(facing)).isReplaceable()
                    && world.getBlockState(pos.offset(facing).up()).isReplaceable()) {
                world.setBlockState(pos.offset(facing), ModBlocks.INTERIOR_DOOR_DOORS.getDefaultState().with(HALF, DoubleBlockHalf.LOWER).with(FACING, facing));
                world.setBlockState(pos.offset(facing).up(), ModBlocks.INTERIOR_DOOR_DOORS.getDefaultState().with(HALF, DoubleBlockHalf.UPPER).with(FACING, facing));
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
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        if (initialBlockState.get(HALF) == DoubleBlockHalf.LOWER) {
            var facing = initialBlockState.get(FACING);

            var exteriorElement = new ItemDisplayElement();
            exteriorElement.setItem(PolymerModels.getStack(PolymerModels.INTERIOR_DOOR));
            exteriorElement.setOffset(new Vec3d(0, 1, 0));
            exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

            return new PerhapsElementHolder() {
                {
                    addElement(exteriorElement);
                }
            };
        }
        return null;
    }
}

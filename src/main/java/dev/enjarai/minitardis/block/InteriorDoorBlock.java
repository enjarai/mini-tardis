package dev.enjarai.minitardis.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class InteriorDoorBlock extends FacingBlock implements PolymerBlock, TardisAware {
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    protected InteriorDoorBlock(Settings settings) {
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        getTardis(world).ifPresent(tardis -> tardis.teleportEntityOut(player));
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        return state.get(HALF) == DoubleBlockHalf.LOWER || blockState.isOf(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.PISTON;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.PISTON.getDefaultState().with(FACING, state.get(FACING));
    }
}

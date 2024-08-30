package dev.enjarai.minitardis.block.console;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.item.ModItems;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ConsoleScreenBlock extends ScreenBlock {
    public static final MapCodec<ConsoleScreenBlock> CODEC = createCodec(ConsoleScreenBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public ConsoleScreenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HAS_FLOPPY, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_FLOPPY);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var facing = state.get(FACING);
        var hitSide = hit.getSide();

        if (hitSide == facing) {
            if (world.getBlockEntity(pos) instanceof ScreenBlockEntity blockEntity) {
                var relative = hit.getPos().toVector3f()
                        .sub(pos.getX(), pos.getY(), pos.getZ());
                var y = 1 - relative.y();
                var x = switch (facing) {
                    case NORTH -> 1 - relative.x();
                    case SOUTH -> relative.x();
                    case WEST -> relative.z();
                    case EAST -> 1 - relative.z();
                    default -> throw new IllegalStateException("Unexpected value: " + facing);
                };

                int x2 = (int) (x * 128);
                int y2 = (int) (y * 128);

                if (x2 >= 0 && x2 < 128 && y2 >= 16 && y2 < 112) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        blockEntity.handleClick(serverPlayer, ClickType.RIGHT, x2, y2);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        } else if (hitSide == facing.rotateYClockwise() || hitSide == facing.rotateYCounterclockwise()) {
            var newPos = pos.offset(hitSide).offset(facing.getOpposite());

            if (world.getBlockState(newPos).isReplaceable()) {
                world.setBlockState(newPos, state.with(FACING, hitSide));

                if (world.getBlockEntity(pos) instanceof ScreenBlockEntity oldEntity && world.getBlockEntity(newPos) instanceof ScreenBlockEntity newEntity) {
                    var tempNbt = new NbtCompound();
                    oldEntity.writeNbt(tempNbt, world.getRegistryManager());
                    newEntity.readNbt(tempNbt, world.getRegistryManager());
                    newEntity.currentView = oldEntity.currentView;
                }

                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                inputSuccess(world, newPos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0);
            }

            return ActionResult.SUCCESS;
        } else if (hitSide == Direction.DOWN) {
            if (trySwitchFloppy(state, world, pos, player, Hand.MAIN_HAND)) {
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hit);
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
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof ScreenBlockEntity entity) {
            entity.cleanUpForRemoval();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, direction);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state.get(FACING).getOpposite() == direction && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}

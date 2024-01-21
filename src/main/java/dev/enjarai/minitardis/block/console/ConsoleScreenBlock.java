package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.item.FloppyItem;
import dev.enjarai.minitardis.item.ModItems;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
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
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@SuppressWarnings("deprecation")
public class ConsoleScreenBlock extends ScreenBlock {

    public ConsoleScreenBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var facing = state.get(FACING);
        var hitSide = hit.getSide();

        if (hitSide == facing.rotateYClockwise() || hitSide == facing.rotateYCounterclockwise()) {
            var newPos = pos.offset(hitSide).offset(facing.getOpposite());

            if (world.getBlockState(newPos).isReplaceable()) {
                world.setBlockState(newPos, state.with(FACING, hitSide));

                if (world.getBlockEntity(pos) instanceof ScreenBlockEntity oldEntity && world.getBlockEntity(newPos) instanceof ScreenBlockEntity newEntity) {
                    var tempNbt = new NbtCompound();
                    oldEntity.writeNbt(tempNbt);
                    newEntity.readNbt(tempNbt);
                    newEntity.currentView = oldEntity.currentView;
                }

                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                inputSuccess(world, newPos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0);
            }

            return ActionResult.SUCCESS;
        } else if (hand == Hand.MAIN_HAND && hitSide == Direction.DOWN && world.getBlockEntity(pos) instanceof ScreenBlockEntity blockEntity) {
            var handStack = player.getStackInHand(hand);
            var blockStack = blockEntity.inventory.getStack(0);
            @SuppressWarnings("DataFlowIssue")
            var elementHolder = (ConsoleScreenElementHolder) BlockBoundAttachment.get(world, pos).holder();

            if (handStack.isOf(ModItems.FLOPPY) && blockStack.isEmpty()) {
                blockEntity.inventory.setStack(0, handStack.split(1));
                world.setBlockState(pos, state.with(HAS_FLOPPY, true));
                elementHolder.setFloppyVisible(true);

                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1, 2);
                return ActionResult.SUCCESS;
            } else if (handStack.isEmpty() && !blockStack.isEmpty()) {
                player.setStackInHand(hand, blockStack.split(1));
                world.setBlockState(pos, state.with(HAS_FLOPPY, false));
                elementHolder.setFloppyVisible(false);

                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1, 1.5f);
                return ActionResult.SUCCESS;
            }
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
        return checkType(type, ModBlocks.CONSOLE_SCREEN_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
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

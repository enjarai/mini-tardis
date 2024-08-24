package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.ccacomponent.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

@SuppressWarnings("deprecation")
public class ConsoleComparatorBlock extends Block implements PolymerBlock, ConsoleInput, TardisAware {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<ComparatorMode> COMPARATOR_MODE = Properties.COMPARATOR_MODE;
    public static final BooleanProperty POWERED = Properties.POWERED;

    private final BiFunction<TardisControl, Boolean, Boolean> controlInput;

    public ConsoleComparatorBlock(Settings settings, BiFunction<TardisControl, Boolean, Boolean> controlInput) {
        super(settings);
        this.controlInput = controlInput;
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(COMPARATOR_MODE, ComparatorMode.COMPARE)
                .with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, COMPARATOR_MODE, POWERED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            world.setBlockState(pos, state.cycle(COMPARATOR_MODE), Block.NOTIFY_ALL);
            var newMode = world.getBlockState(pos).get(COMPARATOR_MODE);
            if (getTardis(world).map(tardis -> controlInput.apply(tardis.getControls(), newMode == ComparatorMode.SUBTRACT)).orElse(false)) {
//                inputSuccess(world, pos, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), newMode.ordinal() + 1);
            } else {
                inputFailure(world, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 3);
            }
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING) && neighborState.getBlock() instanceof ConsoleComparatorDependentBlock) {
            return state.with(POWERED, neighborState.get(ButtonBlock.POWERED));
        }
        return state.with(POWERED, false);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.COMPARATOR.getStateWithProperties(state);
    }
}

package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

@SuppressWarnings("deprecation")
public class ConsoleRepeaterBlock extends Block implements PolymerBlock, ConsoleInput, TardisAware {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty DELAY = Properties.DELAY;

    private final BiFunction<TardisControl, Integer, Boolean> controlInput;

    public ConsoleRepeaterBlock(Settings settings, BiFunction<TardisControl, Integer, Boolean> controlInput) {
        super(settings);
        this.controlInput = controlInput;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(DELAY, 1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, DELAY);
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
            world.setBlockState(pos, state.cycle(DELAY), Block.NOTIFY_ALL);
            var newDelay = world.getBlockState(pos).get(DELAY);
            if (getTardis(world).map(tardis -> controlInput.apply(tardis.getControls(), newDelay)).orElse(false)) {
                inputSuccess(world, pos, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), newDelay / 4f);
            } else {
                inputFailure(world, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 3);
            }
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.REPEATER.getStateWithProperties(state);
    }
}

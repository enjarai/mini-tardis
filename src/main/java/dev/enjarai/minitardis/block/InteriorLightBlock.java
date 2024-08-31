package dev.enjarai.minitardis.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.Map;

public class InteriorLightBlock extends Block implements TardisAware {
    public static final IntProperty ORDER = IntProperty.of("order", 0, 12);
    public static final BooleanProperty LIT = Properties.LIT;

    public InteriorLightBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(LIT, false).with(ORDER, 0));
    }

    @Override
    public ItemStack getPickStack(WorldView worldView, BlockPos pos, BlockState state) {
        var stack = super.getPickStack(worldView, pos, state);

        var order = state.get(ORDER);
        if (order != 0) {
            stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(Map.of(ORDER.getName(), String.valueOf(order))));
        }

        return stack;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            var newState = state.cycle(ORDER);
            var newOrder = newState.get(ORDER);
            world.setBlockState(pos, newState);
            world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1, 0.5f + 1.0f / 12 * newOrder);
            return ActionResult.SUCCESS;
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, ORDER);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        getTardis(world).ifPresent(tardis -> {
            var lit = tardis.getState().isInteriorLightEnabled(state.get(ORDER));
            if (lit != state.get(LIT)) {
                world.setBlockState(pos, state.with(LIT, lit), Block.NOTIFY_LISTENERS);
                world.updateNeighbors(pos, this);
            }
        });
        world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
}

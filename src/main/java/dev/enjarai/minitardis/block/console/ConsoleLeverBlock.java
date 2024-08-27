package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

@SuppressWarnings("deprecation")
public class ConsoleLeverBlock extends LeverBlock implements PolymerBlock, TardisAware, ConsoleInput {
    private final BiFunction<TardisControl, Boolean, Boolean> controlInput;
    @Nullable
    private final BiFunction<TardisControl, Boolean, Boolean> checkStateFunction;

    public ConsoleLeverBlock(Settings settings, BiFunction<TardisControl, Boolean, Boolean> controlInput, @Nullable BiFunction<TardisControl, Boolean, Boolean> checkStateFunction) {
        super(settings);
        this.controlInput = controlInput;
        this.checkStateFunction = checkStateFunction;
    }


    @Override
    public void togglePower(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
        super.togglePower(state, world, pos, player);
        var state2 = world.getBlockState(pos);
        if (!getTardis(world)
                .map(tardis -> controlInput.apply(tardis.getControls(), state2.get(POWERED)))
                .orElseGet(() -> tryActivateMakeshiftEngine(state, world, pos, state2.get(POWERED)))) {
            inputFailure(world, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0);
        }
    }

    protected boolean tryActivateMakeshiftEngine(BlockState state, World world, BlockPos pos, boolean active) {
        for (var direction : Direction.Type.HORIZONTAL) {
            var targetPos = pos.offset(direction).down();
            if (world.getBlockState(targetPos).isOf(ModBlocks.MAKESHIFT_ENGINE)) {
                return world.getBlockEntity(targetPos, ModBlocks.MAKESHIFT_ENGINE_ENTITY)
                        .map(be -> be.tryHandbrake(active)).orElse(false);
            }
        }
        return false;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (checkStateFunction != null) {
            getTardis(world).ifPresent(tardis -> {
                var oldValue = state.get(POWERED);
                var newValue = checkStateFunction.apply(tardis.getControls(), oldValue);

                if (oldValue != newValue) {
                    world.setBlockState(pos, state.with(POWERED, newValue));

                    float pitch = newValue ? 0.6F : 0.5F;
                    world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, pitch);
                }
            });
            world.scheduleBlockTick(pos, this, 10);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (checkStateFunction != null) {
            world.scheduleBlockTick(pos, this, 10);
        }
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return false;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.LEVER.getStateWithProperties(state);
    }
}

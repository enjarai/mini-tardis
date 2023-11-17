package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ConsoleComparatorDependentBlock extends ButtonBlock implements PolymerBlock, ConsoleInput, TardisAware {
    private BiFunction<TardisControl, Boolean, Boolean> controlInput;

    public ConsoleComparatorDependentBlock(Settings settings, BiFunction<TardisControl, Boolean, Boolean> controlInput) {
        super(settings, BlockSetType.JUNGLE, 2, true);
        this.controlInput = controlInput;
    }

    @Override
    public void powerOn(BlockState state, World world, BlockPos pos) {
        super.powerOn(state, world, pos);

        var facing = state.get(FACING);
        var comparatorState = world.getBlockState(pos.offset(facing));

        if (!comparatorState.isOf(ModBlocks.STATE_COMPARATOR) ||
                !getTardis(world).map(tardis -> controlInput.apply(
                        tardis.getControls(),
                        comparatorState.get(ConsoleComparatorBlock.COMPARATOR_MODE) == ComparatorMode.SUBTRACT)
                ).orElse(false)) {
            inputFailure(world, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, 0);
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
    public Block getPolymerBlock(BlockState state) {
        return Blocks.JUNGLE_BUTTON;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return getPolymerBlock(state).getStateWithProperties(state);
    }
}

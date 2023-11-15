package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public class ConsoleLeverBlock extends LeverBlock implements PolymerBlock, TardisAware {
    private final BiConsumer<TardisControl, Boolean> controlInput;

    public ConsoleLeverBlock(Settings settings, BiConsumer<TardisControl, Boolean> controlInput) {
        super(settings);
        this.controlInput = controlInput;
    }

    @Override
    public BlockState togglePower(BlockState state, World world, BlockPos pos) {
        var state2 = super.togglePower(state, world, pos);
        getTardis(world).ifPresent(tardis -> controlInput.accept(tardis.getControls(), state2.get(POWERED)));
        return state2;
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
        return Blocks.LEVER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.LEVER.getStateWithProperties(state);
    }
}

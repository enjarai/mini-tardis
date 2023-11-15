package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public class ConsoleButtonBlock extends ButtonBlock implements PolymerBlock, TardisAware {
    private final Block polymerBlock;
    private final BiConsumer<TardisControl, Direction> controlInput;

    public ConsoleButtonBlock(Settings settings, Block polymerBlock, BiConsumer<TardisControl, Direction> controlInput) {
        super(settings, BlockSetType.OAK, 2, true);
        this.polymerBlock = polymerBlock;
        this.controlInput = controlInput;
    }

    @Override
    public void powerOn(BlockState state, World world, BlockPos pos) {
        super.powerOn(state, world, pos);
        getTardis(world).ifPresent(tardis -> controlInput.accept(tardis.getControls(), state.get(FACING)));
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
        return polymerBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return getPolymerBlock(state).getStateWithProperties(state);
    }
}

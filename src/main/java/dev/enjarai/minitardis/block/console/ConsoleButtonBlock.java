package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ConsoleButtonBlock extends ButtonBlock implements PolymerBlock, TardisAware, ConsoleInput {
    private final Block polymerBlock;
    private final BiFunction<TardisControl, Direction, Boolean> controlInput;

    public ConsoleButtonBlock(Settings settings, BlockSetType buttonType, Block polymerBlock, boolean wooden, BiFunction<TardisControl, Direction, Boolean> controlInput) {
        super(settings, buttonType, 2, wooden);
        this.polymerBlock = polymerBlock;
        this.controlInput = controlInput;
    }

    @Override
    public void powerOn(BlockState state, World world, BlockPos pos) {
        super.powerOn(state, world, pos);
        if (!getTardis(world).map(tardis -> controlInput.apply(tardis.getControls(), state.get(FACING))).orElse(false)) {
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
        return polymerBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return getPolymerBlock(state).getStateWithProperties(state);
    }
}

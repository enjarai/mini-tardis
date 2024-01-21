package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ConsoleScreenBlockEntity extends ScreenBlockEntity {
    public ConsoleScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONSOLE_SCREEN_ENTITY, pos, state);
    }

    @Override
    protected Direction getFacing(BlockPos pos, BlockState state) {
        return state.get(ScreenBlock.FACING);
    }
}

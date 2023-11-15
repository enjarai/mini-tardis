package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConsoleScreenBlockEntity extends BlockEntity implements TardisAware {
    public ConsoleScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONSOLE_SCREEN_ENTITY, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state) {

    }
}

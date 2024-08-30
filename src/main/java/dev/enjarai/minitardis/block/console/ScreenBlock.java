package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.item.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class ScreenBlock extends BlockWithEntity implements TardisAware, ConsoleInput {
    public static final BooleanProperty HAS_FLOPPY = BooleanProperty.of("has_floppy");

    protected ScreenBlock(Settings settings) {
        super(settings);
    }

    public boolean trySwitchFloppy(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        if (world.getBlockEntity(pos) instanceof ScreenBlockEntity blockEntity) {
            var handStack = player.getStackInHand(hand);
            var blockStack = blockEntity.inventory.getStack(0);

            if (handStack.isOf(ModItems.FLOPPY) && blockStack.isEmpty()) {
                blockEntity.inventory.setStack(0, handStack.split(1));
                world.setBlockState(pos, state.with(HAS_FLOPPY, true));

                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1, 2);
                return true;
            } else if (handStack.isEmpty() && !blockStack.isEmpty()) {
                player.setStackInHand(hand, blockStack.split(1));
                world.setBlockState(pos, state.with(HAS_FLOPPY, false));

                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1, 1.5f);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
}

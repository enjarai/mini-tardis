package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.component.TardisControl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ConsoleToggleButtonBlock extends ConsoleButtonBlock {
    private final BiFunction<TardisControl, Boolean, Boolean> controlInput;

    public ConsoleToggleButtonBlock(Settings settings, BlockSetType buttonType, BiFunction<TardisControl, Boolean, Boolean> controlInput) {
        super(settings, buttonType, null);
        this.controlInput = controlInput;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var isPowered = !state.get(POWERED);

        if (!isPowered) {
            playClickSound(player, world, pos, false);
        }

        if (!getTardis(world).map(tardis -> controlInput.apply(tardis.getControls(), isPowered)).orElse(false)) {
            inputFailure(world, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, 0);
        } else {
            world.setBlockState(pos, state.with(POWERED, isPowered));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        getTardis(world).ifPresent(tardis -> {
            var shouldBePowered = tardis.getControls().isDestinationLocked();
            if (shouldBePowered != state.get(POWERED)) {
                world.setBlockState(pos, state.with(POWERED, shouldBePowered));
                playClickSound(null, world, pos, shouldBePowered);
            }
        });
        world.scheduleBlockTick(pos, this, 10);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 10);
    }
}

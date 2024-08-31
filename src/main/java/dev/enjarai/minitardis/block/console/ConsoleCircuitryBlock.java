package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Function;

public class ConsoleCircuitryBlock extends Block implements TardisAware, ConsoleInput {
    private final Function<TardisControl, Boolean> controlInput;

    public ConsoleCircuitryBlock(Settings settings, Function<TardisControl, Boolean> controlInput) {
        super(settings);
        this.controlInput = controlInput;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            if (!getTardis(world).map(t -> controlInput.apply(t.getControls())).orElse(false)) {
                inputFailure(world, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0);
            }
            return ActionResult.SUCCESS;
        }
    }
}

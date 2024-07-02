package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.function.Function;

public class ConsoleCircuitryBlock extends Block implements PolymerBlock, TardisAware, ConsoleInput {
    private final Function<TardisControl, Boolean> controlInput;

    public ConsoleCircuitryBlock(Settings settings, Function<TardisControl, Boolean> controlInput) {
        super(settings);
        this.controlInput = controlInput;
    }

    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld || hand == Hand.OFF_HAND) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            if (!getTardis(world).map(t -> controlInput.apply(t.getControls())).orElse(false)) {
                inputFailure(world, hit.getBlockPos(), SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0);
            }
            return ItemActionResult.SUCCESS;
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.DRIPSTONE_BLOCK.getDefaultState();
    }
}

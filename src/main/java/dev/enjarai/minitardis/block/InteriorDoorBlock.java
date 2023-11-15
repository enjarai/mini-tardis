package dev.enjarai.minitardis.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class InteriorDoorBlock extends FacingBlock implements PolymerBlock, TardisAware {
    protected InteriorDoorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        getTardis(world).ifPresent(tardis -> tardis.teleportEntityOut(player));
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.PISTON;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.PISTON.getDefaultState().with(FACING, state.get(FACING));
    }
}

package dev.enjarai.minitardis.block;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings("deprecation")
public class TardisExteriorExtensionBlock extends Block implements PolymerBlock {
    public TardisExteriorExtensionBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !neighborState.isOf(ModBlocks.TARDIS_EXTERIOR) ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()
                && hit.getSide() == world.getBlockState(pos.down()).get(TardisExteriorBlock.FACING)
                && world.getBlockEntity(pos.down()) instanceof TardisExteriorBlockEntity blockEntity) {
            blockEntity.teleportEntityIn(player);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Override
    public Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasPack(player) ? PolymerBlock.super.getPolymerBlock(state, player) : Blocks.LAPIS_BLOCK;
    }
}

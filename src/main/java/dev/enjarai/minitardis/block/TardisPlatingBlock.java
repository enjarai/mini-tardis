package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;

public class TardisPlatingBlock extends Block implements PolymerBlock, PolymerClientDecoded, PolymerKeepModel, PolymerTexturedBlock {
    public TardisPlatingBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return false;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return this.getDefaultState();
        }

        if (PolymerModels.TARDIS_PLATING_STATE != null) {
            return PolymerModels.TARDIS_PLATING_STATE;
        }

        return Blocks.NETHERITE_BLOCK.getDefaultState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.getDefaultState();
    }

    //    @Override
//    public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player) {
//        return !MiniTardis.playerIsRealGamer(player.networkHandler);
//    }

    @Override
    public boolean canSyncRawToClient(ServerPlayerEntity player) {
        return MiniTardis.playerIsRealGamer(player.networkHandler);
    }
}

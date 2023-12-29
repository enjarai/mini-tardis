package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;

public class TardisPlatingBlock extends SimplePolymerBlock implements PolymerClientDecoded, PolymerKeepModel, PolymerTexturedBlock {
    public TardisPlatingBlock(Settings settings) {
        super(settings, Blocks.DEAD_BRAIN_CORAL_BLOCK);
    }

    @Override
    public boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return false;
    }

    @Override
    public Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return this;
        }

        return super.getPolymerBlock(state, player);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return state;
        }

        if (PolymerModels.TARDIS_PLATING_STATE != null) {
            return PolymerModels.TARDIS_PLATING_STATE;
        }

        return super.getPolymerBlockState(state, player);
    }
}

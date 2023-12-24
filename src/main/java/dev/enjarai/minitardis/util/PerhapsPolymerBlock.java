package dev.enjarai.minitardis.util;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.net.HandshakeServer;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PerhapsPolymerBlock extends PolymerBlock, PolymerClientDecoded, PolymerKeepModel {
    @Override
    default Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.HANDSHAKE_SERVER.getHandshakeState(player.networkHandler) == HandshakeServer.HandshakeState.ACCEPTED) {
            return (Block) this;
        }

        return PolymerBlock.super.getPolymerBlock(state, player);
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.HANDSHAKE_SERVER.getHandshakeState(player.networkHandler) == HandshakeServer.HandshakeState.ACCEPTED) {
            return ((Block) this).getStateWithProperties(state);
        }

        return PolymerBlock.super.getPolymerBlockState(state, player);
    }
}

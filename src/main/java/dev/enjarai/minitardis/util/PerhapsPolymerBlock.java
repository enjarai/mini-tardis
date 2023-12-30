package dev.enjarai.minitardis.util;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface PerhapsPolymerBlock extends PolymerBlock, PolymerClientDecoded, PolymerKeepModel {
    @Override
    default Block getPolymerBlock(BlockState state) {
        return (Block) this;
    }

    @Override
    default Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return PolymerBlock.super.getPolymerBlock(state, player);
        }

        return getPerhapsPolymerBlock(state, player);
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        if (MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return PolymerBlock.super.getPolymerBlockState(state, player);
        }

        return getPerhapsPolymerBlockState(state, player);
    }

    @Override
    default boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return !MiniTardis.playerIsRealGamer(player.networkHandler);
    }

    Block getPerhapsPolymerBlock(BlockState state, ServerPlayerEntity player);

    default BlockState getPerhapsPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        return getPerhapsPolymerBlock(state, player).getDefaultState();
    }
}

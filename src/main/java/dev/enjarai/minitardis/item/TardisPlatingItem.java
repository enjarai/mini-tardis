package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class TardisPlatingItem extends BlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel {
    public TardisPlatingItem(Settings settings) {
        super(ModBlocks.TARDIS_PLATING, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (player != null && MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return this;
        }

        return PolymerModels.TARDIS_PLATING_STATE != null ? PolymerModels.TARDIS_PLATING_ITEM.item() : Items.DEAD_BRAIN_CORAL;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (player != null && MiniTardis.playerIsRealGamer(player.networkHandler)) {
            return -1;
        }

        return PolymerModels.TARDIS_PLATING_STATE != null ? PolymerModels.TARDIS_PLATING_ITEM.value() : -1;
    }
}

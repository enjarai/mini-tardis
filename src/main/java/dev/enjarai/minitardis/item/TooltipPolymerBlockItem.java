package dev.enjarai.minitardis.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class TooltipPolymerBlockItem extends TooltipBlockItem implements PolymerItem {
    private final Item polymerItem;

    public TooltipPolymerBlockItem(Block block, Settings settings, Item virtualItem) {
        super(block, settings);
        this.polymerItem = virtualItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerItem;
    }
}

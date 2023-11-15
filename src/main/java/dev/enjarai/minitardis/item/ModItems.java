package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.block.ModBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static void load() {
        ModBlocks.ITEM_BLOCKS.forEach(block -> {
            if (block instanceof PolymerBlock polymerBlock) {
                var id = Registries.BLOCK.getId(block);
                var polymerItem = polymerBlock.getPolymerBlock(block.getDefaultState()).asItem();
                Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new FabricItemSettings(), polymerItem));
            }
        });
    }
}

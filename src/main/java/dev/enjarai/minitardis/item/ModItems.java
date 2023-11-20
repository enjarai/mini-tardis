package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ModItems {

    private static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, MiniTardis.id("item_group"));

    public static void load() {
        ModBlocks.ITEM_BLOCKS.forEach(block -> {
            if (block instanceof PolymerBlock polymerBlock) {
                var id = Registries.BLOCK.getId(block);
                var polymerItem = polymerBlock.getPolymerBlock(block.getDefaultState()).asItem();
                Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new FabricItemSettings(), polymerItem));
            }
        });

        PolymerItemGroupUtils.registerPolymerItemGroup(MiniTardis.id("item_group"),
                Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
                    .icon(() -> ModBlocks.CONSOLE_SCREEN.asItem().getDefaultStack())
                    .displayName(Text.translatable("mini_tardis.item_group"))
                    .entries((context, entries) -> ModBlocks.ITEM_BLOCKS.forEach(entries::add))
                    .build()));
    }
}

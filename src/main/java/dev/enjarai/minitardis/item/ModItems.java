package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ModItems {

    public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, MiniTardis.id("item_group"));

    public static final FezItem FEZ = register("fez", new FezItem(
            new FabricItemSettings().maxCount(1).equipmentSlot((stack) -> EquipmentSlot.HEAD)));
    public static final FloppyItem FLOPPY = register("floppy", new FloppyItem(new FabricItemSettings().maxCount(1)));

    public static final TardisPlatingItem TARDIS_PLATING = register("tardis_plating", new TardisPlatingItem(new FabricItemSettings()));

    public static void load() {
        ModBlocks.ITEM_BLOCKS.forEach((block, modelData) -> {
            var id = Registries.BLOCK.getId(block);
            if (modelData.isPresent()) {
                Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new FabricItemSettings(), modelData.get().item()) {
                    @Override
                    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
                        return modelData.get().value();
                    }
                });
            } else if (block instanceof PolymerBlock polymerBlock) {
                var polymerItem = polymerBlock.getPolymerBlock(block.getDefaultState()).asItem();
                Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new FabricItemSettings(), polymerItem));
            }
        });

        PolymerItemGroupUtils.registerPolymerItemGroup(MiniTardis.id("item_group"),
                Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
                    .icon(() -> ModBlocks.CONSOLE_SCREEN.asItem().getDefaultStack())
                    .displayName(Text.translatable("mini_tardis.item_group"))
                    .entries((context, entries) -> ModBlocks.ITEM_BLOCKS.keySet().forEach(entries::add))
                    .build()));
    }

    public static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, MiniTardis.id(path), item);
    }
}

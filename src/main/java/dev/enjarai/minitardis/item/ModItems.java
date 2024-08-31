package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ModItems {

    public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, MiniTardis.id("item_group"));

    public static final FezItem FEZ = register("fez", new FezItem(
            new Item.Settings().maxCount(1).equipmentSlot((entity, stack) -> EquipmentSlot.HEAD)));
    public static final FloppyItem FLOPPY = register("floppy", new FloppyItem(new Item.Settings().maxCount(1)));
    public static final TardisPlatingItem TARDIS_PLATING = register("tardis_plating", new TardisPlatingItem(new Item.Settings()));
    public static final InteriorLightItem INTERIOR_LIGHT = register("interior_light",
            new InteriorLightItem(ModBlocks.INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem COPPER_INTERIOR_LIGHT = register("copper_interior_light",
            new InteriorLightItem(ModBlocks.COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem EXPOSED_COPPER_INTERIOR_LIGHT = register("exposed_copper_interior_light",
            new InteriorLightItem(ModBlocks.EXPOSED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem WEATHERED_COPPER_INTERIOR_LIGHT = register("weathered_copper_interior_light",
            new InteriorLightItem(ModBlocks.WEATHERED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem OXIDIZED_COPPER_INTERIOR_LIGHT = register("oxidized_copper_interior_light",
            new InteriorLightItem(ModBlocks.OXIDIZED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem WAXED_COPPER_INTERIOR_LIGHT = register("waxed_copper_interior_light",
            new InteriorLightItem(ModBlocks.WAXED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem WAXED_EXPOSED_COPPER_INTERIOR_LIGHT = register("waxed_exposed_copper_interior_light",
            new InteriorLightItem(ModBlocks.WAXED_EXPOSED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem WAXED_WEATHERED_COPPER_INTERIOR_LIGHT = register("waxed_weathered_copper_interior_light",
            new InteriorLightItem(ModBlocks.WAXED_WEATHERED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final InteriorLightItem WAXED_OXIDIZED_COPPER_INTERIOR_LIGHT = register("waxed_oxidized_copper_interior_light",
            new InteriorLightItem(ModBlocks.WAXED_OXIDIZED_COPPER_INTERIOR_LIGHT, new Item.Settings()));
    public static final TardisLodestoneCompassItem TARDIS_LODESTONE_COMPASS = register("tardis_lodestone_compass",
            new TardisLodestoneCompassItem(new Item.Settings()));

    public static void load() {
        ModBlocks.ITEM_BLOCKS.forEach((block, modelData) -> {
            var id = Registries.BLOCK.getId(block);
            if (!Registries.ITEM.containsId(id)) {
                Registry.register(Registries.ITEM, id, new TooltipBlockItem(block, new Item.Settings()));
            }
        });

        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> ModBlocks.CONSOLE_SCREEN.asItem().getDefaultStack())
                .displayName(Text.translatable("mini_tardis.item_group"))
                .entries((context, entries) -> {
                    ModBlocks.ITEM_BLOCKS.keySet().forEach(entries::add);
                    entries.add(TARDIS_PLATING);
                    entries.add(FLOPPY);
                    entries.add(FEZ);
                })
                .build());
    }

    public static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, MiniTardis.id(path), item);
    }
}

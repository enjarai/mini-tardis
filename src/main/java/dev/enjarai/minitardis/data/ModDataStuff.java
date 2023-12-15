package dev.enjarai.minitardis.data;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.item.ModItems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Set;

public class ModDataStuff {
    public static final LootFunctionType RANDOM_APP_LOOT_FUNCTION_TYPE =
            Registry.register(Registries.LOOT_FUNCTION_TYPE, MiniTardis.id("random_app"),
                    new LootFunctionType(RandomAppLootFunction.CODEC));
    public static final Set<Identifier> FLOPPY_LOOT_TABLES = Set.of(
            LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST, LootTables.ANCIENT_CITY_CHEST,
            LootTables.BASTION_TREASURE_CHEST, LootTables.DESERT_PYRAMID_CHEST, LootTables.JUNGLE_TEMPLE_CHEST,
            LootTables.END_CITY_TREASURE_CHEST
    );

    public static void load() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && FLOPPY_LOOT_TABLES.contains(id)) {
                tableBuilder.pool(LootPool.builder()
                        .with(ItemEntry.builder(ModItems.FLOPPY)
                                .conditionally(RandomChanceLootCondition.builder(0.5f))
                                .apply(RandomAppLootFunction::new))
                        .rolls(UniformLootNumberProvider.create(0, 3))
                        .build());
            }
        });
    }
}

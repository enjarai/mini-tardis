package dev.enjarai.minitardis.data;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.Set;

public class ModDataStuff {
    public static final TagKey<Structure> WAYPOINT_APP_RANDOMLY_FOUND_STRUCTURES = TagKey.of(RegistryKeys.STRUCTURE, MiniTardis.id("waypoint_app_randomly_found"));
    public static final LootFunctionType RANDOM_APP_LOOT_FUNCTION_TYPE =
            Registry.register(Registries.LOOT_FUNCTION_TYPE, MiniTardis.id("random_app"),
                    new LootFunctionType(RandomAppLootFunction.CODEC));
    public static final Set<Identifier> FLOPPY_LOOT_TABLES = Set.of(
            LootTables.SIMPLE_DUNGEON_CHEST, LootTables.ABANDONED_MINESHAFT_CHEST, LootTables.ANCIENT_CITY_CHEST,
            LootTables.BASTION_TREASURE_CHEST, LootTables.DESERT_PYRAMID_CHEST, LootTables.JUNGLE_TEMPLE_CHEST,
            LootTables.END_CITY_TREASURE_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST, LootTables.STRONGHOLD_CROSSING_CHEST
    );

    public static void load() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && FLOPPY_LOOT_TABLES.contains(id)) {
                var additionalDims = new ImmutableList.Builder<RegistryKey<World>>();
                if (id.equals(LootTables.STRONGHOLD_CORRIDOR_CHEST) || id.equals(LootTables.STRONGHOLD_CROSSING_CHEST)) {
                    additionalDims.add(World.END);
                }

                tableBuilder.pool(LootPool.builder()
                        .with(ItemEntry.builder(ModItems.FLOPPY)
                                .conditionally(RandomChanceLootCondition.builder(0.5f))
                                .apply(() -> new RandomAppLootFunction(additionalDims.build())))
                        .rolls(UniformLootNumberProvider.create(0, 3))
                        .build());
            }
        });
    }
}

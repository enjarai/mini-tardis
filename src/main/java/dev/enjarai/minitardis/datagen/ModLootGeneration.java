package dev.enjarai.minitardis.datagen;

import dev.enjarai.minitardis.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

public class ModLootGeneration extends SimpleFabricLootTableProvider {

    public ModLootGeneration(FabricDataOutput output) {
        super(output, LootContextTypes.BLOCK);
    }

    @Override
    public void accept(BiConsumer<Identifier, LootTable.Builder> exporter) {
        for (var itemBlock : ModBlocks.ITEM_BLOCKS.keySet()) {
            var itemEntry = ItemEntry.builder(itemBlock);

            if (itemBlock.getStateManager().getProperties().contains(Properties.DOUBLE_BLOCK_HALF)) {
                itemEntry.conditionally(BlockStatePropertyLootCondition.builder(itemBlock)
                        .properties(StatePredicate.Builder.create()
                                .exactMatch(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)));
            }

            exporter.accept(itemBlock.getLootTableId(), new LootTable.Builder()
                    .pool(LootPool.builder()
                            .with(itemEntry)
                    )
            );
        }
    }
}

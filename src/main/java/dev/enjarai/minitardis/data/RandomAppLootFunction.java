package dev.enjarai.minitardis.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.screen.app.*;
import dev.enjarai.minitardis.item.FloppyItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public record RandomAppLootFunction(List<RegistryKey<World>> additionalDimensions) implements LootFunction {
    public static final Codec<RandomAppLootFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.WORLD).listOf().optionalFieldOf("additional_dimensions", List.of()).forGetter(RandomAppLootFunction::additionalDimensions)
    ).apply(instance, RandomAppLootFunction::new));
    public static final List<Identifier> LOOT_APPS = List.of(
            SnakeApp.ID, ScannerApp.ID, BadAppleApp.ID, HistoryApp.ID, DimensionsApp.ID,
            LookAndFeelApp.ID, WaypointsApp.ID
    );

    @Override
    public LootFunctionType getType() {
        return ModDataStuff.RANDOM_APP_LOOT_FUNCTION_TYPE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        var appId = LOOT_APPS.get(Math.abs(lootContext.getRandom().nextInt() % LOOT_APPS.size()));
        var app = ScreenApp.CONSTRUCTORS.get(appId).get();
        app.applyLootModifications(lootContext, this);
        FloppyItem.addApp(stack, app);
        return stack;
    }
}

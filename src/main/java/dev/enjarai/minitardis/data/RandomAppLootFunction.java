package dev.enjarai.minitardis.data;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.component.screen.app.*;
import dev.enjarai.minitardis.item.FloppyItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;

import java.util.List;

public class RandomAppLootFunction implements LootFunction {
    public static final Codec<RandomAppLootFunction> CODEC = Codec.unit(RandomAppLootFunction::new);
    public static final List<Identifier> LOOT_APPS = List.of(
            SnakeApp.ID, ScannerApp.ID, BadAppleApp.ID, HistoryApp.ID, DimensionsApp.ID
    );

    @Override
    public LootFunctionType getType() {
        return ModDataStuff.RANDOM_APP_LOOT_FUNCTION_TYPE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        var appId = LOOT_APPS.get(lootContext.getRandom().nextInt() % LOOT_APPS.size());
        FloppyItem.addApp(stack, ScreenApp.CONSTRUCTORS.get(appId).get());
        return stack;
    }
}

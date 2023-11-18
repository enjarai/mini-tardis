package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class PolymerModels {
    public static final PolymerModelData TARDIS = get("item/tardis");
    public static final PolymerModelData INTERIOR_DOOR = get("item/interior_door");
    public static final PolymerModelData ROTATING_MONITOR = get("item/rotating_monitor");

    private static PolymerModelData get(String modelPath) {
        return PolymerResourcePackUtils.requestModel(Items.LAPIS_LAZULI, MiniTardis.id(modelPath));
    }

    public static ItemStack getStack(PolymerModelData model) {
        var stack = model.item().getDefaultStack();
        stack.getOrCreateNbt().putInt("CustomModelData", model.value());
        return stack;
    }

    public static void load() {
    }
}

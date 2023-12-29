package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

public class PolymerModels {
    public static final PolymerModelData TARDIS = get("item/tardis");
    public static final PolymerModelData[] TARDIS_ALPHA = new PolymerModelData[16];
    public static final PolymerModelData INTERIOR_DOOR = get("item/interior_door");
    public static final PolymerModelData INTERIOR_DOOR_ITEM = get("item/interior_door_item");
    public static final PolymerModelData ROTATING_MONITOR = get("item/rotating_monitor");
    public static final PolymerModelData ROTATING_MONITOR_PACKED = get("item/rotating_monitor_packed");
    public static final PolymerModelData EXTERIOR_DOOR_OPEN = get("item/exterior_door_open");
    public static final PolymerModelData INTERIOR_DOOR_OPEN = get("item/interior_door_open");
    public static final PolymerModelData TARDIS_PLATING_ITEM = get("item/tardis_plating");

    public static final PolymerBlockModel TARDIS_PLATING = PolymerBlockModel.of(MiniTardis.id("block/tardis_plating"));
    @Nullable
    public static final BlockState TARDIS_PLATING_STATE = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, TARDIS_PLATING);


    private static PolymerModelData get(String modelPath) {
        return PolymerResourcePackUtils.requestModel(Items.BARRIER, MiniTardis.id(modelPath));
    }

    public static ItemStack getStack(PolymerModelData model) {
        var stack = model.item().getDefaultStack();
        stack.getOrCreateNbt().putInt("CustomModelData", model.value());
        return stack;
    }

    public static void load() {
        for (int i = 0; i < 16; i++) {
            TARDIS_ALPHA[i] = get("item/tardis_alpha_" + i);
        }
    }
}

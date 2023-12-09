package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.api.PolymerModelData;
import eu.pb4.polymer.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class FezItem extends Item implements PolymerItem {
    public static final PolymerModelData MODEL = PolymerResourcePackUtils.requestModel(Items.RED_DYE, MiniTardis.id("item/fez"));

    public FezItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return MODEL.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return MODEL.value();
    }
}

package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.ModComponents;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.GlobalPos;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// A custom item is needed in order to send the client different position NBT depending on their dimension.
public class TardisLodestoneCompassItem extends CompassItem implements PolymerItem {
    public TardisLodestoneCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COMPASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack realStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        // Interestingly enough the lodestone NBT keys are in PolymerItemUtils.NBT_TO_COPY,
        // so we don't need to copy them manually (or figure out what LODESTONE_TRACKED_KEY even does).
        var polymerStack = PolymerItem.super.getPolymerItemStack(realStack, tooltipType, lookup, player);
        var lodestoneTrackerComponent = realStack.get(DataComponentTypes.LODESTONE_TRACKER);
        if (lodestoneTrackerComponent == null) {
            return polymerStack;
        }
        boolean tracked = lodestoneTrackerComponent.tracked();
        lodestoneTrackerComponent.target().ifPresent(globalPos -> {
            // If the player is in a TARDIS or the compass is not pointing to a TARDIS, return.
            var realDimKey = globalPos.dimension();
            if (player == null ||
                    Objects.equals(player.getWorld().getRegistryKey().getValue().getNamespace(), MiniTardis.MOD_ID) ||
                    !Objects.equals(realDimKey.getValue().getNamespace(), MiniTardis.MOD_ID)) return;
            var tardisID = UUID.fromString(realDimKey.getValue().getPath().replaceAll("tardis/", ""));
            var tardis = ModComponents.TARDIS_HOLDER
                    .get(player.server.getSaveProperties())
                    .getTardis(tardisID)
                    .orElseThrow(() -> new IllegalStateException("Could not find tardis " + tardisID));
            tardis.getCurrentLandedLocation().ifPresent(tardisLocation -> {
                polymerStack.set(DataComponentTypes.LODESTONE_TRACKER,
                        new LodestoneTrackerComponent(Optional.of(new GlobalPos(tardisLocation.worldKey(), tardisLocation.pos())), tracked));
            });
        });
        return polymerStack;
    }
}

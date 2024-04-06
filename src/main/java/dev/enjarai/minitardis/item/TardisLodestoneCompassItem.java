package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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
    public ItemStack getPolymerItemStack(ItemStack realStack, TooltipContext context,
                                         @Nullable ServerPlayerEntity player) {
        // Interestingly enough the lodestone NBT keys are in PolymerItemUtils.NBT_TO_COPY,
        // so we don't need to copy them manually (or figure out what LODESTONE_TRACKED_KEY even does).
        var polymerStack = PolymerItem.super.getPolymerItemStack(realStack, context, player);
        var optionalRealDimKey = World.CODEC.parse(NbtOps.INSTANCE, realStack.getOrCreateNbt().get("LodestoneDimension")).result();
        optionalRealDimKey.ifPresent(realDimKey -> {
            // If the player is in a TARDIS or the compass is not pointing to a TARDIS, return.
            if (player == null
                    || Tardis.isTardis(player.getWorld().getRegistryKey())
                    || !Tardis.isTardis(realDimKey)) return;
            var tardis = Tardis.getTardis(realDimKey, player.server)
                    .orElseThrow(() -> new IllegalStateException("Could not find TARDIS " + player.getWorld().getRegistryKey()));
            tardis.getCurrentLandedLocation().ifPresent(tardisLocation -> {
                polymerStack.getOrCreateNbt().put(LODESTONE_POS_KEY, NbtHelper.fromBlockPos(tardisLocation.pos()));
                World.CODEC.encodeStart(NbtOps.INSTANCE, tardisLocation.worldKey())
                        .resultOrPartial(MiniTardis.LOGGER::error)
                        .ifPresent(nbtElement -> polymerStack.getOrCreateNbt().put(LODESTONE_DIMENSION_KEY, nbtElement));
            });
        });
        return polymerStack;
    }
}

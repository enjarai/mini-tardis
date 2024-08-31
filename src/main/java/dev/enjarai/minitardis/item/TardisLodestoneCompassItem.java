package dev.enjarai.minitardis.item;

import net.minecraft.item.CompassItem;

// A custom item is needed in order to send the client different position NBT depending on their dimension.
public class TardisLodestoneCompassItem extends CompassItem {
    public TardisLodestoneCompassItem(Settings settings) {
        super(settings);
    }

    // TODO
//    @Override
//    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
//        return Items.COMPASS;
//    }
//
//    @Override
//    public ItemStack getPolymerItemStack(ItemStack realStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
//        // Interestingly enough the lodestone NBT keys are in PolymerItemUtils.NBT_TO_COPY,
//        // so we don't need to copy them manually (or figure out what LODESTONE_TRACKED_KEY even does).
//        var polymerStack = PolymerItem.super.getPolymerItemStack(realStack, tooltipType, lookup, player);
//        var component = polymerStack.get(DataComponentTypes.LODESTONE_TRACKER);
//        if (component != null) {
//            var optionalGlobalPos = component.target();
//            optionalGlobalPos.ifPresent(globalPos -> {
//                // If the player is in a TARDIS or the compass is not pointing to a TARDIS, return.
//                if (player == null ||
//                        Objects.equals(player.getWorld().getRegistryKey().getValue().getNamespace(), MiniTardis.MOD_ID) ||
//                        !Objects.equals(globalPos.dimension().getValue().getNamespace(), MiniTardis.MOD_ID)) return;
//                var tardisID = UUID.fromString(globalPos.dimension().getValue().getPath().replaceAll("tardis/", ""));
//                var tardis = ModCCAComponents.TARDIS_HOLDER
//                        .get(player.server.getSaveProperties())
//                        .getTardis(tardisID)
//                        .orElseThrow(() -> new IllegalStateException("Could not find tardis " + tardisID));
//                tardis.getCurrentLandedLocation().ifPresent(tardisLocation -> {
//                    polymerStack.set(DataComponentTypes.LODESTONE_TRACKER,
//                            new LodestoneTrackerComponent(
//                                    Optional.of(new GlobalPos(globalPos.dimension(), tardisLocation.pos())),
//                                    false
//                            )
//                    );
//                });
//            });
//        }
//        return polymerStack;
//    }
}

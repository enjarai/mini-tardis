package dev.enjarai.minitardis.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.item.ModItems;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(CompassItem.class)
public class CompassItemMixin {

    @ModifyArg(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private ItemStack switchToTardisLodestoneCompassWhenNecessary(ItemStack originalStack,
                                                                  @Local(argsOnly = true) ItemUsageContext context) {
        if (Tardis.isTardis(context.getWorld().getRegistryKey()) && context.getPlayer() != null) {
            var newStack = ModItems.TARDIS_LODESTONE_COMPASS.getDefaultStack();
            newStack.setNbt(originalStack.getNbt());
            // Should always be one, but who knows.
            newStack.setCount(originalStack.getCount());
            return newStack;
        }
        return originalStack;
    }

}

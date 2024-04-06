package dev.enjarai.minitardis.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFireBlock.class)
public abstract class AbstractFireBlockMixin {

    @ModifyReturnValue(method = "isOverworldOrNether", at = @At("TAIL"))
    private static boolean allowNetherPortalsInTardii(boolean original, World world) {
        return original || Tardis.isTardis(world.getRegistryKey());
    }

}

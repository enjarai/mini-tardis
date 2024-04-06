package dev.enjarai.minitardis.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract World getWorld();

    @Inject(method = "getTeleportTarget", at = @At("HEAD"), cancellable = true)
    private void checkIfInTardis(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir, @Share("tardisPos") LocalRef<@Nullable BlockPos> tardisPosRef) {
        if (this.getWorld() instanceof ServerWorld serverWorld && Tardis.isTardis(this.getWorld().getRegistryKey())
                && destination.getRegistryKey() == World.NETHER) {
           Tardis.getTardis(serverWorld.getRegistryKey(), serverWorld.getServer())
                   .flatMap(Tardis::getCurrentLandedLocation)
                   .filter(tardisLocation -> tardisLocation.worldKey() == World.OVERWORLD)
                   .ifPresentOrElse(location -> tardisPosRef.set(location.pos()), () -> cir.setReturnValue(null));
        }
    }

    @ModifyExpressionValue(method = "getTeleportTarget",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getX()D"))
    private double changeTeleportTargetXInTardis(double original,
                                                 @Share("tardisPos") LocalRef<@Nullable BlockPos> tardisPosRef) {
        var tardisPos = tardisPosRef.get();
        if (tardisPos != null) {
            return tardisPos.getX();
        }
        return original;
    }

    @ModifyExpressionValue(method = "getTeleportTarget",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getY()D"))
    private double changeTeleportTargetYInTardis(double original,
                                                 @Share("tardisPos") LocalRef<@Nullable BlockPos> tardisPosRef) {
        var tardisPos = tardisPosRef.get();
        if (tardisPos != null) {
            return tardisPos.getY();
        }
        return original;
    }

    @ModifyExpressionValue(method = "getTeleportTarget",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getZ()D"))
    private double changeTeleportTargetZInTardis(double original,
                                                 @Share("tardisPos") LocalRef<@Nullable BlockPos> tardisPosRef) {
        var tardisPos = tardisPosRef.get();
        if (tardisPos != null) {
            return tardisPos.getZ();
        }
        return original;
    }

}

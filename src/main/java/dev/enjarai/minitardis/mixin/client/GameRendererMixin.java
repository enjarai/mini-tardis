package dev.enjarai.minitardis.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.enjarai.minitardis.client.ScreenShake;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera camera;

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"
            )
    )
    private void applyScreenShake(RenderTickCounter tickCounter, CallbackInfo ci, @Local MatrixStack matrices, @Local Entity entity) {
        if (entity instanceof PlayerEntity player) {
            ScreenShake.applyShake(matrices, player, camera.getLastTickDelta());
        }
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"
            )
    )
    private void applyToHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci, @Local MatrixStack matrices) {
        if (MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player) {
            ScreenShake.applyShake(matrices, player, tickDelta);
        }
    }
}

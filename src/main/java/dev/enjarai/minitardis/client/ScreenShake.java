package dev.enjarai.minitardis.client;

import dev.enjarai.minitardis.ModCCAComponents;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ScreenShake {
    public static void applyShake(MatrixStack matrices, PlayerEntity player, float tickDelta) {
        var shakeComponent = ModCCAComponents.SCREEN_SHAKE.get(player);

        var shake = MathHelper.lerp(tickDelta, shakeComponent.prevShake, shakeComponent.shake);
        var intensity = shakeComponent.getShakeIntensity();

        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(getWave(shake, 0) * intensity * 0.005f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(getWave(shake, 2345) * intensity * 0.005f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(getWave(shake, 8673) * intensity * 0.005f));
    }

    private static float getWave(float shake, float offset) {
        var wave1 = Math.sin(shake + offset);
        var wave2 = Math.sin((shake + offset) * 1.43f) * 0.7;
        var wave3 = Math.sin((shake + offset) * 2.57f) * 0.4;
        var wave4 = Math.sin((shake + offset) * 3.14f) * 0.3;

        return (float) (wave1 + wave2 + wave3 + wave4);
    }
}

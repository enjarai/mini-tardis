package dev.enjarai.minitardis.component;

import dev.enjarai.minitardis.ModCCAComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;

public class ScreenShakeComponent implements ClientTickingComponent, AutoSyncedComponent {
    // Synced values
    private float shakeIntensity;
    private float shakeSpeed;

    // Render data
    public float shake;
    public float prevShake;

    private final PlayerEntity player;

    public ScreenShakeComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void clientTick() {
        prevShake = shake;
        if (shakeIntensity > 0) {
            shake += shakeSpeed;
        }
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        shakeIntensity = buf.readFloat();
        shakeSpeed = buf.readFloat();
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeFloat(shakeIntensity);
        buf.writeFloat(shakeSpeed);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // Nuh uh
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // Funny transitive component
    }

    public void setShake(float shakeIntensity, float shakeSpeed) {
        if (shakeIntensity != this.shakeIntensity || shakeSpeed != this.shakeSpeed) {
            this.shakeIntensity = shakeIntensity;
            this.shakeSpeed = shakeSpeed;
            ModCCAComponents.SCREEN_SHAKE.sync(player);
        }
    }

    public float getShakeIntensity() {
        return shakeIntensity;
    }

    public float getShakeSpeed() {
        return shakeSpeed;
    }
}

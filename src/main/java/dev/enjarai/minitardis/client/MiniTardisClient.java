package dev.enjarai.minitardis.client;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.net.HandshakeClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class MiniTardisClient implements ClientModInitializer {
    public static final HandshakeClient<Unit> HANDSHAKE_CLIENT = new HandshakeClient<>(
            Codec.unit(Unit.INSTANCE), MiniTardis.HANDSHAKE_CHANNEL, (unit) -> {});

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TARDIS_EXTERIOR_EXTENSION, RenderLayer.getTranslucent());
    }
}

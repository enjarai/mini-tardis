package dev.enjarai.minitardis.client;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.nbt.NbtInt;

public class MiniTardisClient implements ClientModInitializer {
//    public static final HandshakeClient<Unit> HANDSHAKE_CLIENT = new HandshakeClient<>(
//            Codec.unit(Unit.INSTANCE), MiniTardis.HANDSHAKE_CHANNEL, (unit) -> {});

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TARDIS_EXTERIOR_EXTENSION, RenderLayer.getTranslucent());

        PolymerClientNetworking.setClientMetadata(MiniTardis.HANDSHAKE_CHANNEL, NbtInt.of(0));
    }
}

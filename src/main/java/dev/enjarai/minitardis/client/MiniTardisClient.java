package dev.enjarai.minitardis.client;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.client.render.ConsoleScreenBlockRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class MiniTardisClient implements ClientModInitializer {
//    public static final HandshakeClient<Unit> HANDSHAKE_CLIENT = new HandshakeClient<>(
//            Codec.unit(Unit.INSTANCE), MiniTardis.HANDSHAKE_CHANNEL, (unit) -> {});

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TARDIS_EXTERIOR_EXTENSION, RenderLayer.getTranslucent());

        ClientTickEvents.START_CLIENT_TICK.register(c -> ConsoleScreenBlockRenderer.tick());
        BlockEntityRendererFactories.register(ModBlocks.CONSOLE_SCREEN_ENTITY, ConsoleScreenBlockRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.STATE_COMPARATOR, RenderLayer.getCutout());
    }
}

package dev.enjarai.minitardis.net;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Consumer;

public class HandshakeClient<T> {
    private final Codec<T> transferCodec;
    private final Consumer<T> updateCallback;
    private T serverConfig = null;

    public HandshakeClient(Codec<T> transferCodec, Identifier channel, Consumer<T> updateCallback) {
        this.transferCodec = transferCodec;
        this.updateCallback = updateCallback;

        ClientPlayNetworking.registerGlobalReceiver(channel, (client, handler, buf, responseSender) ->
                responseSender.sendPacket(channel, handleConfigSync(buf)));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    /**
     * Returns the server config if the client has received one for this server,
     * returns an empty optional in any other case.
     */
    public Optional<T> getConfig() {
        return Optional.ofNullable(serverConfig);
    }

    public PacketByteBuf handleConfigSync(PacketByteBuf buf) {
        var data = buf.readNbt();
        try {
            serverConfig = transferCodec.parse(NbtOps.INSTANCE, data)
                    .getOrThrow(false, MiniTardis.LOGGER::error);
        } catch (RuntimeException e) {
            serverConfig = null;
            MiniTardis.LOGGER.error("Failed to parse config from server", e);
        }

        if (serverConfig != null) {
            updateCallback.accept(serverConfig);
            MiniTardis.LOGGER.info("Received config from server");
        }

        var returnBuf = new PacketByteBuf(Unpooled.buffer());
        returnBuf.writeBoolean(serverConfig != null);
        return returnBuf;
    }

    public void reset() {
        serverConfig = null;
    }
}

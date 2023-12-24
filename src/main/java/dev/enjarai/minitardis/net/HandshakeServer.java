package dev.enjarai.minitardis.net;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class HandshakeServer<T> {
    private final Codec<T> transferCodec;
    private final Supplier<T> configSupplier;
    private final Map<ServerPlayNetworkHandler, HandshakeState> syncStates = new WeakHashMap<>();

    public HandshakeServer(Codec<T> transferCodec, Identifier channel, Supplier<T> configSupplier) {
        this.transferCodec = transferCodec;
        this.configSupplier = configSupplier;

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, channel,
                    (server1, player, handler1, buf, responseSender) -> clientReplied(handler1, buf));

            ServerPlayNetworking.send(handler.getPlayer(), channel, getConfigSyncBuf(handler));

            configSentToClient(handler);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerDisconnected(handler));
    }

    public HandshakeState getHandshakeState(ServerPlayNetworkHandler player) {
        return syncStates.getOrDefault(player, HandshakeState.NOT_SENT);
    }

    public PacketByteBuf getConfigSyncBuf(ServerPlayNetworkHandler player) {
        var buf = new PacketByteBuf(Unpooled.buffer());

        var config = configSupplier.get();
        var data = transferCodec.encodeStart(NbtOps.INSTANCE, config);
        try {
            buf.writeNbt((NbtCompound) data.getOrThrow(false, MiniTardis.LOGGER::error));
        } catch (RuntimeException e) {
            MiniTardis.LOGGER.error("Failed to encode config", e);
            buf.writeNbt(new NbtCompound());
        }

        return buf;
    }

    public void configSentToClient(ServerPlayNetworkHandler player) {
        syncStates.put(player, HandshakeState.SENT);
    }

    public HandshakeState clientReplied(ServerPlayNetworkHandler player, PacketByteBuf buf) {
        var state = getHandshakeState(player);

        if (state == HandshakeState.SENT) {
            if (buf.readBoolean()) {
                syncStates.put(player, HandshakeState.ACCEPTED);
                MiniTardis.LOGGER.info("Client of {} accepted server config.", player.getPlayer().getName().getString());
                return HandshakeState.ACCEPTED;
            } else {
                syncStates.put(player, HandshakeState.FAILED);
                MiniTardis.LOGGER.warn(
                        "Client of {} failed to process server config, check client logs find what went wrong.",
                        player.getPlayer().getName().getString());
                return HandshakeState.FAILED;
            }
        }

        return state;
    }

    public void playerDisconnected(ServerPlayNetworkHandler player) {
        syncStates.remove(player);
    }

    public enum HandshakeState {
        NOT_SENT,
        SENT,
        ACCEPTED,
        FAILED
    }
}
